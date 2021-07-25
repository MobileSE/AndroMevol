package android.media.audiopolicy;

import android.media.AudioFormat;
import android.media.AudioSystem;

public class AudioMix {
    public static final int ROUTE_FLAG_LOOP_BACK = 2;
    public static final int ROUTE_FLAG_RENDER = 1;
    private AudioFormat mFormat;
    private int mRouteFlags;
    private AudioMixingRule mRule;

    private AudioMix(AudioMixingRule rule, AudioFormat format, int routeFlags) {
        this.mRule = rule;
        this.mFormat = format;
        this.mRouteFlags = routeFlags;
    }

    /* access modifiers changed from: package-private */
    public int getRouteFlags() {
        return this.mRouteFlags;
    }

    /* access modifiers changed from: package-private */
    public AudioFormat getFormat() {
        return this.mFormat;
    }

    /* access modifiers changed from: package-private */
    public AudioMixingRule getRule() {
        return this.mRule;
    }

    public static class Builder {
        private AudioFormat mFormat = null;
        private int mRouteFlags = 0;
        private AudioMixingRule mRule = null;

        Builder() {
        }

        public Builder(AudioMixingRule rule) throws IllegalArgumentException {
            if (rule == null) {
                throw new IllegalArgumentException("Illegal null AudioMixingRule argument");
            }
            this.mRule = rule;
        }

        public Builder setMixingRule(AudioMixingRule rule) throws IllegalArgumentException {
            if (rule == null) {
                throw new IllegalArgumentException("Illegal null AudioMixingRule argument");
            }
            this.mRule = rule;
            return this;
        }

        public Builder setFormat(AudioFormat format) throws IllegalArgumentException {
            if (format == null) {
                throw new IllegalArgumentException("Illegal null AudioFormat argument");
            }
            this.mFormat = format;
            return this;
        }

        public Builder setRouteFlags(int routeFlags) throws IllegalArgumentException {
            if (routeFlags == 0) {
                throw new IllegalArgumentException("Illegal empty route flags");
            } else if ((routeFlags & 3) == 0) {
                throw new IllegalArgumentException("Invalid route flags 0x" + Integer.toHexString(routeFlags) + "when creating an AudioMix");
            } else {
                this.mRouteFlags = routeFlags;
                return this;
            }
        }

        public AudioMix build() throws IllegalArgumentException {
            if (this.mRule == null) {
                throw new IllegalArgumentException("Illegal null AudioMixingRule");
            }
            if (this.mRouteFlags == 0) {
                this.mRouteFlags = 1;
            }
            if (this.mFormat == null) {
                int rate = AudioSystem.getPrimaryOutputSamplingRate();
                if (rate <= 0) {
                    rate = 44100;
                }
                this.mFormat = new AudioFormat.Builder().setSampleRate(rate).build();
            }
            return new AudioMix(this.mRule, this.mFormat, this.mRouteFlags);
        }
    }
}
