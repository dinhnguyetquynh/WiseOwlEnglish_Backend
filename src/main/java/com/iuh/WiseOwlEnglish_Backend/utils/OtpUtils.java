package com.iuh.WiseOwlEnglish_Backend.utils;

import java.security.SecureRandom;

public final class OtpUtils {

    private static final SecureRandom R = new SecureRandom();
    public static String generateNumeric(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(R.nextInt(10));
        return sb.toString();
    }
    private OtpUtils() {}
}
