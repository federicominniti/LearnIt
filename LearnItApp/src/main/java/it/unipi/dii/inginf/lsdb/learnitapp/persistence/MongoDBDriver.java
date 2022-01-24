package it.unipi.dii.inginf.lsdb.learnitapp.persistence;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.DBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Review;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.codecs.BsonArrayCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.json.JsonReader;
import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.skip;
import static com.mongodb.client.model.Filters.eq;
import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

public class MongoDBDriver implements DBDriver {
    private static MongoDBDriver mongoDBInstance;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Course> collection;
    private String mongoDBPrimaryIP;
    private int mongoDBPrimaryPort;
    private String mongoDBSecondIP;
    private int mongoDBSecondPort;
    private String mongoDBThirdIP;
    private int mongoDBThirdPort;

    private String mongoDBusername;
    private String mongoDBpassword;
    private String mongoDBName;
    private CodecRegistry pojoCodecRegistry;
    private CodecRegistry codecRegistry;

    public static MongoDBDriver getInstance() {
        if (mongoDBInstance == null) {
            mongoDBInstance = new MongoDBDriver(ConfigParams.getLocalConfig());
            mongoDBInstance.initConnection();
        }

        return mongoDBInstance;
    }

    private MongoDBDriver(ConfigParams configParams) {
        this.mongoDBPrimaryIP = configParams.getMongoDBPrimaryIP();
        this.mongoDBPrimaryPort = configParams.getMongoDBPrimaryPort();
        this.mongoDBSecondIP = configParams.getMongoDBSecondIP();
        this.mongoDBSecondPort = configParams.getMongoDBSecondPort();
        this.mongoDBThirdIP = configParams.getMongoDBThirdIP();
        this.mongoDBThirdPort = configParams.getMongoDBThirdPort();
        this.mongoDBusername = configParams.getMongoDBUsername();
        this.mongoDBpassword = configParams.getMongoDBPassword();
        this.mongoDBName = configParams.getMongoDBName();
    }

    @Override
    public boolean initConnection() {
        try {
            String uriString = "mongodb://";
            if (!mongoDBusername.equals("")) {
                uriString += mongoDBusername + ":" + mongoDBpassword + "@";
            }
            //uriString += primaryIP + ":" + primaryPort + "," + secondIP + ":" + secondPort + "," + thirdIP + ":" + thirdPort;
            pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
            codecRegistry = CodecRegistries.fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
            );

            ConnectionString uri = new ConnectionString("mongodb://127.0.0.1:27017/");
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(uri)
                    //.readPreference(ReadPreference.secondaryPreferred())
                    .retryWrites(true)
                    //.writeConcern(WriteConcern.W2)
                    .codecRegistry(codecRegistry)
                    .build();

            mongoClient = MongoClients.create(settings);

            //database = mongoClient.getDatabase(dbName);
            database = mongoClient.getDatabase("db");

            DBObject ping = new BasicDBObject("ping","1");

            collection = database.getCollection("learnit", Course.class);
            database.runCommand((Bson) ping);
        } catch (Exception e) {
            System.err.println("MongoDB unavailable");
            return false;
        }

        return true;
    }

    @Override
    public void closeConnection() {
        if (mongoClient != null)
            mongoClient.close();
    }

    //provata
    public boolean addCourse(Course newCourse) {
        try {
            collection.insertOne(newCourse);
        } catch (Exception e) {
            System.err.println("Error: cannot add new course");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    //provata
    public Course getCourseByTitle(String title) {
        try {
            Course c = collection.find(Filters.eq("title", title)).first();
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    //provata
    public boolean updateCourse(Course editedCourse) {
        try {
            Document d = new Document("title", editedCourse.getTitle())
                    .append("instructor", editedCourse.getInstructor())
                    .append("description", editedCourse.getDescription())
                    .append("language", editedCourse.getLanguage())
                    .append("level", editedCourse.getLevel())
                    .append("duration", editedCourse.getDuration())
                    .append("price", editedCourse.getPrice())
                    .append("sum_ratings", editedCourse.getSum_ratings())
                    .append("num_reviews", editedCourse.getNum_reviews());

            if (editedCourse.getCategory() != null)
                d.append("category", editedCourse.getCategory());

            if (editedCourse.getModality() != null)
                d.append("modality", editedCourse.getModality());

            if (editedCourse.getLink() != null)
                d.append("link", editedCourse.getLink());

            Bson updateOperation = new Document("$set", d);

            collection.updateOne(new Document("_id", editedCourse.getId()), updateOperation);
            return true;
        } catch (Exception e) {
            System.err.println("Error: cannot edit course");
            e.printStackTrace();
            return false;
        }
    }

    //provata
    public boolean deleteCourse(Course toBeDeleted) {
        try {
            collection.deleteOne(new Document("_id", toBeDeleted.getId()));
            return true;
        } catch (Exception e) {
            System.err.println("Error: cannot delete course");
            return false;
        }
    }

    //provata
    public boolean deleteUserCourses(User user) { // aggiungere indice per la find ???

        List<Course> toBeDeleted = collection.find(Filters.eq("instructor", user.getUsername())).into(new ArrayList<>());

        List<String> oids = new ArrayList<>();

        for (int i = 0; i < toBeDeleted.size(); i++) {
            oids.add(toBeDeleted.get(i).getId().toString());
        }
        Bson deleteFilter = Filters.in("_id", oids);
        return collection.deleteMany(deleteFilter).wasAcknowledged();
    }



    //provata
    public Course getCourseFromId(ObjectId oid){
        try {
            return  (Course) collection.find(eq("_id", oid)).first();
        }
        catch (Exception ex) {
            return null;
        }
    }


    /**
     * Function comment
     * @param title title of the searched courses
     * @param toSkip number of courses to skip because they were already loaded
     * @param quantity number of courses to retrieve
     * @return a list of courses
     */

    //provata
    public List<Course> searchCoursesByTitle (String title, int toSkip, int quantity) {
        Pattern pattern = Pattern.compile("^.*" + title + ".*$", Pattern.CASE_INSENSITIVE);
        Bson match = Aggregates.match(Filters.regex("title", pattern));
        Bson skip = skip(toSkip);
        Bson limit = limit(quantity);
        List<Course> results = collection.aggregate(asList(match, skip, limit))
                .into(new ArrayList<>());
        return results;
    }

    //provata
    public boolean addReview(Course course, Review review) {
        try {
            List<Review> reviewsList = course.getReviews();
            if (reviewsList == null)
                reviewsList = new ArrayList<>();
            reviewsList.add(review);
            updateReviews(course.getId(), reviewsList);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //provata
    public boolean addReviewRedundancies(Course course, Review review) {
        try {
            boolean result = addReview(course, review);
            System.out.println(result);
            if(result){
                System.out.println(course.getNum_reviews());
                System.out.println(course.getSum_ratings());
                System.out.println(review.getRating());
                course.setNum_reviews(course.getNum_reviews() + 1);
                course.setSum_ratings(course.getSum_ratings() + review.getRating());
                return updateCourse(course);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    //provata
    public boolean updateReviews(ObjectId id, List<Review> reviews) {
        Bson update = new Document("reviews", reviews);
        Bson updateOperation = new Document("$set", update);
        return collection.updateOne(new Document("_id", id), updateOperation).wasAcknowledged();
    }

    //provata
    public boolean editReview(Course course, Review review){
        List<Review> reviewsList = course.getReviews();
        int count = 0;
        for (Review r: reviewsList) {
            if(r.getAuthor().equals(review.getAuthor())){
                course.setSum_ratings(review.getRating()-r.getRating()+course.getSum_ratings());
                reviewsList.set(count, review);

                break;
            }
            count++;
        }
        return updateCourse(course) && updateReviews(course.getId(), reviewsList);
    }

    //provata
    public boolean deleteReview(Course course, Review review){
        List<Review> reviewsList  = course.getReviews();
        reviewsList.remove(review);
        course.setNum_reviews(course.getNum_reviews()-1);
        course.setSum_ratings(course.getSum_ratings()-review.getRating());
        return updateCourse(course) && updateReviews(course.getId(), reviewsList);
    }

    //provata
    public boolean deleteUserReviewsRedundancies(User user){
        boolean result = true;
        List<Course> toBeDeleted = collection.find(Filters.elemMatch("reviews", Filters.eq("author.username", user.getUsername()))).into(new ArrayList<>());
        System.out.println(toBeDeleted.get(1).getTitle());
        for(int i=0; i<toBeDeleted.size(); i++){

            List<Review> review = toBeDeleted.get(i).getReviews();

            for(int j=0; j<review.size(); j++){
                if(review.get(j).getAuthor().getUsername().equals(user.getUsername())){
                    Course c = toBeDeleted.get(i);
                    Review r = review.get(j);

                    if (c.getSum_ratings()-r.getRating() < 0)
                        c.setSum_ratings(0);
                    else
                        c.setSum_ratings(c.getSum_ratings()-r.getRating());

                    if (c.getNum_reviews() == 0)
                        c.setNum_reviews(0);
                    else
                        c.setNum_reviews(c.getNum_reviews()-1);

                    result = result && updateCourse(c);
                    break;
                }
            }
        }

        return result;
    }

    //provata
    public boolean deleteUserReviews(User user){

        if(deleteUserReviewsRedundancies(user)){
            System.out.println("qui");
            Bson pullFilter = Updates.pull("reviews", Filters.eq("author.username", user.getUsername()));
            return collection.updateMany(new Document(), pullFilter).wasAcknowledged();
        }
        return false;
    }

    //provata
    public List<Course> findCourses(double priceThreshold, double durationThreshold, String title, String level, String language, int toSkip, int quantity){

        List<Course> courses = null;

        Bson filter = null;

        if(priceThreshold != -1){
            filter = Filters.or(Filters.lte("price", priceThreshold), Filters.eq("price", 0), Filters.eq("price", null));
        }

        if(durationThreshold != -1){
            if(filter!=null)
                filter = Filters.and(filter, Filters.or(Filters.lte("duration", durationThreshold), Filters.eq("duration", 0), Filters.eq("duration", null)));
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
        courses = collection.find(filter)
                .skip(toSkip)
                .limit(quantity)
                .into(new ArrayList<Course>());

        return courses;
    }

    //provata
    public List<Course> findBestRatings(int limit){ // trasformare come trindingCourses se funziona ???

        String json = "[{'$addFields': {" +
                "'avg': { '$divide': ['$sum_ratings', '$num_reviews']}" +
                "}}, {'$sort': {'avg': -1}}, {'$limit': "+limit+"}]";

        List<BsonDocument> pipeline = new BsonArrayCodec().decode(new JsonReader(json), DecoderContext.builder().build())
                .stream().map(BsonValue::asDocument)
                .collect(Collectors.toList());
        List<Course> c = collection.aggregate(pipeline).into(new ArrayList<>());

        return c;
    }

    //provata
    public List<Course> trendingCourses(int limit){
        Date d = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        cal.add(Calendar.DATE, -7);

        Date d2 = cal.getTime();

        List<Course> c = collection.aggregate(Arrays.asList(
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
        List<Course> res = collection.find(Filters.in("_id", ids)).into(new ArrayList<>());
        return res;
    }


    /*
db.learnit.aggregate([{"$match": {"title": " Atención prehospitalaria del ictus agudo y selección de pacientes para tratamiento endovascular con la escala RACE"}},
{"$unwind": "$reviews"},
{"$group": {_id: {year: {"$year": "$reviews.edited_timestamp"}}, num_reviews: {"$sum": 1}}},
{"$set": {title: "$_id.year"}}, {"$unset": ["_id"]}])

    */

    //provata
    public HashMap<String, Double> getCourseAnnualRatings(Course course) {
        HashMap<String, Double> annualRatings = new HashMap<>();
        List<Course> doc = collection.aggregate(Arrays.asList(
                new Document("$match", new Document("_id", course.getId())),
                new Document("$unwind", "$reviews"),
                new Document("$group", new Document("_id", new Document("year", new Document("$year", "$reviews.edited_timestamp")))
                        .append("sum_ratings", new Document("$sum", "$reviews.rating"))
                        .append("num_reviews", new Document("$sum", 1))),
                new Document("$set", new Document("year", "$_id.year")),
                new Document("$unset", "_id")
        )).into(new ArrayList<>());

        for (Course c: doc) {
            System.out.println(c.getYear());
            annualRatings.put(String.valueOf(c.getYear()), ((double)c.getSum_ratings() / c.getNum_reviews()));
        }
        return annualRatings;
    }

    public Course getCourseInfoFromId(ObjectId id) {
        Bson filter = Filters.eq("_id", id);
        Bson projection = Projections.fields(Projections.exclude("reviews"));
        Course c = collection.find(filter).projection(projection).first();
        return c;
    }

    public Review getCourseReviewByUser(Course course, User user) {
        Course c = collection.aggregate(Arrays.asList(
                new Document("$match", new Document("_id", course.getId())),
                new Document("$project", new Document("_id", 1)
                        .append("reviews", 1)),
                new Document("$unwind", "$reviews"),
                new Document("$match", new Document("reviews.author.username", user.getUsername())),
                new Document("$group", new Document("_id", "$_id")
                        .append("reviews", new Document("$push", "$reviews")))
        )).first();

        if (c == null)
            return null;
        else
            return c.getReviews().get(0);
    }

    public List<Review> getCourseReviewsFromId(ObjectId id, int skip, int limit) {
        Course c = collection.aggregate(Arrays.asList(
                new Document("$match", new Document("_id", id)),
                new Document("$project", new Document("_id", 1)
                        .append("reviews", 1)),
                new Document("$unwind", "$reviews"),
                new Document("$skip", skip),
                new Document("$limit", limit),
                new Document("$group", new Document("_id", "$_id")
                        .append("reviews", new Document("$push", "$reviews")))

        )).first();

        return c.getReviews();
    }

}
