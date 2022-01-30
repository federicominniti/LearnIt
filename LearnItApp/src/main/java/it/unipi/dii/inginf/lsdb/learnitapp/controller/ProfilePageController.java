package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User2;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.DBOperations;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
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
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

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
    @FXML private AnchorPane pageAnchorPane;

    private static final String PERSONAL_PAGE = "/fxml/PersonalPage.fxml";

    private Neo4jDriver neo4jDriver;
    private MongoDBDriver mongoDriver;
    private User2 profileUser;
    private User2 loggedUser;
    private boolean isProfileMine;
    private int limit;

    public void initialize() {
        neo4jDriver = Neo4jDriver.getInstance();
        mongoDriver = MongoDBDriver.getInstance();
        limit = ConfigParams.getLocalConfig().getLimitNumber();
        learnItLabel.setOnMouseClicked(clickEvent -> Utils.changeScene(Utils.DISCOVERY_PAGE, clickEvent));
        learnItLabel.setCursor(Cursor.HAND);
    }

    private void loadProfileInformation(){
        completeNameLabel.setText(profileUser.getCompleteName());
        usernameLabel.setText(profileUser.getUsername());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        birthDateLabel.setText(dateFormat.format(profileUser.getDateOfBirth()));

        if (profileUser.getGender() != null)
            genderLabel.setText(profileUser.getGender());

        followerNumberLabel.setText(neo4jDriver.getFollowStats(profileUser).get(0).toString());
        followingNumberLabel.setText(neo4jDriver.getFollowStats(profileUser).get(1).toString());

        if(profileUser.getProfilePic() != null){
            Image profilePicture = new Image(profileUser.getProfilePic());
            propicImageView.setImage(profilePicture);
        }

        if(neo4jDriver.isUserFollowedByUser(usernameLabel.getText(), loggedUser.getUsername()))
            followButton.setText("Unfollow");

    }

    private void loadStatistics() {
        HashMap<String, Integer> stats = mongoDriver.avgStatistics(profileUser);
        if (stats == null || stats.get("count") == null || stats.get("count") == 0) {
            reviewedCoursesLabel.setText("Reviewed courses: 0");
            averagePriceLabel.setText("");
            averageDurationLabel.setText("");
            return;
        }

        reviewedCoursesLabel.setText("Reviewed courses: " + stats.get("count"));
        if (stats.get("avgduration") == null)
            averageDurationLabel.setText("Average hours spent learning: 0");
        else
            averageDurationLabel.setText("Average hours spent learning" + stats.get("avgduration"));
        if (stats.get("avgprice") == null)
            averagePriceLabel.setText("Average price of reviewed courses: 0");
        else
            averagePriceLabel.setText("Average price of reviewed courses:" + stats.get("avgprice"));
    }

    public void setProfileUser(User2 user){
        loggedUser = Session.getLocalSession().getLoggedUser();
        profileUser = mongoDriver.getUserByUsername(user.getUsername());
        isProfileMine = loggedUser.getUsername().equals(profileUser.getUsername());

        if (loggedUser.getRole() == 1 && isProfileMine) { //admin
            pageAnchorPane.getChildren().remove(elementsVBox);

            profileContentBorderPane.setPrefHeight(440);
            profileContentBorderPane.getChildren().remove(statisticsVBox);
            profileContentBorderPane.getChildren().remove(userBorderPane);
            profileContentBorderPane.setPrefWidth(923);
            profileContentBorderPane.setPrefHeight(440);

            ImageView adminImage = new ImageView(
                    String.valueOf(PersonalPageController.class.getResource("/img/createAdmin.png")));
            adminImage.setPreserveRatio(true);
            adminImage.setFitHeight(170);
            adminImage.setFitHeight(170);
            VBox leftColumn = new VBox();
            leftColumn.setPrefWidth(470);
            leftColumn.setPrefHeight(320);
            leftColumn.getChildren().add(adminImage);
            leftColumn.setAlignment(Pos.CENTER);
            profileContentBorderPane.setLeft(leftColumn);
            //BorderPane.setAlignment(adminImage, Pos.CENTER);

            VBox passwordVBox = new VBox();
            passwordVBox.setAlignment(Pos.CENTER);
            passwordVBox.setSpacing(20);
            passwordVBox.setPrefWidth(450);
            passwordVBox.setPrefHeight(320);

            Label oldPasswordLabel = new Label("Old password:");
            oldPasswordLabel.setFont(new Font(15));
            PasswordField oldPasswordField = new PasswordField();
            HBox row1 = new HBox();
            row1.setAlignment(Pos.CENTER);
            row1.getChildren().add(oldPasswordLabel);
            row1.getChildren().add(oldPasswordField);
            row1.setSpacing(25);
            row1.setPrefHeight(25);
            passwordVBox.getChildren().add(row1);

            Label newPasswordLabel = new Label("New password:");
            newPasswordLabel.setFont(new Font(15));
            TextField newPasswordTextField = new TextField();
            HBox row2 = new HBox();
            row2.setAlignment(Pos.CENTER);
            row2.getChildren().add(newPasswordLabel);
            row2.getChildren().add(newPasswordTextField);
            row2.setSpacing(25);
            row2.setPrefHeight(25);
            passwordVBox.getChildren().add(row2);

            Label repeatPasswordLabel = new Label("Repeat password:");
            repeatPasswordLabel.setFont(new Font(15));
            TextField repeatPasswordTextField = new TextField();
            HBox row3 = new HBox();
            row3.setAlignment(Pos.CENTER);
            row3.getChildren().add(repeatPasswordLabel);
            row3.getChildren().add(repeatPasswordTextField);
            row3.setSpacing(25);
            row3.setPrefHeight(25);
            passwordVBox.getChildren().add(row3);

            Button modifyButton = new Button("Modify");
            modifyButton.setStyle("-fx-background-color: lightpink;" +
                    "-fx-background-radius: 13px;" /*+ "-fx-font-size:15"*/);
            modifyButton.setFont(new Font(15));

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
            //gestire eliminazione utente da parte di admin
            followButton.setText("Delete user");
            followButton.setOnMouseClicked(clickEvent -> deleteUserHandler(clickEvent));
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

        if (mongoDriver.login(loggedUser.getUsername(), oldPassword) == null) {
            Utils.showErrorAlert("Error! Old password is wrong");
            return;
        }

        if(newPassword.equals(repeatPassword)) {
            loggedUser.setPassword(newPassword);
            if (!mongoDriver.editProfileInfo(loggedUser)) {
                loggedUser.setPassword(oldPassword);
                Utils.showErrorAlert("Something has gone wrong");
                return;
            }
        }

        Utils.showInfoAlert("Password modified!");

        oldPasswordField.setText("");
        newPasswordTextField.setText("");
        repeatPasswordTextField.setText("");
    }

    public void deleteUserHandler(MouseEvent clickEvent) {
        if (DBOperations.deleteUser(profileUser)) {
            Utils.showInfoAlert("User deleted successfully");
            Utils.changeScene(Utils.DISCOVERY_PAGE, clickEvent);
        }
        else
            Utils.showErrorAlert("Something has gone wrong");
    }

    public void followHandler(MouseEvent clickEvent){
        User2 loggedUser = Session.getLocalSession().getLoggedUser();

        if(neo4jDriver.isUserFollowedByUser(usernameLabel.getText(), loggedUser.getUsername())){
            // Unfollow operation
            neo4jDriver.unfollowUser(loggedUser, profileUser));
            followButton.setText("Follow");
            followerNumberLabel.setText(Integer.toString((Integer.parseInt(followerNumberLabel.getText())-1)));
        }
        else{
            // Follow operation
            neo4jDriver.followUser(loggedUser, profileUser));
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
