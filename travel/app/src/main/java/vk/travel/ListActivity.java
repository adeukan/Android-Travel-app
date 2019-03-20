package vk.travel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity implements Model.PoisListener{

    List<Poi> mPoiList = new ArrayList<>();

    // ---------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // request POIs and save them in the model
        Model model = Model.getInstance(this);
        model.requestPOIs(this);
    }

    // triggered after the POIs request is finished (belongs to Model.PoisListener interface)
    @Override
    public void listUpdated(List<Poi> list) {
        mPoiList = list;
        displayPoiList(null);
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
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        // use adapter to display the list of POIs in RecyclerView
        recyclerView.setAdapter(new PoiListAdapter(this, filteredList));
    }
}
