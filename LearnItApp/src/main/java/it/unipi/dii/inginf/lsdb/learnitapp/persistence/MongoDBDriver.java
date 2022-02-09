package it.unipi.dii.inginf.lsdb.learnitapp.persistence;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.log.LearnItLogger;
import it.unipi.dii.inginf.lsdb.learnitapp.model.*;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.skip;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

public class MongoDBDriver implements DBDriver {
    private static MongoDBDriver mongoDBInstance;

    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Course> coursesCollection;
    private static MongoCollection<User> usersCollection;
    private static String mongoDBPrimaryIP;
    private static int mongoDBPrimaryPort;
    private static String mongoDBSecondIP;
    private static int mongoDBSecondPort;
    private static String mongoDBThirdIP;
    private static int mongoDBThirdPort;
    private static String mongoDBCollectionCourses;
    private static String mongoDBCollectionUsers;

    private static String mongoDBUsername;
    private static String mongoDBPassword;
    private static String mongoDBName;
    private static CodecRegistry pojoCodecRegistry;
    private static CodecRegistry codecRegistry;
    private static String uriString;
    private static Logger mongoLogger;
    private static boolean runningDefault;

    public static MongoDBDriver getInstance() {
        if (mongoDBInstance == null) {
            try {
                //mongoDBInstance = new MongoDBDriver(ConfigParams.getInstance());
                runningDefault = true;
                throw new Exception();
            } catch (Exception e) {
                mongoDBInstance = new MongoDBDriver();
                uriString = "mongodb://127.0.0.1:27017/";
                mongoDBName = "db";
                mongoDBUsername = "root";
                mongoDBPassword = "";
                runningDefault = true;
                mongoDBCollectionCourses = "learnit_edited";
                mongoDBCollectionUsers = "users_new";
            }

            mongoDBInstance.initConnection();
            mongoLogger = LearnItLogger.getMongoLogger();
        }

        return mongoDBInstance;
    }

    private MongoDBDriver() {

    }

    private MongoDBDriver(ConfigParams configParams) {
        mongoDBPrimaryIP = configParams.getMongoDBPrimaryIP();
        mongoDBPrimaryPort = configParams.getMongoDBPrimaryPort();
        mongoDBSecondIP = configParams.getMongoDBSecondIP();
        mongoDBSecondPort = configParams.getMongoDBSecondPort();
        mongoDBThirdIP = configParams.getMongoDBThirdIP();
        mongoDBThirdPort = configParams.getMongoDBThirdPort();
        mongoDBUsername = configParams.getMongoDBUsername();
        mongoDBPassword = configParams.getMongoDBPassword();
        mongoDBName = configParams.getMongoDBName();
        mongoDBCollectionCourses = configParams.getMongoDBCollectionCourses();
        mongoDBCollectionUsers = configParams.getMongoDBCollectionUsers();
    }

    @Override
    public boolean initConnection() {
        try {
            if (!runningDefault) {
                uriString = "mongodb://";
                if (!mongoDBUsername.equals("")) {
                    uriString += mongoDBUsername + ":" + mongoDBPassword + "@";
                }
                uriString += mongoDBPrimaryIP + ":" +
                        mongoDBPrimaryPort + "," +
                        mongoDBSecondIP + ":" +
                        mongoDBSecondPort + "," +
                        mongoDBThirdIP + ":" +
                        mongoDBThirdPort;
            }

            pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
            codecRegistry = CodecRegistries.fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

            ConnectionString uri = new ConnectionString(uriString);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(uri)
                    .readPreference(ReadPreference.nearest())
                    .retryWrites(true)
                    .writeConcern(WriteConcern.W1)
                    .codecRegistry(codecRegistry)
                    .build();

            mongoClient = MongoClients.create(settings);

            database = mongoClient.getDatabase(mongoDBName);
            DBObject ping = new BasicDBObject("ping","1");

            coursesCollection = database.getCollection(mongoDBCollectionCourses, Course.class);
            usersCollection = database.getCollection(mongoDBCollectionUsers, User.class);
            database.runCommand((Bson) ping);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public void closeConnection() {
        if (mongoClient != null)
            mongoClient.close();
    }

    /**
     * Adds a course to the database
     * @param newCourse the course to be created
     */
    public void addCourse(Course newCourse) throws MongoException {
        coursesCollection.insertOne(newCourse);
    }

    /**
     * Returns a course with a certain title
     * @param title the title of the course
     */
    public Course getCourseByTitle(String title) {
        try {
            Course c = coursesCollection.find(Filters.eq("title", title)).first();
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Updates the information of a course
     * If oldCourse is null, only reviews (and redundancies about reviews) will be updated
     * If oldCourse is not null, only information about the course (that are not reviews or reviews redundancies)
     * will be updated
     * Course snapshots embedded in the user collection will be updated if the price or duration or picture of a course
     * changes
     * @param editedCourse the course to be updated
     * @param oldCourse the old course information
     */
    public boolean updateCourse(Course editedCourse, Course oldCourse, User rollbackSnapshotsOfUser) throws MongoException {
        if (oldCourse == null) {
            //this is the case when you want to update the course reviews only
            Bson filter = Filters.eq("_id", editedCourse.getId());
            Bson update_reviews = Updates.set("reviews", editedCourse.getReviews());


            //updating redundancies
            Bson update_sum = null;
            if (editedCourse.getSum_ratings() > 0)
                update_sum = Updates.set("sum_ratings", editedCourse.getSum_ratings());
            Bson update_num = null;
            if (editedCourse.getNum_reviews() > 0)
                update_num = Updates.set(("num_reviews"), editedCourse.getNum_reviews());

            coursesCollection.updateOne(filter, Updates.combine(update_reviews, update_sum, update_num));
            if (rollbackSnapshotsOfUser != null) {
                Bson match = Filters.eq("username", rollbackSnapshotsOfUser.getUsername());
                Bson update = Updates.pull("reviewed", Filters.eq("title", editedCourse.getTitle()));
                usersCollection.updateOne(match, update);
            }
            return true;
        } else {

            //case of update course info only (not reviews)
            Bson filter = Filters.eq("_id", editedCourse.getId());
            List<Bson> updates = new ArrayList<>();
            updates.add(Updates.set("description", editedCourse.getDescription()));
            updates.add(Updates.set("language", editedCourse.getLanguage()));
            updates.add(Updates.set("level", editedCourse.getLevel()));

            if (editedCourse.getCategory() == null)
                updates.add(Updates.unset("category"));
            else
                updates.add(Updates.set("category", editedCourse.getCategory()));

            if (editedCourse.getModality() == null)
                updates.add(Updates.unset("modality"));
            else
                updates.add(Updates.set("modality", editedCourse.getModality()));

            if (editedCourse.getLink() == null)
                updates.add(Updates.unset("link"));
            else
                updates.add(Updates.set("link", editedCourse.getLink()));

            if (editedCourse.getCoursePic() == null)
                updates.add(Updates.unset("course_pic"));
            else
                updates.add(Updates.set("course_pic", editedCourse.getCoursePic()));

            if (editedCourse.getDuration() == 0)
                updates.add(Updates.unset("duration"));
            else
                updates.add(Updates.set("duration", editedCourse.getDuration()));

            if (editedCourse.getPrice() == 0)
                updates.add(Updates.unset("price"));
            else
                updates.add(Updates.set("price", editedCourse.getPrice()));

            if (editedCourse.getNum_reviews() == 0)
                updates.add(Updates.unset("num_reviews"));
            else
                updates.add(Updates.set("num_reviews", editedCourse.getNum_reviews()));

            if (editedCourse.getSum_ratings() == 0)
                updates.add(Updates.unset("sum_ratings"));
            else
                updates.add(Updates.set("sum_ratings", editedCourse.getSum_ratings()));


            //if this optional fields change, we need to update the snapshots present
            //in the user collection
            List<Bson> update_snapshots = new ArrayList<>();
            if (editedCourse.getCoursePic() == null) {
                if (oldCourse.getCoursePic() != null) {
                    update_snapshots.add(Updates.unset("reviewed.$[elem].course_pic"));
                }
            } else if (!editedCourse.getCoursePic().equals(oldCourse.getCoursePic())) {
                update_snapshots.add(Updates.set("reviewed.$[elem].course_pic", editedCourse.getCoursePic()));
            }

            if (Double.compare(editedCourse.getPrice(), oldCourse.getPrice()) != 0) {
                update_snapshots.add(Updates.set("reviewed.$[elem].new_price", editedCourse.getPrice()));
            }

            if (Double.compare(editedCourse.getDuration(), oldCourse.getDuration()) != 0) {
                update_snapshots.add(Updates.set("reviewed.$[elem].new_duration", editedCourse.getDuration()));
            }

            //if there are no changes to be made to the snapshots in the user collection
            if (update_snapshots.size() == 0) {
                coursesCollection.updateOne(filter, Updates.combine(updates));
                return true;
            } else {
                //there is at least one field to change in the snapshots of the user collection
                coursesCollection.updateOne(filter, Updates.combine(updates));

                Bson exists = Filters.exists("reviewed", true);
                Bson set = Updates.combine(update_snapshots);
                UpdateOptions options = new UpdateOptions().arrayFilters(Collections.singletonList(Filters.eq("elem.title", editedCourse.getTitle())));
                try {
                    //try to perform the update
                    usersCollection.updateMany(exists, set, options);
                    return true;
                } catch (MongoException e) {
                    //if the update goes bad, try to rollback by reupdating the course with old information
                    //and write to log
                    e.printStackTrace();
                    boolean ret = updateCourse(oldCourse, oldCourse, null); //rollback
                    if (!ret) {
                        mongoLogger.error(e.getMessage());
                        mongoLogger.error("UPDATE COURSE INFORMATION: ROLLBACK FAILED");
                        mongoLogger.error(oldCourse.toString());
                    }
                    return false;
                }
            }
        }

    }

    /**
     * Deletes a course from the courses collection
     */
    public boolean deleteCourse(Course toBeDeleted) {
        try {
            Bson match = Filters.eq("_id", toBeDeleted.getId());
            coursesCollection.deleteOne(match);
            return true;
        } catch (MongoException e) {
            System.err.println("Error: cannot delete course");
            return false;
        }
    }

    /**
     * Deletes all the courses offered by a user
     */
    public void deleteUserCourses(User user) throws MongoException { // aggiungere indice per la find ???
        Bson deleteFilter = Filters.eq("instructor", user.getUsername());
        coursesCollection.deleteMany(deleteFilter);
    }

    /**
     * Edits the information about a specific review of a course
     */
    public boolean editReview(Course course, Review review){
        List<Review> reviews = course.getReviews();
        int count = 0;
        int sum_ratings = course.getSum_ratings();
        for (Review r: reviews) {
            if (r.getUsername().equals(review.getUsername())) {
                sum_ratings -= r.getRating();
                reviews.set(count, review);
                break;
            }
            count++;
        }

        course.setSum_ratings(sum_ratings + review.getRating());
        return updateCourse(course, null, null);
    }

    /**
     * Deletes all the reviews of a certain user
     */
    public void deleteUserReviews(User user) throws MongoException {
        Bson pullFilter = Updates.pull("reviews", Filters.eq("username", user.getUsername()));
        coursesCollection.updateMany(Filters.empty(), pullFilter);
    }

    /**
     * Finds all courses with certain parameters
     * @param priceThreshold the max price
     * @param durationThreshold the max duration
     * @param title the searched title
     * @param level the selected level
     * @param language the selected language
     * @param toSkip courses to be skipped
     * @param quantity the quantity to be loaded
     * @return a list of Course
     */
    public List<Course> findCourses(double priceThreshold, double durationThreshold, String title, String level, String language, int toSkip, int quantity){

        List<Course> courses = null;

        Bson filter = null;

        if(priceThreshold != -1){
            filter = Filters.or(Filters.lte("price", priceThreshold), Filters.eq("price", null));
        }

        if(durationThreshold != -1){
            if(filter!=null)
                filter = Filters.and(filter, Filters.or(Filters.lte("duration", durationThreshold), Filters.eq("duration", null)));
            else
                filter = Filters.lte("duration", durationThreshold);
        }

        if(!level.equals("")){
            if(filter!=null)
                filter = Filters.and(filter, Filters.eq("level", level));
            else
                filter = Filters.eq("level", level);
        }

        if(!language.equals("")){
            language = language.toLowerCase();
            language = language.substring(0, 1).toUpperCase() + language.substring(1);

            if(filter!=null)
                filter = Filters.and(filter, Filters.eq("language", language));
            else
                filter = Filters.eq("language", language);
        }

        if(!title.equals("")){

            Pattern pattern = Pattern.compile("^.*" + title + ".*$", Pattern.CASE_INSENSITIVE);
            if (filter != null) {
                filter = Filters.and(Filters.regex("title", pattern), filter);
            } else
                filter = Filters.regex("title", pattern);
        }

        Bson skip = skip(toSkip);
        Bson limit = limit(quantity);
        courses = coursesCollection.find(filter)
                .skip(toSkip)
                .limit(quantity)
                .projection(Projections.exclude("reviews"))
                .into(new ArrayList<Course>());

        return courses;
    }

    /**
     * Finds the courses with the best rating
     * @param limit the max number of courses to return
     * @return a list of Course
     */
    public List<Course> findBestRatings(int limit){ // trasformare come trindingCourses se funziona ???
        List<Document> aggregation = Arrays.asList(
                new Document("$match", new Document("num_reviews", new Document("$gt", 0))),
                new Document("$addFields", new Document("avg", new Document("$divide",
                        Arrays.asList("$sum_ratings", "$num_reviews")))),
                new Document("$sort", new Document("avg", -1)),
                new Document("$limit", limit),
                new Document("$project", new Document("reviews", 0)));

        List<Course> c = coursesCollection.aggregate(aggregation).into(new ArrayList<>());

        return c;
    }

    /**
     * Finds the courses that received most reviews during the last 30 days
     * @param limit the max quantity of courses to return
     * @return a list of Course
     */
    public List<Course> trendingCourses(int limit){
        Date d = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        cal.add(Calendar.DATE, -30);

        Date d2 = cal.getTime();

        List<Course> c = coursesCollection.aggregate(Arrays.asList(
                new Document("$match", new Document("reviews.edited_timestamp", new Document("$gte", d2))),
                new Document("$unwind", "$reviews"),
                new Document("$group", new Document("_id", "$_id")
                        .append("size", new Document("$sum", 1))),
                new Document("$sort", new Document("size", -1)),
                new Document("$limit", limit)
        )).into(new ArrayList<>());


        List<ObjectId> ids = new ArrayList<>();
        for (Course item: c) {
            ids.add(item.getId());
        }
        List<Course> res = findSnapshotsByIds(ids);
        return res;
    }

    public List<Course> findSnapshotsByIds(List<ObjectId> ids) {
        List<Course> res = coursesCollection.find(Filters.in("_id", ids)).projection(Projections.exclude("reviews")).into(new ArrayList<>());
        return res;
    }

    /**
     * Groups reviews by year and calculates the mean rating for every past year
     * @return a HashMap associating a year with a mean rating
     */
    public HashMap<String, Double> getCourseAnnualRatings(Course course) {
        HashMap<String, Double> annualRatings = new HashMap<>();
        List<Course> doc = coursesCollection.aggregate(Arrays.asList(
                new Document("$match", new Document("_id", course.getId())),
                new Document("$unwind", "$reviews"),
                new Document("$group", new Document("_id", new Document("year", new Document("$year", "$reviews.edited_timestamp")))
                        .append("sum_ratings", new Document("$sum", "$reviews.rating"))
                        .append("num_reviews", new Document("$sum", 1))),
                new Document("$set", new Document("year", "$_id.year")),
                new Document("$unset", "_id"),
                new Document("$sort", new Document("year", 1))
        )).into(new ArrayList<>());

        for (Course c: doc) {
            annualRatings.put(String.valueOf(c.getYear()), ((double)c.getSum_ratings() / c.getNum_reviews()));
        }
        return annualRatings;
    }

    /**
     * Calculates the users that have written the highest number of reviews in the last 30 days
     * @param limit the max number of users to return
     * @return a list of User
     */
    public List<User> mostActiveUsers(int limit) {
        Date d = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.DATE, -30);
        Date d2 = cal.getTime();

        List<Document> aggregation;
        aggregation = Arrays.asList(new Document("$match", new Document("reviewed", new Document("$exists", true))),
                new Document("$unwind", new Document("path", "$reviewed")),
                new Document("$match", new Document("reviewed.review_timestamp", new Document("$gte", d2))),
                new Document("$group", new Document("_id", new Document("username", "$username"))
                        .append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("count", -1)),
                new Document("$project", new Document("username", "$_id.username")
                        .append("_id", 0)),
                new Document("$limit", limit));

        List<User> users = usersCollection.aggregate(aggregation).into(new ArrayList<>());
        List<String> usernames = new ArrayList<>();
        for (User u: users)
            usernames.add(u.getUsername());

        return findUserSnapshotsByIds(usernames);
    }

    private List<User> findUserSnapshotsByIds(List<String> usernames) {
        List<User> res = usersCollection.find(Filters.in("username", usernames)).projection(Projections.exclude("reviewed")).into(new ArrayList<>());
        return res;
    }

    /**
     * Checks if a course with same title exists (case insensitive)
     * @param title the title to be checked
     */
    public boolean courseAlreadyExists(String title) {
        Pattern pattern = Pattern.compile("^.*" + title + ".*$", Pattern.CASE_INSENSITIVE);
        Course c = coursesCollection.find(Filters.regex("title", pattern)).projection(Projections.exclude("reviews")).first();
        return c != null;
    }

    /**
     * Return the information about a user based on its username
     */
    public User getUserByUsername(String username) {
        try {
            User u = usersCollection.find(Filters.eq("username", username))
                    .projection(Projections.exclude("reviewed"))
                    .first();
            return u;
        } catch (MongoException e) {
            return null;
        }
    }

    /**
     * Calculates the users that are the most skilled when choosing courses, based on their duration and price
     */
    public List<User> bestUsers(int limit){
        List<Document> aggregation = Arrays.asList(new Document("$unwind",
                        new Document("path", "$reviewed")),
                new Document("$match",
                        new Document("reviewed.duration",
                                new Document("$gt", 0))
                                .append("reviewed.price",
                                        new Document("$gt", 0))),
                new Document("$addFields",
                        new Document("duration_div_price",
                                new Document("$divide", Arrays.asList("$reviewed.duration", "$reviewed.price")))),
                new Document("$group",
                        new Document("_id",
                                new Document("username", "$username"))
                                .append("value",
                                        new Document("$avg", "$duration_div_price"))),
                new Document("$project",
                        new Document("_id", 0)
                                .append("username", "$_id.username")
                                .append("value", "$value")),
                new Document("$sort",
                        new Document("value", 1)),
                new Document("$limit", limit));

        List<User> users = usersCollection.aggregate(aggregation).into(new ArrayList<>());
        List<String> usernames = new ArrayList<>();
        for (User u: users)
            usernames.add(u.getUsername());

        List<User> res = findUserSnapshotsByIds(usernames);
        return res;
    }

    /**
     * Adds a review to a course
     */
    private void addReviewToCourse(Course course, Review review) throws MongoException{
        List<Review> reviewsList = course.getReviews();
        if (reviewsList == null)
            reviewsList = new ArrayList<>();
        reviewsList.add(review);
        course.setReviews(reviewsList);
        course.setNum_reviews(course.getNum_reviews() + 1);
        course.setSum_ratings(course.getSum_ratings() + review.getRating());
        updateCourse(course, null, null);
    }

    /**
     * Adds a snapshot of a reviewed course to the document of a user, together with the review timestamp
     */
    private void addSnapshotCourseToUser(Course course, Review review) throws MongoException{
        Document d = new Document("title", course.getTitle())
                .append("review_timestamp", review.getTimestamp());

        if (course.getDuration() != null && course.getDuration() != 0)
            d.append("duration", course.getDuration());

        if (course.getPrice() != null && course.getPrice() != 0)
            d.append("price", course.getPrice());

        if (course.getCoursePic() != null)
            d.append("course_pic", course.getCoursePic());

        usersCollection.updateOne(Filters.eq("username", review.getUsername()),
                Updates.push("reviewed", d));

    }

    /**
     * Handles the whole process of adding a review to a course, updating redundancies of the course
     * and adding the snapshot of the course to the document of the user
     * @param course
     * @param review
     * @return
     */
    public boolean addReview(Course course, Review review, boolean inRollback) throws MongoException{
        addReviewToCourse(course, review);

        String exception;
        try{
            addSnapshotCourseToUser(course, review);
            return true;
        }catch (MongoException e){
            exception = e.getMessage();
        }

        if (inRollback)
            throw new MongoException(exception);
        else {
            try {
                deleteReviewFromCourses(course, review);
            } catch (MongoException e){
                mongoLogger.error(exception);
                mongoLogger.error("ADD REVIEW: ROLLBACK FAILED");
                mongoLogger.error(course.toString());
                mongoLogger.error(review.toString());
            }
        }
        return false;
    }

    private void deleteSnapshotCourseFromUser(Course course, Review review) throws MongoException{
        Bson userFilter = Filters.eq("username", review.getUsername());
        Bson pullFilter = Updates.pull("reviewed", Filters.eq("title", course.getTitle()));
        usersCollection.updateOne(userFilter, pullFilter);
    }

    private void deleteReviewFromCourses(Course course, Review review) throws MongoException{
        Bson courseFilter = Filters.eq("title", course.getTitle());
        Bson pullFilter = Updates.pull("reviews", Filters.eq("username", review.getUsername()));
        coursesCollection.updateOne(courseFilter, pullFilter);
    }

    /**
     * Deletes a review from a course
     * @param redundanciesRollback defines if updating the redundancies of the course or not
     */
    public boolean deleteReview(Course course, Review review, boolean redundanciesRollback) throws MongoException{
        List<Review> reviewsList  = course.getReviews();
        reviewsList.remove(review);
        if(redundanciesRollback) {
            //rollback of an add review operation. In this case we must update redundancies to
            // avoid to fake them
            course.setNum_reviews(course.getNum_reviews() - 1);
            course.setSum_ratings(course.getSum_ratings() - review.getRating());
            User rollbackThis = new User();
            rollbackThis.setUsername(review.getUsername());
            return updateCourse(course, null, rollbackThis);
        }else {
            deleteReviewFromCourses(course, review);

            String exception;
            try {
                deleteSnapshotCourseFromUser(course, review);
                return true;
            } catch (MongoException e) {
                exception = e.getMessage();
            }

            try {
                addReviewToCourse(course, review);
            } catch (MongoException e){
                mongoLogger.error(exception);
                mongoLogger.error("DELETE COURSE SNAPSHOT FROM USER: ROLLBACK FAILED");
                mongoLogger.error(course.toString());
                mongoLogger.error(review.toString());
            }
            return false;
        }
    }

    /**
     * Gets the review of the user for a certain course, if there exists one
     * @return the Review object or null
     */
    public Review getCourseReviewByUser(Course course, User user) {
        Course c = coursesCollection.find(Filters.eq("_id", course.getId())).projection(Projections.elemMatch(
                "reviews", Filters.eq("username", user.getUsername()))).first();
        if (c == null)
            return null;
        else {
            if (c.getReviews() == null)
                return null;
            else
                return c.getReviews().get(0);
        }
    }

    /**
     * Checks if the password submitted by a user is the same as the hash saved in the database
     */
    public User login(String username, String password) {
        String shaPass = "";
        try {
            shaPass = Utils.toHexString(Utils.getSHA(password));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Bson eqUsername = Filters.eq("username", username);
        Bson eqPass = Filters.eq("password", shaPass);
        try {
            User loggedUser = usersCollection.find(Filters.and(eqUsername, eqPass)).projection(Projections.exclude("reviewed")).first();
            return loggedUser;
        } catch (MongoException e) {
            return null;
        }
    }

    /**
     * Updates the information about a user
     */
    public void editProfileInfo(User newInfo) throws MongoException {
        String shaPass = "";
        try {
            shaPass = Utils.toHexString(Utils.getSHA(newInfo.getPassword()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }
        if (newInfo.getRole() == 1) {
            Bson match = Filters.eq("username", newInfo.getUsername());
            Bson update = Updates.set("password", shaPass);
            usersCollection.updateOne(match, update);
        }

        List<Bson> updates = new ArrayList<>();
        if (newInfo.getProfilePic() == null)
            updates.add(Updates.unset("pic"));
        else
            updates.add(Updates.set("pic", newInfo.getProfilePic()));

        if (newInfo.getGender() == null)
            updates.add(Updates.unset("gender"));
        else
            updates.add(Updates.set("gender", newInfo.getGender()));

        if (newInfo.getDateOfBirth() == null)
            updates.add(Updates.unset("birthdate"));
        else
            updates.add(Updates.set("birthdate", newInfo.getDateOfBirth()));

        updates.add(Updates.set("email", newInfo.getEmail()));
        updates.add(Updates.set("password", shaPass));
        updates.add(Updates.set("complete_name", newInfo.getCompleteName()));

        Bson filter = Filters.eq("username", newInfo.getUsername());
        usersCollection.updateOne(filter, Updates.combine(updates));
    }

    /**
     * Calculates the average statistics of a user:
     * average money spent on courses, average duration of reviewed courses and total number of reviewed courses
     */
    public HashMap<String, Double> avgStatistics(User user) {
        List<Document> aggregation =
                Arrays.asList(new Document("$match", new Document("username", user.getUsername())
                                .append("reviewed", new Document("$exists", true))),
                        new Document("$unwind", new Document("path", "$reviewed")),
                        new Document("$group", new Document("_id", "$username")
                                .append("count", new Document("$sum", 1))
                                .append("avgprice", new Document("$avg", "$reviewed.price"))
                                .append("avgduration", new Document("$avg", "$reviewed.duration"))),
                        new Document("$project", new Document("_id", 0)
                                        .append("username", "$_id.username")
                                        .append("count", 1)
                                        .append("avgprice", 1)
                                        .append("avgduration", 1)));

        User doc = null;
        try {
            doc = usersCollection.aggregate(aggregation).first();
        } catch (MongoException e) {
            return null;
        }

        if (doc == null)
            return null;

        HashMap<String, Double> hashMap = new HashMap<>();
        if (doc.getCount() != null && doc.getCount() != 0)
            hashMap.put("count", doc.getCount().doubleValue());

        if (doc.getAvgPrice() != null && doc.getAvgPrice() != 0)
            hashMap.put("avgprice", doc.getAvgPrice());

        if (doc.getAvgDuration() != null && doc.getAvgDuration() != 0)
            hashMap.put("avgduration", doc.getAvgDuration());

        return hashMap;
    }

    /**
     * Search functionality for users, confronts the searched text with the username and the complete name
     */
    public List<User> searchUserByUsername(String searchedText, int skip, int limit) {
        try {
            Pattern pattern = Pattern.compile("^.*" + searchedText + ".*$", Pattern.CASE_INSENSITIVE);
            Bson usernameFilter = Filters.regex("username", pattern);
            Bson completeNameFilter = Filters.regex("complete_name", pattern);
            List<User> res = usersCollection.find(Filters.or(usernameFilter, completeNameFilter))
                    .skip(skip)
                    .limit(limit)
                    .projection(Projections.exclude("reviewed"))
                    .into(new ArrayList<>());

            return res;
        } catch (MongoException e) {
            return new ArrayList<>();
        }
    }

    public boolean checkIfUserExists(String username) {
        try {
            Pattern pattern = Pattern.compile("^.*" + username + ".*$", Pattern.CASE_INSENSITIVE);
            User user = usersCollection.find(Filters.regex("username", pattern))
                    .projection(Projections.exclude("reviewed"))
                    .first();
            return (user != null);
        } catch (MongoException e) {
            return true;
        }
    }

    /**
     * Adds a new user to the database
     */
    public void addUser(User user) throws MongoException {
        String shaPass = "";
        try {
            shaPass = Utils.toHexString(Utils.getSHA(user.getPassword()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        String clear = user.getPassword();
        user.setPassword(shaPass);
        usersCollection.insertOne(user);
        user.setPassword(clear);
    }

    /**
     * Finds all courses where the specified user is the instructor
     */
    public List<Course> findCoursesOfferedByUser(User user, int skip, int limit) {
        try {
            List<Course> list = coursesCollection.find(Filters.eq("instructor", user.getUsername()))
                    .projection(Projections.exclude("reviews"))
                    .skip(skip)
                    .limit(limit)
                    .into(new ArrayList<>());

            return list;
        } catch (MongoException e) {
            return new ArrayList<>();
        }
    }

    public void deleteUser(User user) throws MongoException {
        Bson filter = Filters.eq("username", user.getUsername());
        usersCollection.deleteOne(filter);
    }

    /**
     * Gets all the reviewed courses of a user
     */
    public List<Course> getAllUserReviews(User user, int skip, int limit) {
        List<Document> aggregation =
        Arrays.asList(new Document("$match",
                        new Document("username", user.getUsername())),
                new Document("$unwind",
                        new Document("path", "$reviewed")),
                new Document("$sort",
                        new Document("reviewed.review_timestamp", -1)),
                new Document("$skip", skip),
                new Document("$limit", limit),
                new Document("$group",
                        new Document("_id", "$_id")
                                .append("reviewed",
                                        new Document("$push", "$reviewed"))));

        User res = usersCollection.aggregate(aggregation).first();
        if (res == null)
            return new ArrayList<>();
        if (res.getReviewedCourses() == null)
            return new ArrayList<>();

        return res.getReviewedCourses();
    }
}
