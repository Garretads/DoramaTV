package ru.garretech.garred.doramatv.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import io.reactivex.disposables.CompositeDisposable
import ru.garretech.garred.doramatv.DisposableManager
import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.activities.WebViewActivity
import ru.garretech.garred.doramatv.fragments.MovieSourcesFragment
import ru.garretech.garred.doramatv.fragments.SelectQualityFragment
import ru.garretech.garred.doramatv.model.Series
import ru.garretech.garred.doramatv.model.Source

class MovieSourceAdapter(val fragment: MovieSourcesFragment, data : ArrayList<MultiItemEntity>) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {
    var onExpandableItemClickListener : OnExpandableItemClickListener? = null
    var selectedSeries = -1

    var disposableBag = CompositeDisposable()

    init {
        addItemType(Series.TYPE, R.layout.cardview_series)
        addItemType(Source.TYPE, R.layout.cardview_source)
    }


    fun setOnChapterClickListener(listener : OnExpandableItemClickListener) {
        onExpandableItemClickListener = listener
    }


    override fun convert(helper: BaseViewHolder?, item: MultiItemEntity?) {
        when (helper?.itemViewType) {
            Series.TYPE -> {
                val series = item as Series
                helper.setText(R.id.seriesNameText,series.name)

                val watchedSeriesIndexes = fragment.viewModel.getWatchedSeriesIndexes()

                if (watchedSeriesIndexes.contains(series.index))
                    flagWatchedSeries(helper)
                else
                    unflagWatchedSeries(helper)

                /*
                * Забирать индексы просмотренных серий. Сверять текущий индекс с полученным
                *
                * */

                helper.itemView.setOnClickListener {
                    val pos = helper.adapterPosition

                    //TODO ("Подгрузить список источников")

                    if (series.isExpanded)
                        collapse(pos)
                    else {
                        if (!series.sourcesLoaded)
                            DisposableManager.add(fragment.viewModel.getOneSeriesSources(series, fragment.viewModel.currentMovie?.url!!)
                                    .subscribe({ sourcesArray ->

                                        for (source in sourcesArray) {
                                            series.addSubItem(source)
                                            notifyDataSetChanged()
                                        }
                                        series.sourcesLoaded = true
                                        selectedSeries = series.index
                                        expand(pos)
                                    }, {
                                        Log.e("SOURCE LIST", "ERROR LOADING SOURCES LIST", it)
                                    }))
                        else {
                            selectedSeries = series.index
                            expand(pos)
                        }
                    }
                }

            }
            Source.TYPE -> {
                val source = item as Source
                //val sourceName = jsonObject.getString("sources_name")
                val sourceName = source.name
                helper.setText(R.id.sourceNameText,source.subUnit)

                val watchedSources = fragment.viewModel.getWatchedIdInSeries(selectedSeries)

                if (watchedSources.contains(source.sourceId))
                    flagWatchedSource(helper)
                else
                    unflagWatchedSource(helper)

                helper.itemView.setOnClickListener {

                    if (sourceName.contains("vk.com")) {
                        fragment.viewModel.getVkLink(source).subscribe({ fileLink ->
                            if (fileLink.length() != 0) {
                                fragment.viewModel.historyProvider.addSeries(selectedSeries,source.sourceId)

                                fragment.viewModel.addToHistory().subscribe {
                                    notifyItemChanged(selectedSeries)
                                    flagWatchedSource(helper)
                                    val selectQualityFragment = SelectQualityFragment.newInstance(fileLink!!)
                                    selectQualityFragment.show(fragment.childFragmentManager, "Выберите качество")
                                }.let(disposableBag::add)
                            } else
                                Toast.makeText(fragment.context,"Ошибка при загрузке списка качеств, попробуйте еще раз", Toast.LENGTH_LONG).show()

                        },{
                            Toast.makeText(fragment.context,"Ошибка при получении списка качеств", Toast.LENGTH_SHORT).show()
                            Log.e("MovieSourcesFragment","Ошибка при получении списка качеств",it)
                        }).let(disposableBag::add)

                    } else {
                        fragment.viewModel.getOthersLink(source).subscribe({
                            var seriesLink : String
                            fragment.viewModel.historyProvider.addSeries(selectedSeries,source.sourceId)

                            fragment.viewModel.addToHistory().subscribe {
                                notifyItemChanged(selectedSeries)
                                flagWatchedSource(helper)
                                val intent = Intent(fragment.context, WebViewActivity::class.java)
                                if (!it!!.contains("http") && !it.contains("https")) seriesLink = "https:$it"
                                else seriesLink = it
                                intent.putExtra("link", seriesLink)

                                fragment.startActivity(intent)
                            }.let(disposableBag::add)
                        },{
                            Toast.makeText(fragment.context,"Ошибка при открытии серии", Toast.LENGTH_SHORT).show()
                            Log.e("MovieSourcesFragment","Ошибка при открытии серии",it)
                        }).let(disposableBag::add)
                    }
                }

            }
        }
    }

    override fun getItemId(position: Int): Long {
        return mData[position].hashCode().toLong()
    }

    private fun flagWatchedSeries(helper : BaseViewHolder?) {
        helper?.setVisible(R.id.seriesWatchedImage,true)
    }

    private fun unflagWatchedSeries(helper : BaseViewHolder?) {
        helper?.setVisible(R.id.seriesWatchedImage,false)
    }

    private fun flagWatchedSource(helper: BaseViewHolder?) {
        helper?.setTextColor(R.id.sourceNameText,ContextCompat.getColor(fragment.context!!,R.color.watched_source))
    }

    private fun unflagWatchedSource(helper: BaseViewHolder?) {
        helper?.setTextColor(R.id.sourceNameText,ContextCompat.getColor(fragment.context!!,android.R.color.secondary_text_dark))
    }


    interface OnExpandableItemClickListener {
        fun onSeriesClicked(series: Series)
        fun onSourceClicked(source: Source)
    }


}