package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    private Neo4jDriver neo4jDriver;
    private User profileUser;

    public void initialize() {
        neo4jDriver = Neo4jDriver.getInstance();
        backToHomeButton.setOnMouseClicked(clickEvent -> backToHomeButtonHandler(clickEvent));

        if(profileUser.getUsername().equals(Session.getLocalSession().getLoggedUser().getUsername())){
            // personal profile
            completeNameLabel.setText(Session.getLocalSession().getLoggedUser().getCompleteName());

            followButton.setText("edit");
            followButton.setOnMouseClicked(clickEvent -> editButtonHandler(clickEvent));
        }
        else{
            // another profile
            followButton.setOnMouseClicked(clickEvent -> followHandler(clickEvent));
        }

        loadProfileInformation();

        loadStatistics();

        loadCourses();

    }

    private void loadProfileInformation(){
        completeNameLabel.setText(profileUser.getCompleteName());
        usernameLabel.setText(profileUser.getUsername());
        birthTextField.setText(profileUser.getDateOfBirth().toString());
        genderTextField.setText(profileUser.getGender());
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

        List<Course> likedCourses = neo4jDriver.findCoursesLikedOrCompletedByUser(profileUser.getUsername(), false);
        Utils.createCoursesElements(likedCourses, likedCoursesHBox);

        List<Course> reviewedCourses = neo4jDriver.findCoursesLikedOrCompletedByUser(profileUser.getUsername(), true);
        Utils.createCoursesElements(reviewedCourses, reviewedCoursesHBox);
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
        }
        else{
            // Follow operation
            neo4jDriver.followUser(Session.getLocalSession().getLoggedUser(), new User(usernameLabel.getText(), completeNameLabel.getText()));

            followButton.setText("unfollow");
        }
    }

    public void backToHomeButtonHandler(MouseEvent clickEvent){
        Utils.changeScene("/DiscoveryPage.fxml", clickEvent);
    }

    public void editButtonHandler(MouseEvent clickEvent){
        Utils.changeScene("/PersonalPage.fxml", clickEvent);
    }


}
