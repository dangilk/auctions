package com.djgilk.auctions.presenter;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;

import com.djgilk.auctions.R;

import javax.inject.Inject;

import butterknife.Bind;

/**
 * Created by dangilk on 2/25/16.
 */
public class LoginPresenter extends ViewPresenter {
    private final static String LOGIN_PRESENTER_TAG = "loginPresenter";

    @Bind(R.id.ll_login)
    LinearLayout loginLayout;

    @Inject
    public LoginPresenter () {};

    public void onCreate(Activity activity) {
        super.onCreate(activity);
    }

    @Override
    public View getLayout() {
        return loginLayout;
    }

    @Override
    public String getPresenterTag() {
        return LOGIN_PRESENTER_TAG;
    }

    public void onDestroy() {
    }
}
