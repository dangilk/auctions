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
import com.djgilk.auctions.model.CurrentItem;
import com.djgilk.auctions.model.PrettyTime;
import com.djgilk.auctions.model.User;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by dangilk on 2/27/16.
 */
public class AuctionPresenter extends ViewPresenter {
    private final static String AUCTION_PRESENTER_TAG = "auctionPresenter";

    final CompositeSubscription compositeSubscription = new CompositeSubscription();

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

    @Bind(R.id.tv_highBid)
    TextView tvHighBid;

    @Bind(R.id.tv_userCoins)
    TextView tvUserCoins;

    @Bind(R.id.tv_auctionTimeLeft)
    TextView tvAuctionTimeLeft;

    @Bind(R.id.tv_auctionTimeLeftUnits)
    TextView tvAuctionTimeLeftUnits;

    @Inject
    public AuctionPresenter(){};

    public void onCreate(Activity activity) {
        super.onCreate(activity);
        compositeSubscription.add(rxPublisher.getCurrentItemObservable().observeOn(Schedulers.io())
                .flatMap(new LoadedCurrentItem()).subscribe());
        compositeSubscription.add(rxPublisher.getClientConfigObservable().subscribe());
        compositeSubscription.add(rxPublisher.getUserObservable().subscribe(new UpdateUser()));
        compositeSubscription.add(
                Observable.combineLatest(rxPublisher.getClockOffsetObservable(),
                        Observable.interval(1, TimeUnit.SECONDS),
                        rxPublisher.getCurrentItemObservable(),
                        new OffsetClock())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new UpdateClock()));
    }

    @Override
    public void onDestroy() {
        compositeSubscription.unsubscribe();
    }

    @Override
    public View getLayout() {
        return auctionLayout;
    }

    @Override
    public String getPresenterTag() {
        return AUCTION_PRESENTER_TAG;
    }

    public class UpdateUser implements Action1<User> {
        @Override
        public void call(User user) {
            tvUserCoins.setText(String.valueOf(user.getCoins()));
        }
    }

    public class LoadedCurrentItem implements Func1<CurrentItem, Observable<Boolean>> {
        @Override
        public Observable<Boolean> call(CurrentItem currentItem) {
            return RxAndroid.observeBitmap(currentItem.getImageUrl()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).flatMap(new RxAndroid.ToLoadedImageView(ivAuctionImage))
                    .flatMap(new RxAndroid.ToUpdatedTextView(tvAuctionTitle, currentItem.getName()))
                    .flatMap(new RxAndroid.ToUpdatedTextView(tvHighBid, currentItem.getHighBid()));
        }
    }

    public class OffsetClock implements Func3<Long, Long, CurrentItem, PrettyTime> {
        @Override
        public PrettyTime call(Long clockOffset, Long interval, CurrentItem currentItem) {
            return new PrettyTime(currentItem.getAuctionEndTimeMillis(), System.currentTimeMillis() + clockOffset);
        }
    }

    public class UpdateClock implements Action1<PrettyTime> {
        @Override
        public void call(PrettyTime prettyTime) {
            prettyTime.setText(tvAuctionTimeLeft, tvAuctionTimeLeftUnits);
        }
    }

}
