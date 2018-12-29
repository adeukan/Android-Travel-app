package vk.travel.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class NetworkChecker {

    // get and return the network connection status
    public static boolean hasNetworkAccess(Context context) {

        // get the connectivity manager from the current context
        ConnectivityManager conManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // use the manager to get the network status
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        // return TRUE if the network status is 'Connected' or 'Connecting', otherwise return FALSE
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}


