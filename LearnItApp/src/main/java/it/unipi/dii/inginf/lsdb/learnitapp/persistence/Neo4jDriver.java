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
            instance.initConnection();
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

    //provata
    public boolean followUser(User follower, User followed){
        try(Session session = neo4jDriver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (a:User) WHERE a.username = $followed " +
                        "MATCH (b:User) WHERE b.username = $follower " +
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

    //provata
    public boolean unfollowUser(User follower, User followed){
        try(Session session = neo4jDriver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (a:User {username:$follower})-[r:FOLLOW]->(b:User{username:$followed}) " +
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

    //provata
    public boolean likeCourse(User user, Course course){
        try(Session session = neo4jDriver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User) WHERE u.username = $username " +
                        "MATCH (c:Course) WHERE c.title = $title" +
                        " MERGE (u)-[:LIKE]->(c)", parameters("username", user.getUsername(), "title", course.getTitle()));
                return null;
            });
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }


    //provata
    public boolean dislikeCourse(User user, Course course){
        try(Session session = neo4jDriver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {username: $username} )-[r:LIKE]->(c:Course {title: $title}) " +
                        "DELETE r", parameters("username", user.getUsername(), "title", course.getTitle()));
                return null;
            });
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }


    //provata
    public boolean editProfileInfo(User user){
        try(Session session = neo4jDriver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String birth = sdf.format(user.getDateOfBirth());
                tx.run( "MATCH (u:User {username: $username} ) "+
                        "SET u = {username: $username, complete_name: $complete_name, date_of_birth: $date_of_birth, gender: $gender, email: $email, password: $password, role: $role, profile_picture: $profile_picture}", parameters("username", user.getUsername(), "complete_name", user.getCompleteName(), "date_of_birth", birth, "gender", user.getGender(), "email", user.getEmail(), "password", user.getPassword(), "role", user.getRole().ordinal(), "profile_picture", user.getProfilePic())
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

    //provata
    public boolean deleteUser(User user){
        try(Session session = neo4jDriver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (u:User{username: $username}) "+
                        "OPTIONAL MATCH (u:User{username: $username})-[r:OFFER]->(c:Course) "+
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

    //provata
    public List<Course> findSuggestedCoursesByCompletedCourses(User user, int skip, int limit){
        List<Course> courses = new ArrayList<>();

        try(Session session = neo4jDriver.session()){

            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (c:Course)<-[:OFFER]-(u2: User)-[:OFFER]->(c2:Course)<-[:REVIEW]-(u:User {username : $username }) " +
                        "WHERE NOT ( (u)-[:REVIEW]->(c) ) AND (c.id <> c2.id) AND (u2.username <> u.username)"+
                        "RETURN c.title as title, c.duration as duration, c.price as price, c.course_pic as pic " +
                        "SKIP $skip LIMIT $limit",
                        parameters("username", user.getUsername(), "skip", skip, "limit", limit));

                while(result.hasNext()){
                    Record r = result.next();
                    courses.add(new Course(r.get("title").asString(), r.get("duration").asDouble(), r.get("price").asDouble(), r.get("pic").asString()));
                }
                return null;
            });
            return courses;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //provata
    public int findTotCourses(User user){
        Integer tot;
        try(Session session = neo4jDriver.session()){
            tot = session.readTransaction(tx -> {
                Result result = tx.run("MATCH (c:Course)<-[:REVIEW]-(u:User {username : $username} )"+
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

    //provata
    public double findAvgStatisticOfCompletedCourses(User user, String attribute){
        double avg;
        try(Session session = neo4jDriver.session()){
            avg = session.readTransaction(tx -> {
                Result result = tx.run("MATCH (c:Course)<-[:REVIEW]-(u:User{username:$username})"+
                        "RETURN AVG(c."+attribute+") AS avg", parameters("username", user.getUsername()));

                if (result.hasNext()) {
                    Record r = result.next();
                    double avg_value = r.get("avg").asDouble();
                    return avg_value;
                }

                return -1.0;
            });
            return avg;
        }
        catch(Exception e){
            //e.printStackTrace();
            return -1.0;
        }
    }

    //provata
    public boolean addCourse(Course course) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "CREATE (c:Course {title: $title, duration: $duration, price: $price})",
                        parameters("title", course.getTitle(),
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

    //provata
    public boolean updateCourse(Course course) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (c:Course {title: $title}) " +
                                "SET c = {title: $title, duration: $newduration, price: $newprice}",
                        parameters("title", course.getTitle(), "newduration", course.getDuration(),
                                "newprice", course.getPrice()));
                return null;
            });

            return true;
        } catch (Exception e) {
            System.err.println("Error in updating a course in Neo4J");
            return false;
        }
    }

    //provata
    public boolean deleteCourse(Course course) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (c:Course) WHERE c.title = $title DETACH DELETE c",
                        parameters( "title", course.getTitle() ));
                return null;
            });
            return true;
        }
        catch (Exception ex) {
            System.err.println("Error while deleting course in Neo4J");
            return false;
        }
    }

    //provata
    public boolean addReview(Course ratedCourse, User user) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH" +
                                "  (u:User {username: $username})," +
                                "  (c:Course {title: $title})" +
                                "MERGE (u)-[:REVIEW]->(c)",
                        parameters("username", user.getUsername(),
                                "title", ratedCourse.getTitle()));
                return null;
            });
            return true;
        }
        catch (Exception ex) {
            System.err.println("Error while adding a review in Neo4J");
            return false;
        }
    }


    //provata
    public boolean deleteReview(Course ratedCourse, User user) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {username: $username})-[r:REVIEW]->(c:Course {title: $title}) DELETE r",
                        parameters("username", user.getUsername(), "title", ratedCourse.getTitle()));
                return null;
            });
            return true;
        }
        catch (Exception ex) {
            System.err.println("Error while adding a review in Neo4J");
            return false;
        }
    }

    //provata
    public User login(String username, String password) {
        User loggedUser = null;
        try (Session session = neo4jDriver.session()) {
            loggedUser = session.readTransaction((TransactionWork<User>) tx -> {
                Result r = tx.run("MATCH (u:User {username: $username, password: $password})" +
                                " RETURN u.username as username, u.password as password, " +
                                " u.email as email, u.profile_picture as picture, u.date_of_birth as date_of_birth," +
                                " u.gender as gender, u.role as role, u.complete_name as complete_name",
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

    //provata
    public List<Course> findSuggestedCourses(User user, int toSkip, int limit) {
        List<Course> suggested = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            session.readTransaction(tx -> {
                Result r = tx.run("MATCH (u:User {username: $username})-[:FOLLOW]->(friend:User)-[:LIKE|:REVIEW]->(c:Course) " +
                        "WHERE NOT ((u)-[:OFFER]->(c) OR (u)-[:REVIEW]->(c)) " +
                        "RETURN c.title AS title, c.duration AS duration, c.price AS price, c.course_pic AS pic" +
                        "COUNT(*) AS occurrence " +
                        "ORDER BY occurrence DESC " +
                        "SKIP $skip " +
                        "LIMIT $limit ", parameters("username", user.getUsername(), "skip", toSkip, "limit", limit));

                while (r.hasNext()) {
                    Record rec = r.next();
                    String title = rec.get("title").asString();
                    double duration = rec.get("duration").asDouble();
                    double price = rec.get("price").asDouble();
                    String pic = rec.get("pic").asString();
                    suggested.add(new Course(title, duration, price, pic));
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

    //provata
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

    //provata
    public boolean registerUser(final String username, final String complete_name, final String dateOfBirth,
                                final String gender, final String email, final String password, final String profilePicture,
                                final boolean isAdmin) {
        try (Session session = neo4jDriver.session())
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
                                "profile_picture", profilePicture));
                return null;
            });
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //provata
    //Find courses liked or completed by a specific user
    public List<Course> findCoursesLikedOrCompletedByUser(User user, final boolean researchFlag, int skip, int limit) {
        //0 -> liked
        //1 -> completed
        try (Session session = neo4jDriver.session())
        {
            List<Course> resultCourses = session.readTransaction((TransactionWork<List<Course>>) tx -> {
                String relationship;
                List<Course> courses = new ArrayList<>();
                if(researchFlag)
                    relationship = "REVIEW";
                else
                    relationship = "LIKE";

                Result result = tx.run("MATCH (u:User{username: $username})-[:"+relationship+"]->(c:Course) " +
                        "RETURN c.title as title, c.duration as duration, c.price as price, c.course_pic as pic " +
                        "SKIP $skip LIMIT $limit", parameters( "username", user.getUsername(), "skip", skip,
                        "limit", limit));
                while(result.hasNext()){
                    Record record = result.next();
                    courses.add(new Course(record.get("title").asString(), record.get("duration").asDouble(),
                            record.get("price").asDouble(), record.get("pic").asString()));
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

    //provata
    //Find suggested users that have participated to the same courses of you
    public List<User> findSuggestedUsers(User loggedUser, int skip, int limit) {
        try (Session session = neo4jDriver.session())
        {
            List<User> resultUsers = session.readTransaction((TransactionWork<List<User>>) tx -> {
                List<User> users = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                Result result = tx.run("MATCH (u:User{username: $username})-[:REVIEW]->(c)<-[:REVIEW]-(suggested)" +
                                "WHERE u<>suggested RETURN suggested.username AS username, suggested.complete_name AS complete_name, " +
                                "suggested.profile_picture AS picture " +
                                "SKIP $skip LIMIT $limit",
                        parameters( "username", loggedUser.getUsername(), "skip", skip, "limit", limit));
                while(result.hasNext()){
                    Record record = result.next();
                    System.out.println(record.get("username").asString());
                    User u = new User(record.get("username").asString(), record.get("complete_name").asString());
                    u.setProfilePic(record.get("picture").asString());
                    users.add(u);
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

    //provata
    public boolean checkUserExists(String username) {
        try (Session session = neo4jDriver.session()) {
            boolean res = session.readTransaction(tx -> {
                Result r = tx.run("MATCH (u:User {username: $username}) RETURN u", parameters("username", username));
                if (r.hasNext())
                    return true;
                return false;
            });

            return res;
        }
        catch (Exception ex) {
            System.err.println("Error while retrieving suggestions from Neo4J");
            return true;
        }
    }

    //provata
    public boolean offerCourse(User user, Course course) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (u:User {username: $username}) " +
                                "CREATE (c:Course {title: $title, duration: $duration, price: $price})<-[:OFFER]-(u)",
                        parameters("title", course.getTitle(),
                                "duration", course.getDuration(),
                                "price", course.getPrice(), "username", user.getUsername()));
                return null;
            });
            return true;
        }
        catch (Exception ex) {
            System.err.println("Error in adding a new course in Neo4J");
            return false;
        }
    }

    //provata
    public boolean isUserFollowedByUser(String followed, String follower){
        try (Session session = neo4jDriver.session())
        {
            int ret = session.readTransaction(tx -> {
                Result result = tx.run("MATCH (a:User{username: $follower})-[:FOLLOW]->(b:User{username: $followed}) RETURN COUNT(*) as count", parameters( "follower", follower, "followed", followed));
                Record r = result.next();

                return r.get("count").asInt();
            });
            return ret>0;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    //provata
    public List<User> findFollowerUsers(User followedUser){
        List<User> users = new ArrayList<>();
        try (Session session = neo4jDriver.session())
        {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (a:User{username: $username})<-[:FOLLOW]-(b:User) " +
                        "RETURN b.username as username, b.complete_name as name, b.profile_picture as pic ",
                        parameters( "username", followedUser.getUsername()));

                while(result.hasNext()){
                    Record record = result.next();
                    users.add(new User(record.get("username").asString(), record.get("name").asString(), record.get("pic").asString()));
                }

                return null;
            });

            return users;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    //provata
    public List<User> findFollowedUsers(User followedUser){
        List<User> users = new ArrayList<>();
        try (Session session = neo4jDriver.session())
        {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (a:User{username: $username})-[:FOLLOW]->(b:User) " +
                                "RETURN b.username as username, b.complete_name as name, b.profile_picture as pic ",
                        parameters( "username", followedUser.getUsername()));

                while(result.hasNext()){
                    Record record = result.next();
                    users.add(new User(record.get("username").asString(), record.get("name").asString(), record.get("pic").asString()));
                }

                return null;
            });

            return users;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    //provata
    public List<Integer> getFollowStats(User u) {
        List<Integer> followStats = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            session.readTransaction(tx -> {
                Result r = tx.run("MATCH (u:User {username: $username})" +
                                " OPTIONAL MATCH (u)<-[f1:FOLLOW]-()" +
                                " OPTIONAL MATCH (u)-[f2:FOLLOW]->() " +
                                " RETURN COUNT(DISTINCT f1) AS follower, COUNT(DISTINCT f2) AS following",
                        parameters("username", u.getUsername()));

                if (r.hasNext()) {
                    Record rec = r.next();
                    followStats.add(rec.get("follower").asInt());
                    followStats.add(rec.get("following").asInt());
                }

                return null;
            });

            return followStats;
        }

        catch (Exception ex) {
            System.err.println("Error while retrieving suggestions from Neo4J");
            return null;
        }
    }

    //provata
    public List<Course> findCoursesOfferedByUser(User user) {
        List<Course> courses = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            session.readTransaction(tx -> {
                Result r = tx.run("MATCH (u:User {username: $username})-[:OFFER]->(c:Course)" +
                                " RETURN c.title AS title, c.duration AS duration, c.price AS price, c.course_pic AS pic",
                        parameters("username", user.getUsername()));

                while (r.hasNext()) {
                    Record rec = r.next();
                    Course c = new Course(rec.get("title").asString(), rec.get("duration").asDouble(), rec.get("price").asDouble(), rec.get("pic").asString());
                    courses.add(c);
                }

                return null;
            });

            return courses;
        }
        catch (Exception ex) {
            System.err.println("Error while retrieving suggestions from Neo4J");
            return null;
        }
    }

}
