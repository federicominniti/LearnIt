package it.unipi.dii.inginf.lsdb.learnitapp.model;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SessionTest {

    @Test
    void getLocalSession() {
        Session instance = Session.getLocalSession();
        Assertions.assertEquals(instance, Session.getLocalSession());
    }
}