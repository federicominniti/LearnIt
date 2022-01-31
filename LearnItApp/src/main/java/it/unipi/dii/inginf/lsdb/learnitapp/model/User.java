package it.unipi.dii.inginf.lsdb.learnitapp.model;

import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Date;
import java.util.List;

public class User {
    @BsonProperty(value = "username")
    private String username;
    @BsonProperty(value = "password")
    private String password;
    @BsonProperty(value = "complete_name")
    private String completeName;
    @BsonProperty(value = "birthdate")
    private Date dateOfBirth;
    @BsonProperty(value = "gender")
    private String gender;
    @BsonProperty(value = "email")
    private String email;
    @BsonProperty(value = "role")
    private Integer role;
    @BsonProperty(value = "pic")
    private String profilePic;
    @BsonProperty(value = "reviewed")
    private List<Course> reviewedCourses;
    @BsonProperty(value = "count")
    private Integer count;
    @BsonProperty(value = "avgprice")
    private Double avgPrice;
    @BsonProperty(value = "avgduration")
    private Double avgDuration;

    public User() {

    }

    public User(String username, String profilePic, String gender) {

        this.username = username;

        if(profilePic!=null)
            this.profilePic = profilePic;

        if(gender != null)
            this.gender = gender;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCompleteName() {
        return completeName;
    }

    public void setCompleteName(String completeName) {
        this.completeName = completeName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public List<Course> getReviewedCourses() {
        return reviewedCourses;
    }

    public void setReviewedCourses(List<Course> reviewedCourses) {
        this.reviewedCourses = reviewedCourses;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Double getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(Double avgPrice) {
        this.avgPrice = avgPrice;
    }

    public Double getAvgDuration() {
        return avgDuration;
    }

    public void setAvgDuration(Double avgDuration) {
        this.avgDuration = avgDuration;
    }
}
