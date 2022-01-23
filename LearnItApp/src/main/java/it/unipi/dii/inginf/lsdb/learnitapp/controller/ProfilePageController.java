package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;

import java.util.List;

public class ProfilePageController {

    @FXML private Label completeNameLabel;
    @FXML private Button backToHomeButton;
    @FXML private TextField birthTextField;
    @FXML private Label usernameLabel;
    @FXML private TextField genderTextField;
    @FXML private ImageView propicImageView;
    @FXML private Button followButton;
    @FXML private TextField totReviewedCoursesTextField;
    @FXML private TextField avgDurationTextField;
    @FXML private TextField avgPriceTextField;
    @FXML private HBox likedCoursesHBox;
    @FXML private HBox reviewedCoursesHBox;
    @FXML private HBox offeredCoursesHBox;
    @FXML private HBox followerUsersHBox;
    @FXML private HBox followingUsersHBox;
    @FXML private Label followingNumberLabel;
    @FXML private Label followerNumberLabel;
    @FXML private ImageView moreLikedImageView;
    @FXML private ImageView moreReviewedImageView;
    @FXML private ImageView moreOfferedImageView;
    @FXML private ImageView moreFollowerImageView;
    @FXML private ImageView moreFollowingImageView;


    private Neo4jDriver neo4jDriver;
    private User profileUser;
    private int[]pageNumber = {0, 0, 0, 0, 0};
    private int limit;

    private final static int LIKED_COURSES = 1;
    private final static int REVIEWED_COURSES = 2;
    private final static int OFFERED_COURSES = 3;
    private final static int FOLLOWER_USERS = 4;
    private final static int FOLLOWING_USERS = 5;

    public void initialize() {
        neo4jDriver = Neo4jDriver.getInstance();
        limit = ConfigParams.getLocalConfig().getLimitNumber();

        backToHomeButton.setOnMouseClicked(clickEvent -> backToHomeButtonHandler(clickEvent));

        moreLikedImageView.setOnMouseClicked(clickEvent -> loadMore(LIKED_COURSES));
        moreReviewedImageView.setOnMouseClicked(clickEvent -> loadMore(REVIEWED_COURSES));
        moreOfferedImageView.setOnMouseClicked(clickEvent -> loadMore(OFFERED_COURSES));
        moreFollowerImageView.setOnMouseClicked(clickEvent -> loadMore(FOLLOWER_USERS));
        moreFollowingImageView.setOnMouseClicked(clickEvent -> loadMore(FOLLOWING_USERS));

        if(profileUser.getUsername().equals(Session.getLocalSession().getLoggedUser().getUsername())){ // personal profile

            followButton.setText("edit");
            followButton.setOnMouseClicked(clickEvent -> editButtonHandler(clickEvent));
        }
        else{ // another profile
            followButton.setOnMouseClicked(clickEvent -> followHandler(clickEvent));
        }

        loadProfileInformation();
        loadStatistics();
        loadCourses();
        loadUsers();
    }

    private void loadProfileInformation(){
        completeNameLabel.setText(profileUser.getCompleteName());
        usernameLabel.setText(profileUser.getUsername());
        birthTextField.setText(profileUser.getDateOfBirth().toString());
        genderTextField.setText(profileUser.getGender());
        followerNumberLabel.setText(neo4jDriver.getFollowStats(profileUser).get(0).toString()); // ??? ricontrollare indice
        followingNumberLabel.setText(neo4jDriver.getFollowStats(profileUser).get(1).toString()); // ??? ricontrollare indice

        if(profileUser.getProfilePic() != null){
            Image profilePicture = new Image(profileUser.getProfilePic());
            propicImageView.setImage(profilePicture);
        }

        birthTextField.setEditable(false);
        genderTextField.setEditable(false);
        totReviewedCoursesTextField.setEditable(false);
        avgPriceTextField.setEditable(false);
        avgDurationTextField.setEditable(false);
    }

    private void loadStatistics(){
        totReviewedCoursesTextField.setText(Integer.toString(neo4jDriver.findTotCourses(profileUser)));
        avgDurationTextField.setText(Double.toString(neo4jDriver.findAvgStatisticOfCompletedCourses(profileUser, "duration")));
        avgPriceTextField.setText(Double.toString(neo4jDriver.findAvgStatisticOfCompletedCourses(profileUser, "price")));
    }

    private void loadCourses(){
        List<Course> likedCourses = neo4jDriver.findCoursesLikedOrCompletedByUser(profileUser, false, 0, limit);
        Utils.createCoursesElements(likedCourses, likedCoursesHBox);

        List<Course> reviewedCourses = neo4jDriver.findCoursesLikedOrCompletedByUser(profileUser, true, 0, limit);
        Utils.createCoursesElements(reviewedCourses, reviewedCoursesHBox);

        List<Course> offeredCourses = neo4jDriver.findCoursesOfferedByUser(profileUser, 0, limit);
        Utils.createCoursesElements(offeredCourses, offeredCoursesHBox);
    }

    private void loadUsers(){
        List<User> followerUsers = neo4jDriver.findFollowerUsers(profileUser, 0, limit);
        Utils.createUsersElements(followerUsers, followerUsersHBox);

        List<User> followingUsers = neo4jDriver.findFollowedUsers(profileUser, 0, limit);
        Utils.createUsersElements(followingUsers, followingUsersHBox);
    }

    private void loadMore(int index){
        List<Course> courses;
        List<User> users;
        int skip = pageNumber[index-1]*limit;

        pageNumber[index-1]++;

        switch (index){
            case LIKED_COURSES:
                courses = neo4jDriver.findCoursesLikedOrCompletedByUser(profileUser, false, skip, limit);
                createCoursesElements(courses, likedCoursesHBox);
                break;
            case REVIEWED_COURSES:
                courses = neo4jDriver.findCoursesLikedOrCompletedByUser(profileUser, true, skip, limit);
                createCoursesElements(courses, reviewedCoursesHBox);
                break;
            case OFFERED_COURSES:
                courses = neo4jDriver.findCoursesOfferedByUser(profileUser, skip, limit);
                createCoursesElements(courses, offeredCoursesHBox);
                break;
            case FOLLOWER_USERS:
                users = neo4jDriver.findFollowerUsers(profileUser, skip, limit);
                createUsersElements(users, followerUsersHBox);
                break;
            default:
                users = neo4jDriver.findFollowedUsers(profileUser, skip, limit);
                createUsersElements(users, followingUsersHBox);
        }
    }

    public  void setProfileUser(User user){
        profileUser = user;
    }

    public void followHandler(MouseEvent clickEvent){
        String loggedUser = Session.getLocalSession().getLoggedUser().getUsername();

        if(neo4jDriver.isUserFollowedByUser(usernameLabel.getText(), loggedUser)){
            // Unfollow operation
            neo4jDriver.unfollowUser(Session.getLocalSession().getLoggedUser(), new User(usernameLabel.getText(), completeNameLabel.getText()));

            followButton.setText("follow");

            followingNumberLabel.setText(Integer.toString((Integer.parseInt(followingNumberLabel.getText())-1)));
        }
        else{
            // Follow operation
            neo4jDriver.followUser(Session.getLocalSession().getLoggedUser(), new User(usernameLabel.getText(), completeNameLabel.getText()));

            followButton.setText("unfollow");

            followingNumberLabel.setText(Integer.toString((Integer.parseInt(followingNumberLabel.getText())+1)));
        }

        // update of the shown list of followed users
        followingUsersHBox.getChildren().clear();

        List<User> followingUsers = neo4jDriver.findFollowedUsers(profileUser, 0, limit);
        Utils.createUsersElements(followingUsers, followingUsersHBox);

    }

    public void backToHomeButtonHandler(MouseEvent clickEvent){
        Utils.changeScene("/DiscoveryPage.fxml", clickEvent);
    }

    public void editButtonHandler(MouseEvent clickEvent){
        Utils.changeScene("/PersonalPage.fxml", clickEvent);
    }

}
