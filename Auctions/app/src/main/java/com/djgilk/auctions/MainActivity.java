package com.djgilk.auctions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.djgilk.auctions.helper.RxAndroid;
import com.djgilk.auctions.helper.RxHelper;
import com.djgilk.auctions.helper.RxPublisher;
import com.djgilk.auctions.helper.ViewUtils;
import com.djgilk.auctions.presenter.AuctionPresenter;
import com.djgilk.auctions.presenter.LoginPresenter;
import com.djgilk.auctions.presenter.ProfilePresenter;
import com.djgilk.auctions.presenter.ViewPresenter;
import com.djgilk.auctions.view.PresenterHolder;
import com.facebook.FacebookSdk;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements PresenterHolder {
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    final Map<String,ViewPresenter> presenterMap = new HashMap<String,ViewPresenter>();

    @Inject
    LoginPresenter loginPresenter;

    @Inject
    AuctionPresenter auctionPresenter;

    @Inject
    ProfilePresenter profilePresenter;

    @Inject
    MainApplication mainApplication;

    @Inject
    RxPublisher rxPublisher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate mainActivity, savedInstanceState: " + savedInstanceState);
        super.onCreate(savedInstanceState);
        getMainApplication().getMainComponent().inject(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            Timber.d("publish observables");
            getMainApplication().setCurrentPresenterTag(loginPresenter.getPresenterTag());
            rxPublisher.publish(this);
        } else {
            Timber.d("not re-publishing observables");
        }


        // initialize presenters
        initPresenterMap(profilePresenter, loginPresenter, auctionPresenter);
        profilePresenter.onCreate(this);
        loginPresenter.onCreate(this);
        auctionPresenter.onCreate(this);

        showCurrentPresenter();

        if (savedInstanceState == null) {
            compositeSubscription.add(rxPublisher.getObservablesCompleteObservable().zipWith(Observable.just(null).delay(1, TimeUnit.SECONDS), new RxHelper.ZipWaiter())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(new RxAndroid.ToLayoutFade(getMainApplication(), loginPresenter, auctionPresenter, false))
                    .subscribe(new Observer<Object>() {
                        @Override
                        public void onCompleted() {
                            Timber.i("layout transition onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.i("layout transition onError: " + e.getMessage());
                        }

                        @Override
                        public void onNext(Object aBoolean) {
                            Timber.i("layout transition onNext");
                        }
                    }));
        }

        if (savedInstanceState == null) {
            Timber.d("connect observables");
            rxPublisher.connect();
        } else {
            Timber.d("not re-connecting observables");
        }
    }


    void initPresenterMap(ViewPresenter... presenters) {
        for (ViewPresenter presenter: presenters) {
            presenterMap.put(presenter.getPresenterTag(), presenter);
        }
    }

    @Override
    public Map<String,ViewPresenter> getPresenterMap() {
        return presenterMap;
    }

    void showCurrentPresenter() {
        MainApplication mainApplication = getMainApplication();
        String currentPresenterTag = mainApplication.getCurrentPresenterTag();
        if (currentPresenterTag == null) {
            loginPresenter.setVisibility(View.VISIBLE);
        } else {
            presenterMap.get(currentPresenterTag).setVisibility(View.VISIBLE);
        }
    }

    public Func1<Object, Observable<Object>> fadePresenters(String presenterTag1, String presenterTag2, boolean addToBackstack) {
        return new RxAndroid.ToLayoutFade(getMainApplication(), presenterMap.get(presenterTag1), presenterMap.get(presenterTag2), addToBackstack);
    }

    private MainApplication getMainApplication() {
        return ((MainApplication) getApplication());
    }

    @Override
    protected void onDestroy() {
        Timber.d("onDestroy mainActivity");
        loginPresenter.onDestroy();
        auctionPresenter.onDestroy();
        profilePresenter.onDestroy();
        compositeSubscription.unsubscribe();
        compositeSubscription = new CompositeSubscription();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (rxPublisher.onActivityResult(requestCode, resultCode, data)) {
            return;
        } else if (auctionPresenter.onActivityResult(requestCode, resultCode, data)) {
            return;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!ViewUtils.goBack(mainApplication, this)) {
            super.onBackPressed();
        }
    }
}
