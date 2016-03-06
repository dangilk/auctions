package com.djgilk.auctions.model;

/**
 * Created by dangilk on 3/6/16.
 */
public class UserMappings {
    final static String FACEBOOK_MAPPING = "facebook";
    final static String USER_MAPPINGS_ROOT = "userMappings/";

    public static String getFacebookMapping() {
        return FACEBOOK_MAPPING;
    }

    public static String getUserMappingsRoot() {
        return USER_MAPPINGS_ROOT;
    }
}
