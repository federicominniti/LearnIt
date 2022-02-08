package it.unipi.dii.inginf.lsdb.databasemaintenance;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
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

import static java.lang.Thread.sleep;

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

    public String getMongoDBCollectionCourses() {
        return mongoDBCollectionCourses;
    }

    public void setMongoDBCollectionCourses(String mongoDBCollectionCourses) {
        this.mongoDBCollectionCourses = mongoDBCollectionCourses;
    }

    private int maxReviews;
    private int maxReviewsAfterDiscard;

    public int getMaxReviews() {
        return maxReviews;
    }

    public void setMaxReviews(int maxReviews) {
        this.maxReviews = maxReviews;
    }

    public int getMaxReviewsAfterDiscard() {
        return maxReviewsAfterDiscard;
    }

    public void setMaxReviewsAfterDiscard(int maxReviewsAfterDiscard) {
        this.maxReviewsAfterDiscard = maxReviewsAfterDiscard;
    }

    public static ConfigParams getInstance() throws IOException {
        if (localConfig == null) {
            synchronized (ConfigParams.class) {
                if (localConfig == null) {
                    localConfig = getParams();
                }
            }
        }
        return localConfig;
    }

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
            System.out.println("problema con config.xml");
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(1);
        }

        return null;
    }

    private static boolean validConfigParams() {
        Document document;
        try {
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
}

