package com.yz.myimageloader

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_main.*
/**
 * A placeholder fragment containing a simple view.
 */
class MainActivityFragment : Fragment() {

    var mAdapter: ImageAdapter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (null == mAdapter) {
            mAdapter = ImageAdapter(createUriList(), activity!!)
        }
        gridView.adapter = mAdapter
    }

    fun createUriList(): ArrayList<String>{
        val uris = ArrayList<String>()
        uris.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1533800119348&di=8dd0fb073fa460b70ec0d5136962f9d4&imgtype=0&src=http%3A%2F%2Fs8.sinaimg.cn%2Fmiddle%2F8ee3e0acxb0171b491f27%26690")
        uris.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1533800157515&di=97a102e8069fb131379b0a89d2c9152d&imgtype=0&src=http%3A%2F%2Fphotocdn.sohu.com%2F20150907%2Fmp30906533_1441629699374_2.jpeg")
        uris.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1533800185387&di=9c28f662f98e2493733a8a0424fe8e0e&imgtype=0&src=http%3A%2F%2Fimg3.cache.netease.com%2Fphoto%2F0003%2F2015-03-20%2F600x450_AL5EDPNI00AJ0003.jpg")
        uris.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1533800208364&di=68f6db9894e5280df04a3a70ed06ac01&imgtype=0&src=http%3A%2F%2Fforum.xitek.com%2F201110%2F16883%2F1688347%2F1688347_1317555960.jpg")
        uris.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1533800247857&di=cf2b391498aa4e63f6f5542d8a39165b&imgtype=0&src=http%3A%2F%2Fc1.hoopchina.com.cn%2Fuploads%2Fstar%2Fevent%2Fimages%2F140703%2F5976afee7ae2262831c1ba7562f6e45d2e0c18e0.jpg")
        uris.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1533800279200&di=4866c62dbc534d594ddacd294334645b&imgtype=0&src=http%3A%2F%2Fi10.hoopchina.com.cn%2Fhupuapp%2Fbbs%2F186099152551054%2Fthread_186099152551054_20180621201153_s_122788_w_720_h_1018_54081.jpg%3Fx-oss-process%3Dimage%2Fresize%2Cw_800%2Fformat%2Cjpg")
        uris.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1534395033&di=04a10516529eabf2d6e247fbda11e466&imgtype=jpg&er=1&src=http%3A%2F%2Fi3.hoopchina.com.cn%2Fblogfile%2F201402%2F19%2FBbsImg139281710160564_750%2A422.jpg")
        uris.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1534395057&di=2b742c5c4288fcec149e551d186e2dc6&imgtype=jpg&er=1&src=http%3A%2F%2Fc2.hoopchina.com.cn%2Fuploads%2Fstar%2Fevent%2Fimages%2F130630%2F42c8774dfef335e8c9ad441db31efaffb6c909a3.jpg")
        uris.add("https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=3391186998,1087623237&fm=15&gp=0.jpg")
        uris.add("https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=2982208248,169011994&fm=15&gp=0.jpg")

        uris.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1533800119348&di=8dd0fb073fa460b70ec0d5136962f9d4&imgtype=0&src=http%3A%2F%2Fs8.sinaimg.cn%2Fmiddle%2F8ee3e0acxb0171b491f27%26690")
        uris.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1533800157515&di=97a102e8069fb131379b0a89d2c9152d&imgtype=0&src=http%3A%2F%2Fphotocdn.sohu.com%2F20150907%2Fmp30906533_1441629699374_2.jpeg")
        uris.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1533800185387&di=9c28f662f98e2493733a8a0424fe8e0e&imgtype=0&src=http%3A%2F%2Fimg3.cache.netease.com%2Fphoto%2F0003%2F2015-03-20%2F600x450_AL5EDPNI00AJ0003.jpg")
        uris.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1533800208364&di=68f6db9894e5280df04a3a70ed06ac01&imgtype=0&src=http%3A%2F%2Fforum.xitek.com%2F201110%2F16883%2F1688347%2F1688347_1317555960.jpg")
        uris.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1533800247857&di=cf2b391498aa4e63f6f5542d8a39165b&imgtype=0&src=http%3A%2F%2Fc1.hoopchina.com.cn%2Fuploads%2Fstar%2Fevent%2Fimages%2F140703%2F5976afee7ae2262831c1ba7562f6e45d2e0c18e0.jpg")
        uris.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1533800279200&di=4866c62dbc534d594ddacd294334645b&imgtype=0&src=http%3A%2F%2Fi10.hoopchina.com.cn%2Fhupuapp%2Fbbs%2F186099152551054%2Fthread_186099152551054_20180621201153_s_122788_w_720_h_1018_54081.jpg%3Fx-oss-process%3Dimage%2Fresize%2Cw_800%2Fformat%2Cjpg")
        uris.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1534395033&di=04a10516529eabf2d6e247fbda11e466&imgtype=jpg&er=1&src=http%3A%2F%2Fi3.hoopchina.com.cn%2Fblogfile%2F201402%2F19%2FBbsImg139281710160564_750%2A422.jpg")
        uris.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1534395057&di=2b742c5c4288fcec149e551d186e2dc6&imgtype=jpg&er=1&src=http%3A%2F%2Fc2.hoopchina.com.cn%2Fuploads%2Fstar%2Fevent%2Fimages%2F130630%2F42c8774dfef335e8c9ad441db31efaffb6c909a3.jpg")
        uris.add("https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=3391186998,1087623237&fm=15&gp=0.jpg")
        uris.add("https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=2982208248,169011994&fm=15&gp=0.jpg")
        return uris
    }
}
