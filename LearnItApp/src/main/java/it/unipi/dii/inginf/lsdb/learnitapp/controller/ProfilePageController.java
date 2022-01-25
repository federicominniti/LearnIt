package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

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
    //private int[]pageNumber = {0, 0, 0, 0, 0};
    //private int limit;

    public void initialize() {
        neo4jDriver = Neo4jDriver.getInstance();
        //limit = ConfigParams.getLocalConfig().getLimitNumber();

        backToHomeButton.setOnMouseClicked(clickEvent -> Utils.changeScene(Utils.DISCOVERY_PAGE, clickEvent));
    }

    private void loadProfileInformation(){
        completeNameLabel.setText(profileUser.getCompleteName());
        usernameLabel.setText(profileUser.getUsername());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        birthDateLabel.setText(dateFormat.format(profileUser.getDateOfBirth()));
        genderLabel.setText(profileUser.getGender());
        followerNumberLabel.setText(neo4jDriver.getFollowStats(profileUser).get(0).toString()); // ??? ricontrollare indice
        followingNumberLabel.setText(neo4jDriver.getFollowStats(profileUser).get(1).toString()); // ??? ricontrollare indice

        if(profileUser.getProfilePic() != null){
            Image profilePicture = new Image(profileUser.getProfilePic());
            propicImageView.setImage(profilePicture);
        }

        String loggedUser = Session.getLocalSession().getLoggedUser().getUsername();
        if(neo4jDriver.isUserFollowedByUser(usernameLabel.getText(), loggedUser))
            followButton.setText("Unfollow");

    }

    private void loadStatistics(){
        reviewedCoursesLabel.setText(Integer.toString(neo4jDriver.findTotCourses(profileUser)));
        DecimalFormat numberFormat = new DecimalFormat("#.00");
        double avgDuration = neo4jDriver.findAvgStatisticOfCompletedCourses(profileUser, "duration");
        double avgPrice = neo4jDriver.findAvgStatisticOfCompletedCourses(profileUser, "price");
        averageDurationLabel.setText(numberFormat.format(avgDuration));
        averagePriceLabel.setText(numberFormat.format(avgPrice));
    }

    public void setUserProfile(User user){
        profileUser = user;

        if(profileUser.getUsername().equals(Session.getLocalSession().getLoggedUser().getUsername())){ // personal profile
            followButton.setText("Edit Profile");
            followButton.setOnMouseClicked(clickEvent -> Utils.changeScene(Utils.PROFILE_PAGE, clickEvent));
        }
        else{ // another profile
            followButton.setOnMouseClicked(clickEvent -> followHandler(clickEvent));
        }

        loadProfileInformation();
        loadStatistics();
        loadSocialLists();
    }

    public void followHandler(MouseEvent clickEvent){
        String loggedUser = Session.getLocalSession().getLoggedUser().getUsername();

        if(neo4jDriver.isUserFollowedByUser(usernameLabel.getText(), loggedUser)){
            // Unfollow operation
            neo4jDriver.unfollowUser(Session.getLocalSession().getLoggedUser(),
                    new User(usernameLabel.getText(), completeNameLabel.getText()));
            followButton.setText("Follow");
            followingNumberLabel.setText(Integer.toString((Integer.parseInt(followingNumberLabel.getText())-1)));
        }
        else{
            // Follow operation
            neo4jDriver.followUser(Session.getLocalSession().getLoggedUser(),
                    new User(usernameLabel.getText(), completeNameLabel.getText()));
            followButton.setText("Unfollow");
            followingNumberLabel.setText(Integer.toString((Integer.parseInt(followingNumberLabel.getText())+1)));
        }
    }

    public void loadSocialLists(){
        Utils.addLine(elementsVBox, null, profileUser, Utils.LIKED_COURSES);
        Utils.addLine(elementsVBox, null, profileUser, Utils.REVIEWED_COURSES);
        Utils.addLine(elementsVBox, null, profileUser, Utils.OFFERED_COURSES);
        Utils.addLine(elementsVBox, null, profileUser, Utils.FOLLOWER_USERS);
        Utils.addLine(elementsVBox, null, profileUser, Utils.FOLLOWING_USERS);
    }
}
