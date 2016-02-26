package com.djgilk.auctions.presenter;

import android.app.Activity;

import butterknife.ButterKnife;

/**
 * Created by dangilk on 2/25/16.
 */
public abstract class ViewPresenter {

    public void onCreate(Activity activity) {
        ButterKnife.bind(this, activity);
    }
}
