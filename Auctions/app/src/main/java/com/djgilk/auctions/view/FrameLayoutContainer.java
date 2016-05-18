package com.djgilk.auctions.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.djgilk.auctions.presenter.ViewPresenter;

/**
 * Created by dangilk on 5/12/16.
 */
public class FrameLayoutContainer extends FrameLayout implements Container {
    ViewPresenter currentPresenter;

    public FrameLayoutContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void showItem(String item) {

    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
