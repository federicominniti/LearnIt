package it.unipi.dii.inginf.lsdb.learnitapp.persistence;

public interface DBDriver {
    public boolean initConnection();
    public void closeConnection();
}
