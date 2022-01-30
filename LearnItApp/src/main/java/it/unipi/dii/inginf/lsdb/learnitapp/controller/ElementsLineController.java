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
    private MongoDBDriver mongoDBDriver;
    private ConfigParams configParams;
    private int limit;
    private int limitFirstLvl;
    private int limitSecondLvl;

    public void initialize(){
        pageNumber = 0;
        configParams = ConfigParams.getLocalConfig();
        neo4jDriver = Neo4jDriver.getInstance();
        mongoDBDriver = MongoDBDriver.getInstance();
        limit = configParams.getLimitNumber();
        limitFirstLvl = configParams.getLimitFirstLvl();
        limitSecondLvl = configParams.getLimitSecondLvl();
    }

    public void setCoursesUsers(Course course, User user, int type){
        listType = type;
        this.course = course;
        this.user = user;
        switch (listType){
            case Utils.BEST_RATING:
                headerLabel.setText("Best rating courses");
                break;
            case Utils.TRENDING_COURSE:
                headerLabel.setText("Trending courses");
                break;
            case Utils.COURSES_SUGGESTIONS:
                headerLabel.setText("Your friend also liked/completed..");
                break;
            case Utils.INSTRUCTORS_SUGGESTIONS:
                headerLabel.setText("Your instructor also created..");
                break;
            case Utils.MOST_LIKED_COURSES:
                headerLabel.setText("Most liked courses");
                break;
            case Utils.BEST_USERS:
                headerLabel.setText("Best users in choosing courses");
                break;
            case Utils.USER_SUGGESTIONS:
                headerLabel.setText("Suggested Users");
                break;
            case Utils.MOST_ACTIVE_USERS:
                headerLabel.setText("Most active users");
                break;
            case Utils.MOST_FOLLOWED_USERS:
                headerLabel.setText("Most followed users");
                break;
            case Utils.LIKED_COURSES:
                headerLabel.setText("Liked courses");
                break;
            case Utils.REVIEWED_COURSES:
                headerLabel.setText("Reviewed courses");
                break;
            case Utils.OFFERED_COURSES:
                headerLabel.setText("Offered courses");
                break;
            case Utils.FOLLOWER_USERS:
                headerLabel.setText("Users following you");
                break;
            case Utils.FOLLOWING_USERS:
                headerLabel.setText("Users you are following");
                break;
        }

        loadData();
        if(listType != Utils.BEST_RATING && listType != Utils.TRENDING_COURSE && listType != Utils.MOST_LIKED_COURSES){
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
        List<User> moreUsers = null;
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
            case Utils.MOST_LIKED_COURSES:
                moreCourses = neo4jDriver.findMostLikedCourses(limit);
                if (moreCourses != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
            case Utils.MOST_ACTIVE_USERS:
                moreUsers = mongoDBDriver.mostActiveUsers(limit);
                if (moreUsers != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
            case Utils.MOST_FOLLOWED_USERS:
                moreUsers = neo4jDriver.findMostFollowedUsers(limit);
                if (moreUsers != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
        }
        loadMore();
    }

    private void loadMore() {
        User myUser = Session.getLocalSession().getLoggedUser();
        int skip = pageNumber*limit;
        int skipFirstLvl = pageNumber*limitFirstLvl;
        int skipSecondLvl = pageNumber*limitSecondLvl;
        pageNumber++;
        List<Course> moreCourses = null;
        List<User> moreUsers = null;
        switch (listType) {
            case Utils.COURSES_SUGGESTIONS:
                int numRelationships = configParams.getNumRelationships();
                moreCourses = neo4jDriver.findSuggestedCourses(myUser, skipFirstLvl, limitFirstLvl, skipSecondLvl,
                                                                limitSecondLvl, numRelationships);
                if (moreCourses != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
            case Utils.INSTRUCTORS_SUGGESTIONS:
                moreCourses = neo4jDriver.findSuggestedCoursesByCompletedCourses(myUser, skip, limit);
                if (moreCourses != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
            case Utils.BEST_USERS:
                moreUsers = mongoDBDriver.bestUsers(limit);
                if (moreUsers != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
            case Utils.USER_SUGGESTIONS:
                int numCommonCourses = configParams.getNumCommonCourses();
                int followedThreshold = configParams.getFollowedThreshold();
                moreUsers = neo4jDriver.findSuggestedUsers(myUser, followedThreshold, skipFirstLvl, limitFirstLvl,
                        skipSecondLvl, limitSecondLvl, numCommonCourses);
                if (moreUsers != null)
                    Utils.addUsersSnapshot(itemsHBox, moreUsers);
            case Utils.LIKED_COURSES:
                moreCourses = neo4jDriver.findCoursesLikedOrCompletedByUser(user, false, skip, limit);
                if (moreCourses != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
            case Utils.REVIEWED_COURSES:
                moreCourses = neo4jDriver.findCoursesLikedOrCompletedByUser(user,true ,skip, limit);
                if (moreCourses != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
            case Utils.OFFERED_COURSES:
                moreCourses = mongoDBDriver.findCoursesOfferedByUser(user, skip, limit);
                if (moreCourses != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
            case Utils.FOLLOWER_USERS:
                moreUsers = neo4jDriver.findFollowerUsers(user, skip, limit);
                if (moreUsers != null)
                    Utils.addUsersSnapshot(itemsHBox, moreUsers);
                break;
            case Utils.FOLLOWING_USERS:
                moreUsers = neo4jDriver.findFollowedUsers(user, skip, limit);
                if (moreUsers != null)
                    Utils.addUsersSnapshot(itemsHBox, moreUsers);
                break;
        }
    }
}
