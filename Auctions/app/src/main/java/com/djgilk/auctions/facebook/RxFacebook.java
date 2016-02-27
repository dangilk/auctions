package com.djgilk.auctions.facebook;

import android.app.Activity;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by dangilk on 2/26/16.
 */
public class RxFacebook {

    public static Observable<FacebookAuthEvent> observeFacebookAuth(final Activity activity, final CallbackManager callbackManager) {
        return Observable.create(new Observable.OnSubscribe<FacebookAuthEvent>() {
            @Override
            public void call(final Subscriber<? super FacebookAuthEvent> subscriber) {
                final LoginManager loginManager = LoginManager.getInstance();
                loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.i("Dan", "facebook authed");
                        subscriber.onNext(new FacebookAuthEvent(loginResult.getAccessToken()));
                    }

                    @Override
                    public void onCancel() {
                        subscriber.onNext(new FacebookAuthEvent(null));
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        subscriber.onError(exception);
                    }
                });
                final List<String> permissions = new ArrayList<String>();
                AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
                if (currentAccessToken == null || currentAccessToken.isExpired()) {
                    loginManager.logInWithReadPermissions(activity, permissions);
                } else {
                    subscriber.onNext(new FacebookAuthEvent(currentAccessToken));
                }
            }
        });
    }
}
