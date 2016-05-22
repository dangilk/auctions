package com.djgilk.auctions.helper;

import com.djgilk.auctions.MainApplication;
import com.djgilk.auctions.presenter.ViewPresenter;

import rx.Observable;

/**
 * Created by dangilk on 5/20/16.
 */
public class ViewUtils {

    public static boolean goBack(MainApplication mainApplication) {
        ViewPresenter previous = mainApplication.popBackStack();
        if (previous != null) {
            Observable.just(null).flatMap(new RxAndroid.ToLayoutFade(mainApplication, mainApplication.getCurrentPresenter(), previous, false)).subscribe();
            return true;
        } else {
            return false;
        }
    }
}
