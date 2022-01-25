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
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import java.util.List;

public class DiscoveryPageController {
    @FXML private TextField searchTextField;
    @FXML private Button searchButton;
    @FXML private RadioButton coursesRadio;
    @FXML private RadioButton usersRadio;
    @FXML private ChoiceBox<Object> languageChoiceBox;
    @FXML private ChoiceBox<Object> levelChoiceBox;
    @FXML private TextField maxDurationTextField;
    @FXML private TextField maxPriceTextField;
    @FXML private Label usernameLabel;
    @FXML private ImageView profilePic;
    @FXML private VBox elementsVBox;
    @FXML private Button createNewCourseButton;

    private int[]pageNumber = {0, 0, 0};
    private ToggleGroup searchType;
    private Neo4jDriver neo4jDriver;
    private MongoDBDriver mongoDBDriver;
    private int limit;
    /*private ElementsLineController<Course> bestRating;
    private ElementsLineController<Course> trendingCourse;
    private ElementsLineController<Course> friendsCompletedLiked;
    private ElementsLineController<Course> instructorSuggestions;
    private ElementsLineController<User> suggestedUsers;*/
    private int currentI = 0;
    private int currentJ = 0;
    private GridPane gridPane;

    private final static String PROFILE_PAGE = "/fxml/ProfilePage.fxml";
    private final static String CREATE_NEW_COUSE_PAGE = "/fxml/newCoursePage.fxml";
    //private final static String USER_DEFAULT_PIC = "/img/userDefaultPic.png";


    public void initialize() {
        neo4jDriver = Neo4jDriver.getInstance();
        mongoDBDriver = MongoDBDriver.getInstance();
        limit = ConfigParams.getLocalConfig().getLimitNumber();

        pageNumber[0] = pageNumber[1] = pageNumber[2] = 0;

        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(15);
        gridPane.setVgap(15);
        //GridPane.setMargin(gridPane, new Insets(10, 10, 10, 10));

        fillInterfaceElements();
        usernameLabel.setOnMouseClicked(clickEvent -> myProfile(clickEvent));
        if (Session.getLocalSession().getLoggedUser().getRole() == User.Role.STANDARD) {
            profilePic.setOnMouseClicked(clickEvent -> myProfile(clickEvent));
            initializeSuggestions();
        }
        searchButton.setOnMouseClicked(clickEvent -> searchHandler(clickEvent));
        if (Session.getLocalSession().getLoggedUser().getRole() == User.Role.ADMINISTRATOR) {
            createNewCourseButton.setText("Create new admin");
            createNewCourseButton.setOnMouseClicked(clickEvent -> Utils.changeScene(Utils.REGISTRATION_PAGE, clickEvent));
            return;
        }
        else
            createNewCourseButton.setOnMouseClicked(clickEvent -> Utils.changeScene(CREATE_NEW_COUSE_PAGE, clickEvent));

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

    private void myProfile(MouseEvent clickEvent){
        ProfilePageController profilePageController =
                (ProfilePageController) Utils.changeScene(Utils.PROFILE_PAGE, clickEvent);
        profilePageController.setProfileUser(Session.getLocalSession().getLoggedUser());
    }

    private void fillInterfaceElements(){
        searchType = new ToggleGroup();
        coursesRadio.setToggleGroup(searchType);
        usersRadio.setToggleGroup(searchType);

        languageChoiceBox.setItems(FXCollections.observableArrayList(Utils.LANGUAGES));
        levelChoiceBox.setItems(FXCollections.observableArrayList(Utils.LEVELS));

        usernameLabel.setText(Session.getLocalSession().getLoggedUser().getUsername());
        if (Session.getLocalSession().getLoggedUser().getRole() == User.Role.ADMINISTRATOR)
            profilePic.setImage(new Image(String.valueOf(DiscoveryPageController.class.getResource("/img/learnitUserPropic.png"))));
        else
            profilePic.setImage(new Image(Session.getLocalSession().getLoggedUser().getProfilePic()));
    }

    private void initializeSuggestions(){
        User myUser = Session.getLocalSession().getLoggedUser();
        int skip = 0;

        //List<Course> bestRating = mongoDBDriver.findBestRatings(limit);
        Utils.addLine(elementsVBox, null, null, Utils.BEST_RATING);

        //List<Course> trending = mongoDBDriver.trendingCourses(limit);
        Utils.addLine(elementsVBox, null, null, Utils.TRENDING_COURSE);

        //List<Course> suggested = neo4jDriver.findSuggestedCourses(myUser, skip, limit);
        Utils.addLine(elementsVBox, null, null, Utils.FRIENDS_COMPLETED_LIKED);

        //List<Course> completedSuggestions = neo4jDriver.findSuggestedCoursesByCompletedCourses(myUser, skip, limit);
        Utils.addLine(elementsVBox, null, null, Utils.INSTRUCTORS_SUGGESTIONS);

        //List<User> suggestedUsers = neo4jDriver.findSuggestedUsers(myUser, skip, limit);
        Utils.addLine(elementsVBox, null, null, Utils.USER_SUGGESTIONS);
    }

    private void searchHandler(MouseEvent clickEvent){
        RadioButton selected = (RadioButton) searchType.getSelectedToggle();
        String title = "", level = "", language = "";
        double duration = -1, price = -1;
        title = searchTextField.getText();
        if (levelChoiceBox.getValue() == null)
            level = "";
        else
            level = levelChoiceBox.getValue().toString();
        if (languageChoiceBox.getValue() == null)
            language = "";
        else
            language = languageChoiceBox.getValue().toString();

        if (maxDurationTextField.getText().equals(""))
            duration = -1;
        else
            duration = Double.parseDouble(maxDurationTextField.getText());

        if (maxPriceTextField.getText().equals(""))
            price = -1;
        else
            price = Double.parseDouble(maxPriceTextField.getText());

        if(searchType.getSelectedToggle() == null && title.equals("") && level.equals("") && language.equals("") &&
                duration == -1 && price == -1) {
            elementsVBox.getChildren().clear();
            initializeSuggestions();
            return;
        }

        if(searchType.getSelectedToggle() == null){
            Utils.showErrorAlert("Please select user or course!");
            return;
        }

        if(selected.getText().equals("Courses") && title.equals("") && level.equals("") && language.equals("") &&
                duration == -1 && price == -1) {

            return;
        }else{
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
        searchTextField.setText("");
        searchType.selectToggle(null);
        levelChoiceBox.setDisable(false);
        levelChoiceBox.setValue(null);
        languageChoiceBox.setDisable(false);
        languageChoiceBox.setValue(null);
        maxPriceTextField.setText("");
        maxDurationTextField.setText("");
    }

    private void addMoreResearchedCourses(String title, String level, String language,
                                          double duration, double price){
        List<Course> searchedCourses = mongoDBDriver.findCourses(price, duration, title, level, language, ((currentI*4)-1), 15);
        for(int i = currentI; i<currentI + 4; i++){
            int j;
            if(i == currentI)
                j = currentJ;
            else
                j = 0;
            for(; j<4; j++) {
                System.out.println("i"+i+"j"+j);
                if(((i-currentI)*4+j) < searchedCourses.size() && searchedCourses.get((i-currentI)*4 + j) != null) {
                    Pane coursePane = Utils.loadCourseSnapshot(searchedCourses.get((i-currentI)*4 + j));
                    GridPane.setHalignment(coursePane, HPos.CENTER);
                    GridPane.setValignment(coursePane, VPos.CENTER);
                    gridPane.add(coursePane, j, i);
                }
                if((i == currentI + 3 && j == 3) || ((i-currentI)*4+j) >= searchedCourses.size()) {
                    ImageView more = new ImageView(new Image(
                            String.valueOf(DiscoveryPageController.class.getResource(Utils.READ_MORE))));
                    more.setPreserveRatio(true);
                    more.setFitWidth(100);
                    more.setFitWidth(100);
                    more.setCursor(Cursor.HAND);
                    more.setOnMouseClicked(newClickEvent -> addMoreResearchedCourses(title, level, language,
                            duration, price));
                    GridPane.setHalignment(more, HPos.CENTER);
                    GridPane.setValignment(more, VPos.CENTER);
                    gridPane.add(more, j, i);
                    currentI += i;
                    currentJ += j;
                    return;
                }
            }
        }
    }

    private void addMoreResearchedUsers(String username) {
        List<User> searchedUsers;
        if (currentI == 0)
            searchedUsers = neo4jDriver.searchUserByUsername(15, 0, username);
        else
            searchedUsers = neo4jDriver.searchUserByUsername(15, ((currentI * 4) - 1), username);
        for (int i = currentI; i < currentI + 4; i++) {
            int j;
            if (i == currentI)
                j = currentJ;
            else
                j = 0;
            for (; j < 4; j++) {
                System.out.println("i" + i + "j" + j);
                if (((i - currentI) * 4 + j) < searchedUsers.size() && searchedUsers.get((i - currentI) * 4 + j) != null) {
                    Pane userPane = Utils.loadUserSnapshot(searchedUsers.get((i-currentI)*4 + j));
                    GridPane.setHalignment(userPane, HPos.CENTER);
                    GridPane.setValignment(userPane, VPos.CENTER);
                    gridPane.add(userPane, j, i);
                }
                if ((i == currentI + 3 && j == 3) || ((i - currentI) * 4 + j) >= searchedUsers.size()) {
                    ImageView more = new ImageView(new Image(
                            String.valueOf(DiscoveryPageController.class.getResource(Utils.READ_MORE))));
                    more.setPreserveRatio(true);
                    more.setFitWidth(100);
                    more.setFitWidth(100);
                    more.setCursor(Cursor.HAND);
                    more.setOnMouseClicked(newClickEvent -> addMoreResearchedUsers(username));
                    GridPane.setHalignment(more, HPos.CENTER);
                    GridPane.setValignment(more, VPos.CENTER);
                    gridPane.add(more, j, i);
                    currentI += i;
                    currentJ += j;
                    return;
                }
            }
        }
    }

}
