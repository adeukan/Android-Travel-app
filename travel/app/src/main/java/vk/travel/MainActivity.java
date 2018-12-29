package vk.travel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import vk.travel.model.Poi;
import vk.travel.services.MyIntentService;
import vk.travel.utils.NetworkChecker;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // URL to remote server folder with JSON file
    public static final String SOURCE_FOLDER = "http://192.168.1.5/travel/";
    // full URL to JSON file
    public static final String JSON_URL = SOURCE_FOLDER + "dublin_pois.json";

    // list of POIs (used by the adapter to display in RecyclerView)
    List<Poi> mPoiList;

    // receiver for local broadcast messages from MyIntentService (IntentService)
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // get the response array of parcelable Poi objects from the intent using the key
            Poi[] pois = (Poi[]) intent.getParcelableArrayExtra(MyIntentService.BROADCAST_CONTENT);
            // create new List based on the received array, because the adapter works with a List
            mPoiList = new ArrayList<>(Arrays.asList(pois));
            // call method to display pois in RecyclerView without filtering
            displayPOIs(null);
        }
    };

    // ---------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // reference to the toolbar at the top of MainActivity
        Toolbar toolbar = findViewById(R.id.toolbar);
        // make available the use of Menu icon on the right top of the Toolbar
        setSupportActionBar(toolbar);
        // reference to DrawerLayout of MainActivity
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        // set 'sandwich' menu icon (top left) and toggle object used to open/close the drawer menu
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        // listener to open/close the drawer menu
        drawer.addDrawerListener(toggle);
        // synchronize the toggle state according to the drawer menu state
        toggle.syncState();
        // listener for the icons inside the Drawer Menu
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // makeRequest() is called from onResume()

        // register the broadcast receiver to get messages from MyIntentService
        // only broadcast with specified Intent object will be accepted
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mBroadcastReceiver,
                        new IntentFilter(MyIntentService.BROADCAST));
    }

    // making the Web request ----------------------------------------------------------------------
    protected void makeRequest() {

        // check network connection state
        boolean networkState = NetworkChecker.hasNetworkAccess(this);

        // if connected
        if (networkState) {
            // create Intent object for starting MyIntentService
            Intent intent = new Intent(this, MyIntentService.class);
            // wrap URL string to URI object and attach to intent
            intent.setData(Uri.parse(JSON_URL));
            // MyIntentService is used to get the data from the Web in the background, and broadcast the result
            startService(intent);
        }
        // if not connected
        else {
            // display the Toast message
            Toast.makeText(this, "Network is not available", Toast.LENGTH_SHORT).show();
        }
    }

    // display the list of POIs in RecyclerView ----------------------------------------------------
    protected void displayPOIs(String filter) {

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
        RecyclerView recyclerView = findViewById(R.id.poisRecView);
        // use adapter to display the list of POIs in RecyclerView
        recyclerView.setAdapter(new PoiAdapter(this, filteredList));
    }



    // handle a filtering item click in drawer menu ------------------------------------------------
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.a:
                displayPOIs("a");
                break;
            case R.id.b:
                displayPOIs("b");
                break;
            case R.id.c:
                displayPOIs("c");
                break;
            case R.id.d:
                displayPOIs("e");
                break;
            case R.id.e:
                displayPOIs(null);
                break;
        }
        // close drawer menu
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // inflate (make visible) Menu Icon (on the top right) -----------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    // close the drawer menu by Back key -----------------------------------------------------------
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // called when the activity become active ------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();
        // make request to the Web and return the response data
        makeRequest();
        // after the data is returned, the broadcast receiver calls displayPOIs()
    }


    // handle the Menu item click (on the top right) -----------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        // if the Settings item was clicked
        if (id == R.id.settings) {
            // move to settings activity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
