package com.cropdox.remote;

public class APIUtils {
    public APIUtils() {
    }
    public static final String API_URL = "http://192.168.0.107:80/";

    public static FileService getFileService(){
        return RetrofitClient.getClient(API_URL).create(FileService.class);
    }
}
