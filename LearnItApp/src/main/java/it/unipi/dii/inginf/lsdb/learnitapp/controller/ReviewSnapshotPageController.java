package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Review;

import java.io.FileInputStream;

public class ReviewSnapshotPageController {
    @FXML private Label courseTitleLabel;
    @FXML private ImageView profiePicImageView;
    @FXML private Label usernameLabel;
    @FXML private TextArea commentTextArea;
    @FXML private HBox ratingHBox;
    @FXML private Label lastModifiedLabel;
    @FXML private Label completeNameLabel;

    private Review review;

    public void initialize() {
        loadReviewInformation();
    }

    private void loadReviewInformation(){
        if(review.getTitle()!=null)
            courseTitleLabel.setText(review.getTitle());
        else
            courseTitleLabel.setText("");

        handleReviewRating();

        usernameLabel.setText(review.getAuthor().getUsername());
        completeNameLabel.setText(review.getAuthor().getCompleteName());

        if(review.getContent() != null)
            commentTextArea.setText(review.getContent());
        else
            commentTextArea.setText("");

        lastModifiedLabel.setText(review.getTimestamp());
    }

    private void handleReviewRating(){
        int rating = review.getRating();

        for(int i=1; i<=rating; i++){
            Image starImage = new Image(new FileInputStream("/img/star-on"));
            ImageView starImageView = new ImageView(starImage);
            ratingHBox.getChildren().add(starImageView);
        }

        for(int i = rating+1; i<=5; i++){
            Image starImage = new Image(new FileInputStream("/img/star-off"));
            ImageView starImageView = new ImageView(starImage);
            ratingHBox.getChildren().add(starImageView);
        }
    }

    public void setReview(Review review){
        this.review = review;
    }
}
