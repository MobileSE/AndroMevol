package android.media.audiopolicy;

import android.os.Binder;
import android.os.IBinder;
import java.util.ArrayList;

public class AudioPolicy {
    public static final int POLICY_STATUS_INVALID = 0;
    public static final int POLICY_STATUS_REGISTERED = 2;
    public static final int POLICY_STATUS_UNREGISTERED = 1;
    private static final String TAG = "AudioPolicy";
    private AudioPolicyConfig mConfig;
    private int mStatus;
    private AudioPolicyStatusListener mStatusListener;
    private final IBinder mToken;

    public IBinder token() {
        return this.mToken;
    }

    public AudioPolicyConfig getConfig() {
        return this.mConfig;
    }

    private AudioPolicy(AudioPolicyConfig config) {
        this.mStatusListener = null;
        this.mToken = new Binder();
        this.mConfig = config;
        if (this.mConfig.mMixes.isEmpty()) {
            this.mStatus = 0;
        } else {
            this.mStatus = 1;
        }
    }

    public static class Builder {
        private ArrayList<AudioMix> mMixes = new ArrayList<>();

        public Builder addMix(AudioMix mix) throws IllegalArgumentException {
            if (mix == null) {
                throw new IllegalArgumentException("Illegal null AudioMix argument");
            }
            this.mMixes.add(mix);
            return this;
        }

        public AudioPolicy build() {
            return new AudioPolicy(new AudioPolicyConfig(this.mMixes));
        }
    }

    public int getStatus() {
        return this.mStatus;
    }

    public static abstract class AudioPolicyStatusListener {
        /* access modifiers changed from: package-private */
        public void onStatusChange() {
        }

        /* access modifiers changed from: package-private */
        public void onMixStateUpdate(AudioMix mix) {
        }
    }

    /* access modifiers changed from: package-private */
    public void setStatusListener(AudioPolicyStatusListener l) {
        this.mStatusListener = l;
    }

    public String toString() {
        return new String("android.media.audiopolicy.AudioPolicy:\n") + "config=" + this.mConfig.toString();
    }
}
