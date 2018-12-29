package vk.travel.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;

import java.io.IOException;

import vk.travel.model.Poi;
import vk.travel.utils.HttpHelper;

// this class (service) is well suited to longer running tasks
// this class must be registered as a service in the manifest
// downloading the data in the background won't be interrupted by a configuration change
public class MyIntentService extends IntentService {

    // identifier for the Intent object of local broadcast
    public static final String BROADCAST = "Broadcast";
    // identifier for the data inside the Intent object of local broadcast
    public static final String BROADCAST_CONTENT = "BroadcastContent";
    public MyIntentService() {
        super("MyIntentService");
    }

    // this function is triggered when the service is called ---------------------------------------
    @Override
    protected void onHandleIntent(Intent intent) {

        // get URL String from Intent object
        Uri uri = intent.getData();
        // use HttpHelper to make a request to specified URL, and return response string
        String response;
        try {
            response = HttpHelper.downloadUrl(uri.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // array of pois obtained by parsing JSON
        Poi[] pois = null;
        // Gson library is used to convert the response string the to array of Poi objects
        Gson gson = new Gson();
        pois = gson.fromJson(response, Poi[].class);


        // IntentService doesn't have access to the main thread, so the broadcasting is used to send data back
        // create Intent object with specified key
        Intent broadcastIntent = new Intent(BROADCAST);
        // put the response array to the Intent object
        broadcastIntent.putExtra(BROADCAST_CONTENT, pois);

        // instantiate the broadcast manager
        LocalBroadcastManager manager =
                LocalBroadcastManager.getInstance(getApplicationContext());
        // broadcast the response array
        manager.sendBroadcast(broadcastIntent);
    }
}
