package it.unipi.dii.inginf.lsdb.learnitapp.controller;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.util.List;

public class ElementsLineController<T> {
    @FXML AnchorPane elementsLine;
    @FXML private Label headerLabel;
    @FXML private ImageView buttonImage;
    @FXML private HBox itemsHBox;

    private Neo4jDriver neo4jDriver;
    private List<T> coursesOrUsers;
    private int listType;
    private int pageNumber;
    private int limit;
    private MongoDBDriver mongoDBDriver;

    private static final String READ_MORE = "/img/readMore.png";

    public void initialize(){
        pageNumber = 0;
        limit = ConfigParams.getLocalConfig().getLimitNumber();
        neo4jDriver = Neo4jDriver.getInstance();
        mongoDBDriver = MongoDBDriver.getInstance();
    }

    public void setCoursesUsers(List<T> coursesOrUsers, int type){
        listType = type;
        this.coursesOrUsers = coursesOrUsers;
        switch (listType){
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

        loadMore();
        buttonImage = new ImageView(new Image(String.valueOf(DiscoveryPageController.class.getResource(READ_MORE))));
        buttonImage.setOnMouseClicked(clickEvent -> loadMore());
    }

    private void loadMore(){
        User myUser = Session.getLocalSession().getLoggedUser();
        int skip = pageNumber*limit;
        pageNumber++;
        List<Course> moreCourses = null;
        List<User> moreUsers = null;
        switch (listType){
            case Utils.BEST_RATING:
                moreCourses = mongoDBDriver.findBestRatings(limit);
                if(moreCourses != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
            case Utils.TRENDING_COURSE:
                moreCourses = mongoDBDriver.trendingCourses(limit);
                if(moreCourses != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
            case Utils.FRIENDS_COMPLETED_LIKED:
                moreCourses = neo4jDriver.findSuggestedCourses(myUser, skip, limit);
                if(moreCourses != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
            case Utils.INSTRUCTORS_SUGGESTIONS:
                moreCourses = neo4jDriver.findSuggestedCoursesByCompletedCourses(myUser, skip, limit);
                if(moreCourses != null)
                    Utils.addCoursesSnapshot(itemsHBox, moreCourses);
                break;
            case Utils.USER_SUGGESTIONS:
                moreUsers = neo4jDriver.findSuggestedUsers(myUser, skip, limit);
                if(moreUsers != null)
                    Utils.addUsersSnapshot(itemsHBox, moreUsers);
        }
    }
}
