package com.hr.ninegridimagelayout

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import kotlin.math.min


class NineGridImageView(context: Context?,
                        attrs: AttributeSet?,
                        defStyleAttr: Int): AppCompatImageView(context, attrs, defStyleAttr) {

    private var moreNum = 0 //显示更多的数量
    private var maskColor = 0x78000000 //默认的遮盖颜色
    private var textSize = 35f //显示文字的大小单位sp
    private var tagTextSize = 8f //显示tag文字的大小单位sp
    private var textColor: Int = 0xFFFFFFFF.toInt() //显示文字的颜色
    private var tagBgColor: Int = 0xaa000000.toInt() //显示tag文字背景的颜色
    private var tagTextColor: Int = 0xFFFFFFFF.toInt() //显示tag文字的颜色
    private var textPaint: TextPaint //文字的画笔
    private var tagBgPaint: Paint //右下角文字的背景画笔
    private var tagTextPaint: TextPaint //右下角文字的画笔
    private var msg = "" //要绘制的文字
    private var tag = "" //要绘制的tag
    private var tagMargin = 10f //tag的margin
    private var tagPadding = 8f //tag的padding
    var tagRect: RectF = RectF() // tag圆角

    constructor(context: Context?): this(context, null)
    constructor(context: Context?, attrs: AttributeSet?): this(context, attrs, 0)
    init {
        //转化单位
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize,getContext().resources.displayMetrics)
        tagTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tagTextSize,getContext().resources.displayMetrics)
        textPaint = TextPaint().apply {
            textAlign = Paint.Align.CENTER //文字居中对齐
            isAntiAlias = true //抗锯齿
            textSize = this@NineGridImageView.textSize //设置文字大小
            color = textColor //设置文字颜色
        }
        tagBgPaint = Paint().apply {
            color = tagBgColor
        }
        tagTextPaint = TextPaint().apply {
            textAlign = Paint.Align.CENTER //文字居中对齐
            isAntiAlias = true //抗锯齿
            textSize = tagTextSize //设置文字大小
            color = tagTextColor //设置文字颜色
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (moreNum > 0) {
            canvas.drawColor(maskColor)
            val baseY = height / 2 - (textPaint.ascent() + textPaint.descent()) / 2
            canvas.drawText(msg, (width / 2).toFloat(), baseY, textPaint)
        }
        if(tag.isNotEmpty()){
            val tagTextWidth = tagTextPaint.measureText(tag)
            val tagLeft = width-tagMargin-tagTextWidth-tagPadding*2
            val tagTop = height-tagMargin-tagTextSize-tagPadding*2
            tagRect.set(tagLeft,tagTop,width-tagMargin,height-tagMargin)
            canvas.drawRoundRect(tagRect,6f,6f,tagBgPaint)
            canvas.drawText(tag, width-tagMargin-tagTextWidth/2-tagPadding, height-tagMargin-tagPadding-tagTextSize/2-(tagTextPaint.ascent() + tagTextPaint.descent())/2, tagTextPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 这是因为当只有一张图时，重绘有问题，暂时不知道怎么解决，先不重绘
        if(scaleType == ScaleType.FIT_XY){
            return super.onTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val drawable = drawable
                if (drawable != null) {
                    /**
                     * 默认情况下，所有的从同一资源（R.drawable.XXX）加载来的drawable实例都共享一个共用的状态，
                     * 如果你更改一个实例的状态，其他所有的实例都会收到相同的通知。
                     * 使用使 mutate 可以让这个drawable变得状态不定。这个操作不能还原（变为不定后就不能变为原来的状态）。
                     * 一个状态不定的drawable可以保证它不与其他任何一个drawabe共享它的状态。
                     * 此处应该是要使用的 mutate()，但是在部分手机上会出现点击后变白的现象，所以没有使用
                     * 目前这种解决方案没有问题
                     */
//                    drawable.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                    drawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
                    ViewCompat.postInvalidateOnAnimation(this)
                }
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                val drawableUp = drawable
                if (drawableUp != null) { //                    drawableUp.mutate().clearColorFilter();
                    drawableUp.clearColorFilter()
                    ViewCompat.postInvalidateOnAnimation(this)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    fun getMoreNum(): Int = moreNum

    fun setMoreNum(moreNum: Int) {
        this.moreNum = moreNum
        msg = "+$moreNum"
        invalidate()
    }

    fun getMaskColor(): Int = maskColor

    fun setMaskColor(maskColor: Int) {
        this.maskColor = maskColor
        invalidate()
    }

    fun getTextSize(): Float = textSize

    fun setTextSize(textSize: Float) {
        this.textSize = textSize
        textPaint.textSize = textSize
        invalidate()
    }

    fun getTextColor(): Int = textColor

    fun setTextColor(textColor: Int) {
        this.textColor = textColor
        textPaint.color = textColor
        invalidate()
    }

    fun setTag(tag: String) {
        this.tag = tag
        invalidate()
    }

    fun clearView() {
        moreNum = 0
        msg = ""
        tag = ""
        invalidate()
    }
}