package android.telecom;

import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Surface;
import com.android.internal.telecom.IConnectionService;
import com.android.internal.telecom.IVideoCallback;
import com.android.internal.telecom.IVideoProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class RemoteConnection {
    private Uri mAddress;
    private int mAddressPresentation;
    private int mCallCapabilities;
    private final Set<Callback> mCallbacks;
    private String mCallerDisplayName;
    private int mCallerDisplayNamePresentation;
    private RemoteConference mConference;
    private final List<RemoteConnection> mConferenceableConnections;
    private boolean mConnected;
    private final String mConnectionId;
    private IConnectionService mConnectionService;
    private DisconnectCause mDisconnectCause;
    private boolean mIsVoipAudioMode;
    private boolean mRingbackRequested;
    private int mState;
    private StatusHints mStatusHints;
    private final List<RemoteConnection> mUnmodifiableconferenceableConnections;
    private VideoProvider mVideoProvider;
    private int mVideoState;

    public static abstract class Callback {
        public void onStateChanged(RemoteConnection connection, int state) {
        }

        public void onDisconnected(RemoteConnection connection, DisconnectCause disconnectCause) {
        }

        public void onRingbackRequested(RemoteConnection connection, boolean ringback) {
        }

        public void onCallCapabilitiesChanged(RemoteConnection connection, int callCapabilities) {
        }

        public void onPostDialWait(RemoteConnection connection, String remainingPostDialSequence) {
        }

        public void onVoipAudioChanged(RemoteConnection connection, boolean isVoip) {
        }

        public void onStatusHintsChanged(RemoteConnection connection, StatusHints statusHints) {
        }

        public void onAddressChanged(RemoteConnection connection, Uri address, int presentation) {
        }

        public void onCallerDisplayNameChanged(RemoteConnection connection, String callerDisplayName, int presentation) {
        }

        public void onVideoStateChanged(RemoteConnection connection, int videoState) {
        }

        public void onDestroyed(RemoteConnection connection) {
        }

        public void onConferenceableConnectionsChanged(RemoteConnection connection, List<RemoteConnection> list) {
        }

        public void onVideoProviderChanged(RemoteConnection connection, VideoProvider videoProvider) {
        }

        public void onConferenceChanged(RemoteConnection connection, RemoteConference conference) {
        }
    }

    public static class VideoProvider {
        private final Set<Listener> mListeners = Collections.newSetFromMap(new ConcurrentHashMap(8, 0.9f, 1));
        private final IVideoCallback mVideoCallbackDelegate = new IVideoCallback() {
            /* class android.telecom.RemoteConnection.VideoProvider.AnonymousClass1 */

            @Override // com.android.internal.telecom.IVideoCallback
            public void receiveSessionModifyRequest(VideoProfile videoProfile) {
                for (Listener l : VideoProvider.this.mListeners) {
                    l.onReceiveSessionModifyRequest(VideoProvider.this, videoProfile);
                }
            }

            @Override // com.android.internal.telecom.IVideoCallback
            public void receiveSessionModifyResponse(int status, VideoProfile requestedProfile, VideoProfile responseProfile) {
                for (Listener l : VideoProvider.this.mListeners) {
                    l.onReceiveSessionModifyResponse(VideoProvider.this, status, requestedProfile, responseProfile);
                }
            }

            @Override // com.android.internal.telecom.IVideoCallback
            public void handleCallSessionEvent(int event) {
                for (Listener l : VideoProvider.this.mListeners) {
                    l.onHandleCallSessionEvent(VideoProvider.this, event);
                }
            }

            @Override // com.android.internal.telecom.IVideoCallback
            public void changePeerDimensions(int width, int height) {
                for (Listener l : VideoProvider.this.mListeners) {
                    l.onPeerDimensionsChanged(VideoProvider.this, width, height);
                }
            }

            @Override // com.android.internal.telecom.IVideoCallback
            public void changeCallDataUsage(int dataUsage) {
                for (Listener l : VideoProvider.this.mListeners) {
                    l.onCallDataUsageChanged(VideoProvider.this, dataUsage);
                }
            }

            @Override // com.android.internal.telecom.IVideoCallback
            public void changeCameraCapabilities(CameraCapabilities cameraCapabilities) {
                for (Listener l : VideoProvider.this.mListeners) {
                    l.onCameraCapabilitiesChanged(VideoProvider.this, cameraCapabilities);
                }
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return null;
            }
        };
        private final VideoCallbackServant mVideoCallbackServant = new VideoCallbackServant(this.mVideoCallbackDelegate);
        private final IVideoProvider mVideoProviderBinder;

        public static abstract class Listener {
            public void onReceiveSessionModifyRequest(VideoProvider videoProvider, VideoProfile videoProfile) {
            }

            public void onReceiveSessionModifyResponse(VideoProvider videoProvider, int status, VideoProfile requestedProfile, VideoProfile responseProfile) {
            }

            public void onHandleCallSessionEvent(VideoProvider videoProvider, int event) {
            }

            public void onPeerDimensionsChanged(VideoProvider videoProvider, int width, int height) {
            }

            public void onCallDataUsageChanged(VideoProvider videoProvider, int dataUsage) {
            }

            public void onCameraCapabilitiesChanged(VideoProvider videoProvider, CameraCapabilities cameraCapabilities) {
            }
        }

        public VideoProvider(IVideoProvider videoProviderBinder) {
            this.mVideoProviderBinder = videoProviderBinder;
            try {
                this.mVideoProviderBinder.setVideoCallback(this.mVideoCallbackServant.getStub().asBinder());
            } catch (RemoteException e) {
            }
        }

        public void addListener(Listener l) {
            this.mListeners.add(l);
        }

        public void removeListener(Listener l) {
            this.mListeners.remove(l);
        }

        public void setCamera(String cameraId) {
            try {
                this.mVideoProviderBinder.setCamera(cameraId);
            } catch (RemoteException e) {
            }
        }

        public void setPreviewSurface(Surface surface) {
            try {
                this.mVideoProviderBinder.setPreviewSurface(surface);
            } catch (RemoteException e) {
            }
        }

        public void setDisplaySurface(Surface surface) {
            try {
                this.mVideoProviderBinder.setDisplaySurface(surface);
            } catch (RemoteException e) {
            }
        }

        public void setDeviceOrientation(int rotation) {
            try {
                this.mVideoProviderBinder.setDeviceOrientation(rotation);
            } catch (RemoteException e) {
            }
        }

        public void setZoom(float value) {
            try {
                this.mVideoProviderBinder.setZoom(value);
            } catch (RemoteException e) {
            }
        }

        public void sendSessionModifyRequest(VideoProfile reqProfile) {
            try {
                this.mVideoProviderBinder.sendSessionModifyRequest(reqProfile);
            } catch (RemoteException e) {
            }
        }

        public void sendSessionModifyResponse(VideoProfile responseProfile) {
            try {
                this.mVideoProviderBinder.sendSessionModifyResponse(responseProfile);
            } catch (RemoteException e) {
            }
        }

        public void requestCameraCapabilities() {
            try {
                this.mVideoProviderBinder.requestCameraCapabilities();
            } catch (RemoteException e) {
            }
        }

        public void requestCallDataUsage() {
            try {
                this.mVideoProviderBinder.requestCallDataUsage();
            } catch (RemoteException e) {
            }
        }

        public void setPauseImage(String uri) {
            try {
                this.mVideoProviderBinder.setPauseImage(uri);
            } catch (RemoteException e) {
            }
        }
    }

    RemoteConnection(String id, IConnectionService connectionService, ConnectionRequest request) {
        this.mCallbacks = Collections.newSetFromMap(new ConcurrentHashMap(8, 0.9f, 1));
        this.mConferenceableConnections = new ArrayList();
        this.mUnmodifiableconferenceableConnections = Collections.unmodifiableList(this.mConferenceableConnections);
        this.mState = 1;
        this.mConnectionId = id;
        this.mConnectionService = connectionService;
        this.mConnected = true;
        this.mState = 0;
    }

    RemoteConnection(DisconnectCause disconnectCause) {
        this(WifiEnterpriseConfig.EMPTY_VALUE, null, null);
        this.mConnected = false;
        this.mState = 6;
        this.mDisconnectCause = disconnectCause;
    }

    public void registerCallback(Callback callback) {
        this.mCallbacks.add(callback);
    }

    public void unregisterCallback(Callback callback) {
        if (callback != null) {
            this.mCallbacks.remove(callback);
        }
    }

    public int getState() {
        return this.mState;
    }

    public DisconnectCause getDisconnectCause() {
        return this.mDisconnectCause;
    }

    public int getCallCapabilities() {
        return this.mCallCapabilities;
    }

    public boolean isVoipAudioMode() {
        return this.mIsVoipAudioMode;
    }

    public StatusHints getStatusHints() {
        return this.mStatusHints;
    }

    public Uri getAddress() {
        return this.mAddress;
    }

    public int getAddressPresentation() {
        return this.mAddressPresentation;
    }

    public CharSequence getCallerDisplayName() {
        return this.mCallerDisplayName;
    }

    public int getCallerDisplayNamePresentation() {
        return this.mCallerDisplayNamePresentation;
    }

    public int getVideoState() {
        return this.mVideoState;
    }

    public final VideoProvider getVideoProvider() {
        return this.mVideoProvider;
    }

    public boolean isRingbackRequested() {
        return false;
    }

    public void abort() {
        try {
            if (this.mConnected) {
                this.mConnectionService.abort(this.mConnectionId);
            }
        } catch (RemoteException e) {
        }
    }

    public void answer() {
        try {
            if (this.mConnected) {
                this.mConnectionService.answer(this.mConnectionId);
            }
        } catch (RemoteException e) {
        }
    }

    public void answer(int videoState) {
        try {
            if (this.mConnected) {
                this.mConnectionService.answerVideo(this.mConnectionId, videoState);
            }
        } catch (RemoteException e) {
        }
    }

    public void reject() {
        try {
            if (this.mConnected) {
                this.mConnectionService.reject(this.mConnectionId);
            }
        } catch (RemoteException e) {
        }
    }

    public void hold() {
        try {
            if (this.mConnected) {
                this.mConnectionService.hold(this.mConnectionId);
            }
        } catch (RemoteException e) {
        }
    }

    public void unhold() {
        try {
            if (this.mConnected) {
                this.mConnectionService.unhold(this.mConnectionId);
            }
        } catch (RemoteException e) {
        }
    }

    public void disconnect() {
        try {
            if (this.mConnected) {
                this.mConnectionService.disconnect(this.mConnectionId);
            }
        } catch (RemoteException e) {
        }
    }

    public void playDtmfTone(char digit) {
        try {
            if (this.mConnected) {
                this.mConnectionService.playDtmfTone(this.mConnectionId, digit);
            }
        } catch (RemoteException e) {
        }
    }

    public void stopDtmfTone() {
        try {
            if (this.mConnected) {
                this.mConnectionService.stopDtmfTone(this.mConnectionId);
            }
        } catch (RemoteException e) {
        }
    }

    public void postDialContinue(boolean proceed) {
        try {
            if (this.mConnected) {
                this.mConnectionService.onPostDialContinue(this.mConnectionId, proceed);
            }
        } catch (RemoteException e) {
        }
    }

    public void setAudioState(AudioState state) {
        try {
            if (this.mConnected) {
                this.mConnectionService.onAudioStateChanged(this.mConnectionId, state);
            }
        } catch (RemoteException e) {
        }
    }

    public List<RemoteConnection> getConferenceableConnections() {
        return this.mUnmodifiableconferenceableConnections;
    }

    public RemoteConference getConference() {
        return this.mConference;
    }

    /* access modifiers changed from: package-private */
    public String getId() {
        return this.mConnectionId;
    }

    /* access modifiers changed from: package-private */
    public IConnectionService getConnectionService() {
        return this.mConnectionService;
    }

    /* access modifiers changed from: package-private */
    public void setState(int state) {
        if (this.mState != state) {
            this.mState = state;
            for (Callback c : this.mCallbacks) {
                c.onStateChanged(this, state);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setDisconnected(DisconnectCause disconnectCause) {
        if (this.mState != 6) {
            this.mState = 6;
            this.mDisconnectCause = disconnectCause;
            for (Callback c : this.mCallbacks) {
                c.onDisconnected(this, this.mDisconnectCause);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setRingbackRequested(boolean ringback) {
        if (this.mRingbackRequested != ringback) {
            this.mRingbackRequested = ringback;
            for (Callback c : this.mCallbacks) {
                c.onRingbackRequested(this, ringback);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setCallCapabilities(int callCapabilities) {
        this.mCallCapabilities = callCapabilities;
        for (Callback c : this.mCallbacks) {
            c.onCallCapabilitiesChanged(this, callCapabilities);
        }
    }

    /* access modifiers changed from: package-private */
    public void setDestroyed() {
        if (!this.mCallbacks.isEmpty()) {
            if (this.mState != 6) {
                setDisconnected(new DisconnectCause(1, "Connection destroyed."));
            }
            for (Callback c : this.mCallbacks) {
                c.onDestroyed(this);
            }
            this.mCallbacks.clear();
            this.mConnected = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void setPostDialWait(String remainingDigits) {
        for (Callback c : this.mCallbacks) {
            c.onPostDialWait(this, remainingDigits);
        }
    }

    /* access modifiers changed from: package-private */
    public void setVideoState(int videoState) {
        this.mVideoState = videoState;
        for (Callback c : this.mCallbacks) {
            c.onVideoStateChanged(this, videoState);
        }
    }

    /* access modifiers changed from: package-private */
    public void setVideoProvider(VideoProvider videoProvider) {
        this.mVideoProvider = videoProvider;
        for (Callback c : this.mCallbacks) {
            c.onVideoProviderChanged(this, videoProvider);
        }
    }

    /* access modifiers changed from: package-private */
    public void setIsVoipAudioMode(boolean isVoip) {
        this.mIsVoipAudioMode = isVoip;
        for (Callback c : this.mCallbacks) {
            c.onVoipAudioChanged(this, isVoip);
        }
    }

    /* access modifiers changed from: package-private */
    public void setStatusHints(StatusHints statusHints) {
        this.mStatusHints = statusHints;
        for (Callback c : this.mCallbacks) {
            c.onStatusHintsChanged(this, statusHints);
        }
    }

    /* access modifiers changed from: package-private */
    public void setAddress(Uri address, int presentation) {
        this.mAddress = address;
        this.mAddressPresentation = presentation;
        for (Callback c : this.mCallbacks) {
            c.onAddressChanged(this, address, presentation);
        }
    }

    /* access modifiers changed from: package-private */
    public void setCallerDisplayName(String callerDisplayName, int presentation) {
        this.mCallerDisplayName = callerDisplayName;
        this.mCallerDisplayNamePresentation = presentation;
        for (Callback c : this.mCallbacks) {
            c.onCallerDisplayNameChanged(this, callerDisplayName, presentation);
        }
    }

    /* access modifiers changed from: package-private */
    public void setConferenceableConnections(List<RemoteConnection> conferenceableConnections) {
        this.mConferenceableConnections.clear();
        this.mConferenceableConnections.addAll(conferenceableConnections);
        for (Callback c : this.mCallbacks) {
            c.onConferenceableConnectionsChanged(this, this.mUnmodifiableconferenceableConnections);
        }
    }

    /* access modifiers changed from: package-private */
    public void setConference(RemoteConference conference) {
        if (this.mConference != conference) {
            this.mConference = conference;
            for (Callback c : this.mCallbacks) {
                c.onConferenceChanged(this, conference);
            }
        }
    }

    public static RemoteConnection failure(DisconnectCause disconnectCause) {
        return new RemoteConnection(disconnectCause);
    }
}
