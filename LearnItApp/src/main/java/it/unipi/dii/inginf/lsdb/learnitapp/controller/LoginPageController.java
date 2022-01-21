package it.unipi.dii.inginf.lsdb.learnitapp.controller;

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

    }

    public void signUpHandler(MouseEvent clickEvent) {

    }
}
