package com.djgilk.auctions.model;

/**
 * Created by dangilk on 3/1/16.
 */
public class User {
    //private static String rootPath = "users/";

    // fb values
    private String facebookId;
    private String displayName;
    private String profileImageURL;

    // other values
    private int coins;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;

    public User() {
        new User(null,null,null,0,null,null,null,null,null);
    }

    public User(String facebookId, String displayName, String profileImageURL, int coins, String address1
            , String address2, String city, String state, String zip) {
        this.facebookId = facebookId;
        this.displayName = displayName;
        this.profileImageURL = profileImageURL;
        this.coins = coins;
        this.address1 = address1;
        this.address2 = address2;
        this.city = city;
        this.state = state;
        this.zip = zip;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
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

    public static String getParentRootPath() {
        return "users/";
    }

}
