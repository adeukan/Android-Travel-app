package vk.travel;


import android.os.Bundle;
import android.support.annotation.NonNull;
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


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // URL to remote server folder with JSON file
    public static final String SOURCE_FOLDER = "http://192.168.1.5/travel/";
    // full URL to JSON file
    public static final String JSON_URL = SOURCE_FOLDER + "dublin_pois.json";
    // list of POIs (used by the adapter to display in RecyclerView)
    List<Poi> mPoiList = new ArrayList<>();


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
                            Toast.makeText(MainActivity.this, "Parsing Error!", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Web Response Error!", Toast.LENGTH_LONG).show();
                    }
                }); // end of request definition


//        // the same request but using Gson
//        StringRequest request = new StringRequest(JSON_URL, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                Gson gson = new Gson();
//                Poi[] POIsArray = gson.fromJson(response, Poi[].class);
//                mPoiList = new ArrayList<>(Arrays.asList(POIsArray));
//                displayPoiList(null);
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(MainActivity.this, "Web Response Error!", Toast.LENGTH_LONG).show();
//            }
//        });


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
                displayPoiList("a");
                break;
            case R.id.b:
                displayPoiList("b");
                break;
            case R.id.c:
                displayPoiList("c");
                break;
            case R.id.d:
                displayPoiList("e");
                break;
            case R.id.e:
                displayPoiList(null);
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

    // handle the Menu item click (on the top right) -----------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // if the Settings item was clicked
        if (id == R.id.settings) {
            // move to settings activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
