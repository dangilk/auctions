package com.djgilk.auctions.facebook;

import android.app.Activity;

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
import timber.log.Timber;

/**
 * Created by dangilk on 2/26/16.
 */
@Singleton
public class RxFacebook {

    @Inject
    RxFacebook(){}

    public Observable<FacebookAuthEvent> observeFacebookAuth(final Activity activity, final CallbackManager callbackManager) {
        Timber.d("observeFacebookAuth()");
        return Observable.create(new Observable.OnSubscribe<FacebookAuthEvent>() {
            @Override
            public void call(final Subscriber<? super FacebookAuthEvent> subscriber) {
                Timber.d("subscribe to fb auth: " + subscriber.getClass().getName());
                final LoginManager loginManager = LoginManager.getInstance();
                loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Timber.d("facebook authed");
                        //subscriber.onNext(new FacebookAuthEvent(loginResult.getAccessToken()));
                    }

                    @Override
                    public void onCancel() {
                        Timber.d("facebook onCancel");
                        //subscriber.onError(new RuntimeException("user cancelled login"));
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Timber.d("facebook onError");
                        subscriber.onError(exception);
                    }
                });

                final AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
                    @Override
                    protected void onCurrentAccessTokenChanged(
                            AccessToken oldAccessToken,
                            AccessToken currentAccessToken) {
                        Timber.d("access token changed: " + currentAccessToken.getToken());
                        subscriber.onNext(new FacebookAuthEvent(currentAccessToken));
                    }
                };

                final List<String> permissions = new ArrayList<String>();
                AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
                if (currentAccessToken == null || currentAccessToken.isExpired()) {
                    Timber.d("bad fb access token, need to log in");
                    loginManager.logInWithReadPermissions(activity, permissions);
                } else {
                    Timber.d("facebook access token: " + currentAccessToken.getToken());
                    //subscriber.onNext(new FacebookAuthEvent(currentAccessToken));
                    Timber.d("start tracking facebook data");
                    accessTokenTracker.startTracking();
                }

                // When the subscription is cancelled, clean up
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        Timber.d("stop tracking facebook data");
                        accessTokenTracker.stopTracking();
                    }
                }));
            }
        });
    }
}
