package vk.travel;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import vk.travel.model.Poi;

class PoiAdapter extends RecyclerView.Adapter<PoiAdapter.ViewHolder> {

    // list of POIs
    private List<Poi> mPoiList;
    // MainActivity context
    private Context mContext;

    PoiAdapter(Context context, List<Poi> poiList) {
        this.mContext = context;
        this.mPoiList = poiList;
    }

    // create a View element for a POI inside ViewGroup(RecyclerView)-------------------------------
    @Override
    public PoiAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create inflater
        LayoutInflater inflater = LayoutInflater.from(mContext);
        // inflate the list element layout
        View poiView = inflater.inflate(R.layout.list_item, parent, false);
        // return ViewHolder object to onBindViewHolder() method
        // ViewHolder object contains the View element and the references to its widgets
        return new ViewHolder(poiView);
    }

    // add content to each widget inside current View element --------------------------------------
    @Override
    public void onBindViewHolder(PoiAdapter.ViewHolder holder, int position) {

        // get the next POI from the list based on its position
        final Poi poi = mPoiList.get(position);

        try {
            // if POI doesn't have name, take it from its tourism category
            if(poi.getName().equals("")){
                poi.setName(poi.getCategory());
            }

            // attach the POI name to the TextView
            holder.mName.setText(poi.getName());

//            // try to get the POI photo from the cache
//            Bitmap bitmap = ImageCacheManager.getPhotoFromCache(mContext, poi);
//
//            // if the photo is not found in the cache, start download task in the background
//            if (bitmap == null) {
//                // asyncTask object gets the holder to use its reference to attach the photo to ImageView
//                DownloadPhotoTask asyncTask = new DownloadPhotoTask(holder);
//                // start background process to download POI photo and attach it to ImageView
//                asyncTask.execute(poi);
//            }
//            // if the photo is already in the cache
//            else {
//                // attach POI photo to ImageView
//                holder.mPhoto.setImageBitmap(bitmap);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

            mName = itemView.findViewById(R.id.poiName);
            mPhoto = itemView.findViewById(R.id.progPhoto);
            mListItem = itemView;
        }
    }

//    // member class, used to download an image from the Web in the background ----------------------
//    private class DownloadPhotoTask extends AsyncTask<Poi, Void, Bitmap> {
//
//        // Generic Declaration Types:
//        // Poi - input data type
//        // Void - no additional data is used during the background process
//        // Bitmap - output data
//
//        // POI whose photo should be downloaded
//        private Poi mPoi;
//        // holder with View object and references to View widgets
//        private ViewHolder mHolder;
//
//        // new object gets the holder to use its reference to attach the photo to ImageView
//        // POI object is received at the time of execution the task
//        DownloadPhotoTask(ViewHolder holder) {
//            this.mPoi = null;
//            this.mHolder = holder;
//        }
//
//        // background thread to download the photo from the Web
//        @Override
//        protected Bitmap doInBackground(Poi... pois) {
//
//            // the method gets arguments as an array
//            mPoi = pois[0];
//
//            // construct URL String to the POI photo on the Web server
//            String photoUrl = MainActivity.PHOTO_BASE_URL + mPoi.getPhoto();
//
//            // declare input stream outside the try/catch block
//            InputStream inputStream = null;
//
//            try {
//                // get URL content as input stream
//                inputStream = (InputStream) new URL(photoUrl).getContent();
//                // decode stream to a Bitmap object and return it to onPostExecute()
//                return BitmapFactory.decodeStream(inputStream);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    if (inputStream != null) {
//                        // close input stream to avoid leaks
//                        inputStream.close();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            // if image doesn't exist on the Web server, return null to onPostExecute()
//            return null;
//        }
//
//        // use holder object to attach the photo to ImageView, and store it in the cache
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//            super.onPostExecute(bitmap);
//            // attach the photo to ImageView
//            mHolder.mPhoto.setImageBitmap(bitmap);
//            // store the photo in the cache
//            ImageCacheManager.putPhotoToCache(mContext, mPoi, bitmap);
//        }
//    }

}
