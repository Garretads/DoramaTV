package ru.garretech.garred.doramatv.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import org.json.JSONObject

class Source (val sourceId : Int,val name : String,val subUnit : String) : MultiItemEntity {


    override fun getItemType() = TYPE

    companion object {
        const val TYPE = 1
    }
}