package vk.travel;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import java.net.URL;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class FragmentMap extends Fragment
        implements OnMapReadyCallback,
        Model.PoisListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnCameraMoveStartedListener {

    Activity mActivity;
    Model mModel;
    GoogleMap mMap;
    LatLng mLatLng, mPrevLatLng;                                                                    // current and previous camera position
    boolean mUserMovesCamera = false;                                                               // user moves the map
    boolean mJumpToNewLocation = true;                                                              /* use default zoom and move camera to new location
                                                                                                    when jump to new location via 'FIND MI' or 'SEARCH' */
    FusedLocationProviderClient mLocationClient;                                                    // get current location client
    OnSuccessListener<Location> mLocationListener;                                                  // get current location listener

    // ON FRAGMENT ATTACHED ------------------------------------------------------------------------
    @Override
    public void onAttach(Context ctx) {                                                             // save context activity when fragment attached
        super.onAttach(ctx);
        mActivity = (Activity) ctx;
    }

    // ON CREATE -----------------------------------------------------------------------------------
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);                                                                    // allows to handle menu item clicks
    }

    // ON CREATE VIEW ------------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);           // inflate fragment with layout

        Button button = mActivity.findViewById(R.id.search_button);                                 // reference to search button in Toolbar
        button.setOnClickListener(new View.OnClickListener(){                                       // search button click listener
            @Override
            public void onClick(View v) {
                try {
                    jumpToSearchLocation(v);                                                        // move to desired location
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return view;                                                                                // view object is ready to return
    }

    // ON VIEW CREATED -----------------------------------------------------------------------------
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        mModel = Model.getInstance(mActivity);                                                      // get Model instance
        mLatLng = mModel.mLatLng;                                                                   // get current location from Model

        mLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);               // initialise location client
        mLocationListener = new OnSuccessListener<Location>() {                                     // initialise location client callback
            @Override
            public void onSuccess(Location currentLocation) {                                       // on current location received
                mLatLng = new LatLng(
                        currentLocation.getLatitude(),                                              // save current location
                        currentLocation.getLongitude()
                );
                getPois();                                                                          // get POIs for current location
            }
        };

        if (servicesOK()) {                                                                         // check GooglePlayServices
            SupportMapFragment mf;
            mf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);         // create reference to fragment supporting maps
            mf.getMapAsync(this);                                                    // initialize the map in this fragment

            // SupportMapFragment mf = new SupportMapFragment();                                    // another way to create a fragment for the map
            // getChildFragmentManager().beginTransaction()
            //         .add(R.id.map_frame_layout, mf)                                              // it requires FrameLayout in XML
            //         .commit();
            // mf.getMapAsync(this);
        }
    }

    // ON MAP READY --------------------------------------------------------------------------------
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        if (mMap != null) {
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {                           // customize info window
                @Override
                public View getInfoWindow(Marker marker) { return null; }                           // used to change the window itself (we don't use)
                @Override
                public View getInfoContents(Marker marker) {                                        // used to change info window content

                    View view = getLayoutInflater().inflate(R.layout.marker_info_win, null);           // inflate info window layout

                    TextView tvPoiName = view.findViewById(R.id.infoName);                          // references to info window views
                    ImageView ivIcon = view.findViewById(R.id.infoIcon);

                    tvPoiName.setText(marker.getTitle());                                           // add title from the marker
                    String iconURL = marker.getSnippet();                                           // get icon URL from the marker
                    Picasso.get().load(iconURL).into(ivIcon);                                       // attach icon to ImageView using Picasso
                    // TODO: First touch doesn't show icon

                    return view;
                }
            });

            mMap.setOnCameraMoveStartedListener(this);                                              // apply map movement listeners
            mMap.setOnCameraIdleListener(this);

            if(mModel.mPoiList.size() == 0)
                jumpToCurrentLocation(null);                                               // kick off the process of moving map and getting objects
            else {
                mJumpToNewLocation = true;
                getPois();
            }
        } else {
            Toast.makeText(mActivity, "Map not connected!", Toast.LENGTH_SHORT).show();
        }
    }

    // JUMP TO CURRENT LOCATION --------------------------------------------------------------------
    public void jumpToCurrentLocation(MenuItem itemClicked) {

        if (itemClicked != null)
            mJumpToNewLocation = true;                                                              // use default zoom and move camera afterward
                                                                                                    // check permissions to get current location
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Request the missing permissions
            return;
        }
        mLocationClient.getLastLocation().addOnSuccessListener(mActivity, mLocationListener);       // get current location and trigger the listener
    }

    // JUMP TO SEARCH LOCATION ---------------------------------------------------------------------
    public void jumpToSearchLocation(View view) throws IOException {
        getSearchLocation(view);                                                                    // find new requested location

        if(compareLocations()){                                                                     // if the previous location is far enough
            mJumpToNewLocation = true;                                                              // be ready to make jump
            getPois();                                                                              // and make request to get POIs for new location
        };
    }

    // GET SEARCH LOCATION -------------------------------------------------------------------------
    public void getSearchLocation(View view) throws IOException {

        hideSoftKeyboard(view);                                                                     // hide keyboard

        EditText searchBox = mActivity.findViewById(R.id.search_box);
        String searchString = searchBox.getText().toString();                                       // get string from the searchbox
        searchBox.getText().clear();                                                                // clear searchbox

        Geocoder gc = new Geocoder(mActivity);
        List<Address> addressList = gc.getFromLocationName(searchString, 1);               // use Geocoder to get address from the search string
        Address address = addressList.get(0);
        double lat = address.getLatitude();
        double lng = address.getLongitude();
        mPrevLatLng = mLatLng;                                                                      // save previous location
        mLatLng = new LatLng(lat, lng);                                                             // set new location
    }

    // COMPARE LOCATIONS ---------------------------------------------------------------------------
    private boolean compareLocations() {
        Location past = new Location("past");                                               // previous location
        past.setLatitude(mPrevLatLng.latitude);
        past.setLongitude(mPrevLatLng.longitude);

        Location current = new Location("current");                                         // new location
        current.setLatitude(mLatLng.latitude);
        current.setLongitude(mLatLng.longitude);

        float distanceChange = past.distanceTo(current);                                            // calculate distance change
        float zoomChange = Math.abs(mModel.getmZoomLevel() - 11);                                   // calculate zoom change (any jump uses 11 zoom)

        if (distanceChange > 5000 || zoomChange > 1) {                                              // if distance or zoom increased significantly
            if (distanceChange > 250000)
                mModel.mPoiList.clear();                                                            // clear list if previous location is too far
            return true;                                                                            // allow new POI request
        }
        else
            return false;                                                                           // suppress new POI request
    }

    // GET POIS ------------------------------------------------------------------------------------
    public void getPois() {

        if (mJumpToNewLocation) {                                                                   // if POIs are requested in a jump
            mModel.setmZoomLevel(11);                                                               // use default zoom level
        } else {
            float zoomLevel = mMap.getCameraPosition().zoom;                                        // otherwise use current zoom level
            mModel.setmZoomLevel(zoomLevel);
        }
        mModel.requestPOIs(this, mLatLng);                                                 // make request to get POIs
    }

    // UPDATE POIS ----------------- (Model.PoisListener interface) --------------------------------
    @Override
    public void updatePois(List<Poi> next_poi_portion, boolean nextPageAvailable) {                 // triggered after the POIs request is finished

        mModel.mPoiList.addAll(next_poi_portion);                                                   // add next portion of received POIs to mPoiList

        if(nextPageAvailable) {                                                                     // if nextPageToken available
            mModel.requestPOIs(this, mLatLng);                                             // repeat request to get more POIs
            return;
        }

        updateMarkers();                                                                            // update markers when all POIs received

        if (mJumpToNewLocation) {                                                                   // if POIs are requested in a jump
            mJumpToNewLocation = false;
            moveCamera();                                                                           // move camera to new location
        }
    }

    // UPDATE MARKERS ------------------------------------------------------------------------------
    private void updateMarkers() {
        mMap.clear();                                                                               // remove all markers
        try {
            for (int i = 0; i < mModel.mPoiList.size(); i++) {                                      // add marker for each POI in the list

                MarkerOptions options = new MarkerOptions()                                         // create marker options
                        .title(mModel.mPoiList.get(i).getName())                                    // marker title with POI name
                        .snippet(mModel.mPoiList.get(i).getIcon())                                  // marker snippet with icon URL
                        .anchor(.5f, .5f)
                                                                                                    // marker location
                        .position(new LatLng(mModel.mPoiList.get(i).getGeometry().getJSONObject("location").getDouble("lat"),
                                mModel.mPoiList.get(i).getGeometry().getJSONObject("location").getDouble("lng")));

                mMap.addMarker(options);                                                            // add marker to the map
            }
        } catch (Exception e) {
            Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(mActivity.getLocalClassName(), e.getMessage());
        }
    }

    // MOVE CAMERA ---------------------------------------------------------------------------------
    private void moveCamera() {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(mLatLng, mModel.getmZoomLevel());
        mMap.moveCamera(update);
    }

    // CAMERA EVENTS CALLBACKS ---------------------------------------------------------------------
    @Override
    public void onCameraIdle() {                                                                    // the camera stops
        if (mUserMovesCamera) {                                                                     // if camera moved by user
            mPrevLatLng = mLatLng;                                                                  // save previous camera position
            mLatLng = mMap.getCameraPosition().target;                                              // save current camera position

            Location past = new Location("past");                                           // previous camera position
            past.setLatitude(mPrevLatLng.latitude);
            past.setLongitude(mPrevLatLng.longitude);

            Location current = new Location("current");                                     // current camera position
            current.setLatitude(mLatLng.latitude);
            current.setLongitude(mLatLng.longitude);

            float distanceChange = past.distanceTo(current);                                        // calculate distance change
            float zoomChange = Math.abs(mModel.getmZoomLevel() - mMap.getCameraPosition().zoom);    // calculate zoom change

            if (distanceChange > 5000 || zoomChange > 1) {                                          // if distance or zoom increased significantly
                if (distanceChange > 250000)
                    mModel.mPoiList.clear();                                                        // clear list if previous location is too far
                getPois();                                                                          // get POIs for new location
                mUserMovesCamera = false;
            }
        }
    }
    @Override
    public void onCameraMoveStarted(int reason) {                                                   // the camera starts moving
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE)                         // if user moves camera
            mUserMovesCamera = true;                                                                // set flag
    }

    // GOOGLE SERVICES AVAILABILITY ----------------------------------------------------------------
    public boolean servicesOK() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int isAvailable = googleAPI.isGooglePlayServicesAvailable(mActivity);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleAPI.isUserResolvableError(isAvailable)) {
            Dialog dialog = googleAPI.getErrorDialog(mActivity, isAvailable, 911);
            dialog.show();
        } else {
            Toast.makeText(mActivity, "Can't connect to mapping service", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // HIDE KEYBOARD -------------------------------------------------------------------------------
    private void hideSoftKeyboard(View view) {
        InputMethodManager imm =                                                                    // get input method manager
                (InputMethodManager) mActivity.getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);                                 // find keyboard and hide it
    }

    // MENU ITEM CLICKS ----------------------------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem itemClicked) {
        switch (itemClicked.getItemId()) {

            case R.id.find_me:
                jumpToCurrentLocation(itemClicked);
                return true;
        }
        return false;
    }
}
