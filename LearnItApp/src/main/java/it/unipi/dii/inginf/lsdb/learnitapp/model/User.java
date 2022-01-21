package it.unipi.dii.inginf.lsdb.learnitapp.model;

import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Date;

public class User {
    @BsonProperty(value = "username")
    private String username;
    @BsonProperty(value = "password")
    private String password;
    @BsonProperty(value = "complete_name")
    private String completeName;
    @BsonProperty(value = "date_of_birth")
    private Date dateOfBirth;
    @BsonProperty(value = "gender")
    private String gender;
    @BsonProperty(value = "email")
    private String email;
    @BsonProperty(value = "role")
    private Role role;
    @BsonProperty(value = "profile_picture")
    private String profilePic;

    public User() {

    }
    public User(String username, String password, String completeName, Date dateOfBirth, String gender, String email, Role role, String profilePic) {
        this.username = username;
        this.password = password;
        this.completeName = completeName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.email = email;
        this.role = role;
        this.profilePic = profilePic;
    }

    public User(String username, String completeName) {
        this.username = username;
        this.completeName = completeName;
    }

    public enum Role {
        ADMINISTRATOR,
        STANDARD;

        public static Role fromInteger(int x) {
            switch(x) {
                case 0:
                    return STANDARD;
                case 1:
                    return ADMINISTRATOR;
            }
            return null;
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCompleteName() {
        return completeName;
    }

    public void setCompleteName(String completeName) {
        this.completeName = completeName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}
