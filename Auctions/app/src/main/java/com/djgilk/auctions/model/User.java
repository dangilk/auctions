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
    private String email;
    private String country;

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

    public String getEmail() {
        return email;
    }

    public String getCountry() {
        return country;
    }

    public int getCoins() {
        return coins;
    }

    public void deductCoins(int coins) {
        if (this.coins >= coins) {
            this.coins -= coins;
        } else {
            this.coins = 0;
        }
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

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public static String getParentRootPath() {
        return "users/";
    }

    @Override
    public String toString() {
        return "User{coins: "+coins+", id: "+facebookId+"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (coins != user.coins) return false;
        if (!facebookId.equals(user.facebookId)) return false;
        if (displayName != null ? !displayName.equals(user.displayName) : user.displayName != null)
            return false;
        if (profileImageURL != null ? !profileImageURL.equals(user.profileImageURL) : user.profileImageURL != null)
            return false;
        if (address1 != null ? !address1.equals(user.address1) : user.address1 != null)
            return false;
        if (address2 != null ? !address2.equals(user.address2) : user.address2 != null)
            return false;
        if (city != null ? !city.equals(user.city) : user.city != null) return false;
        if (state != null ? !state.equals(user.state) : user.state != null) return false;
        return !(zip != null ? !zip.equals(user.zip) : user.zip != null);

    }

    @Override
    public int hashCode() {
        int result = facebookId.hashCode();
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (profileImageURL != null ? profileImageURL.hashCode() : 0);
        result = 31 * result + coins;
        result = 31 * result + (address1 != null ? address1.hashCode() : 0);
        result = 31 * result + (address2 != null ? address2.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (zip != null ? zip.hashCode() : 0);
        return result;
    }
}
