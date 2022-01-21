package it.unipi.dii.inginf.lsdb.learnitapp.persistence;

import it.unipi.dii.inginf.lsdb.learnitapp.config.ConfigParams;
import it.unipi.dii.inginf.lsdb.learnitapp.utils.Utils;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4jDriver implements DBDriver {
    private static Neo4jDriver instance = null;
//Branch
    private Driver neo4jDriver;
    private String neo4jIP;
    private int neo4jPort;
    private String neo4jUsername;
    private String neo4jPassword;

    private Neo4jDriver(ConfigParams configParameters)
    {
        this.neo4jIP = configParameters.getNeo4jIP();
        this.neo4jPort = configParameters.getNeo4jPort();
        this.neo4jUsername = configParameters.getNeo4jUsername();
        this.neo4jPassword = configParameters.getNeo4jPassword();
    }

    public static Neo4jDriver getInstance()
    {
        if (instance == null)
        {
            instance = new Neo4jDriver(Utils.getParams());
        }
        return instance;
    }

    /**
     * Method that inits the Driver
     */
    @Override
    public boolean initConnection()
    {
        try
        {
            neo4jDriver = GraphDatabase.driver("neo4j://" + neo4jIP + ":" + neo4jPort,
                    AuthTokens.basic(neo4jUsername, neo4jPassword
                    ));
            neo4jDriver.verifyConnectivity();
        } catch (Exception e)
        {
            System.out.println("Neo4J server is not available");
            return false;
        }
        return true;
    }

    /**
     * Method for closing the connection of the Driver
     */
    @Override
    public void closeConnection ()
    {
        if (neo4jDriver != null)
            neo4jDriver.close();
    }
}
