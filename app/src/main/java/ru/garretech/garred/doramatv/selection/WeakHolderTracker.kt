package ru.garretech.garred.doramatv.selection

import android.support.v7.widget.RecyclerView
import android.util.SparseArray

import java.lang.ref.WeakReference
import java.util.ArrayList

class WeakHolderTracker {
    private val mHoldersByPosition = SparseArray<WeakReference<RecyclerView.ViewHolder>>()

    val trackedHolders: List<RecyclerView.ViewHolder>
        get() {
            val holders = ArrayList<RecyclerView.ViewHolder>()

            for (i in 0 until mHoldersByPosition.size()) {
                val key = mHoldersByPosition.keyAt(i)
                val holder = getHolder(key)

                if (holder != null) {
                    holders.add(holder)
                }
            }

            return holders
        }

    fun bindHolder(holder: RecyclerView.ViewHolder, position: Int) {
        mHoldersByPosition.put(position, WeakReference<RecyclerView.ViewHolder>(holder))
    }

    private fun getHolder(position: Int): RecyclerView.ViewHolder? {
        val holderRef = mHoldersByPosition.get(position)
        if (holderRef == null) {
            mHoldersByPosition.remove(position)
            return null
        }

        val holder = holderRef.get()
        if (holder == null || holder.adapterPosition != position && holder.adapterPosition != RecyclerView.NO_POSITION) {
            mHoldersByPosition.remove(position)
            return null
        }


        return holder
    }
}
