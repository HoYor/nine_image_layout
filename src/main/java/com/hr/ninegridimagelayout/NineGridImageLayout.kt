package com.hr.ninegridimagelayout

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import java.lang.RuntimeException
import kotlin.math.min


class NineGridImageLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int): ViewGroup(context, attrs, defStyleAttr) {
    companion object{
        const val MODE_FILL = 0 //填充模式，类似于微信
        const val MODE_GRID = 1 //网格模式，类似于QQ，4张图会 2X2布局
    }
    private var singleImageSize = 250 // 单张图片时的大小,单位dp
    private var singleImageRatio = 0.5f // 单张图片的宽高比(宽/高)
    private var maxImageCount = 9 // 最大显示的图片数
    private var gridSpacing = 3 // 宫格间距，单位dp
    private var mode = MODE_FILL // 默认使用fill模式
    private var columnCount = 0 // 列数
    private var rowCount = 0 // 行数
    private var gridWidth = 0 // 宫格宽度
    private var gridHeight = 0 // 宫格高度
    private var longPictureScale = 3f // 长图的标准，是宽度的多少倍
    private var imageViews: MutableList<ImageView>? = null
    private var mImageInfoList: List<NineGridImageInfo>? = null
    private var mAdapter: NineGridImageAdapter? = null

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    init {
        val dm: DisplayMetrics = context.resources.displayMetrics
        gridSpacing = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, gridSpacing.toFloat(), dm).toInt()
        singleImageSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,singleImageSize.toFloat(),dm).toInt()
        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.NineGridImageLayout)
        longPictureScale = a.getFloat(R.styleable.NineGridImageLayout_ngl_longPictureHeightScale, longPictureScale)
        gridSpacing = a.getDimension(R.styleable.NineGridImageLayout_ngl_gridSpacing, gridSpacing.toFloat()).toInt()
        singleImageSize = a.getDimension(R.styleable.NineGridImageLayout_ngl_singleImageSize, singleImageSize.toFloat()).toInt()
        singleImageRatio = a.getFloat(R.styleable.NineGridImageLayout_ngl_singleImageRatio, singleImageRatio)
        maxImageCount = a.getInt(R.styleable.NineGridImageLayout_ngl_maxSize, maxImageCount)
        mode = a.getInt(R.styleable.NineGridImageLayout_ngl_mode, mode)
        a.recycle()
        imageViews = ArrayList()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        val totalWidth = width - paddingLeft - paddingRight
        if (mImageInfoList != null && mImageInfoList!!.isNotEmpty()) {
            if (mImageInfoList!!.size == 1) {
                val view: NineGridImageView = getChildAt(0) as NineGridImageView
//                view.maxHeight = (singleImageSize / singleImageRatio).toInt()
                val imageInfo = mImageInfoList?.get(0)
                if(imageInfo!!.width > 0 && imageInfo.height > 0){
                    val imageRatio: Float = imageInfo.width*1f/imageInfo.height
//                    if(imageRatio > singleImageRatio){
                        gridWidth = singleImageSize
                        gridHeight = (gridWidth / imageRatio).toInt()
//                    }else{
//                        gridHeight = simpleMaxHeight
//                        gridWidth = (gridHeight * imageRatio).toInt()
//                    }
                }else{
                    gridWidth = min(view.measuredWidth, singleImageSize)
                    gridHeight = min(view.measuredHeight, (singleImageSize / singleImageRatio).toInt())
                }
            } else {
                // gridWidth = gridHeight = (totalWidth - gridSpacing * (columnCount - 1)) / columnCount;
                // 这里无论是几张图片，宽高都按总宽度的 1/3
                gridHeight = (totalWidth - gridSpacing * 2) / 3
                gridWidth = gridHeight
            }
            width = gridWidth * columnCount + gridSpacing * (columnCount - 1) + paddingLeft + paddingRight
            val height = gridHeight * rowCount + gridSpacing * (rowCount - 1) + paddingTop + paddingBottom
            setMeasuredDimension(width, height)
        }
    }

    override fun onLayout(changed: Boolean,l: Int,t: Int,r: Int,b: Int) {
        if (mImageInfoList == null) return
        val childrenCount = mImageInfoList!!.size
        for (i in 0 until childrenCount) {
            val childrenView: ImageView = getChildAt(i) as ImageView
            val rowNum = i / columnCount
            val columnNum = i % columnCount
            val left = (gridWidth + gridSpacing) * columnNum + paddingLeft
            val top = (gridHeight + gridSpacing) * rowNum + paddingTop
            val right = left + gridWidth
            val bottom = top + gridHeight
            if(childrenCount == 1){
                Log.d("Debug","rect:$left-$top-$right-$bottom")
            }
            childrenView.layout(left, top, right, bottom)
        }
    }

    /**
     * 设置适配器
     */
    fun setAdapter(adapter: NineGridImageAdapter) {
        mAdapter = adapter
        var imageInfoList: List<NineGridImageInfo>? = adapter.getImageInfoList()
        if (imageInfoList == null || imageInfoList.isEmpty()) {
            visibility = View.GONE
            return
        } else {
            visibility = View.VISIBLE
        }
        var imageCount = imageInfoList.size
        if (maxImageCount in 1 until imageCount) {
            imageInfoList = imageInfoList.subList(0, maxImageCount)
            imageCount = imageInfoList.size //再次获取图片数量
        }
        //默认是3列显示，行数根据图片的数量决定
        rowCount = imageCount / 3 + if (imageCount % 3 == 0) 0 else 1
        columnCount = 3
        //grid模式下，显示4张使用2X2模式
        if (mode == MODE_GRID) {
            if (imageCount == 4) {
                rowCount = 2
                columnCount = 2
            }
        }
        //保证View的复用，避免重复创建
        if (mImageInfoList == null) {
            for (i in 0 until imageCount) {
                val iv: ImageView = getImageView(i) ?: return
                if(i == 0 && imageCount == 1){
                    iv.scaleType = ImageView.ScaleType.FIT_XY
                }else{
                    iv.scaleType = ImageView.ScaleType.CENTER_CROP
                }
                addView(iv, generateDefaultLayoutParams())
            }
        } else {
            val oldViewCount = mImageInfoList!!.size
            val newViewCount = imageCount
            // 要刷新复用的ImageView
            for (i in 0 until min(oldViewCount,newViewCount)){
                (getChildAt(i) as? NineGridImageView)?.let {
                    it.clearView()
                    if(i == 0 && newViewCount == 1){
                        it.scaleType = ImageView.ScaleType.FIT_XY
                    }else{
                        it.scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                    mAdapter!!.getImageInfoList()?.get(i)?.let { imageInfo->
                        if(imageInfo.type == NineGridImageInfo.IMAGE_TYPE_GIF){
                            it.setTag("GIF")
                        }else if(imageInfo.height/imageInfo.width >= longPictureScale){
                            it.setTag("长图")
                        }
                        mAdapter!!.loadImage(it, i, imageInfo.url)
                    }
                }
            }
            if (oldViewCount > newViewCount) {
                removeViews(newViewCount, oldViewCount - newViewCount)
            } else {
                for (i in oldViewCount until newViewCount) {
                    val iv: ImageView = getImageView(i) ?: return
                    addView(iv, generateDefaultLayoutParams())
                }
            }
        }
        //修改最后一个条目，决定是否显示更多
        if (mAdapter!!.getImageInfoList()!!.size > maxImageCount) {
            val child = getChildAt(maxImageCount - 1)
            if (child is NineGridImageView) {
                child.setMoreNum(mAdapter!!.getImageInfoList()!!.size - maxImageCount)
            }
        }
        mImageInfoList = imageInfoList
    }

    /**
     * 获得 ImageView 保证了 ImageView 的重用
     */
    private fun getImageView(position: Int): ImageView? {
        val imageView: ImageView
        if(mAdapter == null){
            throw RuntimeException("please set adapter!")
        }
        if (position < imageViews!!.size) {
            imageView = imageViews!![position]
            // 重用的ImageView需要清屏
            (imageView as? NineGridImageView)?.clearView()
        } else {
            imageView = mAdapter!!.generateImageView(context)
            imageView.setOnClickListener {
                mAdapter!!.onImageItemClick(
                    context,
                    this,
                    position,
                    mAdapter!!.getImageInfoList()
                )
            }
            imageViews!!.add(imageView)
        }
        mAdapter!!.getImageInfoList()?.get(position)?.let {imageInfo ->
            (imageView as? NineGridImageView)?.let{
                if(imageInfo.type == NineGridImageInfo.IMAGE_TYPE_GIF){
                    it.setTag("GIF")
                }else if(imageInfo.height/imageInfo.width >= longPictureScale){
                    it.setTag("长图")
                }
            }
            mAdapter!!.loadImage(imageView, position, imageInfo.url)
        }
        return imageView
    }

    /**
     * 设置宫格间距
     */
    fun setGridSpacing(spacing: Int) {
        gridSpacing = spacing
    }

    /**
     * 设置只有一张图片时的宽
     */
    fun setSingleImageSize(maxImageSize: Int) {
        singleImageSize = maxImageSize
    }

    /**
     * 设置只有一张图片时的宽高比
     */
    fun setSingleImageRatio(ratio: Float) {
        singleImageRatio = ratio
    }

    /**
     * 设置最大图片数
     */
    fun setMaxSize(maxSize: Int) {
        maxImageCount = maxSize
    }

    fun getMaxSize(): Int = maxImageCount
}