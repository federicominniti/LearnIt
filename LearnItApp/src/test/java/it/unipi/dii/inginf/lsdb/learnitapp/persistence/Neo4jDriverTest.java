package it.unipi.dii.inginf.lsdb.learnitapp.persistence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Neo4jDriverTest {
    @Test
    void getInstance() {
        Neo4jDriver instance = Neo4jDriver.getInstance();
        Assertions.assertEquals(instance, Neo4jDriver.getInstance());
    }

    @Test
    void closeConnection() {
        // Call close connection before having starting it doesn't throw exceptions
        assertDoesNotThrow(() -> {
            Neo4jDriver.getInstance().closeConnection();
        });
    }
}