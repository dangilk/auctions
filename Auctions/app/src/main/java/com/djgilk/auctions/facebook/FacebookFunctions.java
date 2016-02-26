package com.djgilk.auctions.facebook;

import android.view.View;

import java.lang.ref.WeakReference;

import rx.functions.Action1;

/**
 * Created by dangilk on 2/25/16.
 */
public class FacebookFunctions {

    public static class HideLoginButton implements Action1<FbAuthEvent> {
        final private WeakReference<View> buttonRef;

        public HideLoginButton(View buttonRef) {
            this.buttonRef = new WeakReference<View>(buttonRef);
        }

        @Override
        public void call(FbAuthEvent fbAuthEvent) {
            final View button = buttonRef.get();
            if (button != null) {
                button.setVisibility(fbAuthEvent.isLoggedIn() ? View.GONE : View.VISIBLE);
            }
        }
    }
}
