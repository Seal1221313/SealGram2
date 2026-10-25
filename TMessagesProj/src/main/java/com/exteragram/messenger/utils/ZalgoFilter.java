package com.exteragram.messenger.utils;

import android.text.SpannableStringBuilder;
import android.text.Spanned;

public class ZalgoFilter {
    private static final int MAX_MARKS = 3;

    private static boolean isStackedMark(int cp) {
        if ((cp >= 0xFE00 && cp <= 0xFE0F) || cp == 0x20E3 || (cp >= 0xE0100 && cp <= 0xE01EF)) {
            return false;
        }
        int type = Character.getType(cp);
        return type == Character.NON_SPACING_MARK || type == Character.ENCLOSING_MARK;
    }

    public static CharSequence filterMessage(CharSequence text) {
        if (text == null || text.length() == 0) {
            return text;
        }
        int length = text.length();
        int run = 0;
        boolean needFilter = false;
        for (int i = 0; i < length; ) {
            int cp = Character.codePointAt(text, i);
            if (isStackedMark(cp)) {
                if (++run > MAX_MARKS) {
                    needFilter = true;
                    break;
                }
            } else {
                run = 0;
            }
            i += Character.charCount(cp);
        }
        if (!needFilter) {
            return text;
        }

        if (text instanceof Spanned) {
            var builder = new SpannableStringBuilder(text);
            run = 0;
            for (int i = 0; i < builder.length(); ) {
                int cp = Character.codePointAt(builder, i);
                int count = Character.charCount(cp);
                if (isStackedMark(cp)) {
                    if (++run > MAX_MARKS) {
                        builder.delete(i, i + count);
                        continue;
                    }
                } else {
                    run = 0;
                }
                i += count;
            }
            return builder;
        }

        var result = new StringBuilder(length);
        run = 0;
        for (int i = 0; i < length; ) {
            int cp = Character.codePointAt(text, i);
            if (isStackedMark(cp)) {
                if (++run > MAX_MARKS) {
                    i += Character.charCount(cp);
                    continue;
                }
            } else {
                run = 0;
            }
            result.appendCodePoint(cp);
            i += Character.charCount(cp);
        }
        return result.toString();
    }
}
