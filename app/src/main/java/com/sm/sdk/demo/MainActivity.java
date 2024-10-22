package com.sm.sdk.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.sm.sdk.demo.basic.BasicActivity;
import com.sm.sdk.demo.card.CardActivity;
import com.sm.sdk.demo.emv.DataInput;
import com.sm.sdk.demo.emv.EMVActivity;
import com.sm.sdk.demo.emv.ICProcessActivity;
import com.sm.sdk.demo.etc.ETCActivity;
import com.sm.sdk.demo.other.OtherActivity;
import com.sm.sdk.demo.pin.PinPadActivity;
import com.sm.sdk.demo.print.PrintActivity;
import com.sm.sdk.demo.scan.ScanActivity;
import com.sm.sdk.demo.security.SecurityActivity;
import com.sm.sdk.demo.tax.TaxTestActivity;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class MainActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                InputStream inputStream = getAssets().open("cacert.pem");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder pemBuilder = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    pemBuilder.append(line).append("\n");
                }
                byte[] certBytes = pemBuilder.toString().getBytes();
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));
                KeyStore keyStore = KeyStore.getInstance("BKS");
                keyStore.load(null);
                keyStore.setCertificateEntry("dashencert", certificate);
                File keystoreFile = new File(getFilesDir(), "keystore.bks");
                FileOutputStream fos = new FileOutputStream(keystoreFile);
                keyStore.store(fos, "etta8707".toCharArray());
                fos.close();
                Log.d(TAG, "Certificate stored in Keystore successfully.");

            } catch (Exception e) {
                Log.d(TAG, "onCreate: Certificate store failed"+e.toString());
                e.printStackTrace();
            }

        }
        initView();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("SunmiSDKTestDemo");

        findViewById(R.id.card_view_basic).setOnClickListener(this);
        findViewById(R.id.card_view_card).setOnClickListener(this);
        findViewById(R.id.card_view_pin_pad).setOnClickListener(this);
        findViewById(R.id.card_view_security).setOnClickListener(this);
        findViewById(R.id.card_view_emv).setOnClickListener(this);
        findViewById(R.id.card_view_scan).setOnClickListener(this);
        findViewById(R.id.card_view_print).setOnClickListener(this);
        findViewById(R.id.card_view_other).setOnClickListener(this);
        findViewById(R.id.card_view_etc).setOnClickListener(this);
        findViewById(R.id.card_view_comm).setOnClickListener(this);
        findViewById(R.id.card_view_tax).setOnClickListener(this);
        findViewById(R.id.card_data_input).setOnClickListener(this);


        openActivity(ICProcessActivity.class);

    }

    @Override
    public void onClick(View v) {
        if (!MyApplication.app.isConnectPaySDK()) {
            MyApplication.app.bindPaySDKService();
            showToast(R.string.connect_loading);
            return;
        }
        final int id = v.getId();
        switch (id) {
            case R.id.card_data_input:
                Log.d(TAG, "onClick: Button");
                openActivity(DataInput.class);
                break;
            case R.id.card_view_basic:
                openActivity(BasicActivity.class);
                break;
            case R.id.card_view_card:
                openActivity(CardActivity.class);
                break;
            case R.id.card_view_pin_pad:
                openActivity(PinPadActivity.class);
                break;
            case R.id.card_view_security:
                openActivity(SecurityActivity.class);
                break;
            case R.id.card_view_emv:
                openActivity(EMVActivity.class);
                break;
            case R.id.card_view_scan:
                openActivity(ScanActivity.class);
                break;
            case R.id.card_view_print:
                openActivity(PrintActivity.class);
                break;
            case R.id.card_view_etc:
                openActivity(ETCActivity.class);
                break;
            case R.id.card_view_other:
                openActivity(OtherActivity.class);
                break;
            case R.id.card_view_tax:
                openActivity(TaxTestActivity.class);
                break;

        }
    }

    public static void reStart(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


}
