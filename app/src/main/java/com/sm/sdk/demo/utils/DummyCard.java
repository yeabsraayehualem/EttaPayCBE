package com.sm.sdk.demo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.neapay.spdh.Functions;
import com.sunmi.peripheral.printer.SunmiPrinterService;
import com.neapay.spdh.SpdhMessage;
public class DummyCard {
    private static final Logger log = LoggerFactory.getLogger(DummyCard.class);
    String track2Data = "";
    String pinBlockData = "";
    String smartCardDataStr = "";
    String smartCardAdditionalDataStr = "";
    String pinLengthData = "";
    String amountData = "";
    private Context context;
    private SunmiPrinterService sunmiPrinterService;

    // Constructor to initialize context and printer service
    public DummyCard(Context context, SunmiPrinterService sunmiPrinterService) {
        this.context = context;
        this.sunmiPrinterService = sunmiPrinterService;  // Use the existing SunmiPrinterService
    }
    public void printReceipt(String refNo, String date, String merchantNo,
                             String terminalId, String cardNo, String amount, Bitmap signature,String marchentName) {
        try {
            // Start printer buffer
            sunmiPrinterService.enterPrinterBuffer(true);

            // Print merchant name (logo text)
            sunmiPrinterService.printTextWithFont(marchentName + "\n", "", 32, innerResultCallback);
            sunmiPrinterService.printText("--------------------------------\n", innerResultCallback);

            // Print reference and date
            sunmiPrinterService.printTextWithFont("REFERENCE: " + refNo + "\t\t", "", 20, innerResultCallback);
            sunmiPrinterService.printTextWithFont("DATE: " + date + "\n", "", 20, innerResultCallback);
//            sunmiPrinterService.printText("--------------------------------\n", innerResultCallback);

            // Print merchant information
            sunmiPrinterService.printTextWithFont("Merchant No.: " + merchantNo + "\n", "", 20, innerResultCallback);
            sunmiPrinterService.printTextWithFont("TID: " + terminalId + "\n", "", 20, innerResultCallback);
//            sunmiPrinterService.printText("--------------------------------\n", innerResultCallback);

            // Print transaction information
            sunmiPrinterService.printTextWithFont("Card No.: " + cardNo + "\n", "", 20, innerResultCallback);
            sunmiPrinterService.printTextWithFont("AMOUNT: ETB " + amount + "\n", "", 24, innerResultCallback);
            sunmiPrinterService.printText("--------------------------------\n", innerResultCallback);

            // Print cardholder signature prompt
            sunmiPrinterService.printTextWithFont("CARDHOLDER SIGNATURE:\n", "", 20, innerResultCallback);
            if (signature != null) {
                Bitmap cacheBitmap = BitmapUtils.scale(signature, signature.getWidth() / 3, signature.getHeight() / 3);
                cacheBitmap = BitmapUtils.replaceBitmapColor(cacheBitmap, Color.TRANSPARENT, Color.WHITE);
                if (cacheBitmap.getWidth() > 384) {
                    int newHeight = (int) (1.0 * cacheBitmap.getHeight() * 384 / cacheBitmap.getWidth());
                    cacheBitmap = BitmapUtils.scale(cacheBitmap, 384, newHeight);
                }

                // Print the signature bitmap
                sunmiPrinterService.setAlignment(1, innerResultCallback);
                sunmiPrinterService.printBitmap(cacheBitmap, innerResultCallback);
                sunmiPrinterService.setAlignment(0, innerResultCallback);
            } else {
                sunmiPrinterService.printTextWithFont("Signature not available\n", "", 20, innerResultCallback);
            }
            // Print acknowledgment message
            sunmiPrinterService.printTextWithFont("\n\nI ACKNOWLEDGE SATISFACTORY RECEIPT OF\nRELATIVE GOODS AND SERVICE.\n\n", "", 18, innerResultCallback);
            sunmiPrinterService.printTextWithFont("ETTA POS\n\n", "", 22, innerResultCallback);
            sunmiPrinterService.printText("--------------------------------\n\n\n", innerResultCallback);

            // Finish printer buffer
            sunmiPrinterService.exitPrinterBufferWithCallback(true, innerResultCallback);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // Callback for handling print result
    private final com.sunmi.peripheral.printer.InnerResultCallback innerResultCallback = new com.sunmi.peripheral.printer.InnerResultCallback() {
        @Override
        public void onRunResult(boolean isSuccess) {
            Log.d("SunmiPrinterService", "Print result: " + (isSuccess ? "Success" : "Failed"));
        }

        @Override
        public void onReturnString(String result) {
//            Log.d("SunmiPrinterService", "Return result: " + result);
        }

        @Override
        public void onRaiseException(int code, String msg) {
            Log.d("SunmiPrinterService", "Exception raised, code: " + code + ", message: " + msg);
        }

        @Override
        public void onPrintResult(int code, String msg) {
            Log.d("SunmiPrinterService", "Print result, code: " + code + ", message: " + msg);
        }
    };
    public String getTrack2Data() {
        return track2Data;
    }

    public void setTrack2Data(String track2Data) {
        this.track2Data = track2Data;
    }

    public String getPinBlockData() {
        return pinBlockData;
    }

    public void setPinBlockData(String pinBlockData) {
        this.pinBlockData = pinBlockData;
    }

    public String getSmartCardDataStr() {
        return smartCardDataStr;
    }

    public void setSmartCardDataStr(String smartCardDataStr) {
        this.smartCardDataStr = smartCardDataStr;
    }

    public String getSmartCardAdditionalDataStr() {
        return smartCardAdditionalDataStr;
    }

    public void setSmartCardAdditionalDataStr(String smartCardAdditionalDataStr) {
        this.smartCardAdditionalDataStr = smartCardAdditionalDataStr;
    }

    public String getPinLengthData() {
        return pinLengthData;
    }

    public void setPinLengthData(String pinLengthData) {
        this.pinLengthData = pinLengthData;
    }

    public String getAmountData() {
        return amountData;
    }

    public void setAmountData(String amountData) {
        this.amountData = amountData;
    }

    @NonNull
    @Override
    public String toString() {
        return "DummyCard{" +
                "track2Data='" + track2Data + '\'' +
                ", pinBlockData='" + pinBlockData + '\'' +
                ", smartCardDataStr='" + smartCardDataStr + '\'' +
                ", smartCardAdditionalDataStr='" + smartCardAdditionalDataStr + '\'' +
                ", pinLengthData='" + pinLengthData + '\'' +
                ", amountData='" + amountData + '\'' +
                '}';
    }

    private void copyKeyStoreAndTrustStoreToAppFiles() throws IOException {
        copyFileFromAssets("keystore.jks", "keystore.jks");
        copyFileFromAssets("truststore.bks", "truststore.bks");
    }

    private void copyFileFromAssets(String fileName, String destinationFileName) throws IOException {
        InputStream is = context.getAssets().open(fileName);
        File outFile = new File(context.getFilesDir(), destinationFileName);
        FileOutputStream fos = new FileOutputStream(outFile);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }

        fos.close();
        is.close();
    }

    private void setSSLProperties() {
        try {
            String trustStorePath = new File(context.getFilesDir(), "truststore.bks").getAbsolutePath();
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
            System.setProperty("javax.net.ssl.trustStoreType", "BKS");
            System.setProperty("javax.net.ssl.trustStorePassword", "Etta8707");

            Log.d("SSL Properties", "TrustStore: " + trustStorePath);
        } catch (Exception e) {
            Log.e("SSL Properties Error", "Failed to set SSL properties", e);
        }
    }

    public void makePaymentRequest(String track2data, String req, String add, String amount, Bitmap signiture,String marchentName,String billingAddress) {
        try {

            Date now = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String formattedDateTime = formatter.format(now);
            Log.d("TestTime", formattedDateTime);
            Log.d("makePaymentRequest", "Starting connection");
            System.out.println(track2data.split("=")[0]);
            // Copy the keystore and truststore
//            copyKeyStoreAndTrustStoreToAppFiles();
//            setSSLProperties(); // Set SSL properties to use the copied files

            // Initialize and start the payment request
            Functions.startConnection("172.31.9.21", 18891);
            SpdhMessage m = new SpdhMessage();
            m.setMessageType("A");
            m.setTerminalId("ETTA0001");

            m.setDownloadKey("V");
            m.unpack(Functions.sendReceive(m.pack()));
            Log.d("Keydownload  RS:", m.pack().toString());

            Log.d("Keydownload request RS:", m.getResponseCode());

            m = new SpdhMessage();
            m.setTerminalId("ETTA0001");
            m.setPurchaseTags();
            m.setMessageType("F");
            m.setTransactionCode("00");
            m.setFlag1("0");
            m.setFlag2("5");
            m.setAmount(amount);
            m.setRetailer("ETTA00000001");
            m.setSequenceNumber("1001611");
            m.setTrack2Data(";" + track2data);
            m.setEmvRequestData(req);
            m.setEmvAdditionalData(add);
            m.setEmvSupplementaryData("019F6E04220000009F660436C04000");
            m.unpack(Functions.sendReceive(m.pack()));
            Log.d("PurchaseTransaction RS:", m.getResponseCode());



//            TODO: this should be after the response is succesfull
            int a = Integer.parseInt(amount);
            printReceipt("909998uu","2024-10-21 09:50:10",m.getRetailer(),m.getTerminalId(),track2data.split("=")[0],String.valueOf(a),signiture,marchentName);

        } catch (Exception e) {
            Log.d("PaymentRequestException", e.getMessage(), e);
        }
    }
}
