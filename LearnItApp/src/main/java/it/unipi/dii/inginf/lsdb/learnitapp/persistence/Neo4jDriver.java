package it.unipi.dii.inginf.lsdb.learnitapp.persistence;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import org.bson.types.ObjectId;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;

import static org.neo4j.driver.Values.parameters;

public class Neo4jDriver implements DBDriver {
    private static Neo4jDriver instance = null;
//Branch
    private Driver neo4jDriver;
    private String neo4jIP;
    private int neo4jPort;
    private String neo4jUsername;
    private String neo4jPassword;

    private Neo4jDriver(ConfigParams configParameters)
    {
        this.neo4jIP = configParameters.getNeo4jIP();
        this.neo4jPort = configParameters.getNeo4jPort();
        this.neo4jUsername = configParameters.getNeo4jUsername();
        this.neo4jPassword = configParameters.getNeo4jPassword();
    }

    public static Neo4jDriver getInstance()
    {
        if (instance == null)
        {
            instance = new Neo4jDriver(Utils.getParams());
        }
        return instance;
    }

    /**
     * Method that inits the Driver
     */
    @Override
    public boolean initConnection()
    {
        try
        {
            neo4jDriver = GraphDatabase.driver("neo4j://" + neo4jIP + ":" + neo4jPort,
                    AuthTokens.basic(neo4jUsername, neo4jPassword
                    ));
            neo4jDriver.verifyConnectivity();
        } catch (Exception e)
        {
            System.out.println("Neo4J server is not available");
            return false;
        }
        return true;
    }

    /**
     * Method for closing the connection of the Driver
     */
    @Override
    public void closeConnection ()
    {
        if (neo4jDriver != null)
            neo4jDriver.close();
    }

    public boolean followUser(User follower, User followed){
        try(Session session = neo4jDriver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (a:User) WHERE a.username = $followed" +
                        "MATCH (b:User) WHERE b.username = $follower" +
                        " MERGE (b)-[:FOLLOW]->(a)", parameters("follower", follower.getUsername(), "followed", followed.getUsername()));
                return null;
            });
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean unfollowUser(User follower, User followed){
        try(Session session = neo4jDriver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (a:User{username = $follower})-[r:FOLLOW]->(b:User(username = $followed))" +
                        "DELETE r", parameters("follower", follower.getUsername(),"followed", followed.getUsername()));
                return null;
            });
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean likeCourse(User user, Course course){
        try(Session session = neo4jDriver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User) WHERE u.username = $username" +
                        "MATCH (c:Course) WHERE c.id = $course_id" +
                        " MERGE (u)-[:LIKE]->(c)", parameters("username", user.getUsername(), "course_id", course.getId()));
                return null;
            });
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean dislikeCourse(User user, Course course){
        try(Session session = neo4jDriver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User{username = $user_name})-[r:LIKE]->(c:Course(id = $course_id))" +
                        "DELETE r", parameters("username", user.getUsername(), "course_id", course.getId()));
                return null;
            });
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean editProfileInfo(User user){
        try(Session session = neo4jDriver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (u:User{username = $username})"+
                        "SET (u.username= $username, u.complete_name= $complete_name, u.date_of_birth= $date_of_birth, u.gender= $gender, u.email= $email, u.password= $password, u.role= $role, u.profile_picture= $profile_picture)", parameters("username", user.getUsername(), "complete_name", user.getCompleteName(), "date_of_birth", user.getDateOfBirth(), "gender", user.getGender(), "email", user.getEmail(), "password", user.getPassword(), "role", user.getRole(), "profile_picture", user.getProfilePic())
                );
                return null;
            });
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteUser(User user){
        try(Session session = neo4jDriver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (u:User{username = $username})"+
                        "OPTIONAL MATCH (u:User{username = $username})-[r:OFFER]->(c:Course)"+
                        "DETACH DELETE c,u", parameters( "username", user.getUsername()) );
                return null;
            });
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public List<Course> findCoursesOfferedByUser(User user){
        List<Course> courses = new ArrayList<>();

        try(Session session = neo4jDriver.session()){

            courses = session.readTransaction(tx -> {
                Result result = tx.run("MATCH (c:Course)<-[:OFFER]-(u:User{username = $username})" +
                        "RETURN c", parameters("username", user.getUsername()));

                List<Course> finalCourses = new ArrayList<>();
                while(result.hasNext()){
                    org.neo4j.driver.Record r = result.next();
                    finalCourses.add(new Course(new ObjectId(r.get("id").asString()), r.get("title").asString(), r.get("duration").asDouble(), r.get("price").asDouble()));
                }
                return null;
            });
            return courses;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return courses;
    }

    public List<Course> findSuggestedCoursesByCompletedCourses(User user){
        List<Course> courses = new ArrayList<>();

        try(Session session = neo4jDriver.session()){

            courses = session.readTransaction(tx -> {
                Result result = tx.run("MATCH (c:Course)<-[:OFFER]-(u2: User)-[:OFFER]->(c2:Course)<-[:REVIEW]-(u:User{username=$username})" +
                        "WHERE NOT ( (u)-[:REVIEW]->(c) ) AND (c.id <> c2.id) AND (u2.username <> $username)"+
                        "RETURN c.id, c.title, c.duration, c.price", parameters("username", user.getUsername()));

                List<Course> finalCourses = new ArrayList<>();
                while(result.hasNext()){
                    org.neo4j.driver.Record r = result.next();
                    finalCourses.add(new Course(new ObjectId(r.get("id").asString()), r.get("title").asString(), r.get("duration").asDouble(), r.get("price").asDouble()));
                }
                return null;
            });
            return courses;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return courses;
    }

    public int findTotCourses(User user){
        Integer tot;
        try(Session session = neo4jDriver.session()){
            tot = session.readTransaction(tx -> {
                Result result = tx.run("MATCH (c:Course)<-[:REVIEW]-(u:User{username=$username})"+
                        "RETURN COUNT(*) AS total", parameters("username", user.getUsername()));
                org.neo4j.driver.Record r = result.next();
                int total = r.get("total").asInt();
                return total;
            });
            return tot;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public double findAvgStatisticOfCompletedCourses(User user, String attribute){
        double avg;
        try(Session session = neo4jDriver.session()){
            avg = session.readTransaction(tx -> {
                Result result = tx.run("MATCH (c:Course)<-[:REVIEW]-(u:User{username=$username})"+
                        "RETURN AVG(c."+attribute+") AS avg", parameters("username", user.getUsername()));
                Record r = result.next();
                double avg_value = r.get("avg").asDouble();
                return avg_value;
            });
            return avg;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }
}
