package ru.garretech.garred.doramatv.selection

import android.database.Observable
import androidx.recyclerview.widget.RecyclerView
import android.view.View

import java.lang.ref.WeakReference
import java.util.HashSet

class SelectionHelper(private val mTracker: WeakHolderTracker) {
    val mSelectedItems = HashSet<Int>()
    private val mHolderClickObservable = HolderClickObservable()
    private val mSelectionObservable = SelectionObservable()

    var isSelectable = false
        set(isSelectable) {
            field = isSelectable
            if (!isSelectable) clearSelection()
            mSelectionObservable.notifySelectableChanged(isSelectable)
        }

    val selectedItemsCount: Int
        get() = mSelectedItems.size

    constructor() : this(WeakHolderTracker())


    fun <H : androidx.recyclerview.widget.RecyclerView.ViewHolder> wrapSelectable(holder: H): H {
        ViewHolderMultiSelectionWrapper(holder)
        return holder
    }

    fun <H : androidx.recyclerview.widget.RecyclerView.ViewHolder> wrapClickable(holder: H): H {
        ViewHolderClickWrapper(holder)
        return holder
    }

    fun bindHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        mTracker.bindHolder(holder, position)
    }

    fun toggleItemSelected(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        setItemSelected(holder, !isItemSelected(holder))
    }

    fun setItemSelected(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, isSelected: Boolean): Boolean {
        val position = holder.adapterPosition
        if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
            val isAlreadySelected = isItemSelected(position)
            if (isSelected) {
                mSelectedItems.add(position)
            } else {
                mSelectedItems.remove(position)
            }
            if (isSelected xor isAlreadySelected) {
                mSelectionObservable.notifySelectionChanged(holder, isSelected)
            }
            return true
        } else {
            return false
        }
    }

    fun isItemSelected(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
        return mSelectedItems.contains(holder.adapterPosition)
    }

    fun isItemSelected(position: Int): Boolean {
        return mSelectedItems.contains(position)
    }

    fun clearSelection() {
        mSelectedItems.clear()
        for (holder in mTracker.trackedHolders) {
            if (holder != null) {
                mSelectionObservable.notifySelectionChanged(holder, false)
            }
        }
    }

    fun registerHolderClickObserver(observer: HolderClickObserver) {
        mHolderClickObservable.registerObserver(observer)
    }

    fun unregisterSelectionObserver(observer: SelectionObserver) {
        mSelectionObservable.unregisterObserver(observer)
    }

    fun registerSelectionObserver(observer: SelectionObserver) {
        mSelectionObservable.registerObserver(observer)
    }

    fun unregisterHolderClickObserver(observer: HolderClickObserver) {
        mHolderClickObservable.unregisterObserver(observer)
    }

    private inner class HolderClickObservable : Observable<HolderClickObserver>() {
        fun notifyOnHolderClick(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
            synchronized(mObservers) {
                for (observer in mObservers) {
                    observer.onHolderClick(holder)
                }
            }
        }

        fun notifyOnHolderLongClick(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
            var isConsumed = false
            synchronized(mObservers) {
                for (observer in mObservers) {
                    isConsumed = isConsumed || observer.onHolderLongClick(holder)
                }
            }
            return isConsumed
        }
    }

    private inner class SelectionObservable : Observable<SelectionObserver>() {
        internal fun notifySelectionChanged(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, isSelected: Boolean) {
            synchronized(mObservers) {
                for (observer in mObservers) {
                    observer.onSelectedChanged(holder, isSelected)
                }
            }
        }

        internal fun notifySelectableChanged(isSelectable: Boolean) {
            synchronized(mObservers) {
                for (observer in mObservers) {
                    observer.onSelectableChanged(isSelectable)
                }
            }
        }
    }

    private abstract inner class ViewHolderWrapper protected constructor(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) : View.OnClickListener {
        protected val mWrappedHolderRef: WeakReference<androidx.recyclerview.widget.RecyclerView.ViewHolder>

        init {
            mWrappedHolderRef = WeakReference<androidx.recyclerview.widget.RecyclerView.ViewHolder>(holder)
        }
    }

    private inner class ViewHolderMultiSelectionWrapper internal constructor(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) : ViewHolderWrapper(holder), View.OnLongClickListener {
        init {
            val itemView = holder.itemView
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            itemView.isLongClickable = true
        }

        override fun onClick(v: View) {
            val holder = mWrappedHolderRef.get()
            if (holder != null) {
                if (isSelectable) {
                    toggleItemSelected(holder)
                } else {
                    mHolderClickObservable.notifyOnHolderClick(mWrappedHolderRef.get()!!)
                }
            }
        }

        override fun onLongClick(v: View): Boolean {
            val holder = mWrappedHolderRef.get()
            if (!isSelectable) {
                isSelectable = true
                if (holder != null) {
                    setItemSelected(holder, true)
                }
                return true
            } else {
                return holder == null || mHolderClickObservable.notifyOnHolderLongClick(holder)
            }
        }
    }

    private inner class ViewHolderClickWrapper internal constructor(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) : ViewHolderWrapper(holder) {
        init {
            val itemView = holder.itemView
            itemView.setOnClickListener(this)
            itemView.isClickable = true
        }

        override fun onClick(v: View) {
            val holder = mWrappedHolderRef.get()
            if (holder != null) {
                mHolderClickObservable.notifyOnHolderClick(mWrappedHolderRef.get()!!)
            }
        }
    }
}
