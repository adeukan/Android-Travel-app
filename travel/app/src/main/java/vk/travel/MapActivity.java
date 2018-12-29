package vk.travel;

import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import vk.travel.model.Poi;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // URL to remote server folder with JSON file
    public static final String SOURCE_FOLDER = "http://192.168.1.4/travel/";
    // full URL to JSON file
    public static final String JSON_URL = SOURCE_FOLDER + "dublin_pois.json";
    // list of POIs (used by the adapter to display in RecyclerView)
    ArrayList<Poi> mPoiList = new ArrayList<>();
    private static final int ERROR_DIALOG_REQUEST = 911;
    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

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

                            // check the status of Google Play Services
                            if (servicesOK()) {
                                if (mMap == null) {
                                    // map initialization
                                    ((SupportMapFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.map_fragment_map_activity)))
                                            .getMapAsync(MapActivity.this);
                                }
                            }
                        }
                        catch (JSONException ex) {
                            Toast.makeText(MapActivity.this, "Parsing Error!", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MapActivity.this, "Web Response Error!", Toast.LENGTH_LONG).show();
                    }
                }); // end of request definition

        // add the request to the RequestQueue
        queue.add(request);


    }

    // check the status of Google Play Services
    public boolean servicesOK() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int isAvailable = googleAPI.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleAPI.isUserResolvableError(isAvailable)) {
            Dialog dialog = googleAPI.getErrorDialog(this, isAvailable, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to mapping service", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    // call back method triggered after the map initialization
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        if (mMap != null) {

            // customize the information window
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                // used to change the info window
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }
                // used to change the info window content
                @Override
                public View getInfoContents(Marker marker) {
                    // info window layout
                    View v = getLayoutInflater().inflate(R.layout.info_win, null);
                    // references to info window elements
                    TextView tvPoiName = v.findViewById(R.id.infoName);
                    TextView tvCategory = v.findViewById(R.id.infoCategory);
                    ImageView ivPicture = v.findViewById(R.id.infoPicture);
                    // add info
                    tvPoiName.setText(marker.getTitle());

                    String categoryUpperCase = marker.getSnippet();
                    categoryUpperCase = categoryUpperCase.substring(0,1).toUpperCase() + categoryUpperCase.substring(1);

                    tvCategory.setText(categoryUpperCase);

                    // find the image in the resource folder whose name corresponds to 'image' property
                    int poiPicID = getResources().getIdentifier(
                            marker.getSnippet(), "drawable", getPackageName());
                    ivPicture.setImageResource(poiPicID);
                    // return View
                    return v;
                }
            });
            // move the map to the POI position
            try {
                geoLocate();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Map not connected!", Toast.LENGTH_SHORT).show();
        }
    }

    // move the map to the POI position
    public void geoLocate() throws IOException {

        Geocoder gc = new Geocoder(this);
        // получить адрес и затем координаты отеля
        List<Address> addressList = gc.getFromLocationName("Dublin", 1);
        Address address = addressList.get(0);
        double lat = address.getLatitude();
        double lng = address.getLongitude();
        LatLng latLong = new LatLng(lat, lng);

        try {

            for (int i = 0; i < mPoiList.size(); i++) {

                // marker options
                MarkerOptions options = new MarkerOptions()
                        .title(mPoiList.get(i).getName())
                        .snippet(mPoiList.get(i).getCategory())
                        .anchor(.5f, .5f)
                        .position(new LatLng(mPoiList.get(i).getLat(), mPoiList.get(i).getLon()));
                // add marker on map
                mMap.addMarker(options);
            }

            // move the map
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLong, 11);
            mMap.moveCamera(update);


        }  catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(this.getLocalClassName(), e.getMessage());
        }
    }
}
