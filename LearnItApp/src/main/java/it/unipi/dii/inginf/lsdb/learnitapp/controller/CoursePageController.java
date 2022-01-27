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
    @FXML private TextField courseImageTextField;
    @FXML private HBox buttonsHBox;
    @FXML private VBox profileVBox;
    @FXML LineChart<String, Number> annualMeanRatingLineChart;
    @FXML private Label lastModifiedLabel;
    private XYChart.Series series;


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

        series = new XYChart.Series();
        series.setName("Mean rating");
        annualMeanRatingLineChart.getData().add(series);

        logoImageView.setOnMouseClicked(clickEvent -> backToHome(clickEvent));
        logoImageView.setCursor(Cursor.HAND);
    }

    public void backToHome(MouseEvent clickEvent){
        Utils.changeScene(Utils.DISCOVERY_PAGE, clickEvent);
    }

    public void setCourse(Course snapCourse){
        this.course = mongoDBDriver.getCourseByTitle(snapCourse.getTitle());

        moreReviewsImageView.setOnMouseClicked(clickEvent -> loadMore());
        moreReviewsImageView.setCursor(Cursor.HAND);

        User loggedUser = Session.getLocalSession().getLoggedUser();
        if (loggedUser.getRole() == User.Role.ADMINISTRATOR) {
            ImageView trash = new ImageView(new Image(
                    String.valueOf(CoursePageController.class.getResource(Utils.TRASH_BIN))));
            trash.setPreserveRatio(true);
            trash.setFitWidth(40);
            trash.setFitHeight(40);
            profileVBox.getChildren().remove(buttonsHBox);
            profileVBox.getChildren().add(trash);
            trash.setOnMouseClicked(clickEvent -> deleteCourse(clickEvent));
            trash.setCursor(Cursor.HAND);
            allContentVBox.getChildren().remove(newReviewVBox);
        } else {
            if (course.getInstructor().getUsername().equals(loggedUser.getUsername())) { // own course
                editCourseButton.setOnMouseClicked(clickEvent -> editCourseButtonHandler());
                editCourseButton.setCursor(Cursor.HAND);
                likeCourseButton.setVisible(false);
                newReviewVBox.setVisible(false);
                editCourseButton.setText("Edit course");
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

                        reviewTitleTextField.setText(myReview.getTitle());
                        commentTextArea.setText(myReview.getContent());
                        lastModifiedLabel.setText("Last-modified: "+myReview.getTimestamp());
                        editCourseButton.setVisible(false);
                        System.out.println("rating loaded: "+myReview.getRating());
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
                    }
                } else { // not reviewed course
                    saveReviewButton.setText("Save");
                    saveReviewButton.setOnMouseClicked(clickEvent2 -> saveReviewButtonHandler());
                    saveReviewButton.setCursor(Cursor.HAND);
                    commentTextArea.setEditable(true);
                    handleRatingStars(true);
                    editCourseButton.setVisible(false);
                }

                likeCourseButton.setOnMouseClicked(clickEvent -> likeCourseButtonHandler());
                likeCourseButton.setCursor(Cursor.HAND);

                if (neo4jDriver.isCourseLikedByUser(course, loggedUser)) {
                    likeCourseButton.setText("Dislike");
                }
            }
        }

        loadCourseInformation(loggedUser.getUsername().equals(course.getInstructor().getUsername()));
        loadAnnualMeanRatingLineChart();

        loadMore();
    }

    private void loadMore(){
        int skip = pageNumber*limit;
        pageNumber++;

        int toIndex = skip + limit;
        if (toIndex >= course.getReviews().size()) {
            toIndex = course.getReviews().size();
        }

        if (skip >= toIndex)
            return;

        List<Review> toAdd = course.getReviews().subList(skip, toIndex);
        createReviewsElements(toAdd, reviewsVBox);
    }

    public void deleteCourse(MouseEvent clickEvent) {
        if (DBOperations.deleteCourse(course)) {
            Utils.changeScene(Utils.DISCOVERY_PAGE, clickEvent);
        }
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
            reviewController.setReview(review, reviewsVBox);
            reviewController.setCourse(course);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return borderPane;
    }

    public void saveReviewButtonHandler(){
        if(saveReviewButton.getText().equals("Save")) { // save operation
            int rating = getRatingFromStars();
            System.out.println("rating received: "+rating);
            User loggedUser = Session.getLocalSession().getLoggedUser();
            User author = new User(loggedUser.getUsername(), loggedUser.getCompleteName());
            Date currentTimestamp = new Date();

            if(myReview==null) { // add review
                myReview = new Review(reviewTitleTextField.getText(), commentTextArea.getText(), rating, currentTimestamp, author);
                course.addReview(myReview);
                //System.out.println("add review \n title: "+ myReview.getTitle() + "\n content: "+ myReview.getContent() +"\n rating: "+ myReview.getRating()+"\n timestamp: "+myReview.getTimestamp().toString());

                if (DBOperations.addReview(myReview, course)) {
                    Utils.showInfoAlert("Added new review with success!");

                } else {
                    Utils.showErrorAlert("Error in adding the review");
                }
            }
            else{ // edit review

                System.out.println("here, i'm editing a review");
                myReview.setContent(commentTextArea.getText());
                myReview.setTitle(reviewTitleTextField.getText());
                myReview.setRating(rating);
                myReview.setTimestamp(currentTimestamp);

                //System.out.println("edit review \n title: "+ myReview.getTitle() + "\n content: "+ myReview.getContent() +"\n rating: "+ myReview.getRating()+"\n timestamp: "+myReview.getTimestamp().toString());

                if(mongoDBDriver.editReview(course, myReview)){
                    Utils.showInfoAlert("Review edited with success!");
                }
                else{
                    Utils.showErrorAlert("Error in editing the review");
                }
            }
            lastModifiedLabel.setText("Last-modified: "+myReview.getTimestamp());
            reviewTitleTextField.setEditable(false);
            commentTextArea.setEditable(false);

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

    private int getRatingFromStars(){
        int rating = 0;
        Image starOn = new Image(String.valueOf(CoursePageController.class.getResource(Utils.STAR_ON)));

        for(Node star: ratingHBox.getChildren()){
            ImageView starImageView = (ImageView)star;
            if(starImageView.getImage().getUrl().equals(starOn.getUrl()))
                rating++;

        }

        System.out.println("rating saved: "+rating);
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
        System.out.println("click on star "+index);
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

    public void likeCourseButtonHandler(){
        User loggedUser = Session.getLocalSession().getLoggedUser();
        if(neo4jDriver.isCourseLikedByUser(course, loggedUser)){
            //dislike
            neo4jDriver.dislikeCourse(loggedUser, course);
            likeCourseButton.setText("Like");
        }
        else{
            //like
            neo4jDriver.likeCourse(loggedUser, course);
            likeCourseButton.setText("Dislike");
        }
    }

    private void loadCourseInformation(boolean isCourseMine){
        titleLabel.setText(course.getTitle());
        instructorLabel.setText(course.getInstructor().getUsername());
        descriptionTextArea.setText(course.getDescription());
        descriptionTextArea.setEditable(isCourseMine);

        if(course.getCoursePic() != null){
            Image coursePicture = new Image(course.getCoursePic());
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
            //StringBuilder categories = new StringBuilder();
            for(String c : listOfCategories){
                categories += c;
                if(listOfCategories.indexOf(c) < listOfCategories.size()-1)
                    categories += ", ";
            }
        }
        categoryTextField.setText(categories);
        categoryTextField.setPromptText("Unknown");
        categoryTextField.setEditable(isCourseMine);
        instructorLabel.setText(course.getInstructor().getUsername());
        modalityTextField.setText(course.getModality());
        modalityTextField.setPromptText("Unknown");
        modalityTextField.setEditable(isCourseMine);
        durationTextField.setText(Double.toString(course.getDuration()));
        durationTextField.setEditable(isCourseMine);
        durationTextField.setPromptText("Unknown");
        priceTextField.setText(Double.toString(course.getPrice()));
        priceTextField.setEditable(isCourseMine);
        priceTextField.setPromptText("Unknown");
        courseLinkTextField.setText(course.getLink());
        courseLinkTextField.setPromptText("Unknown");
        courseLinkTextField.setEditable(isCourseMine);
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
        coursePic = (coursePic.equals("")) ? null : coursePic;
        modality = (modality.equals("")) ? null : modality;
        link = (link.equals("")) ? null : link;

        Course newCourse;
        if(categoryTextField.getText().equals("")) {
            newCourse = new Course(course.getTitle(), description, course.getInstructor(), language, level,
                    duration, price, link, modality, coursePic);
        }
        else
            newCourse = new Course(course.getTitle(), description, course.getInstructor(), language, categoryList,
                    level, duration, price, link, modality, coursePic);

        newCourse.setId(course.getId());
        course = newCourse;
        if(DBOperations.updateCourse(newCourse))
            Utils.showInfoAlert("Course's information updated with success!");
        else
            Utils.showErrorAlert("Error in updating course's information.");
    }

    public void loadAnnualMeanRatingLineChart () {
        HashMap<String, Double> courseAnnualMeanRating = mongoDBDriver.getCourseAnnualRatings(course);
        List<Integer> years = new ArrayList<>();
        List<Double> means = new ArrayList<>();
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
