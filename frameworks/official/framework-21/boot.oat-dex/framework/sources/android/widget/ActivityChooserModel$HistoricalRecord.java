package android.widget;

import android.content.ComponentName;
import java.math.BigDecimal;

public final class ActivityChooserModel$HistoricalRecord {
    public final ComponentName activity;
    public final long time;
    public final float weight;

    public ActivityChooserModel$HistoricalRecord(String activityName, long time2, float weight2) {
        this(ComponentName.unflattenFromString(activityName), time2, weight2);
    }

    public ActivityChooserModel$HistoricalRecord(ComponentName activityName, long time2, float weight2) {
        this.activity = activityName;
        this.time = time2;
        this.weight = weight2;
    }

    public int hashCode() {
        return (((((this.activity == null ? 0 : this.activity.hashCode()) + 31) * 31) + ((int) (this.time ^ (this.time >>> 32)))) * 31) + Float.floatToIntBits(this.weight);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ActivityChooserModel$HistoricalRecord other = (ActivityChooserModel$HistoricalRecord) obj;
        if (this.activity == null) {
            if (other.activity != null) {
                return false;
            }
        } else if (!this.activity.equals(other.activity)) {
            return false;
        }
        if (this.time != other.time) {
            return false;
        }
        return Float.floatToIntBits(this.weight) == Float.floatToIntBits(other.weight);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append("; activity:").append(this.activity);
        builder.append("; time:").append(this.time);
        builder.append("; weight:").append(new BigDecimal((double) this.weight));
        builder.append("]");
        return builder.toString();
    }
}
