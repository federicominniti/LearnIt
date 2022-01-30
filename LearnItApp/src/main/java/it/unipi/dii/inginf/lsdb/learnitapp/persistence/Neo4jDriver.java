package it.unipi.dii.inginf.lsdb.learnitapp.persistence;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Course2;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User2;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.List;

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

    public boolean followUser(User2 follower, User2 followed){
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

    public boolean unfollowUser(User2 follower, User2 followed){
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

    public boolean likeCourse(User2 user, Course2 course){
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


    public boolean dislikeCourse(User2 user, Course2 course){
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


    public boolean editProfileInfo(User2 user){
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

    public boolean deleteUser(User2 user){
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

    public List<Course2> findSuggestedCoursesByCompletedCourses(User2 user, int skip, int limit){
        List<Course2> courses = new ArrayList<>();

        try(Session session = neo4jDriver.session()){

            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (c:Course)<-[:OFFER]-(u2: User)-[:OFFER]->(c2:Course)<-[:REVIEW]-(u:User {username : $username }) " +
                                "WHERE NOT ( (u)-[:REVIEW]->(c) ) AND (c.id <> c2.id) AND (u2.username <> u.username)"+
                                "RETURN c.title as title, c.duration as duration, c.price as price, c.course_pic as pic " +
                                "SKIP $skip LIMIT $limit",
                        parameters("username", user.getUsername(), "skip", skip, "limit", limit));

                while(result.hasNext()){
                    Record r = result.next();
                    courses.add(new Course2(r.get("title").asString(), r.get("duration").asDouble(), r.get("price").asDouble(), r.get("pic").asString()));
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

    public boolean addCourse(Course2 course) {
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

    public boolean updateCourse(Course2 course) {
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

    public boolean deleteCourse(Course2 course) {
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

    public boolean addReview(Course2 ratedCourse, User2 user) {
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

    public boolean deleteReview(Course2 ratedCourse, User2 user) {
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

    public List<Course2> findSuggestedCourses(User2 user, int skipFirstLvl, int limitFirstLvl, int skipSecondLvl,
                                              int limitSecondLvl, int numRelationships) {
        List<Course2> suggested = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            session.readTransaction(tx -> {
                Result r = tx.run(
                        "MATCH (c:Course)<-[l:LIKE]-(u:User)<-[f:FOLLOW]-(me:User{username:$username}) " +
                                "WHERE NOT EXISTS((me)-[:LIKE]->(c)) " +
                                "RETURN c.title AS title, c.duration AS duration, c.price AS price, c.course_pic AS pic, " +
                                "COUNT(*) AS numUser " +
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
                                "RETURN DISTINCT c.title AS title, c.duration AS duration, c.price AS price, c.course_pic AS pic, " +
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
                    double duration = rec.get("duration").asDouble();
                    double price = rec.get("price").asDouble();
                    String pic = rec.get("pic").asString();
                    suggested.add(new Course2(title, duration, price, pic));
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

    public List<User2> findSuggestedUsers(User2 user, int followedThreshold, int skipFirstLvl, int limitFirstLvl,
                                          int skipSecondLvl, int limitSecondLvl, int numCommonCourses) {
        List<User2> suggested = new ArrayList<>();
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
                                "LIMIT $secondLvl",
                        parameters("username", user.getUsername(), "followedThreshold", followedThreshold,
                                "skipFirstLvl", skipFirstLvl, "limitFirstLvl", limitFirstLvl,
                                "numCommonCourses", numCommonCourses, "skipSecondLvl", skipSecondLvl,
                                "limitSecondLvl", limitSecondLvl));
                while (r.hasNext()) {
                    Record rec = r.next();
                    String username = rec.get("username").asString();
                    String gender = rec.get("gender").asString();
                    String pic = rec.get("pic").asString();
                    suggested.add(new User2(username, pic, gender));
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

    //Find courses liked or completed by a specific user
    public List<Course2> findCoursesLikedOrCompletedByUser(User2 user, final boolean researchFlag, int skip, int limit) {
        //0 -> liked
        //1 -> completed
        try (Session session = neo4jDriver.session())
        {
            List<Course2> resultCourses = session.readTransaction((TransactionWork<List<Course2>>) tx -> {
                String relationship;
                List<Course2> courses = new ArrayList<>();
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
                    courses.add(new Course2(record.get("title").asString(), record.get("duration").asDouble(),
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

    //Find suggested users that have participated to the same courses of you
    public List<User2> findSuggestedUsers(User2 loggedUser, int skip, int limit) {
        try (Session session = neo4jDriver.session())
        {
            List<User2> resultUsers = session.readTransaction(tx -> {
                List<User2> users = new ArrayList<>();
                Result result = tx.run("MATCH (u:User{username: $username})-[:REVIEW]->(c)<-[:REVIEW]-(suggested) " +
                                "WHERE u<>suggested " +
                                "RETURN suggested.username, suggested.pic, suggested.gender " +
                                "SKIP $skip LIMIT $limit",
                        parameters( "username", loggedUser.getUsername(), "skip", skip, "limit", limit));
                while(result.hasNext()){
                    Record record = result.next();
                    User2 u = new User2(loggedUser.getUsername(),record.get("gender").asString(), record.get("pic").asString());
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


    public boolean offerCourse(User2 user, Course2 course) {
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

    public List<User2> findFollowerUsers(User2 followedUser, int toSkip, int limit){
        List<User2> users = new ArrayList<>();
        try (Session session = neo4jDriver.session())
        {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (a:User{username: $username})<-[:FOLLOW]-(b:User) " +
                                "RETURN b.username, b.pic, b.gender " +
                                "SKIP " + toSkip + " LIMIT " + limit,
                        parameters( "username", followedUser.getUsername()));

                while(result.hasNext()){
                    Record record = result.next();
                    User2 u = new User2(record.get("username").asString(), record.get("pic").asString(), record.get("gender").asString());
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

    public boolean isCourseLikedByUser(Course2 course, User2 user) {
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
            System.err.println("Error while retrieving suggestions from Neo4J");
            return false;
        }
    }

    public boolean isCourseReviewedByUser(Course2 course, User2 user) {
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
            System.err.println("Error while retrieving suggestions from Neo4J");
            return false;
        }
    }

    public List<User2> findFollowedUsers(User2 followedUser, int toSkip, int limit){
        List<User2> users = new ArrayList<>();
        try (Session session = neo4jDriver.session())
        {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (a:User{username: $username})-[:FOLLOW]->(b:User) " +
                                "RETURN b.username, b.pic, b.gender " +
                                "SKIP " + toSkip + " LIMIT " + limit,
                        parameters( "username", followedUser.getUsername()));

                while(result.hasNext()){
                    Record record = result.next();
                    User2 u = new User2(record.get("username").asString(), record.get("pic").asString(), record.get("gender").asString());
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

    public List<Integer> getFollowStats(User2 u) {
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

    // nuova, ricontrollare
    public List<Course2> findMostLikedCourses(int limit){
        List<Course2> courses = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            session.readTransaction(tx -> {

                Result r = tx.run("MATCH (:User)-[l:LIKE]->(c:Course) " +
                                "RETURN c, COUNT (l) AS like_count, "+
                                "CASE c.duration WHEN null THEN 0 ELSE CASE c.price "+
                                "WHEN null THEN 1 ELSE c.duration/c.price END "+
                                "END " +
                                "AS quality_ratio "+
                                "ORDER BY like_count DESC, quality_ratio DESC "+
                                "LIMIT $limit",
                        parameters("limit", limit));

                while (r.hasNext()) {
                    Record rec = r.next();
                    Course2 c = new Course2(rec.get("title").asString(), rec.get("duration").asDouble(), rec.get("price").asDouble(), rec.get("course_pic").asString());
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

    // nuova, ricontrollare
    public List<User2> findMostFollowedUsers(int limit){
        List<User2> users = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            session.readTransaction(tx -> {
                Result r = tx.run("MATCH (u:User)<-[f:FOLLOW]-(u2:User) " +
                                "OPTIONAL MATCH (u)-[l:LIKE]->(:Course) "+
                                "WHERE u.username <> u2.username "+
                                "RETURN DISTINCT u, COUNT(DISTINCT(f)) AS followers, COUNT(DISTINCT(l)) AS likes "+
                                "ORDER BY followers DESC, likes DESC "+
                                "LIMIT $limit ",
                        parameters("limit", limit));

                while (r.hasNext()) {
                    Record rec = r.next();
                    User2 u = new User2(rec.get("username").asString(), rec.get("pic").asString(), rec.get("gender").asString());
                    users.add(u);
                }

                return null;
            });

            return users;
        }
        catch (Exception ex) {
            System.err.println("Error while retrieving suggestions from Neo4J");
            return null;
        }
    }

    /*

    QUERY DA AGGIUNGERE

        Most liked courses

        Most followed users

        Suggested users

        Suggested courses

        Find suggested courses considering instructors of the others courses you  have completed
     */

}
