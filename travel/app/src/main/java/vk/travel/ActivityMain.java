package vk.travel;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ActivityMain extends AppCompatActivity {

    boolean mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);                                                               // use custom Toolbar as ActionBar

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new FragmentMap())
                .commit();

        mMapView = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_toggle:
                if (mMapView) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.fragment_container, new FragmentList());
                    ft.commit();
                    mMapView = !mMapView;

                    // mMenu.getItem(1).setIcon(R.drawable.baseline_map_white_48dp);
                    item.setIcon(R.drawable.baseline_map_white_48dp);
                    item.setTitle(R.string.action_map);
                }
                else {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.fragment_container, new FragmentMap());
                    ft.commit();
                    mMapView = !mMapView;

                    // mMenu.getItem(1).setIcon(R.drawable.baseline_list_alt_white_48dp);
                    item.setIcon(R.drawable.baseline_list_alt_white_48dp);
                    item.setTitle(R.string.action_list);
                }
                return true;

            // case R.id.search:
            //     View searchItem = findViewById(R.id.search);
            //     PopupMenu popupMenu = new PopupMenu(this, searchItem);
            //     popupMenu.inflate(R.menu.popup_menu);
            //     // popupMenu.show();
            //     return true;

            case R.id.find_me:                                                                      // this item click handled by fragments
                return false;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {                                                 // add menu to ActionBar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return true;
    }
}