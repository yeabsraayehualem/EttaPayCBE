package com.sm.sdk.demo;

import com.sm.sdk.demo.utils.KeyDownloadResponse;
import com.sm.sdk.demo.utils.LoginRequest;
import com.sm.sdk.demo.utils.LoginResponse;
import com.sm.sdk.demo.utils.PurchaseRequest;
import com.sm.sdk.demo.utils.PurchaseResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface SantimPayInterface {
    @POST("/terminals/log")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("/terminals/key-download")
    Call<KeyDownloadResponse> downloadKey(@Header("Authorization") String accessToken);


    @POST("terminals/purchase")
    Call<PurchaseResponse> purchase (@Body PurchaseRequest purchaseRequest) ;
}