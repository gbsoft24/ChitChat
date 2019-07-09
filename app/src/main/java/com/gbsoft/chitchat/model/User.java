package com.gbsoft.chitchat.model;

/**
 * Created by chiranjeevi on 2/16/18.
 */

public class User {
    private String userName;
    private String FullName;
    private String passWrd;
    private String email;
    private String usrId;

    public User() {
    }

    public User(String userName, String fullName, String passWrd, String email, String usrId) {
        this.userName = userName;
        FullName = fullName;
        this.passWrd = passWrd;
        this.email = email;
        this.usrId = usrId;
    }

    public String getUsrId() {
        return usrId;
    }

    public void setUsrId(String usrId) {
        this.usrId = usrId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getPassWrd() {
        return passWrd;
    }

    public void setPassWrd(String passWrd) {
        this.passWrd = passWrd;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
