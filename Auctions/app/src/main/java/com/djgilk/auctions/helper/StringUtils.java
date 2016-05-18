package com.djgilk.auctions.helper;

import android.widget.TextView;

/**
 * Created by dangilk on 5/16/16.
 */
public class StringUtils {

    public static String getString(TextView textView) {
        if (textView == null || textView.getText() == null) {
            return "";
        }
        return textView.getText().toString().trim();
    }
}
