package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Course2;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Review2;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User2;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.DBOperations;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ReviewSnapshotPageController {
    @FXML private Label courseTitleLabel;
    @FXML private ImageView profilePicImageView;
    @FXML private Label usernameLabel;
    @FXML private Label reviewContentLabel;
    @FXML private HBox ratingHBox;
    @FXML private Label lastModifiedLabel;
    @FXML private BorderPane thisBorderPane;
    @FXML private ImageView deleteImageView;

    private VBox container;

    private Course2 course;
    private Review2 review;

    public void initialize() {
        if (Session.getLocalSession().getLoggedUser().getRole() == 1) {
            deleteImageView.setVisible(true);
            deleteImageView.setOnMouseClicked(clickEvent -> deleteReview(clickEvent));
            deleteImageView.setCursor(Cursor.HAND);
        }
        else
            deleteImageView.setVisible(false);
    }

    public void deleteReview(MouseEvent clickEvent) {
        DBOperations.deleteReview(review, course);
        container.getChildren().remove(thisBorderPane);
    }

    private void loadReviewInformation(){
        User2 author = MongoDBDriver.getInstance().getUserByUsername(review.getUsername());
        if(review.getTitle()!=null)
            courseTitleLabel.setText(review.getTitle());
        else
            courseTitleLabel.setText("");

        Utils.fillStars(review.getRating(), ratingHBox);

        usernameLabel.setText(review.getUsername());
        usernameLabel.setOnMouseClicked(clickEvent -> visitAuthorProfile(clickEvent));
        profilePicImageView.setOnMouseClicked(clickEvent -> visitAuthorProfile(clickEvent));

        if(author.getProfilePic() != null){
            Image profilePicture = new Image(author.getProfilePic());
            profilePicImageView.setImage(profilePicture);
        }

        if(review.getContent() != null)
            reviewContentLabel.setText(review.getContent());
        else
            reviewContentLabel.setText("");

        lastModifiedLabel.setText("Last-modified: "+review.getTimestamp().toString());
    }

    public void setReview(Review2 review, VBox container){
        this.review = review;
        this.container = container;
        loadReviewInformation();
    }

    public void setCourse(Course2 course) {
        this.course = course;
    }

    public void visitAuthorProfile(MouseEvent mouseEvent){
        User2 author = new User2();
        author.setUsername(review.getUsername());

        ProfilePageController profilePageController = (ProfilePageController) Utils.changeScene(Utils.PROFILE_PAGE, mouseEvent);
        profilePageController.setProfileUser(author);
    }
}
