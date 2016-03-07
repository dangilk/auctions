package com.djgilk.auctions.helper;

import android.app.Activity;
import android.content.Intent;

import com.djgilk.auctions.facebook.FacebookAuthEvent;
import com.djgilk.auctions.facebook.RxFacebook;
import com.djgilk.auctions.firebase.FirebaseAuthEvent;
import com.djgilk.auctions.firebase.RxFirebase;
import com.djgilk.auctions.model.ClientConfig;
import com.djgilk.auctions.model.CurrentItem;
import com.djgilk.auctions.model.User;
import com.facebook.CallbackManager;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Subscription;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

/**
 * Created by dangilk on 3/3/16.
 */
@Singleton
public class RxPublisher {
    ConnectableObservable<FacebookAuthEvent> facebookAuthEventObservable;
    ConnectableObservable<FirebaseAuthEvent> firebaseAuthEventObservable;
    ConnectableObservable<CurrentItem> currentItemObservable;
    ConnectableObservable<ClientConfig> clientConfigObservable;
    ConnectableObservable<Boolean> observablesCompleteObservable;
    ConnectableObservable<User> userObservable;
    ConnectableObservable<Boolean> loginStateObservable;

    Set<ConnectableObservable<?>> connectableObservables = new HashSet<ConnectableObservable<?>>();
    Set<Subscription> subscriptions = new HashSet<Subscription>();

    @Inject
    RxFirebase rxFirebase;

    @Inject
    RxFacebook rxFacebook;

    @Inject
    CallbackManager callbackManager;

    @Inject
    RxPublisher() {}

    public void publish(Activity activity) {
        // auth layer
        facebookAuthEventObservable = rxFacebook.observeFacebookAuth(activity, callbackManager).subscribeOn(Schedulers.io()).publish();
        firebaseAuthEventObservable = facebookAuthEventObservable.flatMap(rxFirebase.toFirebaseAuthEvent()).publish();
        userObservable = firebaseAuthEventObservable.flatMap(rxFirebase.toFirebaseUser()).publish();
        loginStateObservable = userObservable.flatMap(rxFirebase.toLoginState()).publish();

        // data layer
        currentItemObservable = loginStateObservable.flatMap(rxFirebase.toFirebaseObject(CurrentItem.getRootPath(), CurrentItem.class)).publish();
        clientConfigObservable = loginStateObservable.flatMap(rxFirebase.toFirebaseObject(ClientConfig.getRootPath(), ClientConfig.class)).publish();


        // all complete
        observablesCompleteObservable = currentItemObservable.zipWith(clientConfigObservable, new RxHelper.ZipWaiter()).publish();

        connectableObservables.add(facebookAuthEventObservable);
        connectableObservables.add(firebaseAuthEventObservable);
        connectableObservables.add(currentItemObservable);
        connectableObservables.add(clientConfigObservable);
        connectableObservables.add(userObservable);
        connectableObservables.add(loginStateObservable);
        connectableObservables.add(observablesCompleteObservable);
    }

    public void connect() {
        for (ConnectableObservable<?> connectableObservable : connectableObservables) {
            subscriptions.add(connectableObservable.connect());
        }
    }

    public void unsubscribe() {
        for (Subscription subscription : subscriptions) {
            subscription.unsubscribe();
        }
        subscriptions.clear();
        connectableObservables.clear();
    }

    // for facebook login
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public ConnectableObservable<FacebookAuthEvent> getFacebookAuthEventObservable() {
        return facebookAuthEventObservable;
    }

    public ConnectableObservable<FirebaseAuthEvent> getFirebaseAuthEventObservable() {
        return firebaseAuthEventObservable;
    }

    public ConnectableObservable<CurrentItem> getCurrentItemObservable() {
        return currentItemObservable;
    }

    public ConnectableObservable<ClientConfig> getClientConfigObservable() {
        return clientConfigObservable;
    }

    public ConnectableObservable<Boolean> getObservablesCompleteObservable() {
        return observablesCompleteObservable;
    }

    public ConnectableObservable<User> getUserObservable() {
        return userObservable;
    }
}
