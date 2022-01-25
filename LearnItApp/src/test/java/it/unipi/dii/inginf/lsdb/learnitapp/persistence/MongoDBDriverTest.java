package it.unipi.dii.inginf.lsdb.learnitapp.persistence;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MongoDBDriverTest {

    @Test
    void getInstance() {
        MongoDBDriver instance = MongoDBDriver.getInstance();
        assertEquals(instance, MongoDBDriver.getInstance());
    }

    @Test
    void closeConnection() {
        // Call close connection before having starting it doesn't throw exceptions
        assertDoesNotThrow(() -> {
            MongoDBDriver.getInstance().closeConnection();
        });
    }
}
