package com.stofa.shopping;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;


public class ShoppingCart extends ActionBarActivity {

    private final static String TAG = ShoppingCart.class.getSimpleName();

    private ArrayAdapter adapter;
    private ListView     listView;
    private WebConnector connector;

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "on Destroy called");
        try {
            new SaveUtils().execute();
        } catch (Exception e) {
            Log.e("ERROR", e.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);
        Log.v("TAG", "Create() called");

        Collections.sort(Listings.shoppingCart, Listings.comparator);
        adapter = new ArrayAdapter(getApplicationContext(), R.layout.delegate_cart, Listings.shoppingCart);

        init();
        if (!Listings.loadedFromDatabase)
            connectToDatabase();
    }

    private void init() {
        listView = (ListView)findViewById(R.id.listview_cart);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (Listings.removeFromShoppingCart(position)) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_shopping_cart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_refresh_cart) {
            Listings.shoppingCart.clear();
            Listings.unusedArticles.clear();
            Listings.loadedFromDatabase = false;
            connectToDatabase();
            return true;
        } else if (id == R.id.action_add_to_cart) {
            Intent intent = new Intent(this, DisplayArticlesActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void connectToDatabase() {
        try {
            URL url = new URL("https://stofa.iriscouch.com/shopping_cart/_design/shopping/_view/all_articles");
            if (WebConnector.connectionPossible(this)) {
                connector = new WebConnector(adapter);
                connector.execute(url);
            }
        } catch (MalformedURLException malformedURL) {
            Log.e("MALFORMED_URL", malformedURL.toString());
        }
    }
}
