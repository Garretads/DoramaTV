package ru.garretech.garred.doramatv.selection

import androidx.recyclerview.widget.RecyclerView

interface SelectionObserver {
    fun onSelectedChanged(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, isSelected: Boolean)

    fun onSelectableChanged(isSelectable: Boolean)
}
