package au.edu.unimelb.mc.trippal.recommendations;

import au.edu.unimelb.mc.trippal.Constants;
import au.edu.unimelb.mc.trippal.R;

import static au.edu.unimelb.mc.trippal.Constants.mappingBathroom;
import static au.edu.unimelb.mc.trippal.Constants.mappingCoffee;
import static au.edu.unimelb.mc.trippal.Constants.mappingFood;
import static au.edu.unimelb.mc.trippal.Constants.mappingSleep;
import static au.edu.unimelb.mc.trippal.Constants.mappingStretchLegs;
import static au.edu.unimelb.mc.trippal.Constants.mappingSwitchDriver;

/**
 * Created by alexandrafritzen on 14/09/2017.
 */

enum RecommendationMapping {
    COFFEE(mappingCoffee, new PlaceType[]{PlaceType.BAKERY, PlaceType.CAFE}, R.drawable.coffee),
    FOOD(mappingFood, new PlaceType[]{PlaceType.BAKERY, PlaceType.CAFE, PlaceType.GAS_STATION, PlaceType.MEAL_TAKEAWAY, PlaceType.RESTAURANT}, R.drawable.food),
    BATHROOM(mappingBathroom, new PlaceType[]{PlaceType.CAFE, PlaceType.GAS_STATION, PlaceType.RESTAURANT}, R.drawable.bathroom),
    SLEEP(mappingSleep, new PlaceType[]{PlaceType.LODGING}, R.drawable.hotel),
    STRETCH_LEGS(mappingStretchLegs, new PlaceType[]{PlaceType.PARK}, R.drawable.stretch_legs),
    SWITCH_DRIVER(mappingSwitchDriver, new PlaceType[]{PlaceType.PARK, PlaceType.GAS_STATION}, R.drawable.switch_driver);

    private final String activity;
    private final PlaceType[] types;
    private final int icon;

    RecommendationMapping(String activity, PlaceType[] types, int icon) {
        this.activity = activity;
        this.types = types;
        this.icon = icon;
    }

    public String getActivity() {
        return activity;
    }

    public PlaceType[] getTypes() {
        return types;
    }

    public int getIcon() {
        return icon;
    }
}
