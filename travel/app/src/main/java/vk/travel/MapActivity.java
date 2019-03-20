package vk.travel;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        Model.PoisListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnCameraMoveStartedListener {

    private static final int ERROR_DIALOG_REQUEST = 911;
    GoogleMap mMap;
    LatLng mLatLng;
    LatLng mPrevLatLng;
    Model mModel;
    List<Poi> mPoiList = new ArrayList<>();
    boolean mMoving = false;
    boolean mFirstRun = true;
    FusedLocationProviderClient mFusedLocationClient;           // client to get location
    OnSuccessListener<Location> mLocationListener;


    // ON CREATE -----------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // инициализация клиента для последующего получения местонахождения
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // listener срабатывающий при получении клиентом местоположения
        mLocationListener = new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location currentLocation) {
                mLatLng = new LatLng(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude()
                );
                getPois();
            }
        };

        // check the status of Google Play Services
        if (servicesOK()) {
            if (mMap == null) {
                // map initialization
                ((SupportMapFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.map_fragment)))
                        .getMapAsync(MapActivity.this);
            } else {
                moveToCurrentLocation(null);
            }
        }
    }

    // ON MAP READY --------------------------------------------------------------------------------
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

            // movement listeners
            mMap.setOnCameraMoveStartedListener(this);
            mMap.setOnCameraIdleListener(this);

            moveToCurrentLocation(null);       // kick off the process ------

        } else {
            Toast.makeText(this, "Map not connected!", Toast.LENGTH_SHORT).show();
        }
    }

    // MOVE TO CURRENT LOCATION --------------------------------------------------------------------
    public void moveToCurrentLocation(MenuItem itemClicked) {

        if (itemClicked != null)
            mFirstRun = true;

        // check permissions to get current location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // use client to get current location and call the listener
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, mLocationListener);
    }

    // GET POIS ------------------------------------------------------------------------------------
    public void getPois() {

        mModel = Model.getInstance(MapActivity.this);

        if (mFirstRun) {
            mModel.setmZoomLevel(11);
        } else {
            float zoomLevel = mMap.getCameraPosition().zoom;
            mModel.setmZoomLevel(zoomLevel);
        }

        mModel.requestPOIs(MapActivity.this, mLatLng);
    }

    // UPDATE POIS ---------------------------------------------------------------------------------
    // triggered after the POIs request is finished (Model.PoisListener interface)
    @Override
    public void updatePois(List<Poi> list, boolean loading) {

        mMoving = false;
        mPoiList = list;
        updateMarkers();

        if (mFirstRun) {
            mFirstRun = false;
            moveCamera();
        }
    }

    // UPDATE MARKERS ------------------------------------------------------------------------------
    private void updateMarkers() {

        mMap.clear();

        try {
            for (int i = 0; i < mPoiList.size(); i++) {
                // marker options
                MarkerOptions options = new MarkerOptions()
                        .title(mPoiList.get(i).getName())
                        .snippet(mPoiList.get(i).getIcon())
                        .anchor(.5f, .5f)
                        .position(new LatLng(mPoiList.get(i).getGeometry().getJSONObject("location").getDouble("lat"),
                                mPoiList.get(i).getGeometry().getJSONObject("location").getDouble("lng")));
                // add marker on map
                mMap.addMarker(options);
            }
        } catch (Exception e) {
            Toast.makeText(MapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(MapActivity.this.getLocalClassName(), e.getMessage());
        }
    }

    // MOVE CAMERA ---------------------------------------------------------------------------------
    private void moveCamera() {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(mLatLng, mModel.getmZoomLevel());
        mMap.moveCamera(update);
    }

    // MOVE TO SEARCH LOCATION ---------------------------------------------------------------------
    public void moveToSearchLocation(View view) throws IOException {
        getSearchLocation(view);
        mFirstRun = true;           // move the camera to new location
        getPois();
    }

    // GET SEARCH LOCATION -------------------------------------------------------------------------
    public void getSearchLocation(View view) throws IOException {

        hideSoftKeyboard(view);                                         // hide keyboard
        EditText searchWindow = findViewById(R.id.search_window);
        String searchString = searchWindow.getText().toString();        // выяснить что введено в поисковом поле

        Geocoder gc = new Geocoder(this);
        List<Address> addressList = gc.getFromLocationName(searchString, 1);
        Address address = addressList.get(0);
        double lat = address.getLatitude();
        double lng = address.getLongitude();
        mLatLng = new LatLng(lat, lng);
    }

    // CAMERA EVENTS CALLBACKS ----------------------------------------------------------------------
    @Override
    public void onCameraIdle() {
        if (mMoving) {

            mPrevLatLng = mLatLng;

            mLatLng = mMap.getCameraPosition().target;              // get new camera position

            Location past = new Location("past");
            Location current = new Location("current");

            past.setLatitude(mPrevLatLng.latitude);
            past.setLongitude(mPrevLatLng.longitude);
            current.setLatitude(mLatLng.latitude);
            current.setLongitude(mLatLng.longitude);

            float distance = past.distanceTo(current);              // calc distance between positions

            if (distance > 5000) {
                getPois();
                Toast.makeText(MapActivity.this, "Recalculate!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE)
            mMoving = true;
    }

    // OTHER ---------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
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

    // hide the keyboard
    private void hideSoftKeyboard(View view) {

        // для скрытия понадобится менеджер методов ввода
        InputMethodManager imm =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        // выяснить где находится клавиатура и скрыть ее
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void switchToListActivity(MenuItem item) {
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra(MainActivity.CALLING_ACTIVITY, "MapActivity");
        startActivity(intent);
    }
}
