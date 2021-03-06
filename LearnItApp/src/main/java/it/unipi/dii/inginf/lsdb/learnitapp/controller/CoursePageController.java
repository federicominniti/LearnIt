package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Review;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.service.LogicService;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.*;

public class CoursePageController {

    @FXML private ImageView logoImageView;
    @FXML private Label titleLabel;
    @FXML private TextArea descriptionTextArea;
    @FXML private ImageView courseImageImageView;
    @FXML private Button likeCourseButton;
    @FXML private Button editCourseButton;
    @FXML private ChoiceBox<String> languageChoiceBox;
    @FXML private TextField categoryTextField;
    @FXML private ChoiceBox<String> levelChoiceBox;
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
    @FXML private TextField courseImageTextField;
    @FXML private HBox buttonsHBox;
    @FXML private VBox profileVBox;
    @FXML LineChart<String, Number> annualMeanRatingLineChart;
    @FXML private Label lastModifiedLabel;
    @FXML private ImageView deleteImageView;
    @FXML private Label likesLabel;
    private XYChart.Series series;


    //the course to be loaded in the page
    private Course course;
    //the review of the currently logged user (if there is one)
    private Review myReview;
    //pages of already loaded reviews
    private int pageNumber = 0;
    //limit when loading reviews to be showed
    private int limit;
    //likes of the course
    private int likes;

    private MongoDBDriver mongoDBDriver;
    private Neo4jDriver neo4jDriver;

    public void initialize() {
        limit = ConfigParams.getLocalConfig().getLimitNumber();
        neo4jDriver = Neo4jDriver.getInstance();
        mongoDBDriver = MongoDBDriver.getInstance();

        series = new XYChart.Series();
        series.setName("Mean rating");
        annualMeanRatingLineChart.getData().add(series);

        logoImageView.setOnMouseClicked(clickEvent -> backToHome(clickEvent));
        logoImageView.setCursor(Cursor.HAND);
        deleteImageView.setCursor(Cursor.HAND);
    }

    public void backToHome(MouseEvent clickEvent){
        Utils.changeScene(Utils.DISCOVERY_PAGE, clickEvent);
    }

    /**
     * Used to prepare the page loading all the information about the course.
     * If the logged user is an admin, they will see the options for deleting course and reviews.
     * If the logged user is the owner of the course, they will see the option for deleting the course
     * and edit course information
     * Other logged user will see some reviews and their review (if they already have written one), with the possibility
     * of deleting their review (once it has been written), load more reviews from other users and like/unlike the course.
     * @param c the course to be fully loaded
     */
    public void setCourse(Course c){
        this.course = c;

        moreReviewsImageView.setOnMouseClicked(clickEvent -> loadMore());
        moreReviewsImageView.setCursor(Cursor.HAND);

        User loggedUser = Session.getLocalSession().getLoggedUser();
        if (loggedUser.getRole() == 1) {
            ImageView trash = new ImageView(new Image(String.valueOf(CoursePageController.class.getResource(Utils.TRASH_BIN))));
            trash.setPreserveRatio(true);
            trash.setFitWidth(40);
            trash.setFitHeight(40);
            profileVBox.getChildren().remove(buttonsHBox);
            profileVBox.getChildren().add(trash);
            trash.setOnMouseClicked(clickEvent -> deleteCourse(clickEvent));
            trash.setCursor(Cursor.HAND);
            allContentVBox.getChildren().remove(newReviewVBox);
        } else {
            if (course.getInstructor().equals(loggedUser.getUsername())) { // own course
                ImageView trash = new ImageView(new Image(String.valueOf(CoursePageController.class.getResource(Utils.TRASH_BIN))));
                trash.setPreserveRatio(true);
                trash.setFitWidth(40);
                trash.setFitHeight(40);
                editCourseButton.setOnMouseClicked(clickEvent -> editCourseButtonHandler());
                editCourseButton.setCursor(Cursor.HAND);
                likeCourseButton.setVisible(false);
                newReviewVBox.setVisible(false);
                editCourseButton.setText("Edit course");

                profileVBox.getChildren().add(trash);
                trash.setOnMouseClicked(clickEvent -> deleteCourse(clickEvent));
                trash.setCursor(Cursor.HAND);
            } else { // course not owned
                if (neo4jDriver.isCourseReviewedByUser(course, loggedUser)) { // logged user has already written a review of the course
                    myReview = mongoDBDriver.getCourseReviewByUser(course, loggedUser);
                    if (myReview != null) { // review was found
                        newReviewVBox.setVisible(true);
                        reviewTitleTextField.setEditable(false);
                        commentTextArea.setEditable(false);
                        saveReviewButton.setText("Edit");
                        saveReviewButton.setOnMouseClicked(clickEvent -> saveReviewButtonHandler());
                        saveReviewButton.setCursor(Cursor.HAND);

                        deleteImageView.setOnMouseClicked(clickEvent -> deleteButtonHandler());
                        reviewTitleTextField.setText(myReview.getTitle());
                        commentTextArea.setText(myReview.getContent());
                        lastModifiedLabel.setText("Last-modified: "+ myReview.getTimestamp());
                        editCourseButton.setVisible(false);
                        Utils.fillStars(myReview.getRating(), ratingHBox);
                        handleRatingStars(false);
                    } else { //too longer documents
                        saveReviewButton.setVisible(false);
                        handleRatingStars(false);
                        reviewTitleTextField.setText("You reviewed this course in the past");
                        reviewTitleTextField.setDisable(true);
                        commentTextArea.setText("Don't worry, we take in consideration your opinion :)");
                        commentTextArea.setDisable(true);
                        ratingHBox.setVisible(false);

                        deleteImageView.setVisible(false);
                        newReviewVBox.getChildren().remove(deleteImageView);
                    }
                } else { // not reviewed course
                    saveReviewButton.setText("Save");
                    saveReviewButton.setOnMouseClicked(clickEvent2 -> saveReviewButtonHandler());
                    saveReviewButton.setCursor(Cursor.HAND);
                    commentTextArea.setEditable(true);
                    handleRatingStars(true);
                    editCourseButton.setVisible(false);
                    deleteImageView.setVisible(false);
                }

                likeCourseButton.setOnMouseClicked(clickEvent -> likeCourseButtonHandler());
                likeCourseButton.setCursor(Cursor.HAND);

                if (neo4jDriver.isCourseLikedByUser(course, loggedUser)) {
                    likeCourseButton.setText("Dislike");
                }
            }
        }

        loadCourseInformation(loggedUser.getUsername().equals(course.getInstructor()));
        loadAnnualMeanRatingLineChart();

        loadMore();
    }

    private void deleteButtonHandler(){
        if(LogicService.deleteReview(myReview, course)){
            saveReviewButton.setText("Save");
            myReview = null;
            reviewTitleTextField.setEditable(true);
            commentTextArea.setEditable(true);
            reviewTitleTextField.setText("");
            commentTextArea.setText("");
            Utils.fillStars(1, ratingHBox);
            handleRatingStars(true);
            deleteImageView.setVisible(false);
        }
    }

    /**
     * Loads more reviews (if there are more to be loaded)
     */
    private void loadMore(){
        int skip = pageNumber*limit;
        pageNumber++;

        int toIndex = skip + limit;
        if(course.getReviews() == null)
           return; // ricontrollare ???
        if (toIndex >= course.getReviews().size()) {
            toIndex = course.getReviews().size();
        }

        if (skip >= toIndex)
            return;

        List<Review> toAdd = course.getReviews().subList(skip, toIndex);
        if(myReview != null) {
            int i;
            boolean found = false;
            for (i = 0; i < toAdd.size(); i++) {
                if (toAdd.get(i).getUsername().equals(Session.getLocalSession().getLoggedUser().getUsername())) {
                    found = true;
                    break;
                }
            }
            if(found)
                toAdd.remove(i);
        }
        if(toAdd.size() != 0)
            createReviewsElements(toAdd, reviewsVBox);
    }

    private void deleteCourse(MouseEvent clickEvent) {
        if (LogicService.deleteCourse(course)) {
            Utils.changeScene(Utils.DISCOVERY_PAGE, clickEvent);
        }
    }

    /**
     * Creates the GUI elements for the reviews
     * @param reviewsList the list of reviews to be shown
     * @param container the container of the list of reviews
     */
    private void createReviewsElements(List<Review> reviewsList, VBox container){
        BorderPane reviewBorderPane;
        for(Review r: reviewsList){
            reviewBorderPane = loadSingleReview(r);
            container.getChildren().add(reviewBorderPane);
        }
    }

    /**
     * Loads the GUI for a single review
     * @param review the review to be shown
     * @return a BorderPane container for the review
     */
    private BorderPane loadSingleReview(Review review) {
        BorderPane borderPane = null;

        try {
            FXMLLoader loader = new FXMLLoader(Utils.class.getResource(Utils.REVIEW_SNAPSHOT));
            borderPane = loader.load();
            ReviewSnapshotPageController reviewController = loader.getController();
            reviewController.setReview(review, reviewsVBox);
            reviewController.setCourse(course);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return borderPane;
    }

    /**
     * Saves the review of the logged user for this course, adjusting the GUI for future editing or deletion
     */
    private void saveReviewButtonHandler(){
        if(saveReviewButton.getText().equals("Save")) { // save operation
            int rating = getRatingFromStars();
            User loggedUser = Session.getLocalSession().getLoggedUser();
            Date currentTimestamp = new Date();

            String comment = commentTextArea.getText().equals("") ? null : commentTextArea.getText();
            String title = reviewTitleTextField.getText().equals("") ? null : reviewTitleTextField.getText();

            if(myReview==null) { // add review
                myReview = new Review(title, comment, rating, currentTimestamp, loggedUser.getUsername());

                if (LogicService.addReview(myReview, course)) {
                    Utils.showInfoAlert("Added new review with success!");

                } else {
                    Utils.showErrorAlert("Error in adding the review");
                }
                deleteImageView.setVisible(true);
            }
            else{ // edit review

                myReview.setContent(comment);
                myReview.setTitle(title);
                myReview.setRating(rating);
                myReview.setTimestamp(currentTimestamp);

                if(mongoDBDriver.editReview(course, myReview)){
                    Utils.showInfoAlert("Review edited with success!");
                }
                else{
                    Utils.showErrorAlert("Error in editing the review");
                }
            }
            lastModifiedLabel.setText("Last-modified: " + myReview.getTimestamp());
            reviewTitleTextField.setEditable(false);
            commentTextArea.setEditable(false);
            deleteImageView.setOnMouseClicked(clickEvent -> deleteButtonHandler());
            handleRatingStars(false);

            saveReviewButton.setText("Edit");

        }
        else{ // click on edit button -> edit the review should be permitted
            reviewTitleTextField.setEditable(true);
            commentTextArea.setEditable(true);

            handleRatingStars(true);

            saveReviewButton.setText("Save");
        }
    }

    /**
     * converts the number of stars to a rating from 1 to 5
     * @return the rating
     */
    private int getRatingFromStars(){
        int rating = 0;
        Image starOn = new Image(String.valueOf(CoursePageController.class.getResource(Utils.STAR_ON)));

        for(Node star: ratingHBox.getChildren()){
            ImageView starImageView = (ImageView)star;
            if(starImageView.getImage().getUrl().equals(starOn.getUrl()))
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
                star.setOnMouseClicked(mouseEvent -> starOnMouseClickedHandler(index));
                star.setOnMouseEntered(mouseEvent -> starOnMouseOverHandler(star));
                star.setOnMouseExited(mouseEvent -> starOnMouseExitedHandler());
                star.setCursor(Cursor.HAND);
            }
        }
        else{ // disabled
            for(Node star: ratingHBox.getChildren()) {
                star.setOnMouseEntered(mouseEvent -> {});
                star.setOnMouseExited(mouseEvent -> {});
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

    private void starOnMouseClickedHandler(int index){
        Utils.fillStars(index + 1, ratingHBox);
        for(Node star: ratingHBox.getChildren()) {
            int s = ratingHBox.getChildren().indexOf(star);
            if(s<=index){
                star.setOnMouseExited(mouseEvent -> {});
                star.setOnMouseEntered(mouseEvent -> {});
            }
            else{
                star.setOnMouseExited(mouseEvent -> Utils.fillStars(index+1, ratingHBox));
                star.setOnMouseEntered(mouseEvent -> starOnMouseOverHandler(star));
            }

        }
    }

    /**
     * Handles the click on the like/unlike button
     */
    public void likeCourseButtonHandler(){
        User loggedUser = Session.getLocalSession().getLoggedUser();
        if(neo4jDriver.isCourseLikedByUser(course, loggedUser)){
            //dislike
            neo4jDriver.dislikeCourse(loggedUser, course);
            likeCourseButton.setText("Like");
            likes = likes - 1;
            likesLabel.setText("Like: " + likes);
        }
        else{
            //like
            neo4jDriver.likeCourse(loggedUser, course);
            likeCourseButton.setText("Unlike");
            likes = likes + 1;
            likesLabel.setText("Like: " + likes);
        }
    }

    /**
     * Loads all course information, adjusting the GUI if the logged user is also the owner
     * @param isCourseMine is true if the course is owned by the logged user, false otherwise
     */
    private void loadCourseInformation(boolean isCourseMine){
        titleLabel.setText(course.getTitle());
        descriptionTextArea.setText(course.getDescription());
        descriptionTextArea.setEditable(isCourseMine);
        likes = neo4jDriver.getCourseLikes(course);
        likesLabel.setText("Like: " + likes);

        if(course.getCoursePic() != null){
            Image coursePicture;
            try {
                //coursePicture = new Image(course.getCoursePic());
                throw new IllegalArgumentException();
            } catch (IllegalArgumentException e) {
                coursePicture =  new Image(String.valueOf(CoursePageController.class.getResource("/img/courseDefaultImage.png")));
            }
            courseImageImageView.setImage(coursePicture);
            courseImageTextField.setText(course.getCoursePic());
        }
        courseImageTextField.setPromptText("Unknown");
        courseImageTextField.setEditable(isCourseMine);

        languageChoiceBox.setItems(FXCollections.observableArrayList(Utils.LANGUAGES));
        languageChoiceBox.setValue(course.getLanguage());
        levelChoiceBox.setItems(FXCollections.observableArrayList(Utils.LEVELS));
        levelChoiceBox.setValue(course.getLevel());
        languageChoiceBox.setDisable(!isCourseMine);
        levelChoiceBox.setDisable(!isCourseMine);

        String categories = "";
        if(course.getCategory() != null){
            List<String> listOfCategories = course.getCategory();
            for(String c : listOfCategories){
                categories += c;
                if(listOfCategories.indexOf(c) < listOfCategories.size()-1)
                    categories += ", ";
            }
        }
        categoryTextField.setText(categories);
        categoryTextField.setPromptText("Unknown");
        categoryTextField.setEditable(isCourseMine);
        instructorLabel.setText(course.getInstructor());
        modalityTextField.setText(course.getModality());
        modalityTextField.setPromptText("Unknown");
        modalityTextField.setEditable(isCourseMine);
        if (course.getDuration() == null)
            durationTextField.setPromptText("Unknown");
        else
            durationTextField.setText(Double.toString(course.getDuration()));

        if (course.getPrice() == null)
            priceTextField.setText("Free");
        else
            priceTextField.setText(Double.toString(course.getPrice()));

        durationTextField.setEditable(isCourseMine);
        priceTextField.setEditable(isCourseMine);
        courseLinkTextField.setText(course.getLink());
        courseLinkTextField.setPromptText("Unknown");
        courseLinkTextField.setEditable(isCourseMine);
    }

    /**
     * Handles the editing of the course information by the logged user (owner)
     */
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

        String coursePic = courseImageTextField.getText();
        String modality = modalityTextField.getText();
        String description = descriptionTextArea.getText();
        String link = courseLinkTextField.getText();

        if (languageChoiceBox.getValue() == null) {
            Utils.showErrorAlert("Please choose a language");
            return;
        }
        String language = languageChoiceBox.getValue().toString();

        if(levelChoiceBox.getValue() == null) {
            Utils.showErrorAlert("Please choose a level value");
            return;
        }
        String level = levelChoiceBox.getValue().toString();

        double duration;
        try{
            duration = Double.parseDouble(durationTextField.getText());
        }catch (NumberFormatException ex) {
            Utils.showErrorAlert("Duration must be numeric!");
            durationTextField.setText("");
            return;
        }

        double price;
        try{
            price = Double.parseDouble(priceTextField.getText());
        }catch (NumberFormatException ex) {
            Utils.showErrorAlert("Duration must be numeric!");
            durationTextField.setText("");
            return;
        }

        //null values are not added to the db
        coursePic = (coursePic == null || coursePic.equals("")) ? null : coursePic;
        modality = (modality == null || modality.equals("")) ? null : modality;
        link = (link == null || link.equals("")) ? null : link;

        Course newCourse;
        if(categoryTextField.getText().equals("")) {
            newCourse = new Course(course.getTitle(), description, course.getInstructor(), language, level, duration, price, link, null,
                    modality, coursePic);
        }
        else
            newCourse = new Course(course.getTitle(), description, course.getInstructor(), language, level, duration, price, link, categoryList,
                     modality, coursePic);

        newCourse.setId(course.getId());
        newCourse.setNum_reviews(course.getNum_reviews());
        newCourse.setSum_ratings(course.getSum_ratings());

        if(LogicService.updateCourse(newCourse, course)) {
            Utils.showInfoAlert("Course's information updated with success!");
            course = newCourse;
        }
        else
            Utils.showErrorAlert("Error in updating course's information.");
    }

    /**
     * prepares the graph for the annual mean ratings of the course
     */
    private void loadAnnualMeanRatingLineChart () {
        HashMap<String, Double> courseAnnualMeanRating = mongoDBDriver.getCourseAnnualRatings(course);
        List<Integer> years = new ArrayList<>();
        for (String year : courseAnnualMeanRating.keySet()) {
            years.add(Integer.valueOf(year));
        }
        years.sort(Comparator.naturalOrder());

        series.getData().clear();
        for (int year: years){
            series.getData().add(new XYChart.Data(
                    String.valueOf(year), courseAnnualMeanRating.get(String.valueOf(year))));
        }

    }

}
