package com.sm.sdk.demo.other;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.BitmapUtils;
import com.sm.sdk.demo.utils.DeviceUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.view.LinePathView;
import com.sunmi.peripheral.printer.InnerResultCallback;
import com.sunmi.peripheral.printer.SunmiPrinterService;

public class ESignatureActivity extends BaseAppCompatActivity {

    private LinePathView handWriteView;
    private SunmiPrinterService sunmiPrinterService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e_signature);
        initToolbarBringBack(R.string.e_signature);
        sunmiPrinterService = MyApplication.app.sunmiPrinterService;
        initView();
    }

    private void initView() {
        handWriteView = findViewById(R.id.hand_write_view);
        handWriteView.clear();
        findViewById(R.id.tv_clear).setOnClickListener(this);
        findViewById(R.id.tv_ok).setOnClickListener(this);
        if (DeviceUtil.isP2SmartPad()) {
            handWriteView.post(() -> {
                ViewGroup.LayoutParams params = handWriteView.getLayoutParams();
                params.height = dp2px(110);
                handWriteView.setLayoutParams(params);
            });
        }
    }

    /** dp转px */
    private int dp2px(int dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_clear:
                handWriteView.clear();
                break;
            case R.id.tv_ok:
                showLoadingDialog(getString(R.string.handling) + "...");
                new Thread(() -> {
                    try {
                        Bitmap cacheBitmap = handWriteView.getCacheBitmap();
                        cacheBitmap = BitmapUtils.scale(cacheBitmap, cacheBitmap.getWidth() / 3, cacheBitmap.getHeight() / 3);
                        cacheBitmap = BitmapUtils.replaceBitmapColor(cacheBitmap, Color.TRANSPARENT, Color.WHITE);
                        if (cacheBitmap.getWidth() > 384) {
                            int newHeight = (int) (1.0 * cacheBitmap.getHeight() * 384 / cacheBitmap.getWidth());
                            cacheBitmap = BitmapUtils.scale(cacheBitmap, 384, newHeight);
                        }
                        sunmiPrinterService.enterPrinterBuffer(true);
                        sunmiPrinterService.setAlignment(1, InnerResultCallback);
                        sunmiPrinterService.printBitmap(cacheBitmap, InnerResultCallback);
                        sunmiPrinterService.setAlignment(0, InnerResultCallback);
                        sunmiPrinterService.lineWrap(4, InnerResultCallback);
                        sunmiPrinterService.exitPrinterBufferWithCallback(true, InnerResultCallback);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
                break;
        }
    }

    private final InnerResultCallback InnerResultCallback = new InnerResultCallback() {
        @Override
        public void onRunResult(boolean isSuccess) {
            LogUtil.e("lxy", "isSuccess:" + isSuccess);
            dismissLoadingDialog();
        }

        @Override
        public void onReturnString(String result) {
            LogUtil.e("lxy", "result:" + result);
        }

        @Override
        public void onRaiseException(int code, String msg) {
            LogUtil.e("lxy", "code:" + code + ",msg:" + msg);
        }

        @Override
        public void onPrintResult(int code, String msg) {
            LogUtil.e("lxy", "code:" + code + ",msg:" + msg);
        }
    };

}
