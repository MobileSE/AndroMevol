package android.telecom;

import android.net.Uri;
import android.os.Bundle;
import android.telecom.InCallService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Call {
    public static final String AVAILABLE_PHONE_ACCOUNTS = "selectPhoneAccountAccounts";
    public static final int STATE_ACTIVE = 4;
    public static final int STATE_CONNECTING = 9;
    public static final int STATE_DIALING = 1;
    public static final int STATE_DISCONNECTED = 7;
    public static final int STATE_DISCONNECTING = 10;
    public static final int STATE_HOLDING = 3;
    public static final int STATE_NEW = 0;
    public static final int STATE_PRE_DIAL_WAIT = 8;
    public static final int STATE_RINGING = 2;
    private List<String> mCannedTextResponses = null;
    private final List<Call> mChildren = new ArrayList();
    private boolean mChildrenCached;
    private final List<String> mChildrenIds = new ArrayList();
    private final List<Call> mConferenceableCalls = new ArrayList();
    private Details mDetails;
    private final InCallAdapter mInCallAdapter;
    private final List<Listener> mListeners = new CopyOnWriteArrayList();
    private String mParentId = null;
    private final Phone mPhone;
    private String mRemainingPostDialSequence;
    private int mState;
    private final String mTelecomCallId;
    private final List<Call> mUnmodifiableChildren = Collections.unmodifiableList(this.mChildren);
    private final List<Call> mUnmodifiableConferenceableCalls = Collections.unmodifiableList(this.mConferenceableCalls);
    private InCallService.VideoCall mVideoCall;

    public static class Details {
        private final PhoneAccountHandle mAccountHandle;
        private final int mCallCapabilities;
        private final int mCallProperties;
        private final String mCallerDisplayName;
        private final int mCallerDisplayNamePresentation;
        private final long mConnectTimeMillis;
        private final DisconnectCause mDisconnectCause;
        private final Bundle mExtras;
        private final GatewayInfo mGatewayInfo;
        private final Uri mHandle;
        private final int mHandlePresentation;
        private final StatusHints mStatusHints;
        private final int mVideoState;

        public Uri getHandle() {
            return this.mHandle;
        }

        public int getHandlePresentation() {
            return this.mHandlePresentation;
        }

        public String getCallerDisplayName() {
            return this.mCallerDisplayName;
        }

        public int getCallerDisplayNamePresentation() {
            return this.mCallerDisplayNamePresentation;
        }

        public PhoneAccountHandle getAccountHandle() {
            return this.mAccountHandle;
        }

        public int getCallCapabilities() {
            return this.mCallCapabilities;
        }

        public int getCallProperties() {
            return this.mCallProperties;
        }

        public DisconnectCause getDisconnectCause() {
            return this.mDisconnectCause;
        }

        public long getConnectTimeMillis() {
            return this.mConnectTimeMillis;
        }

        public GatewayInfo getGatewayInfo() {
            return this.mGatewayInfo;
        }

        public int getVideoState() {
            return this.mVideoState;
        }

        public StatusHints getStatusHints() {
            return this.mStatusHints;
        }

        public Bundle getExtras() {
            return this.mExtras;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Details)) {
                return false;
            }
            Details d = (Details) o;
            if (!Objects.equals(this.mHandle, d.mHandle) || !Objects.equals(Integer.valueOf(this.mHandlePresentation), Integer.valueOf(d.mHandlePresentation)) || !Objects.equals(this.mCallerDisplayName, d.mCallerDisplayName) || !Objects.equals(Integer.valueOf(this.mCallerDisplayNamePresentation), Integer.valueOf(d.mCallerDisplayNamePresentation)) || !Objects.equals(this.mAccountHandle, d.mAccountHandle) || !Objects.equals(Integer.valueOf(this.mCallCapabilities), Integer.valueOf(d.mCallCapabilities)) || !Objects.equals(Integer.valueOf(this.mCallProperties), Integer.valueOf(d.mCallProperties)) || !Objects.equals(this.mDisconnectCause, d.mDisconnectCause) || !Objects.equals(Long.valueOf(this.mConnectTimeMillis), Long.valueOf(d.mConnectTimeMillis)) || !Objects.equals(this.mGatewayInfo, d.mGatewayInfo) || !Objects.equals(Integer.valueOf(this.mVideoState), Integer.valueOf(d.mVideoState)) || !Objects.equals(this.mStatusHints, d.mStatusHints) || !Objects.equals(this.mExtras, d.mExtras)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hashCode(this.mHandle) + Objects.hashCode(Integer.valueOf(this.mHandlePresentation)) + Objects.hashCode(this.mCallerDisplayName) + Objects.hashCode(Integer.valueOf(this.mCallerDisplayNamePresentation)) + Objects.hashCode(this.mAccountHandle) + Objects.hashCode(Integer.valueOf(this.mCallCapabilities)) + Objects.hashCode(Integer.valueOf(this.mCallProperties)) + Objects.hashCode(this.mDisconnectCause) + Objects.hashCode(Long.valueOf(this.mConnectTimeMillis)) + Objects.hashCode(this.mGatewayInfo) + Objects.hashCode(Integer.valueOf(this.mVideoState)) + Objects.hashCode(this.mStatusHints) + Objects.hashCode(this.mExtras);
        }

        public Details(Uri handle, int handlePresentation, String callerDisplayName, int callerDisplayNamePresentation, PhoneAccountHandle accountHandle, int capabilities, int properties, DisconnectCause disconnectCause, long connectTimeMillis, GatewayInfo gatewayInfo, int videoState, StatusHints statusHints, Bundle extras) {
            this.mHandle = handle;
            this.mHandlePresentation = handlePresentation;
            this.mCallerDisplayName = callerDisplayName;
            this.mCallerDisplayNamePresentation = callerDisplayNamePresentation;
            this.mAccountHandle = accountHandle;
            this.mCallCapabilities = capabilities;
            this.mCallProperties = properties;
            this.mDisconnectCause = disconnectCause;
            this.mConnectTimeMillis = connectTimeMillis;
            this.mGatewayInfo = gatewayInfo;
            this.mVideoState = videoState;
            this.mStatusHints = statusHints;
            this.mExtras = extras;
        }
    }

    public static abstract class Listener {
        public void onStateChanged(Call call, int state) {
        }

        public void onParentChanged(Call call, Call parent) {
        }

        public void onChildrenChanged(Call call, List<Call> list) {
        }

        public void onDetailsChanged(Call call, Details details) {
        }

        public void onCannedTextResponsesLoaded(Call call, List<String> list) {
        }

        public void onPostDialWait(Call call, String remainingPostDialSequence) {
        }

        public void onVideoCallChanged(Call call, InCallService.VideoCall videoCall) {
        }

        public void onCallDestroyed(Call call) {
        }

        public void onConferenceableCallsChanged(Call call, List<Call> list) {
        }
    }

    public String getRemainingPostDialSequence() {
        return this.mRemainingPostDialSequence;
    }

    public void answer(int videoState) {
        this.mInCallAdapter.answerCall(this.mTelecomCallId, videoState);
    }

    public void reject(boolean rejectWithMessage, String textMessage) {
        this.mInCallAdapter.rejectCall(this.mTelecomCallId, rejectWithMessage, textMessage);
    }

    public void disconnect() {
        this.mInCallAdapter.disconnectCall(this.mTelecomCallId);
    }

    public void hold() {
        this.mInCallAdapter.holdCall(this.mTelecomCallId);
    }

    public void unhold() {
        this.mInCallAdapter.unholdCall(this.mTelecomCallId);
    }

    public void playDtmfTone(char digit) {
        this.mInCallAdapter.playDtmfTone(this.mTelecomCallId, digit);
    }

    public void stopDtmfTone() {
        this.mInCallAdapter.stopDtmfTone(this.mTelecomCallId);
    }

    public void postDialContinue(boolean proceed) {
        this.mInCallAdapter.postDialContinue(this.mTelecomCallId, proceed);
    }

    public void phoneAccountSelected(PhoneAccountHandle accountHandle) {
        this.mInCallAdapter.phoneAccountSelected(this.mTelecomCallId, accountHandle);
    }

    public void conference(Call callToConferenceWith) {
        if (callToConferenceWith != null) {
            this.mInCallAdapter.conference(this.mTelecomCallId, callToConferenceWith.mTelecomCallId);
        }
    }

    public void splitFromConference() {
        this.mInCallAdapter.splitFromConference(this.mTelecomCallId);
    }

    public void mergeConference() {
        this.mInCallAdapter.mergeConference(this.mTelecomCallId);
    }

    public void swapConference() {
        this.mInCallAdapter.swapConference(this.mTelecomCallId);
    }

    public Call getParent() {
        if (this.mParentId != null) {
            return this.mPhone.internalGetCallByTelecomId(this.mParentId);
        }
        return null;
    }

    public List<Call> getChildren() {
        if (!this.mChildrenCached) {
            this.mChildrenCached = true;
            this.mChildren.clear();
            for (String id : this.mChildrenIds) {
                Call call = this.mPhone.internalGetCallByTelecomId(id);
                if (call == null) {
                    this.mChildrenCached = false;
                } else {
                    this.mChildren.add(call);
                }
            }
        }
        return this.mUnmodifiableChildren;
    }

    public List<Call> getConferenceableCalls() {
        return this.mUnmodifiableConferenceableCalls;
    }

    public int getState() {
        return this.mState;
    }

    public List<String> getCannedTextResponses() {
        return this.mCannedTextResponses;
    }

    public InCallService.VideoCall getVideoCall() {
        return this.mVideoCall;
    }

    public Details getDetails() {
        return this.mDetails;
    }

    public void addListener(Listener listener) {
        this.mListeners.add(listener);
    }

    public void removeListener(Listener listener) {
        if (listener != null) {
            this.mListeners.remove(listener);
        }
    }

    Call(Phone phone, String telecomCallId, InCallAdapter inCallAdapter) {
        this.mPhone = phone;
        this.mTelecomCallId = telecomCallId;
        this.mInCallAdapter = inCallAdapter;
        this.mState = 0;
    }

    /* access modifiers changed from: package-private */
    public final String internalGetCallId() {
        return this.mTelecomCallId;
    }

    /* access modifiers changed from: package-private */
    public final void internalUpdate(ParcelableCall parcelableCall, Map<String, Call> callIdMap) {
        Details details = new Details(parcelableCall.getHandle(), parcelableCall.getHandlePresentation(), parcelableCall.getCallerDisplayName(), parcelableCall.getCallerDisplayNamePresentation(), parcelableCall.getAccountHandle(), parcelableCall.getCapabilities(), parcelableCall.getProperties(), parcelableCall.getDisconnectCause(), parcelableCall.getConnectTimeMillis(), parcelableCall.getGatewayInfo(), parcelableCall.getVideoState(), parcelableCall.getStatusHints(), parcelableCall.getExtras());
        boolean detailsChanged = !Objects.equals(this.mDetails, details);
        if (detailsChanged) {
            this.mDetails = details;
        }
        if (this.mCannedTextResponses == null && parcelableCall.getCannedSmsResponses() != null && !parcelableCall.getCannedSmsResponses().isEmpty()) {
            this.mCannedTextResponses = Collections.unmodifiableList(parcelableCall.getCannedSmsResponses());
        }
        boolean videoCallChanged = !Objects.equals(this.mVideoCall, parcelableCall.getVideoCall());
        if (videoCallChanged) {
            this.mVideoCall = parcelableCall.getVideoCall();
        }
        int state = stateFromParcelableCallState(parcelableCall.getState());
        boolean stateChanged = this.mState != state;
        if (stateChanged) {
            this.mState = state;
        }
        String parentId = parcelableCall.getParentCallId();
        boolean parentChanged = !Objects.equals(this.mParentId, parentId);
        if (parentChanged) {
            this.mParentId = parentId;
        }
        boolean childrenChanged = !Objects.equals(parcelableCall.getChildCallIds(), this.mChildrenIds);
        if (childrenChanged) {
            this.mChildrenIds.clear();
            this.mChildrenIds.addAll(parcelableCall.getChildCallIds());
            this.mChildrenCached = false;
        }
        List<String> conferenceableCallIds = parcelableCall.getConferenceableCallIds();
        List<Call> conferenceableCalls = new ArrayList<>(conferenceableCallIds.size());
        for (String otherId : conferenceableCallIds) {
            if (callIdMap.containsKey(otherId)) {
                conferenceableCalls.add(callIdMap.get(otherId));
            }
        }
        if (!Objects.equals(this.mConferenceableCalls, conferenceableCalls)) {
            this.mConferenceableCalls.clear();
            this.mConferenceableCalls.addAll(conferenceableCalls);
            fireConferenceableCallsChanged();
        }
        if (stateChanged) {
            fireStateChanged(this.mState);
        }
        if (detailsChanged) {
            fireDetailsChanged(this.mDetails);
        }
        if (0 != 0) {
            fireCannedTextResponsesLoaded(this.mCannedTextResponses);
        }
        if (videoCallChanged) {
            fireVideoCallChanged(this.mVideoCall);
        }
        if (parentChanged) {
            fireParentChanged(getParent());
        }
        if (childrenChanged) {
            fireChildrenChanged(getChildren());
        }
        if (this.mState == 7) {
            fireCallDestroyed();
            this.mPhone.internalRemoveCall(this);
        }
    }

    /* access modifiers changed from: package-private */
    public final void internalSetPostDialWait(String remaining) {
        this.mRemainingPostDialSequence = remaining;
        firePostDialWait(this.mRemainingPostDialSequence);
    }

    /* access modifiers changed from: package-private */
    public final void internalSetDisconnected() {
        if (this.mState != 7) {
            this.mState = 7;
            fireStateChanged(this.mState);
            fireCallDestroyed();
            this.mPhone.internalRemoveCall(this);
        }
    }

    private void fireStateChanged(int newState) {
        for (Listener listener : this.mListeners) {
            listener.onStateChanged(this, newState);
        }
    }

    private void fireParentChanged(Call newParent) {
        for (Listener listener : this.mListeners) {
            listener.onParentChanged(this, newParent);
        }
    }

    private void fireChildrenChanged(List<Call> children) {
        for (Listener listener : this.mListeners) {
            listener.onChildrenChanged(this, children);
        }
    }

    private void fireDetailsChanged(Details details) {
        for (Listener listener : this.mListeners) {
            listener.onDetailsChanged(this, details);
        }
    }

    private void fireCannedTextResponsesLoaded(List<String> cannedTextResponses) {
        for (Listener listener : this.mListeners) {
            listener.onCannedTextResponsesLoaded(this, cannedTextResponses);
        }
    }

    private void fireVideoCallChanged(InCallService.VideoCall videoCall) {
        for (Listener listener : this.mListeners) {
            listener.onVideoCallChanged(this, videoCall);
        }
    }

    private void firePostDialWait(String remainingPostDialSequence) {
        for (Listener listener : this.mListeners) {
            listener.onPostDialWait(this, remainingPostDialSequence);
        }
    }

    private void fireCallDestroyed() {
        for (Listener listener : this.mListeners) {
            listener.onCallDestroyed(this);
        }
    }

    private void fireConferenceableCallsChanged() {
        for (Listener listener : this.mListeners) {
            listener.onConferenceableCallsChanged(this, this.mUnmodifiableConferenceableCalls);
        }
    }

    private int stateFromParcelableCallState(int parcelableCallState) {
        switch (parcelableCallState) {
            case 0:
                return 0;
            case 1:
                return 9;
            case 2:
                return 8;
            case 3:
                return 1;
            case 4:
                return 2;
            case 5:
                return 4;
            case 6:
                return 3;
            case 7:
                return 7;
            case 8:
                return 7;
            case 9:
                return 10;
            default:
                Log.wtf(this, "Unrecognized CallState %s", Integer.valueOf(parcelableCallState));
                return 0;
        }
    }
}
