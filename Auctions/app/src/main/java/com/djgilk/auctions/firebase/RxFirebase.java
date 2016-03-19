package com.djgilk.auctions.firebase;

import com.djgilk.auctions.facebook.FacebookAuthEvent;
import com.djgilk.auctions.model.User;
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
import timber.log.Timber;

/**
 * Created by dangilk on 2/26/16.
 */
@Singleton
public class RxFirebase {

    @Inject
    Firebase firebase;

    @Inject
    public RxFirebase(){}


    public Func1<Boolean, Observable<Long>> toFirebaseClockOffset() {
        return new Func1<Boolean, Observable<Long>>() {
            @Override
            public Observable<Long> call(Boolean b) {
                return observeFirebaseClockOffset();
            }
        };
    }

    public Observable<Long> observeFirebaseClockOffset() {
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(final Subscriber<? super Long> subscriber) {
                Timber.d("get firebase time offset");
                final Firebase infoRef = firebase.child(".info/serverTimeOffset");
                final ValueEventListener listener = infoRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Long clockOffset = (Long) dataSnapshot.getValue();
                        Timber.d("clock offset was updated: " + clockOffset);
                        subscriber.onNext(clockOffset);
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {
                        Timber.d("firebase error getting clock offset");
                        // Turn the FirebaseError into a throwable to conform to the API
                        subscriber.onError(new FirebaseException(error.getMessage()));
                    }
                });

                // When the subscription is cancelled, remove the listener
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        infoRef.removeEventListener(listener);
                    }
                }));
            }
        });
    }

    public Func1<User, Observable<Boolean>> toLoginState() {
        return new Func1<User, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call(User user) {
                Timber.d("login flow complete: user = " + user);
                return Observable.just(user != null);
            }
        };
    }

    public Func1<FirebaseAuthEvent, Observable<User>> toFirebaseUserCreation() {
        return new Func1<FirebaseAuthEvent, Observable<User>>() {
            @Override
            public Observable<User> call(FirebaseAuthEvent firebaseAuthEvent) {
                return observeFirebaseUserCreation(firebaseAuthEvent);
            }
        };
    }

    public Observable<User> observeFirebaseUserCreation(final FirebaseAuthEvent firebaseAuthEvent) {
        return Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(final Subscriber<? super User> subscriber) {
                final String uid = firebaseAuthEvent.getAuthData().getUid();
                final String userPath = User.getParentRootPath() + uid;
                Timber.d("get firebase user at path: " + userPath);
                final Firebase existingUserRef = firebase.child(userPath);
                final ValueEventListener listener = existingUserRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = null;
                        if (dataSnapshot == null || !dataSnapshot.exists()) {
                            // create new user
                            Timber.d("creating new user");
                            final Firebase userRef = firebase.child(userPath);
                            user = new User();
                            user.setFacebookId(uid);
                            userRef.setValue(user);
                        } else {
                            // existing user
                            user = dataSnapshot.getValue(User.class);
                            Timber.d("found existing user: " + user);
                        }
                        existingUserRef.removeEventListener(this);
                        subscriber.onNext(user);
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {
                        Timber.d("firebase error getting user");
                        // Turn the FirebaseError into a throwable to conform to the API
                        subscriber.onError(new FirebaseException(error.getMessage()));
                    }
                });

                // When the subscription is cancelled, remove the listener
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        existingUserRef.removeEventListener(listener);
                    }
                }));
            }
        });
    }

    public Func1<User, Observable<User>> toFirebaseUser() {
        return new Func1<User, Observable<User>>() {
            @Override
            public Observable<User> call(User user) {
                return observeFirebaseObject(User.getParentRootPath() + user.getFacebookId(), User.class);
            }
        };
    }

    public <T extends Object> Func1<Boolean, Observable<T>> toFirebaseObject(final String childRef, final Class<T> clazz) {
        return new Func1<Boolean, Observable<T>>() {
            @Override
            public Observable<T> call(Boolean firebaseAuthEvent) {
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
                        Timber.d("data object updated");
                        //for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        T object = (T) dataSnapshot.getValue(clazz);
                        //}
                        subscriber.onNext(object);
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
                Timber.d("call firebase observable");
                final Firebase.AuthStateListener authStateListener = new Firebase.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(AuthData authData) {
                        Timber.d("firebase auth state updated");
                        if (authData == null) {
                            Timber.d("firebase not authed");
                            // firebase not authed
                            if (facebookAuthEvent.isLoggedIn()) {
                                Timber.d("facebook is logged in");
                                firebase.authWithOAuthToken("facebook", facebookAuthEvent.getAccessToken().getToken(), new Firebase.AuthResultHandler() {
                                    @Override
                                    public void onAuthenticated(AuthData authData) {
                                        // no op, process auth in else clause below
                                        Timber.d("firebase newly authed");
                                    }

                                    @Override
                                    public void onAuthenticationError(FirebaseError firebaseError) {
                                        Timber.w("firebase auth error");
                                        subscriber.onError(firebaseError.toException());
                                    }
                                });
                            } else {
                                Timber.w("no facebook or firebase auth - error");
                                firebase.unauth();
                            }
                        } else {
                            // firebase already authed
                            Timber.d("firebase already authed. uid = " + authData.getUid());
                            for (String value : authData.getProviderData().keySet()) {
                                //Timber.d("fb key: " + value);
                            }
                            final String uid = authData.getUid();
                            //final User user = firebase.
                            subscriber.onNext(new FirebaseAuthEvent(authData));
                        }
                    }
                };

                Timber.d("adding firebase auth state listener");
                firebase.addAuthStateListener(authStateListener);

                // When the subscription is cancelled, remove the listener
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        Timber.d("removing firebase auth listener");
                        firebase.removeAuthStateListener(authStateListener);
                    }
                }));
            }
        });
    }
}