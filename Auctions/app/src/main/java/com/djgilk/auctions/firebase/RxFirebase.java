package com.djgilk.auctions.firebase;

import android.util.Log;

import com.djgilk.auctions.facebook.FacebookAuthEvent;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.firebase.client.ValueEventListener;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/**
 * Created by dangilk on 2/26/16.
 */
@Singleton
public class RxFirebase {

    @Inject
    Firebase firebase;

    @Inject
    public RxFirebase(){}


    public <T extends Object> Func1<FirebaseAuthEvent, Observable<T>> toFirebaseObject(final String childRef, final Class<T> clazz) {
        return new Func1<FirebaseAuthEvent, Observable<T>>() {
            @Override
            public Observable<T> call(FirebaseAuthEvent firebaseAuthEvent) {
                return observeFirebaseObject(childRef, clazz);
            }
        };
    }

    public <T extends Object> Observable<T> observeFirebaseObject(final String childRef, final Class<T> clazz) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                final Firebase firebaseRef = firebase.child(childRef);
                final ValueEventListener listener = firebaseRef.addValueEventListener(new ValueEventListener() {
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
                        Log.d("Dan", "firebase error getting object");
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

    public Func1<FacebookAuthEvent, Observable<FirebaseAuthEvent>> toFirebaseAuthEvent() {
        return new Func1<FacebookAuthEvent, Observable<FirebaseAuthEvent>>() {
            @Override
            public Observable<FirebaseAuthEvent> call(FacebookAuthEvent facebookAuthEvent) {
                return observeFirebaseAuth(facebookAuthEvent);
            }
        };
    }

    private Observable<FirebaseAuthEvent> observeFirebaseAuth(final FacebookAuthEvent facebookAuthEvent) {
        return Observable.create(new Observable.OnSubscribe<FirebaseAuthEvent>() {
            @Override
            public void call(final Subscriber<? super FirebaseAuthEvent> subscriber) {
                final Firebase.AuthStateListener authStateListener = new Firebase.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(AuthData authData) {
                        if (authData == null) {
                            Log.d("Dan", "firebase not authed");
                            // firebase not authed
                            if (facebookAuthEvent.isLoggedIn()) {
                                firebase.authWithOAuthToken("facebook", facebookAuthEvent.getAccessToken().getToken(), new Firebase.AuthResultHandler() {
                                    @Override
                                    public void onAuthenticated(AuthData authData) {
                                        // no op, process auth in else clause below
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
                        } else {
                            // firebase already authed
                            Log.i("Dan", "firebase authed. uid = " + authData.getUid());
                            for (String value : authData.getProviderData().keySet()) {
                                //Log.i("Dan", "fb key: " + value);
                            }
                            final String uid = authData.getUid();
                            //final User user = firebase.
                            subscriber.onNext(new FirebaseAuthEvent(authData));
                        }
                    }
                };

                firebase.addAuthStateListener(authStateListener);

                // When the subscription is cancelled, remove the listener
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        Log.d("Dan", "removing firebase auth listener");
                        firebase.removeAuthStateListener(authStateListener);
                    }
                }));
            }
        });
    }
}