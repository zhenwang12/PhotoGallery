package com.example.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetch {
    private static final String TAG = "FLICKR_FETCH";
    private static final String API_KEY = "5496f6e98861bfe3b19503bfbb5dd0b9";

    public List<GalleryItem> fetchItem() {
        List<GalleryItem> list = new ArrayList<>();
        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.d(TAG, jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(list, jsonBody);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    private void parseItems(List<GalleryItem> list, JSONObject jsonBody) throws IOException, JSONException{
        JSONObject photosObject = jsonBody.getJSONObject("photos");
        JSONArray phtotsArray = photosObject.getJSONArray("photo");

        for (int i = 0; i < phtotsArray.length(); i++) {
            JSONObject pObject = phtotsArray.getJSONObject(i);
            GalleryItem item = new GalleryItem();

            item.setId(pObject.getString("id"));
            item.setCaption(pObject.getString("title"));

            if (!pObject.has("url_s")) {
                continue;
            }

            item.setUrl(pObject.getString("url_s"));
            list.add(item);
        }
    }

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with "
                + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            return outputStream.toByteArray();

        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }
}
