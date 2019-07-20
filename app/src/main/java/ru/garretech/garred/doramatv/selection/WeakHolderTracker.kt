package ru.garretech.garred.doramatv.selection

import androidx.recyclerview.widget.RecyclerView
import android.util.SparseArray

import java.lang.ref.WeakReference
import java.util.ArrayList

class WeakHolderTracker {
    private val mHoldersByPosition = SparseArray<WeakReference<androidx.recyclerview.widget.RecyclerView.ViewHolder>>()

    val trackedHolders: List<androidx.recyclerview.widget.RecyclerView.ViewHolder>
        get() {
            val holders = ArrayList<androidx.recyclerview.widget.RecyclerView.ViewHolder>()

            for (i in 0 until mHoldersByPosition.size()) {
                val key = mHoldersByPosition.keyAt(i)
                val holder = getHolder(key)

                if (holder != null) {
                    holders.add(holder)
                }
            }

            return holders
        }

    fun bindHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        mHoldersByPosition.put(position, WeakReference<androidx.recyclerview.widget.RecyclerView.ViewHolder>(holder))
    }

    private fun getHolder(position: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder? {
        val holderRef = mHoldersByPosition.get(position)
        if (holderRef == null) {
            mHoldersByPosition.remove(position)
            return null
        }

        val holder = holderRef.get()
        if (holder == null || holder.adapterPosition != position && holder.adapterPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
            mHoldersByPosition.remove(position)
            return null
        }


        return holder
    }
}
