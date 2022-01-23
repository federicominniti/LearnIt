package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
    @FXML private TextField maxDurationTextField;
    @FXML private TextField maxPriceTextField;
    @FXML private Label usernameLabel;
    @FXML private ImageView profilePic;
    @FXML private VBox elementsVBox;

    private int[]pageNumber = {0, 0, 0};
    private ToggleGroup searchType;
    private Neo4jDriver neo4jDriver;
    private MongoDBDriver mongoDBDriver;
    private int limit;
    private ElementsLineController<Course> bestRating;
    private ElementsLineController<Course> trendingCourse;
    private ElementsLineController<Course> friendsCompletedLiked;
    private ElementsLineController<Course> instructorSuggestions;
    private ElementsLineController<User> suggestedUsers;
    private int currentI;
    private GridPane gridPane;

    private String []languages = {"Arabic", "Chinese", "English", "French", "German", "Hebrew", "Hungarian", "Indonesian",
            "Italian", "Japanese", "Korean", "Portuguese", "Russian", "Spanish", "Swedish", "Turkish", "Ukrainian", "Albanian",
            "Azeri", "Bengali", "Bulgarian", "Burmese", "Croatian", "Czech", "Greek", "Hindi", "Estonian", "Filipino",
            "Indonesia", "Lithuanian", "Malay", "Marathi", "Nederlands", "Norwegian", "Persian", "Polski", "Română", "Serbian",
            "Swahili", "Tamil", "Telugu", "Urdu", "Vietnamese", "русский", "ไทย", "日本語", "简体中文", "繁體中文", "한국어"};

    private String []levels = {"All Levels", "Beginner", "Expert", "Intermediate"};
    private final static String PROFILE_PAGE = "/fxml/ProfilePage.fxml";
    //private final static String COURSE_DEFAULT_PIC = "/img/courseDefaultPic.png";
    //private final static String USER_DEFAULT_PIC = "/img/userDefaultPic.png";


    public void initialize() {
        neo4jDriver = Neo4jDriver.getInstance();
        mongoDBDriver = MongoDBDriver.getInstance();
        limit = ConfigParams.getLocalConfig().getLimitNumber();

        elementsVBox.getChildren().clear();
        pageNumber[0] = pageNumber[1] = pageNumber[2] = 0;

        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(10, 10, 10, 10));

        fillInterfaceElements();
        usernameLabel.setOnMouseClicked(clickEvent -> Utils.changeScene(PROFILE_PAGE, clickEvent));
        profilePic.setOnMouseClicked(clickEvent -> Utils.changeScene(PROFILE_PAGE,clickEvent));
        initializeSuggestions();
        searchButton.setOnMouseClicked(clickEvent -> searchHandler(clickEvent));

        searchType.selectedToggleProperty().addListener(
                (observable, oldToggle, newToggle) -> {
                    if (newToggle == usersRadio) {
                        maxDurationTextField.setDisable(true);
                        maxPriceTextField.setDisable(true);
                        languageChoiceBox.setDisable(true);
                        levelChoiceBox.setDisable(true);
                    } else if (newToggle == coursesRadio) {
                        maxDurationTextField.setDisable(false);
                        maxPriceTextField.setDisable(false);
                        languageChoiceBox.setDisable(false);
                        levelChoiceBox.setDisable(false);
                    }
                }
        );



    }

    private void fillInterfaceElements(){
        searchType = new ToggleGroup();
        coursesRadio.setToggleGroup(searchType);
        usersRadio.setToggleGroup(searchType);

        languageChoiceBox = new ChoiceBox(FXCollections.observableArrayList(languages));
        levelChoiceBox = new ChoiceBox(FXCollections.observableArrayList(levels));

        usernameLabel.setText(Session.getLocalSession().getLoggedUser().getUsername());
        profilePic.setImage(new Image(Session.getLocalSession().getLoggedUser().getProfilePic()));
    }

    private void initializeSuggestions(){
        User myUser = Session.getLocalSession().getLoggedUser();
        int skip = 0;

        List<Course> bestRating = mongoDBDriver.findBestRatings(limit);
        Utils.addLine(elementsVBox, bestRating, null, Utils.BEST_RATING);

        List<Course> trending = mongoDBDriver.trendingCourses(limit);
        Utils.addLine(elementsVBox, trending, null, Utils.TRENDING_COURSE);

        List<Course> suggested = neo4jDriver.findSuggestedCourses(myUser, skip, limit);
        Utils.addLine(elementsVBox, suggested, null, Utils.FRIENDS_COMPLETED_LIKED);

        List<Course> completedSuggestions = neo4jDriver.findSuggestedCoursesByCompletedCourses(myUser, skip, limit);
        Utils.addLine(elementsVBox, completedSuggestions, null, Utils.INSTRUCTORS_SUGGESTIONS);

        List<User> suggestedUsers = neo4jDriver.findSuggestedUsers(myUser, skip, limit);
        Utils.addLine(elementsVBox, null, suggestedUsers, Utils.USER_SUGGESTIONS);
    }

    private void searchHandler(MouseEvent clickEvent){
        if(searchType.getSelectedToggle() == null) {
            Utils.showErrorAlert("Please select user or course!");
            return;
        }

        RadioButton selected = (RadioButton) searchType.getSelectedToggle();
        String title = "", level = "", language = "";
        double duration = -1, price = -1;
        title = searchTextField.getText();
        level = levelChoiceBox.getValue().toString();
        language = languageChoiceBox.getValue().toString();
        duration = Double.parseDouble(maxDurationTextField.getText());
        price = Double.parseDouble(maxPriceTextField.getText());
        if(selected.getText().equals("Courses") && title.equals("") && level.equals("") && language.equals("") &&
                duration == -1 && price == -1)

            return;
        else{
            if(selected.getText().equals("Courses")) {
                elementsVBox.getChildren().clear();
                addMoreResearchedCourses(title, level, language, duration, price);
            }
            else{
                if(selected.getText().equals("Users") && searchTextField.getText().equals(""))
                    return;
                else{
                    String username = "";
                    username = searchTextField.getText();
                    elementsVBox.getChildren().clear();
                    addMoreResearchedUsers(username);
                }
            }
        }
        elementsVBox.getChildren().add(gridPane);
    }

    private void addMoreResearchedCourses(String title, String level, String language,
                                          double duration, double price){
        List<Course> searchedCourses = mongoDBDriver.findCourses(price, duration, title, level, language, ((currentI*4)-1), 15);
        for(int i = currentI; i<currentI + 4; i++){
            for(int j = 0; j<4; j++) {
                if(searchedCourses.get(i*4 + j) != null) {
                    Pane coursePane = Utils.loadCourseSnapshot(searchedCourses.get(i));
                    GridPane.setHalignment(coursePane, HPos.CENTER);
                    GridPane.setValignment(coursePane, VPos.CENTER);
                    gridPane.add(coursePane, i, j);
                }
                if(i == currentI + 3 && j == 3) {
                    Label more = new Label();
                    more.setText("Read more element..");
                    more.setOnMouseClicked(newClickEvent -> addMoreResearchedCourses(title, level, language,
                            duration, price));
                    GridPane.setHalignment(more, HPos.CENTER);
                    GridPane.setValignment(more, VPos.CENTER);
                    gridPane.add(more, i, j);
                }
            }
        }
        currentI += 4;
    }

    private void addMoreResearchedUsers(String username){
        List<User> searchedUsers = neo4jDriver.searchUserByUsername(15, ((currentI*4)-1), username);
        for(int i = currentI; i<currentI + 4; i++){
            for(int j = 0; j<4; j++) {
                if(searchedUsers.get(i*4 + j) != null) {
                    Pane coursePane = Utils.loadUserSnapshot(searchedUsers.get(i));
                    GridPane.setHalignment(coursePane, HPos.CENTER);
                    GridPane.setValignment(coursePane, VPos.CENTER);
                    gridPane.add(coursePane, i, j);
                }
                if(i == currentI + 3 && j == 3) {
                    Label more = new Label();
                    more.setText("Read more element..");
                    more.setOnMouseClicked(newClickEvent -> addMoreResearchedUsers(username));
                    GridPane.setHalignment(more, HPos.CENTER);
                    GridPane.setValignment(more, VPos.CENTER);
                    gridPane.add(more, i, j);
                }
            }
        }
        currentI += 4;
    }


    /*private void createCoursesElements(List<Course> courses, HBox coursesList){
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

    }*/

    /*private void createUsersElements(List<User> users, HBox usersList){
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

    }*/

    /*private void loadMore(int index){
        User myUser = Session.getLocalSession().getLoggedUser();
        int skip = pageNumber[index-1]*limit;
        pageNumber[index-1]++;
        List<Course> courses = new ArrayList<>();
        List<User> users = new ArrayList<>();
        switch (index){
            case FRIENDS_COMPLETED_LIKED:
                courses = neo4jDriver.findSuggestedCourses(myUser, skip, limit);
                //createCoursesElements(courses, friendsCompletedLikedHBox);
                Utils.addCoursesSnapshot(friendsCompletedLikedHBox, courses);
                break;
            case INSTRUCTORS_SUGGESTIONS:
                courses = neo4jDriver.findSuggestedCoursesByCompletedCourses(myUser, skip, limit);
                //createCoursesElements(courses, instructorSuggestionsHBox);
                Utils.addCoursesSnapshot(friendsCompletedLikedHBox, courses);
                break;
            default:
                users = neo4jDriver.findSuggestedUsers(myUser, skip, limit);
                //createUsersElements(users, usersHBox);
                Utils.addUsersSnapshot(usersHBox, users);
        }
    }*/

}
