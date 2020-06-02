package com.hr.ninegridimagelayout

open class NineGridImageInfo(var url:String,var width:Int = 0,var height:Int = 0,var type:String = IMAGE_TYPE_JPG){
    companion object{
        const val IMAGE_TYPE_PNG = "png"
        const val IMAGE_TYPE_JPG = "jpg"
        const val IMAGE_TYPE_GIF = "gif"
    }
}