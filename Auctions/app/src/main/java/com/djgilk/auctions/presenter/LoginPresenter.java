package com.djgilk.auctions.presenter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.djgilk.auctions.R;
import com.djgilk.auctions.facebook.RxFacebook;
import com.djgilk.auctions.firebase.RxFirebase;
import com.djgilk.auctions.model.ClientConfig;
import com.djgilk.auctions.model.SharedState;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.firebase.client.Firebase;

import javax.inject.Inject;
import javax.inject.Singleton;

import butterknife.Bind;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func2;

/**
 * Created by dangilk on 2/25/16.
 */
@Singleton
public class LoginPresenter extends ViewPresenter {
    final static String FB_APP_ID = "215370665481827";
    Subscription loginSubscription;
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

    }

    public Observable<Boolean> observeLogin(Activity activity) {
        return RxFacebook.observeFacebookAuth(activity, callbackManager).flatMap(new RxFirebase.ToFirebaseAuthEvent(firebase))
                .flatMap(new RxFirebase.ToFirebaseObject<ClientConfig>(firebase.child("clientConfig"), ClientConfig.class))
                .zipWith(new RxFirebase.ToFirebaseObject<SharedState>(firebase.child("sharedState"), SharedState.class).observe(),
                        new Func2<ClientConfig, SharedState, Boolean>() {
                            @Override
                            public Boolean call(ClientConfig clientConfig, SharedState sharedState) {
                                Log.i("Dan", "current item image: " + sharedState.getCurrentItem().getImageUrl());
                                return true;
                            }
                        });

//                .subscribe(new Observer<Boolean>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.i("Dan", "initialization complete");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.i("Dan", "initialization error");
//                    }
//
//                    @Override
//                    public void onNext(Boolean success) {
//                        Log.i("Dan", "initialized successfully");
//                    }
//                });
    }

    @Override
    public View getLayout() {
        return loginLayout;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void onDestroy() {
        //loginSubscription.unsubscribe();
    }
}
