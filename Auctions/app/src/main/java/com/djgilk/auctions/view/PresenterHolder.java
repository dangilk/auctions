package com.djgilk.auctions.view;

import com.djgilk.auctions.presenter.ViewPresenter;

import java.util.Map;

/**
 * Created by dangilk on 8/6/16.
 */
public interface PresenterHolder {
    Map<String,ViewPresenter> getPresenterMap();
}
