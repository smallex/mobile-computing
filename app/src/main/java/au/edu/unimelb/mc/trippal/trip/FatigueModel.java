package au.edu.unimelb.mc.trippal.trip;

import java.util.concurrent.TimeUnit;

/**
 * This class models the fatigue of a driver during a trip, based on certain trip data.
 * Uses very naive assumptions, useful only as proof of concept.
 */
public class FatigueModel {
    private final int MEAN_SLEEP_DURATION_HOURS = 8;
    // The fatigue level assumed by the model from 0 - 100, where 0 = completely rested and 100 =
    // must take a break immediately
    private float fatigueLevel = 0;
    private long timeElapsed = 0;
    private float lastFatigueLevel = 0;

    public FatigueModel(int currentDrowsinessLevel, int lastSleepHours, int lastSleepMinutes, int
            lastSleepQuality) {

        currentDrowsinessLevel = (int) clamp(0.0f, 5.0f, currentDrowsinessLevel);
        lastSleepQuality = (int) clamp(0.0f, 100.0f, lastSleepQuality);

        // 0 - 50 points for subjective tiredness
        fatigueLevel = currentDrowsinessLevel * 10;

        // 10 points for each hour over/under mean duration
        fatigueLevel -= (lastSleepHours * (lastSleepQuality / 100.0) - MEAN_SLEEP_DURATION_HOURS)
                * 10;

        // clamp to 0 - 100
        fatigueLevel = clamp(0.0f, 100.0f, fatigueLevel);
    }

    // Clamp value between min and max
    private float clamp(float min, float max, float value) {
        return Math.min(max, Math.max(min, value));
    }

    /**
     * @return The current estimated fatigue level of the driver, from 0 - 100
     */
    public float getFatigueLevel() {
        float result = computeNewFatigueLevel();
        lastFatigueLevel = result;
        return result;
    }

    private float computeNewFatigueLevel() {
        float fatigue = fatigueLevel + (TimeUnit.MILLISECONDS.toHours(timeElapsed) * 50.0f /
                2.0f);
        float result = clamp(0.0f, 100.0f, fatigue);
        return result;
    }

    public boolean isHighRisk() {
        return lastFatigueLevel < 80.0f && computeNewFatigueLevel() >= 80.0f
                || lastFatigueLevel < 90.0f && computeNewFatigueLevel() >= 90.0f
                || lastFatigueLevel < 100.0f && computeNewFatigueLevel() == 100.0f;
    }

    public void setTimeElapsed(long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }
}
