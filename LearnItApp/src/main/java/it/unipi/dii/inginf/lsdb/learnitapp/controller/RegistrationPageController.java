package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class RegistrationPageController {

    @FXML private Button backToLoginButton;
    @FXML private Button signUpButton;
    @FXML private TextField usernameTextField;
    @FXML private PasswordField passwordPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField completeNameTextField;
    @FXML private TextField emailTextField;
    @FXML private DatePicker birthDatePicker;
    @FXML private ChoiceBox genderChoiceBox;
    @FXML private TextField propicTextField;
    private User admin;
    public void initialize() {
        admin = Session.getLocalSession().getLoggedUser();
        if (admin != null) {
            prepareForAdminCreation();
        } else {
            genderChoiceBox.getItems().add("Man");
            genderChoiceBox.getItems().add("Woman");
            genderChoiceBox.getItems().add("-");
            backToLoginButton.setOnMouseClicked(clickEvent -> backToLoginButtonHandler(clickEvent));
            signUpButton.setOnMouseClicked(clickEvent -> signUpHandler(clickEvent));
        }
    }

    public void backToLoginButtonHandler(MouseEvent clickEvent) {
        Utils.changeScene("/fxml/LoginPage.fxml", clickEvent);
    }

    public void prepareForAdminCreation() {
        backToLoginButton.setOnMouseClicked(clickEvent -> Utils.changeScene(Utils.DISCOVERY_PAGE, clickEvent));
        signUpButton.setText("Create admin");
        signUpButton.setOnMouseClicked(clickEvent -> createAdmin(clickEvent));
        completeNameTextField.setDisable(true);
        emailTextField.setDisable(true);
        birthDatePicker.setDisable(true);
        genderChoiceBox.setDisable(true);
        propicTextField.setDisable(true);
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

        if (emailTextField.getText().equals("") || completeNameTextField.getText() == null) {
            Utils.showErrorAlert("Complete name is not an optional field");
            return;
        } else if (emailTextField.getText().equals("") || emailTextField.getText() == null) {
            Utils.showErrorAlert("Email is not an optional field");
            return;
        }
        if (validateUsernameAndPassword())
            return;

        if (birthDatePicker.getValue() != null) {
            Date dateBirth = Date.from(birthDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            LocalDate today = LocalDate.now();

            Period period = Period.between(birthDatePicker.getValue(), today);
            if (period.getYears() < 18) {
                Utils.showErrorAlert("Only over 18 people can join LearnIt!");
                return;
            }
            birthDate = dateBirth.toString();
        } else {
            birthDate = "";
        }

        ret = Neo4jDriver.getInstance().registerUser(usernameTextField.getText(), completeNameTextField.getText(), birthDate, (String) genderChoiceBox.getValue(), emailTextField.getText(), passwordPasswordField.getText(), propicTextField.getText(), false);

        if (ret) {
            Utils.showInfoAlert("User registered with success");
            Utils.changeScene("/fxml/LoginPage.fxml", clickEvent);
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
}
