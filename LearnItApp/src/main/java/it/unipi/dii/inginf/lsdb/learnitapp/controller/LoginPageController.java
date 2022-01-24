package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class LoginPageController {
    @FXML private TextField usernameTextField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button signUpButton;

    //roba db

    public void initialize() {
        loginButton.setOnMouseClicked(clickEvent -> loginHandler(clickEvent));
        signUpButton.setOnMouseClicked(clickEvent -> signUpHandler(clickEvent));
    }

    public void loginHandler(MouseEvent clickEvent) {
        Neo4jDriver neo4j = Neo4jDriver.getInstance();
        User loggedUser = neo4j.login(usernameTextField.getText(), passwordField.getText());
        if (loggedUser == null) {
            Utils.showErrorAlert("Error: wrong username or password");
        } else {
            Session.getLocalSession().setLoggedUser(loggedUser);
            Utils.changeScene("/fxml/DiscoveryPage.fxml", clickEvent);
        }
    }

    public void signUpHandler(MouseEvent clickEvent) {
        Utils.changeScene("/fxml/RegistrationPage.fxml", clickEvent);
    }
}
