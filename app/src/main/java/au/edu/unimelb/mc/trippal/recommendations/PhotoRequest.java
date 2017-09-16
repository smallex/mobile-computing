package au.edu.unimelb.mc.trippal.recommendations;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by alexandrafritzen on 16/09/2017.
 */

public class PhotoRequest {
    @JsonProperty("key")
    private String key;

    @JsonProperty("photoreference")
    private String photoReference;

    @JsonProperty("maxwidth")
    private int maxWidth;

    @JsonProperty("maxheight")
    private int maxHeight;

    public PhotoRequest(String key, String photoReference, int maxWidth) {
        this.key = key;
        this.photoReference = photoReference;
        this.maxWidth = maxWidth;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

}
