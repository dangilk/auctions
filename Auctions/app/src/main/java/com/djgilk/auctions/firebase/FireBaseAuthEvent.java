package com.djgilk.auctions.firebase;

import com.firebase.client.AuthData;

/**
 * Created by dangilk on 2/25/16.
 */
public class FireBaseAuthEvent {
    final private AuthData authData;

    public FireBaseAuthEvent(AuthData authData) {
        this.authData = authData;
    }
}
