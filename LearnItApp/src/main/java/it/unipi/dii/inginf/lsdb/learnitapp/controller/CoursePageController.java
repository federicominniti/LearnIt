package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Review;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.DBOperations;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CoursePageController {

    @FXML private TextField titleTextField;
    @FXML private TextArea descriptionTextArea;
    @FXML private ImageView courseImageImageView;
    @FXML private Button likeCourseButton;
    @FXML private Button reviewCourseButton;
    @FXML private Button editCourseButton;
    @FXML private TextField languageTextField;
    @FXML private TextField categoryTextField;
    @FXML private TextField levelTextField;
    @FXML private TextField durationTextField;
    @FXML private TextField priceTextField;
    @FXML private TextField modalityTextField;
    @FXML private TextField instructorTextField;
    @FXML private VBox reviewsVBox;
    @FXML private ImageView moreReviewsImageView;
    @FXML private Button backToHomeButton;
    @FXML private VBox newReviewVBox;
    @FXML private TextField reviewTitleTextField;
    @FXML private HBox ratingHBox;
    @FXML private TextArea commentTextArea;
    @FXML private Button saveReviewButton;

    private Course course;
    private Review myReview;
    private int pageNumber = 0;
    private int limit;

    public void initialize() {
        User loggedUser = Session.getLocalSession().getLoggedUser();
        limit = ConfigParams.getLocalConfig().getLimitNumber();

        backToHomeButton.setOnMouseClicked(clickEvent -> backToHomeButtonHandler(clickEvent));
        moreReviewsImageView.setOnMouseClicked(clickEvent -> loadMore());

        if(course.getInstructor().getUsername().equals(loggedUser.getUsername())){ // own course
            editCourseButton.setOnMouseClicked(clickEvent -> editCourseButtonHandler(clickEvent));
            reviewCourseButton.setVisible(false);
            likeCourseButton.setVisible(false);
            newReviewVBox.setVisible(false);
            saveReviewButton.setVisible(false);
        }
        else{ // random course not owned
            editCourseButton.setVisible(false);

            if(Neo4jDriver.getInstance().isCourseReviewedByUser(course, loggedUser)){ // logged user has already written a review of the course
                myReview = isReviewedByUser(course, loggedUser);
                if(myReview != null){ // review was found
                    reviewCourseButton.setVisible(false);
                    newReviewVBox.setVisible(true);
                    reviewTitleTextField.setEditable(false);
                    commentTextArea.setEditable(false);
                    saveReviewButton.setText("Edit");
                    saveReviewButton.setOnMouseClicked(clickEvent -> saveReviewButtonHandler(clickEvent));

                    reviewTitleTextField.setText(myReview.getTitle());
                    commentTextArea.setText(myReview.getContent());

                    fillStars(myReview.getRating());
                    handleRatingStars(false);
                }
                else{
                    newReviewVBox.setVisible(false);
                    saveReviewButton.setVisible(false);
                    handleRatingStars(false);
                    reviewTitleTextField.setText("You reviewed this course in the past");
                    reviewTitleTextField.setVisible(true);
                }
            }
            else{ // logged user has not written a review of the course
                reviewCourseButton.setVisible(true);
                reviewCourseButton.setOnMouseClicked(clickEvent -> reviewCourseButtonHandler(clickEvent));
                newReviewVBox.setVisible(false);
                saveReviewButton.setVisible(false);
                handleRatingStars(false);
            }


            likeCourseButton.setOnMouseClicked(clickEvent -> likeCourseButtonHandler(clickEvent));

            if(isCourseLikedByUser(course, loggedUser)){
                likeCourseButton.setText("dislike");
            }
        }

        loadCourseInformation();
        loadReviews();
    }

    public void backToHomeButtonHandler(MouseEvent clickEvent){
        Utils.changeScene("/DiscoveryPage.fxml", clickEvent);
    }

    private void fillStars(int rating){
        for(Node star: ratingHBox.getChildren()){
            int index = ratingHBox.getChildren().indexOf(star);
            if(index > rating -1)
                break;
            ImageView starImageView = (ImageView)star;
            Image starImage = new Image("/img/star-on.png");
            starImageView.setImage(starImage);
            ratingHBox.getChildren().set(index, (Node)starImageView);
        }

        for(int index=rating; index <5; index++){
            Node star = ratingHBox.getChildren().get(index);
            ImageView starImageView = (ImageView)star;
            Image starImage = new Image("/img/star-off.png");
            starImageView.setImage(starImage);
            ratingHBox.getChildren().set(index, (Node)starImageView);
        }
    }

    public void editCourseButtonHandler(MouseEvent clickEvent){

        if(editCourseButton.getText().equals("Edit")){
            editCourseButton.setText("Save changes");
            languageTextField.setEditable(true);
            categoryTextField.setEditable(true);
            levelTextField.setEditable(true);
            durationTextField.setEditable(true);
            priceTextField.setEditable(true);
            modalityTextField.setEditable(true);
            descriptionTextArea.setEditable(true);
        }
        else{
            String categoriesString = categoryTextField.getText();
            if(categoriesString.equals("")) {
                course.setCategory(null);
            }
            else {
                if (categoriesString.contains(",")){
                    String[] categories = categoriesString.split(", ", -1);
                    List<String> categoriesList = new ArrayList<>();

                    for(String c: categories){
                        categoriesList.add(c);
                    }
                    course.setCategory(categoriesList);
                }
                else{
                    Utils.showErrorAlert("Error in inserting course's categories");
                    return;
                }
            }

            course.setLanguage(languageTextField.getText());
            course.setLevel(levelTextField.getText());
            course.setDuration(Double.parseDouble(durationTextField.getText()));
            course.setPrice(Double.parseDouble(priceTextField.getText()));
            course.setModality(modalityTextField.getText());
            course.setDescription(descriptionTextArea.getText());

            if(DBOperations.updateCourse(course)) {
                languageTextField.setEditable(false);
                categoryTextField.setEditable(false);
                levelTextField.setEditable(false);
                durationTextField.setEditable(false);
                priceTextField.setEditable(false);
                modalityTextField.setEditable(false);
                descriptionTextArea.setEditable(false);
                editCourseButton.setText("Edit");
                Utils.showInfoAlert("Course's information updated with success!");
                return;
            }
            Utils.showErrorAlert("Error in updating course's information.");
        }
    }

    private void handleRatingStars(boolean type){
        // true -> activate
        // false -> deactivate
        if(type){ // activate
            for(Node star: ratingHBox.getChildren()) {
                int index = ratingHBox.getChildren().indexOf(star);
                star.setOnMouseClicked(mouseEvent -> fillStars(index+1));
                star.setOnMouseEntered(mouseEvent -> starOnMouseOverHandler(star));
                star.setOnMouseExited(mouseEvent -> starOnMouseExitedHandler());
            }
        }
        else{ // deactivate
            for(Node star: ratingHBox.getChildren()) {
                star.setOnMouseEntered(mouseEvent -> {});
                star.setOnMouseEntered(mouseEvent -> {});
                star.setOnMouseClicked(mouseEvent -> {});
            }
        }
    }

    private void starOnMouseOverHandler(Node star){
        int starIndex = ratingHBox.getChildren().indexOf(star);
        fillStars(starIndex+1);
    }

    private void starOnMouseExitedHandler(){
        fillStars(1);
    }

    public void reviewCourseButtonHandler(MouseEvent clickEvent){
        newReviewVBox.setVisible(true);
        saveReviewButton.setOnMouseClicked(clickEvent2 -> saveReviewButtonHandler(clickEvent2));
        handleRatingStars(true);
    }

    public void saveReviewButtonHandler(MouseEvent clickEvent){

        if(saveReviewButton.getText().equals("save")){ // save operation
            if(myReview==null){ // new review, system should apply changes
                int rating =1;
                User loggedUser = Session.getLocalSession().getLoggedUser();
                Date currentTimestamp = new Date();

                myReview = new Review(reviewTitleTextField.getText(), commentTextArea.getText(), rating, currentTimestamp, loggedUser);

                if(DBOperations.addReview(myReview, course)){
                    Utils.showInfoAlert("Added new review with success!");
                    reviewTitleTextField.setEditable(false);
                    commentTextArea.setEditable(false);

                    handleRatingStars(false);

                    saveReviewButton.setText("Edit");
                }
                else{
                    Utils.showErrorAlert("Error in adding the review");
                }
            }
            else{ // edited the review, system should apply changes

                myReview.setContent(commentTextArea.getText());
                myReview.setTitle(reviewTitleTextField.getText());
                myReview.setRating(getRatingFromStars());
                myReview.setTimestamp(new Date());

                if(MongoDBDriver.getInstance().editReview(course, myReview)){
                    Utils.showInfoAlert("Review edited with success!");
                    reviewTitleTextField.setEditable(false);
                    commentTextArea.setEditable(false);

                    handleRatingStars(false);

                    saveReviewButton.setText("Edit");
                }
                else{
                    Utils.showErrorAlert("Error in editing the review");
                }
            }

        }
        else{ // click on "edit" button, system should permit the user editing the review
            reviewTitleTextField.setEditable(true);
            commentTextArea.setEditable(true);
            handleRatingStars(true);
            saveReviewButton.setText("save");
        }
    }

    private int getRatingFromStars(){
        int rating = 0;
        for(Node star: ratingHBox.getChildren()){
            ImageView starImageView = (ImageView)star;
            if(starImageView.getImage().getUrl().equals("/img/star-on.png"))
                rating++;
        }
        return rating;
    }

    public void likeCourseButtonHandler(MouseEvent clickEvent){
        User loggedUser = Session.getLocalSession().getLoggedUser();
        if(isCourseLikedByUser(course, loggedUser)){
            //dislike
            Neo4jDriver.getInstance().dislikeCourse(loggedUser, course);
            likeCourseButton.setText("like");
        }
        else{
            //like
            Neo4jDriver.getInstance().likeCourse(loggedUser, course);
            likeCourseButton.setText("dislike");
        }
    }

    public void setCourse(Course course){
        this.course = course;
    }

    private void loadCourseInformation(){
        titleTextField.setText(course.getTitle());
        descriptionTextArea.setText(course.getDescription());

        if(course.getCoursePic()!=null){
            Image coursePicture = new Image(course.getCoursePic());
            courseImageImageView.setImage(coursePicture);
        }

        languageTextField.setText(course.getLanguage());

        if(!course.getCategory().isEmpty()){
            List<String> listOfCategories = course.getCategory();
            StringBuilder categories = new StringBuilder();

            for(String c : listOfCategories){
                categories.append(c);
                if(listOfCategories.indexOf(c) < listOfCategories.size()-1)
                    categories.append(", ");
            }
            categoryTextField.setText(categories.toString());
        }
        else
            modalityTextField.setText("not specified");

        levelTextField.setText(course.getLevel());

        durationTextField.setText(Double.toString(course.getDuration()));

        priceTextField.setText(Double.toString(course.getPrice()));

        if(course.getModality()!=null)
            modalityTextField.setText(course.getModality());
        else
            modalityTextField.setText("not specified");

    }

    private void loadMore(){
        int skip = pageNumber*limit;
        List<Review> toAdd = MongoDBDriver.getInstance().getCourseReviewFromId(course.getId(), skip, limit);

        pageNumber++;

        for(Review r: toAdd){
            createReviewsElements(toAdd, reviewsVBox);
            course.getReviews().add(r);
        }
    }

    private void loadReviews(){
        course.setReviews(MongoDBDriver.getInstance().getCourseReviewFromId(course.getId(), 0, limit));
        createReviewsElements(course.getReviews(), reviewsVBox);
    }

    private BorderPane loadSingleReview(Review review) {
        BorderPane borderPane = null;

        try {
            FXMLLoader loader = new FXMLLoader(Utils.class.getResource("/fxml/ReviewSnapshotPage.fxml"));
            borderPane = loader.load();
            ReviewSnapshotPageController reviewController = loader.getController();
            reviewController.setReview(review);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return borderPane;
    }

    private void createReviewsElements(List<Review> reviewsList, VBox container){
        BorderPane reviewBorderPane;
        for(Review r: reviewsList){
            reviewBorderPane = loadSingleReview(r);
            container.getChildren().add(reviewBorderPane);
        }
    }


}
