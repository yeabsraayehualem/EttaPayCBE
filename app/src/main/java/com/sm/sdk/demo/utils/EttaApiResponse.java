package com.sm.sdk.demo.utils;


//{"type":"success","code":"200","info":"Success, no issue","data":000}
public class EttaApiResponse {
    private String type;
    private String code;
    private String info;
    private  String data;

    public EttaApiResponse(String type,String code,String info,String data ){
        this.type = type;
        this.code = code;
        this.info = info;
        this.data = data;
    }
}
