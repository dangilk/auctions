package com.djgilk.auctions.helper;

import com.djgilk.auctions.MainApplication;
import com.djgilk.auctions.presenter.ViewPresenter;
import com.djgilk.auctions.view.PresenterHolder;

import rx.Observable;

/**
 * Created by dangilk on 5/20/16.
 */
public class ViewUtils {

    public static boolean goBack(MainApplication mainApplication, PresenterHolder holder) {
        String previousTag = mainApplication.popBackStack();
        ViewPresenter previous = holder.getPresenterMap().get(previousTag);
        ViewPresenter current = holder.getPresenterMap().get(mainApplication.getCurrentPresenterTag());
        if (previous != null) {
            Observable.just(null).flatMap(new RxAndroid.ToLayoutFade(mainApplication, current, previous, false)).subscribe();
            return true;
        } else {
            return false;
        }
    }
}
