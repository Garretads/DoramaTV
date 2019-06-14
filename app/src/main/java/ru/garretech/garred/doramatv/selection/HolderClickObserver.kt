package ru.garretech.garred.doramatv.selection

import android.support.v7.widget.RecyclerView

interface HolderClickObserver {
    fun onHolderClick(holder: RecyclerView.ViewHolder)

    fun onHolderLongClick(holder: RecyclerView.ViewHolder): Boolean
}

