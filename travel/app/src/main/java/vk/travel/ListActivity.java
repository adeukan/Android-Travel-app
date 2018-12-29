package vk.travel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import vk.travel.model.Poi;

public class ListActivity extends AppCompatActivity {

    // URL to remote server folder with JSON file
    public static final String SOURCE_FOLDER = "http://192.168.1.4/travel/";
    // full URL to JSON file
    public static final String JSON_URL = SOURCE_FOLDER + "dublin_pois.json";
    // list of POIs (used by the adapter to display in RecyclerView)
    List<Poi> mPoiList = new ArrayList<>();


    // ---------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // create request object that requires an array response from the provided URL
        JsonArrayRequest request = new JsonArrayRequest(JSON_URL,
                // when response array is received
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        try {
                            // for each JSON object in response
                            for (int i = 0; i != jsonArray.length(); i++) {
                                // get next JSON object
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                // parse the JSON object and create Poi object
                                String id = jsonObject.getString("id");
                                float lat = BigDecimal.valueOf(jsonObject.getDouble("lat")).floatValue();
                                float lon = BigDecimal.valueOf(jsonObject.getDouble("lon")).floatValue();
                                String name;
                                if(jsonObject.has("name")) {
                                    name = jsonObject.getString("name");
                                } else {
                                    name = jsonObject.getString("tourism");
                                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                }
                                String category = jsonObject.getString("tourism");
                                Poi poi = new Poi(id, lat, lon, name, category);
                                // add Poi object to the list
                                mPoiList.add(poi);
                            }
                            // display the list of POIs on the screen
                            displayPoiList(null);

                        }
                        catch (JSONException ex) {
                            Toast.makeText(ListActivity.this, "Parsing Error!", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ListActivity.this, "Web Response Error!", Toast.LENGTH_LONG).show();
                    }
                }); // end of request definition

        // add the request to the RequestQueue
        queue.add(request);
    }

    // display the list of POIs in RecyclerView ----------------------------------------------------
    protected void displayPoiList(String filter) {

        // create a copy of POI list
        List<Poi> filteredList = new ArrayList<>(mPoiList);

//        // apply the filtering if necessary
//        if(filter != null) {
//
//            // create a sublist for deletion
//            List<Poi> sublist = new ArrayList<>();
//
//            // add filtered Poi objects to the sublist
//            for (Poi p : filteredList ) {
//                if (!p.getStatus().equals(filter))
//                    sublist.add(p);
//            }
//            // remove filtered objects
//            filteredList.removeAll(sublist);
//        }
//
//        // sort the list of POIs by the name
//        Collections.sort(filteredList, new Comparator<Poi>() {
//            @Override
//            public int compare(Poi p1, Poi p2) {
//                return p1.getName().compareTo(p2.getName());
//            }
//        });

        // create reference to RecyclerView in MainActivity
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        // use adapter to display the list of POIs in RecyclerView
        recyclerView.setAdapter(new PoiListAdapter(this, filteredList));
    }
}
