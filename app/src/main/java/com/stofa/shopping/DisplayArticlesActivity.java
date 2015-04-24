package com.stofa.shopping;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
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
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;


public class DisplayArticlesActivity extends ActionBarActivity {

    private final static String TAG = ShoppingCart.class.getSimpleName();

    private ArrayAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_articles);

        adapter = new ArrayAdapter(getApplicationContext(), R.layout.delegate_cart, Listings.unusedArticles);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_articles, menu);

        return true;
    }

    private void init() {
        listView = (ListView)findViewById(R.id.listview_unused_articles);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
                Listings.moveToShoppingCart(position);
                adapter.notifyDataSetChanged();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Article delete = Listings.unusedArticles.remove(position);

                String s = "https://stofa.iriscouch.com/shopping_cart/";
                String docId = delete.getId();
                s += docId;

                if (docId != null)
                    new DeleteDocument(delete).execute(s);

                return true;

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_add_new_article) {
            // open dialog
        } else if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DeleteDocument extends AsyncTask<String, Void, Void> {
        Article toDelete;

        DeleteDocument(Article toDelete) {
            this.toDelete = toDelete;
        }

        @Override
        protected Void doInBackground(String... urls) {
            try {
                try {
                    urls[0] += "?rev=" + toDelete.getRevision();
                    URL url = new URL(urls[0]);
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

        // Reads an InputStream and converts it to a String.
        private String readIt(InputStream stream) throws IOException {
            String inputStreamString = new Scanner(stream, "UTF-8").useDelimiter("\\A").next();
            return inputStreamString;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
