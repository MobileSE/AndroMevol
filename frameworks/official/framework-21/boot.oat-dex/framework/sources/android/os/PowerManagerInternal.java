package android.os;

public abstract class PowerManagerInternal {

    public interface LowPowerModeListener {
        void onLowPowerModeChanged(boolean z);
    }

    public abstract boolean getLowPowerModeEnabled();

    public abstract void registerLowPowerModeObserver(LowPowerModeListener lowPowerModeListener);

    public abstract void setButtonBrightnessOverrideFromWindowManager(int i);

    public abstract void setDozeOverrideFromDreamManager(int i, int i2);

    public abstract void setScreenBrightnessOverrideFromWindowManager(int i);

    public abstract void setUserActivityTimeoutOverrideFromWindowManager(long j);
}
