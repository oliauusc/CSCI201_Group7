package com.foodlocator.model;

/**
 * Location model class representing a food location on campus
 */
public class Location {
    private int locationID;
    private String name;
    private String address;
    private String category;
    private double lat;
    private double lng;
    private double rating;
    private String description;
    private String[] tags;
    private double distance;
    private int reviewCount;
    
    // Constructors
    public Location() {}
    
    public Location(int locationID, String name, String address, String category) {
        this.locationID = locationID;
        this.name = name;
        this.address = address;
        this.category = category;
    }
    
    public Location(String name, String address, String category) {
        this.name = name;
        this.address = address;
        this.category = category;
    }
    
    public Location(int locationID, String name, String address, String category, double lat, double lng) {
        this.locationID = locationID;
        this.name = name;
        this.address = address;
        this.category = category;
        this.lat = lat;
        this.lng = lng;
    }
    
    // Getters and Setters
    public int getLocationID() {
        return locationID;
    }
    
    public void setLocationID(int locationID) {
        this.locationID = locationID;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
    
    public int getReviewCount() {
        return reviewCount;
    }
    
    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    @Override
    public String toString() {
        return "Location{" +
                "locationID=" + locationID +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", category='" + category + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}
