package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Review;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import org.w3c.dom.events.MouseEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CoursePageController {

    @FXML private Label titleLabel;
    @FXML private TextArea descriptionTextArea;
    @FXML private ImageView courseImageImageView;
    @FXML private Button likeCourseButton;
    @FXML private Button reviewCourseButton;
    @FXML private Button editCourseButton;
    @FXML private Label languageLabel;
    @FXML private Label categoryLabel;
    @FXML private Label levelLabel;
    @FXML private Label durationLabel;
    @FXML private Label priceLabel;
    @FXML private Label modalityLabel;
    @FXML private Label instructorLabel;
    @FXML private VBox reviewsVBox;
    @FXML private ImageView moreReviewsImageView;
    @FXML private Button backToHomeButton;

    private Course course;
    private int pageNumber = 0;
    private int limit;

    public void initialize() {
        limit = ConfigParams.getLocalConfig().getLimitNumber();

        backToHomeButton.setOnMouseClicked(clickEvent -> backToHomeButtonHandler(clickEvent));

        if(course.getInstructor().getUsername().equals(Session.getLocalSession().getLoggedUser().getUsername())){ // the session user is the instructor
            editCourseButton.setOnMouseClicked(clickEvent -> editCourseButtonHandler(clickEvent));
            reviewCourseButton.setVisible(false);
            likeCourseButton.setVisible(false);
        }
        else{
            editCourseButton.setVisible(false);
            reviewCourseButton.setOnMouseClicked(clickEvent -> reviewCourseButtonHandler(clickEvent));
            likeCourseButton.setOnMouseClicked(clickEvent -> likeCourseButtonHandler(clickEvent));
        }

        loadCourseInformation();
        loadReviews();
    }

    public void backToHomeButtonHandler(MouseEvent clickEvent){
        Utils.changeScene("/DiscoveryPage.fxml", clickEvent);
    }

    public void editCourseButtonHandler(MouseEvent clickEvent){
        // gestire ???
    }

    public void reviewCourseButtonHandler(MouseEvent clickEvent){

    }

    public void likeCourseButtonHandler(MouseEvent clickEvent){

    }

    private void loadMore(){
        List<Review> reviews = course.getReviews();
        List<Review> toAdd = new ArrayList<>();
        int skip = pageNumber*limit;
        pageNumber++;

        for(int i=1; i<=limit; i++){
            Review r = reviews.get(skip+i);
            toAdd.add(r);
        }
        createReviewsElements(toAdd, reviewsVBox);
    }

    public void setCourse(Course course){

        this.course = course; // ??? forse va fatta prima una find course su mongoDB perchè course potrebbe contenere solo lo snapshot.
    }

    private void loadCourseInformation(){
        titleLabel.setText(course.getTitle());
        descriptionTextArea.setText(course.getDescription());

        if(course.getCoursePic()!=null){
            // ??? gestione immagine corso
        }

        languageLabel.setText(course.getLanguage());

        if(!course.getCategory().isEmpty()){
            List<String> listOfCategories = course.getCategory();
            String categories = "";

            for(String c : listOfCategories){
                categories += c;
                if(listOfCategories.indexOf(c) < listOfCategories.size()-1)
                    categories += ", ";
            }
            categoryLabel.setText(categories);
        }
        else
            modalityLabel.setText("not specified");

        levelLabel.setText(course.getLevel());

        durationLabel.setText(course.getDuration());

        priceLabel.setText(course.getPrice());

        if(course.getModality()!=null)
            modalityLabel.setText(course.getModality());
        else
            modalityLabel.setText("not specified");

    }

    private void loadReviews(){
        List<Review> reviews = course.getReviews();

        List<Review> reviewsToLoad = new ArrayList<Review>();

        for(int i=0; i<limit; i++){
            if(i>= reviews.size())
                break;
            reviewsToLoad.add(reviews.get(i));
        }

        createReviewsElements(reviewsToLoad, reviewsVBox);
    }

    private BorderPane loadReview(Review review) {
        BorderPane borderPane = null;

        try {
            FXMLLoader loader = new FXMLLoader(Utils.class.getResource("/ReviewSnapshotPage.fxml"));
            borderPane = (BorderPane) loader.load();
            ReviewSnapshotPageController reviewController =
                    (ReviewSnapshotPageController) loader.getController();
            reviewController.setReview(review);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return borderPane;
    }

    private void createReviewsElements(List<Review> reviewsList, VBox container){
        BorderPane* reviewBorderPane;
        for(Review r: reviewsList){
            reviewBorderPane = loadReview();
            container.getChildren().add(reviewBorderPane);
        }
    }


}
