package com.stofa.shopping;

/**
 * Created by stofa on 17.04.2015.
 */
public class Article {
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

    @Override
    public String toString() {
        return name;
    }
}
