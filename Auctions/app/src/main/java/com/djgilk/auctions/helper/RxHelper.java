package com.djgilk.auctions.helper;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;

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
}
