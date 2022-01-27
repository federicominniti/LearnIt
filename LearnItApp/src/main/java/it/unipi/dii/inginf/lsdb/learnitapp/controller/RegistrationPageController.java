package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

public class RegistrationPageController {

    @FXML private Button signUpButton;
    @FXML private TextField usernameTextField;
    @FXML private PasswordField passwordPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField completeNameTextField;
    @FXML private TextField emailTextField;
    @FXML private DatePicker birthDatePicker;
    @FXML private ChoiceBox genderChoiceBox;
    @FXML private TextField propicTextField;
    @FXML private ImageView learnitLogoImageView;
    @FXML private ImageView learnitImageView;
    @FXML private Label completeNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label profilePictureLabel;
    @FXML private Label genderLabel;
    @FXML private Label birthDateLabel;

    private User admin;

    public void initialize() {
        admin = Session.getLocalSession().getLoggedUser();

        if (admin != null) {
            prepareForAdminCreation();
        } else {
            prepareForStandardUserCreation();
        }

    }

    private void prepareForAdminCreation() {
        learnitLogoImageView.setImage(new Image(
                String.valueOf(RegistrationPageController.class.getResource("/img/createAdmin.png"))));
        learnitImageView.setOnMouseClicked(clickEvent -> Utils.changeScene(Utils.DISCOVERY_PAGE, clickEvent));
        learnitLogoImageView.setCursor(Cursor.HAND);
        signUpButton.setOnMouseClicked(clickEvent -> createAdmin(clickEvent));
        signUpButton.setCursor(Cursor.HAND);

        signUpButton.setText("Create admin");
        signUpButton.setStyle("-fx-background-color: lightpink;" +
                "-fx-background-radius: 13px");
        completeNameTextField.setDisable(true);
        emailTextField.setDisable(true);
        birthDatePicker.setDisable(true);
        genderChoiceBox.setDisable(true);
        propicTextField.setDisable(true);
        completeNameLabel.setDisable(true);
        emailLabel.setDisable(true);
        birthDateLabel.setDisable(true);
        genderLabel.setDisable(true);
        profilePictureLabel.setDisable(true);
    }

    private void prepareForStandardUserCreation() {
        genderChoiceBox.getItems().add("Male");
        genderChoiceBox.getItems().add("Female");
        genderChoiceBox.getItems().add("-");
        birthDatePicker.getEditor().setDisable(true);
        birthDatePicker.getEditor().setOpacity(1);

        learnitLogoImageView.setOnMouseClicked(clickEvent ->
                Utils.changeScene(Utils.LOGIN_PAGE, clickEvent));
        learnitLogoImageView.setCursor(Cursor.HAND);
        learnitImageView.setOnMouseClicked(clickEvent ->
                Utils.changeScene(Utils.LOGIN_PAGE, clickEvent));
        learnitImageView.setCursor(Cursor.HAND);
        signUpButton.setOnMouseClicked(clickEvent -> signUpHandler(clickEvent));
        signUpButton.setCursor(Cursor.HAND);
    }

    public void createAdmin(MouseEvent clickEvent) {
        if (validateUsernameAndPassword())
            return;

        boolean ret = Neo4jDriver.getInstance().registerUser(usernameTextField.getText(), "", "", "", "", passwordPasswordField.getText(), "", true);
        if (ret) {
            Utils.showInfoAlert("Admin registered with success");
        } else {
            Utils.showInfoAlert("Error, registration failed");
        }

    }

    public void signUpHandler(MouseEvent clickEvent) {
        boolean ret;
        String birthDate;

        if (!validateNonOptionalFields())
            return;

        birthDate = getValueFromDatePicker();
        if (birthDate == null)
            return;

        ret = registerUser(birthDate);

        if (ret) {
            Utils.showInfoAlert("User registered with success");
            Utils.changeScene(Utils.LOGIN_PAGE, clickEvent);
        } else {
            Utils.showInfoAlert("Error, registration failed");
        }
    }

    private boolean validateUsernameAndPassword() {
        if (!passwordPasswordField.getText().equals(confirmPasswordField.getText())) {
            Utils.showErrorAlert("The passwords do not match!");
            return true;
        } else if (!Utils.isPasswordSecure(passwordPasswordField.getText())) {
            Utils.showErrorAlert("Not secure password, try another password");
            return true;
        } else if (usernameTextField.getText().length() < 5) {
            Utils.showErrorAlert("Username too short, try another username");
            return true;
        } else if (Neo4jDriver.getInstance().checkUserExists(usernameTextField.getText())) {
            Utils.showErrorAlert("Username already taken, try another username");
            return true;
        }
        return false;
    }

    private boolean validateNonOptionalFields() {
        if (emailTextField.getText().equals("") || completeNameTextField.getText().equals("")) {
            Utils.showErrorAlert("Please fill all non-optional fields");
            return false;
        }

        return !validateUsernameAndPassword();
    }

    private String getValueFromDatePicker() {
        String birthDate = "";
        if (birthDatePicker.getValue() != null) {
            Date dateBirth = Date.from(birthDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            LocalDate today = LocalDate.now();

            Period period = Period.between(birthDatePicker.getValue(), today);
            if (period.getYears() < 18) {
                Utils.showErrorAlert("Only over 18 people can join LearnIt!");
                return null;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            birthDate = sdf.format(dateBirth);
        }

        return birthDate;
    }

    private boolean registerUser(String birthDate) {
        String username = usernameTextField.getText();
        String password = passwordPasswordField.getText();
        String complete_name = completeNameTextField.getText();

        if (birthDate.equals(""))
            birthDate = null;

        String propic = null;
        if (!propicTextField.getText().equals(""))
            propic = propicTextField.getText();

        String gender = null;
        if (genderChoiceBox.getValue() != null)
            gender = genderChoiceBox.getValue().toString();
        return Neo4jDriver.getInstance().registerUser(username,
                complete_name,
                birthDate,
                gender,
                emailTextField.getText(),
                password,
                propic,
                false);

    }
}
