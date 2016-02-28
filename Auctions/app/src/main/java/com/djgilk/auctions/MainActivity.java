package com.djgilk.auctions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.djgilk.auctions.helper.RxAndroid;
import com.djgilk.auctions.presenter.AuctionPresenter;
import com.djgilk.auctions.presenter.LoginPresenter;
import com.djgilk.auctions.view.LinearLayoutContainer;
import com.facebook.FacebookSdk;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observer;

public class MainActivity extends AppCompatActivity {

    @Inject
    LoginPresenter loginPresenter;

    @Inject
    AuctionPresenter auctionPresenter;

    @Inject
    MainApplication mainApplication;

    @Bind(R.id.container)
    LinearLayoutContainer container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainApplication) getApplication()).getMainComponent().inject(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        loginPresenter.onCreate(this);
        auctionPresenter.onCreate(this);

        loginPresenter.observeLogin(this).flatMap(new RxAndroid.ToLayoutFade(mainApplication, loginPresenter, auctionPresenter))
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {
                        Log.i("Dan", "initialization complete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("Dan", "initialization error");
                    }

                    @Override
                    public void onNext(Object success) {
                        Log.i("Dan", "initialized successfully");
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginPresenter.onDestroy();
        auctionPresenter.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginPresenter.onActivityResult(requestCode, resultCode, data);
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
