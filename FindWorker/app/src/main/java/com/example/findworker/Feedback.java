package com.example.findworker;

public class Feedback {

    public String userName;
    public float rating;
    public String feedback;
    public long time;

    public Feedback() {} // required for Firebase

    public Feedback(String userName, float rating, String feedback, long time) {
        this.userName = userName;
        this.rating = rating;
        this.feedback = feedback;
        this.time = time;
    }
}
