package com.djgilk.auctions.model;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.firebase.client.ValueEventListener;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func2;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

/**
 * Created by dangilk on 3/20/16.
 */
public class Bid {
    private long timestamp;
    private long coins;

    public long getTimestamp() {
        return timestamp;
    }

    public long getCoins() {
        return coins;
    }

    public static Func2<AuctionState, User, Observable<Long>> observeAggregateBids(final Firebase firebase) {
        return new Func2<AuctionState, User, Observable<Long>>() {
            @Override
            public Observable<Long> call(AuctionState auctionState, User user) {
                return observeAggregateBids(firebase, auctionState.getAuctionItemId(), user.getFacebookId());
            }
        };
    }

    public static Observable<Long> observeAggregateBids(final Firebase firebase, final String itemId, final String userId) {
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(final Subscriber<? super Long> subscriber) {
                final Firebase firebaseRef = firebase.child("bids/" + itemId + "/" + userId);
                final ValueEventListener listener = firebaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Timber.d("bids updated");
                        Long aggregateBid = Long.valueOf(0);
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            Bid bid = snapshot.getValue(Bid.class);
                            aggregateBid += bid.getCoins();
                        }
                        Timber.d("aggregate bid: " + aggregateBid);
                        subscriber.onNext(aggregateBid);
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {
                        Timber.d("firebase error getting object");
                        // Turn the FirebaseError into a throwable to conform to the API
                        subscriber.onError(new FirebaseException(error.getMessage()));
                    }
                });

                // When the subscription is cancelled, remove the listener
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        firebaseRef.removeEventListener(listener);
                    }
                }));
            }
        });
    }
}
