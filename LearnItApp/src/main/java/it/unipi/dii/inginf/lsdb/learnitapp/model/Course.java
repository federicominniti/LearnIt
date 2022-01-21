package it.unipi.dii.inginf.lsdb.learnitapp.model;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class Course {

    @BsonId
    private ObjectId id;
    @BsonProperty(value = "title")
    private String title;
    @BsonProperty(value = "description")
    private String description;
    @BsonProperty(value = "instructors")
    private List<User> instructors;
    @BsonProperty(value = "language")
    private String language;
    @BsonProperty(value = "category")
    private List<String> category;
    @BsonProperty(value = "level")
    private String level;
    @BsonProperty(value = "duration")
    private double duration;
    @BsonProperty(value = "price")
    private double price;
    @BsonProperty(value = "link")
    private String link;
    @BsonProperty(value = "modality")
    private String modality;
    @BsonProperty(value = "reviews")
    private List<Review> reviews;
    @BsonProperty(value="num_reviews")
    private int num_reviews;
    @BsonProperty(value="sum_ratings")
    private int sum_ratings;

    public Course(ObjectId id, String title, String description, List<User> instructors, String language, List<String> category, String level, double duration, double price, String link, String modality, List<Review> reviews, int num_reviews, int sum_ratings) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instructors = instructors;
        this.language = language;
        this.category = category;
        this.level = level;
        this.duration = duration;
        this.price = price;
        this.link = link;
        this.modality = modality;
        this.reviews = reviews;
        this.num_reviews = num_reviews;
        this.sum_ratings = sum_ratings;
    }

    public Course(String title) {
        this.title = title;
    }

    public Course(String title, String description, List<User> instructors, String language, List<String> category, String level, double duration, double price, String link, String modality, List<Review> reviews, int num_reviews, int sum_ratings) {
        this.title = title;
        this.description = description;
        this.instructors = instructors;
        this.language = language;
        this.category = category;
        this.level = level;
        this.duration = duration;
        this.price = price;
        this.link = link;
        this.modality = modality;
        this.reviews = reviews;
        this.num_reviews = num_reviews;
        this.sum_ratings = sum_ratings;
    }

    public Course(ObjectId id, String title, double duration, double price) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.price = price;
    }

    public Course() {

    }

    public Course(ObjectId oid, String title, String description, List<User> instructors, String language, List<String> category, String level, double duration, double price, String link, String modality, int num_reviews, int sum_ratings) {
        this(oid, title, description, instructors, language, category, level, duration, price, link, modality, new ArrayList<Review>(), num_reviews, sum_ratings);
    }

    public Course(ObjectId oid, String title, String description, List<User> instructors, String language, String level, double duration, double price, String link, String modality, List<Review> reviews, int num_reviews, int sum_ratings) {
        this(oid, title, description, instructors, language, new ArrayList<String>(), level, duration, price, link, modality, reviews, num_reviews, sum_ratings);
    }

    public Course(ObjectId oid, String title, String description, List<User> instructors, String language, String level, double duration, double price, String link, String modality, int num_reviews, int sum_ratings) {
        this(oid, title, description, instructors, language, new ArrayList<String>(), level, duration, price, link, modality, new ArrayList<Review>(), num_reviews, sum_ratings);
    }

    public void addReview(Review review) {
        this.reviews.add(review);
    }

    public void addInstructor(User user) {
        this.instructors.add(user);
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void addCategory(String category) {
        this.category.add(category);
    }

    public void deleteReview(Review review) {
        this.reviews.remove(review);
    }

    public void deleteInstructor(User user) {
        this.instructors.remove(user);
    }

    public void deleteCategory(String category) {
        this.category.remove(category);
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<User> getInstructors() {
        return instructors;
    }

    public void setInstructors(List<User> instructors) {
        this.instructors = instructors;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<String> getCategory() {
        return category;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public int getSum_ratings() {
        return sum_ratings;
    }

    public void setSum_ratings(int sum_ratings) {
        this.sum_ratings = sum_ratings;
    }

    public int getNum_reviews() {
        return num_reviews;
    }

    public void setNum_reviews(int num_reviews) {
        this.sum_ratings = num_reviews;
    }
}
