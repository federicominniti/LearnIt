package it.unipi.dii.inginf.lsdb.learnitapp.model;

import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Date;

public class Review {
    @BsonProperty(value = "title")
    private String title;
    @BsonProperty(value = "content")
    private String content;
    @BsonProperty(value = "rating")
    private int rating;
    @BsonProperty(value = "edited_timestamp")
    private Date timestamp;
    @BsonProperty(value = "author")
    private User author;

    public Review() {

    }

    public Review(String title, String content, int rating, Date timestamp, User author) {
        this.title = title;
        this.content = content;
        this.rating = rating;
        this.timestamp = timestamp;
        this.author = author;
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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
}
