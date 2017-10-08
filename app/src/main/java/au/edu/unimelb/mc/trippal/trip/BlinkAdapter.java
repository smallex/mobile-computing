package au.edu.unimelb.mc.trippal.trip;

import com.robinhood.spark.SparkAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Custom SparkAdapter for showing blinking frequency.
 */
class BlinkAdapter extends SparkAdapter {

    private List<Integer> blinkFrequencies;

    BlinkAdapter() {
        this.blinkFrequencies = new ArrayList<>();
    }

    void update(List<Long> blinkTimes) {

        long currentTime = System.currentTimeMillis();

        int i;
        for (i = 0; i < blinkTimes.size(); i++) {
            if (currentTime - blinkTimes.get(i) <= 10 * 60 * 1000) {
                break;
            }
        }
        this.blinkFrequencies = new ArrayList<Integer>();
        for (int k = 0; k < 10; k++) {
            blinkFrequencies.add(0);
        }
        for (int j = i; j < blinkTimes.size(); j++) {
            int diffMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(currentTime - blinkTimes.get
                    (j));
            if (diffMinutes < 10) {
                blinkFrequencies.set(9 - diffMinutes, blinkFrequencies.get(9 - diffMinutes) + 1);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public boolean hasBaseLine() {
        return true;
    }

    @Override
    public float getBaseLine() {
        return 10;
    }

    @Override
    public int getCount() {
        return this.blinkFrequencies.size();
    }

    @Override
    public Object getItem(int index) {
        return this.blinkFrequencies.get(index);
    }

    @Override
    public float getY(int index) {
        return this.blinkFrequencies.get(index);
    }
}
