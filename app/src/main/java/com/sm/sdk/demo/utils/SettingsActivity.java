package com.sm.sdk.demo.utils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sm.sdk.demo.R;

public class SettingsActivity extends AppCompatActivity {

    private EditText editTextMerchantName, editTextTerminalId, editTextRetailerId;
    private Button buttonSave;

    // Key names for SharedPreferences
    public static final String PREFS_NAME = "MerchantSettings";
    public static final String KEY_MERCHANT_NAME = "merchant_name";
    public static final String KEY_TERMINAL_ID = "terminal_id";
    public static final String KEY_RETAILER_ID = "retailer_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_view);

        // Link XML components to Java
        editTextMerchantName = findViewById(R.id.editTextMerchantName);
        editTextTerminalId = findViewById(R.id.editTextTerminalId);
        editTextRetailerId = findViewById(R.id.editTextRetailerId);
        buttonSave = findViewById(R.id.buttonSave);

        // Load saved settings (if any)
        loadSettings();

        // Save button click listener
        buttonSave.setOnClickListener(view -> {
            saveSettings();
        });
    }

    // Load the stored settings from SharedPreferences
    private void loadSettings() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String merchantName = preferences.getString(KEY_MERCHANT_NAME, ""); // Default empty
        String terminalId = preferences.getString(KEY_TERMINAL_ID, ""); // Default empty

        // Set the values to the EditText fields
        editTextMerchantName.setText(merchantName);
        editTextTerminalId.setText(terminalId);
    }

    // Save the settings to SharedPreferences
    private void saveSettings() {
        String merchantName = editTextMerchantName.getText().toString();
        String terminalId = editTextTerminalId.getText().toString();
        String retailerId = editTextRetailerId.getText().toString();
        if (merchantName.isEmpty() || terminalId.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the data in SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_MERCHANT_NAME, merchantName);
        editor.putString(KEY_TERMINAL_ID, terminalId);
        editor.putString(KEY_RETAILER_ID, retailerId);
        editor.apply();

        Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();
    }

    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String merchantName = sharedPreferences.getString(KEY_MERCHANT_NAME, "Default Merchant");
        String terminalID = sharedPreferences.getString(KEY_TERMINAL_ID, "000000");
        String retailerId = sharedPreferences.getString(KEY_RETAILER_ID,"00000000");

        // Set the loaded data into the EditText fields
        editTextMerchantName.setText(merchantName);
        editTextTerminalId.setText(terminalID);
        editTextRetailerId.setText(retailerId);
    }

}
