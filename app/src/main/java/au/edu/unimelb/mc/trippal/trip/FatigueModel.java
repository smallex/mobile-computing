package au.edu.unimelb.mc.trippal.trip;

import java.util.concurrent.TimeUnit;

/**
 * This class models the fatigue of a driver during a trip, based on certain trip data.
 * Uses very naive assumptions, useful only as proof of concept.
 */
class FatigueModel {
    private final int MEAN_SLEEP_DURATION_HOURS = 8;
    // The fatigue level assumed by the model from 0 - 100, where 0 = completely rested and 100 =
    // must take a break immediately
    private float fatigueLevel = 0;
    private long timeElapsed = 0;
    private float lastFatigueLevel = 0;

    FatigueModel(int currentDrowsinessLevel, int lastSleepHours, int lastSleepMinutes, int
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
    float getFatigueLevel() {
        float result = computeNewFatigueLevel();
        lastFatigueLevel = result;
        return result;
    }

    private float computeNewFatigueLevel() {
        // 4 hours driving => 100 units
        float fatigue = fatigueLevel + (TimeUnit.MILLISECONDS.toHours(timeElapsed) * 50.0f /
                2.0f);
        float result = clamp(0.0f, 100.0f, fatigue);
        return result;
    }

    boolean isHighRisk() {
        return lastFatigueLevel < 80.0f && computeNewFatigueLevel() >= 80.0f
                || lastFatigueLevel < 90.0f && computeNewFatigueLevel() >= 90.0f
                || lastFatigueLevel < 100.0f && computeNewFatigueLevel() == 100.0f;
    }

    void setTimeElapsed(long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    void setCurrentStopDuration(long currentStopDuration) {
        // Substract 1.5 fatigue units per minute stopped => 30 min break => 45 points
        long minutes = TimeUnit.MILLISECONDS.toMinutes(currentStopDuration);
        this.fatigueLevel -= minutes * 1.5;
        this.fatigueLevel = clamp(0.0f, 100.0f, this.fatigueLevel);
    }

    FatigueRisk getCurrentRisk() {
        if (fatigueLevel < 40) {
            return FatigueRisk.LOW;
        } else if (fatigueLevel < 80) {
            return FatigueRisk.MEDIUM;
        } else {
            return FatigueRisk.HIGH;
        }
    }

    public void setMaximum() {
        this.fatigueLevel = 100.0f;
    }

    enum FatigueRisk {
        LOW, MEDIUM, HIGH
    }
}
