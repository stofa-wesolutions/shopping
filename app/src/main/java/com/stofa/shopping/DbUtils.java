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
 * Created by stofa on 25.04.2015.
 */
public class DbUtils {

    private String dbUrl;

    public DbUtils() {
        dbUrl = "https://stofa.iriscouch.com/shopping_cart/";
    }
    public void loadDocuments(ArrayAdapter<Article> adapter) {
        try {
            URL url = new URL(dbUrl + "_design/shopping/_view/all_articles");
            new WebConnector(adapter).execute(url);
        } catch (MalformedURLException malformedURL) {
            Log.e("MALFORMED_URL", malformedURL.toString());
        }
    }

    public void updateDocuments() {
        new SaveUtils().execute();
    }

    public void createDocument(Article newArticle) {
        new CreateDocument(newArticle).execute(dbUrl);
    }

    public void deleteDocument(Article deleteArticle) {
        if (deleteArticle.getId() != null)
            new DeleteDocument(deleteArticle).execute(dbUrl + deleteArticle.getId());
    }
    /* load documents from database when app starts */
    private class WebConnector extends AsyncTask<URL, Void, JSONObject> {
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
    }
    /* update existing documents in database */
    private class SaveUtils extends AsyncTask<Void, Void, JSONObject> {

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
                            if (!inShoppingCart.getRevision().equals(a.getRevision()))
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

                    if (a.isDirty()) {
                        String s = "https://stofa.iriscouch.com/shopping_cart/";
                        String id = a.getId();
                        s += id;

                        new SaveDocument(a).execute(s);
                        a.setDirty(false);
                    }
                }
            } catch (JSONException jsonException) {
                Log.e("JSON_ERROR", jsonException.toString());
            } catch (Exception e) {
                Log.e("ERROR", e.toString());
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

    /* create new document */
    private class CreateDocument extends AsyncTask<String, Void, String> {
        Article create;

        CreateDocument(Article create) {
            this.create = create;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                try {
                    URL url = new URL(urls[0]);
                    JSONObject createArticle = new JSONObject();
                    createArticle.put("article", create.getName());
                    createArticle.put("toBuy", create.isToBuy());
                    HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                    httpCon.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    httpCon.setConnectTimeout(10000);
                    httpCon.setReadTimeout(10000);
                    httpCon.setDoOutput(true);
                    httpCon.setRequestMethod("POST");
                    OutputStreamWriter out = new OutputStreamWriter(
                            httpCon.getOutputStream());
                    out.write(createArticle.toString());
                    out.close();
                    httpCon.connect();

                    Log.v("RESPONSECODE", httpCon.getResponseCode() + "   " + httpCon.getResponseMessage());
                    return readIt(httpCon.getInputStream());
                } catch (Exception e) {
                    Log.v("ERROR", e.toString());
                }
            } catch (Exception e) {
                Log.e("ERROR", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            Log.v("POSTEXECUTE", response);
            try {
                JSONObject jsonResponse = new JSONObject(response);

                create.setId(jsonResponse.getString("id"));
                create.setRevision(jsonResponse.getString("rev"));

                for (int i = 0; i < Listings.unusedArticles.size(); i++) {
                    Log.v("UNUSEDLISTING", Listings.unusedArticles.get(i).getId());
                }
            } catch (Exception e) {
                Log.e("ERROR", e.toString());
            }
        }
    }

    /* delete existing document */
    private class DeleteDocument extends AsyncTask<String, Void, Void> {
        Article toDelete;

        DeleteDocument(Article toDelete) {
            this.toDelete = toDelete;
        }

        @Override
        protected Void doInBackground(String... urls) {
            try {
                try {
                    URL url = new URL(urls[0]);
                    HttpURLConnection getConnection = (HttpURLConnection) url.openConnection();
                    getConnection.setRequestMethod("GET");
                    getConnection.setConnectTimeout(10000);
                    getConnection.setReadTimeout(10000);
                    getConnection.connect();

                    JSONObject docInDb = new JSONObject(readIt(getConnection.getInputStream()));

                    url = new URL(urls[0] + "?rev=" + docInDb.getString("_rev"));
                    HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                    httpCon.setRequestMethod("DELETE");

                    httpCon.connect();
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

    /* helper functions */
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

    public boolean connectionPossible(Context context) {
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
