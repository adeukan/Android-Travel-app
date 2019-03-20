package vk.travel;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class FragmentList extends Fragment implements Model.PoisListener {

    Activity mActivity;
    Model mModel;
    RecyclerView mRecyclerView;                                                                     // reference to RecyclerView containing the list
    LinearLayoutManager mLayoutManager;                                                             // layout manager for RecyclerView (used in the scroll listener)
    FusedLocationProviderClient mLocationClient;                                                    // client to get location
    OnSuccessListener<Location> mLocationListener;                                                  // location client callback
    private AdapterPoiList mAdapter;                                                                // RecyclerView Adapter
    private LatLng mLatLng;                                                                         // current location
    private LatLng mPrevLatLng;                                                                     // previous location

    // ON FRAGMENT ATTACHED ------------------------------------------------------------------------
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

        View view = inflater.inflate(R.layout.fragment_list, container, false);          // inflate fragment with layout

        Button button = mActivity.findViewById(R.id.search_button);                                 // reference to search button in Toolbar
        button.setOnClickListener(new View.OnClickListener(){                                       // search button click listener
            @Override
            public void onClick(View view) {
                try {
                    jumpToSearchLocation(view);
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
        mLatLng = mModel.mLatLng;                                                                   // get current location

        mAdapter = new AdapterPoiList(mActivity, mModel.mPoiList);                                  // initialise RecyclerView Adapter
        mRecyclerView = view.findViewById(R.id.recyclerView);                                       // reference to RecyclerView containing the list
        mRecyclerView.setAdapter(mAdapter);                                                         // apply adapter to display the list in RecyclerView

        mLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);               // initialise location client
        mLocationListener = new OnSuccessListener<Location>() {                                     // initialise location client callback
            @Override
            public void onSuccess(Location currentLocation) {                                       // on current location received
                mLatLng = new LatLng(                                                               // save current location
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude()
                );
                if (compareLocations()) {                                                           // if the previous location is far enough
                    mModel.setmZoomLevel(11);                                                       // use default zoom level
                    mModel.requestPOIs(FragmentList.this, mLatLng);                        // and make new request to get POIs
                }
            }
        };
       // mLayoutManager = new LinearLayoutManager(this.mActivity);                                 // layout manager for RecyclerView
       // mRecyclerView.setLayoutManager(mLayoutManager);
       // mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {                   // scroll listener
       //     @Override
       //     public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
       //         // check for scroll down ('dy' - distance and direction of vertical scroll)
       //         if (dy > 0) {
       //             final int totalItemCount = mLayoutManager.getItemCount();
       //             final int visibleItemCount = mLayoutManager.getChildCount();
       //             final int pastItemCount = mLayoutManager.findFirstVisibleItemPosition();
       //
       //             // track the moment when the bottom is reached
       //             if (mLoading) {
       //                 if ((visibleItemCount + pastItemCount) == totalItemCount) {
       //                     Log.v("mylog", "The bottom is reached!");
       //
       //                     mLoading = false;                                                     // stop tracking the reaching the bottom
       //                     mModel.requestPOIs(FragmentList.this, null);                          // request POIs and add them to Model
       //                     // then updatePois() executed
       //                 }
       //             }
       //         }
       //     }
       // });
    }

    // JUMP TO SEARCH LOCATION ---------------------------------------------------------------------
    public void jumpToSearchLocation(View view) throws IOException {
        getSearchLocation(view);                                                                    // find new requested location

        if(compareLocations()){                                                                     // if the previous location is far enough
            mModel.setmZoomLevel(11);                                                               // use default zoom level
            mModel.requestPOIs(FragmentList.this, mLatLng);                                // and make new request to get POIs
        };
    }

    // GET SEARCH LOCATION -------------------------------------------------------------------------
    public void getSearchLocation(View view) throws IOException {

        hideSoftKeyboard(view);                                                                     // hide keyboard

        EditText searchBox = mActivity.findViewById(R.id.search_box);
        String searchString = searchBox.getText().toString();                                       // get string from the search box
        searchBox.getText().clear();                                                                // clear searchbox

        Geocoder gc = new Geocoder(mActivity);
        List<Address> addressList = gc.getFromLocationName(searchString, 1);              // use Geocoder to get address from the search string
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

    // UPDATE POIS ----------------- (Model.PoisListener interface) --------------------------------
    @Override
    public void updatePois(List<Poi> next_poi_portion, boolean nextPageAvailable) {                 // triggered after requestPOIs is finished

        mModel.mPoiList.addAll(next_poi_portion);                                                   // add next portion of result to mPoiList
        if(nextPageAvailable) {                                                                     // if nextPageToken available
            mModel.requestPOIs(this, mLatLng);                                             // repeat request to get more POIs
            return;
        }
        mAdapter.notifyDataSetChanged();                                                            // ask adapter to rebuild the list
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
                mPrevLatLng = mLatLng;                                                              // save previous location
                                                                                                    // check permissions to get current location
                if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Request the missing permissions
                }

                mLocationClient.getLastLocation().addOnSuccessListener(mActivity, mLocationListener);// get current location and trigger the listener
                return true;
        }
        return false;
    }
}
