package android.telecom;

import android.os.RemoteException;
import com.android.internal.telecom.IConnectionService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public final class RemoteConference {
    private int mCallCapabilities;
    private final Set<Callback> mCallbacks = new CopyOnWriteArraySet();
    private final List<RemoteConnection> mChildConnections = new CopyOnWriteArrayList();
    private final List<RemoteConnection> mConferenceableConnections = new ArrayList();
    private final IConnectionService mConnectionService;
    private DisconnectCause mDisconnectCause;
    private final String mId;
    private int mState = 1;
    private final List<RemoteConnection> mUnmodifiableChildConnections = Collections.unmodifiableList(this.mChildConnections);
    private final List<RemoteConnection> mUnmodifiableConferenceableConnections = Collections.unmodifiableList(this.mConferenceableConnections);

    public static abstract class Callback {
        public void onStateChanged(RemoteConference conference, int oldState, int newState) {
        }

        public void onDisconnected(RemoteConference conference, DisconnectCause disconnectCause) {
        }

        public void onConnectionAdded(RemoteConference conference, RemoteConnection connection) {
        }

        public void onConnectionRemoved(RemoteConference conference, RemoteConnection connection) {
        }

        public void onCapabilitiesChanged(RemoteConference conference, int capabilities) {
        }

        public void onConferenceableConnectionsChanged(RemoteConference conference, List<RemoteConnection> list) {
        }

        public void onDestroyed(RemoteConference conference) {
        }
    }

    RemoteConference(String id, IConnectionService connectionService) {
        this.mId = id;
        this.mConnectionService = connectionService;
    }

    /* access modifiers changed from: package-private */
    public String getId() {
        return this.mId;
    }

    /* access modifiers changed from: package-private */
    public void setDestroyed() {
        for (RemoteConnection connection : this.mChildConnections) {
            connection.setConference(null);
        }
        for (Callback c : this.mCallbacks) {
            c.onDestroyed(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void setState(int newState) {
        if (newState != 4 && newState != 5 && newState != 6) {
            Log.w(this, "Unsupported state transition for Conference call.", Connection.stateToString(newState));
        } else if (this.mState != newState) {
            int oldState = this.mState;
            this.mState = newState;
            for (Callback c : this.mCallbacks) {
                c.onStateChanged(this, oldState, newState);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void addConnection(RemoteConnection connection) {
        if (!this.mChildConnections.contains(connection)) {
            this.mChildConnections.add(connection);
            connection.setConference(this);
            for (Callback c : this.mCallbacks) {
                c.onConnectionAdded(this, connection);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeConnection(RemoteConnection connection) {
        if (this.mChildConnections.contains(connection)) {
            this.mChildConnections.remove(connection);
            connection.setConference(null);
            for (Callback c : this.mCallbacks) {
                c.onConnectionRemoved(this, connection);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setCallCapabilities(int capabilities) {
        if (this.mCallCapabilities != capabilities) {
            this.mCallCapabilities = capabilities;
            for (Callback c : this.mCallbacks) {
                c.onCapabilitiesChanged(this, this.mCallCapabilities);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setConferenceableConnections(List<RemoteConnection> conferenceableConnections) {
        this.mConferenceableConnections.clear();
        this.mConferenceableConnections.addAll(conferenceableConnections);
        for (Callback c : this.mCallbacks) {
            c.onConferenceableConnectionsChanged(this, this.mUnmodifiableConferenceableConnections);
        }
    }

    /* access modifiers changed from: package-private */
    public void setDisconnected(DisconnectCause disconnectCause) {
        if (this.mState != 6) {
            this.mDisconnectCause = disconnectCause;
            setState(6);
            for (Callback c : this.mCallbacks) {
                c.onDisconnected(this, disconnectCause);
            }
        }
    }

    public final List<RemoteConnection> getConnections() {
        return this.mUnmodifiableChildConnections;
    }

    public final int getState() {
        return this.mState;
    }

    public final int getCallCapabilities() {
        return this.mCallCapabilities;
    }

    public void disconnect() {
        try {
            this.mConnectionService.disconnect(this.mId);
        } catch (RemoteException e) {
        }
    }

    public void separate(RemoteConnection connection) {
        if (this.mChildConnections.contains(connection)) {
            try {
                this.mConnectionService.splitFromConference(connection.getId());
            } catch (RemoteException e) {
            }
        }
    }

    public void merge() {
        try {
            this.mConnectionService.mergeConference(this.mId);
        } catch (RemoteException e) {
        }
    }

    public void swap() {
        try {
            this.mConnectionService.swapConference(this.mId);
        } catch (RemoteException e) {
        }
    }

    public void hold() {
        try {
            this.mConnectionService.hold(this.mId);
        } catch (RemoteException e) {
        }
    }

    public void unhold() {
        try {
            this.mConnectionService.unhold(this.mId);
        } catch (RemoteException e) {
        }
    }

    public DisconnectCause getDisconnectCause() {
        return this.mDisconnectCause;
    }

    public void playDtmfTone(char digit) {
        try {
            this.mConnectionService.playDtmfTone(this.mId, digit);
        } catch (RemoteException e) {
        }
    }

    public void stopDtmfTone() {
        try {
            this.mConnectionService.stopDtmfTone(this.mId);
        } catch (RemoteException e) {
        }
    }

    public void setAudioState(AudioState state) {
        try {
            this.mConnectionService.onAudioStateChanged(this.mId, state);
        } catch (RemoteException e) {
        }
    }

    public List<RemoteConnection> getConferenceableConnections() {
        return this.mUnmodifiableConferenceableConnections;
    }

    public final void registerCallback(Callback callback) {
        this.mCallbacks.add(callback);
    }

    public final void unregisterCallback(Callback callback) {
        this.mCallbacks.remove(callback);
    }
}
