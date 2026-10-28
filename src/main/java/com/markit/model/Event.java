package com.markit.model;

public class Event {
    private String id;
    private String name;
    private String venue;
    private String pin;
    private int duration;
    private long createdAt;
    private long expiresAt;

    public Event() {}

    public Event(String id, String name, String venue, String pin, int duration, long createdAt, long expiresAt) {
        this.id = id;
        this.name = name;
        this.venue = venue;
        this.pin = pin;
        this.duration = duration;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
        private long pinExpiresAt;

    public long getPinExpiresAt() { return pinExpiresAt; }
    public void setPinExpiresAt(long pinExpiresAt) { this.pinExpiresAt = pinExpiresAt; }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
}