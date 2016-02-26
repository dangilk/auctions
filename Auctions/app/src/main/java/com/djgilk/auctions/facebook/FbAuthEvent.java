package com.djgilk.auctions.facebook;

import com.facebook.AccessToken;

/**
 * Created by dangilk on 2/25/16.
 */
public class FbAuthEvent {
    final private AccessToken accessToken;
    public FbAuthEvent(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public boolean isLoggedIn() {
        return accessToken != null;
    }

    public static class Observable<FbAuthEvent> {
        
    }
}
