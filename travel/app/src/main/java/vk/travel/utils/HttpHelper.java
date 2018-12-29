package vk.travel.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpHelper {

    // this method is used to create the connection and make a request to the Web
    public static String downloadUrl(String address) throws IOException {

        // input stream with the response from the Web
        InputStream inputStream = null;

        try {
            // create URL object from the passed in address String
            URL url = new URL(address);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // configure the connection
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(20000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // use connection object to open a communication link to the resource
            conn.connect();

            // get and check the response code
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("Got response code " + responseCode);
            }

            // get stream from the connection
            inputStream = conn.getInputStream();
            // pass the stream to the function to read it, and return the response string to MyIntentService()
            return readStream(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                // close stream
                inputStream.close();
            }
        }
        // if connection failed, return null to MyIntentService()
        return null;
    }

    // read stream and return string response ------------------------------------------------------
    private static String readStream(InputStream inputStream) throws IOException {

        // each portion of the data from the input stream temporarily stored in this buffer
        byte[] buffer = new byte[1024];
        // streams read and write data byte by byte
        // ArrayList can't use primitive types as byte (Byte class doesn't fit in this case)
        // Java array can use bytes, but its size can't be changed
        // So, I use ByteArrayOutputStream object just as dynamic array of primitive bytes
        // output stream in which the data is written into a byte array
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        // buffer payload length
        int length;
        // read portion of data from input stream, put it to the buffer and return the buffer payload length
        // continue the loop while the buffer can be filled by the data
        while ((length = inputStream.read(buffer)) > 0) {
            // if buffer has a data, move data to byteArray and empty the buffer
            byteArray.write(buffer, 0, length);
        }
        // convert bytes to String and return the result to MyIntentService()
        return byteArray.toString();
    }
}
