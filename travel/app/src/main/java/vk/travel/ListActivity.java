package vk.travel;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity implements Model.PoisListener {

    Model mModel;
    List<Poi> mPoiList = new ArrayList<>();                     // the list of POIs to display in RecyclerView
    RecyclerView mRecyclerView;                                 // reference to RecyclerView
    LinearLayoutManager mLayoutManager;                         // layout manager for RecyclerView (used in the scroll listener)
    boolean mLoading = true;                                    // track the moment when the list bottom is reached
    FusedLocationProviderClient mFusedLocationClient;           // client to get location
    OnSuccessListener<Location> mLocationListener;
    private LatLng mLatLng;

    // ---------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // инициализация клиента для последующего получения местонахождения
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationListener = new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location currentLocation) {
                mLatLng = new LatLng(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude()
                );
                mModel.requestPOIs(ListActivity.this, mLatLng);
            }
        };

        Intent intent = getIntent();
        String stringExtra = intent.getStringExtra(MainActivity.CALLING_ACTIVITY);

        if (stringExtra.equals(MainActivity.class.toString())) {
            getCurrentLocationItems();
        }
        else {

            mModel = Model.getInstance(this);
            displayPoiList(null, mModel.mModelPoiList);
        }

        mRecyclerView = findViewById(R.id.recyclerView);            // reference to RecyclerView
        mLayoutManager = new LinearLayoutManager(this);      // initialise layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);             // set layout manager for RecyclerView

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {         // scroll listener
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                // check for scroll down ('dy' - distance and direction of vertical scroll)
                if (dy > 0) {
                    final int totalItemCount = mLayoutManager.getItemCount();
                    final int visibleItemCount = mLayoutManager.getChildCount();
                    final int pastItemCount = mLayoutManager.findFirstVisibleItemPosition();

                    // track the moment when the bottom is reached
                    if (mLoading) {
                        if ((visibleItemCount + pastItemCount) == totalItemCount) {
                            Log.v("mylog", "The bottom is reached!");

                            mLoading = false;                                           // stop tracking the reaching the bottom
                            mModel.requestPOIs(ListActivity.this, null);    // request POIs and add them to Model
                            // then updatePois() executed
                        }
                    }
                }
            }
        });
    }


    // triggered after the POIs request is finished (Model.PoisListener interface) -----------------
    @Override
    public void updatePois(List<Poi> list, boolean loading) {

        mLoading = loading;             // continue or stop tracking the reaching the bottom
        mPoiList = list;                // update the list
        displayPoiList(null, mPoiList);      // display the updated list
    }


    // display the list of POIs in RecyclerView ----------------------------------------------------
    public void displayPoiList(String filter, List<Poi> poiList) {

        List<Poi> filteredList = new ArrayList<>(poiList);                         // filtered copy of POI list
        Log.v("mylog", String.valueOf(filteredList.size()));                    // log list elements count
        mRecyclerView.setAdapter(new PoiListAdapter(this, filteredList));    // display POIs in RecyclerView
    }

    // FIND CURRENT LOCATION -----------------------------------------------------------------------
    public void getCurrentLocationItems() {

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
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, mLocationListener);
    }
}