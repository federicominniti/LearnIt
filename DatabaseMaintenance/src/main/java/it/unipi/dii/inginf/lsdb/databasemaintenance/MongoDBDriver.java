package it.unipi.dii.inginf.lsdb.databasemaintenance;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.util.*;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

public class MongoDBDriver {
    private static MongoDBDriver mongoDBInstance;

    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Course> coursesCollection;
    private static String mongoDBPrimaryIP;
    private static int mongoDBPrimaryPort;
    private static String mongoDBSecondIP;
    private static int mongoDBSecondPort;
    private static String mongoDBThirdIP;
    private static int mongoDBThirdPort;
    private static String mongoDBCollectionCourses;

    private static String mongoDBUsername;
    private static String mongoDBPassword;
    private static String mongoDBName;
    private static CodecRegistry pojoCodecRegistry;
    private static CodecRegistry codecRegistry;
    private static String uriString;
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
            }

            mongoDBInstance.initConnection();
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
    }

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
            database.runCommand((Bson) ping);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void closeConnection() {
        if (mongoClient != null)
            mongoClient.close();
    }

    public boolean updateReviews(ObjectId id, List<Review> reviews) {
        Bson update = new Document("reviews", reviews);
        Bson updateOperation = new Document("$set", update);
        return coursesCollection.updateOne(new Document("_id", id), updateOperation).wasAcknowledged();
    }

    /**
     * Fetches documents of courses with a large amount of reviews
     * @param maxReviews the number of reviews necessary to be considered a "big" document
     * @return a cursor to iterate over big documents
     */
    public MongoCursor<Course> fetchBigDocuments(int maxReviews) {
        Bson filter = Filters.and(Filters.exists("reviews"), Filters.where("this.reviews.length > " + maxReviews));
        MongoCursor<Course> bigCourses = coursesCollection.find(filter).cursor();
        return bigCourses;
    }


}
