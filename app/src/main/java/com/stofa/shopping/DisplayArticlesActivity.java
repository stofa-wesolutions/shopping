package com.stofa.shopping;


import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.Collections;

public class DisplayArticlesActivity extends ActionBarActivity {

    private final static String TAG = ShoppingCart.class.getSimpleName();

    private ArrayAdapter adapter;
    private ListView listView;
    private DbUtils  dbUtils;

    public DisplayArticlesActivity() {
        dbUtils = new DbUtils();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_articles);

        Collections.sort(Listings.unusedArticles, Listings.comparator);
        adapter = new ArrayAdapter(getApplicationContext(), R.layout.delegate_cart, Listings.unusedArticles);
        init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "on Stop called");
        try {
            dbUtils.updateDocuments();
        } catch (Exception e) {
            Log.e("ERROR", e.toString());
        }
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
                Toast.makeText(getApplicationContext(),
                               R.string.toast_moved_to_cart,
                               Toast.LENGTH_SHORT).show();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Article delete = Listings.unusedArticles.remove(position);
                adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(),
                               R.string.toast_deleted,
                               Toast.LENGTH_SHORT).show();
                String docId = delete.getId();

                if (docId != null)
                    dbUtils.deleteDocument(delete);

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
            AddArticleDialog dialog = new AddArticleDialog(this);

            dialog.show(this.getSupportFragmentManager(), "addArticle");

        } else if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
