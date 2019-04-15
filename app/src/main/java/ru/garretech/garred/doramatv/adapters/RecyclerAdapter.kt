package ru.garretech.garred.doramatv.adapters

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.model.Movie

class RecyclerAdapter(layoutResId: Int, data: List<Movie>?) : BaseQuickAdapter<Movie, BaseViewHolder>(layoutResId, data) {

    override fun addData(data: Movie) {
        super.addData(data)
    }

    fun addAll(movies: Collection<Movie>) {
        data.clear()
        for (movie in movies) {
            data.add(movie)
            notifyItemInserted(itemCount - 1)
        }
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun convert(helper: BaseViewHolder, item: Movie) {
        helper.setText(R.id.movie_title, item.title)
        helper.setImageBitmap(R.id.movie_photo, item.image)
    }
}
