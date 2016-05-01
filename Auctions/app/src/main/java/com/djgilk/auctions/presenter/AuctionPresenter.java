package com.djgilk.auctions.presenter;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.djgilk.auctions.R;
import com.djgilk.auctions.firebase.RxFirebase;
import com.djgilk.auctions.helper.RxAndroid;
import com.djgilk.auctions.helper.RxHelper;
import com.djgilk.auctions.helper.RxPublisher;
import com.djgilk.auctions.model.AuctionState;
import com.djgilk.auctions.model.Bid;
import com.djgilk.auctions.model.CurrentItem;
import com.djgilk.auctions.model.PrettyTime;
import com.djgilk.auctions.model.User;
import com.jakewharton.rxbinding.view.RxView;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
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

    @Bind(R.id.tv_yourBid)
    TextView tvYourBid;

    @Bind(R.id.tv_bidIncrement)
    TextView tvBidIncrement;

    @Bind(R.id.bt_bid)
    LinearLayout bidButton;

    @Inject
    public AuctionPresenter(){};

    public void onCreate(Activity activity) {
        super.onCreate(activity);
        compositeSubscription.add(rxPublisher.getCurrentItemObservable().doOnNext(new RxHelper.Log<CurrentItem>()).observeOn(Schedulers.io())
                .flatMap(new LoadedCurrentItem()).subscribe());
        compositeSubscription.add(rxPublisher.getClientConfigObservable().subscribe());
        compositeSubscription.add(rxPublisher.getUserObservable().doOnNext(new RxHelper.Log<User>()).flatMap(new UpdateUser()).subscribe());
        compositeSubscription.add(
                Observable.combineLatest(rxPublisher.getClockOffsetObservable(),
                        Observable.interval(1, TimeUnit.SECONDS),
                        rxPublisher.getCurrentItemObservable(),
                        new OffsetClock())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new UpdateClock()));
        compositeSubscription.add(rxPublisher.getAggregateBidObservable().subscribe(new UpdateMyBids()));
        // calculate incremental bid
        final ConnectableObservable<Long> incrementalBidObservable =
                Observable.combineLatest(rxPublisher.getAggregateBidObservable(),
                    rxPublisher.getCurrentItemObservable(),
                    toIncrementalBid()).publish();

        final ConnectableObservable<Long> deductCoinsObservable = Observable.concat(
                RxHelper.withLatestFrom(RxView.clicks(bidButton).throttleLast(1, TimeUnit.SECONDS), rxPublisher.getUserObservable(),
                        incrementalBidObservable, new ToCoinsToDeduct())).publish();

        Observable.zip(
                Observable.concat(RxHelper.withLatestFrom(deductCoinsObservable, rxPublisher.getUserObservable(),
                        rxPublisher.getAuctionStateObservable(), new InsertUserBid())),
                Observable.concat(deductCoinsObservable.withLatestFrom(rxPublisher.getUserObservable(), new DeductUserCoins())),
                Observable.concat(deductCoinsObservable.withLatestFrom(rxPublisher.getAuctionStateObservable(), new UpdateHighBid())),
                new FinalizeIncrementalBid()).subscribe();

        //update the incremental bid ui
        compositeSubscription.add(incrementalBidObservable.subscribe(new UpdateIncrementalBid()));

        //click bid button logic:
        //if has enough coins:
            //deduct coins, do fanfare, update button state to "youre in the lead!"
        // if not enough coins:
            //throw dialog: "not enough coins, buy coins?"

        incrementalBidObservable.connect();
        deductCoinsObservable.connect();
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

    public class UpdateUser implements Func1<User, Observable<User>> {
        @Override
        public Observable<User> call(User user) {
            tvUserCoins.setText(String.valueOf(user.getCoins()));
            return Observable.just(user);
        }
    }

    public class LoadedCurrentItem implements Func1<CurrentItem, Observable<Boolean>> {
        @Override
        public Observable<Boolean> call(CurrentItem currentItem) {
            return Observable.zip(
                    RxAndroid.observeBitmap(currentItem.getImageUrl()).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()).flatMap(new RxAndroid.ToLoadedImageView(ivAuctionImage)),
                    Observable.just(currentItem).observeOn(AndroidSchedulers.mainThread())
                            .flatMap(new RxAndroid.ToUpdatedTextView(tvAuctionTitle, currentItem.getName()))
                            .flatMap(new RxAndroid.ToUpdatedTextView(tvHighBid, String.valueOf(currentItem.getHighBid()))),
                    new RxHelper.ZipWaiter());
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

    public class UpdateMyBids implements Action1<Long> {
        @Override
        public void call(Long myBid) {
            tvYourBid.setText(String.valueOf(myBid));
        }
    }

    public class UpdateIncrementalBid implements Action1<Long> {
        @Override
        public void call(Long bid) {
            tvBidIncrement.setText(String.valueOf(bid));
        }
    }

    public class FinalizeIncrementalBid implements Func3<Bid, User, Boolean, Observable<Boolean>> {
        @Override
        public Observable<Boolean> call(Bid bid, User user, Boolean transactionResult) {
            return Observable.just(true);
        }
    }

    public class UpdateHighBid implements Func2<Long, AuctionState, Observable<Boolean>> {
        @Override
        public Observable<Boolean> call(Long bid, AuctionState auctionState) {
            Timber.d("update high bid");
            return rxFirebase.observableFirebaseObjectIncrementTransaction(
                   CurrentItem.getParentRootPath() + "/" + auctionState.getAuctionItemId() + "/" + CurrentItem.getHighBidKey());
        }
    }

    public class DeductUserCoins implements Func2<Long, User, Observable<User>> {
        @Override
        public Observable<User> call(Long bidAmount, final User user) {
            Timber.d("deduct user coins");
            user.deductCoins(bidAmount.intValue());
            return rxFirebase.observableFirebaseObjectUpdate(user, User.getParentRootPath() + "/" + user.getFacebookId(), false);
        }
    }

    public class InsertUserBid implements Func3<Long, User, AuctionState, Observable<Bid>> {
        @Override
        public Observable<Bid> call(Long bidAmount, User user, AuctionState auctionState) {
            Timber.d("insert user bid");
            Bid bid = new Bid(bidAmount);
            return rxFirebase.observableFirebaseObjectUpdate(bid,
                    Bid.getParentRootPath() + "/" + auctionState.getAuctionItemId() + "/" + user.getFacebookId(), true);
        }
    }

    public class ToCoinsToDeduct implements Func3<Void, User, Long, Observable<Long>> {
        @Override
        public Observable<Long> call(Void ignored, User user, Long incrementalBid) {
            Timber.d("coins to deduct: " + user.getCoins() + " , " + incrementalBid);
            if (user.getCoins() >= incrementalBid) {
                return Observable.just(incrementalBid);
            } else {
                return Observable.just(Long.valueOf(0));
            }
        }
    }


    public Func2<Long, CurrentItem, Long> toIncrementalBid() {
        return new Func2<Long, CurrentItem, Long>() {
            @Override
            public Long call(Long myBid, CurrentItem currentItem) {
                Timber.d("calculate incremental bid");
                final int highBid = currentItem.getHighBid();
                if (highBid <= 0) {
                    return Long.valueOf(1);
                } else if (myBid < highBid) {
                    return Long.valueOf(highBid - myBid + 1);
                }
                return Long.valueOf(1);
            }
        };
    }

}
