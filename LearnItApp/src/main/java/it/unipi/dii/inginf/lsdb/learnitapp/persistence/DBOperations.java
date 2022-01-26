package it.unipi.dii.inginf.lsdb.learnitapp.persistence;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Review;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;

public class DBOperations {
    private static Neo4jDriver neo4jDriver = Neo4jDriver.getInstance();
    private static MongoDBDriver mongoDBDriver = MongoDBDriver.getInstance();

    public static boolean updateCourse(Course course){
        Course oldCourse = mongoDBDriver.getCourseFromTitle(course.getTitle(), 0, 0);
        if (mongoDBDriver.updateCourse(course)) {
            if (!neo4jDriver.updateCourse(course)) {
                mongoDBDriver.updateCourse(oldCourse);
                Utils.showErrorAlert("Something has gone wrong");
                return false;
            } else {
                Utils.showInfoAlert("Course updated successfully");
                return true;
            }
        }
        return false;
    }

    public static boolean deleteCourse(Course course) {
        Course oldCourse = mongoDBDriver.getCourseByTitle(course.getTitle());
        if (mongoDBDriver.deleteCourse(course)) {
            if (!neo4jDriver.deleteCourse(course)) {
                mongoDBDriver.addCourse(oldCourse);
                Utils.showErrorAlert("Something has gone wrong");
                return false;
            } else {
                Utils.showInfoAlert("Course removed successfully");
                return true;
            }
        }
        return false;
    }

    public static boolean addReview(Review newReview, Course course){
        if (mongoDBDriver.addReviewRedundancies(course, newReview)) {
            if (!neo4jDriver.addReview(course, newReview.getAuthor())) {
                mongoDBDriver.deleteReview(course, newReview);
                Utils.showErrorAlert("Something has gone wrong");
                return false;
            } else {
                Utils.showInfoAlert("Review added successfully");
                return true;
            }
        }
        return false;
    }

    public static boolean deleteReview(Review review, Course course){
        if (mongoDBDriver.deleteReview(course, review)) {
            if (!neo4jDriver.deleteReview(course, review.getAuthor())) {
                mongoDBDriver.addReview(course, review);
                Utils.showErrorAlert("Something has gone wrong");
                return false;
            } else {
                Utils.showInfoAlert("Review deleted");
                return true;
            }
        }
        return false;
    }


    public static boolean addCourse(Course course){
        if (mongoDBDriver.addCourse(course)) {
            if (!neo4jDriver.addCourse(course)) {
                mongoDBDriver.deleteCourse(course);
                Utils.showErrorAlert("Something has gone wrong");
                return false;
            } else {
                Utils.showInfoAlert("Course added successfully");
                return true;
            }
        }
        return false;
    }

    public static boolean deleteUser(User user) {
        boolean ret;
        ret = neo4jDriver.deleteUser(user);
        if (ret)
            ret = ret && mongoDBDriver.deleteUserCourses(user) && mongoDBDriver.deleteUserReviewsRedundancies(user);

        if (!ret)
            Utils.showErrorAlert("Something has gone wrong. Please retry");
        return ret;
    }
}
