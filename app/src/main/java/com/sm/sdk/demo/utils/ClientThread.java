//package com.sm.sdk.demo.utils;
//
//import android.util.Log;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.InetSocketAddress;
//import java.net.Socket;
//import java.net.SocketTimeoutException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//public class ClientThread implements Runnable {
//
//
//    String track2Data = "";
//    String pinBlockData = "";
//    String smartCardDataStr = "";
//    String smartCardAdditionalDataStr = "";
//    String pinLengthData = "";
//    String amountData = "";
//    DummyCard dummyCard = new DummyCard(track2Data,pinBlockData,smartCardDataStr,smartCardAdditionalDataStr,pinLengthData,amountData);
//    String cardData = dummyCard.toString();
//    private String request;
//    private String response;
//    private Socket socket;
//    private String ip = "172.31.9.21";
//    private int port = 18891;
//
//    private int connectionTimeout = 5000; // 5 seconds
//    private int readTimeout = 30000; // 10 seconds
//
//    public ClientThread() {
//    }
//    public static String getAsciiMessageHexSize(String message) {
//        // Calculate the size of the message in bytes
//        int size = message.length();
//
//        // Convert the size to a hexadecimal string
//        String hexSize = Integer.toHexString(size);
//
//        // Ensure the hex size is properly formatted (2-byte representation, 4 characters)
//        hexSize = String.format("%04x", size);
//
//        return hexSize;
//    }
//
//    public byte[] hex2byte(byte[] b, int offset, int len) {
//        byte[] d = new byte[len];
//        for (int i = 0; i < len * 2; i++) {
//            int shift = i % 2 == 1 ? 0 : 4;
//            int hexValue = Character.digit((char) b[offset + i], 16);
//            d[i >> 1] = (byte) (d[i >> 1] | (hexValue << shift));
//        }
//        return d;
//    }
//
//    public byte[] hex2byte(String s) {
//        if (s.length() % 2 == 0) {
//            return hex2byte(s.getBytes(), 0, s.length() >> 1);
//        } else {
//            // Padding left zero to make it even size #Bug raised by tommy
//            return hex2byte("0" + s);
//        }
//    }
//
//    public byte[] hexStringToByteArray(String s) {
//        if (s.length() % 2 == 0) {
//            return hex2byte(s.getBytes(), 0, s.length() >> 1);
//        } else {
//            // Padding left zero to make it even size #Bug raised by tommy
//            return hexStringToByteArray("0" + s);
//        }
//    }
//
//    final protected char[] hexArray = "0123456789ABCDEF".toCharArray();
//
//    public String byteArrayToHexString(byte[] bytes) {
//
//        if (bytes == null)
//            return "";
//        char[] hexChars = new char[bytes.length * 2];
//        for (int j = 0; j < bytes.length; j++) {
//            int v = bytes[j] & 0xFF;
//            hexChars[j * 2] = hexArray[v >>> 4];
//            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
//        }
//        return new String(hexChars);
//    }
//
//
//    @Override
//    public void run() {
//        Date now = new Date();
//
//        // Define the date-time format
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//
//        // Format the current time
//        String formattedNow = formatter.format(now);
//
//        request = "0041392e303145545441303030312020202020202020202020202020323430373032313335303539414f39303130303030301C56303035672030303030303030303030";
//        response = sendReceive(request);
//
//
////        request = "010B392E313845545441303030312020202020202020202020202020323430373139313130363438464F30353035303030301C423030303030303030353030301C644554544130303030303030311C713B343538333030383138303935343034323D32343132363231313637343934373730303030303F1C62464443314632433030303033353645371C361E453035311E493233301E4F30313830323331323330343139313532304235384131394133383230333543303030324643343642373446304630323830303438303030303132333030303030303030303530303030363031304130334130413030301E50303130313232343230333030303038434130303030303030303331303130";
////        response = sendReceive(request);
//         Log.d("np", "Second request response: " + response);
//    }
//
//
//    public String sendReceive(String request) {
//
//
//        // Format the current time
//
//        String response = "";
//        Socket socket = null;
//        try {
//            // Connect to server with timeout
//            socket = new Socket();
//            Log.d("np", "Connecting to server...");
//            socket.connect(new InetSocketAddress(ip, port), connectionTimeout);
//             socket.setSoTimeout(readTimeout);
//            Log.d("np", "Connected to server");
//
//            // Send request
//            OutputStream output = socket.getOutputStream();
//            output.write(hexStringToByteArray(request));
//            output.flush();
//            Log.d("np", "Sent: " + request);
//
//            // Read response
//            InputStream stream = socket.getInputStream();
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            byte[] buffer = new byte[1024];
//            int bytesRead;
//
//            try {
//                while ((bytesRead = stream.read(buffer)) != -1) {
//                    byteArrayOutputStream.write(buffer, 0, bytesRead);
//                    Log.d("np", "Read " + bytesRead + " bytes");
//                }
//            } catch (SocketTimeoutException e) {
//                Log.e("np", "SocketTimeoutException: " + e.getMessage());
//                Log.e("np", Log.getStackTraceString(e));
//            }
//
//            response = byteArrayToHexString(byteArrayOutputStream.toByteArray());
//            Log.d("np", "Received: " + response);
//
//        } catch (IOException e) {
//            Log.e("np", "IOException: " + e.getMessage());
//            Log.e("np", Log.getStackTraceString(e));
//        } finally {
//            // Close socket
//            if (socket != null) {
//                try {
//                    socket.close();
//                } catch (IOException e) {
//                    Log.e("np", "IOException while closing socket: " + e.getMessage());
//                    Log.e("np", Log.getStackTraceString(e));
//                }
//            }
//        }
//        return response;
//    }
//}
