package com.djgilk.auctions.firebase;

import android.util.Log;

import com.djgilk.auctions.facebook.FacebookAuthEvent;
import com.djgilk.auctions.model.User;
import com.djgilk.auctions.model.UserMappings;
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

    public Func1<FirebaseAuthEvent, Observable<FirebaseAuthEvent>> toFirebaseUserId() {
        return new Func1<FirebaseAuthEvent, Observable<FirebaseAuthEvent>>() {
            @Override
            public Observable<FirebaseAuthEvent> call(FirebaseAuthEvent firebaseAuthEvent) {
                return observeFirebaseUserMapping(firebaseAuthEvent);
            }
        };
    }

    public Observable<FirebaseAuthEvent> observeFirebaseUserMapping(final FirebaseAuthEvent firebaseAuthEvent) {
        return Observable.create(new Observable.OnSubscribe<FirebaseAuthEvent>() {
            @Override
            public void call(final Subscriber<? super FirebaseAuthEvent> subscriber) {
                final String uid = firebaseAuthEvent.getAuthData().getUid();
                final String provider = firebaseAuthEvent.getAuthData().getProvider();
                final String userMapPath = UserMappings.getUserMappingsRoot() + provider + "/" + uid;
                Log.d("Dan", "get firebase userMapping at path: " + userMapPath);
                final Firebase existingUserMapRef = firebase.child(userMapPath);
                final ValueEventListener listener = existingUserMapRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot == null || !dataSnapshot.exists()) {
                            // no user mapping
                            Log.d("Dan", "no user mapping");
                        } else {
                            //existing user mapping
                            Log.d("Dan", "found existing user mapping");
                            String firebaseId = dataSnapshot.getValue(String.class);
                            firebaseAuthEvent.setFirebaseUid(firebaseId);
                        }
                        subscriber.onNext(firebaseAuthEvent);
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {
                        Log.d("Dan", "firebase error getting user mapping");
                        // Turn the FirebaseError into a throwable to conform to the API
                        subscriber.onError(new FirebaseException(error.getMessage()));
                    }
                });

                // When the subscription is cancelled, remove the listener
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        existingUserMapRef.removeEventListener(listener);
                    }
                }));
            }
        });
    }

    public Func1<FirebaseAuthEvent, Observable<User>> toFirebaseUser() {
        return new Func1<FirebaseAuthEvent, Observable<User>>() {
            @Override
            public Observable<User> call(FirebaseAuthEvent firebaseId) {
                return observeFirebaseUser(firebaseId);
            }
        };
    }

    public Observable<User> observeFirebaseUser(final FirebaseAuthEvent firebaseAuthEvent) {
        return Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(final Subscriber<? super User> subscriber) {
                final String firebaseId = firebaseAuthEvent.getFirebaseUid();
                if (firebaseId == null) {
                    // create new user
                    Log.d("Dan", "creating new user");
                    final Firebase usersRef = firebase.child(User.getParentRootPath());
                    final Firebase userRef = usersRef.push();
                    final String newUserId = userRef.getKey();
                    User user = new User();
                    user.setFirebaseId(newUserId);
                    userRef.setValue(user);

                    // update user mappings
                    final AuthData authData = firebaseAuthEvent.getAuthData();
                    final Firebase mappingsRef = firebase.child(UserMappings.getUserMappingsRoot() + authData.getProvider() + "/" + authData.getUid() );
                    // setting the mapping will trigger an event upstream
                    mappingsRef.setValue(newUserId);
                    return;
                }

                String userPath = User.getParentRootPath() + firebaseId;
                Log.d("Dan", "get firebase user at path: " + userPath);
                final Firebase existingUserRef = firebase.child(userPath);
                final ValueEventListener listener = existingUserRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot == null || !dataSnapshot.exists()) {
                            subscriber.onError(new RuntimeException("could not get existing user"));
                        } else {
                            //existing user
                            Log.d("Dan", "found existing user");
                            User user = dataSnapshot.getValue(User.class);
                            subscriber.onNext(user);
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {
                        Log.d("Dan", "firebase error getting user");
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
                                Log.d("Dan", "facebook is logged in");
                                firebase.authWithOAuthToken("facebook", facebookAuthEvent.getAccessToken().getToken(), new Firebase.AuthResultHandler() {
                                    @Override
                                    public void onAuthenticated(AuthData authData) {
                                        // no op, process auth in else clause below
                                        Log.d("Dan", "firebase newly authed");
                                    }

                                    @Override
                                    public void onAuthenticationError(FirebaseError firebaseError) {
                                        Log.w("Dan", "firebase auth error");
                                        subscriber.onError(firebaseError.toException());
                                    }
                                });
                            } else {
                                Log.w("Dan", "no facebook or firebase auth - error");
                                firebase.unauth();
                            }
                        } else {
                            // firebase already authed
                            Log.i("Dan", "firebase already authed. uid = " + authData.getUid());
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