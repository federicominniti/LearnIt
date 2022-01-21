package it.unipi.dii.inginf.lsdb.learnitapp.persistence;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import org.bson.types.ObjectId;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;

import static org.neo4j.driver.Values.parameters;
import static org.neo4j.driver.internal.value.NullValue.NULL;

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

    public boolean addCourse(Course course) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "CREATE (c:Course {id: $id, title: $title, duration: $duration, price: $price})",
                        parameters("id", course.getId().toString(),
                                "title", course.getTitle(),
                                "duration", course.getDuration(),
                                "price", course.getPrice()));
                return null;
            });
            return true;
        }
        catch (Exception ex) {
            System.err.println("Error in adding a new course in Neo4J");
            return false;
        }
    }

    public boolean updateCourse(Course course) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (c:Course {id: $course_id}" +
                                "SET c.title = $newtitle, c.duration = $newduration, c.price = $newprice",
                        parameters("course_id", course.getId().toString(),
                                "newtitle", course.getTitle(),
                                "newduration", course.getDuration(),
                                "newprice", course.getPrice()));
                return null;
            });

            return true;
        } catch (Exception e) {
            System.err.println("Error in updating a course in Neo4J");
            return false;
        }
    }

    public boolean deleteCourse(Course course) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (c:Course) WHERE c.id = $course_id DETACH DELETE c",
                        parameters( "course_id", course.getId().toString()) );
                return null;
            });
            return true;
        }
        catch (Exception ex) {
            System.err.println("Error while deleting course in Neo4J");
            return false;
        }
    }

    public boolean addReview(Course ratedCourse, User user) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH" +
                                "  (u:User {username: $username})," +
                                "  (c:Course {id: $course_id})" +
                                "MERGE (u)-[:REVIEW]->(c)",
                        parameters("username", user.getUsername(),
                                "id", ratedCourse.getId().toString()));
                return null;
            });
            return true;
        }
        catch (Exception ex) {
            System.err.println("Error while adding a review in Neo4J");
            return false;
        }
    }

    public boolean deleteReview(Course ratedCourse, User user) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {username: $username})-[r:REVIEW]->(c:Course {id: $course_id}) DELETE r",
                        parameters("username", user.getUsername(), "id", ratedCourse.getId().toString()));
                return null;
            });
            return true;
        }
        catch (Exception ex) {
            System.err.println("Error while adding a review in Neo4J");
            return false;
        }
    }

    public User login(String username, String password) {
        User loggedUser = null;
        try (Session session = neo4jDriver.session()) {
            loggedUser = session.readTransaction((TransactionWork<User>) tx -> {
                Result r = tx.run("MATCH (u:User {username: $username, password: $password}) RETURN u" +
                        parameters("username", username, "password", password));

                User user = null;
                try {
                    Record rec = r.next();
                    String complete_name = rec.get("complete_name").asString();
                    String email = rec.get("email").asString();
                    User.Role role = User.Role.fromInteger(rec.get("role").asInt());
                    Date date_of_birth = null;
                    String gender = null;
                    String profile_pic = null;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    if (rec.get("date_of_birth") != NULL)
                        date_of_birth = sdf.parse(rec.get("date_of_birth").asString());
                    if (rec.get("gender") != NULL)
                        gender = rec.get("gender").asString();
                    if (rec.get("picture") != NULL)
                        profile_pic = rec.get("picture").asString();

                    user = new User(username, password, complete_name, date_of_birth, gender, email, role, profile_pic);
                } catch (Exception e) {
                    user = null;
                }

                return user;
            });

            return loggedUser;
        }
        catch (Exception ex) {
            System.err.println("Error while trying to login");
            return null;
        }
    }

    public List<Course> findSuggestedCourses(User user, int toSkip, int limit) {
        List<Course> suggested = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            session.readTransaction(tx -> {
                Result r = tx.run("MATCH (u:User {username: $username})-[:FOLLOW]->(friend:User)-[:LIKE|:REVIEW]->(c:Course)" +
                        "WHERE NOT ((u)-[:OFFER]->(c) OR (u)-[:REVIEW]->(c))" +
                        "RETURN c.id AS id, c.title AS title, c.duration AS duration, c.price AS price," +
                        "COUNT(*) AS occurrence " +
                        "ORDER BY occurrence DESC" +
                        "SKIP $skip " +
                        "LIMIT $limit ", parameters("username", user.getUsername(), "skip", toSkip, "limit", limit));

                while (r.hasNext()) {
                    Record rec = r.next();
                    ObjectId id = new ObjectId(rec.get("id").asString());
                    String title = rec.get("title").asString();
                    double duration = rec.get("duration").asDouble();
                    double price = rec.get("price").asDouble();
                    suggested.add(new Course(id, title, duration, price));
                }

                return null;
            });

            return suggested;
        }
        catch (Exception ex) {
            System.err.println("Error while retrieving suggestions from Neo4J");
            return null;
        }
    }

    public List<User> searchUserByUsername (int limit, int toSkip, String searchText)
    {
        List<User> users = new ArrayList<>();
        try(Session session = neo4jDriver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (u:User) " +
                                "WHERE toLower(u.username) CONTAINS toLower($username) " +
                                "RETURN u.username AS username, u.complete_name AS complete_name, u.profile_picture AS picture " +
                                "SKIP $skip LIMIT $limit",
                        parameters("username",searchText, "skip", toSkip, "limit", limit));

                while(result.hasNext()){
                    Record r = result.next();
                    String username = r.get("username").asString();
                    String complete_name = r.get("lastName").asString();
                    User user = new User(username, complete_name);
                    if (r.get("picture") != NULL) {
                        String picture = r.get("picture").asString();
                        user.setProfilePic(picture);
                    }
                    users.add(user);
                }
                return null;
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean registerUser(final String username, final String complete_name, final String dateOfBirth,
                                final String gender, final String email, final String password, final String profilePicture,
                                final boolean isAdmin) {
        try (org.neo4j.driver.Session session = neo4jDriver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                String query = "CREATE (u:User {username: $username, complete_name: $complete_name, " +
                        " email: $email, password: $password, date_of_birth: $date_of_birth, gender: $gender," +
                        " profile_picture: $profile_picture, role: ";

                if(isAdmin)
                    query += "1";
                else
                    query += "0";
                query += "})";

                tx.run( query,
                        parameters( "username", username, "complete_name", complete_name,
                                "date_of_birth", dateOfBirth, "gender", gender, "email", email, "password", password,
                                profilePicture, "profile_picture"));
                return null;
            });
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //Find courses liked or completed by a specific user
    public List<Course> findCoursesLikedOrCompletedByUser(final String username, final boolean researchFlag) {
        //0 -> liked
        //1 -> completed
        try (org.neo4j.driver.Session session = neo4jDriver.session())
        {
            List<Course> resultCourses = session.readTransaction((TransactionWork<List<Course>>) tx -> {
                String relationship;
                List<Course> courses = new ArrayList<>();
                if(researchFlag)
                    relationship = "REVIEW";
                else
                    relationship = "LIKE";

                Result result = tx.run("MATCH (c:Course{username: $username})-[:"+relationship+"]->()" +
                        "RETURN c");
                parameters( "username", username);
                while(result.hasNext()){
                    Record record = result.next();
                    courses.add(new Course(new ObjectId(record.get("id").asString()), record.get("title").asString(), record.get("price").asDouble(), record.get("duration").asDouble()));
                }
                return courses;
            });
            return resultCourses;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    //Find suggested users that have participated to the same courses of you
    public List<User> findSuggestedUsers(User loggedUser) {
        try (org.neo4j.driver.Session session = neo4jDriver.session())
        {
            List<User> resultUsers = session.readTransaction((TransactionWork<List<User>>) tx -> {
                String relationship;
                List<User> users = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                Result result = tx.run("MATCH (u:User{username: $username})-[:REVIEW]->(c)<-[:REVIEW]-(suggested)" +
                        "WHERE u<>suggested RETURN suggested");
                parameters( "username", loggedUser.getUsername());
                while(result.hasNext()){
                    Record record = result.next();
                    try {
                        users.add(new User(record.get("username").asString(), record.get("password").asString(),
                                record.get("complete_name").asString(), sdf.parse(record.get("date_of_birth").asString()),
                                record.get("gender").asString(), record.get("email").asString(),
                                User.Role.fromInteger(record.get("role").asInt()), record.get("profile_picture").asString()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                return users;
            });
            return resultUsers;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
