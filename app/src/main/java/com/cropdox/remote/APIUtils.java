package com.cropdox.remote;

public class APIUtils {
    public APIUtils() {
    }
    public static final String API_URL = "https://cropdox.com/";

    public static FileService getFileService(){
        return RetrofitClient.getClient(API_URL).create(FileService.class);
    }
}
