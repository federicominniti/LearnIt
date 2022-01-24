package it.unipi.dii.inginf.lsdb.learnitapp.app;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LearnIt extends Application {

    private final static String LOGIN_PAGE = "/fxml/LoginPage.fxml";

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(LearnIt.class.getResource(LOGIN_PAGE));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("LearnIt!");
        primaryStage.show();
        primaryStage.setResizable(false);
        ConfigParams.getInstance();

        MongoDBDriver m = MongoDBDriver.getInstance();
        Neo4jDriver neo4j = Neo4jDriver.getInstance();

        /*List<Course> courses = m.findBestRatings(10);
        for (Course c: courses) {
            System.out.println(c.getTitle());
        }*/

        primaryStage.setOnCloseRequest(windowEvent -> {
            m.closeConnection();
            neo4j.closeConnection();
            System.exit(0);
        });

    }

    public static void main(String[] args) {
        launch(args);
    }
}