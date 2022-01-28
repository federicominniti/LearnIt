package it.unipi.dii.inginf.lsdb.learnitapp.log;

import it.unipi.dii.inginf.lsdb.learnitapp.persistence.MongoDBDriver;
import it.unipi.dii.inginf.lsdb.learnitapp.persistence.Neo4jDriver;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class LearnItLogger {
    private static LearnItLogger learnItLogger = new LearnItLogger();

    private static final String LOG_FILE = "log4j.properties";

    private static final Logger mongoLogger = Logger.getLogger(MongoDBDriver.class);
    private static final Logger neo4jLogger = Logger.getLogger(Neo4jDriver.class);

    private LearnItLogger() {
        try {
            Properties loggerProperties = new Properties();
            loggerProperties.load(new FileReader(LOG_FILE));
            PropertyConfigurator.configure(loggerProperties);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getMongoLogger(){
        return mongoLogger;
    }

    public static Logger getNeo4jLogger(){
        return neo4jLogger;
    }
}