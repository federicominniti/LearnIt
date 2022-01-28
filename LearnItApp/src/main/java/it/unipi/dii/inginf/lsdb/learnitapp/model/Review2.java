package it.unipi.dii.inginf.lsdb.learnitapp.model;

import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Date;

public class Review2 {
    @BsonProperty(value = "title")
    private String title;
    @BsonProperty(value = "content")
    private String content;
    @BsonProperty(value = "rating")
    private int rating;
    @BsonProperty(value = "edited_timestamp")
    private Date timestamp;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @BsonProperty(value = "username")
    private String username;

    public Review2() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}