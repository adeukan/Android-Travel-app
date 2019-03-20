package vk.travel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

class AdapterPoiList extends RecyclerView.Adapter<AdapterPoiList.ViewHolder> {

    private final ActivityMain mActivity;
    // list of POIs
    private List<Poi> mPoiList;
    // ActivityMain context
    private Context mCtx;
    // used to identify the information attached to intent object
    static final String KEY_LAT = "lat";
    static final String KEY_LON = "lon";
    static final String KEY_NAME = "name";
    static final String KEY_CATEGORY = "category";

    AdapterPoiList(Context context, List<Poi> poiList) {
        this.mCtx = context;
        this.mActivity = (ActivityMain) mCtx;
        this.mPoiList = poiList;
    }

    // CREATE LIST ELEMENT -------------------------------------------------------------------------
    @Override
    public AdapterPoiList.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mCtx);                                    // create inflater
        View poiView = inflater.inflate(R.layout.list_item, parent, false);              // inflate element with layout
        return new ViewHolder(poiView);                                                             // return to onBindViewHolder() wrapped in ViewHolder
                                                                                                    // ViewHolder contains references to element widgets
    }

    // ADD ELEMENT CONTENT  ------------------------------------------------------------------------
    @Override
    public void onBindViewHolder(final AdapterPoiList.ViewHolder holder, int position) {

        final Poi poi = mPoiList.get(position);                                                     // get the POI for current element
        try {
            holder.mName.setText(poi.getName());                                                    // attach POI name to the element

            Bitmap bitmap = ManagerImageCache.getPhotoFromCache(mCtx, poi);                     // try to get POI photo from cache
            if (bitmap == null) {                                                                   // if not found in cache, download it in background
                DownloadPhotoTask asyncTask = new DownloadPhotoTask(holder);                        // asyncTask needs holder to attach photo to element
                asyncTask.execute(poi);                                                             // download POI photo and attach to element
            }
            else                                                                                    // if photo is already in the cache
                holder.mPhoto.setImageBitmap(bitmap);                                               // attach photo to element
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.mListItem.setOnClickListener(new View.OnClickListener() {                            // element click handler
            @Override
            public void onClick(View v) {
                mActivity.mPoi = poi;                                                               // save POI for use in FragmentDetail
                FragmentTransaction ft;
                ft = mActivity.getSupportFragmentManager().beginTransaction();                      // start transaction
                ft.replace(R.id.fragment_container, new FragmentDetail());                          // replace fragment
                ft.commit();
            }
        });
    }

    // get the size of the list of POIs ------------------------------------------------------------
    @Override
    public int getItemCount() {
        return mPoiList.size();
    }


    // ViewHolder contains a View element and the references to its widgets ------------------------
    static class ViewHolder extends RecyclerView.ViewHolder {

        // references to widgets inside View
        TextView mName;
        ImageView mPhoto;
        View mListItem;

        // constructor (wrapping a View object in ViewHolder class)
        ViewHolder(View itemView) {
            super(itemView);

            mName = itemView.findViewById(R.id.itemName);
            mPhoto = itemView.findViewById(R.id.itemPicture);
            mListItem = itemView;
        }
    }

    // member class, used to download an image from the Web in the background ----------------------
    private class DownloadPhotoTask extends AsyncTask<Poi, Void, Bitmap> {

        // Generic Declaration Types:
        // Poi - input data type
        // Void - no additional data is used during the background process
        // Bitmap - output data

        // POI whose photo should be downloaded
        private Poi mPoi;
        // holder with View object and references to View widgets
        private ViewHolder mHolder;
        private String mPhotoURL;

        // new object gets the holder to use its reference to attach the photo to ImageView
        // POI object is received at the time of execution the task
        DownloadPhotoTask(ViewHolder holder) {
            this.mPoi = null;
            this.mHolder = holder;
            this.mPhotoURL = null;
        }

        // background thread to download the photo from the Web
        @Override
        protected Bitmap doInBackground(Poi... pois) {

            // the method gets arguments as an array
            mPoi = pois[0];

            // construct URL String to the POI photo on the Web server
            if(mPoi.getTitlePhotos() != null) {
                try {
                    mPhotoURL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=300&photoreference="
                            + mPoi.getTitlePhotos().getJSONObject(0).getString("photo_reference")
                            + "&key=AIzaSyCZac9ubfqe9Sy-SZCxfZCeNbiDyhv_2hs";
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // declare input stream outside the try/catch block
            InputStream inputStream = null;

            try {
                // get URL content as input stream
                inputStream = (InputStream) new URL(mPhotoURL).getContent();
                // decode stream to a Bitmap object and return it to onPostExecute()
                return BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        // close input stream to avoid leaks
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // if image doesn't exist on the Web server, return null to onPostExecute()
            return null;
        }

        // use holder object to attach the photo to ImageView, and store it in the cache
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            // attach the photo to ImageView
            mHolder.mPhoto.setImageBitmap(bitmap);
            // store the photo in the cache
            try {
                ManagerImageCache.putPhotoToCache(mCtx, mPoi, bitmap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
