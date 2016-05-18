package com.djgilk.auctions.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.djgilk.auctions.MainApplication;
import com.djgilk.auctions.presenter.ViewPresenter;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by dangilk on 2/27/16.
 */
public class RxAndroid {

    public static Observable<Bitmap> observeBitmap(final String imageUrl) {
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                try {
                    URL urlConnection = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) urlConnection
                            .openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(input));
                    subscriber.onNext(bitmap);
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static class ToLoadedImageView implements Func1<Bitmap, Observable<Boolean>> {
        final ImageView imageView;

        public ToLoadedImageView(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        public Observable<Boolean> call(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
            return Observable.just(true);
        }
    }

    public static class ToUpdatedTextView implements Func1<Object, Observable<Boolean>> {
        final TextView textView;
        final String text;

        public ToUpdatedTextView(TextView textView, String text) {
            this.textView = textView;
            this.text = text;
        }

        @Override
        public Observable<Boolean> call(Object o) {
            textView.setText(String.valueOf(text));
            return Observable.just(true);
        }
    }

    public static class ToLayoutFade implements Func1<Object, Observable<Object>> {
        private final MainApplication application;
        private final ViewPresenter presenter1;
        private final ViewPresenter presenter2;
        private final boolean addToBackStack;

        public ToLayoutFade(MainApplication application, ViewPresenter presenter1, ViewPresenter presenter2, boolean addToBackStack) {
            this.application = application;
            this.presenter1 = presenter1;
            this.presenter2 = presenter2;
            this.addToBackStack = addToBackStack;
        }

        public Observable<Object> observeFadeLayout() {
            return Observable.create(new Observable.OnSubscribe<Object>() {
                @Override
                public void call(final Subscriber<? super Object> subscriber) {
                    final Animation fadeOut = AnimationUtils.loadAnimation(application, android.R.anim.fade_out);
                    final Animation fadeIn = AnimationUtils.loadAnimation(application, android.R.anim.fade_in);
                    final View layout1 = presenter1.getLayout();
                    final View layout2 = presenter2.getLayout();

                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            layout1.setVisibility(View.GONE);
                            subscriber.onNext(presenter2);
                        }
                    });
                    layout1.startAnimation(fadeOut);
                    layout2.setVisibility(View.VISIBLE);
                    layout2.startAnimation(fadeIn);
                    if (addToBackStack) {
                        application.addToBackStack(presenter1);
                    }
                    application.setCurrentPresenter(presenter2);
                }
            });
        }

        @Override
        public Observable<Object> call(Object bool) {
            return observeFadeLayout();
        }
    }
}
