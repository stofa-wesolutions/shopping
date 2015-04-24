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
import java.util.Scanner;

/**
 * Created by stofa on 24.04.2015.
 */
public class SaveUtils extends AsyncTask<Void, Void, JSONObject> {

    public SaveUtils() { }

    @Override
    protected JSONObject doInBackground(Void... params) {
        try {
            URL url = new URL("https://stofa.iriscouch.com/shopping_cart/_design/shopping/_view/all_articles");
            return downloadUrl(url);
        } catch (MalformedURLException malformedURL) {
            Log.e("ERROR", malformedURL.toString());
        } catch (Exception e) {
            Log.e("ERROR", e.toString());
        }
        return null;
    }
    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            JSONArray array = result.getJSONArray("rows");
            ArrayList<Article> saveArticles = new ArrayList<Article>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject article = array.getJSONObject(i);

                String id = article.getString("id");
                JSONArray values = article.getJSONArray("value");

                Article a = new Article();

                a.setId(id);
                a.setRevision(values.getString(0));
                a.setName(values.getString(1));
                a.setToBuy(values.getBoolean(2));

                boolean found = false;
                for (int j = 0; j < Listings.shoppingCart.size(); j++) {
                    Article inShoppingCart = Listings.shoppingCart.get(j);

                    if (inShoppingCart.getId().equals(a.getId())) {
                        if(!inShoppingCart.getRevision().equals(a.getRevision()))
                            inShoppingCart.setRevision(a.getRevision());
                        found = true;
                        saveArticles.add(inShoppingCart);
                        break;
                    }
                }

                for (int j = 0; j < Listings.unusedArticles.size() && !found; j++) {
                    Article unused = Listings.unusedArticles.get(j);

                    if (unused.getId().equals(a.getId())) {
                        if (!unused.getRevision().equals(a.getRevision()))
                            unused.setRevision(a.getRevision());
                        saveArticles.add(unused);
                        break;
                    }
                }
            }

            for (int i = 0; i < saveArticles.size(); i++) {
                Article a = saveArticles.get(i);

                String s = "https://stofa.iriscouch.com/shopping_cart/";
                String id = a.getId();
                s += id;

                new SaveDocument(a).execute(s);

            }
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

    private class SaveDocument extends AsyncTask<String, Void, Void> {
        Article toSave;

        SaveDocument(Article toSave) {
            this.toSave = toSave;
        }

        @Override
        protected Void doInBackground(String... urls) {
            try {
                try {
                    URL url = new URL(urls[0]);
                    HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                    httpCon.setDoOutput(true);
                    httpCon.setRequestMethod("PUT");
                    OutputStreamWriter out = new OutputStreamWriter(
                            httpCon.getOutputStream());
                    out.write(toSave.toJSON().toString());
                    out.close();
                    InputStream is = httpCon.getInputStream();
                    Log.v("STREAM", readIt(is));
                } catch (Exception e) {
                    Log.v("ERROR", e.toString());
                }
            } catch (Exception e) {
                Log.e("ERROR", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
