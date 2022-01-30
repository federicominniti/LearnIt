package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User2;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.DBOperations;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
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
    @FXML private TextField completeNameTextField;
    @FXML private TextField emailTextField;
    @FXML private PasswordField passwordPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ChoiceBox<String> genderChoiceBox;
    @FXML private DatePicker birthDatePicker;
    @FXML private TextField propicTextField;
    @FXML private ImageView profileImageView;
    @FXML private ImageView learnitImageView;

    private User2 loggedUser;

    public void initialize() {
        loggedUser = Session.getLocalSession().getLoggedUser();
        learnitImageView.setOnMouseClicked(clickEvent -> backToHomeButtonHandler(clickEvent));
        learnitImageView.setCursor(Cursor.HAND);
        saveButton.setOnMouseClicked(clickEvent -> saveButtonHandler(clickEvent));
        saveButton.setCursor(Cursor.HAND);

        usernameLabel.setText(loggedUser.getUsername());
        completeNameTextField.setText(loggedUser.getCompleteName());
        emailTextField.setText(loggedUser.getEmail());

        genderChoiceBox.getItems().add("Male");
        genderChoiceBox.getItems().add("Female");
        genderChoiceBox.getItems().add("-");
        if (loggedUser.getGender() != null)
            genderChoiceBox.setValue(loggedUser.getGender());

        if (loggedUser.getProfilePic() != null) {
            propicTextField.setText(loggedUser.getProfilePic());
            profileImageView.setImage(new Image(loggedUser.getProfilePic()));
        }

        birthDatePicker.setValue(loggedUser.getDateOfBirth().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate());
    }

    public void backToHomeButtonHandler(MouseEvent clickEvent) {
        ProfilePageController profilePageController =
                (ProfilePageController) Utils.changeScene(Utils.PROFILE_PAGE, clickEvent);
        profilePageController.setProfileUser(Session.getLocalSession().getLoggedUser());
    }

    public void saveButtonHandler(MouseEvent clickEvent) {
        boolean ret;
        Date birthDate;

        if (!passwordPasswordField.getText().equals("")) {
            if (validatePassword())
                return;
        }

        birthDate = getValueFromDatePicker();
        if (birthDate == null) {

        }
        ret = editProfileInfo(birthDate);

        if (ret) {
            Utils.showInfoAlert("Your personal information was updated with success!");
        } else {
            Utils.showInfoAlert("Something has gone wrong in updating your personal information");
        }
    }

    private boolean validatePassword() {
        if (!passwordPasswordField.getText().equals(confirmPasswordField.getText())) {
            Utils.showErrorAlert("The passwords do not match!");
            return true;
        } else if (!Utils.isPasswordSecure(passwordPasswordField.getText())) {
            Utils.showErrorAlert("Not secure password, try another password");
            return true;
        }

        return false;
    }

    private Date getValueFromDatePicker() {
        Date birthDate = loggedUser.getDateOfBirth();
        if (birthDatePicker.getValue() != null) {
            birthDate = Date.from(birthDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            LocalDate today = LocalDate.now();

            Period period = Period.between(birthDatePicker.getValue(), today);
            if (period.getYears() < 18) {
                Utils.showErrorAlert("Only over 18 people can join LearnIt!");
                return loggedUser.getDateOfBirth();
            }
        }

        return birthDate;
    }

    private boolean editProfileInfo(Date birthDate) {
        String password = passwordPasswordField.getText();
        if (password.equals(""))
            password = loggedUser.getPassword();

        String complete_name = completeNameTextField.getText();
        if (complete_name.equals(""))
            complete_name = loggedUser.getCompleteName();

        String propic = null;
        if (!propicTextField.getText().equals(""))
            propic = propicTextField.getText();

        String gender = null;
        if (genderChoiceBox.getValue() != null)
            gender = genderChoiceBox.getValue();

        String email = emailTextField.getText();
        if (email.equals(""))
            email = loggedUser.getEmail();

        User2 editedUser = new User2();
        editedUser.setUsername(loggedUser.getUsername());
        editedUser.setPassword(password);
        editedUser.setGender(gender);
        editedUser.setEmail(email);
        editedUser.setDateOfBirth(birthDate);
        editedUser.setRole(0);
        editedUser.setCompleteName(complete_name);
        editedUser.setProfilePic(propic);

        if (DBOperations.editProfile(editedUser)) {
            Session.getLocalSession().setLoggedUser(editedUser);
            return true;
        }

        return false;
    }
}
