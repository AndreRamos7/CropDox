package com.cropdox.utilitarios;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class Testes {

    private static boolean isNetworkConnected;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean isOnline(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
            NetworkRequest.Builder builder = new NetworkRequest.Builder();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback(){
                       @Override
                       public void onAvailable(Network network) {
                           isNetworkConnected = true; // Global Static Variable
                       }
                       @Override
                       public void onLost(Network network) {
                           isNetworkConnected = false; // Global Static Variable
                       }
                   }
    
                );
            }
            isNetworkConnected = false;
        }catch (Exception e){
            isNetworkConnected = false;
        }
        return isNetworkConnected;
    }
}
