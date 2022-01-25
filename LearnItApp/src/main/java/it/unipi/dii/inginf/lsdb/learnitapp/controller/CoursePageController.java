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
import javafx.collections.FXCollections;
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

    @FXML private ImageView logoImageView;
    @FXML private Label titleLabel;
    @FXML private TextArea descriptionTextArea;
    @FXML private ImageView courseImageImageView;
    @FXML private Button likeCourseButton;
    @FXML private Button editCourseButton;
    @FXML private ChoiceBox languageChoiceBox;
    @FXML private TextField categoryTextField;
    @FXML private ChoiceBox levelChoiceBox;
    @FXML private TextField durationTextField;
    @FXML private TextField priceTextField;
    @FXML private TextField modalityTextField;
    @FXML private Label instructorLabel;
    @FXML private VBox reviewsVBox;
    @FXML private ImageView moreReviewsImageView;
    @FXML private VBox newReviewVBox;
    @FXML private TextField reviewTitleTextField;
    @FXML private HBox ratingHBox;
    @FXML private TextArea commentTextArea;
    @FXML private Button saveReviewButton;
    @FXML private VBox allContentVBox;
    @FXML private TextField courseLinkTextField;

    private Course course;
    private Review myReview;
    private int pageNumber = 0;
    private int limit;
    private MongoDBDriver mongoDBDriver;
    private Neo4jDriver neo4jDriver;

    public void initialize() {
        limit = ConfigParams.getLocalConfig().getLimitNumber();
        neo4jDriver = Neo4jDriver.getInstance();
        mongoDBDriver = MongoDBDriver.getInstance();

        logoImageView.setOnMouseClicked(clickEvent -> backToHome(clickEvent));
    }

    public void backToHome(MouseEvent clickEvent){
        Utils.changeScene(Utils.DISCOVERY_PAGE, clickEvent);
    }

    public void setCourse(Course course){
        this.course = mongoDBDriver.getCourseFromTitle(course.getTitle(), 0 , 1);

        moreReviewsImageView.setOnMouseClicked(clickEvent -> loadMore());

        User loggedUser = Session.getLocalSession().getLoggedUser();
        if(course.getInstructor().getUsername().equals(loggedUser.getUsername())){ // own course
            //editCourseButton.setOnMouseClicked(clickEvent -> editCourseButtonHandler());
            likeCourseButton.setVisible(false);
            newReviewVBox.setVisible(false);
            editCourseButton.setText("Apply changes");
        }
        else{ // course not owned
            if(neo4jDriver.isCourseReviewedByUser(course, loggedUser)){ // logged user has already written a review of the course
                myReview = mongoDBDriver.getCourseReviewByUser(course, loggedUser);
                if(myReview != null){ // review was found
                    newReviewVBox.setVisible(true);
                    reviewTitleTextField.setEditable(false);
                    commentTextArea.setEditable(false);
                    saveReviewButton.setText("Edit");
                    saveReviewButton.setOnMouseClicked(clickEvent -> saveReviewButtonHandler());

                    reviewTitleTextField.setText(myReview.getTitle());
                    commentTextArea.setText(myReview.getContent());

                    Utils.fillStars(myReview.getRating(), ratingHBox);
                    handleRatingStars(false);
                }
                else{ //too longer documents
                    saveReviewButton.setVisible(false);
                    handleRatingStars(false);
                    reviewTitleTextField.setText("You reviewed this course in the past");
                    reviewTitleTextField.setDisable(true);
                    commentTextArea.setText("Don't worry, we take in consideration your opinion :)");
                    commentTextArea.setDisable(true);
                    ratingHBox.setVisible(false);
                }
            }else{ // not reviewed course
                saveReviewButton.setOnMouseClicked(clickEvent2 -> saveReviewButtonHandler());
                handleRatingStars(true);
                editCourseButton.setVisible(false);
            }

            likeCourseButton.setOnMouseClicked(clickEvent -> likeCourseButtonHandler());

            if(neo4jDriver.isCourseLikedByUser(course, loggedUser)){
                likeCourseButton.setText("Dislike");
            }
        }

        loadCourseInformation();
        //loadReviews();


        loadMore();
    }

    private void loadMore(){
        int skip = pageNumber*limit;
        pageNumber++;

        Course toAdd = mongoDBDriver.getCourseFromTitle(course.getTitle(), skip, limit);
        createReviewsElements(toAdd.getReviews(), reviewsVBox);
    }

    private void createReviewsElements(List<Review> reviewsList, VBox container){
        BorderPane reviewBorderPane;
        for(Review r: reviewsList){
            reviewBorderPane = loadSingleReview(r);
            container.getChildren().add(reviewBorderPane);
        }
    }

    private BorderPane loadSingleReview(Review review) {
        BorderPane borderPane = null;

        try {
            FXMLLoader loader = new FXMLLoader(Utils.class.getResource(Utils.REVIEW_SNAPSHOT));
            borderPane = loader.load();
            ReviewSnapshotPageController reviewController = loader.getController();
            reviewController.setReview(review);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return borderPane;
    }

    public void saveReviewButtonHandler(){
        if(saveReviewButton.getText().equals("Save")) { // save operation
            int rating = 1;
            User loggedUser = Session.getLocalSession().getLoggedUser();
            Date currentTimestamp = new Date();

            myReview = new Review(reviewTitleTextField.getText(), commentTextArea.getText(), rating,
                    currentTimestamp, loggedUser);

            if (DBOperations.addReview(myReview, course)) {
                Utils.showInfoAlert("Added new review with success!");
                reviewTitleTextField.setEditable(false);
                commentTextArea.setEditable(false);

                handleRatingStars(false);

                saveReviewButton.setText("Edit");
            } else {
                Utils.showErrorAlert("Error in adding the review");
            }
        }
        else{ // edited the review, system should apply changes
            myReview.setContent(commentTextArea.getText());
            myReview.setTitle(reviewTitleTextField.getText());
            myReview.setRating(getRatingFromStars());
            myReview.setTimestamp(new Date());

            if(mongoDBDriver.editReview(course, myReview)){
                Utils.showInfoAlert("Review edited with success!");
            }
            else{
                Utils.showErrorAlert("Error in editing the review");
            }
        }
    }

    private int getRatingFromStars(){
        int rating = 0;
        for(Node star: ratingHBox.getChildren()){
            ImageView starImageView = (ImageView)star;
            if(starImageView.getImage().getUrl().equals(
                    CoursePageController.class.getResource("/img/star-on.png")))
                rating++;
        }
        return rating;
    }

    private void handleRatingStars(boolean type){
        // true -> activate
        // false -> disabled
        if(type){ // activate
            for(Node star: ratingHBox.getChildren()) {
                int index = ratingHBox.getChildren().indexOf(star);
                star.setOnMouseClicked(mouseEvent -> Utils.fillStars(index+1, ratingHBox));
                star.setOnMouseEntered(mouseEvent -> starOnMouseOverHandler(star));
                star.setOnMouseExited(mouseEvent -> starOnMouseExitedHandler());
            }
        }
        else{ // disabled
            for(Node star: ratingHBox.getChildren()) {
                star.setOnMouseEntered(mouseEvent -> {});
                star.setOnMouseEntered(mouseEvent -> {});
                star.setOnMouseClicked(mouseEvent -> {});
            }
        }
    }

    private void starOnMouseOverHandler(Node star){
        int starIndex = ratingHBox.getChildren().indexOf(star);
        Utils.fillStars(starIndex+1, ratingHBox);
    }

    private void starOnMouseExitedHandler(){
        Utils.fillStars(1, ratingHBox);
    }

    public void likeCourseButtonHandler(){
        User loggedUser = Session.getLocalSession().getLoggedUser();
        if(neo4jDriver.isCourseLikedByUser(course, loggedUser)){
            //dislike
            neo4jDriver.dislikeCourse(loggedUser, course);
            likeCourseButton.setText("like");
        }
        else{
            //like
            neo4jDriver.likeCourse(loggedUser, course);
            likeCourseButton.setText("dislike");
        }
    }

    private void loadCourseInformation(){
        titleLabel.setText(course.getTitle());
        descriptionTextArea.setText(course.getDescription());

        if(course.getCoursePic() != null){
            Image coursePicture = new Image(course.getCoursePic());
            courseImageImageView.setImage(coursePicture);
        }

        languageChoiceBox.setItems(FXCollections.observableArrayList(Utils.LANGUAGES));
        languageChoiceBox.setValue(course.getLanguage());
        levelChoiceBox.setItems(FXCollections.observableArrayList(Utils.LEVELS));
        levelChoiceBox.setValue(course.getLevel());

        String categories = "";
        if(!course.getCategory().isEmpty()){
            List<String> listOfCategories = course.getCategory();
            //StringBuilder categories = new StringBuilder();
            for(String c : listOfCategories){
                categories += c;
                if(listOfCategories.indexOf(c) < listOfCategories.size()-1)
                    categories += ", ";
            }
        }
        categoryTextField.setText(categories);
        instructorLabel.setText(course.getInstructor().getUsername());
        modalityTextField.setText(course.getModality());
        durationTextField.setText(Double.toString(course.getDuration()));
        priceTextField.setText(Double.toString(course.getPrice()));
        courseLinkTextField.setText(course.getLink());
    }

    public void editCourseButtonHandler(){
        String categoriesString = categoryTextField.getText();
        List<String> categoryList = null;
        if (categoriesString.contains(",")){
            String[] categories = categoriesString.split(", ", -1);
            categoryList = new ArrayList<>(List.of(categories));
        }else
        if(categoriesString.length() !=0) {
            categoryList = new ArrayList<>();
            categoryList.add(categoriesString);
        }

        String language = languageChoiceBox.getValue().toString();
        String coursePic = course.getCoursePic();
        String modality = modalityTextField.getText();
        String description = descriptionTextArea.getText();
        String level = levelChoiceBox.getValue().toString();
        double duration = Double.parseDouble(durationTextField.getText());
        double price = Double.parseDouble(priceTextField.getText());
        String link = courseLinkTextField.getText();

        Course newCourse;
        if(categoryTextField.getText().equals("")) {
            newCourse = new Course(course.getTitle(), description, course.getInstructor(), language, level,
                    duration, price, link, modality, coursePic);
        }
        else
            newCourse = new Course(course.getTitle(), description, course.getInstructor(), language, categoryList,
                    level, duration, price, link, modality, coursePic);

        course = newCourse;
        if(DBOperations.updateCourse(newCourse))
            Utils.showInfoAlert("Course's information updated with success!");
        else
            Utils.showErrorAlert("Error in updating course's information.");
    }

}
