package vk.travel;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity implements Model.PoisListener {

    Model mModel;
    List<Poi> mPoiList = new ArrayList<>();    // the list of POIs to display in RecyclerView
    RecyclerView mRecyclerView;                // reference to RecyclerView
    LinearLayoutManager mLayoutManager;        // layout manager for RecyclerView (used in the scroll listener)
    boolean mLoading = true;                   // track the moment when the list bottom is reached

    // ---------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mModel = Model.getInstance(this);
        mModel.requestPOIs(this);                            // request POIs and save them in Model
        // then listUpdated() is executed

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

                            mLoading = false;                                  // stop tracking the reaching the bottom
                            mModel.requestPOIs(ListActivity.this);      // request POIs and add them to Model
                            // then listUpdated() executed
                        }
                    }
                }
            }
        });
    }

    // triggered after the POIs request is finished (Model.PoisListener interface) -----------------
    @Override
    public void listUpdated(List<Poi> list, boolean loading) {

        mLoading = loading;             // continue or stop tracking the reaching the bottom
        mPoiList = list;                // update the list
        displayPoiList(null);      // display the updated list
    }

    // display the list of POIs in RecyclerView ----------------------------------------------------
    protected void displayPoiList(String filter) {

        List<Poi> filteredList = new ArrayList<>(mPoiList);                         // filtered copy of POI list
        Log.v("mylog", String.valueOf(filteredList.size()));                    // log list elements count
        mRecyclerView.setAdapter(new PoiListAdapter(this, filteredList));    // display POIs in RecyclerView
    }
}