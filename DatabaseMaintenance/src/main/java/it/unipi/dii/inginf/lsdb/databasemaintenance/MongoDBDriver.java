package it.unipi.dii.inginf.lsdb.databasemaintenance;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.DBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
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
import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

public class MongoDBDriver {
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

            collection = database.getCollection("learnitprova", Course.class);
            database.runCommand((Bson) ping);
        } catch (Exception e) {
            System.err.println("MongoDB unavailable");
            return false;
        }

        return true;
    }

    public void closeConnection() {
        if (mongoClient != null)
            mongoClient.close();
    }

    //provata
    public boolean updateReviews(ObjectId id, List<Review> reviews) {
        Bson update = new Document("reviews", reviews);
        Bson updateOperation = new Document("$set", update);
        return collection.updateOne(new Document("_id", id), updateOperation).wasAcknowledged();
    }

    public MongoCursor<Course> fetchBigDocuments(int maxReviews) {
        Bson filter = Filters.and(Filters.exists("reviews"), Filters.where("this.reviews.length > " + maxReviews));
        MongoCursor<Course> bigCourses = collection.find(filter).cursor();
        return bigCourses;
    }


}
