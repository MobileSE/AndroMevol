package android.util;

import android.hardware.camera2.utils.HashCodeHelpers;
import com.android.internal.util.Preconditions;
import java.lang.Comparable;

public final class Range<T extends Comparable<? super T>> {
    private final T mLower;
    private final T mUpper;

    public Range(T lower, T upper) {
        this.mLower = (T) ((Comparable) Preconditions.checkNotNull(lower, "lower must not be null"));
        this.mUpper = (T) ((Comparable) Preconditions.checkNotNull(upper, "upper must not be null"));
        if (lower.compareTo(upper) > 0) {
            throw new IllegalArgumentException("lower must be less than or equal to upper");
        }
    }

    public static <T extends Comparable<? super T>> Range<T> create(T lower, T upper) {
        return new Range<>(lower, upper);
    }

    public T getLower() {
        return this.mLower;
    }

    public T getUpper() {
        return this.mUpper;
    }

    public boolean contains(T value) {
        boolean gteLower;
        boolean lteUpper;
        Preconditions.checkNotNull(value, "value must not be null");
        if (value.compareTo(this.mLower) >= 0) {
            gteLower = true;
        } else {
            gteLower = false;
        }
        if (value.compareTo(this.mUpper) <= 0) {
            lteUpper = true;
        } else {
            lteUpper = false;
        }
        return gteLower && lteUpper;
    }

    public boolean contains(Range<T> range) {
        boolean gteLower;
        boolean lteUpper;
        Preconditions.checkNotNull(range, "value must not be null");
        if (range.mLower.compareTo(this.mLower) >= 0) {
            gteLower = true;
        } else {
            gteLower = false;
        }
        if (range.mUpper.compareTo(this.mUpper) <= 0) {
            lteUpper = true;
        } else {
            lteUpper = false;
        }
        return gteLower && lteUpper;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Range)) {
            return false;
        }
        Range other = (Range) obj;
        if (!this.mLower.equals(other.mLower) || !this.mUpper.equals(other.mUpper)) {
            z = false;
        }
        return z;
    }

    public T clamp(T value) {
        Preconditions.checkNotNull(value, "value must not be null");
        if (value.compareTo(this.mLower) < 0) {
            return this.mLower;
        }
        if (value.compareTo(this.mUpper) > 0) {
            return this.mUpper;
        }
        return value;
    }

    public Range<T> intersect(Range<T> range) {
        Preconditions.checkNotNull(range, "range must not be null");
        int cmpLower = range.mLower.compareTo(this.mLower);
        int cmpUpper = range.mUpper.compareTo(this.mUpper);
        if (cmpLower <= 0 && cmpUpper >= 0) {
            return this;
        }
        if (cmpLower >= 0 && cmpUpper <= 0) {
            return range;
        }
        return create(cmpLower <= 0 ? this.mLower : range.mLower, cmpUpper >= 0 ? this.mUpper : range.mUpper);
    }

    public Range<T> intersect(T lower, T upper) {
        Preconditions.checkNotNull(lower, "lower must not be null");
        Preconditions.checkNotNull(upper, "upper must not be null");
        int cmpLower = lower.compareTo(this.mLower);
        int cmpUpper = upper.compareTo(this.mUpper);
        if (cmpLower <= 0 && cmpUpper >= 0) {
            return this;
        }
        if (cmpLower <= 0) {
            lower = this.mLower;
        }
        if (cmpUpper >= 0) {
            upper = this.mUpper;
        }
        return create(lower, upper);
    }

    public Range<T> extend(Range<T> range) {
        Preconditions.checkNotNull(range, "range must not be null");
        int cmpLower = range.mLower.compareTo(this.mLower);
        int cmpUpper = range.mUpper.compareTo(this.mUpper);
        if (cmpLower <= 0 && cmpUpper >= 0) {
            return range;
        }
        if (cmpLower >= 0 && cmpUpper <= 0) {
            return this;
        }
        return create(cmpLower >= 0 ? this.mLower : range.mLower, cmpUpper <= 0 ? this.mUpper : range.mUpper);
    }

    public Range<T> extend(T lower, T upper) {
        Preconditions.checkNotNull(lower, "lower must not be null");
        Preconditions.checkNotNull(upper, "upper must not be null");
        int cmpLower = lower.compareTo(this.mLower);
        int cmpUpper = upper.compareTo(this.mUpper);
        if (cmpLower >= 0 && cmpUpper <= 0) {
            return this;
        }
        if (cmpLower >= 0) {
            lower = this.mLower;
        }
        if (cmpUpper <= 0) {
            upper = this.mUpper;
        }
        return create(lower, upper);
    }

    public Range<T> extend(T value) {
        Preconditions.checkNotNull(value, "value must not be null");
        return extend(value, value);
    }

    public String toString() {
        return String.format("[%s, %s]", this.mLower, this.mUpper);
    }

    public int hashCode() {
        return HashCodeHelpers.hashCode(this.mLower, this.mUpper);
    }
}
