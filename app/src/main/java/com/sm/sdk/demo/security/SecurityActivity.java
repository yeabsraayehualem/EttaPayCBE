package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;

public class SecurityActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);
        initToolbarBringBack(R.string.security);
        initView();
    }

    private void initView() {
        View view = findViewById(R.id.security_save_plaintext_key);
        TextView leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_save_plaintext_key);

        view = findViewById(R.id.security_save_cipher_text_key);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_save_cipher_text_key);

        view = findViewById(R.id.security_inject_plaintext_key);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_inject_plaintext_key);

        view = findViewById(R.id.security_inject_cipher_text_key);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_inject_cipher_text_key);

        view = findViewById(R.id.security_calc_mac);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_calc_mac);

        view = findViewById(R.id.security_data_encrypt);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_data_encrypt);

        view = findViewById(R.id.security_data_decrypt);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_data_decrypt);

        view = findViewById(R.id.security_get_encrypt_sn);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_get_encrypt_sn);

        view = findViewById(R.id.security_DuKpt_save_key);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_DuKpt_save_key);

        view = findViewById(R.id.security_DuKpt_AES_save_key);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_DuKpt_aes_save_key);

        view = findViewById(R.id.security_DuKpt_calc_mac);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_DuKpt_calc_mac);

        view = findViewById(R.id.security_DuKpt_data_encrypt);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_DuKpt_data_encrypt);

        view = findViewById(R.id.security_DuKpt_data_decrypt);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_DuKpt_data_decrypt);

        view = findViewById(R.id.security_DuKpt_current_ksn);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_DuKpt_current_ksn);

        view = findViewById(R.id.security_get_kcv);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_get_kcv);

        view = findViewById(R.id.rsa_test);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_rsa_test);

        view = findViewById(R.id.rsa_recover);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.hsm_rsa_recover);

        view = findViewById(R.id.save_tr31_key);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_save_tr31_key);

        view = findViewById(R.id.delete_key);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_delete_key);

        view = findViewById(R.id.sm2_test);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_sm2_test);

        view = findViewById(R.id.calc_hash);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_calc_hash);

        view = findViewById(R.id.device_cert_test);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_hsm_device_cert_test);

        view = findViewById(R.id.apacs_mac_test);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_apacs_mac);

        view = findViewById(R.id.security_save_cipher_text_key_under_rsa);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_save_ciphertext_key_under_rsa);

        view = findViewById(R.id.security_inject_cipher_text_key_under_rsa);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_inject_ciphertext_key_under_rsa);

        view = findViewById(R.id.security_inject_sym_key);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.security_inejct_symmetric_key);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.security_save_plaintext_key:
                openActivity(SaveKeyPlainTextActivity.class);
                break;
            case R.id.security_save_cipher_text_key:
                openActivity(SaveKeyCipherTextActivity.class);
                break;
            case R.id.security_inject_plaintext_key:
                openActivity(InjectPlainTextKeyActivity.class);
                break;
            case R.id.security_inject_cipher_text_key:
                openActivity(InjectCiphertextKeyActivity.class);
                break;
            case R.id.security_calc_mac:
                openActivity(CalcMacActivity.class);
                break;
            case R.id.security_data_encrypt:
                openActivity(DataEncryptActivity.class);
                break;
            case R.id.security_data_decrypt:
                openActivity(DataDecryptActivity.class);
                break;
            case R.id.security_get_encrypt_sn:
                openActivity(GetEncryptBySerialNumberActivity.class);
                break;
            case R.id.security_DuKpt_save_key:
                openActivity(DuKptSaveKeyActivity.class);
                break;
            case R.id.security_DuKpt_AES_save_key:
                openActivity(DuKptAesSaveKeyActivity.class);
                break;
            case R.id.security_DuKpt_calc_mac:
                openActivity(DuKptCalcMacActivity.class);
                break;
            case R.id.security_DuKpt_data_encrypt:
                openActivity(DuKptDataEncryptActivity.class);
                break;
            case R.id.security_DuKpt_data_decrypt:
                openActivity(DuKptDataDecryptActivity.class);
                break;
            case R.id.security_DuKpt_current_ksn:
                openActivity(DuKptKSNOperateActivity.class);
                break;
            case R.id.security_get_kcv:
                openActivity(GetKeyCheckValueActivity.class);
                break;
            case R.id.rsa_test:
                openActivity(RSATestActivity.class);
                break;
            case R.id.rsa_recover:
                openActivity(RSARecoverActivity.class);
                break;
            case R.id.save_tr31_key:
                openActivity(SaveTR31KeyActivity.class);
                break;
            case R.id.delete_key:
                openActivity(DeleteKeyActivity.class);
                break;
            case R.id.sm2_test:
                openActivity(SM2TestActivity.class);
                break;
            case R.id.calc_hash:
                openActivity(CalcHashActivity.class);
                break;
            case R.id.device_cert_test:
                openActivity(DeviceCertificateTestActivity.class);
                break;
            case R.id.apacs_mac_test:
                openActivity(APACSMacTestActivity.class);
                break;
            case R.id.security_save_cipher_text_key_under_rsa:
                openActivity(SaveKeyCipherTextUnderRsaActivity.class);
                break;
            case R.id.security_inject_cipher_text_key_under_rsa:
                openActivity(InjectCiphertextKeyUnderRsaActivity.class);
                break;
            case R.id.security_inject_sym_key:
                openActivity(InjectSymKeyActivity.class);
                break;
        }
    }


}
