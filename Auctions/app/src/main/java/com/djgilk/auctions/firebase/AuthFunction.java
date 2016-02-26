package com.djgilk.auctions.firebase;

import com.djgilk.auctions.facebook.FbAuthEvent;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by dangilk on 2/25/16.
 */
public class AuthFunction implements Func1<FbAuthEvent, Observable<FireBaseAuthEvent>> {
//    private void onFacebookAccessTokenChange(AccessToken token) {
//        if (token != null) {
//            ref.authWithOAuthToken("facebook", token.getToken(), new Firebase.AuthResultHandler() {
//                @Override
//                public void onAuthenticated(AuthData authData) {
//                    // The Facebook user is now authenticated with your Firebase app
//                }
//                @Override
//                public void onAuthenticationError(FirebaseError firebaseError) {
//                    // there was an error
//                }
//            });
//        } else {
//        /* Logged out of Facebook so do a logout from the Firebase app */
//            ref.unauth();
//        }
//    }

    @Override
    public Observable<FireBaseAuthEvent> call(FbAuthEvent fbAuthEvent) {
        //return Observable.just(new Auth);
        return null;
    }
}
