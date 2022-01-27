package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Review;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.DBOperations;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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
    @FXML private TextArea commentTextArea;
    @FXML private HBox ratingHBox;
    @FXML private Label lastModifiedLabel;
    @FXML private BorderPane thisBorderPane;
    @FXML private ImageView deleteImageView;

    private VBox container;

    private Course course;
    private Review review;

    public void initialize() {
        if (Session.getLocalSession().getLoggedUser().getRole() == User.Role.ADMINISTRATOR) {
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
        if(review.getTitle()!=null)
            courseTitleLabel.setText(review.getTitle());
        else
            courseTitleLabel.setText("");

        Utils.fillStars(review.getRating(), ratingHBox);

        usernameLabel.setText(review.getAuthor().getUsername());
        usernameLabel.setOnMouseClicked(clickEvent -> visitAuthorProfile(clickEvent));
        profilePicImageView.setOnMouseClicked(clickEvent -> visitAuthorProfile(clickEvent));

        if(review.getAuthor().getProfilePic() != null){
            Image profilePicture = new Image(review.getAuthor().getProfilePic());
            profilePicImageView.setImage(profilePicture);
        }

        if(review.getContent() != null)
            commentTextArea.setText(review.getContent());
        else
            commentTextArea.setText("");

        lastModifiedLabel.setText("Last-modified: "+review.getTimestamp().toString());
    }

    public void setReview(Review review, VBox container){
        this.review = review;
        this.container = container;
        loadReviewInformation();
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void visitAuthorProfile(MouseEvent mouseEvent){
        User author = review.getAuthor();
        ProfilePageController profilePageController = (ProfilePageController) Utils.changeScene(Utils.PROFILE_PAGE, mouseEvent);
        profilePageController.setProfileUser(author);
    }
}
