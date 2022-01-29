package it.unipi.dii.inginf.lsdb.learnitapp.persistence;

import com.mongodb.*;
import com.mongodb.bulk.BulkWriteUpsert;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.internal.operation.BulkWriteBatch;
import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.log.LearnItLogger;
import it.unipi.dii.inginf.lsdb.learnitapp.model.*;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
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

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.skip;
import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

public class MongoDBDriver implements DBDriver {
    private static MongoDBDriver mongoDBInstance;

    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Course2> coursesCollection;
    private static MongoCollection<User2> usersCollection;
    private static String mongoDBPrimaryIP;
    private static int mongoDBPrimaryPort;
    private static String mongoDBSecondIP;
    private static int mongoDBSecondPort;
    private static String mongoDBThirdIP;
    private static int mongoDBThirdPort;

    private static String mongoDBUsername;
    private static String mongoDBPassword;
    private static String mongoDBName;
    private static CodecRegistry pojoCodecRegistry;
    private static CodecRegistry codecRegistry;
    private static String uriString;
    private static Logger mongoLogger;

    public static MongoDBDriver getInstance() {
        if (mongoDBInstance == null) {
            try {
                mongoDBInstance = new MongoDBDriver(ConfigParams.getInstance());
            } catch (Exception e) {
                uriString = "mongodb://127.0.0.1:27017/";
                mongoDBName = "db";
                mongoDBUsername = "root";
                mongoDBPassword = "";
            }

            mongoDBInstance = new MongoDBDriver();
            mongoDBInstance.initConnection();
            mongoLogger = LearnItLogger.getMongoLogger();
        }

        return mongoDBInstance;
    }

    private MongoDBDriver() {

    }

    private MongoDBDriver(ConfigParams configParams) {
        this.mongoDBPrimaryIP = configParams.getMongoDBPrimaryIP();
        this.mongoDBPrimaryPort = configParams.getMongoDBPrimaryPort();
        this.mongoDBSecondIP = configParams.getMongoDBSecondIP();
        this.mongoDBSecondPort = configParams.getMongoDBSecondPort();
        this.mongoDBThirdIP = configParams.getMongoDBThirdIP();
        this.mongoDBThirdPort = configParams.getMongoDBThirdPort();
        this.mongoDBUsername = configParams.getMongoDBUsername();
        this.mongoDBPassword = configParams.getMongoDBPassword();
        this.mongoDBName = configParams.getMongoDBName();
    }

    @Override
    public boolean initConnection() {
        try {
            uriString = "mongodb://";
            if (!mongoDBUsername.equals("")) {
                uriString += mongoDBUsername + ":" + mongoDBPassword + "@";
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

            coursesCollection = database.getCollection("learnit_edited", Course2.class);
            usersCollection = database.getCollection("users", User2.class);

            User u = usersCollection.find().first();
            System.out.println(u.getUsername());
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

    //provata - ok
    public boolean addCourse(Course2 newCourse) {
        try {
            coursesCollection.insertOne(newCourse);
        } catch (Exception e) {
            System.err.println("Error: cannot add new course");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    //provata
    public Course2 getCourseByTitle(String title) {
        try {
            Course2 c = coursesCollection.find(Filters.eq("title", title)).first();
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    //fare update su snapshot inserendo newprice e newduration se necessario
    //fare update anche su recensioni
    public boolean updateCourse(Course2 editedCourse, Course2 oldCourse) {
        try {
            Document d = new Document("title", editedCourse.getTitle())
                    .append("instructor", editedCourse.getInstructor())
                    .append("description", editedCourse.getDescription())
                    .append("language", editedCourse.getLanguage())
                    .append("level", editedCourse.getLevel());

            if (editedCourse.getCategory() != null)
                d.append("category", editedCourse.getCategory());

            if (editedCourse.getModality() != null)
                d.append("modality", editedCourse.getModality());

            if (editedCourse.getLink() != null)
                d.append("link", editedCourse.getLink());

            if (editedCourse.getCoursePic() != null && !editedCourse.getCoursePic().equals("") )
                d.append("course_pic", editedCourse.getCoursePic());

            if (editedCourse.getDuration() != 0)
                d.append("duration", editedCourse.getDuration());

            if (editedCourse.getPrice() != 0)
                d.append("price", editedCourse.getPrice());

            if (editedCourse.getNum_reviews() != 0)
                d.append("num_reviews", editedCourse.getNum_reviews());

            if (editedCourse.getSum_ratings() != 0)
                d.append("sum_ratings", editedCourse.getSum_ratings());

            if (editedCourse.getReviews() != null)
                d.append("sum_ratings")

            Bson updateOperation = new Document("$set", d);

            coursesCollection.updateOne(new Document("_id", editedCourse.getId()), updateOperation);

            /*
            db.learnitprova.updateMany(
  { reviews: {$exists: true}},
  { $set: { "reviews.$[elem].author.username" : "hello" } },
  {  arrayFilters: [ { "elem.author.username": "william" } ],
  multi: true
 })

             */

            //nuova
            /*
            Bson filter = Filters.exists("reviewed", true);
            Bson set = Updates.set("reviews.[$elem].author.username", "pino");
            UpdateOptions options = new UpdateOptions().arrayFilters(Collections.singletonList(Filters.eq("elem.author.username", "michael")));
            coursesCollection.updateMany(filter, set, options);

             */
            return true;
        } catch (Exception e) {
            System.err.println("Error: cannot edit course");
            return false;
        }
    }

    //non cancellare snapshot
    public boolean deleteCourse(Course2 toBeDeleted) {
        try {
            Bson match = Filters.eq("_id", toBeDeleted.getId());
            coursesCollection.deleteOne(match);
        } catch (MongoException e) {
            System.err.println("Error: cannot delete course");
            return false;
        }

        String errmsg;
        try {
            Bson update = Updates.pull("reviewed", Filters.eq("title", toBeDeleted.getTitle()));
            Bson empty = Filters.empty();
            usersCollection.updateMany(empty, update);
            return true;
        } catch (MongoException e) {
            System.err.println("Could not delete course in users collection");
            errmsg = e.getMessage();
        }

        try {
            coursesCollection.insertOne(toBeDeleted);
        } catch (MongoException e) {
            mongoLogger.error(errmsg);
            return false;
        }
    }

    //non cancellare snapshot su collection utenti
    public boolean deleteUserCourses(User2 user) { // aggiungere indice per la find ???

        List<Course2> toBeDeleted = coursesCollection.find(Filters.eq("instructor", user.getUsername())).projection(Projections.exclude("reviews")).into(new ArrayList<>());

        List<String> titles = new ArrayList<>();

        for (int i = 0; i < toBeDeleted.size(); i++) {
            titles.add(toBeDeleted.get(i).getTitle());
        }
        Bson deleteFilter = Filters.in("title", titles);

        return coursesCollection.deleteMany(deleteFilter).wasAcknowledged();
    }

    //ripensare e aggiungere snapshot su user collection
    private boolean addReviewToCourse(Course2 course, Review2 review) {
        try {
            List<Review2> reviewsList = course.getReviews();
            if (reviewsList == null)
                reviewsList = new ArrayList<>();
            reviewsList.add(review);
            updateReviews(course.getId(), reviewsList);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //accorpare a quella sopra
    public boolean addReviewRedundancies(Course2 course, Review2 review) {
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

    public boolean updateReviews(ObjectId id, List<Review2> reviews) {
        Bson update = new Document("reviews", reviews);
        Bson updateOperation = new Document("$set", update);
        return coursesCollection.updateOne(new Document("_id", id), updateOperation).wasAcknowledged();
    }

    //fare solo updatecourse aggiornata con aggiornamento ridondanze
    public boolean editReview(Course2 course, Review2 review){
        List<Review2> reviewsList = course.getReviews();
        int count = 0;
        for (Review2 r: reviewsList) {
            //System.out.println("scorro review \n title: "+ review.getTitle() + "\n content: "+ review.getContent() +"\n rating: "+ review.getRating()+"\n timestamp: "+review.getTimestamp().toString());

            if(r.getAuthor().getUsername().equals(review.getAuthor().getUsername())){
                //System.out.println("stesso author, count: "+count);
                course.setSum_ratings(review.getRating()-r.getRating()+course.getSum_ratings());
                reviewsList.set(count, review);

                break;
            }
            count++;
        }
        return updateCourse(course) && updateReviews(course.getId(), reviewsList);
    }

    //fare solo updatecourse aggiornata e rimuovere snapshot con rollback
    //non aggiornare ridondanze
    public boolean deleteReview(Course2 course, Review2 review){
        List<Review2> reviewsList  = course.getReviews();
        reviewsList.remove(review);
        course.setNum_reviews(course.getNum_reviews()-1);
        course.setSum_ratings(course.getSum_ratings()-review.getRating());
        return updateCourse(course) && updateReviews(course.getId(), reviewsList);
    }

    //rivedere ma va bene
    public boolean deleteUserReviews(User2 user){

            //System.out.println("qui");
            Bson pullFilter = Updates.pull("reviews", Filters.eq("author.username", user.getUsername()));
            return coursesCollection.updateMany(new Document(), pullFilter).wasAcknowledged();

    }

    //provata
    public List<Course2> findCourses(double priceThreshold, double durationThreshold, String title, String level, String language, int toSkip, int quantity){

        List<Course2> courses = null;

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
                .into(new ArrayList<Course2>());

        return courses;
    }

    //provata - OK
    public List<Course2> findBestRatings(int limit){ // trasformare come trindingCourses se funziona ???

        List<Document> aggregation = Arrays.asList(new Document("$match",
                        new Document("num_reviews",
                        new Document("$gt", 0L))),
                        new Document("$addFields",
                        new Document("avg",
                        new Document("$divide", Arrays.asList("$sum_ratings", "$num_reviews")))),
                        new Document("$sort",
                        new Document("avg", -1L)),
                        new Document("$limit", 10L),
                        new Document("$project",
                        new Document("reviews", 0L)));

        List<Course2> c = coursesCollection.aggregate(aggregation).into(new ArrayList<>());

        return c;
    }

    //provata
    public List<Course2> trendingCourses(int limit){
        Date d = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        cal.add(Calendar.DATE, -7);

        Date d2 = cal.getTime();

        List<Course2> c = coursesCollection.aggregate(Arrays.asList(
                new Document("$match", new Document("reviews.edited_timestamp", new Document("$gte", d2))),
                new Document("$unwind", "$reviews"),
                new Document("$group", new Document("_id", "$_id")
                        .append("size", new Document("$sum", 1))),
                new Document("$sort", new Document("size", -1)),
                new Document("$limit", limit)
        )).into(new ArrayList<>());


        List<ObjectId> ids = new ArrayList<>();
        for (Course2 item: c) {
            ids.add(item.getId());
        }
        List<Course2> res = findSnapshotsByIds(ids);
        return res;
    }

    public List<Course2> findSnapshotsByIds(List<ObjectId> ids) {
        List<Course2> res = coursesCollection.find(Filters.in("_id", ids)).projection(Projections.exclude("reviews")).into(new ArrayList<>());
        return res;
    }


    /*
db.learnit.aggregate([{"$match": {"title": " Atención prehospitalaria del ictus agudo y selección de pacientes para tratamiento endovascular con la escala RACE"}},
{"$unwind": "$reviews"},
{"$group": {_id: {year: {"$year": "$reviews.edited_timestamp"}}, num_reviews: {"$sum": 1}}},
{"$set": {title: "$_id.year"}}, {"$unset": ["_id"]}])

    */

    //provata
    public HashMap<String, Double> getCourseAnnualRatings(Course2 course) {
        HashMap<String, Double> annualRatings = new HashMap<>();
        List<Course2> doc = coursesCollection.aggregate(Arrays.asList(
                new Document("$match", new Document("_id", course.getId())),
                new Document("$unwind", "$reviews"),
                new Document("$group", new Document("_id", new Document("year", new Document("$year", "$reviews.edited_timestamp")))
                        .append("sum_ratings", new Document("$sum", "$reviews.rating"))
                        .append("num_reviews", new Document("$sum", 1))),
                new Document("$set", new Document("year", "$_id.year")),
                new Document("$unset", "_id"),
                new Document("$sort", new Document("year", 1))
        )).into(new ArrayList<>());

        for (Course2 c: doc) {
            System.out.println(c.getYear());
            annualRatings.put(String.valueOf(c.getYear()), ((double)c.getSum_ratings() / c.getNum_reviews()));
        }
        return annualRatings;
    }

    public Review2 getCourseReviewByUser(Course2 course, User2 user) {
        Course2 c = coursesCollection.find(Filters.eq("_id", course.getId())).projection(Projections.elemMatch("reviews", Filters.eq("author.username", user.getUsername()))).first();
        if (c == null)
            return null;
        else {
            if (c.getReviews() == null)
                return null;
            else
                return c.getReviews().get(0);
        }
    }

    public boolean checkCourseExists(String title) {
        Course2 c = coursesCollection.find(Filters.eq("title", title)).projection(Projections.include("title")).first();
        if (c == null)
            return false;
        return true;
    }

    //aggiungere
    /*
    mostActiveUsers
    bestUsers
     */

}
