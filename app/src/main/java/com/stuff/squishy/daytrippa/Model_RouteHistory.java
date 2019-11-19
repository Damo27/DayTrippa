package com.stuff.squishy.daytrippa;


import android.location.Address;

import com.mapbox.api.geocoding.v5.models.CarmenFeature;

public class Model_RouteHistory
{

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    private String origin;
    private String destination;
    private Double distance;
    private Double duration;


}
