package it.unipi.dii.inginf.lsdb.learnitapp.app;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

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

        MongoDBDriver xx = MongoDBDriver.getInstance();
        xx.initConnection();
        //Course c = new Course("prova");
        //xx.addCourse(c);
        //xx.deleteUserReviews(new User("boris", "pippo"));
        List<Course> c = xx.findCourses(15,15, "ictus", "Intermediate", "Spanish", 0, 10);
        System.out.println(c.get(0).getTitle());
        // PROVA MIa
    }

    public static void main(String[] args) {
        launch(args);
    }
}