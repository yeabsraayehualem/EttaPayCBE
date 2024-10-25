package com.sm.sdk.demo.utils;

import android.os.Build;

import com.neapay.spdh.a;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class b {
    private static char[] a = "0123456789ABCDEF".toCharArray();

    public static String a(byte[] var0) {
        char[] var1 = new char[var0.length << 1];

        for(int var2 = 0; var2 < var0.length; ++var2) {
            int var3 = var0[var2] & 255;
            var1[var2 << 1] = a[var3 >>> 4];
            var1[(var2 << 1) + 1] = a[var3 & 15];
        }

        return new String(var1);
    }

    public static int a(byte[] var0, int var1) {
        var1 = 0;

        for(int var2 = 0; var2 < 2; ++var2) {
            var1 = (var1 <<= 8) | var0[var2] & 255;
        }

        return var1;
    }

    static String a(String var0, int var1, boolean var2) {
        if (var0.length() > var1) {
            var0 = var0.substring(var0.length() - var1);
        } else {
            if (var0.length() >= var1) {
                return var0;
            }

            var0 = String.format("%-" + var1 + "s", var0);
        }

        return var0;
    }

    public static String a(String var0) {
        String var1 = "";

        try {
            LocalDateTime var2 = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var2 = LocalDateTime.now();
            }
            DateTimeFormatter var4 = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var4 = DateTimeFormatter.ofPattern(var0);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var1 = var2.format(var4);
            }
        } catch (Exception var3) {
            com.neapay.spdh.a.a("Date generation exception", var3);
        }

        return var1;
    }

    public static List<byte[]> a(byte[] var0, byte var1) {
        LinkedList var2 = new LinkedList();
        int var4 = 0;

        for(int var5 = 0; var5 < var0.length; ++var5) {
            byte[] var3;
            if (var0[var5] == var1) {
                if (var5 == 0) {
                    var4 = var5;
                    continue;
                }

                var3 = Arrays.copyOfRange(var0, var4 + 1, var5);
                var2.add(var3);
                var4 = var5;
            }

            if (var5 == var0.length - 1) {
                var3 = Arrays.copyOfRange(var0, var4 + 1, var0.length);
                var2.add(var3);
            }
        }

        return var2;
    }

    public static String b(byte[] var0) {
        try {
            return new String(var0, "ASCII");
        } catch (UnsupportedEncodingException var1) {
            var1.printStackTrace();
            return "";
        }
    }
}
