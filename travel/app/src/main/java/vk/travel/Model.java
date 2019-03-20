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
    private static Model mInstance = null;                                                          // Model instance

    private static final String START_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static final String API_KEY = "&key=AIzaSyCZac9ubfqe9Sy-SZCxfZCeNbiDyhv_2hs";
    private static final int EQUATOR = 40075000;

    List<Poi> mPoiList;                                                                             // result list of POIs
    LatLng mLatLng;
    private List<Poi> mPoiListPortion;                                                              // next portion of POIs
    private RequestQueue mRequestQueue;                                                             // request queue
    private String mNextPageToken = null;
    private float mZoomLevel;
    private Context mCtx;


    private Model(Context ctx) {                                                                    // Model constructor
        mCtx = ctx;
        mRequestQueue = Volley.newRequestQueue(ctx);
        mPoiListPortion = new ArrayList<>();
        mPoiList = new ArrayList<>();
    }

    static synchronized Model getInstance(Context ctx) {                                            // getting Model instance
        if (mInstance == null) {
            mInstance = new Model(ctx);
        }
        return mInstance;
    }

    // REQUEST POIS --------------------------------------------------------------------------------
    void requestPOIs(final PoisListener listenerCtx, LatLng latLng) {

        mLatLng = latLng;                                                                           // used in fragment to get last request location

        // updatePois() of PoisListener interface is triggered after request is finished
        // 'listener_ctx' is a calling fragment with PoisListener interface implemented

        double lat = latLng.latitude;                                                               // request coordinates
        double lng = latLng.longitude;

        double numTiles = Math.pow(2, mZoomLevel);                                                  // calculate radius based on zoom level
        double metersPerTile = Math.cos(Math.toRadians(latLng.latitude)) * EQUATOR / numTiles;
        int diameter = (int) Math.round(metersPerTile);
        int radius = diameter;                                                                      // double the search radius to get more results
        if (radius > 50000)
            radius = 50000;                                                                         // max available radius

        String query = "location=" + lat + "," + lng                                                // part of request url
                + "&radius=" + radius
                + "&type=point_of_interest";

        String url = "";                                                                            // generate request url
        if (mNextPageToken == null)
            url = START_URL.concat(query).concat(API_KEY);
        else
            url = START_URL.concat("pagetoken=").concat(mNextPageToken).concat(API_KEY);

        JsonObjectRequest request = new JsonObjectRequest                                           // create GET request
                (Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {                                       // response listener
                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    boolean nextPage;
                                    if (response.has("next_page_token")) {
                                        mNextPageToken = response.getString("next_page_token");// save NextPageToken for next request
                                        nextPage = true;                                             // track reaching the bottom of the list
                                    } else {
                                        mNextPageToken = null;
                                        nextPage = false;                                           // stop tracking reaching the bottom of the list
                                    }

                                    JSONArray results = response.getJSONArray("results");     // get results from the response object

                                    for (int i = 0; i != results.length(); i++) {                   // for each JSON object in results
                                        JSONObject jsonObject = results.getJSONObject(i);           // get next JSON object

                                        JSONArray photos;
                                        if (jsonObject.has("photos"))                         // get the photo reference or skip this object
                                            photos = jsonObject.getJSONArray("photos");
                                        else
                                            continue;

                                        String id = jsonObject.getString("place_id");         // get the ID of the next place found
                                        if (!findPoiByPlaceId(id)) {                                // skip place if already in the list
                                            String name = jsonObject.getString("name");       // parse JSON object
                                            JSONObject geometry = jsonObject.getJSONObject("geometry");
                                            // String address = jsonObject.getString("formatted_address");
                                            JSONArray types = jsonObject.getJSONArray("types");
                                            String icon = jsonObject.getString("icon");

                                            Poi poi = new Poi(id, name, geometry, types, photos, icon);
                                            mPoiListPortion.add(poi);                               // save POI in the list
                                        }
                                    }                                                               // end of parsing

                                    try {
                                        listenerCtx.updatePois(mPoiListPortion, nextPage);          // call updatePois() of calling fragment
                                        mPoiListPortion.clear();                                    // clear list before next use
                                    } catch (IOException e) {
                                        e.printStackTrace();
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

        mRequestQueue.add(request);                                                                 // add the request to queue
    }

    // FIND DUPLICATES -----------------------------------------------------------------------------
    private boolean findPoiByPlaceId(String placeId) {
        boolean found = false;
        for (Poi poi : mPoiListPortion) {
            if (poi.getPlace_id().equals(placeId)) {
                found = true;
                break;
            }
        }
        return found;
    }

    float getmZoomLevel() {
        return mZoomLevel;
    }
    void setmZoomLevel(float mZoomLevel) {
        this.mZoomLevel = mZoomLevel;
    }

    public interface PoisListener {                                                                 // implemented by FragmentList and FragmentMap
        void updatePois(List<Poi> list, boolean loading) throws IOException;
    }
}