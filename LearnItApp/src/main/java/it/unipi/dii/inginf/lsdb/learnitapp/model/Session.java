package it.unipi.dii.inginf.lsdb.learnitapp.model;

public class Session {
    private static Session localSession = null; // Singleton
    private User2 loggedUser;

    public static Session getLocalSession()
    {
        if(localSession == null) {
            System.out.println("qui");
            localSession = new Session();
        }
        return localSession;
    }

    private Session() {}

    public void setLoggedUser(User2 loggedUser) {
        this.loggedUser = loggedUser;
    }

    public User2 getLoggedUser() {
        return loggedUser;
    }

    public static void destroySession(){
        localSession =null;
    }
}
