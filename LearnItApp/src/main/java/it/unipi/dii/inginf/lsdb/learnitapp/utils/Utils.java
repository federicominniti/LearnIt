package it.unipi.dii.inginf.lsdb.learnitapp.utils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.Thread.sleep;

public class Utils {

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
            //Utils.showAlert("Problem with the configuration file!");
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(1);
        }

        return null;
    }

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
}
