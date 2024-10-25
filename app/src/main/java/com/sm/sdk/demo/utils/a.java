package com.sm.sdk.demo.utils;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class a {
    private static boolean a = false;
    private static DateFormat b = new SimpleDateFormat("dd-MM-yy HH:mm:ss:SSS");
    private static boolean c = false;
    private static boolean d = true;

    public static void a(String var0) {
        Date var1 = new Date(System.currentTimeMillis());
        System.out.println(b.format(var1) + ":" + var0);
    }

    public static void a(String var0, Exception var1) {
        a(var0);
        if (d) {
            StringWriter var2 = new StringWriter();
            PrintWriter var3 = new PrintWriter(var2);
            var1.printStackTrace(var3);
        }

    }

    public static void b(String var0) {
        a("ERR:".concat(String.valueOf(var0)));
    }

    public static void c(String var0) {
    }
}
