package com.sm.sdk.demo.utils;

import android.os.RemoteException;

public interface KeyDownloadCallback {
    void onResult(boolean success, KeyDownloadResponse key) throws RemoteException;
}