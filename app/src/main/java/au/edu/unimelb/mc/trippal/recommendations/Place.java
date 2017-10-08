package au.edu.unimelb.mc.trippal.recommendations;

import android.graphics.Bitmap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by alexandrafritzen on 9/09/2017.
 */

@SuppressWarnings("unused")
class Place {
    @JsonProperty("geometry")
    private Geometry geometry;

    private int distance;

    @JsonProperty("icon")
    private String icon;

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("opening_hours")
    private OpeningHours openingHours;

    @JsonProperty("photos")
    private ArrayList<Photo> photos;

    private Bitmap image;

    @JsonProperty("place_id")
    private String placeId;

    @JsonProperty("price_level")
    private int priceLevel;

    @JsonProperty("rating")
    private double rating;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("alt_ids")
    private ArrayList<AltId> altIds;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("types")
    private ArrayList<String> types;

    @JsonProperty("vicinity")
    private String vicinity;

    Geometry getGeometry() {
        return geometry;
    }

    void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    int getDistance() {
        return distance;
    }

    void setDistance(int distance) {
        this.distance = distance;
    }

    String getIcon() {
        return icon;
    }

    void setIcon(String icon) {
        this.icon = icon;
    }

    String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    OpeningHours getOpeningHours() {
        return openingHours;
    }

    void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }

    ArrayList<Photo> getPhotos() {
        return photos;
    }

    void setPhotos(ArrayList<Photo> photos) {
        this.photos = photos;
    }

    Bitmap getImage() {
        return image;
    }

    void setImage(Bitmap image) {
        this.image = image;
    }

    String getPlaceId() {
        return placeId;
    }

    void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    int getPriceLevel() {
        return priceLevel;
    }

    void setPriceLevel(int priceLevel) {
        this.priceLevel = priceLevel;
    }

    double getRating() {
        return rating;
    }

    void setRating(double rating) {
        this.rating = rating;
    }

    String getScope() {
        return scope;
    }

    void setScope(String scope) {
        this.scope = scope;
    }

    ArrayList<AltId> getAltIds() {
        return altIds;
    }

    void setAltIds(ArrayList<AltId> altIds) {
        this.altIds = altIds;
    }

    String getReference() {
        return reference;
    }

    void setReference(String reference) {
        this.reference = reference;
    }

    ArrayList<String> getTypes() {
        return types;
    }

    void setTypes(ArrayList<String> types) {
        this.types = types;
    }

    String getVicinity() {
        return vicinity;
    }

    void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Place))
            return false;
        if (obj == this)
            return true;

        Place place = (Place) obj;
        return place.getId().equals(this.getId());
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id.hashCode();
        return result;
    }
}

@JsonIgnoreProperties(value = {"viewport"})
class Geometry {
    @JsonProperty("location")
    private Location location;

    Location getLocation() {
        return location;
    }

    void setLocation(Location location) {
        this.location = location;
    }
}

@SuppressWarnings("unused")
class Location {
    @JsonProperty("lat")
    private double latitude;

    @JsonProperty("lng")
    private double longitude;

    double getLatitude() {
        return latitude;
    }

    void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    double getLongitude() {
        return longitude;
    }

    void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

@SuppressWarnings("unused")
@JsonIgnoreProperties(value = {"weekday_text"})
class OpeningHours {
    @JsonProperty("open_now")
    private boolean openNow;

    boolean isOpenNow() {
        return openNow;
    }

    void setOpenNow(boolean openNow) {
        this.openNow = openNow;
    }
}

@SuppressWarnings("unused")
class Photo {
    @JsonProperty("height")
    private int height;

    @JsonProperty("html_attributions")
    private ArrayList<String> htmlAttributions;

    @JsonProperty("photo_reference")
    private String photoReference;

    @JsonProperty("width")
    private int width;

    int getHeight() {
        return height;
    }

    void setHeight(int height) {
        this.height = height;
    }

    ArrayList<String> getHtmlAttributions() {
        return htmlAttributions;
    }

    void setHtmlAttributions(ArrayList<String> htmlAttributions) {
        this.htmlAttributions = htmlAttributions;
    }

    String getPhotoReference() {
        return photoReference;
    }

    void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }

    int getWidth() {
        return width;
    }

    void setWidth(int width) {
        this.width = width;
    }
}

@SuppressWarnings("unused")
class AltId {
    @JsonProperty("place_id")
    private String placeId;

    @JsonProperty("scope")
    private String scope;

    String getPlaceId() {
        return placeId;
    }

    void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    String getScope() {
        return scope;
    }

    void setScope(String scope) {
        this.scope = scope;
    }
}