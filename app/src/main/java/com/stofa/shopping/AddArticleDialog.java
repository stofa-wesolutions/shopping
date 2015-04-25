package com.stofa.shopping;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Scanner;

/**
 * Created by stofa on 24.04.2015.
 */
public class AddArticleDialog extends DialogFragment {
    Activity activity;
    View view;

    AddArticleDialog(Activity activity) {
        this.activity = activity;
        view = null;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        view = (View)inflater.inflate(R.layout.add_article_layout, null);
        builder.setView(view);

        builder.setMessage(R.string.add_article_title);

        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Article newArticle = new Article();

                EditText enterName = (EditText)view.findViewById(R.id.article_name);

                newArticle.setName(enterName.getText().toString());
                newArticle.setToBuy(false);

                if (!newArticle.getName().equals("")) {
                    Listings.unusedArticles.add(newArticle);
                    Collections.sort(Listings.unusedArticles, Listings.comparator);
                    new CreateDocument(newArticle).execute("https://stofa.iriscouch.com/shopping_cart/");
                }


            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();


    }

    private class CreateDocument extends AsyncTask<String, Void, String> {
        Article create;

        CreateDocument(Article create) {
            this.create = create;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                try {
                    URL url = new URL(urls[0]);
                    JSONObject createArticle = new JSONObject();
                    createArticle.put("article", create.getName());
                    createArticle.put("toBuy", create.isToBuy());
                    HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                    httpCon.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    httpCon.setConnectTimeout(10000);
                    httpCon.setReadTimeout(10000);
                    httpCon.setDoOutput(true);
                    httpCon.setRequestMethod("POST");
                    OutputStreamWriter out = new OutputStreamWriter(
                            httpCon.getOutputStream());
                    out.write(createArticle.toString());
                    out.close();
                    httpCon.connect();
                    //InputStream is = httpCon.getInputStream();
                    //Log.v("STREAM", readIt(is));
                    //return readIt(is);
                    Log.v("RESPONSECODE", httpCon.getResponseCode() + "   " + httpCon.getResponseMessage());
                    return readIt(httpCon.getInputStream());
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
        protected void onPostExecute(String response) {
            Log.v("POSTEXECUTE", response);
            try {
                JSONObject jsonResponse = new JSONObject(response);

                create.setId(jsonResponse.getString("id"));
                create.setRevision(jsonResponse.getString("rev"));

                for (int i = 0; i < Listings.unusedArticles.size(); i++) {
                    Log.v("UNUSEDLISTING", Listings.unusedArticles.get(i).getId());
                }
            } catch (Exception e) {
                Log.e("ERROR", e.toString());
            }
        }
    }
}
