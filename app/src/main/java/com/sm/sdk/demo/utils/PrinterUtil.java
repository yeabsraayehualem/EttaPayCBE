package com.sm.sdk.demo.utils;

import android.content.Context;
import android.os.RemoteException;

import com.sm.sdk.demo.MyApplication;
import com.sunmi.peripheral.printer.InnerResultCallback;
import com.sunmi.peripheral.printer.SunmiPrinterService;

public class PrinterUtil {
    private SunmiPrinterService sunmiPrinterService;

    public PrinterUtil(Context context) {
        sunmiPrinterService = MyApplication.app.sunmiPrinterService;
    }

    public boolean isPrinterAvailable() {
        return sunmiPrinterService != null;
    }

    public void printText(String text, int textSize, InnerResultCallback callback) {
        if (sunmiPrinterService != null) {
            try {
                sunmiPrinterService.printTextWithFont(text + "\n", "", textSize, callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void printTextWithBuffer(String text, int textSize, InnerResultCallback callback) {
        if (sunmiPrinterService != null) {
            try {
                sunmiPrinterService.enterPrinterBuffer(true);
                printText(text, textSize, callback);
                sunmiPrinterService.exitPrinterBufferWithCallback(true, callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setHeight(int height) {
        if (sunmiPrinterService != null) {
            byte[] returnText = new byte[3];
            returnText[0] = 0x1B;
            returnText[1] = 0x33;
            returnText[2] = (byte) height;
            try {
                sunmiPrinterService.sendRAWData(returnText, null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
