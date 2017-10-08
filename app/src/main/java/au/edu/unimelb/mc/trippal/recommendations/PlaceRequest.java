package au.edu.unimelb.mc.trippal.recommendations;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by alexandrafritzen on 9/09/2017.
 */

@SuppressWarnings("unused")
class PlaceRequest {
    @JsonProperty("key")
    private String key;

    @JsonProperty("location")
    private String location;

    @JsonProperty("radius")
    private int radius;

    //https://developers.google.com/places/web-service/supported_types
    @JsonProperty("type")
    private String type;

    PlaceRequest(String key, double latitude, double longitude, int radius, String type) {
        this.key = key;
        this.location = latitude + "," + longitude;
        this.radius = radius;
        this.type = type;
    }

    String getKey() {
        return key;
    }

    void setKey(String key) {
        this.key = key;
    }

    String getLocation() {
        return location;
    }

    void setLocation(String location) {
        this.location = location;
    }

    int getRadius() {
        return radius;
    }

    void setRadius(int radius) {
        this.radius = radius;
    }

    String getType() {
        return type;
    }

    void setType(String type) {
        this.type = type;
    }
}
