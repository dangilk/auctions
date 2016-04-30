package com.djgilk.auctions.helper.operator;

/**
 * Created by dangilk on 4/30/16.
 */

import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Func3;
import rx.observers.SerializedSubscriber;

/**
 * Combines values from two sources only when the main source emits.
 * @param <T> the element type of the main observable
 * @param <S> the element type of another observable that is merged into the main
 * @param <U> the element type of another observable that is merged into the main
 * @param <R> the result element type
 */
public final class OperatorWithLatestFrom2<T, S, U, R> implements Observable.Operator<R, T> {
    final Func3<? super T, ? super S, ? super U, ? extends R> resultSelector;
    final Observable<? extends S> other1;
    final Observable<? extends U> other2;
    /** Indicates the other has not yet emitted a value. */
    static final Object EMPTY = new Object();

    public OperatorWithLatestFrom2(Observable<? extends S> other1, Observable<? extends U> other2, Func3<? super T, ? super S, ? super U, ? extends R> resultSelector) {
        this.other1 = other1;
        this.other2 = other2;
        this.resultSelector = resultSelector;
    }
    @Override
    public Subscriber<? super T> call(Subscriber<? super R> child) {
        // onError and onCompleted may happen either from the main or from other.
        final SerializedSubscriber<R> serializedSubscriber = new SerializedSubscriber<R>(child, false);
        child.add(serializedSubscriber);

        final AtomicReference<Object> current1 = new AtomicReference<Object>(EMPTY);
        final AtomicReference<Object> current2 = new AtomicReference<Object>(EMPTY);

        final Subscriber<T> subscriber = new Subscriber<T>(serializedSubscriber, true) {
            @Override
            public void onNext(T t) {
                Object o1 = current1.get();
                Object o2 = current2.get();
                if (o1 != EMPTY && o2 != EMPTY) {
                    try {
                        @SuppressWarnings("unchecked")
                        S s = (S)o1;
                        U u = (U)o2;
                        R result = resultSelector.call(t, s, u);

                        serializedSubscriber.onNext(result);
                    } catch (Throwable e) {
                        Exceptions.throwOrReport(e, this);
                    }
                }
            }
            @Override
            public void onError(Throwable e) {
                serializedSubscriber.onError(e);
                serializedSubscriber.unsubscribe();
            }
            @Override
            public void onCompleted() {
                serializedSubscriber.onCompleted();
                serializedSubscriber.unsubscribe();
            }
        };

        Subscriber<S> otherSubscriber1 = new Subscriber<S>() {
            @Override
            public void onNext(S s) {
                current1.set(s);
            }
            @Override
            public void onError(Throwable e) {
                serializedSubscriber.onError(e);
                serializedSubscriber.unsubscribe();
            }
            @Override
            public void onCompleted() {
                if (current1.get() == EMPTY) {
                    serializedSubscriber.onCompleted();
                    serializedSubscriber.unsubscribe();
                }
            }
        };

        Subscriber<U> otherSubscriber2 = new Subscriber<U>() {
            @Override
            public void onNext(U u) {
                current2.set(u);
            }
            @Override
            public void onError(Throwable e) {
                serializedSubscriber.onError(e);
                serializedSubscriber.unsubscribe();
            }
            @Override
            public void onCompleted() {
                if (current2.get() == EMPTY) {
                    serializedSubscriber.onCompleted();
                    serializedSubscriber.unsubscribe();
                }
            }
        };
        serializedSubscriber.add(subscriber);
        serializedSubscriber.add(otherSubscriber1);
        serializedSubscriber.add(otherSubscriber2);

        other1.unsafeSubscribe(otherSubscriber1);
        other2.unsafeSubscribe(otherSubscriber2);

        return subscriber;
    }
}
