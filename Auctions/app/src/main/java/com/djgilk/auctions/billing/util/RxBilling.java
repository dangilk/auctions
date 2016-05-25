package com.djgilk.auctions.billing.util;

import com.djgilk.auctions.MainActivity;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by dangilk on 5/24/16.
 */
public class RxBilling {

    public static Observable<Purchase> observePurchase(final IabHelper helper, final MainActivity activity) {
        return Observable.create(new Observable.OnSubscribe<Purchase>() {
            @Override
            public void call(final Subscriber<? super Purchase> subscriber) {
                IabHelper.OnIabPurchaseFinishedListener listener = new IabHelper.OnIabPurchaseFinishedListener() {
                    @Override
                    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                        if (result.isFailure()) {
                           subscriber.onError(new RuntimeException(result.getMessage()));
                        } else {
                            subscriber.onNext(purchase);
                            //subscriber.onCompleted();
                        }
                    }
                };

                try {
                    helper.launchPurchaseFlow(activity, "coins50", 10001,
                            listener, "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
                } catch (Exception e) {
                    subscriber.onError(new RuntimeException(e.getMessage()));
                }
            }

            /*

        @Override
        public void call(Subscriber<? super String> subscriber) {
            ValueUpdateListener listener = new ValueUpdateListener() {

                @Override
                public void onValueChanged(@NonNull String value) {
                    if (subscriber.isUnsubscribed()) {
                        registerListener.unregisterListener(this);
                    } else {
                        subscriber.onNext(value);
                    }
                }
            };

            registerListener.registerListener(listener);
        }
             */
        });
    }
}
