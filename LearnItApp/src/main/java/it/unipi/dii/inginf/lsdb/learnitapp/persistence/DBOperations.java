package it.unipi.dii.inginf.lsdb.learnitapp.persistence;

import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Review;

public class DBOperations {
    private static Neo4jDriver neo4jDriver = Neo4jDriver.getInstance();
    private static MongoDBDriver mongoDBDriver = MongoDBDriver.getInstance();

    public static boolean updateCourse(Course course){
        boolean ret;
         ret = neo4jDriver.updateCourse(course);
         if(!ret)
             return false;
         ret = mongoDBDriver.updateCourse(course);
         return ret;
    }

    public static boolean addReview(Review newReview, Course course){
        boolean ret;
        ret = neo4jDriver.addReview(course, newReview.getAuthor());
        if(!ret)
            return false;
        ret = mongoDBDriver.addReviewRedundancies(course, newReview);
        return ret;
    }

    public static boolean addCourse(Course course){
        boolean ret;
        ret = neo4jDriver.addCourse(course);
        if(!ret)
            return false;
        ret = mongoDBDriver.addCourse(course);
        return ret;
    }


}
