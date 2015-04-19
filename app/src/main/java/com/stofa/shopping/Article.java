package com.stofa.shopping;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by stofa on 17.04.2015.
 */
public class Article implements Parcelable {

    String  id;
    String  revision;
    String  name;
    boolean toBuy;

    public boolean isToBuy() {
        return toBuy;
    }

    public void setToBuy(boolean toBuy) {
        this.toBuy = toBuy;
    }

    private final static String TAG  = Article.class.getSimpleName();

    public Article() {
        super();
        this.id       = null;
        this.revision = null;
        this.name     = null;
        this.toBuy    = false;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    @Override
    public int describeContents() {
        return name.hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(revision);
        dest.writeString(name);
        boolean[] toBuy = new boolean[1];
        toBuy[0] = this.toBuy;
        dest.writeBooleanArray(toBuy);
    }

    public static final Parcelable.Creator<Article> CREATOR = new Parcelable.Creator<Article>() {
        public Article createFromParcel(Parcel in) {
            Article article = new Article();

            article.setId(in.readString());
            article.setRevision(in.readString());
            article.setName(in.readString());
            boolean[] toBuy = new boolean[1];
            in.readBooleanArray(toBuy);
            article.setToBuy(toBuy[0]);

            Log.v(TAG, article.toString() + "  parceld");
            return article;
        }

        @Override
        public Article[] newArray (int size) {
            return new Article[size];
        }
    };
}
