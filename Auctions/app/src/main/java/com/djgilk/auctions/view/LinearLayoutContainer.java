package com.djgilk.auctions.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by dangilk on 2/25/16.
 */
public class LinearLayoutContainer extends LinearLayout implements Container {

    public LinearLayoutContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void showItem(String item) {

    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
