package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.DBOperations;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class ProfilePageController {

    @FXML private Label completeNameLabel;
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
    @FXML private ImageView learnItLabel;
    @FXML private VBox userInfoVBox;
    @FXML private BorderPane profileContentBorderPane;
    @FXML private BorderPane userBorderPane;
    @FXML private VBox statisticsVBox;

    private static final String PERSONAL_PAGE = "/fxml/PersonalPage.fxml";

    private Neo4jDriver neo4jDriver;
    private User profileUser;
    private User loggedUser;
    private boolean isProfileMine;
    private int limit;

    public void initialize() {
        neo4jDriver = Neo4jDriver.getInstance();
        limit = ConfigParams.getLocalConfig().getLimitNumber();
        learnItLabel.setOnMouseClicked(clickEvent -> Utils.changeScene(Utils.DISCOVERY_PAGE, clickEvent));
        learnItLabel.setCursor(Cursor.HAND);
    }

    private void loadProfileInformation(){
        completeNameLabel.setText(profileUser.getCompleteName());
        usernameLabel.setText(profileUser.getUsername());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        if (profileUser.getDateOfBirth() != null)
            birthDateLabel.setText(dateFormat.format(profileUser.getDateOfBirth()));

        if (profileUser.getGender() != null)
            genderLabel.setText(profileUser.getGender());

        followerNumberLabel.setText(neo4jDriver.getFollowStats(profileUser).get(0).toString()); // ??? ricontrollare indice
        followingNumberLabel.setText(neo4jDriver.getFollowStats(profileUser).get(1).toString()); // ??? ricontrollare indice

        if(profileUser.getProfilePic() != null){
            Image profilePicture = new Image(profileUser.getProfilePic());
            propicImageView.setImage(profilePicture);
        }

        if(neo4jDriver.isUserFollowedByUser(usernameLabel.getText(), loggedUser.getUsername()))
            followButton.setText("Unfollow");

    }

    private void loadStatistics() {
        int totCourses = neo4jDriver.findTotCourses(profileUser);
        reviewedCoursesLabel.setText("Reviewed courses: " + totCourses);
        DecimalFormat numberFormat = new DecimalFormat("#.00");
        if (totCourses > 0) {
            double avgDuration = neo4jDriver.findAvgStatisticOfCompletedCourses(profileUser, "duration");
            double avgPrice = neo4jDriver.findAvgStatisticOfCompletedCourses(profileUser, "price");
            averageDurationLabel.setText("Average hours spent learning: " + numberFormat.format(avgDuration));
            averagePriceLabel.setText("Average price of reviewed courses: " + numberFormat.format(avgPrice));
        }

    }

    public void setProfileUser(User user){
        loggedUser = Session.getLocalSession().getLoggedUser();
        profileUser = user;
        isProfileMine = loggedUser.getUsername().equals(profileUser.getUsername());

        if (loggedUser.getRole() == User.Role.ADMINISTRATOR) {
            profileContentBorderPane.getChildren().remove(statisticsVBox);
            profileContentBorderPane.getChildren().remove(userBorderPane);
            profileContentBorderPane.setPrefWidth(923);
            profileContentBorderPane.setPrefHeight(326);

            ImageView adminImage = new ImageView(
                    String.valueOf(PersonalPageController.class.getResource("/img/createAdmin.png")));
            adminImage.setPreserveRatio(true);
            adminImage.setFitHeight(100);
            adminImage.setFitHeight(100);
            profileContentBorderPane.setLeft(adminImage);
            BorderPane.setAlignment(adminImage, Pos.CENTER);

            VBox passwordVBox = new VBox();
            passwordVBox.setAlignment(Pos.CENTER);
            passwordVBox.setSpacing(20);

            GridPane modifyPasswordGridPane = new GridPane();
            modifyPasswordGridPane.setAlignment(Pos.CENTER);
            modifyPasswordGridPane.setHgap(15);
            modifyPasswordGridPane.setVgap(15);
            passwordVBox.getChildren().add(modifyPasswordGridPane);

            Label oldPasswordLabel = new Label("Old password:");
            PasswordField oldPasswordField = new PasswordField();
            Label newPasswordLabel = new Label("New password:");
            TextField newPasswordTextField = new TextField();
            Label repeatPasswordLabel = new Label("Repeat password:");
            TextField repeatPasswordTextField = new TextField();

            modifyPasswordGridPane.add(oldPasswordLabel, 0, 0);
            modifyPasswordGridPane.add(oldPasswordField, 1, 0);
            modifyPasswordGridPane.add(newPasswordLabel, 0, 1);
            modifyPasswordGridPane.add(newPasswordTextField, 1, 1);
            modifyPasswordGridPane.add(repeatPasswordLabel, 0, 2);
            modifyPasswordGridPane.add(repeatPasswordTextField, 1, 2);

            Button modifyButton = new Button("Modify");
            modifyButton.setStyle("-fx-background-color: lightpink;" +
                    "-fx-background-radius: 13px");

            modifyButton.setOnMouseClicked(clickEvent -> modifyAdminPasswordHandler(oldPasswordField,
                    newPasswordTextField, repeatPasswordTextField));
            passwordVBox.getChildren().add(modifyButton);
            profileContentBorderPane.setRight(passwordVBox);
            BorderPane.setAlignment(passwordVBox, Pos.CENTER);
            //userInfoVBox.getChildren().remove(followButton);
            //followButton.setText("Delete user");
            //ImageView trashBin = new ImageView(new Image(
            //        String.valueOf(ProfilePageController.class.getResource(Utils.TRASH_BIN))));
            //trashBin.setPreserveRatio(true);
            //trashBin.setFitWidth(40);
            //trashBin.setFitHeight(40);
            //trashBin.setOnMouseClicked(clickEvent -> deleteUserHandler(profileUser, clickEvent));
            //trashBin.setCursor(Cursor.HAND);
            //userInfoVBox.getChildren().add(trashBin);
        }
        else if(isProfileMine){ // personal profile
            followButton.setText("Edit Profile");
            followButton.setOnMouseClicked(clickEvent -> Utils.changeScene(PERSONAL_PAGE, clickEvent));
            followButton.setCursor(Cursor.HAND);
        }
        else{ // another profile
            followButton.setOnMouseClicked(clickEvent -> followHandler(clickEvent));
            followButton.setCursor(Cursor.HAND);
        }

        loadProfileInformation();
        loadStatistics();
        loadSocialLists();
    }

    public void modifyAdminPasswordHandler(PasswordField oldPasswordField, TextField newPasswordTextField,
                                           TextField repeatPasswordTextField){

        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordTextField.getText();
        String repeatPassword = repeatPasswordTextField.getText();

        //if(! QUERY MONGO PER VERIFICA OLD PASSWORD CORRETTA)
            //Utils.showErrorAlert("Old password is wrong!!");

        if(newPassword.equals(repeatPassword))
            //QUERY DI MODIFICA PASSWORD UTENTE
            Utils.showInfoAlert("Password modified!");

        oldPasswordField.setText("");
        newPasswordTextField.setText("");
        repeatPasswordTextField.setText("");
    }

    public void deleteUserHandler(User profileUser, MouseEvent clickEvent) {
        if (DBOperations.deleteUser(profileUser)) {
            Utils.showInfoAlert("User deleted successfully");
            Utils.changeScene(Utils.DISCOVERY_PAGE, clickEvent);
        }
        else
            Utils.showErrorAlert("Something has gone wrong");
    }

    public void followHandler(MouseEvent clickEvent){
        String loggedUser = Session.getLocalSession().getLoggedUser().getUsername();

        if(neo4jDriver.isUserFollowedByUser(usernameLabel.getText(), loggedUser)){
            // Unfollow operation
            neo4jDriver.unfollowUser(Session.getLocalSession().getLoggedUser(),
                    new User(usernameLabel.getText(), completeNameLabel.getText()));
            followButton.setText("Follow");
            followerNumberLabel.setText(Integer.toString((Integer.parseInt(followerNumberLabel.getText())-1)));
        }
        else{
            // Follow operation
            neo4jDriver.followUser(Session.getLocalSession().getLoggedUser(),
                    new User(usernameLabel.getText(), completeNameLabel.getText()));
            followButton.setText("Unfollow");
            followerNumberLabel.setText(Integer.toString((Integer.parseInt(followerNumberLabel.getText())+1)));
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
