package vk.travel;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, Model.PoisListener {

    List<Poi> mPoiList = new ArrayList<>();
    private static final int ERROR_DIALOG_REQUEST = 911;
    GoogleMap mMap;
    boolean second = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // request POIs and save them in the model
        Model model = Model.getInstance(this);
        model.requestPOIs(this);
    }

    // triggered after the POIs request is finished (belongs to Model.PoisListener interface)
    @Override
    public void listUpdated(List<Poi> list) {
        mPoiList = list;

        // check the status of Google Play Services
        if (servicesOK()) {
            if (mMap == null) {
                // map initialization
                ((SupportMapFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.map_fragment_map_activity)))
                        .getMapAsync(MapActivity.this);
            }
            else {
                try {
                    geoLocate();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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

                // used to change the info window (we don't use)
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
                    ImageView ivIcon = v.findViewById(R.id.infoIcon);

                    // add title
                    tvPoiName.setText(marker.getTitle());
                    // add icon
                    Picasso.get().load(marker.getSnippet()).into(ivIcon);
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
        // get address and coordinates
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
                        .snippet(mPoiList.get(i).getIcon())
                        .anchor(.5f, .5f)
                        .position(new LatLng( mPoiList.get(i).getGeometry().getJSONObject("location").getDouble("lat"),
                                              mPoiList.get(i).getGeometry().getJSONObject("location").getDouble("lng")));
                // add marker on map
                mMap.addMarker(options);
            }

            // move the map
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLong, 11);
            mMap.moveCamera(update);


            if (!second) {
                Model model = Model.getInstance(this);
                model.requestPOIs(this);
                second = true;
            }

        }  catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(this.getLocalClassName(), e.getMessage());
        }
    }
}
