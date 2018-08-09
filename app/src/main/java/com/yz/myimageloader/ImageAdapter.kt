package com.yz.myimageloader

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

class ImageAdapter(uris: ArrayList<String>, context: Context): BaseAdapter() {
    var uriList = uris
    var mContext = context
    private val mImageLoader = ImageLoader.build(context)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var viewHolder: ViewHolder
        var itemView: View? = convertView
        if (null == itemView) {
            viewHolder = ViewHolder()
            itemView = LayoutInflater.from(mContext).inflate(R.layout.grid_itme_layout, parent, false)
            viewHolder.imageView = itemView.findViewById(R.id.image)
            itemView.tag = viewHolder
        } else {
            viewHolder = itemView.tag as ViewHolder
        }
        val tag = viewHolder.imageView!!.tag
        val uri = getItem(position).toString()
        if (uri != tag) {
            viewHolder.imageView?.setImageResource(R.mipmap.ic_launcher)
        }
        viewHolder.imageView?.tag = uri

        mImageLoader.bindBitmap(uri, viewHolder.imageView!!, viewHolder.imageView!!.width, viewHolder.imageView!!.height)

        return itemView!!
    }

    override fun getItem(position: Int): Any = uriList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = uriList.size

    inner class ViewHolder {
        var imageView: ImageView? = null
    }
}