package com.tomykrisgreen.airbasetabbed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionReceiver extends BroadcastReceiver {
    // Initialize listener
    public static ReceiverListener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Initialize connectivity manager
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Initialize network info
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        // Check condition
        if (listener != null){
            // When connectivity receiver listener not null, get connection status
            boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            // Call listener
            listener.onNetworkChange(isConnected);
        }
    }

    public interface ReceiverListener {
        // Create method
        void onNetworkChange(boolean isConnected);
    }
}
