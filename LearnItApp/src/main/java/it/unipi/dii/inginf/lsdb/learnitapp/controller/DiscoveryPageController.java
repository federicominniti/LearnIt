package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.neo4j.driver.internal.shaded.io.netty.handler.ssl.ClientAuth;
import org.w3c.dom.events.Event;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryPageController {
    @FXML private TextField searchTextField;
    @FXML private Button searchButton;
    @FXML private RadioButton coursesRadio;
    @FXML private RadioButton usersRadio;
    @FXML private ChoiceBox languageChoiceBox;
    @FXML private ChoiceBox levelChoiceBox;
    @FXML private ChoiceBox durationChoiceBox;
    @FXML private ChoiceBox priceChoiceBox;
    @FXML private Label usernameLabel;
    @FXML private ImageView profilePic;
    @FXML private HBox bestRatingHBox;
    @FXML private HBox trendingHBox;
    @FXML private HBox friendsCompletedLikedHBox;
    @FXML private HBox instructorSuggestionsHBox;
    @FXML private HBox usersHBox;
    @FXML private ImageView readMore1Image;
    @FXML private ImageView readMore2Image;
    @FXML private ImageView readMore3Image;
    private int[]pageNumber = {0, 0, 0};
    private ToggleGroup searchType;
    private Neo4jDriver neo4jDriver;
    private MongoDBDriver mongoDBDriver;
    private int limit;

    private String []languages = {"Arabic", "Chinese", "English", "French", "German", "Hebrew", "Hungarian", "Indonesian",
        "Italian", "Japanese", "Korean", "Portuguese", "Russian", "Spanish", "Swedish", "Turkish", "Ukrainian", "Albanian",
        "Azeri", "Bengali", "Bulgarian", "Burmese", "Croatian", "Czech", "Greek", "Hindi", "Estonian", "Filipino",
        "Indonesia", "Lithuanian", "Malay", "Marathi", "Nederlands", "Norwegian", "Persian", "Polski", "Română", "Serbian",
        "Swahili", "Tamil", "Telugu", "Urdu", "Vietnamese", "русский", "ไทย", "日本語", "简体中文", "繁體中文", "한국어"};

    private String []levels = {"All Levels", "Beginner", "Expert", "Intermediate"};
    private String []durations = {"1-30 ore", "30-60 ore", "60-90 ore", "90-120 ore", ">120 ore"};
    private String []prices = {"0-30 €", "30-70  €", "70-100 €", "100-200 €", ">200 €"};
    private final static String PROFILE_PAGE = "/fxml/ProfilePage.fxml";
    private final static String COURSE_DEFAULT_PIC = "/img/courseDefaultPic.png";
    private final static String USER_DEFAULT_PIC = "/img/userDefaultPic.png";
    private final static int FRIENDS_COMPLETED_LIKED = 1;
    private final static int INSTRUCTORS_SUGGESTIONS = 2;
    private final static int USER_SUGGESTIONS = 3;

    public void initialize() {
        neo4jDriver = Neo4jDriver.getInstance();
        mongoDBDriver = MongoDBDriver.getInstance();
        limit = ConfigParams.getLocalConfig().getLimitNumber();

        bestRatingHBox.getChildren().clear();
        trendingHBox.getChildren().clear();
        friendsCompletedLikedHBox.getChildren().clear();
        instructorSuggestionsHBox.getChildren().clear();
        usersHBox.getChildren().clear();
        pageNumber[0] = pageNumber[1] = pageNumber[2] = 0;

        fillInterfaceElements();
        usernameLabel.setOnMouseClicked(clickEvent -> Utils.changeScene(PROFILE_PAGE, clickEvent));
        profilePic.setOnMouseClicked(clickEvent -> Utils.changeScene(PROFILE_PAGE,clickEvent));
        fillSuggestions();
        searchButton.setOnMouseClicked(clickEvent -> searchHandler(clickEvent));
        readMore1Image.setOnMouseClicked(clickEvent -> loadMore(FRIENDS_COMPLETED_LIKED));
        readMore2Image.setOnMouseClicked(clickEvent -> loadMore(INSTRUCTORS_SUGGESTIONS));
        readMore3Image.setOnMouseClicked(clickEvent -> loadMore(USER_SUGGESTIONS));
    }

    private void fillInterfaceElements(){
        searchType = new ToggleGroup();
        coursesRadio.setToggleGroup(searchType);
        usersRadio.setToggleGroup(searchType);

        languageChoiceBox = new ChoiceBox(FXCollections.observableArrayList(languages));
        levelChoiceBox = new ChoiceBox(FXCollections.observableArrayList(levels));
        durationChoiceBox = new ChoiceBox(FXCollections.observableArrayList(durations));
        priceChoiceBox = new ChoiceBox(FXCollections.observableArrayList(prices));

        usernameLabel.setText(Session.getLocalSession().getLoggedUser().getUsername());
        profilePic.setImage(new Image(Session.getLocalSession().getLoggedUser().getProfilePic()));

        bestRatingHBox.setPrefHeight(180);
        trendingHBox.setPrefHeight(180);
        friendsCompletedLikedHBox.setPrefHeight(180);
        instructorSuggestionsHBox.setPrefHeight(180);
        usersHBox.setPrefHeight(180);
    }

    private void createCoursesElements(List<Course> courses, HBox coursesList){
        for(int i = 0; i<courses.size(); i++){
            VBox course = new VBox();
            course.setAlignment(Pos.CENTER);
            course.setPrefHeight(180);
            course.setPrefWidth(180);
            Label title = new Label();
            title.setText(courses.get(i).getTitle());
            title.setFont(Font.font("Arial", FontPosture.ITALIC, 15));
            course.getChildren().add(title);

            ImageView coursePic;
            if(courses.get(i).getCoursePic() != null)
                coursePic = new ImageView(new Image(courses.get(i).getCoursePic()));
            else
                coursePic = new ImageView(new Image(COURSE_DEFAULT_PIC));
            coursePic.setFitHeight(77);
            coursePic.setFitWidth(83);
            coursePic.setPreserveRatio(true);
            course.getChildren().add(coursePic);

            Label instructor = new Label();
            instructor.setText(courses.get(i).getInstructor().getUsername());
            instructor.setFont(Font.font(instructor.getFont().getFamily(), FontWeight.NORMAL, 15));
            course.getChildren().add(instructor);

            Label rating = new Label();
            rating.setText(String.valueOf(courses.get(i).getSum_ratings()/courses.get(i).getNum_reviews()) + "/5");
            rating.setFont(Font.font(rating.getFont().getFamily(), FontWeight.NORMAL, 15));
            course.getChildren().add(rating);

            Label price = new Label();
            price.setText(courses.get(i).getPrice() + "€");
            price.setFont(Font.font(price.getFont().getFamily(), FontWeight.NORMAL, 15));
            course.getChildren().add(price);

            coursesList.getChildren().add(course);
        }

    }

    private void createUsersElements(List<User> users, HBox usersList){
        for(int i = 0; i<users.size(); i++){
            VBox user = new VBox();
            user.setAlignment(Pos.CENTER);
            user.setPrefHeight(180);
            user.setPrefWidth(180);
            Label username = new Label();
            username.setText(users.get(i).getUsername());
            username.setFont(Font.font("Arial", FontPosture.ITALIC, 15));
            user.getChildren().add(username);

            ImageView userPic;
            if(users.get(i).getProfilePic() != null)
                userPic = new ImageView(new Image(users.get(i).getProfilePic()));
            else
                userPic = new ImageView(new Image(USER_DEFAULT_PIC));
            userPic.setFitHeight(77);
            userPic.setFitWidth(83);
            userPic.setPreserveRatio(true);
            user.getChildren().add(userPic);

            Label completeName = new Label();
            completeName.setText(users.get(i).getCompleteName());
            completeName.setFont(Font.font(completeName.getFont().getFamily(), FontWeight.NORMAL, 15));
            user.getChildren().add(completeName);

            Label gender = new Label();
            if(users.get(i).getGender() != null)
                gender.setText(users.get(i).getGender());
            else
                gender.setText("-");
            gender.setFont(Font.font(gender.getFont().getFamily(), FontWeight.NORMAL, 15));
            user.getChildren().add(gender);

            Label totCourses = new Label();
            totCourses.setText("Completed courses: " + neo4jDriver.findTotCourses(users.get(i)));
            totCourses.setFont(Font.font(totCourses.getFont().getFamily(), FontWeight.NORMAL, 15));
            user.getChildren().add(totCourses);

            usersList.getChildren().add(user);
        }

    }

    private void loadMore(int index){
        User myUser = Session.getLocalSession().getLoggedUser();
        int skip = pageNumber[index-1]*limit;
        pageNumber[index-1]++;
        List<Course> courses = new ArrayList<>();
        List<User> users = new ArrayList<>();
        switch (index){
            case FRIENDS_COMPLETED_LIKED:
                courses = neo4jDriver.findSuggestedCourses(myUser, skip, limit);
                createCoursesElements(courses, friendsCompletedLikedHBox);
                break;
            case INSTRUCTORS_SUGGESTIONS:
                courses = neo4jDriver.findSuggestedCoursesByCompletedCourses(myUser, skip, limit);
                createCoursesElements(courses, instructorSuggestionsHBox);
                break;
            default:
                users = neo4jDriver.findSuggestedUsers(myUser, skip, limit);
                createUsersElements(users, usersHBox);
        }
    }

    private void fillSuggestions(){
        bestRatingHBox.getChildren().clear();
        List<Course> bestRating = mongoDBDriver.findBestRatings(limit);
        createCoursesElements(bestRating, bestRatingHBox);

        trendingHBox.getChildren().clear();
        List<Course> trending = mongoDBDriver.trendingCourses(limit);
        createCoursesElements(trending, trendingHBox);

        loadMore(FRIENDS_COMPLETED_LIKED);
        loadMore(INSTRUCTORS_SUGGESTIONS);
        loadMore(USER_SUGGESTIONS);
    }

    private void searchHandler(MouseEvent clickEvent){}

}
