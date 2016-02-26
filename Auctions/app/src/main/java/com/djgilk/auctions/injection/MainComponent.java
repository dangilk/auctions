package com.djgilk.auctions.injection;

import com.djgilk.auctions.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by dangilk on 2/25/16.
 */
@Singleton
@Component(modules = {MainModule.class})
public interface MainComponent {
    void inject(MainActivity activity);
}
