package com.djgilk.auctions.facebook;

import android.app.Activity;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Created by dangilk on 2/26/16.
 */
@Singleton
public class RxFacebook {

    AccessTokenTracker accessTokenTracker;

    @Inject
    RxFacebook(){}

    public Observable<FacebookAuthEvent> observeFacebookAuth(final Activity activity, final CallbackManager callbackManager) {
        return Observable.create(new Observable.OnSubscribe<FacebookAuthEvent>() {
            @Override
            public void call(final Subscriber<? super FacebookAuthEvent> subscriber) {
                final LoginManager loginManager = LoginManager.getInstance();
                loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.i("Dan", "facebook authed");
                        //subscriber.onNext(new FacebookAuthEvent(loginResult.getAccessToken()));
                    }

                    @Override
                    public void onCancel() {
                        //subscriber.onError(new RuntimeException("user cancelled login"));
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        subscriber.onError(exception);
                    }
                });

                if (accessTokenTracker == null) {
                    Log.d("Dan", "start tracking facebook data");
                    accessTokenTracker = new AccessTokenTracker() {
                        @Override
                        protected void onCurrentAccessTokenChanged(
                                AccessToken oldAccessToken,
                                AccessToken currentAccessToken) {
                            Log.d("Dan", "access token changed: " + currentAccessToken.getToken());

                            subscriber.onNext(new FacebookAuthEvent(currentAccessToken));
                        }
                    };
                    accessTokenTracker.startTracking();
                }

                final List<String> permissions = new ArrayList<String>();
                AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
                if (currentAccessToken == null || currentAccessToken.isExpired()) {
                    loginManager.logInWithReadPermissions(activity, permissions);
                } else {
                    Log.d("Dan", "facebook access token: " + currentAccessToken.getToken());
                    subscriber.onNext(new FacebookAuthEvent(currentAccessToken));
                }

                // When the subscription is cancelled, clean up
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        Log.d("Dan", "stop tracking facebook data");
                        accessTokenTracker.stopTracking();
                    }
                }));
            }
        });
    }
}
