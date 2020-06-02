package com.hr.ninegridimagelayout

import android.content.Context
import android.widget.ImageView

abstract class NineGridImageAdapter(private var context: Context, private var imageInfoList: List<NineGridImageInfo>?) {

    /**
     * 如果要实现图片点击的逻辑，重写此方法即可
     *
     * @param context 上下文
     * @param gridView 九宫格控件
     * @param position 当前点击图片的的索引
     * @param urls 图片地址的数据集合
     */
    open fun onImageItemClick(context: Context, gridView: NineGridImageLayout, position: Int, imageInfoList: List<NineGridImageInfo>?) {
    }

    abstract fun loadImage(imageView: ImageView, position: Int, url: String)

    /**
     * 生成ImageView容器的方式，默认使用NineGridImageView类
     * 如果需要自定义图片展示效果，重写此方法即可
     *
     * @param context 上下文
     * @return 生成的 ImageView
     */
    open fun generateImageView(context: Context): ImageView {
        val imageView = NineGridImageView(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageResource(R.drawable.gradient_mask)
        return imageView
    }

    open fun getImageInfoList(): List<NineGridImageInfo>? = imageInfoList
}