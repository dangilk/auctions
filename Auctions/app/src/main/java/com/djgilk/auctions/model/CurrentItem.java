package com.djgilk.auctions.model;

/**
 * Created by dangilk on 2/27/16.
 */
public class CurrentItem {
    private String name;
    private String highBid;
    private String imageUrl;
    private int price;
    private long auctionEndTimeMillis;

    public static String getRootPath() {
        return "sharedState/currentItem";
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getPrice() {
        return price;
    }

    public String getHighBid() {
        return highBid;
    }

    public long getAuctionEndTimeMillis() {
        return auctionEndTimeMillis;
    }
}
