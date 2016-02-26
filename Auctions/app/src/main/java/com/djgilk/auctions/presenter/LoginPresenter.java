package com.djgilk.auctions.presenter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.LinearLayout;

import com.djgilk.auctions.R;
import com.djgilk.auctions.facebook.FacebookFunctions;
import com.djgilk.auctions.facebook.FbAuthEvent;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.Firebase;

import javax.inject.Inject;
import javax.inject.Singleton;

import butterknife.Bind;
import rx.Observable;
import rx.Observer;

/**
 * Created by dangilk on 2/25/16.
 */
@Singleton
public class LoginPresenter extends ViewPresenter {
    final static String FB_APP_ID = "215370665481827";

    CallbackManager callbackManager;
    AccessToken accessToken;

    final Observable<FbAuthEvent> loginStream = Observable.empty();

    @Inject
    Firebase firebase;

    @Bind(R.id.ll_login)
    LinearLayout loginLayout;

    @Bind(R.id.lb_login)
    LoginButton loginButton;

    @Inject
    public LoginPresenter () {};

    public void onCreate(Activity activity) {
        super.onCreate(activity);
        callbackManager = CallbackManager.Factory.create();
        loginStream.doOnNext(new FacebookFunctions.HideLoginButton(loginButton)).subscribe(new Observer<FbAuthEvent>() {
            @Override
            public void onCompleted() {
                Log.i("Dan", "onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.i("Dan", "onError");
            }

            @Override
            public void onNext(FbAuthEvent fbAuthEvent) {
                Log.i("Dan", "onNext");
            }
        });
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //loginButton.setVisibility(View.GONE);
                emitAuthEvent();
            }

            @Override
            public void onCancel() {
                emitAuthEvent();
            }

            @Override
            public void onError(FacebookException exception) {
                emitAuthEvent();
            }
        });
        emitAuthEvent();
    }

    public void emitAuthEvent() {
        accessToken = AccessToken.getCurrentAccessToken();
        Log.i("Dan", "emit auth event: " + accessToken.getToken());
        loginStream.mergeWith(Observable.just(new FbAuthEvent(accessToken)));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
