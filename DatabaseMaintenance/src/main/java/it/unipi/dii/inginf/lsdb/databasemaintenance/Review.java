package it.unipi.dii.inginf.lsdb.databasemaintenance;

import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Date;

public class Review implements Comparable<Review> {
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public int compareTo(Review o) {
        return o.getTimestamp().compareTo(getTimestamp());
    }
}