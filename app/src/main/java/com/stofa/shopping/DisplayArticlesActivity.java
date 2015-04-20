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
    private WebConnector connector;

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
                Article putIntoCart = unusedArticles.remove(position);
                shoppingCart.add(putIntoCart);
                adapter.notifyDataSetChanged();
            }
        });
        connectToDatabase();
    }

    private void connectToDatabase() {
        try {
            URL url = new URL("https://stofa.iriscouch.com/shopping_cart/_design/shopping/_view/articles_not_in_cart");
            if (WebConnector.connectionPossible(this, url)) {
                unusedArticles.clear();
                connector = new WebConnector(adapter);
                connector.execute(url);
            }
        } catch (MalformedURLException malformedURL) {
            Log.e("MALFORMED_URL", malformedURL.toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            Intent parentIntent = getParentActivityIntent();
            parentIntent.putParcelableArrayListExtra("CART", shoppingCart);
            NavUtils.navigateUpTo(this, parentIntent);
            return true;

        } else if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
