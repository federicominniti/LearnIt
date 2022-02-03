package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.service.LogicService;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

public class NewCoursePageController {

    @FXML private ImageView textLogoImageView;
    @FXML private TextField titleTextField;
    @FXML private TextArea descriptionTextArea;
    @FXML private TextField coursePicTextField;
    @FXML private ChoiceBox<String> languageChoiceBox;
    @FXML private TextArea categoriesTextArea;
    @FXML private TextField hourTextField;
    @FXML private TextField modalityTextField;
    @FXML private ChoiceBox<String> levelChoiceBox;
    @FXML private TextField priceTextField;
    @FXML private Button backButton;
    @FXML private Button createButton;
    @FXML private TextField courseLinkTextField;

    private final int MIN_DESCRIPTION_LEN = 20;
    private final int MIN_TITLE_LEN = 10;

    public void initialize() {
        languageChoiceBox.setItems(FXCollections.observableArrayList(Utils.LANGUAGES));
        levelChoiceBox.setItems(FXCollections.observableArrayList(Utils.LEVELS));

        backButton.setOnMouseClicked(clickEvent -> Utils.changeScene(Utils.DISCOVERY_PAGE, clickEvent));
        backButton.setCursor(Cursor.HAND);
        createButton.setOnMouseClicked(clickEvent -> createButtonHandler(clickEvent));
        createButton.setCursor(Cursor.HAND);

        textLogoImageView.setOnMouseClicked(clickEvent -> Utils.changeScene(Utils.DISCOVERY_PAGE, clickEvent));
        textLogoImageView.setCursor(Cursor.HAND);
    }

    /**
     * Handles the creation of the course, carefully unsetting all the empty optional fields
     */
    private void createButtonHandler(MouseEvent clickEvent){
        if (!validateNonOptionalFields())
            return;

        String title = titleTextField.getText();
        String language = languageChoiceBox.getValue();
        String coursePic = coursePicTextField.getText();
        String modality = modalityTextField.getText();
        String description = descriptionTextArea.getText();
        String level = levelChoiceBox.getValue();
        double price;
        double duration;
        try {
            price = Double.parseDouble(priceTextField.getText());
            if (price < 0)
                price = -1;
        } catch (NumberFormatException nf) {
            price = -1;
        }

        try {
            duration = Double.parseDouble(hourTextField.getText());
            if (duration < 0)
                duration = -1;
        } catch (NumberFormatException nf) {
            duration = -1;
        }

        String link = courseLinkTextField.getText();
        List<String> categoryList = null;
        if (categoriesTextArea.getText().contains(",")){
            String[] categories = categoriesTextArea.getText().split(", ", -1);
            categoryList = new ArrayList<>(List.of(categories));
        }else
        if(categoriesTextArea.getText().length() !=0) {
            categoryList = new ArrayList<>();
            categoryList.add(categoriesTextArea.getText());
        }

        //null values are not added to the db
        coursePic = (coursePic.equals("")) ? null : coursePic;
        modality = (modality.equals("")) ? null : modality;
        link = (link.equals("")) ? null : link;

        Course newCourse = new Course();
        newCourse.setTitle(title);
        newCourse.setDescription(description);
        newCourse.setInstructor(Session.getLocalSession().getLoggedUser().getUsername());
        newCourse.setLanguage(language);
        newCourse.setLevel(level);
        if (duration != -1)
            newCourse.setDuration(duration);
        if (price != -1)
            newCourse.setPrice(price);
        newCourse.setCoursePic(coursePic);
        newCourse.setModality(modality);
        newCourse.setLink(link);
        if (!categoriesTextArea.getText().equals(""))
            newCourse.setCategory(categoryList);

        if(LogicService.addCourse(newCourse)){
            Utils.showInfoAlert("Course added successfully!");
            CoursePageController coursePageController = (CoursePageController) Utils.changeScene(Utils.COURSE_PAGE, clickEvent);
            coursePageController.setCourse(newCourse);
        }
        else
            Utils.showErrorAlert("Error in adding course");

    }

    /**
     * Validates non optional fields: description, title, language, level
     */
    private boolean validateNonOptionalFields() {
        if(descriptionTextArea.getText().length() < MIN_DESCRIPTION_LEN){
            Utils.showErrorAlert("Too short description");
            return false;
        }

        if(titleTextField.getText().length() < MIN_TITLE_LEN){
            Utils.showErrorAlert("Too short course title");
            return false;
        }

        if(languageChoiceBox.getValue() == null){
            Utils.showErrorAlert("Please insert a language");
            return false;
        }
        if (levelChoiceBox.getValue() == null) {
            Utils.showErrorAlert("Please insert a course level");
            return false;
        }

        if (MongoDBDriver.getInstance().courseAlreadyExists(titleTextField.getText())) {
            Utils.showErrorAlert("Course title already exists!");
            return false;
        }
        return true;
    }
}
