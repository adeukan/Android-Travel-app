package vk.travel.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import vk.travel.model.Poi;

public class ImageCacheManager {

    // try to get a programmer photo from the cache ------------------------------------------------
    public static Bitmap getPhotoFromCache(Context context, Poi prog) {

//        // get the path to caching directory and construct the full pathname to a desired image file
//        String pathName = context.getCacheDir() + "/" + prog.getPhoto();
//
//        // create a File object with an abstract pathname
//        File file = new File(pathName);
//
//        // test whether the file denoted by the abstract pathname exists
//        if (file.exists()) {
//            try {
//                // create an input stream from the file, decode it, and return as a Bitmap object
//                return BitmapFactory.decodeStream(new FileInputStream(file));
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
        // if the file denoted by the abstract pathname doesn't exist, return null
        return null;
    }

    // compress and put a programmer photo to the cache --------------------------------------------
    public static void putPhotoToCache(Context context, Poi prog, Bitmap photoBitmap) {

//        // get the path to caching directory and construct the full pathname for the file to be saved
//        String pathName = context.getCacheDir() + "/" + prog.getPhoto();
//
//        // create a File object with an abstract pathname
//        File file = new File(pathName);
//
//        // declare the output stream outside the try/catch block
//        FileOutputStream outputStream = null;
//
//        try {
//            // initialise the output stream to the file (with abstract pathname)
//            outputStream = new FileOutputStream(file);
//
//            // write a compressed version of the bitmap to the specified output stream (and then to the cache)
//            // JPEG allows you to significantly reduce the file size
//            photoBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (outputStream != null) {
//                try {
//                    // close the stream to avoid leaks
//                    outputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }
}
