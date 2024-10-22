package com.sm.sdk.demo.emv;

import static com.sm.sdk.demo.utils.SettingsActivity.KEY_MERCHANT_NAME;
import static com.sm.sdk.demo.utils.SettingsActivity.KEY_RETAILER_ID;
import static com.sm.sdk.demo.utils.SettingsActivity.KEY_TERMINAL_ID;
import static com.sm.sdk.demo.utils.SettingsActivity.PREFS_NAME;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.card.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.DummyCard;
import com.sm.sdk.demo.utils.EttaApiManager;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.PrinterUtil;
import com.sm.sdk.demo.utils.PurchaseRequest;
import com.sm.sdk.demo.utils.SettingUtil;
import com.sm.sdk.demo.utils.SettingsActivity;
import com.sm.sdk.demo.utils.ThreadPoolUtil;
import com.sm.sdk.demo.view.LinePathView;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidl.bean.CardInfo;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.bean.EMVCandidateV2;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadConfigV2;
import com.sunmi.pay.hardware.aidlv2.emv.EMVListenerV2;
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadOptV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2;
import com.sunmi.peripheral.printer.SunmiPrinterService;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This page show the emv procedure.
 * Any transaction which should do emv process can refer
 * to this page.
 */
public class ICProcessActivity extends BaseAppCompatActivity {
    private Button zero, one, two, three, four, five, six, seven, eight, nine, dot;
    RelativeLayout del;
    private boolean isVolumeDownPressed = false;
    private Handler handler;
    private Runnable longPressRunnable;

    private long backPressedTime;
    private Toast backToast;
    private EMVOptV2 mEMVOptV2;
    private PinPadOptV2 mPinPadOptV2;
    private ReadCardOptV2 mReadCardOptV2;

    private EditText mEditAmount;
    private TextView mTvShowInfo, tx_reciept_amount, mReadCardNo, refNo, tx_reciept_marchent_no, tx_reciept_terminal_id, marchentName;
    private Button mBtnOperate;
    private ImageView settings_btn;
    private int mCardType;  // card type
    private String mCardNo; // card number
    private String existedPinBlock;
    private int mPinType;   // 0-online pin, 1-offline pin
    private String mCertInfo;
    private int pinLenght = 0;
    private int mSelectIndex;

    private int mAppSelect = 0;
    private Map<String, TLV> tlvMap;
    private int mProcessStep;
    private AlertDialog mAppSelectDialog;
    private Map<String, String> configMap;
    Date currentDate = new Date();
    private static final int EMV_APP_SELECT = 1;
    private static final int EMV_FINAL_APP_SELECT = 2;
    private static final int EMV_CONFIRM_CARD_NO = 3;
    private static final int EMV_CERT_VERIFY = 4;
    private static final int EMV_SHOW_PIN_PAD = 5;
    private static final int EMV_ONLINE_PROCESS = 6;
    private static final int EMV_SIGNATURE = 7;
    private static final int EMV_TRANS_SUCCESS = 888;
    private static final int EMV_TRANS_FAIL = 999;
    private static final int REMOVE_CARD = 1000;

    private static final int PIN_CLICK_NUMBER = 50;
    private static final int PIN_CLICK_PIN = 51;
    private static final int PIN_CLICK_CONFIRM = 52;
    private static final int PIN_CLICK_CANCEL = 53;
    private static final int PIN_ERROR = 54;
    private SunmiPrinterService sunmiPrinterService;
    private String signiture = "";
    private LinePathView handWriteView;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case EMV_FINAL_APP_SELECT:
                    importFinalAppSelectStatus(0);
                    break;
                case EMV_APP_SELECT:
                    dismissLoadingDialog();
                    String[] candiNames = (String[]) msg.obj;
                    mAppSelectDialog = new AlertDialog.Builder(ICProcessActivity.this).setTitle(R.string.emv_app_select).setNegativeButton(R.string.cancel, (dialog, which) -> {
                        importAppSelect(-1);
                    }).setPositiveButton(R.string.ok, (dialog, which) -> {
                        showLoadingDialog(R.string.handling);
                        importAppSelect(mSelectIndex);
                    }).setSingleChoiceItems(candiNames, 0, (dialog, which) -> {
                        mSelectIndex = which;
                        LogUtil.e(Constant.TAG, "singleChoiceItems which:" + which);
                    }).create();
                    mSelectIndex = 0;
                    mAppSelectDialog.show();
                    break;
                case EMV_CONFIRM_CARD_NO:
                    dismissLoadingDialog();
                    mReadCardNo.setText(mCardNo);
                    tx_reciept_amount.setText(mEditAmount.getText().toString());
                    mReadCardNo.setTextColor(getResources().getColor(R.color.black));
                    mBtnOperate.setText(R.string.emv_confirm_card_no);
                    break;
                case EMV_CERT_VERIFY:
                    dismissLoadingDialog();
                    mTvShowInfo.setText(mCertInfo);
                    mBtnOperate.setText(R.string.emv_confirm_cert);
                    break;
                case EMV_SHOW_PIN_PAD:
                    dismissLoadingDialog();
                    initPinPad();
                    break;
                case EMV_ONLINE_PROCESS:
                    //mockRequestToServer();
                    try {
                        // makeRequestToServer();
                        mockRequestToServer();
                        //makeDummyRequest();
//                        makeRequestToNeaPayServer();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case EMV_SIGNATURE:
                    importSignatureStatus(0);
                    break;
                case PIN_CLICK_NUMBER:
                    break;
                case PIN_CLICK_PIN:
                    importPinInputStatus(0);
                    break;
                case PIN_CLICK_CONFIRM:
                    importPinInputStatus(2);
                    break;
                case PIN_CLICK_CANCEL:
                    showToast("user cancel");
                    setRefNo();
                    mReadCardNo.setText("");
                    tx_reciept_amount.setText("0.00");
                    mEditAmount.setText("0.00");

                    importPinInputStatus(1);
                    break;
                case PIN_ERROR:
                    showToast("error:" + msg.obj + " -- " + msg.arg1);
                    importPinInputStatus(3);
                    break;
                case EMV_TRANS_FAIL:
                    resetUI();
                    dismissLoadingDialog();
                    showToast("error:" + msg.obj + " -- " + msg.arg1);
                    break;
                case EMV_TRANS_SUCCESS:
                    resetUI();
                    checkAndRemoveCard();
                    showToast(R.string.success);
                    setRefNo();
                    break;
                case REMOVE_CARD:
                    checkAndRemoveCard();
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emv_ic);
        Intent intent = getIntent();
        if (intent != null) {
            String param1 = intent.getStringExtra("EXTRA_PARAM1");
            int param2 = intent.getIntExtra("EXTRA_PARAM2", -1);
//            Toast.makeText(this, "Param1: " + param1 + ", Param2: " + param2, Toast.LENGTH_LONG).show();
        }
        initData();
        initView();

        TextView dateView = findViewById(R.id.dateDisplay);
        LocalDateTime currentDateTime = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String formattedDateTime = currentDateTime.format(formatter);
        dateView.setText(formattedDateTime);
        marchentName = findViewById(R.id.marchentName);
        marchentName.setText("Etta POS");

        setRefNo();
        zero = findViewById(R.id.bn_0);
        one = findViewById(R.id.bn_1);
        two = findViewById(R.id.bn_2);
        three = findViewById(R.id.bn_3);
        four = findViewById(R.id.bn_4);
        five = findViewById(R.id.bn_5);
        six = findViewById(R.id.bn_6);
        seven = findViewById(R.id.bn_7);
        eight = findViewById(R.id.bn_8);
        nine = findViewById(R.id.bn_9);
        dot = findViewById(R.id.bn_dot);
        del = findViewById(R.id.bn_del);

        mReadCardNo = findViewById(R.id.cardNo);
        tx_reciept_amount = findViewById(R.id.tx_reciept_amount);
        tx_reciept_terminal_id = findViewById(R.id.tx_reciept_terminal_id);
        tx_reciept_marchent_no = findViewById(R.id.tx_reciept_marchent_no);
        marchentName = findViewById(R.id.marchentName);
        handWriteView = findViewById(R.id.hand_write_view);
        setStatic();
        del.setOnClickListener(view -> {
            changeText("x");
        });

        dot.setOnClickListener(view -> {
            changeText(".");
        });
        zero.setOnClickListener(view -> {
            changeText("0");
        });

        one.setOnClickListener(view -> {
            changeText("1");
        });

        two.setOnClickListener(view -> {
            changeText("2");
        });

        three.setOnClickListener(view -> {
            changeText("3");
        });

        four.setOnClickListener(view -> {
            changeText("4");
        });

        five.setOnClickListener(view -> {
            changeText("5");
        });

        six.setOnClickListener(view -> {
            changeText("6");
        });

        seven.setOnClickListener(view -> {
            changeText("7");
        });

        eight.setOnClickListener(view -> {
            changeText("8");
        });

        nine.setOnClickListener(view -> {
            changeText("9");
        });
        settings_btn = findViewById(R.id.settings_btn);
        settings_btn.setOnClickListener(v -> {
            showPinDialog();
        });
        ImageView calculator_bnt = findViewById(R.id.calculator_btn);
        calculator_bnt.setOnClickListener(v->{
            showCalculatorDialog();
        });
        sunmiPrinterService = MyApplication.app.sunmiPrinterService;


    }

    private void goToSettings() {

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void showPinDialog() {
        // Create a dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_pin, null);
        builder.setView(dialogView);

        EditText pinInput = dialogView.findViewById(R.id.pin_input);
        Button buttonOk = dialogView.findViewById(R.id.button_ok);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);

        AlertDialog dialog = builder.create();

        // Set the OK button action
        buttonOk.setOnClickListener(v -> {
            String pinCode = pinInput.getText().toString();
            if (isPinValid(pinCode)) {
                dialog.dismiss();
                goToSettings(); // Navigate to SettingsActivity
            } else {
                Toast.makeText(this, "Invalid PIN. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set the Cancel button action
        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private boolean isPinValid(String pinCode) {
        // Replace "1234" with your actual PIN code
        return "1234".equals(pinCode);
    }

    private void setRefNo() {
        LocalDateTime endTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            endTime = LocalDateTime.now();
        }
        LocalDateTime startTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startTime = LocalDateTime.of(2020, 10, 5, 0, 0);
        }

        Duration duration = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            duration = Duration.between(startTime, endTime);
        }

        long totalSeconds = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            totalSeconds = duration.getSeconds();
        }

        String hexValue = Long.toHexString(totalSeconds).toUpperCase();

        refNo = findViewById(R.id.refNo);
        refNo.setText(hexValue);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (!isVolumeDownPressed) {
                isVolumeDownPressed = true;
                // Start counting time after the Volume Down button is pressed
                handler.postDelayed(longPressRunnable, 10000); // 10 seconds
                Toast.makeText(this, "Hold Volume Down to open settings...", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void showCalculatorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_clculator, null);
        builder.setView(dialogView);

        TextView display = dialogView.findViewById(R.id.display);
        Button buttonConfirm = dialogView.findViewById(R.id.button_confirm);

        setCalculatorButtonClickListeners(dialogView, display);

        AlertDialog dialog = builder.create();

        buttonConfirm.setOnClickListener(v -> {
            String result = display.getText().toString();
            addToExistingAmount(result);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setCalculatorButtonClickListeners(View dialogView, TextView display) {
        // Number buttons
        for (int i = 0; i <= 9; i++) {
            int resId = dialogView.getResources().getIdentifier("button_" + i, "id", getPackageName());
            int finalI = i;
            dialogView.findViewById(resId).setOnClickListener(v -> {
                String currentText = display.getText().toString();
                display.setText(currentText + finalI);
            });
        }

        // Operation buttons
        dialogView.findViewById(R.id.button_add).setOnClickListener(v -> appendOperator(display, "+"));
        dialogView.findViewById(R.id.button_subtract).setOnClickListener(v -> appendOperator(display, "-"));
        dialogView.findViewById(R.id.button_multiply).setOnClickListener(v -> appendOperator(display, "*"));
        dialogView.findViewById(R.id.button_divide).setOnClickListener(v -> appendOperator(display, "/"));
        dialogView.findViewById(R.id.button_clear).setOnClickListener(v -> display.setText(""));
        dialogView.findViewById(R.id.button_equals).setOnClickListener(v -> {
            String result = calculateResult(display.getText().toString());
            display.setText(result);
        });
    }

    private void appendOperator(TextView display, String operator) {
        String currentText = display.getText().toString();
        if (!currentText.isEmpty() && !currentText.endsWith(operator)) {
            display.setText(currentText + operator);
        }
    }

    private String calculateResult(String input) {
        String[] tokens = input.split("(?<=[-+*/])|(?=[-+*/])"); // Split input into tokens
        if (tokens.length == 0) {
            return "0"; // Return 0 if no input
        }

        double result = Double.parseDouble(tokens[0]);
        for (int i = 1; i < tokens.length; i += 2) {
            String operator = tokens[i];
            double number = Double.parseDouble(tokens[i + 1]);

            switch (operator) {
                case "+":
                    result += number;
                    break;
                case "-":
                    result -= number;
                    break;
                case "*":
                    result *= number;
                    break;
                case "/":
                    if (number != 0) {
                        result /= number;
                    } else {
                        return "Error"; // Handle division by zero
                    }
                    break;
                default:
                    return "Error"; // Invalid operator
            }
        }
        return String.valueOf(result);
    }

    private void addToExistingAmount(String result) {
        try {
            double existingAmount = getExistingAmount();
            double newAmount = existingAmount + Double.parseDouble(result);
            updateAmount(newAmount);
        } catch (NumberFormatException e) {
            // Handle errors
        }
    }

    private double getExistingAmount() {
        String existingValue = mEditAmount.getText().toString();
        try{
            double existVal = Double.parseDouble(existingValue);
            return existVal;
        } catch(Exception e){
            return 0.0;
        }
    }

    private void updateAmount(double amount) {

        mEditAmount.setText(amount+"");
        mProcessStep=0;
    }
    private void changeText(String key) {
        String amount = mEditAmount.getText().toString();
        if (key.equals("x")) {

            if (!amount.isEmpty()) {
                amount = amount.substring(0, amount.length() - 1);

            }
        } else {
            amount += key;
        }
        mEditAmount.setText(amount);
    }

    private void setStatic() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        marchentName.setText(sharedPreferences.getString(KEY_MERCHANT_NAME, "TEST MARCHENT"));
        tx_reciept_marchent_no.setText(sharedPreferences.getString(KEY_RETAILER_ID, "TEST MARCHENT ID"));
        tx_reciept_terminal_id.setText(sharedPreferences.getString(KEY_TERMINAL_ID, "TEXT TERMINAL ID"));
    }

    private void initView() {
        mEMVOptV2 = MyApplication.app.emvOptV2;
        mPinPadOptV2 = MyApplication.app.pinPadOptV2;
        mReadCardOptV2 = MyApplication.app.readCardOptV2;
        Toolbar toolbar = findViewById(R.id.toolbar);
//        toolbar.setTitle(R.string.emv_ic_process);
//        toolbar.setNavigationIcon(R.drawable.ic_back_white);
//        toolbar.setTitle(R.string.etta_pay_title);
//        setSupportActionBar(toolbar);
//        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        mEditAmount = findViewById(R.id.edit_amount);
//        mTvShowInfo = findViewById(R.id.tv_info);
//        mTvShowInfo= findViewById(R.id.cardNo);

        mBtnOperate = findViewById(R.id.mb_ok);

        findViewById(R.id.mb_ok).setOnClickListener(this);
    }

    /**
     * Do essential initialization, client App should
     * initialize their own data at this step.
     * Note: this method just show how to initialize data before
     * the emv process, the initialized data may not useful for your
     * App. Please init your own data.
     */
    private void initData() {
        // disable check card buzzer
        SettingUtil.setBuzzerEnable(false);
        // Client should config their own countryCode,capability,etc...
        configMap = EmvUtil.getConfig(EmvUtil.COUNTRY_CHINA);
        ThreadPoolUtil.executeInCachePool(() -> {
            EmvUtil.initKey(getApplicationContext());
            EmvUtil.initAidAndRid();
            EmvUtil.setTerminalParam(configMap);
//            showToast("emv init process finished.");
        });
    }

    //
//    @Override
//    public void onBackPressed() {
////        if (mProcessStep == EMV_APP_SELECT) {
////            importAppSelect(-1);
////        } else if (mProcessStep == EMV_FINAL_APP_SELECT) {
////            importFinalAppSelectStatus(-1);
////        } else if (mProcessStep == EMV_CONFIRM_CARD_NO) {
////            importCardNoStatus(1);
////        } else if (mProcessStep == EMV_CERT_VERIFY) {
////            importCertStatus(1);
////        } else if (mProcessStep == PIN_ERROR) {
////            importPinInputStatus(3);
////        } else if (mProcessStep == EMV_ONLINE_PROCESS) {
//////            importOnlineProcessStatus(1);
////        } else if (mProcessStep == EMV_SIGNATURE) {
////            importSignatureStatus(1);
////        }
//        super.onBackPressed();
//    }
    @Override
    public void onBackPressed() {
        // Check if the back button was pressed within 2 seconds
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            // Cancel the existing toast if it's showing
            if (backToast != null) backToast.cancel();
            // Close the app completely
            finishAffinity(); // This closes all activities and exits the app
            // Alternatively, you can use finishAndRemoveTask() to remove the task as well
            // finishAndRemoveTask();
            return;
        } else {
            // Show a message to the user
            backToast = Toast.makeText(getBaseContext(), "Tap back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis(); // Update the back pressed time
    }


    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ok:
                if (mProcessStep == 0) {
                    LogUtil.e(Constant.TAG, "***************************************************************");
                    LogUtil.e(Constant.TAG, "****************************Start Process**********************");
                    LogUtil.e(Constant.TAG, "***************************************************************");
                    Log.d(TAG, "onClick: " + mEditAmount.getText().toString());
//                    mTvShowInfo.setText("");
                    String amount = mEditAmount.getText().toString();
                    try {
                        // Before check card, initialize emv process(clear all TLV)
                        addStartTimeWithClear("start");
                        addStartTime("initEmvProcess");
                        mEMVOptV2.initEmvProcess();
                        addEndTime("initEmvProcess");
                        addStartTime("initEmvTlvData");
                        initEmvTlvData();
                        addEndTime("initEmvTlvData");
                        long parseLong = (long) Double.parseDouble(amount);
                        if (parseLong > 0) {
                            checkCard();
                        } else {
                            showToast(R.string.card_cost_hint);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast(R.string.card_cost_hint);
                    }
                } else if (mProcessStep == EMV_CONFIRM_CARD_NO) {
                    showLoadingDialog(R.string.handling);
                    importCardNoStatus(0);
                } else if (mProcessStep == EMV_CERT_VERIFY) {
                    showLoadingDialog(R.string.handling);
                    importCertStatus(0);
                }
                break;
        }
    }

    /**
     * Set tlv essential tlv data
     */
    private void initEmvTlvData() {
        try {
            // set PayPass(MasterCard) tlv data
            String[] tagsPayPass = {"DF8117", "DF8118", "DF8119", "DF811F", "DF811E", "DF812C", "DF8123", "DF8124", "DF8125", "DF8126", "DF811B", "DF811D", "DF8122", "DF8120", "DF8121"};
            String[] valuesPayPass = {"E0", "F8", "F8", "E8", "00", "00", "000000000000", "000000100000", "999999999999", "000000100000", "30", "02", "0000000000", "000000000000", "000000000000"};
            mEMVOptV2.setTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS, tagsPayPass, valuesPayPass);

            // set AMEX(AmericanExpress) tlv data
            String[] tagsAE = {"9F6D", "9F6E", "9F33", "9F35", "DF8168", "DF8167", "DF8169", "DF8170"};
            String[] valuesAE = {"C0", "D8E00000", "E0E888", "22", "00", "00", "00", "60"};
            mEMVOptV2.setTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_AE, tagsAE, valuesAE);

            String[] tagsJCB = {"9F53", "DF8161"};
            String[] valuesJCB = {"708000", "7F00"};
            mEMVOptV2.setTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_JCB, tagsJCB, valuesJCB);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start check card
     */
    private void checkCard() {
        try {
            showLoadingDialog(R.string.emv_swing_card_ic);
            int cardType = AidlConstantsV2.CardType.NFC.getValue() | AidlConstantsV2.CardType.IC.getValue();
            addStartTime("checkCard()");

            mReadCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check card callback
     */
    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findMagCard:" + bundle);
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findICCard:" + atr);
            //IC card Beep buzzer when check card success
            MyApplication.app.basicOptV2.buzzerOnDevice(1, 2750, 200, 0);
            mCardType = AidlConstantsV2.CardType.IC.getValue();
            transactProcess();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            addEndTime("checkCard()");
            LogUtil.e(Constant.TAG, "findRFCard:" + uuid);
            mCardType = AidlConstantsV2.CardType.NFC.getValue();
            transactProcess();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            addEndTime("checkCard()");
            String error = "onError:" + message + " -- " + code;
            LogUtil.e(Constant.TAG, error);
            showToast(error);
            dismissLoadingDialog();
        }
    };

    /**
     * Start emv transact process
     */
    private void transactProcess() {
        LogUtil.e(Constant.TAG, "transactProcess");
        try {
            Bundle bundle = new Bundle();
            bundle.putString("amount", mEditAmount.getText().toString());
            bundle.putString("transType", "00");
            //flowType:0x01-emv standard, 0x04：NFC-Speedup
            //Note:(1) flowType=0x04 only valid for QPBOC,PayPass,PayWave contactless transaction
            //     (2) set fowType=0x04, only EMVListenerV2.onRequestShowPinPad(),
            //         EMVListenerV2.onCardDataExchangeComplete() and EMVListenerV2.onTransResult() may will be called.
            if (mCardType == AidlConstantsV2.CardType.NFC.getValue()) {
                bundle.putInt("flowType", AidlConstantsV2.EMV.FlowType.TYPE_NFC_SPEEDUP);
            } else {
                bundle.putInt("flowType", AidlConstantsV2.EMV.FlowType.TYPE_EMV_STANDARD);
            }
            bundle.putInt("cardType", mCardType);
//            bundle.putBoolean("preProcessCompleted", false);
//            bundle.putInt("emvAuthLevel", 0);
            addTransactionStartTimes();
            mEMVOptV2.transactProcessEx(bundle, mEMVListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void addTransactionStartTimes() {
        addStartTime("transactProcess");
        addStartTime("onTransactionStart");
        addStartTime("onWaitAppSelect()");
        addStartTime("onAppFinalSelect()");
        addStartTime("onConfirmCardNo()");
        addStartTime("onRequestShowPinPad()");
        addStartTime("onRequestSignature()");
        addStartTime("onCertVerify()");
        addStartTime("onOnlineProc()");
        addStartTime("onCardDataExchangeComplete()");
        addStartTime("onTransResult()");
        addStartTime("onConfirmationCodeVerified()");
        addStartTime("onRequestDataExchange()");
        addStartTime("onTermRiskManagement()");
    }

    /**
     * EMV process callback
     */
    private final EMVListenerV2 mEMVListener = new EMVListenerV2.Stub() {
        /**
         * Notify client to do multi App selection, this method may called when card have more than one Application
         * <br/> For Contactless and flowType set as AidlConstants.FlowType.TYPE_NFC_SPEEDUP, this
         * method will not be called
         *
         * @param appNameList   The App list for selection
         * @param isFirstSelect is first time selection
         */
        @Override
        public void onWaitAppSelect(List<EMVCandidateV2> appNameList, boolean isFirstSelect) throws RemoteException {
            addEndTime("onWaitAppSelect()");
            LogUtil.e(Constant.TAG, "onWaitAppSelect isFirstSelect:" + isFirstSelect);
            mProcessStep = EMV_APP_SELECT;
            String[] candidateNames = getCandidateNames(appNameList);
            mHandler.obtainMessage(EMV_APP_SELECT, candidateNames).sendToTarget();
        }

        /**
         * Notify client the final selected Application
         * <br/> For Contactless and flowType set as AidlConstants.FlowType.TYPE_NFC_SPEEDUP, this
         * method will not be called
         *
         * @param tag9F06Value The final selected Application id
         */
        @Override
        public void onAppFinalSelect(String tag9F06Value) throws RemoteException {
            addEndTime("onAppFinalSelect()");
            LogUtil.e(Constant.TAG, "onAppFinalSelect tag9F06Value:" + tag9F06Value);
            if (tag9F06Value != null && tag9F06Value.length() > 0) {
                boolean isUnionPay = tag9F06Value.startsWith("A000000333");
                boolean isVisa = tag9F06Value.startsWith("A000000003");
                boolean isMaster = tag9F06Value.startsWith("A000000004") || tag9F06Value.startsWith("A000000005");
                boolean isAmericanExpress = tag9F06Value.startsWith("A000000025");
                boolean isJCB = tag9F06Value.startsWith("A000000065");
                boolean isRupay = tag9F06Value.startsWith("A000000524");
                boolean isPure = tag9F06Value.startsWith("D999999999") || tag9F06Value.startsWith("D888888888") || tag9F06Value.startsWith("D777777777") || tag9F06Value.startsWith("D666666666") || tag9F06Value.startsWith("A000000615");
                String paymentType = "unknown";
                if (isUnionPay) {
                    paymentType = "UnionPay";
                    mAppSelect = 0;
                } else if (isVisa) {
                    paymentType = "Visa";
                    mAppSelect = 1;
                } else if (isMaster) {
                    paymentType = "MasterCard";
                    mAppSelect = 2;
                } else if (isAmericanExpress) {
                    paymentType = "AmericanExpress";
                } else if (isJCB) {
                    paymentType = "JCB";
                } else if (isRupay) {
                    paymentType = "Rupay";
                } else if (isPure) {
                    paymentType = "Pure";
                }
                LogUtil.e(Constant.TAG, "detect " + paymentType + " card");
            }
            mProcessStep = EMV_FINAL_APP_SELECT;
            mHandler.obtainMessage(EMV_FINAL_APP_SELECT, tag9F06Value).sendToTarget();
        }

        /**
         * Notify client to confirm card number
         * <br/> For Contactless and flowType set as AidlConstants.FlowType.TYPE_NFC_SPEEDUP, this
         * method will not be called
         *
         * @param cardNo The card number
         */
        @Override
        public void onConfirmCardNo(String cardNo) throws RemoteException {
            addEndTime("onConfirmCardNo()");
            LogUtil.e(Constant.TAG, "onConfirmCardNo cardNo:" + cardNo);
            mCardNo = cardNo;
            mProcessStep = EMV_CONFIRM_CARD_NO;
            mHandler.obtainMessage(EMV_CONFIRM_CARD_NO).sendToTarget();
//            importCardNoStatus(0);
        }

        /**
         * Notify client to input PIN
         *
         * @param pinType    The PIN type, 0-online PIN，1-offline PIN
         * @param remainTime The the remain retry times of offline PIN, for online PIN, this param
         *                   value is always -1, and if this is the first time to input PIN, value
         *                   is -1 too.
         */
        @Override
        public void onRequestShowPinPad(int pinType, int remainTime) throws RemoteException {
            addEndTime("onRequestShowPinPad()");
            LogUtil.e(Constant.TAG, "onRequestShowPinPad pinType:" + pinType + " remainTime:" + remainTime);
            mPinType = pinType;
            if (mCardNo == null) {
                mCardNo = getCardNo();
            }
            mProcessStep = EMV_SHOW_PIN_PAD;
            mHandler.obtainMessage(EMV_SHOW_PIN_PAD).sendToTarget();
        }

        /**
         * Notify  client to do signature
         */
        @Override
        public void onRequestSignature() throws RemoteException {
            addEndTime("onRequestSignature()");
            LogUtil.e(Constant.TAG, "onRequestSignature");
            mProcessStep = EMV_SIGNATURE;
            mHandler.obtainMessage(EMV_SIGNATURE).sendToTarget();
        }

        /**
         * Notify client to do certificate verification
         *
         * @param certType The certificate type, refer to AidlConstants.CertType
         * @param certInfo The certificate info
         */
        @Override
        public void onCertVerify(int certType, String certInfo) throws RemoteException {
            addEndTime("onCertVerify()");
            LogUtil.e(Constant.TAG, "onCertVerify certType:" + certType + " certInfo:" + certInfo);
            mCertInfo = certInfo;
            mProcessStep = EMV_CERT_VERIFY;
            mHandler.obtainMessage(EMV_CERT_VERIFY).sendToTarget();
        }

        /**
         * Notify client to do online process
         */
        @Override
        public void onOnlineProc() throws RemoteException {
            addEndTime("onOnlineProc()");
            LogUtil.e(Constant.TAG, "onOnlineProcess");
            mProcessStep = EMV_ONLINE_PROCESS;
            mHandler.obtainMessage(EMV_ONLINE_PROCESS).sendToTarget();
        }

        /**
         * Notify client EMV kernel and card data exchange finished, client can remove card
         */
        @Override
        public void onCardDataExchangeComplete() throws RemoteException {
            addEndTime("onCardDataExchangeComplete()");
            LogUtil.e(Constant.TAG, "onCardDataExchangeComplete");
            if (mCardType == AidlConstantsV2.CardType.NFC.getValue()) {
                //NFC card Beep buzzer to notify remove card
                addStartTime("buzzerOnDevice()");
                MyApplication.app.basicOptV2.buzzerOnDevice(1, 2750, 200, 0);
                addEndTime("buzzerOnDevice()");
            }
        }

        /**
         * Notify client EMV process ended
         *
         * @param code The transaction result code, 0-success, 1-offline approval, 2-offline denial,
         *             4-try again, other value-error code
         * @param desc The corresponding message of this code
         */
        @Override
        public void onTransResult(int code, String desc) throws RemoteException {
            addEndTime("onTransResult()");
            if (mCardNo == null) {
                mCardNo = getCardNo();
            }
            getCardNo();
            //2335

            LogUtil.e(Constant.TAG, "onTransResult code:" + code + " desc:" + desc);
            LogUtil.e(Constant.TAG, "***************************************************************");
            LogUtil.e(Constant.TAG, "****************************End Process************************");
            LogUtil.e(Constant.TAG, "***************************************************************");

            //mockRequestToServer();
            try {
                // makeRequestToServer();
                mockRequestToServer();
                //  makeDummyRequest();
                //makeRequestToNeaPayServer();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (code == 0) {
                mHandler.obtainMessage(EMV_TRANS_SUCCESS, code, code, desc).sendToTarget();
            } else if (code == 4) {
                tryAgain();
            } else {
                mHandler.obtainMessage(EMV_TRANS_FAIL, code, code, desc).sendToTarget();
            }
            showSpendTime();
        }

        /**
         * Notify client the confirmation code verified(See phone)
         */
        @Override
        public void onConfirmationCodeVerified() throws RemoteException {
            addEndTime("onConfirmationCodeVerified()");
            showSpendTime();
            LogUtil.e(Constant.TAG, "onConfirmationCodeVerified");

            byte[] outData = new byte[512];
            int len = mEMVOptV2.getTlv(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS, "DF8129", outData);
            if (len > 0) {
                byte[] data = new byte[len];
                System.arraycopy(outData, 0, data, 0, len);
                String hexStr = ByteUtil.bytes2HexStr(data);
                LogUtil.e(Constant.TAG, "DF8129: " + hexStr);
            }
            // card off
            mReadCardOptV2.cardOff(mCardType);
            runOnUiThread(() -> new AlertDialog.Builder(ICProcessActivity.this).setTitle("See Phone").setMessage("execute See Phone flow").setPositiveButton("OK", (dia, which) -> {
                dia.dismiss();
                // Restart transaction procedure.
                try {
                    mEMVOptV2.initEmvProcess();
                    checkCard();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).show());
        }

        /**
         * Notify client to exchange data
         * <br/> This method only used for Russia MIR
         *
         * @param cardNo The card number
         */
        @Override
        public void onRequestDataExchange(String cardNo) throws RemoteException {
            addEndTime("onRequestDataExchange()");
            LogUtil.e(Constant.TAG, "onRequestDataExchange,cardNo:" + cardNo);
            mEMVOptV2.importDataExchangeStatus(0);
        }

        @Override
        public void onTermRiskManagement() throws RemoteException {
            addEndTime("onTermRiskManagement()");
            LogUtil.e(Constant.TAG, "onTermRiskManagement");
            mEMVOptV2.importTermRiskManagementStatus(0);
        }

        @Override
        public void onPreFirstGenAC() throws RemoteException {
            addEndTime("onPreFirstGenAC()");
            LogUtil.e(Constant.TAG, "onPreFirstGenAC");
            mEMVOptV2.importPreFirstGenACStatus(0);
        }

        @Override
        public void onDataStorageProc(String[] containerID, String[] containerContent) throws RemoteException {
            addEndTime("onDataStorageProc()");
            LogUtil.e(Constant.TAG, "onDataStorageProc,");
            //此回调为Dpas2.0专用
            //根据需求配置tag及values
            String[] tags = new String[0];
            String[] values = new String[0];
            mEMVOptV2.importDataStorage(tags, values);
        }

    };

    /**
     * getCard number
     */
    private String getCardNo() {
        LogUtil.e(Constant.TAG, "getCardNo");
        try {
            String[] tagList = {"57", "5A"};
            byte[] outData = new byte[256];
            addStartTime("getCardNo()");
            int len = mEMVOptV2.getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tagList, outData);
            addEndTime("getCardNo()");
            if (len <= 0) {
                LogUtil.e(Constant.TAG, "getCardNo error,code:" + len);
                return "";
            }
            byte[] bytes = Arrays.copyOf(outData, len);
            tlvMap = TLVUtil.buildTLVMap(bytes);

            if (!TextUtils.isEmpty(Objects.requireNonNull(tlvMap.get("57")).getValue())) {
                TLV tlv57 = tlvMap.get("57");
                Log.d("TLV57", "tlv57: " + tlv57.getValue());
                CardInfo cardInfo = parseTrack2(tlv57.getValue());

//                Log.d("Trace2", "getCardNo: "+ cardInfo.);
                Log.d("The card No", "getCardNo: " + cardInfo.cardNo);
                return cardInfo.cardNo;
            }
            if (!TextUtils.isEmpty(Objects.requireNonNull(tlvMap.get("5A")).getValue())) {

                return Objects.requireNonNull(tlvMap.get("5A")).getValue();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getTrack2() {
        LogUtil.e(Constant.TAG, "getCardNo");
        try {
            String[] tagList = {"57", "5A"};
            byte[] outData = new byte[256];
            addStartTime("getCardNo()");
            int len = mEMVOptV2.getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tagList, outData);
            addEndTime("getCardNo()");
            if (len <= 0) {
                LogUtil.e(Constant.TAG, "getCardNo error,code:" + len);
                return "";
            }
            byte[] bytes = Arrays.copyOf(outData, len);
            Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(bytes);
            if (!TextUtils.isEmpty(Objects.requireNonNull(tlvMap.get("57")).getValue())) {
                TLV tlv57 = tlvMap.get("57");
                CardInfo cardInfo = parseTrack2(tlv57.getValue());
                Log.d("Trace 2", "getTrack2: " + tlv57.getValue());
                return cardInfo.track2;
            }
            if (!TextUtils.isEmpty(Objects.requireNonNull(tlvMap.get("5A")).getValue())) {
                return Objects.requireNonNull(tlvMap.get("5A")).getValue();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Parse track2 data
     */
    public static CardInfo parseTrack2(String track2) {
        LogUtil.e(Constant.TAG, "track2:" + track2);
        // we get this when the user inserts the correct pin.
//        tlv57: 4583008180289464D24126211053861200000F
        String track_2 = stringFilter(track2);
        int index = track_2.indexOf("=");
        if (index == -1) {
            index = track_2.indexOf("D");
        }
        CardInfo cardInfo = new CardInfo();
        if (index == -1) {
            return cardInfo;
        }
        String cardNumber = "";
        if (track_2.length() > index) {
            cardNumber = track_2.substring(0, index);
        }
        String expiryDate = "";
        if (track_2.length() > index + 5) {
            expiryDate = track_2.substring(index + 1, index + 5);
        }
        String serviceCode = "";
        if (track_2.length() > index + 8) {
            serviceCode = track_2.substring(index + 5, index + 8);
        }
        LogUtil.e(Constant.TAG, "cardNumber:" + cardNumber + " expireDate:" + expiryDate + " serviceCode:" + serviceCode);
        cardInfo.cardNo = cardNumber;
        cardInfo.expireDate = expiryDate;
        cardInfo.serviceCode = serviceCode;
        Log.d("Parse Trace", "parseTrack2: " + cardInfo.toString());
        return cardInfo;
    }

    /**
     * remove characters not number,=,D
     */
    static String stringFilter(String str) {
        String regEx = "[^0-9=D]";
        Pattern p = Pattern.compile(regEx);
        Matcher matcher = p.matcher(str);
        return matcher.replaceAll("").trim();
    }

    /**
     * Start show PinPad
     */
    private void initPinPad() {
        LogUtil.e(Constant.TAG, "initPinPad");
        try {
            PinPadConfigV2 pinPadConfig = new PinPadConfigV2();
            pinPadConfig.setPinPadType(0);
            pinPadConfig.setPinType(mPinType);
            pinPadConfig.setOrderNumKey(false);
            byte[] panBytes = mCardNo.substring(mCardNo.length() - 13, mCardNo.length() - 1).getBytes("US-ASCII");
            pinPadConfig.setPan(panBytes);
            pinPadConfig.setTimeout(60 * 1000); // input password timeout
            pinPadConfig.setPinKeyIndex(12);    // pik index
            pinPadConfig.setMaxInput(12);
            pinPadConfig.setMinInput(0);
            pinPadConfig.setKeySystem(0);
            pinPadConfig.setAlgorithmType(0);
            addStartTime("initPinPad()");
            mPinPadOptV2.initPinPad(pinPadConfig, mPinPadListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Input pin callback
     */
    private final PinPadListenerV2 mPinPadListener = new PinPadListenerV2.Stub() {

        @Override
        public void onPinLength(int len) {
            LogUtil.e(Constant.TAG, "onPinLength:" + len);
            if (len > pinLenght) {
                pinLenght = len;
            }
            mHandler.obtainMessage(PIN_CLICK_NUMBER, len).sendToTarget();
        }

        @Override
        public void onConfirm(int i, byte[] pinBlock) {
            addEndTime("initPinPad()");
            if (pinBlock != null) {
                String hexStr = ByteUtil.bytes2HexStr(pinBlock);
                LogUtil.e(Constant.TAG, "onConfirm pin block:" + hexStr);
                existedPinBlock = hexStr;
                mHandler.obtainMessage(PIN_CLICK_PIN, pinBlock).sendToTarget();
            } else {
                mHandler.obtainMessage(PIN_CLICK_CONFIRM).sendToTarget();
            }
        }

        @Override
        public void onCancel() {
            addEndTime("initPinPad()");
            LogUtil.e(Constant.TAG, "onCancel");
            mHandler.obtainMessage(PIN_CLICK_CANCEL).sendToTarget();
        }

        @Override
        public void onError(int code) {
            addEndTime("initPinPad()");
            LogUtil.e(Constant.TAG, "onError:" + code);
            String msg = AidlErrorCodeV2.valueOf(code).getMsg();
            mHandler.obtainMessage(PIN_ERROR, code, code, msg).sendToTarget();
        }
    };

    /**
     * Notify emv process the Application select result
     *
     * @param selectIndex the index of selected App, start from 0
     */
    private void importAppSelect(int selectIndex) {
        LogUtil.e(Constant.TAG, "importAppSelect selectIndex:" + selectIndex);
        try {
            mEMVOptV2.importAppSelect(selectIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Notify emv process the final Application select result
     *
     * @param status 0:success, other value:failed
     */
    private void importFinalAppSelectStatus(int status) {
        try {
            LogUtil.e(Constant.TAG, "importFinalAppSelectStatus status:" + status);
            mEMVOptV2.importAppFinalSelectStatus(status);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Notify emv process the card number confirm status
     *
     * @param status 0:success, other value:failed
     */
    private void importCardNoStatus(int status) {
        LogUtil.e(Constant.TAG, "importCardNoStatus status:" + status);
        try {
            mEMVOptV2.importCardNoStatus(status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Notify emv process the certification verify status
     *
     * @param status 0:success, other value:failed
     */
    private void importCertStatus(int status) {
        LogUtil.e(Constant.TAG, "importCertStatus status:" + status);
        try {
            mEMVOptV2.importCertStatus(status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Notify emv process the PIN input result
     *
     * @param inputResult 0:success,1:input PIN canceled,2:input PIN skipped,3:PINPAD problem,4:input PIN timeout
     */
    private void importPinInputStatus(int inputResult) {
        LogUtil.e(Constant.TAG, "importPinInputStatus:" + inputResult);
        try {
            mEMVOptV2.importPinInputStatus(mPinType, inputResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Import signature result to emv process
     *
     * @param status 0:success, other value:failed
     */
    private void importSignatureStatus(int status) {
        LogUtil.e(Constant.TAG, "importSignatureStatus status:" + status);
        try {
            mEMVOptV2.importSignatureStatus(status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Import online process result data(eg: field 55 ) to emv process.
     * if no date to import, set param tags and values as empty array
     *
     * @param status 0:online approval, 1:online denial, 2:online failed
     */
//    private void importOnlineProcessStatus(int status) {
//        LogUtil.e(Constant.TAG, "importOnlineProcessStatus status:" + status);
//        try {
//            String[] tags = {"71", "72", "91", "8A", "89"};
//            String[] values = {"", "", "", "", ""};
//            byte[] out = new byte[1024];
//            int len = mEMVOptV2.importOnlineProcStatus(status, tags, values, out);
//            if (len < 0) {
//                LogUtil.e(Constant.TAG, "importOnlineProcessStatus error,code:" + len);
//            } else {
//                byte[] bytes = Arrays.copyOf(out, len);
//                String hexStr = ByteUtil.bytes2HexStr(bytes);
//                LogUtil.e(Constant.TAG, "importOnlineProcessStatus outData:" + hexStr);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Mock a POSP to do some data exchange(online process), we don't have a really POSP,
     * client should connect to a really POSP at this step.
     */
    String emvRequest, sumplimentary, additional;

    private void mockRequestToServer() {
        new Thread(() -> {
            try {
                Log.d("MockRequestToServer", "Thread started");
                Log.d("MockRequestToServer", "makePaymentRequest completed");

                showLoadingDialog(R.string.requesting);

                if (AidlConstantsV2.CardType.MAGNETIC.getValue() != mCardType) {
                    StringBuilder carddata = getTlvData();
                    assert carddata != null;

                    String track2 = null;
                    if (!TextUtils.isEmpty(Objects.requireNonNull(tlvMap.get("57")).getValue())) {
                        TLV tlv57 = tlvMap.get("57");
                        track2 = tlv57.getValue().toString();
                        TLV tlv02 = tlvMap.get("02");
                        String tlv02String = tlv02.toString();
                    }

                    SimpleDateFormat dateFormat = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        dateFormat = new SimpleDateFormat("YYMMdd");
                    }

                    LogUtil.e(Constant.TAG, "Track24: " + track2);

                    Map<String, TLV> tlvValue = getTlvValue();
                    LogUtil.e(Constant.TAG, "GET TLV DATA - : " + getTlvData());
                    LogUtil.e(Constant.TAG, "TLV DATA -----> :" + tlvValue.toString());

                    String smartCardRequestData = "01" + tlvValue.get("9F27").getValue().toString() + "230" + dateFormat.format(currentDate) + tlvValue.get("9F26").getValue().toString() + tlvValue.get("82").getValue().toString() + tlvValue.get("9F36").getValue().toString() + tlvValue.get("9F37").getValue().toString() + tlvValue.get("95").getValue().toString() + tlvValue.get("9C").getValue().toString() + tlvValue.get("5F2A").getValue().toString() + tlvValue.get("9F02").getValue().toString() + tlvValue.get("9F10").getValue().toString();
                    String smartCardAdditionalData = "01" + tlvValue.get("5F34").getValue().toString() + tlvValue.get("9F35").getValue().toString() + tlvValue.get("9F34").getValue().toString() + tlvValue.get("9F09").getValue().toString() + tlvValue.get("84").getValue().toString();

                    LogUtil.e(Constant.TAG, "smartCardData:" + smartCardRequestData);
                    LogUtil.e(Constant.TAG, "smartCardAdditionalData: " + smartCardAdditionalData);
                    Log.d("SMARTCARDDATA", "mockRequestToServer: " + smartCardRequestData);
                    this.runOnUiThread(() -> {
                        ProgressDialog loadingDialog = ProgressDialog.show(ICProcessActivity.this, "Please wait", "Processing transaction ...", true, true);
                        new Handler().postDelayed(() -> {
                            if (loadingDialog.isShowing()) {
                                loadingDialog.dismiss();
                            }
                        }, 5000);
                    });

                    String track2parsed = track2.replace("D", "=");
                    track2parsed = ";" + track2parsed + "?";
                    PurchaseRequest purchaseRequest = new PurchaseRequest(track2parsed, existedPinBlock, smartCardRequestData, smartCardAdditionalData, pinLenght, tlvValue.get("9F02").getValue().toString());
//                    purchaseRequest.toString();
                    LogUtil.e(Constant.TAG, "Purchase request:" + purchaseRequest.toString());

                    // Uncomment and use the appropriate API manager
                    // new EttaApiManager(purchaseRequest, ICProcessActivity.this).makePurchaseRequest();
                    // new SantimPayApiManager(this).doPurchaseTest(purchaseRequest, null);

                    dismissLoadingDialog();
                }

                Thread.sleep(1000);
                // Uncomment if needed
                // importOnlineProcessStatus(0);
            } catch (Exception e) {
                runOnUiThread(this::dismissLoadingDialog);
                e.printStackTrace();
                Log.e("MockRequestToServer", "Exception: " + e.getMessage());
                // Uncomment if needed
                // importOnlineProcessStatus(-1);
            } finally {
                try {
                    makeDummyRequest();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                runOnUiThread(this::dismissLoadingDialog);
            }
        }).start();
    }

    private String getEmvRequestData() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        String[] tagList = {"9F27", "9F1A", "9A", "9F26", "82", "9F36", "9F37", "95", "9C", "5F2A", "9F02", "9F10"};
        byte[] outData = new byte[256];
        int len = mEMVOptV2.getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tagList, outData);

        if (len > 0) {
            byte[] bytes = Arrays.copyOf(outData, len);
            String hexStr = ByteUtil.bytes2HexStr(bytes);
            Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(hexStr);


            for (String t : tagList) {
                sb.append(tlvMap.get(t).getValue());
            }
        }
        Log.d(TAG, "getEmvRequestData: " + sb.toString());
        return sb.toString();
    }
    private String getBillingAdress() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        String[] tagList = {"9F10"};
        byte[] outData = new byte[256];
        int len = mEMVOptV2.getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tagList, outData);

        if (len > 0) {
            byte[] bytes = Arrays.copyOf(outData, len);
            String hexStr = ByteUtil.bytes2HexStr(bytes);
            Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(hexStr);


            for (String t : tagList) {
                sb.append(tlvMap.get(t).getValue());
            }
        }
        Log.d(TAG, "getEMVRequest Data: " + sb.toString());
        return sb.toString();
    }

    private String getEmvAdditionalData() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        sb.append("02");
        String[] tagList = {"5F34", "9F35", "9F34", "9F09", "9F33", "84"};
        byte[] outData = new byte[256];
        int len = mEMVOptV2.getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tagList, outData);

        if (len > 0) {
            byte[] bytes = Arrays.copyOf(outData, len);
            String hexStr = ByteUtil.bytes2HexStr(bytes);
            Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(hexStr);

            for (String t : tagList) {
                sb.append(tlvMap.get(t).getValue());
            }


        }

        return sb.toString();
    }

    private String getEmvSupplementaryData() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        sb.append("00");
        String[] tagList = {"9F6E", "9F7C", "9F66"};
        byte[] outData = new byte[256];
        int len = mEMVOptV2.getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tagList, outData);
        if (len > 0) {
            byte[] bytes = Arrays.copyOf(outData, len);
            String hexStr = ByteUtil.bytes2HexStr(bytes);
            Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(hexStr);
            for (String t : tagList) {
                sb.append(tlvMap.get(t).getValue());
            }
        }
        return sb.toString();
    }

    private String getT2data() throws RemoteException {
        String t2d = "";
        String[] taglist = {"57"};
        byte[] outdata = new byte[256];
        int len = mEMVOptV2.getTlvList(AidlConstants.EMV.TLVOpCode.OP_NORMAL, taglist, outdata);
        if (len > 0) {
            byte[] bytes = Arrays.copyOf(outdata, len);
            String hex = ByteUtil.bytes2HexStr(bytes);
            Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(hex);
            for (String t : taglist) {
                t2d += tlvMap.get(t).getValue();
            }
        }
        t2d = t2d.replace("D", "=");
        t2d = t2d.replace("F", "?");
        return t2d;
    }

    public void makeDummyRequest() throws RemoteException {
        Log.d("Dummy", "Making dummy request");
        DummyCard dummyCard = new DummyCard(this, sunmiPrinterService);  // Pass context and SunmiPrinterService

        // todo: here we need to pass the request data, the suplimentary data and additional data from makereqeust to server function
        String[] taglist = {"9F02"};
        byte[] outdata = new byte[256];
        int len = mEMVOptV2.getTlvList(AidlConstants.EMV.TLVOpCode.OP_NORMAL, taglist, outdata);
        String amount = "";
        if (len > 0) {
            byte[] bytes = Arrays.copyOf(outdata, len);
            String hex = ByteUtil.bytes2HexStr(bytes);
            Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(hex);
            for (String t : taglist) {
                amount += tlvMap.get(t).getValue();
            }
        }

        Bitmap signatureBitmap = getSignatureBitmap();
//        handWriteView.clear();
        // Pass the signature bitmap and the receipt data to DummyCard
        dummyCard.makePaymentRequest(getT2data(), getEmvRequestData(), getEmvAdditionalData(), amount, signatureBitmap, marchentName.getText().toString(),getBillingAdress());

    }

    // Extract signature as bitmap from LinePathView
    private Bitmap getSignatureBitmap() {
        if (handWriteView != null) {
            return   handWriteView.getCacheBitmap();


        }
        return null;
    }
    private void makeRequestToNeaPayServer() {
        new Thread(() -> {
            try {
                showLoadingDialog(R.string.requesting);
                if (AidlConstantsV2.CardType.MAGNETIC.getValue() != mCardType) {
                    StringBuilder carddata = getTlvData();
                    assert carddata != null;
                    String track2 = null;
                    if (!TextUtils.isEmpty(Objects.requireNonNull(tlvMap.get("57")).getValue())) {
                        TLV tlv57 = tlvMap.get("57");
                        track2 = tlv57.getValue().toString();
                    }
                    SimpleDateFormat dateFormat = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        dateFormat = new SimpleDateFormat("YYMMdd");
                    }

                    LogUtil.e(Constant.TAG, "Track24: " + track2);

                    Map<String, TLV> tlvValue = getTlvValue();
                    LogUtil.e(Constant.TAG, "GET TLV DATA - : " + getTlvData());
                    LogUtil.e(Constant.TAG, "TLV DATA -----> :" + tlvValue.toString());

                    String smartCardRequestData = "01" + tlvValue.get("9F27").getValue().toString() + "230" + dateFormat.format(currentDate) + tlvValue.get("9F26").getValue().toString() + tlvValue.get("82").getValue().toString() + tlvValue.get("9F36").getValue().toString() + tlvValue.get("9F37").getValue().toString() + tlvValue.get("95").getValue().toString() + tlvValue.get("9C").getValue().toString() + tlvValue.get("5F2A").getValue().toString() + tlvValue.get("9F02").getValue().toString() + tlvValue.get("9F10").getValue().toString();
                    String smartCardAdditionalData = "01" + tlvValue.get("5F34").getValue().toString() + tlvValue.get("9F35").getValue().toString() + tlvValue.get("9F34").getValue().toString() + tlvValue.get("9F09").getValue().toString() + tlvValue.get("84").getValue().toString();
                    LogUtil.e(Constant.TAG, "smartCardData:" + smartCardRequestData);
                    LogUtil.e(Constant.TAG, "smartCardAdditionalData: " + smartCardAdditionalData);
                    this.runOnUiThread(new Runnable() {
                        public void run() {
                            Log.d("LoadingTAG", "run: running loader");
                            ProgressDialog loadingDialog = ProgressDialog.show(ICProcessActivity.this, "Please wait", "Processing transaction ...", true, false);


                        }
                    });
                    String track2parsed = track2.replace("D", "=");
                    track2parsed = ";" + track2parsed + "?";
                    PurchaseRequest purchaseRequest = new PurchaseRequest(track2parsed, existedPinBlock, smartCardRequestData, smartCardAdditionalData, pinLenght, tlvValue.get("9F02").getValue().toString());

                    LogUtil.e(Constant.TAG, "Purhcase request:" + purchaseRequest.toString());
                    new EttaApiManager(purchaseRequest, this).makePurchaseRequest();
                    // new SantimPayApiManager(this).doPurchaseTest(purchaseRequest,null);


                }
                Thread.sleep(1000);
                // notice  ==  import the online result to SDK and end the process.
                //                importOnlineProcessStatus(0);
            } catch (Exception e) {
                e.printStackTrace();
                //                importOnlineProcessStatus(-1);
            } finally {
                dismissLoadingDialog();
            }
        }).start();
    }
//    public void makeRequestToServer() throws Exception {
//        new Thread(new ClientThread()).start();
//    }

    /**
     * Read we interested tlv data
     *
     * @return
     */
    private StringBuilder getTlvData() {
        try {
            String[] tagList = {"DF02", "5F34", "9F06", "FF30", "FF31", "95", "9B", "9F36", "9F26", "9F27", "DF31", "5A", "57", "5F24", "9F1A", "9F33", "9F35", "9F40", "9F03", "9F10", "9F37", "9C", "9A", "9F02", "5F2A", "82", "9F34", "9F1E", "84", "4F", "9F66", "9F6C", "9F09", "9F41", "9F63", "5F20", "9F12", "50",};
            byte[] outData = new byte[2048];
            Map<String, TLV> map = new TreeMap<>();
            int tlvOpCode;
            if (AidlConstantsV2.CardType.NFC.getValue() == mCardType) {
                if (mAppSelect == 2) {
                    tlvOpCode = AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS;
                } else if (mAppSelect == 1) {
                    tlvOpCode = AidlConstantsV2.EMV.TLVOpCode.OP_PAYWAVE;
                } else {
                    tlvOpCode = AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL;
                }
            } else {
                tlvOpCode = AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL;
            }
            int len = mEMVOptV2.getTlvList(tlvOpCode, tagList, outData);
            if (len > 0) {
                byte[] bytes = Arrays.copyOf(outData, len);
                String hexStr = ByteUtil.bytes2HexStr(bytes);
                Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(hexStr);
                map.putAll(tlvMap);
            }

            // payPassTags
            String[] payPassTags = {"DF811E", "DF812C", "DF8118", "DF8119", "DF811F", "DF8117", "DF8124", "DF8125", "9F6D", "DF811B", "9F53", "DF810C", "9F1D", "DF8130", "DF812D", "DF811C", "DF811D", "9F7C",};
            len = mEMVOptV2.getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS, payPassTags, outData);
            if (len > 0) {
                byte[] bytes = Arrays.copyOf(outData, len);
                String hexStr = ByteUtil.bytes2HexStr(bytes);
                Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(hexStr);
                map.putAll(tlvMap);
            }

            final StringBuilder sb = new StringBuilder();
            Set<String> keySet = map.keySet();
            for (String key : keySet) {
                TLV tlv = map.get(key);
                sb.append(key);
                sb.append(":");
                if (tlv != null) {
                    String value = tlv.getValue();
                    sb.append(value);
                }
                sb.append("\n");
            }

//            runOnUiThread(() -> mTvShowInfo.setText(sb));
            return sb;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<String, TLV> getTlvValue() {
        try {
            String[] tagList = {"DF02", "5F34", "9F06", "FF30", "FF31", "95", "9B", "9F36", "9F26", "9F27", "DF31", "5A", "57", "5F24", "9F1A", "9F33", "9F35", "9F40", "9F03", "9F10", "9F37", "9C", "9A", "9F02", "5F2A", "82", "9F34", "9F1E", "84", "4F", "9F66", "9F6C", "9F09", "9F41", "9F63", "5F20", "9F12", "50",};
            byte[] outData = new byte[2048];
            Map<String, TLV> map = new TreeMap<>();
            int tlvOpCode;
            if (AidlConstantsV2.CardType.NFC.getValue() == mCardType) {
                if (mAppSelect == 2) {
                    tlvOpCode = AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS;
                } else if (mAppSelect == 1) {
                    tlvOpCode = AidlConstantsV2.EMV.TLVOpCode.OP_PAYWAVE;
                } else {
                    tlvOpCode = AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL;
                }
            } else {
                tlvOpCode = AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL;
            }
            int len = mEMVOptV2.getTlvList(tlvOpCode, tagList, outData);
            if (len > 0) {
                byte[] bytes = Arrays.copyOf(outData, len);
                String hexStr = ByteUtil.bytes2HexStr(bytes);
                Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(hexStr);
                map.putAll(tlvMap);
            }

            // payPassTags
            String[] payPassTags = {"DF811E", "DF812C", "DF8118", "DF8119", "DF811F", "DF8117", "DF8124", "DF8125", "9F6D", "DF811B", "9F53", "DF810C", "9F1D", "DF8130", "DF812D", "DF811C", "DF811D", "9F7C",};
            len = mEMVOptV2.getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS, payPassTags, outData);
            if (len > 0) {
                byte[] bytes = Arrays.copyOf(outData, len);
                String hexStr = ByteUtil.bytes2HexStr(bytes);
                Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(hexStr);
                map.putAll(tlvMap);

            }
            return map;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void resetUI() {
        runOnUiThread(() -> {
            mProcessStep = 0;
            mEditAmount.setText("");
            mBtnOperate.setText(R.string.ok);
            dismissLoadingDialog();
            dismissAppSelectDialog();
        });
    }

    private void dismissAppSelectDialog() {
        runOnUiThread(() -> {
            if (mAppSelectDialog != null) {
                try {
                    mAppSelectDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mAppSelectDialog = null;
            }
        });
    }

    /**
     * Create Candidate names
     */
    private String[] getCandidateNames(List<EMVCandidateV2> candiList) {
        if (candiList == null || candiList.size() == 0) return new String[0];
        String[] result = new String[candiList.size()];
        for (int i = 0; i < candiList.size(); i++) {
            EMVCandidateV2 candi = candiList.get(i);
            String name = candi.appPreName;
            name = TextUtils.isEmpty(name) ? candi.appLabel : name;
            name = TextUtils.isEmpty(name) ? candi.appName : name;
            name = TextUtils.isEmpty(name) ? "" : name;
            result[i] = name;
            LogUtil.e(Constant.TAG, "EMVCandidateV2: " + name);
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
        SettingUtil.setBuzzerEnable(true);
    }

    private void cancelCheckCard() {
        try {
            mReadCardOptV2.cardOff(AidlConstantsV2.CardType.NFC.getValue());
            mReadCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryAgain() {
        try {
            runOnUiThread(() -> new AlertDialog.Builder(this).setTitle("Try again").setMessage("Please read the card again").setPositiveButton("OK", (dia, which) -> {
                dia.dismiss();
                checkCard();
            }).show());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check and notify remove card
     */
    private void checkAndRemoveCard() {
        try {
            addStartTimeWithClear("getCardExistStatus()");
            int status = mReadCardOptV2.getCardExistStatus(mCardType);
            addEndTime("getCardExistStatus()");
            showSpendTime();
            if (status < 0) {
                LogUtil.e(Constant.TAG, "getCardExistStatus error, code:" + status);
                dismissLoadingDialog();
                return;
            }
            if (status == AidlConstantsV2.CardExistStatus.CARD_ABSENT) {
                dismissLoadingDialog();
            } else if (status == AidlConstantsV2.CardExistStatus.CARD_PRESENT) {
                showLoadingDialog(R.string.emv_remove_card);
                addStartTimeWithClear("buzzerOnDevice()");
                MyApplication.app.basicOptV2.buzzerOnDevice(1, 2750, 200, 0);
                addEndTime("buzzerOnDevice()");
                showSpendTime();
                mHandler.sendEmptyMessageDelayed(REMOVE_CARD, 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

