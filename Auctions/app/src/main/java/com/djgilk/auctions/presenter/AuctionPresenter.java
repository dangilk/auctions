package com.djgilk.auctions.presenter;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.djgilk.auctions.R;
import com.djgilk.auctions.firebase.FirebaseAuthEvent;
import com.djgilk.auctions.firebase.RxFirebase;
import com.djgilk.auctions.helper.RxAndroid;
import com.djgilk.auctions.helper.RxHelper;
import com.djgilk.auctions.model.ClientConfig;
import com.djgilk.auctions.model.CurrentItem;

import javax.inject.Inject;

import butterknife.Bind;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

/**
 * Created by dangilk on 2/27/16.
 */
public class AuctionPresenter extends ViewPresenter {
    private final static String AUCTION_PRESENTER_TAG = "auctionPresenter";
    private Subscription clientConfigSubscription;

    @Inject
    RxFirebase rxFirebase;

    @Bind(R.id.ll_auction)
    LinearLayout auctionLayout;

    @Bind(R.id.iv_auctionImage)
    ImageView ivAuctionImage;

    @Bind(R.id.tv_auctionEnabled)
    TextView tvAuctionEnabled;

    @Inject
    public AuctionPresenter(){};

    public Observable<Boolean> onCreate(Activity activity, ConnectableObservable<FirebaseAuthEvent> authEvent) {
        super.onCreate(activity);
        final ConnectableObservable<FirebaseAuthEvent> connectableAuthEvent = authEvent.publish();
        final Observable<Boolean> observeCurrentItem =
                rxFirebase.observeFirebaseObject(connectableAuthEvent, CurrentItem.getRootPath(), CurrentItem.class)
                .observeOn(Schedulers.io()).flatMap(new LoadedCurrentItem(ivAuctionImage));
        final Observable<ClientConfig> observeClientConfig =
                rxFirebase.observeFirebaseObject(connectableAuthEvent, ClientConfig.getRootPath(), ClientConfig.class);

        clientConfigSubscription = observeClientConfig.subscribe(new Observer<ClientConfig>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(ClientConfig clientConfig) {
                tvAuctionEnabled.setText(clientConfig.getTest());
            }
        });

        connectableAuthEvent.connect();
        return observeCurrentItem.zipWith(observeClientConfig, new RxHelper.ZipWaiter());
    }

    @Override
    public void onDestroy() {
        clientConfigSubscription.unsubscribe();
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
