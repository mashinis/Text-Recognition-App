package ru.mashinis.mlkit;

public class Milestone {
    private int kilometer;
    private double latitude;
    private double longitude;
    private long timestamp;
    private double altitude;

    public Milestone(int kilometer, double latitude, double longitude, long timestamp, double altitude) {
        this.kilometer = kilometer;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.altitude = altitude;
    }

    public int getKilometer() {
        return kilometer;
    }

    public void setKilometer(int kilometer) {
        this.kilometer = kilometer;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
}
