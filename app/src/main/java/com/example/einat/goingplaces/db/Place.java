package com.example.einat.goingplaces.db;

/**
 * Created by Einat on 15/07/2015.
 */
public class Place {

    private String name, address;
    private Double dist, lat, lon;

    public Place(String name, String address, Double dist, Double lat, Double lon) {
        this.name = name;
        this.address = address;
        this.dist = dist;
        this.lat = lat;
        this.lon = lon;
    }

    public Place(String name, String address, Double lat, Double lon) {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public String toString() {
        return "Place{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", dist=" + dist +
                '}';
    }


    public String allToString() {
        return "Place{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", dist=" + dist +
                ", lat=" + lat +
                ", lon=" + lon +
                '}';
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

    public Double getDist() {
        return dist;
    }

    public void setDist(Double dist) {
        this.dist = dist;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }
}
