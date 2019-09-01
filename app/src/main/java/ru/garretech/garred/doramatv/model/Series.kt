package ru.garretech.garred.doramatv.model

import com.chad.library.adapter.base.entity.AbstractExpandableItem
import com.chad.library.adapter.base.entity.MultiItemEntity

class Series(val index : Int, val name : String) : AbstractExpandableItem<Source>(), MultiItemEntity {

    var url : String? = null
    var sourcesLoaded = false

    override fun getItemType() = TYPE

    override fun getLevel() = 0

    companion object {
        const val TYPE = 0
    }

}
