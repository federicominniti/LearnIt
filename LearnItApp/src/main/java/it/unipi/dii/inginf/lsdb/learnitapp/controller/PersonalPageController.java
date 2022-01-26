package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

public class PersonalPageController {

    @FXML private Label usernameLabel;
    @FXML private Button saveButton;
    @FXML private Button backToHomeButton;
    @FXML private TextField completeNameTextField;
    @FXML private TextField emailTextField;
    @FXML private PasswordField passwordPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ChoiceBox genderChoiceBox;
    @FXML private DatePicker birthDatePicker;
    @FXML private TextField propicTextField;
    @FXML private ImageView profileImageView;

    private User user;

    public void initialize() {
        user = Session.getLocalSession().getLoggedUser();

        backToHomeButton.setOnMouseClicked(clickEvent -> backToHomeButtonHandler(clickEvent));
        saveButton.setOnMouseClicked(clickEvent -> saveButtonHandler(clickEvent));

        usernameLabel.setText(user.getUsername());
        completeNameTextField.setText(user.getCompleteName());
        emailTextField.setText(user.getEmail());

        genderChoiceBox.setValue(user.getGender());
        if(user.getGender()==null){
            genderChoiceBox.getItems().add("F");
            genderChoiceBox.getItems().add("M");
        }
        else if(user.getGender().equals("F")){
            genderChoiceBox.getItems().add("M");
        }
        else
            genderChoiceBox.getItems().add("F");

        propicTextField.setText(user.getProfilePic());
        if(user.getProfilePic() != null)
            profileImageView.setImage(new Image(
                    String.valueOf(PersonalPageController.class.getResource(user.getProfilePic()))));
        passwordPasswordField.setText(user.getPassword());
        confirmPasswordField.setText(user.getPassword());
    }

    public void backToHomeButtonHandler(MouseEvent clickEvent) {
        ProfilePageController profilePageController =
                (ProfilePageController) Utils.changeScene(Utils.PROFILE_PAGE, clickEvent);
        profilePageController.setProfileUser(Session.getLocalSession().getLoggedUser());
    }

    public void saveButtonHandler(MouseEvent clickEvent) {

        boolean flag;
        Date dateBirth;

        if (!passwordPasswordField.getText().equals(confirmPasswordField.getText())) {
            Utils.showErrorAlert("The passwords do not match!");
            return;
        } else if (!Utils.isPasswordSecure(passwordPasswordField.getText())) {
            Utils.showErrorAlert("Not secure password, try another password");
            return;
        } else if (completeNameTextField.getText() == null) {
            Utils.showErrorAlert("Complete name is not an optional field");
            return;
        } else if (emailTextField.getText() == null) {
            Utils.showErrorAlert("Email is not an optional field");
            return;
        }

        if (birthDatePicker.getValue() != null) {
            dateBirth = Date.from(birthDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            LocalDate today = LocalDate.now();

            Period period = Period.between(birthDatePicker.getValue(), today);
            if (period.getYears() < 18) {
                Utils.showErrorAlert("You should have at least 18 years to be registered to LearnIt!");
                return;
            }
            user.setDateOfBirth(dateBirth);
        } else
            user.setDateOfBirth(null);

        user.setCompleteName(completeNameTextField.getText());
        user.setEmail(emailTextField.getText());
        user.setGender((String) genderChoiceBox.getValue());
        user.setProfilePic(propicTextField.getText());
        user.setPassword(passwordPasswordField.getText());

        flag = Neo4jDriver.getInstance().editProfileInfo(user);

        if(flag){
            Utils.showInfoAlert("Changes applied!");
            Utils.changeScene("/fxml/ProfilePage.fxml", clickEvent);
        }
        else
            Utils.showErrorAlert("Error, changes not applied");
    }
}
