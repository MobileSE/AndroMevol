package android.location;

import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

abstract class LocalListenerHelper<TListener> {
    private final HashSet<TListener> mListeners = new HashSet<>();
    private final String mTag;

    protected interface ListenerOperation<TListener> {
        void execute(TListener tlistener) throws RemoteException;
    }

    /* access modifiers changed from: protected */
    public abstract boolean registerWithServer() throws RemoteException;

    /* access modifiers changed from: protected */
    public abstract void unregisterFromServer() throws RemoteException;

    protected LocalListenerHelper(String name) {
        Preconditions.checkNotNull(name);
        this.mTag = name;
    }

    public boolean add(TListener listener) {
        Preconditions.checkNotNull(listener);
        synchronized (this.mListeners) {
            if (this.mListeners.isEmpty()) {
                try {
                    if (!registerWithServer()) {
                        Log.e(this.mTag, "Unable to register listener transport.");
                        return false;
                    }
                } catch (RemoteException e) {
                    Log.e(this.mTag, "Error handling first listener.", e);
                    return false;
                }
            }
            if (this.mListeners.contains(listener)) {
                return true;
            }
            this.mListeners.add(listener);
            return true;
        }
    }

    public void remove(TListener listener) {
        Preconditions.checkNotNull(listener);
        synchronized (this.mListeners) {
            if (this.mListeners.remove(listener) && this.mListeners.isEmpty()) {
                try {
                    unregisterFromServer();
                } catch (RemoteException e) {
                }
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: android.location.LocalListenerHelper$ListenerOperation */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    public void foreach(ListenerOperation operation) {
        Collection<TListener> listeners;
        synchronized (this.mListeners) {
            listeners = new ArrayList<>(this.mListeners);
        }
        for (TListener listener : listeners) {
            try {
                operation.execute(listener);
            } catch (RemoteException e) {
                Log.e(this.mTag, "Error in monitored listener.", e);
            }
        }
    }
}
