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
    Observable<FacebookAuthEvent> facebookAuthEventObservable;
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
        facebookAuthEventObservable = rxFacebook.observeFacebookAuth(activity, callbackManager)/*.subscribeOn(Schedulers.io()) maybe causes weird race conditions?*/;
        firebaseAuthEventObservable = facebookAuthEventObservable.flatMap(rxFirebase.toFirebaseAuthEvent()).publish();
        userCreationObservable = firebaseAuthEventObservable.flatMap(rxFirebase.toFirebaseUserCreation()).publish();
        loginStateObservable = userCreationObservable.flatMap(rxFirebase.toLoginState()).publish();

        // data layer
        auctionStateObservable = loginStateObservable.flatMap(rxFirebase.toFirebaseObject(AuctionState.getRootPath(), AuctionState.class)).publish();
        clockOffsetObservable = loginStateObservable.flatMap(rxFirebase.toFirebaseClockOffset()).publish();
        currentItemObservable = auctionStateObservable.flatMap(CurrentItem.fromAuctionState(rxFirebase)).publish();
        clientConfigObservable = loginStateObservable.flatMap(rxFirebase.toFirebaseObject(ClientConfig.getRootPath(), ClientConfig.class)).publish();
        userObservable = userCreationObservable.flatMap(rxFirebase.toFirebaseUser()).publish();
        aggregateBidObservable = Observable.concat(Observable.combineLatest(auctionStateObservable, userObservable, Bid.observeAggregateBids(firebase))).publish();

        // initialization complete
        observablesCompleteObservable = Observable.zip(currentItemObservable, clientConfigObservable, userObservable, new RxHelper.ZipWaiter3()).publish();

        //connectableObservables.add(facebookAuthEventObservable);
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
        Timber.d("rxPublisher.publish() complete");
    }

    public void connect() {
        Timber.d("rxPublisher.connect()");
        for (ConnectableObservable<?> connectableObservable : connectableObservables) {
            subscriptions.add(connectableObservable.connect());
        }
        Timber.d("rxPublisher.connect() complete");
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
