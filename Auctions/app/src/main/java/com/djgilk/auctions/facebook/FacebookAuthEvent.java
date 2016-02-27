package com.djgilk.auctions.facebook;

import com.facebook.AccessToken;

/**
 * Created by dangilk on 2/25/16.
 */
public class FacebookAuthEvent {
    final private AccessToken accessToken;
    public FacebookAuthEvent(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public boolean isLoggedIn() {
        return accessToken != null;
    }

    public AccessToken getAccessToken(){
        return accessToken;
    };
}
