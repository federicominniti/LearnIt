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
import javafx.scene.layout.VBox;

import java.util.List;

public class ProfilePageController {

    @FXML private Label completeNameLabel;
    @FXML private Button backToHomeButton;
    @FXML private Label birthDateLabel;
    @FXML private Label usernameLabel;
    @FXML private Label genderLabel;
    @FXML private ImageView propicImageView;
    @FXML private Button followButton;
    @FXML private Label reviewedCoursesLabel;
    @FXML private Label averageDurationLabel;
    @FXML private Label averagePriceLabel;
    @FXML private Label followingNumberLabel;
    @FXML private Label followerNumberLabel;
    @FXML private VBox elementsVBox;


    private Neo4jDriver neo4jDriver;
    private User profileUser;
    private int[]pageNumber = {0, 0, 0, 0, 0};
    private int limit;

    public void initialize() {
        neo4jDriver = Neo4jDriver.getInstance();
        limit = ConfigParams.getLocalConfig().getLimitNumber();

        backToHomeButton.setOnMouseClicked(clickEvent -> backToHomeButtonHandler(clickEvent));

        if(profileUser.getUsername().equals(Session.getLocalSession().getLoggedUser().getUsername())){ // personal profile

            followButton.setText("edit");
            followButton.setOnMouseClicked(clickEvent -> editButtonHandler(clickEvent));
        }
        else{ // another profile
            followButton.setOnMouseClicked(clickEvent -> followHandler(clickEvent));
        }

        loadProfileInformation();
        loadStatistics();
    }

    private void loadProfileInformation(){
        completeNameLabel.setText(profileUser.getCompleteName());
        usernameLabel.setText(profileUser.getUsername());
        birthDateLabel.setText(profileUser.getDateOfBirth().toString());
        genderLabel.setText(profileUser.getGender());
        followerNumberLabel.setText(neo4jDriver.getFollowStats(profileUser).get(0).toString()); // ??? ricontrollare indice
        followingNumberLabel.setText(neo4jDriver.getFollowStats(profileUser).get(1).toString()); // ??? ricontrollare indice

        if(profileUser.getProfilePic() != null){
            Image profilePicture = new Image(profileUser.getProfilePic());
            propicImageView.setImage(profilePicture);
        }

    }

    private void loadStatistics(){
        reviewedCoursesLabel.setText(Integer.toString(neo4jDriver.findTotCourses(profileUser)));
        averageDurationLabel.setText(Double.toString(neo4jDriver.findAvgStatisticOfCompletedCourses(profileUser, "duration")));
        averagePriceLabel.setText(Double.toString(neo4jDriver.findAvgStatisticOfCompletedCourses(profileUser, "price")));
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

    }

    public void backToHomeButtonHandler(MouseEvent clickEvent){
        Utils.changeScene("/DiscoveryPage.fxml", clickEvent);
    }

    public void editButtonHandler(MouseEvent clickEvent){
        Utils.changeScene("/PersonalPage.fxml", clickEvent);
    }

}
