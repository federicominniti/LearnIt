package it.unipi.dii.inginf.lsdb.learnitapp.model;

public class Session {
    private static Session localSession = null; // Singleton
    private User loggedUser;

    public static Session getLocalSession()
    {
        if(localSession == null) {
            localSession = new Session();
        }
        return localSession;
    }

    private Session() {}

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }

    public User getLoggedUser() {
        return loggedUser;
    }

    public static void destroySession(){
        localSession =null;
    }
}
