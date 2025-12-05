package com.foodlocator.model;

/**
 * Location model class representing a food location on campus
 */
public class Location {
    private int locationID;
    private String name;
    private String address;
    private String category;
    
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
    
    @Override
    public String toString() {
        return "Location{" +
                "locationID=" + locationID +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
