package com.stofa.shopping;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

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

                if (!newArticle.getName().equals(""))
                    Listings.unusedArticles.add(newArticle);


            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();


    }
}
