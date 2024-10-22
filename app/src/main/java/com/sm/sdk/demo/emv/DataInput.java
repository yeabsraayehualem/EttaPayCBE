package com.sm.sdk.demo.emv;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;

public class DataInput extends BaseAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_data_input);

        EditText track2 = findViewById(R.id.track2);
        EditText pinBlock = findViewById(R.id.pinBlock);
        EditText smartCardData = findViewById(R.id.smartCardData);
        EditText smartCardAdditionalData = findViewById(R.id.smartCardAdditionalData);
        EditText pinLength = findViewById(R.id.pinLength);
        EditText amount = findViewById(R.id.amount);
        Button submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String track2Data = track2.getText().toString().trim();
                String pinBlockData = pinBlock.getText().toString().trim();
                String smartCardDataStr = smartCardData.getText().toString().trim();
                String smartCardAdditionalDataStr = smartCardAdditionalData.getText().toString().trim();
                String pinLengthData = pinLength.getText().toString().trim();
                String amountData = amount.getText().toString().trim();

                // Log the collected data for debugging
                Log.d("DataInput", "Track2: " + track2Data);
                Log.d("DataInput", "PIN Block: " + pinBlockData);
                Log.d("DataInput", "Smart Card Data: " + smartCardDataStr);
                Log.d("DataInput", "Smart Card Additional Data: " + smartCardAdditionalDataStr);
                Log.d("DataInput", "PIN Length: " + pinLengthData);
                Log.d("DataInput", "Amount: " + amountData);

                // Optionally, you can display the collected data in a Toast for quick verification
                Toast.makeText(DataInput.this, "Data Collected", Toast.LENGTH_SHORT).show();

                // Do something with the collected data, e.g., send to server or another activity
            }
        });
    }
}