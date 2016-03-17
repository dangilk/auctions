package com.djgilk.auctions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.djgilk.auctions.helper.RxAndroid;
import com.djgilk.auctions.helper.RxPublisher;
import com.djgilk.auctions.presenter.AuctionPresenter;
import com.djgilk.auctions.presenter.LoginPresenter;
import com.facebook.FacebookSdk;

import javax.inject.Inject;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @Inject
    LoginPresenter loginPresenter;

    @Inject
    AuctionPresenter auctionPresenter;

    @Inject
    MainApplication mainApplication;

    @Inject
    RxPublisher rxPublisher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMainApplication().getMainComponent().inject(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        rxPublisher.publish(this);

        // initialize presenters
        loginPresenter.onCreate(this);
        auctionPresenter.onCreate(this);

        rxPublisher.getObservablesCompleteObservable()
        .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new RxAndroid.ToLayoutFade(mainApplication, loginPresenter, auctionPresenter))
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
                });
        rxPublisher.connect();
    }

    private MainApplication getMainApplication() {
        return ((MainApplication) getApplication());
    }

    @Override
    protected void onDestroy() {
        loginPresenter.onDestroy();
        auctionPresenter.onDestroy();
        rxPublisher.unsubscribe();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        rxPublisher.onActivityResult(requestCode, resultCode, data);
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


}
