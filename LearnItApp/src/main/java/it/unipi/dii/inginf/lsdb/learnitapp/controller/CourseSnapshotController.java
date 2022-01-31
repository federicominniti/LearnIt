package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
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
    private Course course;

    public void initialize(){
        courseSnapshot.setCursor(Cursor.HAND);
        courseSnapshot.setOnMouseClicked(mouseEvent -> showCompleteCourseInfo(mouseEvent));
    }

    public void setSnapshotCourse(Course course) {
        titleLabel.setText(course.getTitle());
        /*
        if(course.getCoursePic() != null){
            coursePicImage = new ImageView(new Image(course.getCoursePic()));
        }


         */
        if (course.getDuration() == null)
            durationLabel.setText("Duration: unknown");
        else
            durationLabel.setText("Duration: " + course.getDuration() + " hour");

        if (course.getPrice() == null)
            priceLabel.setText("Price: Free");
        else
            priceLabel.setText("Price: " + course.getPrice() + "€");

        this.course = course;
    }

    private void showCompleteCourseInfo(MouseEvent mouseEvent){
        CoursePageController coursePageController =
                (CoursePageController) Utils.changeScene(Utils.COURSE_PAGE, mouseEvent);
        coursePageController.setCourse(course);
    }
}
