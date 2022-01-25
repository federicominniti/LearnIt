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
    @BsonProperty(value = "instructor")
    private User instructor;
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
    @BsonProperty(value="course_pic")
    private String coursePic;
    @BsonProperty(value="year")
    private int year;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Course(ObjectId id, String title, String description, User instructor, String language, List<String> category,
                  String level, double duration, double price, String link, String modality, List<Review> reviews,
                  int num_reviews, int sum_ratings, String coursePic) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instructor = instructor;
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
        this.coursePic = coursePic;
    }

    public Course(String title) {
        this.title = title;
    }

    public Course(ObjectId id) { this.id = id; }

    public Course(String title, String description, User instructor, String language, List<String> category, String level,
                  double duration, double price, String link, String modality, List<Review> reviews, int num_reviews,
                  int sum_ratings, String coursePic) {
        this.title = title;
        this.description = description;
        this.instructor = instructor;
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
        this.coursePic = coursePic;
    }

    public Course(ObjectId id, String title, double duration, double price) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.price = price;
    }

    public Course(String title, double duration, double price, String coursePic) {
        this.title = title;
        this.duration = duration;
        this.price = price;
        this.coursePic = coursePic;
    }

    public Course() {

    }

    public Course(ObjectId oid, String title, String description, User instructor, String language, List<String> category,
                  String level, double duration, double price, String link, String modality, int num_reviews,
                  int sum_ratings, String coursePic) {
        this(oid, title, description, instructor, language, category, level, duration, price, link, modality,
                new ArrayList<Review>(), num_reviews, sum_ratings, coursePic);
    }

    public Course(ObjectId oid, String title, String description, User instructor, String language, String level,
                  double duration, double price, String link, String modality, List<Review> reviews, int num_reviews,
                  int sum_ratings, String coursePic) {
        this(oid, title, description, instructor, language, new ArrayList<String>(), level, duration, price, link,
                modality, reviews, num_reviews, sum_ratings, coursePic);
    }

    public Course(ObjectId oid, String title, String description, User instructor, String language, String level,
                  double duration, double price, String link, String modality, int num_reviews, int sum_ratings,
                  String coursePic) {
        this(oid, title, description, instructor, language, new ArrayList<String>(), level, duration, price, link,
                modality, new ArrayList<Review>(), num_reviews, sum_ratings, coursePic);
    }

    public Course(String title, String description, User instructor, String language, String level,
                  double duration, double price, String link, String modality, String coursePic) {
        this(title, description, instructor, language, new ArrayList<String>(), level, duration, price, link,
                modality, new ArrayList<Review>(), 0, 0, coursePic);
    }

    public Course(String title, String description, User instructor, String language, List<String> category, String level,
                  double duration, double price, String link, String modality, String coursePic) {
        this(title, description, instructor, language, category, level, duration, price, link,
                modality, new ArrayList<Review>(), 0, 0, coursePic);
    }

    public void addReview(Review review) {
        this.reviews.add(review);
    }

    public void setInstructor(User user) {
        this.instructor = user;
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

    public User getInstructor() {
        return instructor;
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
        this.num_reviews = num_reviews;
    }

    public String  getCoursePic() {
        return coursePic;
    }

    public void setCoursePic(String coursePic) {
        this.coursePic = coursePic;
    }
}
