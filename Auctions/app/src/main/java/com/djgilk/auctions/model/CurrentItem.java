package com.djgilk.auctions.model;

import com.djgilk.auctions.firebase.RxFirebase;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by dangilk on 2/27/16.
 */
public class CurrentItem {
    private String name;
    private int highBid;
    private String imageUrl;
    private int price;
    private long auctionEndTimeMillis;

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getPrice() {
        return price;
    }

    public int getHighBid() {
        return highBid;
    }

    public long getAuctionEndTimeMillis() {
        return auctionEndTimeMillis;
    }

    public static String getParentRootPath() {
        return "items";
    }

    public static String getHighBidKey() {
        return "highBid";
    }

    public static Func1<AuctionState, Observable<CurrentItem>> fromAuctionState(final RxFirebase rxFirebase) {
        return new Func1<AuctionState, Observable<CurrentItem>>() {
            @Override
            public Observable<CurrentItem> call(AuctionState auctionState) {
                final String itemPath = getParentRootPath() + "/" + auctionState.getAuctionItemId();
                return rxFirebase.observeFirebaseObject(itemPath, CurrentItem.class);
            }
        };
    }

    @Override
    public String toString() {
        return "CurrentItem {highBid: "+highBid+"}";
    }
}
