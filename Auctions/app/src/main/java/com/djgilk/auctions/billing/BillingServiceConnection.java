package com.djgilk.auctions.billing;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.android.vending.billing.IInAppBillingService;

/**
 * Created by dangilk on 5/24/16.
 */
public class BillingServiceConnection implements ServiceConnection {
    public final static String BILLING_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtE0g6SBWF0jB8k5BgAJPkPoAK/Jv06kHxdaN4VJEVmo5BovzedGZzhJ9A/rmexQ0ggBT7wHvpz1cY9JgLfPDOFIP4NZpwwuuoISWNV7X3vIS+ecSR97LqcALfuMJg197hUcJtqvX1N+OUN9v//oTTctb1aGZbW/36Y6d6PTa6Xh6jZppIza+EOT/1WNIwsYSHzyN+4BgNINAqJPkjAlSAgvHchNrgKHfBjax3KVBYph59iMQ4gJoGHBYXNcP6mbdtjLHeBl03ZyQLbf/AfRYF6CYl0kvAaf4ULTKOhVYkDN67+4xpOu3HJ6cGNIKBIk5wKkd/D+Yf25Do7PI6WhRPwIDAQAB";
    final static int BILLING_API_VERSION = 3;
    IInAppBillingService service;

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        //service = IInAppBillingService.Stub.asInterface(service);
    }

    public IInAppBillingService getService() {
        return service;
    }

//    public purchase(String packageName, String sku, MainActivity activity) {
//        Bundle buyIntentBundle = service.getBuyIntent(BILLING_API_VERSION, getPackageName(),
//                sku, "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
//        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
//        activity.startIntentSenderForResult(pendingIntent.getIntentSender(),
//                1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
//                Integer.valueOf(0));
//    }
}
