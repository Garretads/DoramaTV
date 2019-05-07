package ru.garretech.garred.doramatv.adapters

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.model.Movie

class RecyclerAdapter(layoutResId: Int, data: List<Movie>?) : BaseQuickAdapter<Movie, BaseViewHolder>(layoutResId, data) {

    override fun addData(data: Movie) {
        synchronized(this) {
            super.addData(data)
            notifyDataSetChanged()
        }
    }

    fun addAll(movies: Collection<Movie>) {
        data.clear()
        var index = 0
        synchronized(this) {
            for (movie in movies) {
                data.add(movie)
                notifyItemInserted(index)
                index++
            }
            notifyDataSetChanged()
        }
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun convert(helper: BaseViewHolder, item: Movie) {
        helper.setText(R.id.movieTitleNew, item.title)
        helper.setImageBitmap(R.id.moviePhotoNew, item.image)
    }
}
