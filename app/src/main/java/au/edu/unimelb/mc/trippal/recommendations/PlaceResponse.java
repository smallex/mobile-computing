package au.edu.unimelb.mc.trippal.recommendations;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by alexandrafritzen on 9/09/2017.
 */

public class PlaceResponse {

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

    public ArrayList<String> getAttributions() {
        return attributions;
    }

    public void setAttributions(ArrayList<String> attributions) {
        this.attributions = attributions;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public ArrayList<Place> getResults() {
        return results;
    }

    public void setResults(ArrayList<Place> results) {
        this.results = results;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
