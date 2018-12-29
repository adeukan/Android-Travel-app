package vk.travel;

import android.app.Dialog;
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

import java.io.IOException;
import java.util.Objects;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int ERROR_DIALOG_REQUEST = 911;
    GoogleMap mMap;
    Float mPoiLat;
    Float mPoiLon;
    String mPoiName;
    String mPoiCategory;
    private int mPoiPicID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // check the status of Google Play Services
        if (servicesOK()) {
            if (mMap == null) {
                // map initialization
                ((SupportMapFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.map_fragment_detail_activity)))
                        .getMapAsync(this);
            }
        }

        // get values transferred into activity 
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        mPoiLat = extras.getFloat(PoiListAdapter.KEY_LAT);
        mPoiLon = extras.getFloat(PoiListAdapter.KEY_LON);
        mPoiName = extras.getString(PoiListAdapter.KEY_NAME);
        mPoiCategory = extras.getString(PoiListAdapter.KEY_CATEGORY);

        // set the header in action bar
        setTitle(mPoiName);

        // set text views
        TextView tvPoiName = findViewById(R.id.name);
        tvPoiName.setText(mPoiName);

        TextView tvPoiCategory = findViewById(R.id.category);
        String categoryUpperCase = mPoiCategory.substring(0,1).toUpperCase() + mPoiCategory.substring(1);
        tvPoiCategory.setText(categoryUpperCase);

        TextView tvDescription = findViewById(R.id.description);
        tvDescription.setText("No description at the moment\n");

        // find the image in the resource folder whose name corresponds to 'image' property
        mPoiPicID = getResources().getIdentifier(
                mPoiCategory, "drawable", getPackageName());
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
                    tvCategory.setText(marker.getSnippet());
                    ivPicture.setImageResource(mPoiPicID);
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
        try {
            LatLng latLong = new LatLng(mPoiLat, mPoiLon);
            // move the map
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLong, 15);
            mMap.moveCamera(update);

            String categoryUpperCase = mPoiCategory.substring(0,1).toUpperCase() + mPoiCategory.substring(1);

            // marker options
            MarkerOptions options = new MarkerOptions()
                    .title(mPoiName)
                    .snippet(categoryUpperCase)
                    .anchor(.5f, .5f)
                    .position(new LatLng(mPoiLat, mPoiLon));
            // add marker on map
            mMap.addMarker(options);

        }  catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(this.getLocalClassName(), e.getMessage());
        }
    }
}
