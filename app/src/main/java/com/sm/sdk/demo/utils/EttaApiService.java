package com.sm.sdk.demo.utils;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface EttaApiService {
    @Headers("Content-Type: application/json")
    @POST("/")
    Call<EttaApiResponse> sendTransaction(@Body PurchaseRequest transactionRequest);
}

