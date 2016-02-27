package com.djgilk.auctions.firebase;

import android.util.Log;

import com.djgilk.auctions.facebook.FacebookAuthEvent;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.firebase.client.ValueEventListener;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/**
 * Created by dangilk on 2/26/16.
 */
public class RxFirebase {


    public static class ToFirebaseObject<T extends Object> implements Func1<FirebaseAuthEvent, Observable<T>> {
        private final Firebase firebase;
        private final Class clazz;

        public ToFirebaseObject(Firebase firebase, Class clazz) {
            this.firebase = firebase;
            this.clazz = clazz;
        }

        @Override
        public Observable<T> call(FirebaseAuthEvent firebaseAuthEvent) {
            return observe(firebase, clazz);
        }

        public Observable<T> observe(final Firebase firebase, final Class clazz) {
            return Observable.create(new Observable.OnSubscribe<T>() {
                @Override
                public void call(final Subscriber<? super T> subscriber) {
                    final ValueEventListener listener = firebase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d("Dan", "found " + dataSnapshot.getChildrenCount() + " data items");
                            //for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            T object = (T) dataSnapshot.getValue(clazz);
                            //}
                            subscriber.onNext(object);
                        }

                        @Override
                        public void onCancelled(FirebaseError error) {
                            // Turn the FirebaseError into a throwable to conform to the API
                            subscriber.onError(new FirebaseException(error.getMessage()));
                        }
                    });

                    // When the subscription is cancelled, remove the listener
                    subscriber.add(Subscriptions.create(new Action0() {
                        @Override
                        public void call() {
                            firebase.removeEventListener(listener);
                        }
                    }));
                }
            });
        }
    }

    public static class ToFirebaseAuthEvent implements Func1<FacebookAuthEvent, Observable<FirebaseAuthEvent>> {
        final Firebase firebase;

        public ToFirebaseAuthEvent(Firebase firebase) {
            this.firebase = firebase;
        }

        @Override
        public Observable<FirebaseAuthEvent> call(FacebookAuthEvent facebookAuthEvent) {
            return observeFirebaseAuth(facebookAuthEvent);
        }

        private Observable<FirebaseAuthEvent> observeFirebaseAuth(final FacebookAuthEvent facebookAuthEvent) {
            return Observable.create(new Observable.OnSubscribe<FirebaseAuthEvent>() {
                @Override
                public void call(final Subscriber<? super FirebaseAuthEvent> subscriber) {
                    if (facebookAuthEvent.isLoggedIn()) {
                        firebase.authWithOAuthToken("facebook", facebookAuthEvent.getAccessToken().getToken(), new Firebase.AuthResultHandler() {
                            @Override
                            public void onAuthenticated(AuthData authData) {
                                Log.i("Dan", "firebase authed");
                                subscriber.onNext(new FirebaseAuthEvent(authData));
                            }

                            @Override
                            public void onAuthenticationError(FirebaseError firebaseError) {
                                Log.w("Dan", "firebase auth error");
                                subscriber.onError(firebaseError.toException());
                            }
                        });
                    } else {
                        firebase.unauth();
                    }
                }
            });
        }
    }



}