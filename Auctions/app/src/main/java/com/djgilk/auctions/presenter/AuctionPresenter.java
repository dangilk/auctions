package com.djgilk.auctions.presenter;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.djgilk.auctions.R;
import com.djgilk.auctions.firebase.RxFirebase;
import com.djgilk.auctions.helper.RxAndroid;
import com.djgilk.auctions.helper.RxPublisher;
import com.djgilk.auctions.model.ClientConfig;
import com.djgilk.auctions.model.CurrentItem;

import javax.inject.Inject;

import butterknife.Bind;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by dangilk on 2/27/16.
 */
public class AuctionPresenter extends ViewPresenter {
    private final static String AUCTION_PRESENTER_TAG = "auctionPresenter";

    @Inject
    RxFirebase rxFirebase;

    @Inject
    RxPublisher rxPublisher;

    @Bind(R.id.ll_auction)
    LinearLayout auctionLayout;

    @Bind(R.id.iv_auctionImage)
    ImageView ivAuctionImage;

    @Bind(R.id.tv_auctionTitle)
    TextView tvAuctionTitle;

    @Inject
    public AuctionPresenter(){};

    public Observable<Boolean> onCreate(Activity activity) {
        super.onCreate(activity);
        //final ConnectableObservable<FirebaseAuthEvent> connectableAuthEvent = authEvent.publish();
        //final Observable<Boolean> observeCurrentItem =
                rxPublisher.getCurrentItemObservable().observeOn(Schedulers.io()).flatMap(new LoadedCurrentItem(ivAuctionImage)).subscribe();
       // final Observable<ClientConfig> observeClientConfig = rxPublisher.getClientConfigObservable();
        rxPublisher.getClientConfigObservable().subscribe(new Observer<ClientConfig>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(ClientConfig clientConfig) {
                tvAuctionTitle.setText(clientConfig.getTest());
            }
        });
        //return observeCurrentItem.zipWith(observeClientConfig, new RxHelper.ZipWaiter());
        return Observable.just(true);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public View getLayout() {
        return auctionLayout;
    }

    @Override
    public String getPresenterTag() {
        return AUCTION_PRESENTER_TAG;
    }


    public static class LoadedCurrentItem implements Func1<CurrentItem, Observable<Boolean>> {
        private final ImageView imageView;

        public LoadedCurrentItem(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        public Observable<Boolean> call(CurrentItem currentItem) {
            return RxAndroid.observeBitmap(currentItem.getImageUrl()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).flatMap(new RxAndroid.ToLoadedImageView(imageView));
        }
    }
}
