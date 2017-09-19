package au.edu.unimelb.mc.trippal.recommendations;

import au.edu.unimelb.mc.trippal.R;

/**
 * Created by alexandrafritzen on 14/09/2017.
 */

public enum RecommendationMapping {
    COFFEE("coffee", new PlaceType[]{PlaceType.BAKERY, PlaceType.CAFE}, R.drawable.coffee),
    FOOD("food", new PlaceType[]{PlaceType.BAKERY, PlaceType.CAFE, PlaceType.GAS_STATION, PlaceType.MEAL_TAKEAWAY, PlaceType.RESTAURANT}, R.drawable.food),
    BATHROOM("bathroom", new PlaceType[]{PlaceType.CAFE, PlaceType.GAS_STATION, PlaceType.RESTAURANT}, R.drawable.bathroom),
    SLEEP("sleep", new PlaceType[]{PlaceType.LODGING}, R.drawable.hotel),
    STRETCH_LEGS("stretch legs", new PlaceType[]{PlaceType.PARK}, R.drawable.stretch_legs),
    SWITCH_DRIVER("switch driver", new PlaceType[]{PlaceType.PARK, PlaceType.GAS_STATION}, R.drawable.switch_driver);

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
