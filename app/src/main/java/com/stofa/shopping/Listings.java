package com.stofa.shopping;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by stofa on 21.04.2015.
 */
public class Listings {
    public static ArrayList<Article>  shoppingCart   = new ArrayList<Article>();
    public static ArrayList<Article>  unusedArticles = new ArrayList<Article>();
    public static Comparator<Article> comparator     = new Comparator<Article>() {
        @Override
        public int compare(Article lhs, Article rhs) {
            return lhs.compareTo(rhs);
        }
    };

    public static boolean loadedFromDatabase = false;

    public static boolean moveToShoppingCart (int index) {
        Article unused = unusedArticles.remove(index);
        unused.setToBuy(true);
        unused.setDirty(true);
        boolean added = shoppingCart.add(unused);
        Collections.sort(unusedArticles, comparator);
        return added;
    }

    public static boolean removeFromShoppingCart (int index) {
        Article shopped = shoppingCart.remove(index);
        shopped.setToBuy(false);
        shopped.setDirty(true);
        boolean added = unusedArticles.add(shopped);
        Collections.sort(shoppingCart, comparator);
        return added;
    }
}
