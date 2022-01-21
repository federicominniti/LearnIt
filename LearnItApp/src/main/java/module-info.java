module it.unipi.dii.inginf.lsdb.learnitapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens it.unipi.dii.inginf.lsdb.learnitapp to javafx.fxml;
    exports it.unipi.dii.inginf.lsdb.learnitapp;
}