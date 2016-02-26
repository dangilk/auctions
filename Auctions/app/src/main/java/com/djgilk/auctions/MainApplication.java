package com.djgilk.auctions;

import android.app.Application;

import com.djgilk.auctions.injection.DaggerMainComponent;
import com.djgilk.auctions.injection.MainComponent;
import com.djgilk.auctions.injection.MainModule;

/**
 * Created by dangilk on 2/25/16.
 */
public class MainApplication extends Application {

    private MainComponent mainComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // Dagger%COMPONENT_NAME%
        mainComponent = DaggerMainComponent.builder()
                // list of modules that are part of this component need to be created here too
                .mainModule(new MainModule(this)) // This also corresponds to the name of your module: %component_name%Module
                .build();

        // If a Dagger 2 component does not have any constructor arguments for any of its modules,
        // then we can use .create() as a shortcut instead:
        //  mAppComponent = com.codepath.dagger.components.DaggerNetComponent.create();
    }

    public MainComponent getMainComponent() {
        return mainComponent;
    }
}
