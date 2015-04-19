package com.stofa.shopping;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;


public class DisplayArticlesActivity extends ActionBarActivity {

    private final static String TAG = ShoppingCart.class.getSimpleName();
    private ArrayList<Article> unusedArticles;
    private ArrayAdapter adapter;
    private ListView listView;
    private ArrayList<Article> shoppingCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_articles);

        shoppingCart = getIntent().getParcelableArrayListExtra("CART");

        init();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v("TAG", "Restart() called");
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_articles, menu);
        return true;
    }

    private void init() {
        unusedArticles = new ArrayList<Article>();
        adapter = new ArrayAdapter<>(
                getApplicationContext(), R.layout.delegate_cart, unusedArticles);
        listView = (ListView)findViewById(R.id.listview_unused_articles);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
                unusedArticles.remove(position);
                adapter.notifyDataSetChanged();
            }
        });
        connectToDatabase();
    }

    private void connectToDatabase() {
        ConnectivityManager connMngr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMngr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            Log.v(TAG, "connected !!!!");

            try {
                URL loadArticles = new URL("https://stofa.iriscouch.com/shopping_cart/_design/shopping/_view/articles_not_in_cart");
                new DownloadWebpageTask().execute(loadArticles);
            } catch (MalformedURLException mfURLe) {
                Log.e("ERROR", mfURLe.toString());
            }
        } else {
            Log.v(TAG, "error, no connection available");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DownloadWebpageTask extends AsyncTask<URL, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(URL... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0].toString());
            } catch (IOException e) {
                return new JSONObject();
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(JSONObject result) {
            Log.v("AFTER EXECUTE: ", result.toString());

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

                    unusedArticles.add(a);
                    adapter.notifyDataSetChanged();
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

        private JSONObject downloadUrl(String myUrl) throws IOException {
            InputStream is = null;

            try {
                URL url = new URL(myUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestMethod("GET");
                connection.connect();

                int response = connection.getResponseCode();
                Log.d("RESPONSE", "response of http: " + response);
                is = connection.getInputStream();

                String contentString = readIt(is);
                Log.v("COUCHDB RESPONSE", contentString);

                if (is != null)
                    is.close();

                return new JSONObject(contentString);

            } catch (Exception e) {
                Log.v("ERROR", "something went wrong with http request");
                return null;
            }
        }
    }
}
