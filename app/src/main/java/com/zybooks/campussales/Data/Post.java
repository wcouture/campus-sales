package com.zybooks.campussales.Data;

import android.graphics.Bitmap;
import android.media.Image;

public class Post {
    private int price;
    private String title;
    private String description;
    private long author_id;
    private long post_id;
    private Bitmap img;

    public Post(int price, String title, String description, long author_id, long post_id, Bitmap bmp){
        this.price = price;
        this.title = title;
        this.description = description;
        this.author_id = author_id;
        this.post_id = post_id;
        this.img = bmp;
    }

    public int getPrice() { return price; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getAuthor_id() { return author_id; }
    public long getPost_id() { return post_id; }
    public Bitmap getImg() { return img; }
    public void setImg(Bitmap bmp) { img = bmp; }

    @Override
    public String toString() {
        String output = "Post: [ " + title + " | " + description + " | " + price + " ]";
        return output;
    }
}
