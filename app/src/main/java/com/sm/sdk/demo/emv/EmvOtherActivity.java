package com.sm.sdk.demo.emv;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.card.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.ThreadPoolUtil;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Map;

public class EmvOtherActivity extends BaseAppCompatActivity {
    private TextView txtInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emv_other);
        initView();
        initData();
        checkCard();
    }

    private void initView() {
        initToolbarBringBack(R.string.emv_other);
        findViewById(R.id.query_ec_balance).setOnClickListener(this);
        txtInfo = findViewById(R.id.tv_info);
    }

    private void initData() {
        Map<String, String> configMap = EmvUtil.getConfig(EmvUtil.COUNTRY_CHINA);
        ThreadPoolUtil.executeInCachePool(
                () -> {
                    EmvUtil.initKey(getApplicationContext());
                    EmvUtil.initAidAndRid();
                    EmvUtil.setTerminalParam(configMap);
                }
        );
    }

    private void checkCard() {
        try {
            showLoadingDialog("swipe card or insert card");
            addStartTimeWithClear("checkCard()");
            int cardType = AidlConstantsV2.CardType.NFC.getValue() | AidlConstantsV2.CardType.IC.getValue();
            MyApplication.app.readCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findMagCard:" + bundle);
            dismissLoadingDialog();
            showSpendTime();
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findICCard:" + atr);
            dismissLoadingDialog();
            showSpendTime();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findRFCard:" + uuid);
            dismissLoadingDialog();
            showSpendTime();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            addEndTime("checkCard()");
            String error = "onError:" + message + " -- " + code;
            LogUtil.e(Constant.TAG, error);
            showToast(error);
            dismissLoadingDialog();
            showSpendTime();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.query_ec_balance:
                queryECBalance();
                break;
        }
    }

    @SuppressLint("DefaultLocale")
    private void queryECBalance() {
        try {
            Bundle bundle = new Bundle();
            addStartTimeWithClear("queryECBalance()");
            int code = MyApplication.app.emvOptV2.queryECBalance(bundle);
            addEndTime("queryECBalance()");
            if (code < 0) {
                LogUtil.e(TAG, "query electronic balance error,code:" + code);
                return;
            }
            String currencyCode = bundle.getString("9F51");
            long balance = bundle.getLong("9F79");
            String msg = String.format("Currency code:%s\n Balance:%d", currencyCode, balance);
            LogUtil.e(TAG, msg);
            txtInfo.setText(msg);
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
