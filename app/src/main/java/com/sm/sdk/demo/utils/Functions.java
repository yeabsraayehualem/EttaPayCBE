package com.sm.sdk.demo.utils;

import com.neapay.spdh.a;
import com.neapay.spdh.b;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Functions {
    static Socket socket = null;
    static String ip = null;
    static int port = 0;
    protected static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public Functions() {
    }

    public static void startConnection(String var0, int var1) {
        try {
            ip = var0;
            port = var1;
            socket = new Socket(ip, port);
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }

    public static byte[] sendReceive(byte[] var0) throws SocketException {
        sendMessage(var0);
        return receiveMessage(socket);
    }

    private static byte[] receiveMessage(Socket var0) throws SocketException {
        return readBuffer(socket);
    }

    private static void sendMessage(byte[] var0) {
        writeBuffer(var0);
    }

    private static String writeBuffer(byte[] var0) {
        if (socket == null) {
            a.a("ERROR: Connection is not established. Retrying. on " + ip + "|" + port);
            startConnection(ip, port);
        }

        int var2;
        byte[] var3 = integerToBytes(var2 = var0.length);

        byte[] var1 = new byte[var2 + 2];
        System.arraycopy(var3, 0, var1, 0, 2);
        System.arraycopy(var0, 0, var1, 2, var2);
        a.a("Out:" + ip + ":" + port + ":" + byteArrayToHexString(var1));
        try {
            socket.getOutputStream().write(var1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] readBuffer(Socket var0) throws SocketException {
        a.c("Socket:" + var0.getPort());
        InputStream var1 = null;
        try {
            var1 = var0.getInputStream();
            byte[] var3 = new byte[2];

            int var2 = var1.read(var3);
            int var4 = b.a(var3, 0);
            if (var2 == -1) {
                a.a("Reading nothing, disconnecting");
                return null;
            } else {
                byte[] var6 = new byte[var4];
                var1.read(var6, 0, var4);
                a.a(" In:" + ip + ":" + port + ":" + b.a(var3) + b.a(var6));
                return var6;
            }
        } catch (SocketTimeoutException var5) {
            a.a("Socket read timeout " + var0.getSoTimeout() / 1000, var5);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String byteArrayToHexString(byte[] var0) {
        if (var0 == null) {
            return "";
        } else {
            char[] var1 = new char[var0.length << 1];

            for (int var2 = 0; var2 < var0.length; ++var2) {
                int var3 = var0[var2] & 255;
                var1[var2 << 1] = hexArray[var3 >>> 4];
                var1[(var2 << 1) + 1] = hexArray[var3 & 15];
            }

            return new String(var1);
        }
    }

    public static byte[] integerToBytes(int var0) {
        byte[] var1 = new byte[2];
        var1[0] = (byte) (var0 >> 8);
        var1[1] = (byte) var0;
        return var1;
    }

    // Method to send the key download request
    public static byte[] sendKeyDownloadRequest() throws SocketException {
        // Create the key download request byte array
        // This is an example; update it with your actual key download request format
        byte[] request = new byte[]{
                0x00, 0x01, // Example header
                0x02, // Command for key download
                // Additional fields as required for the request
        };

        return sendReceive(request);
    }
}
