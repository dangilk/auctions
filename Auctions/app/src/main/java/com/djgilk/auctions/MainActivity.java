package com.djgilk.auctions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.djgilk.auctions.helper.RxAndroid;
import com.djgilk.auctions.helper.RxHelper;
import com.djgilk.auctions.helper.RxPublisher;
import com.djgilk.auctions.presenter.AuctionPresenter;
import com.djgilk.auctions.presenter.LoginPresenter;
import com.facebook.FacebookSdk;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;

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
        ((MainApplication) getApplication()).getMainComponent().inject(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        rxPublisher.publish(this);
        //final ConnectableObservable<FirebaseAuthEvent> authObservable = rxAuth.publishAuthEvents(this);

        Observable<Boolean> loginObservable = loginPresenter.onCreate(this);
        Observable<Boolean> auctionObservable = auctionPresenter.onCreate(this);

        Observable.zip(loginObservable, auctionObservable, new RxHelper.ZipWaiter())
        .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new RxAndroid.ToLayoutFade(mainApplication, loginPresenter, auctionPresenter))
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {
                        Log.i("Dan", "layout transition onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("Dan", "layout transition onError: " + e.getMessage());
                    }

                    @Override
                    public void onNext(Object aBoolean) {
                        Log.i("Dan", "layout transition onNext");
                    }
                });
        rxPublisher.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rxPublisher.unsubscribe();
        loginPresenter.onDestroy();
        auctionPresenter.onDestroy();
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
