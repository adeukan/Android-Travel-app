package vk.travel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Model {
    @SuppressLint("StaticFieldLeak")
    private static Model mInstance = null;          // Model instance
    private Context mCtx;
    private RequestQueue mQueue;                    // request queue
    List<Poi> mModelPoiList;                        // result list of POIs

    private float mZoomLevel;
    private static final int EQUATOR = 40075000;

    // query URL parts
    private String mNextPageToken = null;
    private static final String START_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
    private static final String API_KEY = "&key=AIzaSyCZac9ubfqe9Sy-SZCxfZCeNbiDyhv_2hs";
    String mQuery;

    // getting the Model instance
    static synchronized Model getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new Model(ctx);
        }
        return mInstance;
    }

    // constructor
    private Model(Context ctx) {
        mCtx = ctx;
        mQueue = Volley.newRequestQueue(ctx);
        mModelPoiList = new ArrayList<>();
    }

    public float getmZoomLevel() {
        return mZoomLevel;
    }
    public void setmZoomLevel(float mZoomLevel) {
        this.mZoomLevel = mZoomLevel;
    }

    // implemented by ListActivity and MapActivity
    public interface PoisListener {
        void updatePois(List<Poi> list, boolean loading) throws IOException;
    }

    private boolean findPoiByPlaceId(String placeId) {
        boolean found = false;
        for (Poi poi: mModelPoiList) {
            if (poi.getPlace_id().equals(placeId)) {
                found = true;
                break;
            }
        }
        return found;
    }

    // getting the POIs
    // this method gets a context indicating the activity with implemented PoisListener interface
    // the method updatePois() of PoisListener interface is used after the request done
    void requestPOIs(final PoisListener listener, LatLng latLng) {

        // calculate the radius based on new zoom level
        double numTiles = Math.pow(2, mZoomLevel);
        double metersPerTile = Math.cos(Math.toRadians(latLng.latitude)) * EQUATOR / numTiles;
        double radius = metersPerTile/1.5;

        if(radius > 50000) radius = 50000;      // max available radius

        double lat = latLng.latitude;
        double lng = latLng.longitude;

        mQuery = "location=" + lat + "," + lng + "&radius=" + radius + "&type=point_of_interest&query=point+of+interest";

        // determine the type of request and generate request URL
        String url = "";
        if (mNextPageToken == null)
            url = START_URL.concat(mQuery).concat(API_KEY);
        else if (mNextPageToken != null)
            url = START_URL.concat("pagetoken=").concat(mNextPageToken).concat(API_KEY);

        // request
        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, url, null,

                        // when response object is received
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    // save the pagetoken value for next time
                                    boolean loading;
                                    if (response.has("next_page_token")) {

                                        mNextPageToken = response.getString("next_page_token");
                                        // track the reaching the bottom in ListActivity
                                        loading = true;
                                    }
                                    else {
                                        mNextPageToken = null;
                                        // stop tracking the reaching the bottom in ListActivity
                                        loading = false;
                                    }

                                    // get results from the response object
                                    JSONArray results = response.getJSONArray("results");

                                    // for each JSON object in results
                                    for (int i = 0; i != results.length(); i++) {
                                        // get next JSON object
                                        JSONObject jsonObject = results.getJSONObject(i);

                                        // get the photo reference or skip this object
                                        JSONArray photos;
                                        if (jsonObject.has("photos"))
                                            photos = jsonObject.getJSONArray("photos");
                                        else
                                            continue;  // skip objects without photo

                                        // parse the JSON object and create Poi object
                                        String place_id = jsonObject.getString("place_id");
                                        if (!findPoiByPlaceId(place_id)) {
                                            String name = jsonObject.getString("name");
                                            JSONObject geometry = jsonObject.getJSONObject("geometry");
                                            String formatted_address = jsonObject.getString("formatted_address");
                                            JSONArray types = jsonObject.getJSONArray("types");
                                            String icon = jsonObject.getString("icon");

                                            // create and add Poi object to the list
                                            Poi poi = new Poi(place_id, name, geometry, formatted_address, types, photos, icon);
                                            mModelPoiList.add(poi);
                                        }
                                    }

                                    // send the result to updatePois() method of the calling activity
                                    if (listener != null) {
                                        try {
                                            listener.updatePois(mModelPoiList, loading);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } catch (JSONException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(mCtx, "Web Response Error!", Toast.LENGTH_LONG).show();
                            }
                        });

        // add the request to the RequestQueue
        mQueue.add(request);
    }
}