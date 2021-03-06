package it.unipi.dii.inginf.lsdb.learnitapp.persistence;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.NULL;
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
            instance = new Neo4jDriver(ConfigParams.getInstance());
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
            System.err.println("Neo4J server is not available");
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


    public boolean editProfileInfo(User user){
        try(Session session = neo4jDriver.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {;
                tx.run( "MATCH (u:User {username: $username}) "+
                        "SET u = {username: $username, gender: $gender, profile_picture: $profile_picture}", parameters("username", user.getUsername(), "gender", user.getGender(), "profile_picture", user.getProfilePic())
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

    /**
     * Suggests the user some courses having the same instructor of other courses they already have reviewed
     */
    public List<Course> findSuggestedCoursesByCompletedCourses(User user, int skip, int limit){
        List<Course> courses = new ArrayList<>();

        try(Session session = neo4jDriver.session()){

            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (c:Course)<-[:OFFER]-(u2: User)-[:OFFER]->(c2:Course)<-[:REVIEW]-(u:User {username : $username }) " +
                                "WHERE NOT EXISTS( (u)-[:REVIEW]->(c) ) AND (c.title <> c2.title) AND (u2.username <> u.username) "+
                                "RETURN c.title as title, c.duration as duration, c.price as price, c.course_pic as pic " +
                                "SKIP $skip LIMIT $limit",
                        parameters("username", user.getUsername(), "skip", skip, "limit", limit));

                while(result.hasNext()){
                    Record rec = result.next();
                    Double duration = null;
                    Double price = null;
                    String pic = null;
                    if (rec.get("duration") != NULL)
                        duration = rec.get("duration").asDouble();
                    if (rec.get("price") != NULL)
                        price = rec.get("price").asDouble();
                    if (rec.get("pic") != NULL)
                        pic = rec.get("pic").asString();
                    courses.add(new Course(rec.get("title").asString(), duration, price, pic));
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

    public boolean updateCourse(Course course) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (c:Course {title: $title}) " +
                                "SET c = {title: $title, duration: $newduration, price: $newprice, course_pic: $pic}",
                        parameters("title", course.getTitle(), "newduration", course.getDuration(),
                                "newprice", course.getPrice(), "pic", course.getCoursePic()));
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

    public boolean addReview(Course ratedCourse, String username) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH" +
                                "  (u:User {username: $username})," +
                                "  (c:Course {title: $title})" +
                                "MERGE (u)-[:REVIEW]->(c)",
                        parameters("username", username,
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

    public boolean deleteReview(Course ratedCourse, String username) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {username: $username})-[r:REVIEW]->(c:Course {title: $title}) DELETE r",
                        parameters("username", username, "title", ratedCourse.getTitle()));
                return null;
            });
            return true;
        }
        catch (Exception ex) {
            System.err.println("Error while adding a review in Neo4J");
            return false;
        }
    }

    /**
     * This query recommends courses to a specified user based on two levels of suggestion: the first levels suggests
     * courses liked by most users followed by the users, while the second level finds the courses liked by the users
     * (having at least "numRelationships" liked courses in common with this user) followed by users already followed
     * by this user. Second level suggested courses are returned only if they are not already reviewed or liked by this user
     * and are ordered by decreasing duration and ascending price.
     */
    public List<Course> findSuggestedCourses(User user, int skipFirstLvl, int limitFirstLvl, int skipSecondLvl,
                                             int limitSecondLvl, int numRelationships) {
        List<Course> suggested = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            session.readTransaction(tx -> {
                Result r = tx.run(
                        "MATCH (c:Course)<-[l:LIKE]-(u:User)<-[f:FOLLOW]-(me:User{username:$username}) " +
                                "WHERE NOT EXISTS((me)-[:LIKE]->(c)) " +
                                "WITH DISTINCT(c) as c, COUNT(*) as numUser " +
                                "RETURN c.title AS title, c.duration AS duration, c.price AS price, c.course_pic AS pic " +
                                "ORDER BY numUser DESC " +
                                "SKIP $skipFirstLvl " +
                                "LIMIT $limitFirstLvl " +
                                "UNION " +
                                "MATCH (me:User{username:$username})-[l:LIKE]->(c:Course)<-[l1:LIKE]-(u:User)<-[:FOLLOW*2..2]-(me) " +
                                "WHERE NOT EXISTS((me)-[:FOLLOW]->(u)) AND me.username <> u.username " +
                                "WITH DISTINCT(u) as user, COUNT(DISTINCT(c)) as numRelationships " +
                                "WHERE numRelationships > $numRelationships " +
                                "MATCH (user)-[:LIKE|:REVIEW]->(c:Course) " +
                                "WHERE NOT EXISTS((:User{username:$username})-[:LIKE|:REVIEW]->(c)) " +
                                "RETURN DISTINCT c.title AS title, c.duration AS duration, c.price AS price, c.course_pic AS pic " +
                                "ORDER BY CASE c.duration WHEN null THEN 0 ELSE c.duration END DESC, " +
                                "CASE c.price WHEN null THEN 0 ELSE c.price END ASC " +
                                "SKIP $skipSecondLvl " +
                                "LIMIT $limitSecondLvl",
                        parameters("username", user.getUsername(), "skipFirstLvl", skipFirstLvl,
                                "limitFirstLvl", limitFirstLvl, "numRelationships", numRelationships,
                                "skipSecondLvl", skipSecondLvl, "limitSecondLvl", limitSecondLvl));
                while (r.hasNext()) {
                    Record rec = r.next();
                    String title = rec.get("title").asString();
                    Double duration = null;
                    Double price = null;
                    String pic = null;
                    if (rec.get("duration") != NULL)
                        duration = rec.get("duration").asDouble();
                    if (rec.get("price") != NULL)
                        price = rec.get("price").asDouble();
                    if (rec.get("pic") != NULL)
                        pic = rec.get("pic").asString();
                    suggested.add(new Course(title, duration, price, pic));
                }

                return null;
            });

            return suggested;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * This query recommends users to a specified user based on two levels of suggestion: the first level suggests
     * not followed users who are followed by at least "followedThreshold" users followed by the specified user,
     * ordered by total number of followers in common between the users, while the second level recommends those users
     * (not followed by the user) who like at least "numCommonCourses" courses also liked by the specified user,
     * ordered by the total number of liked courses in common between the users.
     */
    public List<User> findSuggestedUsers(User user, int followedThreshold, int skipFirstLvl, int limitFirstLvl,
                                         int skipSecondLvl, int limitSecondLvl, int numCommonCourses) {
        List<User> suggested = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            session.readTransaction(tx -> {
                Result r = tx.run(
                        "MATCH (me:User {username: $username})-[:FOLLOW*2..2]->(u:User) " +
                                "WHERE NOT EXISTS((me)-[:FOLLOW]->(u)) AND u.username <> me.username " +
                                "WITH DISTINCT(u) as user, COUNT(u) as followed " +
                                "WHERE followed > $followedThreshold " +
                                "RETURN user.username AS username, user.gender AS gender, user.pic as pic " +
                                "ORDER BY followed DESC " +
                                "SKIP $skipFirstLvl " +
                                "LIMIT $limitFirstLvl " +
                                "UNION " +
                                "MATCH (me:User {username: $username })-[:LIKE]->(commonCourse:Course)<-[:LIKE]-(u:User) " +
                                "WHERE NOT EXISTS((me)-[:FOLLOW]->(u)) " +
                                "WITH DISTINCT(u) as user, COUNT(commonCourse) as numCommonCourses " +
                                "WHERE numCommonCourses > $numCommonCourses " +
                                "RETURN  user.username AS username, user.gender AS gender, user.pic as pic " +
                                "ORDER BY numCommonCourses DESC " +
                                "SKIP $skipSecondLvl " +
                                "LIMIT $limitSecondLvl",
                        parameters("username", user.getUsername(), "followedThreshold", followedThreshold,
                                "skipFirstLvl", skipFirstLvl, "limitFirstLvl", limitFirstLvl,
                                "numCommonCourses", numCommonCourses, "skipSecondLvl", skipSecondLvl,
                                "limitSecondLvl", limitSecondLvl));
                while (r.hasNext()) {
                    Record rec = r.next();

                    String gender = null;
                    String pic = null;
                    if (rec.get("gender") != NULL)
                        gender = rec.get("gender").asString();
                    if (rec.get("pic") != NULL)
                        pic = rec.get("pic").asString();

                    String username = rec.get("username").asString();
                    suggested.add(new User(username, pic, gender));
                }

                return null;
            });

            return suggested;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean registerUser(final String username, final String gender, final String profilePicture) {
        try (Session session = neo4jDriver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "CREATE (u:User {username: $username, gender: $gender, profile_picture: $profile_picture})",
                        parameters( "username", username, "gender", gender, "profile_picture", profilePicture));
                return null;
            });
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Finds all courses liked by a user
     */
    public List<Course> findCoursesLikedByUser(User user, int skip, int limit) {
        try (Session session = neo4jDriver.session())
        {
            List<Course> resultCourses = session.readTransaction((TransactionWork<List<Course>>) tx -> {
                List<Course> courses = new ArrayList<>();

                Result result = tx.run("MATCH (u:User{username: $username})-[:LIKE]->(c:Course) " +
                        "RETURN c.title as title, c.duration as duration, c.price as price, c.course_pic as pic " +
                        "SKIP $skip LIMIT $limit", parameters( "username", user.getUsername(), "skip", skip,
                        "limit", limit));
                while(result.hasNext()){
                    Record rec = result.next();
                    Double duration = null;
                    Double price = null;
                    String pic = null;
                    if (rec.get("duration") != NULL)
                        duration = rec.get("duration").asDouble();
                    if (rec.get("price") != NULL)
                        price = rec.get("price").asDouble();
                    if (rec.get("pic") != NULL)
                        pic = rec.get("pic").asString();
                    courses.add(new Course(rec.get("title").asString(), duration, price, pic));
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

    /**
     * Checks if a user is followed by another user
     */
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

    /**
     * Finds all the users following a certain user
     */
    public List<User> findFollowerUsers(User followedUser, int toSkip, int limit){
        List<User> users = new ArrayList<>();
        try (Session session = neo4jDriver.session())
        {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (a:User{username: $username})<-[:FOLLOW]-(b:User) " +
                                "RETURN b.username as username, b.pic as pic, b.gender as gender" +
                                " SKIP " + toSkip + " LIMIT " + limit,
                        parameters( "username", followedUser.getUsername()));

                while(result.hasNext()){
                    Record rec = result.next();
                    String pic = null;
                    String gender = null;
                    if (rec.get("gender") != NULL)
                        gender = rec.get("gender").asString();
                    if (rec.get("pic") != NULL)
                        pic = rec.get("pic").asString();
                    User u = new User(rec.get("username").asString(), pic, gender);
                    users.add(u);
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

    /**
     * Checks if a course is liked by a user
     */
    public boolean isCourseLikedByUser(Course course, User user) {
        try (Session session = neo4jDriver.session()) {
            boolean res = session.readTransaction(tx -> {
                Result r = tx.run("MATCH (u:User {username: $username})-[:LIKE]->(c:Course {title: $title}) RETURN u",
                        parameters("username", user.getUsername(), "title", course.getTitle()));
                if (r.hasNext())
                    return true;
                return false;
            });

            return res;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if the user has already written a review for a certain course
     */
    public boolean isCourseReviewedByUser(Course course, User user) {
        try (Session session = neo4jDriver.session()) {
            boolean res = session.readTransaction(tx -> {
                Result r = tx.run("MATCH (u:User {username: $username})-[:REVIEW]->(c:Course {title: $title}) RETURN u",
                        parameters("username", user.getUsername(), "title", course.getTitle()));
                if (r.hasNext())
                    return true;
                return false;
            });

            return res;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Finds all users followed by a certain user
     */
    public List<User> findFollowedUsers(User followedUser, int toSkip, int limit){
        List<User> users = new ArrayList<>();
        try (Session session = neo4jDriver.session())
        {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (a:User{username: $username})-[:FOLLOW]->(b:User) " +
                                "RETURN b.username as username, b.pic as pic, b.gender as gender " +
                                "SKIP " + toSkip + " LIMIT " + limit,
                        parameters( "username", followedUser.getUsername()));

                while(result.hasNext()){
                    Record rec = result.next();
                    String pic = null;
                    String gender = null;
                    if (rec.get("gender") != NULL)
                        gender = rec.get("gender").asString();
                    if (rec.get("pic") != NULL)
                        pic = rec.get("pic").asString();
                    User u = new User(rec.get("username").asString(), pic, gender);
                    users.add(u);
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

    /**
     * Finds the number of following and follower users for a certain user in a single query
     */
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
            ex.printStackTrace();
            return null;
        }
    }

    public List<Course> findMostLikedCourses(int limit){
        List<Course> courses = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            session.readTransaction(tx -> {

                Result r = tx.run("MATCH (:User)-[l:LIKE]->(c:Course) " +
                                "RETURN c.title AS title, c.course_pic AS course_pic, " +
                                "c.duration AS duration, c.price AS price, COUNT (l) AS like_count, "+
                                "CASE c.duration WHEN null THEN 0 ELSE CASE c.price "+
                                "WHEN null THEN 1 ELSE c.duration/c.price END "+
                                "END " +
                                "AS quality_ratio "+
                                "ORDER BY like_count DESC, quality_ratio DESC "+
                                "LIMIT $limit",
                        parameters("limit", limit));

                while (r.hasNext()) {
                    Record rec = r.next();
                    Double duration = null;
                    Double price = null;
                    String pic = null;
                    if (rec.get("duration") != NULL)
                        duration = rec.get("duration").asDouble();
                    if (rec.get("price") != NULL)
                        price = rec.get("price").asDouble();
                    if (rec.get("course_pic") != NULL)
                        pic = rec.get("course_pic").asString();
                    Course c = new Course(rec.get("title").asString(), duration, price, pic);
                    courses.add(c);
                }
                return null;
            });

            return courses;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<User> findMostFollowedUsers(int limit){
        List<User> users = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            session.readTransaction(tx -> {
                Result r = tx.run("MATCH (u:User)<-[f:FOLLOW]-(u2:User) " +
                                "OPTIONAL MATCH (u)-[l:LIKE]->(:Course) "+
                                "WHERE u.username <> u2.username "+
                                "WITH DISTINCT (u), COUNT(DISTINCT(f)) AS followers, COUNT(DISTINCT(l)) AS likes "+
                                "RETURN u.username as username, u.pic as pic, u.gender as gender " +
                                "ORDER BY followers DESC, likes DESC "+
                                "LIMIT $limit ",
                        parameters("limit", limit));

                while (r.hasNext()) {
                    Record rec = r.next();
                    String pic = null;
                    String gender = null;
                    if (rec.get("gender") != NULL)
                        gender = rec.get("gender").asString();
                    if (rec.get("pic") != NULL)
                        pic = rec.get("pic").asString();
                    User u = new User(rec.get("username").asString(), pic, gender);
                    users.add(u);
                }

                return null;
            });

            return users;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the number of likes received by a course
     */
    public int getCourseLikes(Course course) {
        try (Session session = neo4jDriver.session()) {
            int res = session.readTransaction(tx -> {
                Result r = tx.run("MATCH (c:Course {title: $title})<-[l:LIKE]-() " +
                                    " RETURN count(l) AS count ",
                        parameters("title", course.getTitle()));

                if (r.hasNext()) {
                    Record rec = r.next();
                    return rec.get("count").asInt();
                }

                return 0;
            });

            return res;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }
}
