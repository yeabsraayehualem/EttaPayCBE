package com.sm.sdk.demo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.sm.sdk.demo.emv.ICProcessActivity;
import com.sm.sdk.demo.other.GB2312Activity;
import com.sm.sdk.demo.utils.KeyDownloadCallback;
import com.sm.sdk.demo.utils.KeyDownloadResponse;
import com.sm.sdk.demo.utils.LoginCallback;
import com.sm.sdk.demo.utils.LoginRequest;
import com.sm.sdk.demo.utils.LoginResponse;
import com.sm.sdk.demo.utils.PurchaseCallback;
import com.sm.sdk.demo.utils.PurchaseRequest;
import com.sm.sdk.demo.utils.PurchaseResponse;
import com.sm.sdk.demo.utils.SystemDateTime;
import com.sunmi.peripheral.printer.SunmiPrinterService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SantimPayApiManager {

    private Context context;

    private static final String BASE_URL = "http://172.20.10.7:4000/";
    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public SantimPayApiManager(Context context) {
        this.context = context;
    }

    public void doApiLogin(LoginRequest loginRequest, LoginCallback callback) {
        SantimPayInterface apiService = retrofit.create(SantimPayInterface.class);

        // Show loading dialog
        ProgressDialog loadingDialog = ProgressDialog.show(context, "Please wait", "Logging in...", true, false);

        Call<LoginResponse> call = apiService.login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                loadingDialog.dismiss();
                Log.d("api-LOG", response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    saveAccessToken(response.body().getTerminal().getAccess_token());
                    if (callback != null) {
                        callback.onResult(true);
                    }
                } else {
                    if (callback != null) {
                        callback.onResult(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loadingDialog.dismiss();
                if (callback != null) {
                    callback.onResult(false);
                }
            }
        });
    }

    public void doPurchaseTest(PurchaseRequest purchaseRequest , PurchaseCallback callback) {
        Log.d("Purchase request", "purchase request Message:  "+purchaseRequest.toString());
        SantimPayInterface apiService = retrofit.create(SantimPayInterface.class);

        // Show loading dialog

//        ProgressDialog loadingDialog = ProgressDialog.show(context, "Please wait", "Processing transaction ...", true, false);

        Call<PurchaseResponse> call = apiService.purchase(purchaseRequest);
        call.enqueue(new Callback<PurchaseResponse>() {

            @Override
            public void onResponse(Call<PurchaseResponse> call, Response<PurchaseResponse> response) {
//                callback.onResult(true);
                PurchaseResponse purchaseResponse = response.body();
                ProgressDialog loadingDialog = ProgressDialog.show(context, "Please wait", "Success transaction"+ purchaseResponse.toString(), true, false);
                SunmiPrinterService sunmiPrinterService = MyApplication.app.sunmiPrinterService;
                try {
                    sunmiPrinterService.printTextWithFont(purchaseResponse.toString()+ "\n", "", 30, null);
                    sunmiPrinterService.lineWrap(6, null);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }


                Log.d("SantimResponse ", response.toString());

            }

            @Override
            public void onFailure(Call<PurchaseResponse> call, Throwable t) {
//                callback.onResult(false);
                Log.d("SantimResponse ", t.toString() + call.toString());
            }
        });
    }

    public void doKeyDownload(String bearerToken, KeyDownloadCallback callback) {
        SantimPayInterface apiService = retrofit.create(SantimPayInterface.class);

        ProgressDialog loadingDialog = ProgressDialog.show(context, "Please wait", "Fetching data...", true, false);

        Call<KeyDownloadResponse> call = apiService.downloadKey("Bearer " + bearerToken);
        call.enqueue(new Callback<KeyDownloadResponse>() {
            @Override
            public void onResponse(Call<KeyDownloadResponse> call, Response<KeyDownloadResponse> response) {
                try {
                    loadingDialog.dismiss();
                    Log.d("api-LOG", response.toString());
                    if (response.isSuccessful() && response.body() != null) {
                            callback.onResult(true, response.body());
                    } else {
                        callback.onResult(false, null);
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public void onFailure(Call<KeyDownloadResponse> call, Throwable t) {
                loadingDialog.dismiss();

                // Call the callback with success false and null data
                try {
                    callback.onResult(false, null);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public boolean doApiLoginSync(LoginRequest loginRequest) {
        Log.d("doApiLoginSync", "IN login");
        SantimPayInterface apiService = retrofit.create(SantimPayInterface.class);

        try {
            // Synchronous network call
            Response<LoginResponse> response = apiService.login(loginRequest).execute();
            Log.d("doApiLoginSync response", response.toString());
            if (response.isSuccessful() && response.body() != null) {
                saveAccessToken(response.body().getTerminal().getAccess_token());
                return true;
            }
        } catch (Exception e) {
            Log.e("SantimPayApiManager", "Login failed", e);
        }
        return false;
    }

    public KeyDownloadResponse doKeyDownloadSync(String bearerToken) {
        SantimPayInterface apiService = retrofit.create(SantimPayInterface.class);

        try {
            // Synchronous network call
            Response<KeyDownloadResponse> response = apiService.downloadKey("Bearer " + bearerToken).execute();

            if (response.isSuccessful() && response.body() != null) {
                Log.d("api-LOG", response.toString());
                return response.body();
            }
        } catch (Exception e) {
            Log.e("SantimPayApiManager", "Key download failed", e);
        }
        return null;
    }

    private void saveAccessToken(String accessToken) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("accessToken", accessToken);
        editor.apply();
    }

    public String getAccessToken(Context context) {
        try{
            SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            // The second parameter is the default value to return if the key doesn't exist.
            return sharedPreferences.getString("accessToken", "");
        }
        catch (Exception ex){
            return ex.getMessage();
        }
    }
}
