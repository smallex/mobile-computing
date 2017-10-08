package au.edu.unimelb.mc.trippal.recommendations;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by alexandrafritzen on 9/09/2017.
 */

@SuppressWarnings("unused")
class PlaceResponse {

    @JsonProperty("html_attributions")
    private ArrayList<String> attributions;

    @JsonProperty("next_page_token")
    private String nextPageToken;

    @JsonProperty("results")
    private ArrayList<Place> results;

    @JsonProperty("radius")
    private int radius;

    @JsonProperty("status")
    private String status;

    @JsonProperty("error_message")
    private String errorMessage;

    ArrayList<String> getAttributions() {
        return attributions;
    }

    void setAttributions(ArrayList<String> attributions) {
        this.attributions = attributions;
    }

    String getNextPageToken() {
        return nextPageToken;
    }

    void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    ArrayList<Place> getResults() {
        return results;
    }

    void setResults(ArrayList<Place> results) {
        this.results = results;
    }

    int getRadius() {
        return radius;
    }

    void setRadius(int radius) {
        this.radius = radius;
    }

    String getStatus() {
        return status;
    }

    void setStatus(String status) {
        this.status = status;
    }

    String getErrorMessage() {
        return errorMessage;
    }

    void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
