package com.sm.sdk.demo.utils;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.utils.PurchaseCallback;
import com.sm.sdk.demo.utils.PurchaseRequest;
import com.sm.sdk.demo.utils.PurchaseResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class EttaApiManager  {
    //private static String Neaurl = "http://196.189.126.150:9020/";
    private static String Neaurl = "http://192.168.0.48:12345/";
    private PurchaseRequest purchaseRequest;
    private Context context;
    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Neaurl)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public EttaApiManager(PurchaseRequest purchaseRequest,Context context){
        this.context = context;
        this.purchaseRequest = purchaseRequest;
    }
    public void makePurchaseRequest(){

        EttaApiService apiService =retrofit.create(EttaApiService.class);
        Call<EttaApiResponse> call = apiService.sendTransaction(this.purchaseRequest);
        Boolean paymentStatus = false;

        call.enqueue(new Callback<EttaApiResponse>() {
            @Override
            public void onResponse(Call<EttaApiResponse> call, Response<EttaApiResponse> response) {
                Log.d("OnResponse", response.body().toString());
                EttaApiResponse purchaseResponse  = response.body();
            }

            @Override
            public void onFailure(Call<EttaApiResponse> call, Throwable t) {
                try{
                    if (context instanceof BaseAppCompatActivity) {
                        ((BaseAppCompatActivity) context).runOnUiThread(() -> {
                           Toast.makeText(context, "Purchase successful", Toast.LENGTH_SHORT).show();
                            ((BaseAppCompatActivity) context).loaderDissmissAbstraction();
                        });
                    }
                }catch (Exception e){
                    Log.d("Exception", "onFailure: ");
                }
                Log.d("Failure", "Failure: " + t.getMessage());



            }
        });

    }
}
