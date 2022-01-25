package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.DBOperations;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

public class NewCoursePageController {

    @FXML private ImageView textLogoImageView;
    @FXML private TextField titleTextField;
    @FXML private TextArea descriptionTextArea;
    @FXML private TextField coursePicTextField;
    @FXML private ChoiceBox languageChoiceBox;
    @FXML private TextArea categoriesTextArea;
    @FXML private TextField hourTextField;
    @FXML private TextField modalityTextField;
    @FXML private ChoiceBox levelChoiceBox;
    @FXML private TextField priceTextField;
    @FXML private Button backButton;
    @FXML private Button createButton;
    @FXML private TextField courseLinkTextField;

    public void initialize() {
        languageChoiceBox.setItems(FXCollections.observableArrayList(Utils.LANGUAGES));
        levelChoiceBox.setItems(FXCollections.observableArrayList(Utils.LEVELS));

        backButton.setOnMouseClicked(clickEvent -> Utils.changeScene(Utils.DISCOVERY_PAGE, clickEvent));
        createButton.setOnMouseClicked(clickEvent -> createButtonHandler(clickEvent));

        textLogoImageView.setOnMouseClicked(clickEvent -> Utils.changeScene(Utils.DISCOVERY_PAGE, clickEvent));
    }

    private void createButtonHandler(MouseEvent clickEvent){
        String title = titleTextField.getText();
        String language = languageChoiceBox.getValue().toString();
        String coursePic = coursePicTextField.getText();
        String modality = modalityTextField.getText();
        String []category = categoriesTextArea.getText().split(",");
        String description = descriptionTextArea.getText();
        String level = levelChoiceBox.getValue().toString();
        double duration = Double.parseDouble(hourTextField.getText());
        double price = Double.parseDouble(priceTextField.getText());
        String link = courseLinkTextField.getText();
        List<String> categoryList = new ArrayList<>(List.of(category));

        if(languageChoiceBox.getValue() == null || title == "" || levelChoiceBox.getValue() == null ||
            duration == 0){

            Utils.showErrorAlert("Please check that the required fields are inserted!");
            return;
        }

        Course newCourse;
        if(categoriesTextArea.getText().equals(""))
            newCourse = new Course(title, description, Session.getLocalSession().getLoggedUser(), language, level,
                 duration, price, link, modality, coursePic);
        else
            newCourse = new Course(title, description,  Session.getLocalSession().getLoggedUser(), language, categoryList,
                    level, duration, price, link, modality, coursePic);

        if(DBOperations.addCourse(newCourse))
            Utils.showInfoAlert("Course added!");
        Utils.changeScene("/DiscoveryPage.fxml", clickEvent);
    }
}
