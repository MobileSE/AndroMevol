package android.location;

import android.os.Parcel;
import android.os.Parcelable;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class GpsMeasurementsEvent implements Parcelable {
    public static final Parcelable.Creator<GpsMeasurementsEvent> CREATOR = new Parcelable.Creator<GpsMeasurementsEvent>() {
        /* class android.location.GpsMeasurementsEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public GpsMeasurementsEvent createFromParcel(Parcel in) {
            GpsMeasurement[] measurementsArray = new GpsMeasurement[in.readInt()];
            in.readTypedArray(measurementsArray, GpsMeasurement.CREATOR);
            return new GpsMeasurementsEvent((GpsClock) in.readParcelable(getClass().getClassLoader()), measurementsArray);
        }

        @Override // android.os.Parcelable.Creator
        public GpsMeasurementsEvent[] newArray(int size) {
            return new GpsMeasurementsEvent[size];
        }
    };
    private final GpsClock mClock;
    private final Collection<GpsMeasurement> mReadOnlyMeasurements;

    public interface Listener {
        void onGpsMeasurementsReceived(GpsMeasurementsEvent gpsMeasurementsEvent);
    }

    public GpsMeasurementsEvent(GpsClock clock, GpsMeasurement[] measurements) {
        if (clock == null) {
            throw new InvalidParameterException("Parameter 'clock' must not be null.");
        } else if (measurements == null || measurements.length == 0) {
            throw new InvalidParameterException("Parameter 'measurements' must not be null or empty.");
        } else {
            this.mClock = clock;
            this.mReadOnlyMeasurements = Collections.unmodifiableCollection(Arrays.asList(measurements));
        }
    }

    public GpsClock getClock() {
        return this.mClock;
    }

    public Collection<GpsMeasurement> getMeasurements() {
        return this.mReadOnlyMeasurements;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(this.mClock, flags);
        GpsMeasurement[] measurementsArray = (GpsMeasurement[]) this.mReadOnlyMeasurements.toArray(new GpsMeasurement[0]);
        parcel.writeInt(measurementsArray.length);
        parcel.writeTypedArray(measurementsArray, flags);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("[ GpsMeasurementsEvent:\n\n");
        builder.append(this.mClock.toString());
        builder.append("\n");
        for (GpsMeasurement measurement : this.mReadOnlyMeasurements) {
            builder.append(measurement.toString());
            builder.append("\n");
        }
        builder.append("]");
        return builder.toString();
    }
}
