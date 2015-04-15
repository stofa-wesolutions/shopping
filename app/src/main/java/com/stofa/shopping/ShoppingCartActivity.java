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
import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


public class ShoppingCartActivity extends ActionBarActivity {

    private final static String TAG = ShoppingCartActivity.class.getSimpleName();
    public  final static String EXTRA_MESSAGE = "extra message";

    ShoppingCart cart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);
        cart = new ShoppingCart();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
        return super.onCreateOptionsMenu(menu);
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
        } else if (id == R.id.action_refresh) {
            Log.v(TAG, "action pressed ;)");
            connectToIrisCouchDB();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* called when user clicks the send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, com.stofa.shopping.DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = cart.toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException {

        String inputStreamString = new Scanner(stream, "UTF-8").useDelimiter("\\A").next();
        return inputStreamString;
    }

    private void connectToIrisCouchDB() {
        ConnectivityManager connMngr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMngr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            Log.v(TAG, "connected !!!!");
            new DownloadWebpageTask().execute("https://stofa.iriscouch.com/shopping_cart/_design/shopping/_view/articles");

        } else {
            Log.v(TAG, "error, no connection available");
        }
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.v("AFTER EXECUTE: ", result);

            try {
                JSONObject obj = new JSONObject(result);
                JSONArray  array = obj.getJSONArray("rows");

                for (int i = 0; i < array.length(); i++) {
                    String id = obj.getString("id");
                    JSONArray values = obj.getJSONArray("value");

                    Article article = new Article();
                    article.setId(id);
                    article.setRevision(values.getString(0));
                    article.setArticle(values.getString(1));
                }
            } catch (JSONException jsonException) {
                Log.e("JSON_ERROR", jsonException.toString());
            } catch (Exception e) {
                Log.e("ERROR", e.toString());
            }
            Log.v("SHOPPINGCART", cart.toString());
        }
    }

    private String downloadUrl(String myUrl) throws IOException {
        InputStream is = null;
        int len = 1024;

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

            String contentString = readIt(is, len);
            Log.v("COUCHDB RESPONSE", contentString);

            if (is != null)
                is.close();

            return contentString;

        } catch (Exception e) {
            Log.v("ERROR", "something went wrong with http request");
            return "ERROR";
        }
    }

}