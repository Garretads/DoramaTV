package ru.garretech.garred.doramatv.fragments;

import com.chad.library.adapter.base.loadmore.LoadMoreView;

import ru.garretech.garred.doramatv.R;

public class CustomLoadMoreView extends LoadMoreView {
    @Override
    public int getLayoutId() {
        return R.layout.fragment_load_more;
    }

    @Override
    public boolean isLoadEndGone() {
        return true;
    }

    @Override
    protected int getLoadingViewId() {
        return R.id.load_more_loading_view;
    }

    @Override
    protected int getLoadFailViewId() {
        return R.id.load_more_load_fail_view;
    }

    /**
     * IsLoadEndGone () for true, you can return 0
     * IsLoadEndGone () for false, can not return 0
     */
    @Override
    protected int getLoadEndViewId() {
        return 0;
    }
}
