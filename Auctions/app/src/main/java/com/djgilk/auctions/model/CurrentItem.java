package com.djgilk.auctions.model;

/**
 * Created by dangilk on 2/27/16.
 */
public class CurrentItem {
    private String name;
    private String imageUrl;
    private int price;

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
}
