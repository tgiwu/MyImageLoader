package com.yz.myimageloader

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import android.util.Log
import android.util.LruCache
import android.widget.ImageView
import com.jakewharton.disklrucache.DiskLruCache
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ImageLoader(context: Context) {
    companion object {
        private const val TAG = "ImageLoader"
        const val MESSAGE_POST_RESULT = 1
        private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
        private val CORE_POOL_SIZE = CPU_COUNT + 1
        private val MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1
        private const val KEEP_ALIVE = 10L
        const val TAG_KEY_URI = R.id.action_image
        const val DISK_CACHE_SIZE : Long = 1024 * 1024 * 50
        const val IO_BUFFER_SIZE = 8 * 1024
        var DISK_CACHE_INDEX = 0
        var mIsDiskLRUCacheCreate = false
        private val sThreadFactory = object :ThreadFactory {
            val mCount: AtomicInteger = AtomicInteger(1)
            override fun newThread(r: Runnable?): Thread {
                return Thread(r, "#ImageLoader# ${mCount.getAndIncrement()}")
            }
        }
        val THREAD_POOL_EXECUTOR = ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE, TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>(), sThreadFactory)

        fun build(context: Context) : ImageLoader {
            return ImageLoader(context)
        }

        val mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message?) {
                val result = msg?.obj as ImageLoader.LoaderResult
                result?.let {
                    val imageView = it.imageView
                    imageView.setImageBitmap(it.bitmap)
                    val uri = imageView.getTag(TAG_KEY_URI)
                    if (uri == it.uri) {
                        imageView.setImageBitmap(it.bitmap)
                    } else {
                        Log.w(TAG, "set image bitmap, but url has changed, ignored")
                    }
                }
            }
        }
    }

    private val  mImageResizer = ImageResizer()
   var mMemoryCache: LruCache<String, Bitmap>
    var mDiskLruCache: DiskLruCache? = null
    private var mContext: Context = context

    init {
        val maxMemory = Runtime.getRuntime().maxMemory() / 1024L
        val cacheSize = (maxMemory / 8L).toInt()
        mMemoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String?, value: Bitmap?): Int {
                return value!!.rowBytes * value.height / 1024
            }
        }
        val diskCacheDir = getDiskCacheDir(context, "BitmapLoaderCache")
        Log.i(TAG, "disk dir: $diskCacheDir")
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdir()
        }
        if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE)
                mIsDiskLRUCacheCreate = true
            } catch (e : IOException) {
                e.printStackTrace()
            }
        }
    }

    fun bindBitmap(uri: String, imageView: ImageView) {
        bindBitmap(uri, imageView, 0, 0)
    }

    fun bindBitmap(uri: String, imageView: ImageView, reqWidth: Int, reqHeight: Int) {
        imageView.setTag(TAG_KEY_URI, uri)
        val bitmap = loadBitmapFromMomeryCache(uri)
        bitmap?.let {
            imageView.setImageBitmap(bitmap)
            return
        }

        val loadBitmapRunnable = Runnable {
            var bm = loadBitmap(uri, reqWidth, reqHeight)
            bm?.let {
                val result = LoaderResult(imageView, uri, it)
                mHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget()
            }
        }
        THREAD_POOL_EXECUTOR.execute(loadBitmapRunnable)

    }

    fun loadBitmap(uri: String, reqWidth: Int,reqHeight: Int) :Bitmap?{
        var bitmap = loadBitmapFromMomeryCache(uri)
        bitmap?.let {
            Log.i(TAG, "loadBitmapFromMemoryCache, url: $uri")
            return it
        }

        try {
            bitmap = loadBitmapFromDiskCache(uri, reqWidth, reqHeight)
            bitmap?.let {
                Log.i(TAG, "loadBitmapFromDisk, url:  $uri")
                return it
            }
            bitmap = loadBitmapFromHttp(uri, reqWidth, reqHeight)
            Log.w(TAG, "loadBitmapFromHttp, url: $uri")
        } catch (e : IOException) {
            e.printStackTrace()
        }

        if (bitmap == null && !mIsDiskLRUCacheCreate) {
            Log.w(TAG, "encounter error, DiskLruCache is not created")
            bitmap = downloadBitmapFromUrl(uri)
        }
        return bitmap
    }

    fun loadBitmapFromMomeryCache(urlString: String) : Bitmap?  =
         getBitmapFromMemoryCache(hashKeyFromUrl(urlString))


    fun loadBitmapFromHttp(urlString: String, reqWidth: Int, reqHeight: Int) : Bitmap?{
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw RuntimeException("can not visit network from UI Thread")
        }
        if (mDiskLruCache == null) return null

        val key = hashKeyFromUrl(urlString)
        val editor = mDiskLruCache?.edit(key)
        editor?.let {
            val outputStream = it.newOutputStream(DISK_CACHE_INDEX)
            if (downloadUrlToStream(urlString, outputStream)) {
                editor.commit()
            } else {
                editor.abort()
            }
            mDiskLruCache?.flush()
        }
        return loadBitmapFromDiskCache(urlString, reqWidth, reqHeight)
    }

    fun addBitmapToMemoryCache(key : String, bitmap: Bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap)
        }
    }

    fun getBitmapFromMemoryCache(key: String) : Bitmap?{
        return mMemoryCache.get(key)
    }

    fun loadBitmapFromDiskCache(urlString: String, reqWidth: Int, reqHeight: Int) :Bitmap? {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.w(TAG, "load bitmap from UI thread, it's not recommended")
        }
        if (mDiskLruCache == null) return null

        var bitmap: Bitmap? = null
        val key: String = hashKeyFromUrl(urlString)
        val snapShot: DiskLruCache.Snapshot? = mDiskLruCache?.get(key)
        snapShot?.let {
            val fileInputStream = it.getInputStream(DISK_CACHE_INDEX) as FileInputStream
            val fileDescriptor = fileInputStream.fd
            bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor, reqWidth, reqHeight)
            bitmap?.let {
                addBitmapToMemoryCache(key, it)
            }
        }
        return bitmap
    }

    fun downloadUrlToStream(urlString: String, outputStream: OutputStream) : Boolean {
        var urlConnection: HttpURLConnection? = null
        var bos : BufferedOutputStream? = null
        var bis : BufferedInputStream? = null
        try {
            val url = URL(urlString)
            urlConnection = url.openConnection() as HttpURLConnection
            bis = BufferedInputStream(urlConnection.inputStream, IO_BUFFER_SIZE)
            bos = BufferedOutputStream(outputStream, IO_BUFFER_SIZE)

            var b: Int
            while (true) {
                b = bis.read()
                if (b != -1) {
                    bos.write(b)
                } else {
                    break
                }
            }
            return true
        } catch (e : IOException) {
            e.printStackTrace()
        } finally {
            urlConnection?.disconnect()
            bis?.close()
            bos?.close()
        }
        return false
    }

    fun downloadBitmapFromUrl(urlString: String):Bitmap? {
        var bitmap:Bitmap? = null
        var urlConnection : HttpURLConnection? = null
        var bis :BufferedInputStream? = null
        try {
            val url = URL(urlString)
            urlConnection = url.openConnection() as HttpURLConnection
            bis = BufferedInputStream(urlConnection.inputStream, IO_BUFFER_SIZE)
            bitmap = BitmapFactory.decodeStream(bis)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            urlConnection?.disconnect()
            bis?.close()
        }
        return bitmap
    }

    fun hashKeyFromUrl(url :String) : String{
        val  cacheKey: String
        cacheKey = try {
            val mDigest = MessageDigest.getInstance("MD5")
            mDigest.update(url.toByteArray())
            bytesToHexString(mDigest.digest())
        } catch (e : NoSuchAlgorithmException) {
            url.hashCode().toString()
        }
        return cacheKey
    }

    fun bytesToHexString(byteArray: ByteArray) : String{
        val sb= StringBuilder()
        val size = byteArray.size
        for (i in byteArray.indices){
            var hex = Integer.toHexString (0xFF and byteArray[i].toInt())
            if (hex.length == 1) {
                sb.append('0')
            }
            sb.append(hex)
        }
        return sb.toString()
    }

    fun getDiskCacheDir(context: Context, uniqueName: String) : File {
        val externalStorageAvailable = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        val cachePath = if (externalStorageAvailable) {
            context.externalCacheDir.path
        } else {
            context.cacheDir.path
        }
        return File(cachePath + File.separator + uniqueName)
    }

    fun getUsableSpace(path: File) : Long{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ) {
            return path.usableSpace
        }
        val stats = StatFs(path.path)
        return stats.blockSizeLong * stats.availableBlocksLong
    }

    inner class ImageResizer {
        private val TAG = "ImageResizer"

        fun decodeSampledBitmapFromResource(res: Resources, resId: Int, reqWidth: Int, reqHeight: Int) : Bitmap{
            val option = BitmapFactory.Options()
            option.inJustDecodeBounds = true
            BitmapFactory.decodeResource(res, resId, option)

            option.inSampleSize = calculateInSampleSize(option, reqHeight, reqWidth)

            option.inJustDecodeBounds = false

            return BitmapFactory.decodeResource(res, resId, option)
        }

        fun decodeSampledBitmapFromFileDescriptor( fileDescriptor: FileDescriptor, reqWidth: Int, reqHeight: Int) : Bitmap? {
            val option = BitmapFactory.Options()
            option.inJustDecodeBounds = true
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, option)
            option.inSampleSize = calculateInSampleSize(option, reqHeight, reqWidth)
            option.inJustDecodeBounds = false
            return BitmapFactory.decodeFileDescriptor(fileDescriptor, null , option)
        }

        private fun calculateInSampleSize(option: BitmapFactory.Options, reqHeight: Int, reqWidth: Int) : Int{
            if (0 == reqHeight || 0 == reqWidth) {
                return 1
            }

            val height = option.outHeight
            val width = option.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            Log.i(TAG, "sampleSize is $inSampleSize")
            return inSampleSize
        }
    }

     data class LoaderResult(var imageView: ImageView, var uri: String, var bitmap: Bitmap)
}