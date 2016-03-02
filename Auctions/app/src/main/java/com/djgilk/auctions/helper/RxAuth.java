package com.djgilk.auctions.helper;

import android.app.Activity;

import com.djgilk.auctions.facebook.RxFacebook;
import com.djgilk.auctions.firebase.FirebaseAuthEvent;
import com.djgilk.auctions.firebase.RxFirebase;
import com.facebook.CallbackManager;
import com.firebase.client.Firebase;

import javax.inject.Inject;

import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

/**
 * Created by dangilk on 2/29/16.
 */
public class RxAuth {
    @Inject
    CallbackManager callbackManager;

    @Inject
    Firebase firebase;

    @Inject
    RxFacebook rxFacebook;

    @Inject
    public RxAuth() {
    }

    public ConnectableObservable<FirebaseAuthEvent> publishAuthEvents (Activity activity) {
        return rxFacebook.observeFacebookAuth(activity, callbackManager).subscribeOn(Schedulers.io()).flatMap(new RxFirebase.ToFirebaseAuthEvent(firebase)).publish();
    }
}
