package com.djgilk.auctions;

import android.app.Application;

import com.djgilk.auctions.injection.DaggerMainComponent;
import com.djgilk.auctions.injection.MainComponent;
import com.djgilk.auctions.injection.MainModule;
import com.pavlospt.androidiap.billing.BillingProcessor;

import java.util.Stack;

import timber.log.Timber;

/**
 * Created by dangilk on 2/25/16.
 */
public class MainApplication extends Application {

    private MainComponent mainComponent;

    private Stack<String> backStack = new Stack<String>();
    private String currentPresenterTag;

    @Override
    public void onCreate() {
        super.onCreate();

        //if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        //}

        // Dagger%COMPONENT_NAME%
        mainComponent = DaggerMainComponent.builder()
                // list of modules that are part of this component need to be created here too
                .mainModule(new MainModule(this)) // This also corresponds to the name of your module: %component_name%Module
                .build();

        BillingProcessor.init(this);

        //can add this line to enable memory leak detection
        //LeakCanary.install(this);

        // If a Dagger 2 component does not have any constructor arguments for any of its modules,
        // then we can use .create() as a shortcut instead:
        //  mAppComponent = com.codepath.dagger.components.DaggerNetComponent.create();

    }

    public void addToBackStack(String viewPresenterTag) {
        this.backStack.push(viewPresenterTag);
    }

    public String popBackStack() {
        if (backStack.size() == 0) {
            return null;
        } else {
            return backStack.pop();
        }
    }

    public void setCurrentPresenterTag(String viewPresenterTag) {
        this.currentPresenterTag = viewPresenterTag;
    }

    public String getCurrentPresenterTag() {
        return currentPresenterTag;
    }

    public MainComponent getMainComponent() {
        return mainComponent;
    }

}
