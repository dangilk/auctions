package com.djgilk.auctions.presenter;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.djgilk.auctions.BuildConfig;
import com.djgilk.auctions.MainActivity;
import com.djgilk.auctions.MainApplication;
import com.djgilk.auctions.R;
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
import com.pavlospt.androidiap.billing.BillingProcessor;
import com.pavlospt.androidiap.models.ConsumeModel;
import com.pavlospt.androidiap.models.PurchaseDataModel;
import com.pavlospt.androidiap.models.PurchaseModel;
import com.pavlospt.androidiap.utils.Constants;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
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
public class AuctionPresenter extends ViewPresenter implements BillingProcessor.BillingProcessorListener {
    public final static String AUCTION_PRESENTER_TAG = "auctionPresenter";
    String CONSUMABLE_PRODUCT_ID = "coins50";
    String BILLING_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtE0g6SBWF0jB8k5BgAJPkPoAK/Jv06kHxdaN4VJEVmo5BovzedGZzhJ9A/rmexQ0ggBT7wHvpz1cY9JgLfPDOFIP4NZpwwuuoISWNV7X3vIS+ecSR97LqcALfuMJg197hUcJtqvX1N+OUN9v//oTTctb1aGZbW/36Y6d6PTa6Xh6jZppIza+EOT/1WNIwsYSHzyN+4BgNINAqJPkjAlSAgvHchNrgKHfBjax3KVBYph59iMQ4gJoGHBYXNcP6mbdtjLHeBl03ZyQLbf/AfRYF6CYl0kvAaf4ULTKOhVYkDN67+4xpOu3HJ6cGNIKBIk5wKkd/D+Yf25Do7PI6WhRPwIDAQAB";
    BillingProcessor billingProcessor;
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Inject
    RxFirebase rxFirebase;

    @Inject
    MainApplication mainApplication;

    @Inject
    RxPublisher rxPublisher;

    @Bind(R.id.ll_auction)
    FrameLayout auctionLayout;

    @Bind(R.id.ll_auctionMain)
    LinearLayout mainAuctionLayout;

    @Bind(R.id.ll_winConfirmation)
    LinearLayout winConfirmationLayout;

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

    @Bind(R.id.bt_confirmProfile)
    Button confirmProfileButton;

    @Bind(R.id.iv_wonAuctionImage)
    ImageView wonAuctionImage;

    @Bind(R.id.tv_wonAuctionTitle)
    TextView wonAuctionTitle;

    @Inject
    public AuctionPresenter(){};

    public void onCreate(final MainActivity activity) {
        super.onCreate(activity);
        Timber.d("auctionPresenter.onCreate()");

        if(BuildConfig.DEBUG) {
            CONSUMABLE_PRODUCT_ID = "android.test.purchased";
            BILLING_KEY = "";
        }

        this.billingProcessor = new BillingProcessor(mainApplication, BILLING_KEY, this);

        compositeSubscription.add(rxPublisher.getCurrentItemObservable().observeOn(Schedulers.io())
                .flatMap(loadedCurrentItem()).subscribe(new RxHelper.EmptyObserver<Boolean>()));
        compositeSubscription.add(rxPublisher.getClientConfigObservable().subscribe(new RxHelper.EmptyObserver<ClientConfig>()));
        Timber.e("subscribe to user updates");
        final Observable<User> userObservable = rxPublisher.getUserObservable().doOnCompleted(new Action0() {
            @Override
            public void call() {
                Timber.d("userObservable onComplete()");
            }
        }).doOnNext(new Action1<User>() {
            @Override
            public void call(User user) {
                Timber.d("userObservable onNext()");
            }
        });
        compositeSubscription.add(userObservable.flatMap(updateUser()).subscribe(new RxHelper.EmptyObserver<User>()));
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
                    toIncrementalBid()).replay(1);

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

        // throw monetization dialog
        compositeSubscription.add(Observable.concat(deductCoinsObservable.filter(new PositiveNumFilter())
                .flatMap(observePurchase(activity))
                .flatMap(observeConsume())
                .withLatestFrom(rxPublisher.getUserObservable(), observeUserConsume()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxHelper.EmptyObserver<User>()));

        // go to profile page
        compositeSubscription.add(RxView.clicks(ivSettings).throttleFirst(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(activity.fadePresenters(AUCTION_PRESENTER_TAG, ProfilePresenter.PROFILE_PRESENTER_TAG, true))
                .subscribe());

        // go to profile page
        compositeSubscription.add(RxView.clicks(confirmProfileButton).throttleFirst(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(activity.fadePresenters(AUCTION_PRESENTER_TAG, ProfilePresenter.PROFILE_PRESENTER_TAG, true))
                .subscribe());

        // observe win confirmation item
        compositeSubscription.add(rxPublisher.getItemWinConfirmationObservable()
                .flatMap(loadedWonItem())
                .subscribe(new RxHelper.EmptyObserver<Boolean>()));

        compositeSubscription.add(incrementalBidObservable.connect());
        compositeSubscription.add(deductCoinsObservable.connect());

        Timber.d("auctionPresenter.onCreate() complete");
    }

    @Override
    public void onDestroy() {
        Timber.e("unsubscribing");
        compositeSubscription.unsubscribe();
        compositeSubscription = null;
        if (billingProcessor != null) {
            billingProcessor.release();
        }
        billingProcessor = null;
    }

    @Override
    public View getLayout() {
        return auctionLayout;
    }

    @Override
    public String getPresenterTag() {
        return AUCTION_PRESENTER_TAG;
    }

    public Func1<User, Observable<User>> updateUser() {
        return new Func1<User, Observable<User>>() {
            @Override
            public Observable<User> call(User user) {
                Timber.e("update user ui");
                tvUserCoins.setText(String.valueOf(user.getCoins()));
                final String winConfirmation = user.getWinConfirmation();
                if (winConfirmation == null || winConfirmation.isEmpty()) {
                    mainAuctionLayout.setVisibility(View.VISIBLE);
                    winConfirmationLayout.setVisibility(View.GONE);
                } else {
                    winConfirmationLayout.setVisibility(View.VISIBLE);
                    mainAuctionLayout.setVisibility(View.GONE);
                }
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

    public Func1<CurrentItem, Observable<Boolean>> loadedWonItem() {
        return new Func1<CurrentItem, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call(CurrentItem currentItem) {
                return Observable.zip(
                        RxAndroid.observeBitmap(currentItem.getImageUrl()).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread()).flatMap(new RxAndroid.ToLoadedImageView(wonAuctionImage)),
                        Observable.just(currentItem).observeOn(AndroidSchedulers.mainThread())
                                .flatMap(new RxAndroid.ToUpdatedTextView(wonAuctionTitle, currentItem.getName())),
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

    public Func2<ConsumeModel, User, Observable<User>> observeUserConsume() {
        Timber.e("observeUserConsume()");
        return new Func2<ConsumeModel, User, Observable<User>>() {
            @Override
            public Observable<User> call(ConsumeModel consumeModel, User user) {
                Timber.e("consume purchase");
                if (consumeModel != null && consumeModel.getErrorCode() == 0) {
                    user.incCoins(50);
                    return rxFirebase.observableFirebaseObjectUpdate(user, User.getPath(user), false);
                }
                return Observable.just(null);
            }
        };
    }

    boolean canConsumePurchase(PurchaseModel purchaseModel) {
        return purchaseModel != null && purchaseModel.getPurchaseDataModel() != null &&
                (purchaseModel.isSuccess() || purchaseModel.getErrorCode() == Constants.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED);
    }

    Func1<PurchaseModel, Observable<ConsumeModel>> observeConsume() {
        return new Func1<PurchaseModel, Observable<ConsumeModel>>() {
            @Override
            public Observable<ConsumeModel> call(PurchaseModel purchaseModel) {
                if (canConsumePurchase(purchaseModel)) {
                    final String productId = purchaseModel.getPurchaseDataModel().getProductId();
                    Timber.d("consume product:" + productId);
                    return billingProcessor.consumePurchaseObservable(productId);
                } else {
                    Timber.e("Product purchase error code:" + purchaseModel.getErrorCode());
                    return Observable.just(null);
                }
            }
        };
    }

    Func1<Long, Observable<PurchaseModel>> observePurchase(final MainActivity activity) {
        return new Func1<Long, Observable<PurchaseModel>>() {
            @Override
            public Observable<PurchaseModel> call(Long aLong) {
                return billingProcessor.purchaseObservable(activity, CONSUMABLE_PRODUCT_ID);
            }
        };
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return billingProcessor.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onProductPurchased(String s, PurchaseDataModel purchaseDataModel) {
        Timber.e("onProductPurchased");
        compositeSubscription.add(Observable.concat(
                billingProcessor.consumePurchaseObservable(CONSUMABLE_PRODUCT_ID)
                .subscribeOn(Schedulers.io())
                .withLatestFrom(rxPublisher.getUserObservable(), observeUserConsume()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxHelper.EmptyObserver<User>()));
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int i, Throwable throwable) {

    }
}
