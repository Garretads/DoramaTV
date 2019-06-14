package ru.garretech.garred.doramatv.selection

import android.support.v7.widget.RecyclerView

interface SelectionObserver {
    fun onSelectedChanged(holder: RecyclerView.ViewHolder, isSelected: Boolean)

    fun onSelectableChanged(isSelectable: Boolean)
}
