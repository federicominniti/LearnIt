package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class CourseSnapshotController {
    @FXML AnchorPane courseSnapshot;
    @FXML private Label titleLabel;
    @FXML private ImageView coursePicImage;
    @FXML private Label durationLabel;
    @FXML private Label priceLabel;
    //the snapshot of the course to be shown
    private Course course;

    public void initialize(){
        courseSnapshot.setCursor(Cursor.HAND);
        courseSnapshot.setOnMouseClicked(mouseEvent -> showCompleteCourseInfo(mouseEvent));
    }

    /**
     * Prepares GUI for the snapshot of the course
     * @param course the snapshot to be shown
     */
    public void setSnapshotCourse(Course course) {
        titleLabel.setText(course.getTitle());
        /*
        if(course.getCoursePic() != null){
            coursePicImage = new ImageView(new Image(course.getCoursePic()));
        }
        */
        double duration = 0;
        double price = 0;
        if (course.getNewDuration() != null)
            duration = course.getNewDuration();
        else if (course.getDuration() != null)
            duration = course.getDuration();

        if (course.getNewPrice() != null)
            price = course.getNewPrice();
        else if (course.getPrice() != null)
            price = course.getPrice();

        if (duration == 0)
            durationLabel.setText("Duration: unknown");
        else
            durationLabel.setText("Duration: " + duration + " hour");

        if (price == 0)
            priceLabel.setText("Price: Free");
        else
            priceLabel.setText("Price: " + price + "€");

        this.course = course;
    }

    /**
     * Redirects to the course page
     */
    private void showCompleteCourseInfo(MouseEvent mouseEvent){
        Course fullCourse = MongoDBDriver.getInstance().getCourseByTitle(course.getTitle());
        if (fullCourse == null) {
            Utils.showInfoAlert("Sorry, this course doesn't exist anymore");
        } else {
            CoursePageController coursePageController =
                    (CoursePageController) Utils.changeScene(Utils.COURSE_PAGE, mouseEvent);
            coursePageController.setCourse(fullCourse);
        }
    }
}
