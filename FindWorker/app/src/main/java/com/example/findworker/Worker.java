package com.example.findworker;
import java.io.Serializable;

public class Worker implements Serializable {
    private String id;
    private String name;
    private String phone;
    private String email;
    private String dob;
    private String pincode;
    private String location;
    private String profession;

    private boolean isFavorite;
    private double latitude;
    private double longitude;
    private String joinDateTime;
    private double averageRating;
    private int ratingCount;
    private double distanceFromUser;


    private String nameTranslated;
    private String professionTranslated;
    private String locationTranslated;

    public Worker() {
        // Required for Firebase
    }

    public Worker(String id, String name,String dob ,String phone, String pincode, String location, String profession,double latitude, double longitude,String joinDateTime, double averageRating, int ratingCount, String email) {
        this.id = id;
        this.name = name;
        this.dob = dob;
        this.phone = phone;
        this.pincode = pincode;
        this.location = location;
        this.profession = profession;
        this.latitude = latitude;
        this.longitude = longitude;
        this.joinDateTime = joinDateTime;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
        this.email = email;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDob() { return dob; }
    public String getPhone() { return phone; }
    public String getPincode() { return pincode; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { this.isFavorite = favorite; }
    public double getLatitude() {
        return latitude;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getDistanceFromUser() {
        return distanceFromUser;
    }

    public void setDistanceFromUser(double distanceFromUser) {
        this.distanceFromUser = distanceFromUser;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getJoinDateTime() {
        return joinDateTime;
    }

    public void setJoinDateTime(String joinDateTime) {
        this.joinDateTime = joinDateTime;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }


    public String getNameTranslated() { return nameTranslated != null ? nameTranslated : name; }
    public void setNameTranslated(String nameTranslated) { this.nameTranslated = nameTranslated; }

    public String getProfessionTranslated() { return professionTranslated != null ? professionTranslated : profession; }
    public void setProfessionTranslated(String professionTranslated) { this.professionTranslated = professionTranslated; }

    public String getLocationTranslated() { return locationTranslated != null ? locationTranslated : location; }
    public void setLocationTranslated(String locationTranslated) { this.locationTranslated = locationTranslated; }

}
