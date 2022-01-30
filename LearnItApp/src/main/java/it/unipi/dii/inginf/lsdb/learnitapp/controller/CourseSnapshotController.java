package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Course2;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class CourseSnapshotController {
    @FXML AnchorPane courseSnapshot;
    @FXML private Label titleLabel;
    @FXML private ImageView coursePicImage;
    @FXML private Label durationLabel;
    @FXML private Label priceLabel;
    private Course2 course;

    public void initialize(){
        courseSnapshot.setCursor(Cursor.HAND);
        courseSnapshot.setOnMouseClicked(mouseEvent -> showCompleteCourseInfo(mouseEvent));
    }

    public void setSnapshotCourse(Course2 course) {
        titleLabel.setText(course.getTitle());
        if(course.getCoursePic() != null){
            coursePicImage = new ImageView(new Image(course.getCoursePic()));
        }

        durationLabel.setText("Duration: " + course.getDuration() + " hour");
        priceLabel.setText("Price: " + course.getPrice() + " €");

        this.course = course;
    }

    private void showCompleteCourseInfo(MouseEvent mouseEvent){
        CoursePageController coursePageController =
                (CoursePageController) Utils.changeScene(Utils.COURSE_PAGE, mouseEvent);
        coursePageController.setCourse(course);
    }
}
