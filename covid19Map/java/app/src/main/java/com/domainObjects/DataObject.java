package com.domainObjects;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;


public class DataObject {
    String covid;
    String healthy;
    Symptom flu;
    long timestamp;
    LatLng location;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getCovid() {
        return covid;
    }

    public void setCovid(String covid) {
        this.covid = covid;
    }

    public String getHealthy() {
        return healthy;
    }

    public void setHealthy(String healthy) {
        this.healthy = healthy;
    }

    public Symptom getFlu() {
        return flu;
    }

    public void setFlu(Symptom flu) {
        this.flu = flu;
    }
}
