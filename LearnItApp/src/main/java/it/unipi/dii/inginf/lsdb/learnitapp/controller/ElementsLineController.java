package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.util.List;

public class ElementsLineController {
    @FXML AnchorPane elementsLine;
    @FXML private Label headerLabel;
    @FXML private ImageView buttonImage;
    @FXML private HBox itemsHBox;

    private Neo4jDriver neo4jDriver;
    private Course course;
    private User user;
    private int listType;
    private int pageNumber;
    private int limit;
    private MongoDBDriver mongoDBDriver;

    public void initialize(){
        pageNumber = 0;
        limit = ConfigParams.getLocalConfig().getLimitNumber();
        neo4jDriver = Neo4jDriver.getInstance();
        mongoDBDriver = MongoDBDriver.getInstance();
    }

    public void setCoursesUsers(Course course, User user, int type){
        listType = type;
        this.course = course;
        this.user = user;
        switch (listType){
            case Utils.REVIEWED_COURSES:
                headerLabel.setText("Reviewed courses");
                break;
            case Utils.OFFERED_COURSES:
                headerLabel.setText("Offered courses");
                break;
            case Utils.LIKED_COURSES:
                headerLabel.setText("Liked courses");
                break;
            case Utils.FOLLOWING_USERS:
                headerLabel.setText("Users you are following");
                break;
            case Utils.FOLLOWER_USERS:
                headerLabel.setText("Users following you");
                break;
            case Utils.BEST_RATING:
                headerLabel.setText("Best rating courses");
                break;
            case Utils.TRENDING_COURSE:
                headerLabel.setText("Trending courses");
                break;
            case Utils.FRIENDS_COMPLETED_LIKED:
                headerLabel.setText("Your friend also liked/completed..");
                break;
            case Utils.INSTRUCTORS_SUGGESTIONS:
                headerLabel.setText("Your instructor also created..");
                break;
            case Utils.USER_SUGGESTIONS:
                headerLabel.setText("Suggested Users");
                break;
        }

        loadData();
        if(listType != Utils.BEST_RATING && listType != Utils.TRENDING_COURSE){
            buttonImage.setImage(new Image(
                    String.valueOf(ElementsLineController.class.getResource(Utils.READ_MORE))));
            buttonImage.setPreserveRatio(true);
            buttonImage.setFitWidth(40);
            buttonImage.setFitWidth(40);
            buttonImage.setOnMouseClicked(clickEvent -> loadMore());
            buttonImage.setCursor(Cursor.HAND);
        }
    }

    private void loadData(){
        List<Course> moreCourses = null;
        switch (listType){
            case Utils.BEST_RATING:
                moreCourses = mongoDBDriver.findBestRatings(limit);
                if(moreCourses != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                for(int i = 0; i<moreCourses.size();i++){
                    System.out.println(moreCourses.get(i));
                }
                break;
            case Utils.TRENDING_COURSE:
                moreCourses = mongoDBDriver.trendingCourses(limit);
                if(moreCourses != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
        }
        loadMore();
    }

    private void loadMore() {
        User myUser = Session.getLocalSession().getLoggedUser();
        int skip = pageNumber*limit;
        pageNumber++;
        List<Course> moreCourses = null;
        List<User> moreUsers = null;
        switch (listType) {
            case Utils.OFFERED_COURSES:
                moreCourses = mongoDBDriver.findCoursesOfferedByUser(user, skip, limit);
                if (moreCourses != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
            case Utils.REVIEWED_COURSES:
                moreCourses = neo4jDriver.findCoursesLikedOrCompletedByUser(user,true ,skip, limit);
                if (moreCourses != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
            case Utils.LIKED_COURSES:
                moreCourses = neo4jDriver.findCoursesLikedOrCompletedByUser(user, false, skip, limit);
                if (moreCourses != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
            case Utils.FOLLOWING_USERS:
                moreUsers = neo4jDriver.findFollowedUsers(user, skip, limit);
                if (moreUsers != null)
                    Utils.addUsersSnapshot(itemsHBox, moreUsers);
                break;
            case Utils.FOLLOWER_USERS:
                moreUsers = neo4jDriver.findFollowerUsers(user, skip, limit);
                if (moreUsers != null)
                    Utils.addUsersSnapshot(itemsHBox, moreUsers);
                break;
            case Utils.FRIENDS_COMPLETED_LIKED:
                moreCourses = neo4jDriver.findSuggestedCourses(myUser, skip, limit);
                if (moreCourses != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
            case Utils.INSTRUCTORS_SUGGESTIONS:
                moreCourses = neo4jDriver.findSuggestedCoursesByCompletedCourses(myUser, skip, limit);
                if (moreCourses != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
            case Utils.USER_SUGGESTIONS:
                moreUsers = neo4jDriver.findSuggestedUsers(myUser, skip, limit);
                if (moreUsers != null)
                    Utils.addUsersSnapshot(itemsHBox, moreUsers);
        }
    }
}
