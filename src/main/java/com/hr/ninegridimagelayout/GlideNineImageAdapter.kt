package com.hr.ninegridimagelayout

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.XPopupImageLoader
import java.io.File

open class GlideNineImageAdapter(context: Context, imageInfoList: List<NineGridImageInfo>) : NineGridImageAdapter(context, imageInfoList) {

    companion object{
        var LONG_TAG_PIC_HEIGHT_SCALE = 3f
    }

    private val glideCacheAllOptions: RequestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)

    private fun loadGif(imageView: ImageView, position: Int, url: String){
        val requestManager = Glide.with(imageView).setDefaultRequestOptions(glideCacheAllOptions)
        //gif 先用 bitmap 加载第一帧，再加载全部
        var asBitmap = requestManager.asBitmap()
        if(getImageInfoList()?.size == 1 && position == 0){
            asBitmap = asBitmap.override(getImageInfoList()!![position].width,getImageInfoList()!![position].height)
        }
        asBitmap.load(url).addListener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean
            ): Boolean {
                imageView.post {
                    var asGif = requestManager.asGif()
                    if(getImageInfoList()?.size == 1 && position == 0){
                        asGif = asGif.override(getImageInfoList()!![position].width,getImageInfoList()!![position].height)
                    }
                    asGif.load(url).into(imageView)
                }
                return true
            }

            override fun onResourceReady(
                resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean
            ): Boolean {
                if(getImageInfoList()!![position].width <= 0) {
                    resource?.let {
                        if(getImageInfoList()?.size == 1 && position == 0) {
                            imageView.layoutParams.width = resource.width
                            imageView.layoutParams.height = resource.height
                        }
                        if(resource.height/resource.width > LONG_TAG_PIC_HEIGHT_SCALE){
                            (imageView as? NineGridImageView)?.setTag("长图")
                        }
                    }
                }
                imageView.setImageBitmap(resource)
                imageView.post {
                    var asGif = requestManager.asGif()
                    if(getImageInfoList()?.size == 1 && position == 0){
                        if(getImageInfoList()!![position].width <= 0) {
                            resource?.let {
                                asGif = asGif.override(resource.width, resource.height)
                            }
                        }else {
                            asGif = asGif.override(getImageInfoList()!![position].width, getImageInfoList()!![position].height)
                        }
                    }
                    asGif.load(url).into(imageView)
                }
                return true
            }
        }).into(imageView)
    }

    // 建议业务层重写
    override fun loadImage(imageView: ImageView, position: Int, url: String) {
//        glide4不再支持加载第一帧
//        if(url.endsWith(".gif", true)){
//            loadGif(imageView, position, url)
//            return
//        }
        // 使用SIZE_ORIGINAL就不用再xpopup里面折腾获取原图了，但是性能不如自己修改的好（使不得，加载大图会崩），自己考虑吧
        if(getImageInfoList()!![position].width > 0){
            // 不知道为什么一张图片显示模糊，override就可以了
            if(getImageInfoList()?.size == 1 && position == 0){
                Glide.with(imageView.context).load(url)
                    .override(getImageInfoList()!![position].width,getImageInfoList()!![position].height)
                    .into(imageView)
                return
            }
            Glide.with(imageView.context).load(url)
//                .apply(RequestOptions().override(Target.SIZE_ORIGINAL))
                .into(imageView)
        }else {
            Glide.with(imageView.context).asBitmap().load(url)
//                .apply(RequestOptions().override(Target.SIZE_ORIGINAL))
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        if(getImageInfoList()?.size == 1 && position == 0) {
                            imageView.layoutParams.width = resource.width
                            imageView.layoutParams.height = resource.height
                        }
                        imageView.setImageBitmap(resource)
                        if(resource.height/resource.width > LONG_TAG_PIC_HEIGHT_SCALE){
                            (imageView as? NineGridImageView)?.setTag("长图")
                        }
                    }
                })
        }
    }

    override fun onImageItemClick(
        context: Context,
        gridView: NineGridImageLayout,
        position: Int,
        imageInfoList: List<NineGridImageInfo>?
    ) {
        XPopup.Builder(context).asImageViewer(gridView.getChildAt(position) as ImageView, position, imageInfoList?.map { it.url }?.toList(),
            { popupView, updatePosition -> popupView.updateSrcView(gridView.getChildAt(updatePosition) as? ImageView) },
            object : XPopupImageLoader {
                override fun loadImage(position: Int, uri: Any, imageView: ImageView) {
                    Glide.with(context).load(uri).into(imageView)
                }

                override fun getImageFile(context: Context, uri: Any): File? {
                    try {
                        return Glide.with(context).downloadOnly().load(uri).submit().get()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return null
                }
            }
        ).show()
    }
}