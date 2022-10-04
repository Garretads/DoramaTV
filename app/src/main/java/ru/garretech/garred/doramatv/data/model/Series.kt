package ru.garretech.garred.doramatv.data.model

import com.chad.library.adapter.base.entity.AbstractExpandableItem
import com.chad.library.adapter.base.entity.MultiItemEntity

class Series(
    val index: Int,
    val name: String,
    val url: String,
    val isNew: Boolean,
    val date: String?,
) : AbstractExpandableItem<Source>(), MultiItemEntity {

    var sourcesLoaded = false

    override fun getItemType() = TYPE

    override fun getLevel() = 0

    companion object {
        const val TYPE = 0
    }

}
