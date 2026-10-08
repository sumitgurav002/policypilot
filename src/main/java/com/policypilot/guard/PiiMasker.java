package com.policypilot.guard;

import java.util.regex.Pattern;

/**
 * Masks personal data before it is logged or returned to the model.
 * Covers emails, Indian mobile numbers, PAN and Aadhaar-style numbers.
 */
public final class PiiMasker {

    private static final Pattern EMAIL = Pattern.compile("([A-Za-z0-9._%+-])[A-Za-z0-9._%+-]*(@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})");
    private static final Pattern AADHAAR = Pattern.compile("\\b\\d{4}\\s?\\d{4}\\s?(\\d{4})\\b");
    private static final Pattern PHONE = Pattern.compile("(?<!\\d)(?:\\+91[- ]?)?[6-9]\\d{5}(\\d{4})(?!\\d)");
    private static final Pattern PAN = Pattern.compile("\\b[A-Z]{5}\\d{4}[A-Z]\\b");

    private PiiMasker() {
    }

    public static String mask(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String out = EMAIL.matcher(text).replaceAll("$1***$2");
        out = AADHAAR.matcher(out).replaceAll("XXXX-XXXX-$1");
        out = PHONE.matcher(out).replaceAll("XXXXXX$1");
        out = PAN.matcher(out).replaceAll("XXXXX####X");
        return out;
    }
}
