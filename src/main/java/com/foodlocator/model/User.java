package com.foodlocator.model;

/**
 * User model class representing a user in the system
 */
public class User {
    private int userID;
    private String email;
    private String name;
    private String password;
    
    // Constructors
    public User() {}
    
    public User(int userID, String email, String name, String password) {
        this.userID = userID;
        this.email = email;
        this.name = name;
        this.password = password;
    }
    
    public User(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
    }
    
    // Getters and Setters
    public int getUserID() {
        return userID;
    }
    
    public void setUserID(int userID) {
        this.userID = userID;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userID=" + userID +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
