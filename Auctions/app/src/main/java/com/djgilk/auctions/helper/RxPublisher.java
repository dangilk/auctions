package com.djgilk.auctions.helper;

import android.app.Activity;
import android.content.Intent;

import com.djgilk.auctions.facebook.FacebookAuthEvent;
import com.djgilk.auctions.facebook.RxFacebook;
import com.djgilk.auctions.firebase.FirebaseAuthEvent;
import com.djgilk.auctions.firebase.RxFirebase;
import com.djgilk.auctions.model.AuctionState;
import com.djgilk.auctions.model.Bid;
import com.djgilk.auctions.model.ClientConfig;
import com.djgilk.auctions.model.CurrentItem;
import com.djgilk.auctions.model.User;
import com.facebook.CallbackManager;
import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscription;
import rx.observables.ConnectableObservable;
import timber.log.Timber;

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
    ConnectableObservable<User> userCreationObservable;
    ConnectableObservable<Boolean> loginStateObservable;
    ConnectableObservable<User> userObservable;
    ConnectableObservable<Long> clockOffsetObservable;
    ConnectableObservable<AuctionState> auctionStateObservable;
    ConnectableObservable<Long> aggregateBidObservable;

    List<ConnectableObservable<?>> connectableObservables = new ArrayList<ConnectableObservable<?>>();
    Set<Subscription> subscriptions = new HashSet<Subscription>();

    @Inject
    RxFirebase rxFirebase;

    @Inject
    RxFacebook rxFacebook;

    @Inject
    Firebase firebase;

    @Inject
    CallbackManager callbackManager;

    @Inject
    RxPublisher() {}

    public void publish(Activity activity) {
        Timber.d("rxPublisher.publish()");
        // auth layer
        facebookAuthEventObservable = rxFacebook.observeFacebookAuth(activity, callbackManager).replay(1);/*.subscribeOn(Schedulers.io()) maybe causes weird race conditions?*/;
        firebaseAuthEventObservable = facebookAuthEventObservable.flatMap(rxFirebase.toFirebaseAuthEvent()).replay(1);
        userCreationObservable = firebaseAuthEventObservable.flatMap(rxFirebase.toFirebaseUserCreation()).replay(1);
        loginStateObservable = userCreationObservable.flatMap(rxFirebase.toLoginState()).replay(1);

        // data layer
        auctionStateObservable = loginStateObservable.flatMap(rxFirebase.toFirebaseObject(AuctionState.getRootPath(), AuctionState.class)).replay(1);
        clockOffsetObservable = loginStateObservable.flatMap(rxFirebase.toFirebaseClockOffset()).replay(1);
        currentItemObservable = auctionStateObservable.flatMap(CurrentItem.fromAuctionState(rxFirebase)).replay(1);
        clientConfigObservable = loginStateObservable.flatMap(rxFirebase.toFirebaseObject(ClientConfig.getRootPath(), ClientConfig.class)).replay(1);
        userObservable = userCreationObservable.flatMap(rxFirebase.toFirebaseUser()).replay(1);
        aggregateBidObservable = Observable.concat(Observable.combineLatest(auctionStateObservable, userObservable, Bid.observeAggregateBids(firebase))).replay(1);

        observablesCompleteObservable = Observable.zip(currentItemObservable, clientConfigObservable, userObservable, new RxHelper.ZipWaiter3()).replay(1);


        connectableObservables.add(firebaseAuthEventObservable);
        connectableObservables.add(currentItemObservable);
        connectableObservables.add(clientConfigObservable);
        connectableObservables.add(userCreationObservable);
        connectableObservables.add(userObservable);
        connectableObservables.add(loginStateObservable);
        connectableObservables.add(observablesCompleteObservable);
        connectableObservables.add(clockOffsetObservable);
        connectableObservables.add(auctionStateObservable);
        connectableObservables.add(aggregateBidObservable);
        // must be last
        connectableObservables.add(facebookAuthEventObservable);
        Timber.d("rxPublisher.publish() complete");
    }

    public void connect() {
        Timber.d("rxPublisher.connect()");
        for (ConnectableObservable<?> connectableObservable : connectableObservables) {
            connectableObservable.connect();
        }
        Timber.d("rxPublisher.connect() complete");
    }

    // for facebook login
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return callbackManager.onActivityResult(requestCode, resultCode, data);
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

    public ConnectableObservable<User> getUserCreationObservable() {
        return userCreationObservable;
    }

    public ConnectableObservable<AuctionState> getAuctionStateObservable() {
        return auctionStateObservable;
    }

    public ConnectableObservable<User> getUserObservable() {
        return userObservable;
    }

    public ConnectableObservable<Long> getClockOffsetObservable() {
        return clockOffsetObservable;
    }

    public ConnectableObservable<Long> getAggregateBidObservable() {
        return aggregateBidObservable;
    }
}
