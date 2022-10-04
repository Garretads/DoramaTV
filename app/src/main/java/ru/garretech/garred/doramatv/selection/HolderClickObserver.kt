package ru.garretech.garred.doramatv.selection

import androidx.recyclerview.widget.RecyclerView

interface HolderClickObserver {
    fun onHolderClick(holder: RecyclerView.ViewHolder)

    fun onHolderLongClick(holder: RecyclerView.ViewHolder): Boolean
}

