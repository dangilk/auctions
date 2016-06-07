package com.djgilk.auctions.injection;

import com.djgilk.auctions.MainApplication;
import com.facebook.CallbackManager;
import com.firebase.client.Firebase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by dangilk on 2/25/16.
 */
@Module
public class MainModule {
    private final static String FIREBASE_URL = "https://djgauctions.firebaseIO.com";//"https://fiery-heat-6556.firebaseio.com/";
    private MainApplication mainApplication;

    public MainModule(MainApplication mainApplication) {
        this.mainApplication = mainApplication;
    }

    @Provides
    @Singleton
    public MainApplication provideMainApplication() {
        return mainApplication;
    }

    @Provides
    @Singleton
    public Firebase provideFirebase(MainApplication mainApplication) {
        Firebase.setAndroidContext(mainApplication);
        return new Firebase(FIREBASE_URL);
    }

    @Provides
    @Singleton
    public CallbackManager provideCallbackManager() {
        return CallbackManager.Factory.create();
    }
}
