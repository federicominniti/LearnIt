package it.unipi.dii.inginf.lsdb.learnitapp.service;

import com.mongodb.MongoException;
import it.unipi.dii.inginf.lsdb.learnitapp.log.LearnItLogger;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Review;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import org.apache.log4j.Logger;
import org.neo4j.driver.exceptions.Neo4jException;

public class LogicService {
    private static Neo4jDriver neo4jDriver = Neo4jDriver.getInstance();
    private static MongoDBDriver mongoDBDriver = MongoDBDriver.getInstance();
    private static Logger mongoLogger = LearnItLogger.getMongoLogger();
    private static Logger neo4jLogger = LearnItLogger.getNeo4jLogger();

    /**
     * Updates the information about a course
     * If the update fails on Neo4J, try to rollback by reupdating mongo with the old information
     * If the rollback fails, write to log the information about the error and the information about the course
     * to be restored
     */
    public static boolean updateCourse(Course newCourse, Course oldCourse){
        if (mongoDBDriver.updateCourse(newCourse, oldCourse)) {
            if (!neo4jDriver.updateCourse(newCourse)) {
                try {
                    return mongoDBDriver.updateCourse(oldCourse, oldCourse);
                } catch (MongoException e) {
                    mongoLogger.error(e.getMessage());
                    mongoLogger.error("UPDATE COURSE: ROLLBACK FAILED");
                    mongoLogger.error(oldCourse.toString());
                }
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes a course
     * If the delete operation fails on Neo4J, try to rollback by re-adding the course to mongo
     * If the rollback fails, write to log the information about the error and the information about the course
     * to be restored
     */
    public static boolean deleteCourse(Course course) {
        Course oldCourse = mongoDBDriver.getCourseByTitle(course.getTitle());
        if (mongoDBDriver.deleteCourse(course)) {
            if (!neo4jDriver.deleteCourse(course)) {
                try {
                    mongoDBDriver.addCourse(oldCourse);
                    return true;
                } catch (MongoException e) {
                    mongoLogger.error(e.getMessage());
                    mongoLogger.error("DELETE COURSE: ROLLBACK FAILED");
                    mongoLogger.error(oldCourse.toString());
                    return false;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a review to a course
     * If the add fails on Neo4J, try to rollback by deleting the review on mongo
     * If the rollback fails, write to log the information about the error and the information about the course
     * and the review
     */
    public static boolean addReview(Review newReview, Course course){
        try {
            mongoDBDriver.addReview(course, newReview);
        } catch (MongoException e) {
            return false;
        }

        if (!neo4jDriver.addReview(course, newReview.getUsername())) {
            try {
                mongoDBDriver.deleteReview(course, newReview, true);
                return false;
            } catch (MongoException e) {
                mongoLogger.error(e.getMessage());
                mongoLogger.error("ADD REVIEW: ROLLBACK FAILED");
                mongoLogger.error(course.toString());
                mongoLogger.error(newReview.toString());
                return false;
            }
        }

        return true;
    }

    /**
     * Deletes a review from a course
     * If the delete operation fails on Neo4J, try to rollback by re-adding to mongo the review
     * If the rollback fails, write to log the information about the error and the information about the course
     * and the review
     */
    public static boolean deleteReview(Review review, Course course){
        try {
            mongoDBDriver.deleteReview(course, review, false);
        } catch (MongoException e) {
            return false;
        }

        if (!neo4jDriver.deleteReview(course, review.getUsername())) {
            try {
                mongoDBDriver.deleteReview(course, review, false);
                return false;
            } catch (MongoException e) {
                mongoLogger.error(e.getMessage());
                mongoLogger.error("DELETE REVIEW: ROLLBACK FAILED");
                mongoLogger.error(course.toString());
                mongoLogger.error(review.toString());
                return false;
            }
        }

        return true;
    }


    /**
     * Adds a new course to the databases
     * If the add fails on Neo4J, try to rollback by deleting the course from mongo
     * If the rollback fails, write to log the information about the error and the information about the course
     */
    public static boolean addCourse(Course course){
        try {
            mongoDBDriver.addCourse(course);
        } catch (MongoException e) {
            return false;
        }

        if (!neo4jDriver.addCourse(course)) {
            try {
                mongoDBDriver.deleteCourse(course);
                return false;
            } catch (MongoException e) {
                mongoLogger.error(e.getMessage());
                mongoLogger.error("ADD COURSE: ROLLBACK FAILED");
                mongoLogger.error(course.toString());
                return false;
            }
        }

        return true;
    }

    /**
     * Deletes a user from the databases
     * If one of the operation goes bad, write to log the information about the error and the user to be deleted
     */
    public static boolean deleteUser(User user) {
        try {
            mongoDBDriver.deleteUserReviews(user);
        } catch (MongoException e) {
            return false;
        }

        try {
            mongoDBDriver.deleteUserCourses(user);
        } catch (MongoException e) {
            mongoLogger.error(e.getMessage());
            mongoLogger.error("DELETE USER: DELETE USER COURSES FAILED");
            mongoLogger.error(user.toString());
            return false;
        }

        try {
            neo4jDriver.deleteUser(user);
        } catch (Neo4jException e) {
            neo4jLogger.error(e.getMessage());
            neo4jLogger.error("DELETE USER: FAILED");
            neo4jLogger.error(user.toString());
            return false;
        }

        try {
            mongoDBDriver.deleteUser(user);
        } catch (MongoException e) {
            mongoLogger.error(e.getMessage());
            mongoLogger.error("DELETE USER: FAILED");
            mongoLogger.error(user.toString());
            return false;
        }

        return true;
    }

    /**
     * Updates the information about a user
     * If the update fails on Neo4J, try to rollback by reupdating mongo with the old information
     * If the rollback fails, write to log the information about the error and the information about the user
     * to be restored
     */
    public static boolean editProfileInfo(User newUser, User oldUser) {
        try {
            mongoDBDriver.editProfileInfo(newUser);
            if (newUser.getRole() == 1)
                return true;
        } catch (MongoException e) {
            return false;
        }

        if (!neo4jDriver.editProfileInfo(newUser)) {
            try {
                mongoDBDriver.editProfileInfo(oldUser);
            } catch (MongoException e) {
                mongoLogger.error(e.getMessage());
                mongoLogger.error("EDIT PROFILE INFO: ROLLBACK FAILED");
                mongoLogger.error(oldUser.toString());
            }

            return false;
        }

        return true;
    }

    /**
     * Adds a new user to the databases
     * If the add fails on Neo4J, try to rollback by deleting the user from mongo
     * If the rollback fails, write to log the information about the error and the information about the user
     */
    public static boolean addUser(User newUser) {
        try {
            mongoDBDriver.addUser(newUser);
            if (newUser.getRole() == 1)
                return true;
        } catch (MongoException e) {
            return false;
        }

        if (!neo4jDriver.registerUser(newUser.getUsername(), newUser.getGender(), newUser.getProfilePic())) {
            try {
                mongoDBDriver.deleteUser(newUser);
            } catch (MongoException e) {
                mongoLogger.error(e.getMessage());
                mongoLogger.error("ADD USER: ROLLBACK FAILED");
                mongoLogger.error(newUser.toString());
            }

            return false;
        }

        return true;
    }
}
