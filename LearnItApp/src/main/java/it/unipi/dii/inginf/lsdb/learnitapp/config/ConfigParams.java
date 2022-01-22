package it.unipi.dii.inginf.lsdb.learnitapp.config;

import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;

import java.io.IOException;

public class ConfigParams {
    private static volatile ConfigParams localConfig;
    private String mongoDBPrimaryIP;
    private int mongoDBPrimaryPort;
    private String mongoDBSecondIP;
    private int mongoDBSecondPort;
    private String mongoDBThirdIP;
    private int mongoDBThirdPort;

    private String mongoDBUsername;
    private String mongoDBPassword;
    private String mongoDBName;

    private String neo4jIP;
    private int neo4jPort;
    private String neo4jUsername;
    private String neo4jPassword;

    private int limitNumber;

    public static ConfigParams getInstance() throws IOException {
        if (localConfig == null) {
            synchronized (ConfigParams.class) {
                if (localConfig == null) {
                    localConfig = Utils.getParams();
                }
            }
        }
    return localConfig;
    }

    public static ConfigParams getLocalConfig() {
        return localConfig;
    }

    public static void setLocalConfig(ConfigParams localConfig) {
            ConfigParams.localConfig = localConfig;
        }

    public String getMongoDBPrimaryIP() {
            return mongoDBPrimaryIP;
        }

    public void setMongoDBPrimaryIP(String mongoDBPrimaryIP) {
            this.mongoDBPrimaryIP = mongoDBPrimaryIP;
        }

    public int getMongoDBPrimaryPort() {
            return mongoDBPrimaryPort;
        }

    public void setMongoDBPrimaryPort(int mongoDBPrimaryPort) {
            this.mongoDBPrimaryPort = mongoDBPrimaryPort;
        }

    public String getMongoDBSecondIP() {
            return mongoDBSecondIP;
        }

    public void setMongoDBSecondIP(String mongoDBSecondIP) {
            this.mongoDBSecondIP = mongoDBSecondIP;
        }

    public int getMongoDBSecondPort() {
            return mongoDBSecondPort;
        }

    public void setMongoDBSecondPort(int mongoDBSecondPort) {
            this.mongoDBSecondPort = mongoDBSecondPort;
        }

    public String getMongoDBThirdIP() {
            return mongoDBThirdIP;
        }

    public void setMongoDBThirdIP(String mongoDBThirdIP) {
            this.mongoDBThirdIP = mongoDBThirdIP;
        }

    public int getMongoDBThirdPort() {
            return mongoDBThirdPort;
        }

    public void setMongoDBThirdPort(int mongoDBThirdPort) {
            this.mongoDBThirdPort = mongoDBThirdPort;
        }

    public String getMongoDBUsername() {
            return mongoDBUsername;
        }

    public void setMongoDBUsername(String mongoDBUsername) {
            this.mongoDBUsername = mongoDBUsername;
        }

    public String getMongoDBPassword() {
            return mongoDBPassword;
        }

    public void setMongoDBPassword(String mongoDBPassword) {
            this.mongoDBPassword = mongoDBPassword;
        }

    public String getMongoDBName() {
            return mongoDBName;
        }

    public void setMongoDBName(String mongoDBName) {
            this.mongoDBName = mongoDBName;
        }

    public String getNeo4jIP() {
            return neo4jIP;
        }

    public void setNeo4jIP(String neo4jIP) {
            this.neo4jIP = neo4jIP;
        }

    public int getNeo4jPort() {
            return neo4jPort;
        }

    public void setNeo4jPort(int neo4jPort) {
            this.neo4jPort = neo4jPort;
        }

    public String getNeo4jUsername() {
            return neo4jUsername;
        }

    public void setNeo4jUsername(String neo4jUsername) {
            this.neo4jUsername = neo4jUsername;
        }

    public String getNeo4jPassword() {
            return neo4jPassword;
        }

    public void setNeo4jPassword(String neo4jPassword) {
            this.neo4jPassword = neo4jPassword;
        }

    public int getLimitNumber() {
        return limitNumber;
    }

    public void setLimitNumber(int limitNumber) {
        this.limitNumber = limitNumber;
    }
}
