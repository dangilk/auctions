package com.djgilk.auctions.model;

/**
 * Created by dangilk on 3/20/16.
 */
public class AuctionState {
    private String auctionItemId;

    public String getAuctionItemId() {
        return auctionItemId;
    }

    public static String getRootPath() {
        return "auctionState";
    }
}
