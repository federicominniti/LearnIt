package it.unipi.dii.inginf.lsdb.learnitapp.utils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.controller.CoursePageController;
import it.unipi.dii.inginf.lsdb.learnitapp.controller.CourseSnapshotController;
import it.unipi.dii.inginf.lsdb.learnitapp.controller.ElementsLineController;
import it.unipi.dii.inginf.lsdb.learnitapp.controller.UserSnapshotController;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Course;
import it.unipi.dii.inginf.lsdb.learnitapp.model.Session;
import it.unipi.dii.inginf.lsdb.learnitapp.model.User;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

public class Utils {

    public final static int LIKED_COURSES = 4;
    public final static int REVIEWED_COURSES = 5;
    public final static int OFFERED_COURSES = 6;
    public final static int FOLLOWER_USERS = 7;
    public final static int FOLLOWING_USERS = 8;
    public final static int FRIENDS_COMPLETED_LIKED = 1;
    public final static int INSTRUCTORS_SUGGESTIONS = 2;
    public final static int USER_SUGGESTIONS = 3;
    public final static int BEST_RATING = -1;
    public final static int TRENDING_COURSE = -2;

    public final static String[] LANGUAGES = {"Arabic", "Chinese", "English", "French", "German", "Hebrew", "Hungarian", "Indonesian",
            "Italian", "Japanese", "Korean", "Portuguese", "Russian", "Spanish", "Swedish", "Turkish", "Ukrainian", "Albanian",
            "Azeri", "Bengali", "Bulgarian", "Burmese", "Croatian", "Czech", "Greek", "Hindi", "Estonian", "Filipino",
            "Indonesia", "Lithuanian", "Malay", "Marathi", "Nederlands", "Norwegian", "Persian", "Polski", "Română", "Serbian",
            "Swahili", "Tamil", "Telugu", "Urdu", "Vietnamese", "русский", "ไทย", "日本語", "简体中文", "繁體中文", "한국어"};

    public final static String[] LEVELS  = {"All Levels", "Beginner", "Expert", "Intermediate"};

    public static final String READ_MORE = "/img/readMore.png";
    public static final String STAR_ON = "/img/star-on.png";
    public static final String STAR_OFF = "/img/star-off.png";
    public static final String TRASH_BIN = "/img/trash-bin.png";
    public static final String USER_DEFAULT = "/img/userDefault.png";
    public static final String LOGIN_PAGE = "/fxml/LoginPage.fxml";
    public final static String DISCOVERY_PAGE = "/fxml/DiscoveryPage.fxml";
    public final static String REGISTRATION_PAGE = "/fxml/RegistrationPage.fxml";
    public final static String PROFILE_PAGE = "/fxml/ProfilePage.fxml";
    public final static String COURSE_PAGE = "/fxml/CoursePage.fxml";
    public final static String REVIEW_SNAPSHOT = "/fxml/ReviewSnapshotPage.fxml";
    public final static String USER_SNAPSHOT = "/fxml/UserSnapshot.fxml";

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
            System.out.println("problem con config.xml");
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

    public static boolean isPasswordSecure(String password){
        Pattern pattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$");
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public static Object changeScene (String fileName, Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(Utils.class.getResource(fileName));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.show();
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void showErrorAlert (String text) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setContentText(text);
        errorAlert.setHeaderText("Please click 'OK' and try again");
        errorAlert.setTitle("Error...");
        Image errorImage = new Image(String.valueOf(Utils.class.getResource("/img/error.png")));
        ImageView errorImageView = new ImageView(errorImage);
        errorImageView.setFitHeight(70);
        errorImageView.setFitWidth(70);
        errorAlert.setGraphic(errorImageView);
        errorAlert.show();
    }

    public static void showInfoAlert (String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(text);
        alert.setHeaderText("Confirm Message");
        alert.setTitle("Information");
        ImageView imageView = new ImageView(
                new Image(String.valueOf(Utils.class.getResource("/img/success.png"))));
        imageView.setFitHeight(60);
        imageView.setFitWidth(60);
        imageView.setPreserveRatio(true);
        alert.setGraphic(imageView);
        alert.show();
    }

    public static Pane loadCourseSnapshot(Course course) {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(Utils.class.getResource("/fxml/CourseSnapshot.fxml"));
            pane = (Pane) loader.load();
            CourseSnapshotController courseSnapshotController = (CourseSnapshotController) loader.getController();
            courseSnapshotController.setSnapshotCourse(course);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pane;
    }

    public static void addCoursesSnapshot(HBox courseHBox, List<Course> courses) {
        for(int i = 0; i<courses.size(); i++) {
            Pane coursePane = loadCourseSnapshot(courses.get(i));
            courseHBox.getChildren().add(coursePane);
        }
    }

    public static void addLine(VBox discoverySections, Course course, User user, int type) {
        Pane line = loadElementsLine(course, user, type);
        discoverySections.getChildren().add(line);
    }

    private static Pane loadElementsLine(Course course, User user, int type) {
        //coursesUsersLine
        // 0 -> courses
        // 1 -> users
        //buttonLoadMore
        // 0 -> no button
        // 1 -> yes button
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(Utils.class.getResource("/fxml/ElementsLine.fxml"));
            pane = (Pane) loader.load();
            /*if(type != USER_SUGGESTIONS) {
                ElementsLineController<Course> coursesLine = (ElementsLineController<Course>) loader.getController();
                coursesLine.setCoursesUsers(course, type);
            }
            else {
                ElementsLineController<User> coursesLine = (ElementsLineController<User>) loader.getController();
                coursesLine.setCoursesUsers(user, type);
            }*/
            ElementsLineController coursesLine = (ElementsLineController) loader.getController();
            coursesLine.setCoursesUsers(course, user, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pane;
    }

    public static Pane loadUserSnapshot (User user)
    {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(Utils.class.getResource(USER_SNAPSHOT));
            pane = (Pane) loader.load();
            UserSnapshotController userSnapshotController = (UserSnapshotController) loader.getController();
            userSnapshotController.setSnapshotUser(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pane;
    }

    public static void addUsersSnapshot(HBox usersHBox, List<User> users) {
        for(int i = 0; i<users.size(); i++) {
            Pane coursePane = loadUserSnapshot(users.get(i));
            usersHBox.getChildren().add(coursePane);
        }
    }

    public static void fillStars(int rating, HBox ratingHBox){
        for(Node star: ratingHBox.getChildren()){
            int index = ratingHBox.getChildren().indexOf(star);
            if(index > rating - 1)
                break;
            ImageView starImageView = (ImageView)star;
            Image starImage = new Image(
                    String.valueOf(CoursePageController.class.getResource(STAR_ON)));
            starImageView.setImage(starImage);
            ratingHBox.getChildren().set(index, (Node)starImageView);
        }

        for(int index=rating; index <5; index++){
            Node star = ratingHBox.getChildren().get(index);
            ImageView starImageView = (ImageView)star;
            Image starImage = new Image(
                    String.valueOf(CoursePageController.class.getResource(STAR_OFF)));
            starImageView.setImage(starImage);
            ratingHBox.getChildren().set(index, (Node)starImageView);
        }
    }

    public static void  logout(MouseEvent clickEvent){
        Session.destroySession();
        changeScene(LOGIN_PAGE, clickEvent);

    }
}
