package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Review;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ReviewSnapshotPageController {
    @FXML private Label courseTitleLabel;
    @FXML private ImageView profilePicImageView;
    @FXML private Label usernameLabel;
    @FXML private TextArea commentTextArea;
    @FXML private HBox ratingHBox;
    @FXML private Label lastModifiedLabel;

    private Review review;

    public void initialize() {
    }

    private void loadReviewInformation(){
        if(review.getTitle()!=null)
            courseTitleLabel.setText(review.getTitle());
        else
            courseTitleLabel.setText("");

        Utils.fillStars(review.getRating(), ratingHBox);

        usernameLabel.setText(review.getAuthor().getUsername());

        if(review.getAuthor().getProfilePic() != null){
            Image profilePicture = new Image(review.getAuthor().getProfilePic());
            profilePicImageView.setImage(profilePicture);
        }

        if(review.getContent() != null)
            commentTextArea.setText(review.getContent());
        else
            commentTextArea.setText("");

        lastModifiedLabel.setText(review.getTimestamp().toString());
    }

    public void setReview(Review review){
        this.review = review;
        loadReviewInformation();
    }
}
