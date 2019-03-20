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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class Model {

    @SuppressLint("StaticFieldLeak")
    private static Model mInstance = null;
    private Context mCtx;
    private RequestQueue mQueue;
    private List<Poi> mPoiList;
    private String mNextPageToken;
    private static final String mURL =
            "https://maps.googleapis.com/maps/api/place/textsearch/json?query=dublin+point+of+interest&type=point_of_interest&region=.ie&language=en&key=AIzaSyCZac9ubfqe9Sy-SZCxfZCeNbiDyhv_2hs";


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
        mPoiList = new ArrayList<>();
    }

    // implemented by ListActivity and MapActivity
    public interface PoisListener {
        void listUpdated(List<Poi> list);
    }

    // getting the POIs
    // this method gets a context indicating the activity with implemented PoisListener interface
    // the method listUpdated() of PoisListener interface is used after the request done
    void requestPOIs(final PoisListener listener) {
        String url = mURL;
        if (mNextPageToken != null) {
            // append token to URL
        }
        // request
        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, url, null,

                        // when response object is received
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    mNextPageToken = response.getString("next_page_token");
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
                                            continue;

                                        // parse the JSON object and create Poi object
                                        String place_id = jsonObject.getString("place_id");
                                        String name = jsonObject.getString("name");
                                        JSONObject geometry = jsonObject.getJSONObject("geometry");
                                        String formatted_address = jsonObject.getString("formatted_address");
                                        JSONArray types = jsonObject.getJSONArray("types");
                                        String icon = jsonObject.getString("icon");

                                        // create and add Poi object to the list
                                        Poi poi = new Poi(place_id, name, geometry, formatted_address, types, photos, icon);
                                        mPoiList.add(poi);
                                    }

                                    // send the result to listUpdated() method of the calling activity
                                    if (listener != null) {
                                        listener.listUpdated(mPoiList);
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
