module it.unipi.dii.inginf.lsdb.learnitapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.driver.core;
    requires java.xml;
    requires xstream;
    requires gson;
    requires org.mongodb.bson;
    requires org.neo4j.driver;
    requires log4j;
    requires java.sql;

    opens it.unipi.dii.inginf.lsdb.learnitapp.config to xstream;
    opens it.unipi.dii.inginf.lsdb.learnitapp.app to javafx.fxml;
    opens it.unipi.dii.inginf.lsdb.learnitapp.controller to javafx.fxml;
    exports it.unipi.dii.inginf.lsdb.learnitapp.model to org.mongodb.bson;
    exports it.unipi.dii.inginf.lsdb.learnitapp.app;
    exports it.unipi.dii.inginf.lsdb.learnitapp.controller;
}