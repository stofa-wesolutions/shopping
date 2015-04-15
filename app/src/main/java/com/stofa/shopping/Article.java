package com.stofa.shopping;

/**
 * Created by stofa on 14.04.2015.
 */
public class Article extends Document {
    String article;

    private final static String TAG  = Document.class.getSimpleName();

    public Article() {
        this.article = null;
    }

    public Article(String article) {
        this.article = article;
    }

    public String getArticle() {
        return this.article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    @Override
    public String toString() {
        return article;
    }
}
