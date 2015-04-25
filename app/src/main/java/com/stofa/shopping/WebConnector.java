package com.stofa.shopping;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * Created by stofa on 20.04.2015.
 */
public class WebConnector extends AsyncTask<URL, Void, JSONObject> {

    ArrayAdapter<Article> adapter;

    public WebConnector(ArrayAdapter<Article> adapter) {
        this.adapter = adapter;
    }

    @Override
    protected JSONObject doInBackground(URL... urls) {
        // params comes from the execute() call: params[0] is the url.
        try {
            return downloadUrl(urls[0]);
        } catch (IOException e) {
            return new JSONObject();
        }
    }
    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            JSONArray array = result.getJSONArray("rows");

            for (int i = 0; i < array.length(); i++) {
                JSONObject article = array.getJSONObject(i);

                String id = article.getString("id");
                JSONArray values = article.getJSONArray("value");

                Article a = new Article();

                a.setId(id);
                a.setRevision(values.getString(0));
                a.setName(values.getString(1));
                a.setToBuy(values.getBoolean(2));

                if (a.isToBuy())
                    Listings.shoppingCart.add(a);
                else
                    Listings.unusedArticles.add(a);
            }
            Listings.loadedFromDatabase = true;
            Collections.sort(Listings.shoppingCart, Listings.comparator);
            adapter.notifyDataSetChanged();

        } catch (JSONException jsonException) {
            Log.e("JSON_ERROR", jsonException.toString());
        } catch (Exception e) {
            Log.e("ERROR", e.toString());
        }
    }

    // Reads an InputStream and converts it to a String.
    private String readIt(InputStream stream) throws IOException {
        String inputStreamString = new Scanner(stream, "UTF-8").useDelimiter("\\A").next();
        return inputStreamString;
    }

    private JSONObject downloadUrl(URL myUrl) throws IOException {
        InputStream is = null;

        try {
            HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod("GET");
            connection.connect();

            is = connection.getInputStream();

            String contentString = readIt(is);
            if (is != null)
                is.close();

            return new JSONObject(contentString);

        } catch (Exception e) {
            Log.v("ERROR", "something went wrong with http request");
            return null;
        }
    }

    public static boolean connectionPossible(Context context) {
        ConnectivityManager connMngr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMngr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
}
