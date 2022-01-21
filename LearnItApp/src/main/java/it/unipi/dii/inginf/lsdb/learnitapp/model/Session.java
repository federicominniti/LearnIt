package it.unipi.dii.inginf.lsdb.learnitapp.model;

public class Session {
    private static Session localSession = null; // Singleton
    private User loggedUser;

    public static Session getLocalSession()
    {
        if(localSession == null)
            localSession = new Session();
        return localSession;
    }

    private Session () {}

    public static void setLoggedUser(User loggedUser) {
        localSession.loggedUser = loggedUser;
    }

    public void updateLoggedUserInfo(User user) {
        localSession.loggedUser = user;
    }

    public User getLoggedUser() {
        return loggedUser;
    }
}
