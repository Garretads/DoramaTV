package ru.garretech.garred.doramatv.selection

import androidx.recyclerview.widget.RecyclerView

interface HolderClickObserver {
    fun onHolderClick(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder)

    fun onHolderLongClick(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean
}

