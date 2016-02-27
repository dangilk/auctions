package com.djgilk.auctions.presenter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.LinearLayout;

import com.djgilk.auctions.R;
import com.djgilk.auctions.facebook.RxFacebook;
import com.djgilk.auctions.firebase.RxFirebase;
import com.djgilk.auctions.model.ClientConfig;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.firebase.client.Firebase;

import javax.inject.Inject;
import javax.inject.Singleton;

import butterknife.Bind;
import rx.Observer;

/**
 * Created by dangilk on 2/25/16.
 */
@Singleton
public class LoginPresenter extends ViewPresenter {
    final static String FB_APP_ID = "215370665481827";

    AccessToken accessToken;

    @Inject
    Firebase firebase;

    @Inject
    CallbackManager callbackManager;

    @Bind(R.id.ll_login)
    LinearLayout loginLayout;

    @Inject
    public LoginPresenter () {};

    public void onCreate(Activity activity) {
        super.onCreate(activity);
        LoginManager loginManager = LoginManager.getInstance();
        Log.i("Dan", "subscribe to auth");
        RxFacebook.observeFacebookAuth(activity, callbackManager).flatMap(new RxFirebase.ToFirebaseAuthEvent(firebase))
                .flatMap(new RxFirebase.ToFirebaseObject<ClientConfig>(firebase.child("clientConfig"), ClientConfig.class))

                .subscribe(new Observer<ClientConfig>() {
                    @Override
                    public void onCompleted() {
                        Log.i("Dan", "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("Dan", "onError");
                    }

                    @Override
                    public void onNext(ClientConfig config) {
                        Log.i("Dan", "got client config! test string = " + config.getTest());
                    }
                });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
