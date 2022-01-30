package it.unipi.dii.inginf.lsdb.learnitapp.model;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.List;

public class Course2 {
    @BsonId
    private ObjectId id;
    @BsonProperty(value = "title")
    private String title;
    @BsonProperty(value = "description")
    private String description;
    @BsonProperty(value = "instructor")
    private String instructor;
    @BsonProperty(value = "language")
    private String language;
    @BsonProperty(value = "category")
    private List<String> category;
    @BsonProperty(value = "level")
    private String level;
    @BsonProperty(value = "duration")
    private Double duration;
    @BsonProperty(value = "price")
    private Double price;
    @BsonProperty(value = "link")
    private String link;
    @BsonProperty(value = "modality")
    private String modality;
    @BsonProperty(value = "reviews")
    private List<Review2> reviews;
    @BsonProperty(value="num_reviews")
    private Integer num_reviews;
    @BsonProperty(value="sum_ratings")
    private Integer sum_ratings;
    @BsonProperty(value="course_pic")
    private String coursePic;
    @BsonProperty(value="new_price")
    private Integer newPrice;
    @BsonProperty(value="year")
    private Integer year;

    public Course2() {

    }

    public Course2(String title, Double duration, Double price, String course_pic){ // for snapshots
        this.title = title;

        if(duration!=null)
            this.duration = duration;

        if(price!=null)
            this.price = price;

        if(course_pic != null)
            this.coursePic = course_pic;

    }

    public Course2(String title, String description, String instructor, String language, String level,
                   Double duration, Double price, String link, List<String> category, String modality, String coursePic){
        this.title = title;
        this.description = description;
        this.instructor = instructor;
        if(language != null)
            this.language = language;
        this.level = level;
        if(duration!=null)
            this.duration = duration;
        if(price!=null)
            this.price = price;
        if(link!=null)
            this.link = link;
        if(category!=null){
            if(category.size()>0)
                this.category = category;
        }
        if(modality!=null)
            this.modality = modality;
        if(coursePic!=null)
            this.coursePic = coursePic;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
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

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
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

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
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

    public List<Review2> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review2> reviews) {
        this.reviews = reviews;
    }

    public Integer getNum_reviews() {
        return num_reviews;
    }

    public void setNum_reviews(Integer num_reviews) {
        this.num_reviews = num_reviews;
    }

    public Integer getSum_ratings() {
        return sum_ratings;
    }

    public void setSum_ratings(Integer sum_ratings) {
        this.sum_ratings = sum_ratings;
    }

    public String getCoursePic() {
        return coursePic;
    }

    public void setCoursePic(String coursePic) {
        this.coursePic = coursePic;
    }

    public Integer getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(Integer newPrice) {
        this.newPrice = newPrice;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
