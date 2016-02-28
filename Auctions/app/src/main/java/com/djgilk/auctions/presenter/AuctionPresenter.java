package com.djgilk.auctions.presenter;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.djgilk.auctions.R;
import com.djgilk.auctions.helper.RxAndroid;

import javax.inject.Inject;

import butterknife.Bind;

/**
 * Created by dangilk on 2/27/16.
 */
public class AuctionPresenter extends ViewPresenter {

    @Bind(R.id.ll_auction)
    LinearLayout auctionLayout;

    @Bind(R.id.iv_auctionImage)
    ImageView ivAuctionImage;

    @Inject
    public AuctionPresenter(){};

    @Override
    public void onCreate(Activity activity) {
        super.onCreate(activity);

        RxAndroid.loadImage(ivAuctionImage, "http://www.heraldnet.com/apps/pbcsi.dll/bilde?Site=DH&Date=20090924&Category=NEWS01&ArtNo=709249870&Ref=AR&MaxW=800&MaxH=800&q=90");
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public View getLayout() {
        return auctionLayout;
    }
}
