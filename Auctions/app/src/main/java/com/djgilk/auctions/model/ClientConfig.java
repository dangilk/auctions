package com.djgilk.auctions.model;

/**
 * Created by dangilk on 2/26/16.
 */
public class ClientConfig {
    private boolean killSwitch;
    private String test;

    public ClientConfig(){};

    public static String getRootPath() {
        return "clientConfig";
    }

    public boolean isKillSwitch() {
        return killSwitch;
    }

    public String getTest() {
        return test;
    }
}
