/*

 This is the source code of exteraGram for Android.

 We do not and cannot prevent the use of our code,
 but be respectful and credit the original author.

 Copyright @immat0x1, 2023

*/

package com.exteragram.messenger.utils;

import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONTokener;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class TranslatorUtils {

    public static final DispatchQueue translateQueue = new DispatchQueue("translateQueue", false);

    public static final String[] deviceModels = {
            "Galaxy S6", "Galaxy S7", "Galaxy S8", "Galaxy S9", "Galaxy S10", "Galaxy S21",
            "Pixel 3", "Pixel 4", "Pixel 5",
            "OnePlus 6", "OnePlus 7", "OnePlus 8", "OnePlus 9", "Xperia XZ", "Xperia XZ2", "Xperia XZ3", "Xperia 1", "Xperia 5", "Xperia 10", "Xperia L4"
    };
    private static final String[] chromeVersions = {
            "111.0.5563.57", "94.0.4606.81", "80.0.3987.119", "69.0.3497.100", "92.0.4515.159", "71.0.3578.99"
    };

    public static String formatUserAgent() {
        String androidVersion = String.valueOf(Utilities.random.nextInt(7) + 6);
        String deviceModel = deviceModels[Utilities.random.nextInt(deviceModels.length)];
        String chromeVersion = chromeVersions[Utilities.random.nextInt(chromeVersions.length)];
        return String.format("Mozilla/5.0 (Linux; Android %s; %s) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/%s Mobile Safari/537.36", androidVersion, deviceModel, chromeVersion);
    }

    public interface OnTranslationSuccess {
        void run(CharSequence translated);
    }

    public interface OnTranslationFail {
        void run();
    }

    // todo
    public static void translate(MessageObject messageObject) {
    }

    public static void translate(CharSequence text, String toLang, OnTranslationSuccess onSuccess, OnTranslationFail onFail) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (LanguageDetector.hasSupport()) {
            LanguageDetector.detectLanguage(text.toString(), lng -> {
                String fromLang = "auto";
                if (lng != null && !lng.equals("und")) {
                    fromLang = lng;
                }
                translate(text, fromLang, toLang, onSuccess, onFail);
            }, e -> {
                FileLog.e(e);
                translate(text, "auto", toLang, onSuccess, onFail);
            });
        } else {
            translate(text, "auto", toLang, onSuccess, onFail);
        }
    }

    public static void translate(CharSequence text, String fromLang, String toLang, OnTranslationSuccess onSuccess, OnTranslationFail onFail) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        var req = new TLRPC.TL_messages_translateText();
        req.flags |= 2;
        var item = new TLRPC.TL_textWithEntities();
        item.text = text.toString();
        req.text.add(item);
        req.to_lang = toLang;
        ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (response instanceof TLRPC.TL_messages_translateResult && !((TLRPC.TL_messages_translateResult) response).result.isEmpty()) {
                String result = ((TLRPC.TL_messages_translateResult) response).result.get(0).text;
                if (text.length() > 0 && text.charAt(0) == '\n') {
                    result = "\n" + result;
                }
                if (onSuccess != null) {
                    onSuccess.run(result);
                }
            } else if (onFail != null) {
                onFail.run();
            }
        }));
    }
}
