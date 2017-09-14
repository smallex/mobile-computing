package au.edu.unimelb.mc.trippal.recommendations;

/**
 * Created by alexandrafritzen on 14/09/2017.
 */

public enum RecommendationMapping {
    COFFEE("coffee",new PlaceType[]{PlaceType.BAKERY, PlaceType.CAFE}),
    FOOD("food",new PlaceType[]{PlaceType.BAKERY, PlaceType.CAFE, PlaceType.GAS_STATION, PlaceType.MEAL_TAKEAWAY, PlaceType.RESTAURANT}),
    BATHROOM("bathroom",new PlaceType[]{PlaceType.CAFE, PlaceType.GAS_STATION, PlaceType.RESTAURANT}),
    SLEEP("sleep",new PlaceType[]{PlaceType.LODGING}),
    STRETCH_LEGS("stretch legs",new PlaceType[]{PlaceType.PARK});

    private final String activity;
    private final PlaceType[] types;

    RecommendationMapping(String activity, PlaceType[] types){
        this.activity = activity;
        this.types = types;
    }

    public String getActivity(){
        return activity;
    }

    public PlaceType[] getTypes(){
        return types;
    }
}
