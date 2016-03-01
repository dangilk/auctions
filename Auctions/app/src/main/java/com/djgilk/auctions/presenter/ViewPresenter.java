package com.djgilk.auctions.presenter;

import android.app.Activity;
import android.view.View;

import butterknife.ButterKnife;
import rx.Observable;

/**
 * Created by dangilk on 2/25/16.
 */
public abstract class ViewPresenter {

    public abstract void onDestroy();
    public abstract View getLayout();
    public abstract String getPresenterTag();
    public Observable<Boolean> onCreate(Activity activity) {
        ButterKnife.bind(this, activity);
        return Observable.just(true);
    }

}
