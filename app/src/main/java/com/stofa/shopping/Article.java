package com.stofa.shopping;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by stofa on 17.04.2015.
 */
public class Article {

    String  id;
    String  revision;
    String  name;
    boolean toBuy;
    boolean dirty;

    private final static String TAG  = Article.class.getSimpleName();

    public Article() {
        super();
        this.id       = null;
        this.revision = null;
        this.name     = null;
        this.toBuy    = false;
        this.dirty    = false;
    }

    public boolean isToBuy() {
        return toBuy;
    }

    public void setToBuy(boolean toBuy) {
        this.toBuy = toBuy;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
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

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put ("_rev", revision);
            jsonObject.put ("article", name);
            jsonObject.put ("toBuy", toBuy);
        } catch (JSONException jsonExc) {
            Log.e("JSON_EXCEPTION", jsonExc.toString());
        }
        return jsonObject;
    }

    public int compareTo(Article compareTo) {
        return name.compareTo(compareTo.getName());
    }
}
