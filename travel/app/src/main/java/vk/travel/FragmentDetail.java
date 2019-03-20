package vk.travel;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class FragmentDetail extends Fragment
        implements OnMapReadyCallback,
        Model.PoiDetailListener {

    Model mModel;
    ActivityMain mActivity;
    private GoogleMap mMap;
    private List<Bitmap> carouselImages = new ArrayList<>();                                        // images for carousel


    // ON FRAGMENT ATTACHED ------------------------------------------------------------------------
    @Override
    public void onAttach(Context ctx) {                                                             // save context activity when fragment attached
        super.onAttach(ctx);
        mActivity = (ActivityMain) ctx;
    }

    // ON CREATE -----------------------------------------------------------------------------------
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);                                                                    // allows to handle menu item clicks
    }

    // ON CREATE VIEW ------------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);        // inflate fragment with layout

        TextView tvPoiName = rootView.findViewById(R.id.name);
        tvPoiName.setText(mActivity.mPoi.getName());

        // TextView tvPoiCategory = rootView.findViewById(R.id.category);
        // String categoryUpperCase = mPoiCategory.substring(0,1).toUpperCase() + mPoiCategory.substring(1);
        // tvPoiCategory.setText("No Category");
        // TextView tvDescription = rootView.findViewById(R.id.description);
        // tvDescription.setText("No description at the moment\n");

        return rootView;                                                                                // view object is ready to return
    }

    // ON VIEW CREATED -----------------------------------------------------------------------------
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        mModel = Model.getInstance(mActivity);

        if (servicesOK()) {                                                                         // check GooglePlayServices
            SupportMapFragment mf;
            mf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.detail_map);         // create reference to fragment supporting maps
            mf.getMapAsync(this);                                                   // initialize the map in this fragment
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

                    View view = getLayoutInflater().inflate(R.layout.marker_info_win, null);   // inflate info window layout

                    TextView tvPoiName = view.findViewById(R.id.infoName);                          // references to info window views
                    ImageView ivIcon = view.findViewById(R.id.infoIcon);

                    tvPoiName.setText(marker.getTitle());                                           // add title from the marker
                    String iconURL = marker.getSnippet();                                           // get icon URL from the marker
                    Picasso.get().load(iconURL).into(ivIcon);                                       // attach icon to ImageView using Picasso
                    return view;
                }
            });

            mModel.requestPoiDetail(this, mActivity.mPoi.getId());

        } else {
            Toast.makeText(mActivity, "Map not connected!", Toast.LENGTH_SHORT).show();
        }
    }

    // DISPLAY POI ---------------------------------------------------------------------------------
    public void displayPoiWhenReady() {

        List<String> imageList = new ArrayList();
        JSONArray imageJsonArray = mActivity.mPoi.getPhotos();
        if (imageJsonArray != null) {
            for (int i = 0; i < imageJsonArray.length(); i++){
                try {
                    imageList.add(imageJsonArray.getJSONObject(i).getString("photo_reference"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        DownloadPhotoTask asyncTask = new DownloadPhotoTask();                                      // создать asyncTask
        asyncTask.execute(imageList);


        LatLng latLng = null;
        try {
            latLng = new LatLng(
                    mActivity.mPoi.getGeometry().getJSONObject("location").getDouble("lat"),
                    mActivity.mPoi.getGeometry().getJSONObject("location").getDouble("lng"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mMap.moveCamera(update);

        // marker options
        MarkerOptions options = new MarkerOptions()
                .title(mActivity.mPoi.getName())
                .snippet("no category")
                .anchor(.5f, .5f)
                .position(latLng);
        // add marker
        mMap.addMarker(options);
    }

    // DOWNLOAD CAROUSEL IMAGES --------------------------------------------------------------------
    private class DownloadPhotoTask extends AsyncTask<List<String>, Bitmap, Void> {

        @Override
        protected Void doInBackground(List<String>... lists) {

            List<String> list = lists[0];
            for (String string : list) {                                                         // выполлнить для каждого из двух пришедших string

                String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=300&photoreference="
                        + string
                        + "&key=AIzaSyCZac9ubfqe9Sy-SZCxfZCeNbiDyhv_2hs";

                InputStream inputStream = null;                                                     // создать input stream
                try {
                    inputStream = (InputStream) new URL(url).getContent();                          // подключить input stream к URL
                    Bitmap bitmapImage = BitmapFactory.decodeStream(inputStream);
                    publishProgress(bitmapImage);                       // скачать изображение и передать в onProgressUpdate()
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();                                                    // закрыть input stream
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;                                                                            // doInBackground() должен что-то вернуть (Void - это объект)
        }

        @Override
        protected void onProgressUpdate(Bitmap... bitmaps) {                                        // этот метод работает в основном потоке
            super.onProgressUpdate(bitmaps);
            carouselImages.add(bitmaps[0]);                                                         // отправить промежуточный результат в основной поток
        }

        @Override
        protected void onPostExecute(Void aVoid) {                                                  // то, что нужно сделать по окончании работы asyncTask
            super.onPostExecute(aVoid);

            ViewPager viewPager = mActivity.findViewById(R.id.viewPager);
            AdapterImgCarousel adapter = new AdapterImgCarousel(getContext(), carouselImages);
            viewPager.setAdapter(adapter);
        }
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

    // MENU ITEM CLICKS ----------------------------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem itemClicked) {
        switch (itemClicked.getItemId()) {

            case R.id.find_me:
                return true;
        }
        return false;
    }
}

