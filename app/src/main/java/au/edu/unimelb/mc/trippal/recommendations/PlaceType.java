package au.edu.unimelb.mc.trippal.recommendations;

/**
 * Created by alexandrafritzen on 14/09/2017.
 */

enum PlaceType {
    BAKERY("bakery"),
    CAFE("cafe"),
    GAS_STATION("gas_station"),
    LODGING("lodging"),
    MEAL_TAKEAWAY("meal_takeaway"),
    PARK("park"),
    RESTAURANT("restaurant");

    private final String name;

    PlaceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}