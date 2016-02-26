package com.djgilk.auctions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.djgilk.auctions.presenter.LoginPresenter;
import com.djgilk.auctions.view.LinearLayoutContainer;
import com.facebook.FacebookSdk;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Inject
    LoginPresenter loginPresenter;

    @Bind(R.id.container)
    LinearLayoutContainer container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainApplication) getApplication()).getMainComponent().inject(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        //Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        loginPresenter.onCreate(this);
        //firebase = new Firebase(FIREBASE_URL);

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
