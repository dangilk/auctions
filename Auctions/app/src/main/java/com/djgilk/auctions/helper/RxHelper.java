package com.djgilk.auctions.helper;

import com.djgilk.auctions.helper.operator.OperatorWithLatestFrom2;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;
import timber.log.Timber;

/**
 * Created by dangilk on 2/28/16.
 */
public class RxHelper {

    public static class ZipWaiter implements Func2<Object, Object, Boolean> {
        @Override
        public Boolean call(Object o, Object o2) {
            return true;
        }
    }

    public static class ZipWaiter3 implements Func3<Object,Object,Object,Boolean> {
        @Override
        public Boolean call(Object o, Object o2, Object o3) {
            return true;
        }
    }

    public static class ToBoolean<T extends Object> implements Func1<T, Observable<Boolean>> {
        @Override
        public Observable<Boolean> call(T o) {
            return Observable.just(true);
        }
    }

    public static class Log<T extends Object> implements Action1<T> {
        @Override
        public void call(T t) {
            if (t == null) {
                Timber.d("logging null object");
            } else {
                Timber.d(t.toString());
            }
        }
    }

    /**
     * Dan's implementation of a 3-ary withLatestFrom. default only supports 2
     */
    public static final <T, S, U, R> Observable<R> withLatestFrom(Observable<? extends T> main, Observable<? extends S> other1, Observable<? extends U> other2, Func3<? super T, ? super S, ? super U, ? extends R> resultSelector) {
        return main.lift(new OperatorWithLatestFrom2<T, S, U, R>(other1, other2, resultSelector));
    }

}
