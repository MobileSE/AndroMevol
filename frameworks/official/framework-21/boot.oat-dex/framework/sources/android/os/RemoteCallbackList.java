package android.os;

import android.os.IBinder;
import android.os.IInterface;
import android.util.ArrayMap;

public class RemoteCallbackList<E extends IInterface> {
    private Object[] mActiveBroadcast;
    private int mBroadcastCount = -1;
    ArrayMap<IBinder, RemoteCallbackList<E>.Callback> mCallbacks = new ArrayMap<>();
    private boolean mKilled = false;

    /* access modifiers changed from: private */
    public final class Callback implements IBinder.DeathRecipient {
        final E mCallback;
        final Object mCookie;

        Callback(E callback, Object cookie) {
            this.mCallback = callback;
            this.mCookie = cookie;
        }

        /* JADX DEBUG: Multi-variable search result rejected for r0v4, resolved type: android.os.RemoteCallbackList */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (RemoteCallbackList.this.mCallbacks) {
                RemoteCallbackList.this.mCallbacks.remove(this.mCallback.asBinder());
            }
            RemoteCallbackList.this.onCallbackDied(this.mCallback, this.mCookie);
        }
    }

    public boolean register(E callback) {
        return register(callback, null);
    }

    public boolean register(E callback, Object cookie) {
        boolean z = false;
        synchronized (this.mCallbacks) {
            if (!this.mKilled) {
                IBinder binder = callback.asBinder();
                try {
                    RemoteCallbackList<E>.Callback cb = new Callback(callback, cookie);
                    binder.linkToDeath(cb, 0);
                    this.mCallbacks.put(binder, cb);
                    z = true;
                } catch (RemoteException e) {
                }
            }
        }
        return z;
    }

    public boolean unregister(E callback) {
        boolean z = false;
        synchronized (this.mCallbacks) {
            RemoteCallbackList<E>.Callback cb = this.mCallbacks.remove(callback.asBinder());
            if (cb != null) {
                cb.mCallback.asBinder().unlinkToDeath(cb, 0);
                z = true;
            }
        }
        return z;
    }

    public void kill() {
        synchronized (this.mCallbacks) {
            for (int cbi = this.mCallbacks.size() - 1; cbi >= 0; cbi--) {
                RemoteCallbackList<E>.Callback cb = this.mCallbacks.valueAt(cbi);
                cb.mCallback.asBinder().unlinkToDeath(cb, 0);
            }
            this.mCallbacks.clear();
            this.mKilled = true;
        }
    }

    public void onCallbackDied(E e) {
    }

    public void onCallbackDied(E callback, Object cookie) {
        onCallbackDied(callback);
    }

    public int beginBroadcast() {
        int N;
        synchronized (this.mCallbacks) {
            if (this.mBroadcastCount > 0) {
                throw new IllegalStateException("beginBroadcast() called while already in a broadcast");
            }
            N = this.mCallbacks.size();
            this.mBroadcastCount = N;
            if (N <= 0) {
                N = 0;
            } else {
                Object[] active = this.mActiveBroadcast;
                if (active == null || active.length < N) {
                    active = new Object[N];
                    this.mActiveBroadcast = active;
                }
                for (int i = 0; i < N; i++) {
                    active[i] = this.mCallbacks.valueAt(i);
                }
            }
        }
        return N;
    }

    /* JADX WARN: Type inference failed for: r0v3, types: [E, E extends android.os.IInterface] */
    public E getBroadcastItem(int index) {
        return ((Callback) this.mActiveBroadcast[index]).mCallback;
    }

    public Object getBroadcastCookie(int index) {
        return ((Callback) this.mActiveBroadcast[index]).mCookie;
    }

    public void finishBroadcast() {
        if (this.mBroadcastCount < 0) {
            throw new IllegalStateException("finishBroadcast() called outside of a broadcast");
        }
        Object[] active = this.mActiveBroadcast;
        if (active != null) {
            int N = this.mBroadcastCount;
            for (int i = 0; i < N; i++) {
                active[i] = null;
            }
        }
        this.mBroadcastCount = -1;
    }

    public int getRegisteredCallbackCount() {
        int size;
        synchronized (this.mCallbacks) {
            if (this.mKilled) {
                size = 0;
            } else {
                size = this.mCallbacks.size();
            }
        }
        return size;
    }
}
