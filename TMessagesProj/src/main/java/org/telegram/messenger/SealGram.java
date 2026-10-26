package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

public class SealGram {

    private static volatile SharedPreferences prefsInstance;

    private static SharedPreferences prefs() {
        SharedPreferences p = prefsInstance;
        if (p == null) {
            p = ApplicationLoader.applicationContext.getSharedPreferences("sealgram", Context.MODE_PRIVATE);
            prefsInstance = p;
        }
        return p;
    }

    public static boolean pref(String key, boolean def) {
        return prefs().getBoolean(key, def);
    }

    public static void setPref(String key, boolean value) {
        prefs().edit().putBoolean(key, value).apply();
    }

    public static int intPref(String key, int def) {
        return prefs().getInt(key, def);
    }

    public static void setIntPref(String key, int value) {
        prefs().edit().putInt(key, value).apply();
    }
}
