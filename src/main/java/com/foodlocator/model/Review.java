package com.foodlocator.model;

import java.sql.Timestamp;

/**
 * Review model class representing a user review for a location
 */
public class Review {
    private int reviewID;
    private int locationID;
    private int userID;
    private double rating;
    private String title;
    private String body;
    private Timestamp createdAt;
    
    // Constructors
    public Review() {}
    
    public Review(int reviewID, int locationID, int userID, double rating, 
                  String title, String body, Timestamp createdAt) {
        this.reviewID = reviewID;
        this.locationID = locationID;
        this.userID = userID;
        this.rating = rating;
        this.title = title;
        this.body = body;
        this.createdAt = createdAt;
    }
    
    public Review(int locationID, int userID, double rating, String title, String body) {
        this.locationID = locationID;
        this.userID = userID;
        this.rating = rating;
        this.title = title;
        this.body = body;
    }
    
    // Getters and Setters
    public int getReviewID() {
        return reviewID;
    }
    
    public void setReviewID(int reviewID) {
        this.reviewID = reviewID;
    }
    
    public int getLocationID() {
        return locationID;
    }
    
    public void setLocationID(int locationID) {
        this.locationID = locationID;
    }
    
    public int getUserID() {
        return userID;
    }
    
    public void setUserID(int userID) {
        this.userID = userID;
    }
    
    public double getRating() {
        return rating;
    }
    
    public void setRating(double rating) {
        this.rating = rating;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "Review{" +
                "reviewID=" + reviewID +
                ", locationID=" + locationID +
                ", userID=" + userID +
                ", rating=" + rating +
                ", title='" + title + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    private String locationName; // Add this field

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}
