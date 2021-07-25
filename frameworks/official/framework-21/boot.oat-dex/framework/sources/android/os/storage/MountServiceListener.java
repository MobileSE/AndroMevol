package android.os.storage;

public abstract class MountServiceListener {
    /* access modifiers changed from: package-private */
    public void onUsbMassStorageConnectionChanged(boolean connected) {
    }

    /* access modifiers changed from: package-private */
    public void onStorageStateChange(String path, String oldState, String newState) {
    }
}
