package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class UserSnapshotController {
    @FXML AnchorPane userSnapshot;
    @FXML private Label usernameLabel;
    @FXML private ImageView userPicImage;
    @FXML private Label genderLabel;
    @FXML private Label totalCoursesLabel;
    private User referredUser;

    public void initialize(){
        userSnapshot.setOnMouseClicked(mouseEvent -> showCompleteUserInfo(mouseEvent));
        userSnapshot.setCursor(Cursor.HAND);
    }

    public void setSnapshotUser(User user) {
        usernameLabel.setText(user.getUsername());
        /*
        if(user.getProfilePic() != null)
            userPicImage = new ImageView(new Image(user.getProfilePic()));

        */
        //completeNameLabel.setText(user.getCompleteName());

        if(user.getGender() != null)
            genderLabel.setText(user.getGender());
        else
            genderLabel.setText("N/S");

        totalCoursesLabel.setText("Completed courses: " +
                MongoDBDriver.getInstance().avgStatistics(user).get("count").intValue());

        referredUser = user;
    }

    private void showCompleteUserInfo(MouseEvent mouseEvent){
        ProfilePageController profilePageController =
                (ProfilePageController) Utils.changeScene(Utils.PROFILE_PAGE, mouseEvent);
        profilePageController.setProfileUser(referredUser);
    }
}
