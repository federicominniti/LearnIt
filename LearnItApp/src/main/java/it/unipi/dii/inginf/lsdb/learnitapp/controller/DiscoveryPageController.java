package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

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
    @FXML private ImageView learnItImageView;
    @FXML private Button logoutButton;
    @FXML private Label discoveryPageLabel;
    @FXML private Button suggestionsSwitchButton;
    @FXML private AnchorPane allContentAnchorPane;

    private ToggleGroup searchType;
    private MongoDBDriver mongoDBDriver;
    private int currentI = 0;
    private int currentJ = 0;
    private GridPane gridPane;
    private int limit;

    private User loggedUser = Session.getLocalSession().getLoggedUser();

    private final static String CREATE_NEW_COURSE_PAGE = "/fxml/NewCoursePage.fxml";

    public void initialize() {
        mongoDBDriver = MongoDBDriver.getInstance();
        limit = ConfigParams.getLocalConfig().getLimitNumber();

        suggestionsSwitchButton.setCursor(Cursor.HAND);

        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(15);
        gridPane.setVgap(15);

        fillInterfaceElements();

        logoutButton.setOnMouseClicked(clickEvent -> Utils.logout(clickEvent));
        learnItImageView.setOnMouseClicked(clickEvent -> Utils.changeScene(Utils.DISCOVERY_PAGE, clickEvent));
        usernameLabel.setOnMouseClicked(clickEvent -> myProfile(clickEvent));
        usernameLabel.setCursor(Cursor.HAND);

        profilePic.setOnMouseClicked(clickEvent -> myProfile(clickEvent));
        profilePic.setCursor(Cursor.HAND);
        if (loggedUser.getRole() == 0) {
            coursesSuggestions();
        }else{
            allContentAnchorPane.getChildren().remove(discoveryPageLabel);
            allContentAnchorPane.getChildren().remove(suggestionsSwitchButton);
        }

        searchButton.setOnMouseClicked(clickEvent -> searchHandler(clickEvent));
        searchButton.setCursor(Cursor.HAND);

        createNewCourseButton.setCursor(Cursor.HAND);
        if (loggedUser.getRole() == 1) {
            createNewCourseButton.setText("Create new admin");
            createNewCourseButton.setStyle("-fx-background-color: lightpink;" +
                    "-fx-background-radius: 13px;" + "-fx-text-fill: red");
            createNewCourseButton.setOnMouseClicked(clickEvent -> Utils.changeScene(Utils.REGISTRATION_PAGE, clickEvent));
            return;
        }

        createNewCourseButton.setOnMouseClicked(clickEvent -> Utils.changeScene(CREATE_NEW_COURSE_PAGE, clickEvent));

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

    /**
     * Redirects to the profile page of the logged user
     */
    private void myProfile(MouseEvent clickEvent){
        ProfilePageController profilePageController =
                (ProfilePageController) Utils.changeScene(Utils.PROFILE_PAGE, clickEvent);
        profilePageController.setProfileUser(loggedUser);
    }

    /**
     * Loads the user's information to be shown in the page
     */
    private void fillInterfaceElements() {
        searchType = new ToggleGroup();
        coursesRadio.setToggleGroup(searchType);
        usersRadio.setToggleGroup(searchType);

        languageChoiceBox.setItems(FXCollections.observableArrayList(Utils.LANGUAGES));
        levelChoiceBox.setItems(FXCollections.observableArrayList(Utils.LEVELS));

        usernameLabel.setText(loggedUser.getUsername());

        if (loggedUser.getRole() == 1) {
            profilePic.setImage(new Image(String.valueOf(DiscoveryPageController.class.getResource(Utils.ADMIN_IMAGE))));
            return;
        }

        /*if (loggedUser.getProfilePic() == null)
            profilePic.setImage(new Image(String.valueOf(DiscoveryPageController.class.getResource(Utils.USER_DEFAULT))));
        else
            profilePic.setImage(new Image(loggedUser.getProfilePic()));*/
    }

    /**
     * Loads to the GUI the suggestions for the courses
     */
    private void coursesSuggestions(){
        suggestionsSwitchButton.setOnMouseClicked(clickEvent -> usersSuggestions());
        suggestionsSwitchButton.setText("Courses");
        suggestionsSwitchButton.setStyle("-fx-background-color: #9370DB; -fx-background-radius: 13");
        suggestionsSwitchButton.setTextFill(Paint.valueOf("#E6E6FA"));
        elementsVBox.getChildren().clear();
        Utils.addLine(elementsVBox, null, null, Utils.BEST_RATING);
        Utils.addLine(elementsVBox, null, null, Utils.TRENDING_COURSE);
        Utils.addLine(elementsVBox, null, null, Utils.COURSES_SUGGESTIONS);
        Utils.addLine(elementsVBox, null, null, Utils.INSTRUCTORS_SUGGESTIONS);
        Utils.addLine(elementsVBox, null, null, Utils.MOST_LIKED_COURSES);
    }

    /**
     * Loads to the GUI the suggestions for the users
     */
   private void usersSuggestions(){
       suggestionsSwitchButton.setOnMouseClicked(clickEvent -> coursesSuggestions());
       suggestionsSwitchButton.setText("Users");
       suggestionsSwitchButton.setStyle("-fx-background-color: #E6E6FA; -fx-background-radius: 13");
       suggestionsSwitchButton.setTextFill(Paint.valueOf("#9370DB"));
       elementsVBox.getChildren().clear();
       Utils.addLine(elementsVBox, null, null, Utils.BEST_USERS);
       Utils.addLine(elementsVBox, null, loggedUser, Utils.USER_SUGGESTIONS);
       Utils.addLine(elementsVBox, null, null, Utils.MOST_ACTIVE_USERS);
       Utils.addLine(elementsVBox, null, null, Utils.MOST_FOLLOWED_USERS);
   }

    /**
     * Handler for the search functionality
     */
    private void searchHandler(MouseEvent clickEvent){
        currentI = 0;
        currentJ = 0;
        RadioButton selected = (RadioButton) searchType.getSelectedToggle();
        String title, level, language;
        double duration, price;
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
            try {
                duration = Double.parseDouble(maxDurationTextField.getText());
            } catch (NumberFormatException nf) {
                maxDurationTextField.setText("");
                Utils.showErrorAlert("Duration must be a number");
                return;
            }

        if (maxPriceTextField.getText().equals(""))
            price = -1;
        else
            try {
                price = Double.parseDouble(maxPriceTextField.getText());
            } catch (NumberFormatException nf) {
                maxPriceTextField.setText("");
                Utils.showErrorAlert("Price must be a number");
                return;
            }

        if(searchType.getSelectedToggle() == null){
            Utils.showErrorAlert("Please select user or course!");
            return;
        }

        elementsVBox.getChildren().clear();
        gridPane.getChildren().clear();
        if((searchType.getSelectedToggle() == null && title.equals("") && level.equals("") && language.equals("") &&
                duration == -1 && price == -1) || (selected.getText().equals("Courses") && title.equals("") && level.equals("") && language.equals("") &&
                duration == -1 && price == -1)) {
            if (loggedUser.getRole() == 1)
                return;

            coursesSuggestions();
            allContentAnchorPane.getChildren().add(discoveryPageLabel);
            allContentAnchorPane.getChildren().add(suggestionsSwitchButton);
        }else if((selected.getText().equals("Users") && searchTextField.getText().equals(""))){
            if (loggedUser.getRole() == 1)
                return;
            usersSuggestions();
            allContentAnchorPane.getChildren().add(discoveryPageLabel);
            allContentAnchorPane.getChildren().add(suggestionsSwitchButton);
        } else if(selected.getText().equals("Courses")) {
            addMoreResearchedCourses(title, level, language, duration, price);
        } else if(selected.getText().equals("Users")) {
            String username = "";
            username = searchTextField.getText();
            addMoreResearchedUsers(username);
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
        allContentAnchorPane.getChildren().remove(discoveryPageLabel);
        allContentAnchorPane.getChildren().remove(suggestionsSwitchButton);
    }

    /**
     * Loads more courses after a search
     * @param title the searched title
     * @param level the searched level
     * @param language the searched language
     * @param duration the searched duration threshold
     * @param price the searched price threshold
     */
    private void addMoreResearchedCourses(String title, String level, String language, double duration, double price){
        List<Course> searchedCourses = mongoDBDriver.findCourses(price, duration, title, level, language, ((currentI*4)+currentJ), limit);
        for(int i = currentI; i<currentI + 4; i++){
            int j;
            if(i == currentI)
                j = currentJ;
            else
                j = 0;
            for(; j<4; j++) {
                //System.out.println("i"+i+"j"+j);
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
                    currentI = i;
                    currentJ = (j);
                    more.setOnMouseClicked(newClickEvent -> addMoreResearchedCourses(title, level, language,
                            duration, price));
                    more.setCursor(Cursor.HAND);
                    GridPane.setHalignment(more, HPos.CENTER);
                    GridPane.setValignment(more, VPos.CENTER);
                    gridPane.add(more, j, i);
                    //System.out.println("-------i"+currentJ+"j"+currentJ);
                    return;
                }
            }
        }
    }

    /**
     * Loads more users after a search
     * @param username the searched username
     */
    private void addMoreResearchedUsers(String username) {
        List<User> searchedUsers;
        if (currentI == 0)
            searchedUsers = mongoDBDriver.searchUserByUsername(username, 0, limit);
        else
            searchedUsers = mongoDBDriver.searchUserByUsername(username, ((currentI * 4) + currentJ), 15);
        for(int i = currentI; i<currentI + 4; i++){
            int j;
            if(i == currentI)
                j = currentJ;
            else
                j = 0;
            for(; j<4; j++) {
                if(((i-currentI)*4+j) < searchedUsers.size() && searchedUsers.get((i-currentI)*4 + j) != null) {
                    Pane coursePane = Utils.loadUserSnapshot(searchedUsers.get((i-currentI)*4 + j));
                    GridPane.setHalignment(coursePane, HPos.CENTER);
                    GridPane.setValignment(coursePane, VPos.CENTER);
                    gridPane.add(coursePane, j, i);
                }
                if((i == currentI + 3 && j == 3) || ((i-currentI)*4+j) >= searchedUsers.size()) {
                    ImageView more = new ImageView(new Image(
                            String.valueOf(DiscoveryPageController.class.getResource(Utils.READ_MORE))));
                    more.setPreserveRatio(true);
                    more.setFitWidth(100);
                    more.setFitWidth(100);
                    more.setCursor(Cursor.HAND);
                    currentI = i;
                    currentJ = (j);
                    more.setOnMouseClicked(newClickEvent -> addMoreResearchedUsers(username));
                    GridPane.setHalignment(more, HPos.CENTER);
                    GridPane.setValignment(more, VPos.CENTER);
                    gridPane.add(more, j, i);
                    return;
                }
            }
        }
    }

}
