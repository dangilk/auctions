package com.djgilk.auctions.model;

/**
 * Created by dangilk on 3/1/16.
 */
public class User {
    // fb values
    private final String id;
    private final String displayName;
    private final String profileImageURL;

    // other values
    private final int coins;
    private final String address1;
    private final String address2;
    private final String city;
    private final String state;
    private final String zip;

    public User(String id, String displayName, String profileImageURL, int coins, String address1
            , String address2, String city, String state, String zip) {
        this.id = id;
        this.displayName = displayName;
        this.profileImageURL = profileImageURL;
        this.coins = coins;
        this.address1 = address1;
        this.address2 = address2;
        this.city = city;
        this.state = state;
        this.zip = zip;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    public int getCoins() {
        return coins;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }
}
