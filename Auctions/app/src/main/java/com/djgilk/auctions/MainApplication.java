package com.djgilk.auctions;

import android.app.Application;

import com.djgilk.auctions.injection.DaggerMainComponent;
import com.djgilk.auctions.injection.MainComponent;
import com.djgilk.auctions.injection.MainModule;
import com.djgilk.auctions.presenter.ViewPresenter;

import java.util.Stack;

import timber.log.Timber;

/**
 * Created by dangilk on 2/25/16.
 */
public class MainApplication extends Application {

    private MainComponent mainComponent;

    // TODO OH GOD DONT DO THIS IT LEAKS LIKE CRAZY?
    private Stack<ViewPresenter> backStack = new Stack<ViewPresenter>();
    private ViewPresenter currentPresenter;

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

        //can add this line to enable memory leak detection
        //LeakCanary.install(this);

        // If a Dagger 2 component does not have any constructor arguments for any of its modules,
        // then we can use .create() as a shortcut instead:
        //  mAppComponent = com.codepath.dagger.components.DaggerNetComponent.create();

    }

    public void addToBackStack(ViewPresenter viewPresenter) {
        this.backStack.push(viewPresenter);
    }

    public ViewPresenter popBackStack() {
        if (backStack.size() == 0) {
            return null;
        } else {
            return backStack.pop();
        }
    }

    public void setCurrentPresenter(ViewPresenter viewPresenter) {
        this.currentPresenter = viewPresenter;
    }

    public ViewPresenter getCurrentPresenter() {
        return currentPresenter;
    }

    public MainComponent getMainComponent() {
        return mainComponent;
    }

}
