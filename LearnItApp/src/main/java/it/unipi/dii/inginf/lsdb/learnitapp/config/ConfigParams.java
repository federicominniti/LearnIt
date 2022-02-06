package it.unipi.dii.inginf.lsdb.learnitapp.config;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
    private String mongoDBCollectionCourses;
    private String mongoDBCollectionUsers;

    private String neo4jIP;
    private int neo4jPort;
    private String neo4jUsername;
    private String neo4jPassword;

    private int limitNumber;
    private int limitFirstLvl;
    private int limitSecondLvl;
    private int numRelationships;
    private int numCommonCourses;
    private int followedThreshold;

    /**
     * Returns the instance of the local configuration parameters, creating it if needed
     * @return the ConfigParams instance
     */
    public static ConfigParams getInstance() {
        if (localConfig == null) {
            synchronized (ConfigParams.class) {
                if (localConfig == null) {
                    localConfig = getParams();
                }
            }
        }
    return localConfig;
    }

    /**
     * Exploits Xstream to easily read from the config.xml and load a ConfigParams instance
     * @return a ConfigParams object
     */
    public static ConfigParams getParams() {
        if (validConfigParams()) {
            XStream xstream = new XStream();
            xstream.addPermission(AnyTypePermission.ANY);
            xstream.addPermission(NullPermission.NULL);   // allow "null"
            xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
            String text = null;

            try {
                text = new String(Files.readAllBytes(Paths.get("config.xml")));
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

            return (ConfigParams) xstream.fromXML(text);

        } else {
            Utils.showErrorAlert("Problem with the configuration file!");
            System.exit(1);
        }

        return null;
    }

    /**
     * Validates the config.xml file against the config.xsd XML schema file
     */
    private static boolean validConfigParams()
    {
        Document document;
        try
        {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            document = documentBuilder.parse("config.xml");
            Schema schema = schemaFactory.newSchema(new StreamSource("config.xsd"));
            schema.newValidator().validate(new DOMSource(document));
        }
        catch (Exception e) {
            if (e instanceof SAXException)
                System.err.println("Validation Error: " + e.getMessage());
            else
                System.err.println(e.getMessage());

            return false;
        }
        return true;
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

    public String getMongoDBCollectionCourses() {
        return mongoDBCollectionCourses;
    }

    public void setMongoDBCollectionCourses(String mongoDBCollectionCourses) {
        this.mongoDBCollectionCourses = mongoDBCollectionCourses;
    }

    public String getMongoDBCollectionUsers() {
        return mongoDBCollectionUsers;
    }

    public void setMongoDBCollectionUsers(String mongoDBCollectionUsers) {
        this.mongoDBCollectionUsers = mongoDBCollectionUsers;
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

    public int getLimitFirstLvl() {
        return limitFirstLvl;
    }

    public void setLimitFirstLvl(int limitFirstLvl) {
        this.limitFirstLvl = limitFirstLvl;
    }

    public int getLimitSecondLvl() {
        return limitSecondLvl;
    }

    public void setLimitSecondLvl(int limitSecondLvl) {
        this.limitSecondLvl = limitSecondLvl;
    }

    public int getNumRelationships() {
        return numRelationships;
    }

    public void setNumRelationships(int numRelationships) {
        this.numRelationships = numRelationships;
    }

    public int getNumCommonCourses() {
        return numCommonCourses;
    }

    public void setNumCommonCourses(int numCommonCourses) {
        this.numCommonCourses = numCommonCourses;
    }

    public int getFollowedThreshold() {
        return followedThreshold;
    }

    public void setFollowedThreshold(int followedThreshold) {
        this.followedThreshold = followedThreshold;
    }
}
