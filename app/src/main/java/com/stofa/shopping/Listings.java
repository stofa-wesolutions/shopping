package com.stofa.shopping;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by stofa on 21.04.2015.
 */
public class Listings {
    public static ArrayList<Article> shoppingCart   = new ArrayList<Article>();
    public static ArrayList<Article> unusedArticles = new ArrayList<Article>();
    public static boolean loadedFromDatabase = false;

    public static boolean moveToShoppingCart (int index) {
        Article unused = unusedArticles.remove(index);
        unused.setToBuy(true);
        return shoppingCart.add(unused);
    }

    public static boolean removeFromShoppingCart (int index) {
        Article shopped = shoppingCart.remove(index);
        shopped.setToBuy(false);
        return unusedArticles.add(shopped);
    }
}
