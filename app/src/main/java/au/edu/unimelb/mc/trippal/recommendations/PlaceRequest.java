package au.edu.unimelb.mc.trippal.recommendations;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by alexandrafritzen on 9/09/2017.
 */

public class PlaceRequest {
    @JsonProperty("key")
    private String key;

    @JsonProperty("location")
    private String location;

    @JsonProperty("radius")
    private int radius;

    //https://developers.google.com/places/web-service/supported_types
    @JsonProperty("type")
    private String type;

    public PlaceRequest(String key, double latitude, double longitude, int radius, String type) {
        this.key = key;
        this.location = latitude + "," + longitude;
        this.radius = radius;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
