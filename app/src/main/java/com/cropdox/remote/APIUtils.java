package com.cropdox.remote;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class APIUtils {
    public APIUtils() {
    }
    //public static final String API_URL = "http://192.168.0.107:80/";
    public static final String API_URL = "https://cropdox.com/";
    public static FileService getFileService(){
        return RetrofitClient.getClient(API_URL).create(FileService.class);
    }

    public static String md5(String senha) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        BigInteger hash = new BigInteger(1, md.digest(senha.getBytes()));
        return hash.toString(16);
    }
}
