package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Review;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.service.LogicService;
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

    // the container of the review in the course page
    private VBox container;

    //the reviewed course
    private Course course;
    //the review to be shown
    private Review review;

    public void initialize() {
        if (Session.getLocalSession().getLoggedUser().getRole() == 1) {
            deleteImageView.setVisible(true);
            deleteImageView.setOnMouseClicked(clickEvent -> deleteReview(clickEvent));
            deleteImageView.setCursor(Cursor.HAND);
        }
        else
            deleteImageView.setVisible(false);
    }

    /**
     * Handler to delete the review from the GUI of the course page
     */
    public void deleteReview(MouseEvent clickEvent) {
        LogicService.deleteReview(review, course);
        container.getChildren().remove(thisBorderPane);
    }

    /**
     * Load review information
     */
    private void loadReviewInformation(){
        User author = MongoDBDriver.getInstance().getUserByUsername(review.getUsername());
        if(review.getTitle()!=null)
            courseTitleLabel.setText(review.getTitle());
        else
            courseTitleLabel.setText("");

        Utils.fillStars(review.getRating(), ratingHBox);

        usernameLabel.setText(review.getUsername());
        usernameLabel.setOnMouseClicked(clickEvent -> visitAuthorProfile(clickEvent));
        profilePicImageView.setOnMouseClicked(clickEvent -> visitAuthorProfile(clickEvent));
        Image profilePicture = new Image(String.valueOf(ReviewSnapshotPageController.class.getResource(Utils.USER_DEFAULT)));
        /*
        if(author.getProfilePic() != null){
            try {
                profilePicture = new Image(author.getProfilePic());
            } catch (IllegalArgumentException e) {
                profilePicture = new Image(String.valueOf(ReviewSnapshotPageController.class.getResource("/img/userDefault.png")));
            }
        */
        profilePicImageView.setImage(profilePicture);

        if(review.getContent() != null)
            reviewContentLabel.setText(review.getContent());
        else
            reviewContentLabel.setText("");

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

    private void visitAuthorProfile(MouseEvent mouseEvent){
        User author = new User();
        author.setUsername(review.getUsername());

        ProfilePageController profilePageController = (ProfilePageController) Utils.changeScene(Utils.PROFILE_PAGE, mouseEvent);
        profilePageController.setProfileUser(author);
    }
}
