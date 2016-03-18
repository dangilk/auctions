package com.djgilk.auctions.model;

import android.widget.TextView;

/**
 * Created by dangilk on 3/17/16.
 */
public class PrettyTime {
    final Long targetTimeMillis;
    final Long currentTimeMillis;

    final static String DAY = "Day";
    final static String HOUR = "Hour";
    final static String MINUTE = "Minute";
    final static String SECOND = "Second";
    final static String S = "s";

    public PrettyTime(Long targetTimeMillis, Long currentTimeMillis) {
        this.targetTimeMillis = targetTimeMillis;
        this.currentTimeMillis = currentTimeMillis;
    }

    public void setText(TextView valueView, TextView unitView) {
        Long intervalMillis = targetTimeMillis - currentTimeMillis;
        Long intervalSeconds = intervalMillis / 1000;
        Long intervalMinutes = intervalSeconds / 60;
        Long intervalHours = intervalMinutes / 60;
        Long intervalDays = intervalHours / 24;

        if (intervalSeconds < 0) {
            setText(valueView, unitView, Long.valueOf(0), SECOND);
        } else if (intervalSeconds < 60) {
            setText(valueView, unitView, intervalSeconds, SECOND);
        } else if (intervalMinutes < 90) {
            setText(valueView, unitView, intervalMinutes, MINUTE);
        } else if (intervalHours < 36) {
            setText(valueView, unitView, intervalHours, HOUR);
        } else {
            setText(valueView, unitView, intervalDays, DAY);
        }
    }

    private void setText(TextView valueView, TextView unitView, Long value, String unit) {
        valueView.setText(String.valueOf(value));
        unitView.setText(appendS(unit, value));
    }

    private String appendS(String input, Long count) {
        if (count == 1) {
            return String.valueOf(input);
        }
        return String.valueOf(input + S);
    }
}
