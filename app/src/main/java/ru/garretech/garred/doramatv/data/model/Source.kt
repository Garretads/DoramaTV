package ru.garretech.garred.doramatv.data.model

import com.chad.library.adapter.base.entity.MultiItemEntity

class Source (val sourceId : Int,val name : String,val subUnit : String) : MultiItemEntity {


    override fun getItemType() = TYPE

    companion object {
        const val TYPE = 1
    }
}