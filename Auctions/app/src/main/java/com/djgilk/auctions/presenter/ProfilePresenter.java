package com.djgilk.auctions.presenter;

import android.app.Activity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.djgilk.auctions.MainApplication;
import com.djgilk.auctions.R;
import com.djgilk.auctions.firebase.RxFirebase;
import com.djgilk.auctions.helper.RxPublisher;
import com.djgilk.auctions.helper.StringUtils;
import com.djgilk.auctions.helper.ViewUtils;
import com.djgilk.auctions.model.User;
import com.djgilk.auctions.view.PresenterHolder;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func7;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by dangilk on 5/8/16.
 */
public class ProfilePresenter extends ViewPresenter {
    public final static String PROFILE_PRESENTER_TAG = "profilePresenter";
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Inject
    RxPublisher rxPublisher;

    @Inject
    RxFirebase rxFirebase;

    @Inject
    MainApplication mainApplication;

    @Inject
    public ProfilePresenter(){};

    @Bind(R.id.ll_profile)
    LinearLayout profileLayout;

    @Bind(R.id.etName)
    EditText etName;

    @Bind(R.id.etEmail)
    EditText etEmail;

    @Bind(R.id.etAddress1)
    EditText etAddress1;

    @Bind(R.id.etAddress2)
    EditText etAddress2;

    @Bind(R.id.etCity)
    EditText etCity;

    @Bind(R.id.etState)
    EditText etState;

    @Bind(R.id.etPostal)
    EditText etPostal;

    @Bind(R.id.etCountry)
    EditText etCountry;

    @Bind(R.id.bt_profile)
    Button btProfile;

    @Override
    public void onCreate(Activity activity) {
        super.onCreate(activity);
        Timber.d("profilePresenter.onCreate()");

        Observable<Boolean> nameValidObservable = validateStringTextObservable(etName);
        Observable<Boolean> emailValidObservable = RxTextView.textChanges(etEmail).flatMap(new ValidateEmail())
                .doOnNext(new EditTextErrorAction(etEmail)).distinctUntilChanged();
        Observable<Boolean> address1ValidObservable = validateStringTextObservable(etAddress1);
        Observable<Boolean> cityValidObservable = validateStringTextObservable(etCity);
        Observable<Boolean> stateValidObservable = validateStringTextObservable(etState);
        Observable<Boolean> postalValidObservable = validateStringTextObservable(etPostal);
        Observable<Boolean> countryValidObservable = validateStringTextObservable(etCountry);

        compositeSubscription.add(Observable.concat(Observable.combineLatest(nameValidObservable, emailValidObservable, address1ValidObservable, cityValidObservable,
                stateValidObservable, postalValidObservable, countryValidObservable, new CheckValidProfile()))
                .subscribe(validProfileObserver()));

        compositeSubscription.add(Observable.concat(RxView.clicks(btProfile).throttleFirst(3, TimeUnit.SECONDS)
                .withLatestFrom(rxPublisher.getUserObservable(), updateUserInfo()))
                .subscribe(updateObserver((PresenterHolder)activity)));

        compositeSubscription.add(rxPublisher.getUserObservable().subscribe(presetValues()));
        Timber.d("profilePresenter.onCreate() complete");
    }

    @Override
    public void onDestroy() {
        compositeSubscription.unsubscribe();
        compositeSubscription = null;
    }

    @Override
    public View getLayout() {
        Timber.d("get profile layout: " + profileLayout);
        return profileLayout;
    }

    @Override
    public String getPresenterTag() {
        return PROFILE_PRESENTER_TAG;
    }

    static class ValidateEmail implements Func1<CharSequence, Observable<Boolean>> {
        @Override
        public Observable<Boolean> call(CharSequence email) {
            Timber.d("call validate email: " + email);
            return Observable.just(Patterns.EMAIL_ADDRESS.matcher(email).matches());
        }
    }

    static class ValidateStringField implements Func1<CharSequence, Observable<Boolean>> {
        @Override
        public Observable<Boolean> call(CharSequence charSequence) {
            return Observable.just(charSequence != null && charSequence.toString().trim().length() > 0);
        }
    }

    static class ValidateStringTransformer implements Observable.Transformer<CharSequence, Boolean> {
        @Override
        public Observable<Boolean> call(Observable<CharSequence> charSequenceObservable) {
            return charSequenceObservable.flatMap(new ValidateStringField());//.throttleLast(1, TimeUnit.SECONDS);//.distinctUntilChanged();
        }
    }

    Observable<Boolean> validateStringTextObservable(final EditText editText) {
        return RxTextView.textChanges(editText).compose(new ValidateStringTransformer())
                .doOnNext(new EditTextErrorAction(editText));
    }

    Observer<User> updateObserver(final PresenterHolder holder) {
        return new Observer<User>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(User user) {
                ViewUtils.goBack(mainApplication, holder);
            }
        };
    }

    Observer<User> presetValues() {
        return new Observer<User>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(User user) {
                etName.setText(user.getDisplayName());
                etAddress1.setText(user.getAddress1());
                etAddress2.setText(user.getAddress2());
                etCity.setText(user.getCity());
                etPostal.setText(user.getZip());
                etState.setText(user.getState());
                etCountry.setText(user.getCountry());
                etEmail.setText(user.getEmail());
                if (user.getWinConfirmation().isEmpty()) {
                    btProfile.setText("save and return");
                } else {
                    btProfile.setText("ship my item!");
                }
            }
        };
    }

    static class EditTextErrorAction implements Action1<Boolean> {
        final EditText editText;
        EditTextErrorAction(EditText editText) {
            this.editText = editText;
        }
        @Override
        public void call(Boolean isValid) {
            Timber.d("is text valid? " + isValid);
            if (isValid) {
                editText.setError(null);
            } else {
                editText.setError("invalid entry");
            }
        }
    }

    static class CheckValidProfile implements Func7<Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Observable<Boolean>> {
        @Override
        public Observable<Boolean> call(Boolean b1, Boolean b2, Boolean b3, Boolean b4, Boolean b5, Boolean b6, Boolean b7) {
            return  Observable.just(b1&&b2&&b3&&b4&&b5&&b6&&b7);
        }
    }

    Observer<Boolean> validProfileObserver() {
        return new Observer<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Boolean valid) {
                btProfile.setEnabled(valid);
            }
        };
    }


    Func2<Void, User, Observable<User>> updateUserInfo() {
        return new Func2<Void, User, Observable<User>>() {
            @Override
            public Observable<User> call(Void click, User user) {
                Timber.d("update user info");
                user.setDisplayName(StringUtils.getString(etName));
                user.setAddress1(StringUtils.getString(etAddress1));
                user.setAddress2(StringUtils.getString(etAddress2));
                user.setCity(StringUtils.getString(etCity));
                user.setZip(StringUtils.getString(etPostal));
                user.setState(StringUtils.getString(etState));
                user.setCountry(StringUtils.getString(etCountry));
                user.setEmail(StringUtils.getString(etEmail));
                user.clearWinConfirmation();
                return rxFirebase.observableFirebaseObjectUpdate(user, User.getPath(user), false);
            }
        };
    }
}
