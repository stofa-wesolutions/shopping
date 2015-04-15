package com.stofa.shopping;

import java.util.ArrayList;

/**
 * Created by stofa on 15.04.2015.
 */
public class ShoppingCart extends Document {
    ArrayList<Article> cart;

    private final static String TAG = Document.class.getSimpleName();

    public ShoppingCart() {
        cart = new ArrayList<Article>();
    }

    public ShoppingCart(ArrayList<Article> articles) {
        cart = (ArrayList)articles;
    }

    public boolean addArticle (Article newArticle) {
        return cart.add(newArticle);
    }

    @Override
    public String toString() {
        String shoppingCart = "";

        for (int i = 0; i < cart.size(); i++) {
            shoppingCart += cart.get(i).toString() + "\n";
        }

        return shoppingCart;
    }
}
