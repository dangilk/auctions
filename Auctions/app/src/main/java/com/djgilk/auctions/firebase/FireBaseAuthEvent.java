package com.djgilk.auctions.firebase;

import com.firebase.client.AuthData;

/**
 * Created by dangilk on 2/25/16.
 */
public class FirebaseAuthEvent {
    final private AuthData authData;
    private String firebaseUid;

    public FirebaseAuthEvent(AuthData authData) {
        this.authData = authData;
    }

    public AuthData getAuthData() {
        return authData;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }
}
