/*
 * This is the source code of AyuGram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @Radolyn, 2023
 */

package com.radolyn.ayugram.sync;

import com.radolyn.ayugram.AyuConfig;

public class AyuSyncConfig {
    private static String getWebSocketProtocol() {
        return "ws://";
    }

    private static String getHTTPProtocol() {
        return "http://";
    }

    public static String getWebSocketURL() {
        return "ws://127.0.0.1:1/disabled";
    }

    public static String getUserDataURL() {
        return "http://127.0.0.1:1/disabled";
    }

    public static String getRegisterDeviceURL() {
        return "http://127.0.0.1:1/disabled";
    }

    public static String getForceSyncURL() {
        return "http://127.0.0.1:1/disabled";
    }

    public static String getToken() {
        return "";
    }

    public static String getProfileURL() {
        return "http://127.0.0.1:1/disabled";
    }
}
