package au.edu.unimelb.mc.trippal.backend;

import com.google.android.gms.maps.model.LatLng;
import com.microsoft.azure.storage.table.TableServiceEntity;

import java.util.Date;
import java.util.UUID;

/**
 * Domain model that represents a single trip of a user.
 */
public class TripEntity extends TableServiceEntity {

    private String destinationName;
    private double startLocationLat;
    private double startLocationLng;
    private double endLocationLng;
    private double endLocationLat;
    private Date tripDate;
    private String duration;
    private String distance;

    public TripEntity(String userId) {
        this.partitionKey = userId;
        this.rowKey = UUID.randomUUID().toString();
    }

    // Default constructor needed for Azure
    public TripEntity() {
    }

    public void setStartLocation(LatLng location) {
        startLocationLat = location.latitude;
        startLocationLng = location.longitude;
    }

    public void setEndLocation(LatLng location) {
        endLocationLat = location.latitude;
        endLocationLng = location.longitude;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public Date getTripDate() {
        return tripDate;
    }

    public void setTripDate(Date tripDate) {
        this.tripDate = tripDate;
    }

    public double getStartLocationLat() {
        return startLocationLat;
    }

    public void setStartLocationLat(double startLocationLat) {
        this.startLocationLat = startLocationLat;
    }

    public double getStartLocationLng() {
        return startLocationLng;
    }

    public void setStartLocationLng(double startLocationLng) {
        this.startLocationLng = startLocationLng;
    }

    public double getEndLocationLng() {
        return endLocationLng;
    }

    public void setEndLocationLng(double endLocationLng) {
        this.endLocationLng = endLocationLng;
    }

    public double getEndLocationLat() {
        return endLocationLat;
    }

    public void setEndLocationLat(double endLocationLat) {
        this.endLocationLat = endLocationLat;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
