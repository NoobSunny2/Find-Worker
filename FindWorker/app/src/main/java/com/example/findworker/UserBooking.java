package com.example.findworker;

public class UserBooking {
    private String requestId;
    private String workerId;
    private String workerName;
    private String status;
    private long timestamp;
    private double userLat;
    private double userLng;
    private String startOtp;
    private String completeOtp;

    public UserBooking() {}

    public UserBooking(String requestId, String workerId, String workerName, String status, long timestamp, double userLat, double userLng) {
        this.requestId = requestId;
        this.workerId = workerId;
        this.workerName = workerName;
        this.status = status;
        this.timestamp = timestamp;
        this.userLat = userLat;
        this.userLng = userLng;
        this.startOtp = startOtp;
        this.completeOtp = completeOtp;
    }

    public String getRequestId() { return requestId; }
    public String getWorkerId() { return workerId; }
    public String getWorkerName() { return workerName; }
    public String getStatus() { return status; }
    public long getTimestamp() { return timestamp; }
    public double getUserLat() { return userLat; }
    public double getUserLng() { return userLng; }
    public String getStartOtp() { return startOtp; }
    public String getCompleteOtp() { return completeOtp; }
}
