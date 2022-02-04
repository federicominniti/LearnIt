package it.unipi.dii.inginf.lsdb.learnitapp.app;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LearnIt extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(LearnIt.class.getResource(Utils.LOGIN_PAGE));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("LearnIt!");
        primaryStage.show();
        primaryStage.setResizable(true);
        primaryStage.sizeToScene();

        ConfigParams.getInstance();
        MongoDBDriver mongo = MongoDBDriver.getInstance();
        Neo4jDriver neo4j = Neo4jDriver.getInstance();

        primaryStage.setOnCloseRequest(windowEvent -> {
            mongo.closeConnection();
            neo4j.closeConnection();
            System.exit(0);
        });

    }

    public static void main(String[] args) {
        launch(args);
    }
}