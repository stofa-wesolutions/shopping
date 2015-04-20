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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;


public class ShoppingCart extends ActionBarActivity {

    private final static String TAG = ShoppingCart.class.getSimpleName();
    private ArrayList<Article> shoppingCart;
    private ArrayAdapter adapter;
    private ListView    listView;
    private WebConnector connector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);
        Log.v("TAG", "Create() called");
        init();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v("TAG", "Restart() called");
        init();
    }

    private void init() {
        boolean loadData = false;

        shoppingCart = getIntent().getParcelableArrayListExtra("CART");

        if (shoppingCart == null) {
            shoppingCart = new ArrayList<Article>();
            loadData = true;
        }

        adapter = new ArrayAdapter<>(
                getApplicationContext(), R.layout.delegate_cart, shoppingCart);

        listView = (ListView)findViewById(R.id.listview_cart);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                shoppingCart.remove(position);
                adapter.notifyDataSetChanged();
            }
        });

        if (loadData)
            connectToDatabase();
        else
            adapter.notifyDataSetChanged();
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
            connectToDatabase();
            return true;
        } else if (id == R.id.action_add_to_cart) {
            Intent intent = new Intent(this, DisplayArticlesActivity.class);

            intent.putParcelableArrayListExtra("CART", shoppingCart);

            startActivity(intent);
        } else if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void connectToDatabase() {
        try {
            URL url = new URL("https://stofa.iriscouch.com/shopping_cart/_design/shopping/_view/articles_in_cart");
            if (WebConnector.connectionPossible(this, url)) {
                shoppingCart.clear();
                connector = new WebConnector(adapter);
                connector.execute(url);
            }
        } catch (MalformedURLException malformedURL) {
            Log.e("MALFORMED_URL", malformedURL.toString());
        }
    }
}
