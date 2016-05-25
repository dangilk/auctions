package com.djgilk.auctions.presenter;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.djgilk.auctions.MainActivity;
import com.djgilk.auctions.MainApplication;
import com.djgilk.auctions.R;
import com.djgilk.auctions.billing.util.IabHelper;
import com.djgilk.auctions.billing.util.IabResult;
import com.djgilk.auctions.billing.util.Purchase;
import com.djgilk.auctions.billing.util.RxBilling;
import com.djgilk.auctions.firebase.RxFirebase;
import com.djgilk.auctions.helper.RxAndroid;
import com.djgilk.auctions.helper.RxHelper;
import com.djgilk.auctions.helper.RxPublisher;
import com.djgilk.auctions.model.AuctionState;
import com.djgilk.auctions.model.Bid;
import com.djgilk.auctions.model.ClientConfig;
import com.djgilk.auctions.model.CurrentItem;
import com.djgilk.auctions.model.PrettyTime;
import com.djgilk.auctions.model.User;
import com.jakewharton.rxbinding.view.RxView;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import rx.Observable;
import rx.Observer;
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
    public final static String BILLING_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtE0g6SBWF0jB8k5BgAJPkPoAK/Jv06kHxdaN4VJEVmo5BovzedGZzhJ9A/rmexQ0ggBT7wHvpz1cY9JgLfPDOFIP4NZpwwuuoISWNV7X3vIS+ecSR97LqcALfuMJg197hUcJtqvX1N+OUN9v//oTTctb1aGZbW/36Y6d6PTa6Xh6jZppIza+EOT/1WNIwsYSHzyN+4BgNINAqJPkjAlSAgvHchNrgKHfBjax3KVBYph59iMQ4gJoGHBYXNcP6mbdtjLHeBl03ZyQLbf/AfRYF6CYl0kvAaf4ULTKOhVYkDN67+4xpOu3HJ6cGNIKBIk5wKkd/D+Yf25Do7PI6WhRPwIDAQAB";
    IabHelper iabHelper;
    final CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Inject
    RxFirebase rxFirebase;

    @Inject
    MainApplication mainApplication;

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

    @Bind(R.id.ll_winning)
    LinearLayout winningNotification;

    @Bind(R.id.iv_settings)
    ImageView ivSettings;

    @Inject
    public AuctionPresenter(){};

    public void onCreate(MainActivity activity) {
        super.onCreate(activity);
        Timber.d("auctionPresenter.onCreate()");
        iabHelper = new IabHelper(activity, BILLING_KEY);
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Timber.d("Problem setting up In-app Billing: " + result);
                } else {
                    Timber.d("iab helper is set up!");
                }
                // Hooray, IAB is fully set up!
            }
        });

        compositeSubscription.add(rxPublisher.getCurrentItemObservable().doOnNext(new RxHelper.Log<CurrentItem>()).observeOn(Schedulers.io())
                .flatMap(loadedCurrentItem()).subscribe(new RxHelper.EmptyObserver<Boolean>()));
        compositeSubscription.add(rxPublisher.getClientConfigObservable().subscribe(new RxHelper.EmptyObserver<ClientConfig>()));
        compositeSubscription.add(rxPublisher.getUserObservable().doOnNext(new RxHelper.Log<User>())
                .flatMap(updateUser()).subscribe(new RxHelper.EmptyObserver<User>()));
        compositeSubscription.add(
                Observable.combineLatest(rxPublisher.getClockOffsetObservable(),
                        Observable.interval(1, TimeUnit.SECONDS),
                        rxPublisher.getCurrentItemObservable(),
                        offsetClock())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(updateClock()));
        compositeSubscription.add(rxPublisher.getAggregateBidObservable().subscribe(updateMyBids()));
        // calculate incremental bid
        final ConnectableObservable<Long> incrementalBidObservable =
                Observable.combineLatest(rxPublisher.getAggregateBidObservable(),
                    rxPublisher.getCurrentItemObservable(),
                    toIncrementalBid()).publish();

        // observe bid click
        final ConnectableObservable<Long> deductCoinsObservable = Observable.concat(
                RxHelper.withLatestFrom(RxView.clicks(bidButton).throttleLast(1, TimeUnit.SECONDS), rxPublisher.getUserObservable(),
                        incrementalBidObservable, toCoinsToDeduct())).publish();

        // observe bid processing
        compositeSubscription.add(Observable.zip(
                Observable.concat(RxHelper.withLatestFrom(deductCoinsObservable.filter(zeroFilter()),
                        rxPublisher.getUserObservable(), rxPublisher.getAuctionStateObservable(), insertUserBid())),
                Observable.concat(deductCoinsObservable.filter(zeroFilter()).withLatestFrom(rxPublisher.getUserObservable(), deductUserCoins())),
                Observable.concat(deductCoinsObservable.filter(zeroFilter()).withLatestFrom(rxPublisher.getAuctionStateObservable(), updateHighBid())),
                new RxHelper.ZipWaiter3()).subscribe(bidObserver()));

        // update the incremental bid ui
        compositeSubscription.add(incrementalBidObservable.subscribe(updateIncrementalBid()));

        // update the user's 'winning' status
        compositeSubscription.add(Observable.combineLatest(rxPublisher.getCurrentItemObservable(),
                rxPublisher.getAggregateBidObservable(), updateWinningStatus()).subscribe(new RxHelper.EmptyObserver<Void>()));

        // TODO throw monetization dialog
        compositeSubscription.add(deductCoinsObservable.filter(new PositiveNumFilter())
                .flatMap(observePurchase(iabHelper, activity)).subscribe());

        //click bid button logic:
        //if has enough coins:
            //deduct coins, do fanfare, update button state to "youre in the lead!"
        // if not enough coins:
            //throw dialog: "not enough coins, buy coins?"

        // go to profile page
        compositeSubscription.add(RxView.clicks(ivSettings).throttleFirst(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(activity.fadeFromAuctionToProfilePresenter())
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
                }));

        compositeSubscription.add(incrementalBidObservable.connect());
        compositeSubscription.add(deductCoinsObservable.connect());
        Timber.d("auctionPresenter.onCreate() complete");
    }

    @Override
    public void onDestroy() {
        compositeSubscription.unsubscribe();
        if (iabHelper != null) {
            try {
                iabHelper.dispose();
            } catch (Exception e) {

            }
        }
        iabHelper = null;
    }

    @Override
    public View getLayout() {
        return auctionLayout;
    }

    @Override
    public String getPresenterTag() {
        return AUCTION_PRESENTER_TAG;
    }

    public Func1<Long, Observable<Purchase>> observePurchase(final IabHelper helper, final MainActivity activity) {
        return new Func1<Long, Observable<Purchase>>() {
            @Override
            public Observable<Purchase> call(Long aLong) {
                return RxBilling.observePurchase(helper, activity);
            }
        };
    }

    public Func1<User, Observable<User>> updateUser() {
        return new Func1<User, Observable<User>>() {
            @Override
            public Observable<User> call(User user) {
                tvUserCoins.setText(String.valueOf(user.getCoins()));
                return Observable.just(user);
            }
        };
    }

    public Func1<Long, Boolean> zeroFilter() {
        return new Func1<Long, Boolean>() {
            @Override
            public Boolean call(Long value) {
                return value > 0;
            }
        };
    }


    public static class PositiveNumFilter implements Func1<Long, Boolean> {
        @Override
        public Boolean call(Long value) {
            return value <= 0;
        }
    }

    public Func2<CurrentItem, Long, Void> updateWinningStatus() {
        return new Func2<CurrentItem, Long, Void>() {
            @Override
            public Void call(CurrentItem currentItem, Long aggregateBid) {
                if (aggregateBid >= currentItem.getHighBid()) {
                    // currently winning
                    bidButton.setVisibility(View.GONE);
                    winningNotification.setVisibility(View.VISIBLE);
                } else {
                    // currently losing
                    bidButton.setVisibility(View.VISIBLE);
                    winningNotification.setVisibility(View.GONE);
                }
                return null;
            }
        };
    }

    public Func1<CurrentItem, Observable<Boolean>> loadedCurrentItem() {
        return new Func1<CurrentItem, Observable<Boolean>>() {
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
        };
    }

    public Func3<Long, Long, CurrentItem, PrettyTime> offsetClock() {
        return new Func3<Long, Long, CurrentItem, PrettyTime>() {
            @Override
            public PrettyTime call(Long clockOffset, Long interval, CurrentItem currentItem) {
                return new PrettyTime(currentItem.getAuctionEndTimeMillis(), System.currentTimeMillis() + clockOffset);
            }
        };
    }

    public Action1<PrettyTime> updateClock() {
        return new Action1<PrettyTime>() {
            @Override
            public void call(PrettyTime prettyTime) {
                prettyTime.setText(tvAuctionTimeLeft, tvAuctionTimeLeftUnits);
            }
        };
    }

    public Action1<Long> updateMyBids() {
        return new Action1<Long>() {
            @Override
            public void call(Long myBid) {
                tvYourBid.setText(String.valueOf(myBid));
            }
        };
    }

    public Action1<Long> updateIncrementalBid() {
        return new Action1<Long>() {
            @Override
            public void call(Long bid) {
                tvBidIncrement.setText(String.valueOf(bid));
            }
        };
    }

    public Observer<Boolean> bidObserver() {
        return new Observer<Boolean>() {
            @Override
            public void onCompleted() {
                Timber.d("bid onComplete()");
            }

            @Override
            public void onError(Throwable e) {
                // TODO manage errors
                Timber.e("error bidding: " + e.getMessage());
            }

            @Override
            public void onNext(Boolean success) {
                Timber.d("bid onNext()");
                if (success) {

                }
            }
        };
    }

    public Func2<Long, AuctionState, Observable<Boolean>> updateHighBid() {
        return new Func2<Long, AuctionState, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call(Long aLong, AuctionState auctionState) {
                Timber.d("update high bid");
                return rxFirebase.observableFirebaseObjectIncrementTransaction(
                        CurrentItem.getParentRootPath() + "/" + auctionState.getAuctionItemId() + "/" + CurrentItem.getHighBidKey());
            }
        };
    }

    public Func2<Long, User, Observable<User>> deductUserCoins() {
        return new Func2<Long, User, Observable<User>>() {
            @Override
            public Observable<User> call(Long bidAmount, User user) {
                Timber.d("deduct user coins");
                user.deductCoins(bidAmount.intValue());
                return rxFirebase.observableFirebaseObjectUpdate(user, User.getParentRootPath() + "/" + user.getFacebookId(), false);
            }
        };
    }

    public Func3<Long, User, AuctionState, Observable<Bid>> insertUserBid() {
        return new Func3<Long, User, AuctionState, Observable<Bid>>() {
            @Override
            public Observable<Bid> call(Long bidAmount, User user, AuctionState auctionState) {
                Timber.d("insert user bid");
                Bid bid = new Bid(bidAmount);
                return rxFirebase.observableFirebaseObjectUpdate(bid,
                        Bid.getParentRootPath() + "/" + auctionState.getAuctionItemId() + "/" + user.getFacebookId(), true);
            }
        };
    }

    public Func3<Void, User, Long, Observable<Long>> toCoinsToDeduct() {
        return new Func3<Void, User, Long, Observable<Long>>() {
            @Override
            public Observable<Long> call(Void aVoid, User user, Long incrementalBid) {
                if (user.getCoins() >= incrementalBid) {
                    return Observable.just(incrementalBid);
                } else {
                    return Observable.just(Long.valueOf(0));
                }
            }
        };
    }

    public Func2<Long, CurrentItem, Long> toIncrementalBid() {
        return new Func2<Long, CurrentItem, Long>() {
            @Override
            public Long call(Long myBid, CurrentItem currentItem) {
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
