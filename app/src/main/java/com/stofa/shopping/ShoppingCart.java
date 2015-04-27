package com.stofa.shopping;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.Toast;
import java.util.Collections;



public class ShoppingCart extends ActionBarActivity {

    private final static String TAG = ShoppingCart.class.getSimpleName();

    private ArrayAdapter adapter;
    private ListView     listView;
    private DbUtils      dbUtils;
    private PendingIntent pendingIntent;


    public ShoppingCart() {
        dbUtils = new DbUtils();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "on Destroy called");
        try {
            dbUtils.updateDocuments();
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
                    Toast.makeText(getApplicationContext(),
                                   R.string.toast_removed_from_cart,
                                   Toast.LENGTH_SHORT).show();
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
        if (dbUtils.connectionPossible(this))
            dbUtils.loadDocuments(adapter);
    }
}
