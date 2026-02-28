package com.example.weighttrackingapp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Small wrapper around SharedPreferences to keep session + settings.
 */
public class Session {

    private static final String PREFS = "wt_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_SMS_ASKED = "sms_asked";
    private static final String KEY_SMS_PHONE = "sms_phone";

    public static void setUserId(Context ctx, long userId) {
        prefs(ctx).edit().putLong(KEY_USER_ID, userId).apply();
    }

    public static long getUserId(Context ctx) {
        return prefs(ctx).getLong(KEY_USER_ID, -1);
    }

    public static void clear(Context ctx) {
        prefs(ctx).edit().remove(KEY_USER_ID).apply();
    }

    public static boolean hasAskedSms(Context ctx) {
        return prefs(ctx).getBoolean(KEY_SMS_ASKED, false);
    }

    public static void setAskedSms(Context ctx, boolean asked) {
        prefs(ctx).edit().putBoolean(KEY_SMS_ASKED, asked).apply();
    }

    public static String getSmsPhone(Context ctx) {
        return prefs(ctx).getString(KEY_SMS_PHONE, "5554"); // default emulator "phone"
    }

    public static void setSmsPhone(Context ctx, String phone) {
        prefs(ctx).edit().putString(KEY_SMS_PHONE, phone).apply();
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
