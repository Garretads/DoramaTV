package ru.garretech.garred.doramatv.adapters

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.model.Movie
import com.bumptech.glide.Glide

class MovieListAdapter(layoutResId: Int, data: List<Movie>?) : BaseQuickAdapter<Movie, BaseViewHolder>(layoutResId, data) {

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
        helper.setText(R.id.movieTitle, item.title)
        helper.setImageBitmap(R.id.moviePhoto, item.image)
        val imageView = helper.itemView.findViewById<ImageView>(R.id.moviePhoto)

        Glide
            .with(helper.itemView.context!!)
            .load(item.movieImageURL)
            .fitCenter()
            //.placeholder(R.drawable.loading_spinner)
            .into(imageView)
    }
}
