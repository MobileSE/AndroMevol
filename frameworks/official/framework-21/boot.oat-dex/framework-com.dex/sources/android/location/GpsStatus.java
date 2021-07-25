package android.location;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class GpsStatus {
    public static final int GPS_EVENT_FIRST_FIX = 3;
    public static final int GPS_EVENT_SATELLITE_STATUS = 4;
    public static final int GPS_EVENT_STARTED = 1;
    public static final int GPS_EVENT_STOPPED = 2;
    private static final int NUM_SATELLITES = 255;
    private Iterable<GpsSatellite> mSatelliteList = new Iterable<GpsSatellite>() {
        /* class android.location.GpsStatus.AnonymousClass1 */

        @Override // java.lang.Iterable
        public Iterator<GpsSatellite> iterator() {
            return new SatelliteIterator(GpsStatus.this.mSatellites);
        }
    };
    private GpsSatellite[] mSatellites = new GpsSatellite[255];
    private int mTimeToFirstFix;

    public interface Listener {
        void onGpsStatusChanged(int i);
    }

    public interface NmeaListener {
        void onNmeaReceived(long j, String str);
    }

    private final class SatelliteIterator implements Iterator<GpsSatellite> {
        int mIndex = 0;
        private GpsSatellite[] mSatellites;

        SatelliteIterator(GpsSatellite[] satellites) {
            this.mSatellites = satellites;
        }

        public boolean hasNext() {
            for (int i = this.mIndex; i < this.mSatellites.length; i++) {
                if (this.mSatellites[i].mValid) {
                    return true;
                }
            }
            return false;
        }

        @Override // java.util.Iterator
        public GpsSatellite next() {
            while (this.mIndex < this.mSatellites.length) {
                GpsSatellite[] gpsSatelliteArr = this.mSatellites;
                int i = this.mIndex;
                this.mIndex = i + 1;
                GpsSatellite satellite = gpsSatelliteArr[i];
                if (satellite.mValid) {
                    return satellite;
                }
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    GpsStatus() {
        for (int i = 0; i < this.mSatellites.length; i++) {
            this.mSatellites[i] = new GpsSatellite(i + 1);
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void setStatus(int svCount, int[] prns, float[] snrs, float[] elevations, float[] azimuths, int ephemerisMask, int almanacMask, int usedInFixMask) {
        boolean z;
        for (int i = 0; i < this.mSatellites.length; i++) {
            this.mSatellites[i].mValid = false;
        }
        for (int i2 = 0; i2 < svCount; i2++) {
            int prn = prns[i2] - 1;
            int prnShift = 1 << prn;
            if (prn >= 0 && prn < this.mSatellites.length) {
                GpsSatellite satellite = this.mSatellites[prn];
                satellite.mValid = true;
                satellite.mSnr = snrs[i2];
                satellite.mElevation = elevations[i2];
                satellite.mAzimuth = azimuths[i2];
                satellite.mHasEphemeris = (ephemerisMask & prnShift) != 0;
                satellite.mHasAlmanac = (almanacMask & prnShift) != 0;
                if ((usedInFixMask & prnShift) != 0) {
                    z = true;
                } else {
                    z = false;
                }
                satellite.mUsedInFix = z;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setStatus(GpsStatus status) {
        this.mTimeToFirstFix = status.getTimeToFirstFix();
        for (int i = 0; i < this.mSatellites.length; i++) {
            this.mSatellites[i].setStatus(status.mSatellites[i]);
        }
    }

    /* access modifiers changed from: package-private */
    public void setTimeToFirstFix(int ttff) {
        this.mTimeToFirstFix = ttff;
    }

    public int getTimeToFirstFix() {
        return this.mTimeToFirstFix;
    }

    public Iterable<GpsSatellite> getSatellites() {
        return this.mSatelliteList;
    }

    public int getMaxSatellites() {
        return 255;
    }
}
