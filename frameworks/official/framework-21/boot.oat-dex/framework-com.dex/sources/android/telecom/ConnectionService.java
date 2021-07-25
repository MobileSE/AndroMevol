package android.telecom;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.telecom.Conference;
import android.telecom.Connection;
import com.android.ims.ImsCallProfile;
import com.android.internal.os.SomeArgs;
import com.android.internal.telecom.IConnectionService;
import com.android.internal.telecom.IConnectionServiceAdapter;
import com.android.internal.telecom.IVideoProvider;
import com.android.internal.telecom.RemoteServiceCallback;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ConnectionService extends Service {
    private static final int MSG_ABORT = 3;
    private static final int MSG_ADD_CONNECTION_SERVICE_ADAPTER = 1;
    private static final int MSG_ANSWER = 4;
    private static final int MSG_ANSWER_VIDEO = 17;
    private static final int MSG_CONFERENCE = 12;
    private static final int MSG_CREATE_CONNECTION = 2;
    private static final int MSG_DISCONNECT = 6;
    private static final int MSG_HOLD = 7;
    private static final int MSG_MERGE_CONFERENCE = 18;
    private static final int MSG_ON_AUDIO_STATE_CHANGED = 9;
    private static final int MSG_ON_POST_DIAL_CONTINUE = 14;
    private static final int MSG_PLAY_DTMF_TONE = 10;
    private static final int MSG_REJECT = 5;
    private static final int MSG_REMOVE_CONNECTION_SERVICE_ADAPTER = 16;
    private static final int MSG_SPLIT_FROM_CONFERENCE = 13;
    private static final int MSG_STOP_DTMF_TONE = 11;
    private static final int MSG_SWAP_CONFERENCE = 19;
    private static final int MSG_UNHOLD = 8;
    private static final boolean PII_DEBUG = Log.isLoggable(3);
    public static final String SERVICE_INTERFACE = "android.telecom.ConnectionService";
    private static Connection sNullConnection;
    private final ConnectionServiceAdapter mAdapter = new ConnectionServiceAdapter();
    private boolean mAreAccountsInitialized = false;
    private final IBinder mBinder = new IConnectionService.Stub() {
        /* class android.telecom.ConnectionService.AnonymousClass1 */

        @Override // com.android.internal.telecom.IConnectionService
        public void addConnectionServiceAdapter(IConnectionServiceAdapter adapter) {
            ConnectionService.this.mHandler.obtainMessage(1, adapter).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void removeConnectionServiceAdapter(IConnectionServiceAdapter adapter) {
            ConnectionService.this.mHandler.obtainMessage(16, adapter).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void createConnection(PhoneAccountHandle connectionManagerPhoneAccount, String id, ConnectionRequest request, boolean isIncoming, boolean isUnknown) {
            int i;
            int i2 = 1;
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionManagerPhoneAccount;
            args.arg2 = id;
            args.arg3 = request;
            if (isIncoming) {
                i = 1;
            } else {
                i = 0;
            }
            args.argi1 = i;
            if (!isUnknown) {
                i2 = 0;
            }
            args.argi2 = i2;
            ConnectionService.this.mHandler.obtainMessage(2, args).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void abort(String callId) {
            ConnectionService.this.mHandler.obtainMessage(3, callId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void answerVideo(String callId, int videoState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = callId;
            args.argi1 = videoState;
            ConnectionService.this.mHandler.obtainMessage(17, args).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void answer(String callId) {
            ConnectionService.this.mHandler.obtainMessage(4, callId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void reject(String callId) {
            ConnectionService.this.mHandler.obtainMessage(5, callId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void disconnect(String callId) {
            ConnectionService.this.mHandler.obtainMessage(6, callId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void hold(String callId) {
            ConnectionService.this.mHandler.obtainMessage(7, callId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void unhold(String callId) {
            ConnectionService.this.mHandler.obtainMessage(8, callId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void onAudioStateChanged(String callId, AudioState audioState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = callId;
            args.arg2 = audioState;
            ConnectionService.this.mHandler.obtainMessage(9, args).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void playDtmfTone(String callId, char digit) {
            ConnectionService.this.mHandler.obtainMessage(10, digit, 0, callId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void stopDtmfTone(String callId) {
            ConnectionService.this.mHandler.obtainMessage(11, callId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void conference(String callId1, String callId2) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = callId1;
            args.arg2 = callId2;
            ConnectionService.this.mHandler.obtainMessage(12, args).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void splitFromConference(String callId) {
            ConnectionService.this.mHandler.obtainMessage(13, callId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void mergeConference(String callId) {
            ConnectionService.this.mHandler.obtainMessage(18, callId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void swapConference(String callId) {
            ConnectionService.this.mHandler.obtainMessage(19, callId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionService
        public void onPostDialContinue(String callId, boolean proceed) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = callId;
            args.argi1 = proceed ? 1 : 0;
            ConnectionService.this.mHandler.obtainMessage(14, args).sendToTarget();
        }
    };
    private final Map<String, Conference> mConferenceById = new ConcurrentHashMap();
    private final Conference.Listener mConferenceListener = new Conference.Listener() {
        /* class android.telecom.ConnectionService.AnonymousClass3 */

        @Override // android.telecom.Conference.Listener
        public void onStateChanged(Conference conference, int oldState, int newState) {
            String id = (String) ConnectionService.this.mIdByConference.get(conference);
            switch (newState) {
                case 4:
                    ConnectionService.this.mAdapter.setActive(id);
                    return;
                case 5:
                    ConnectionService.this.mAdapter.setOnHold(id);
                    return;
                default:
                    return;
            }
        }

        @Override // android.telecom.Conference.Listener
        public void onDisconnected(Conference conference, DisconnectCause disconnectCause) {
            ConnectionService.this.mAdapter.setDisconnected((String) ConnectionService.this.mIdByConference.get(conference), disconnectCause);
        }

        @Override // android.telecom.Conference.Listener
        public void onConnectionAdded(Conference conference, Connection connection) {
        }

        @Override // android.telecom.Conference.Listener
        public void onConnectionRemoved(Conference conference, Connection connection) {
        }

        @Override // android.telecom.Conference.Listener
        public void onConferenceableConnectionsChanged(Conference conference, List<Connection> conferenceableConnections) {
            ConnectionService.this.mAdapter.setConferenceableConnections((String) ConnectionService.this.mIdByConference.get(conference), ConnectionService.this.createConnectionIdList(conferenceableConnections));
        }

        @Override // android.telecom.Conference.Listener
        public void onDestroyed(Conference conference) {
            ConnectionService.this.removeConference(conference);
        }

        @Override // android.telecom.Conference.Listener
        public void onCapabilitiesChanged(Conference conference, int capabilities) {
            Log.d(this, "call capabilities: conference: %s", PhoneCapabilities.toString(capabilities));
            ConnectionService.this.mAdapter.setCallCapabilities((String) ConnectionService.this.mIdByConference.get(conference), capabilities);
        }
    };
    private final Map<String, Connection> mConnectionById = new ConcurrentHashMap();
    private final Connection.Listener mConnectionListener = new Connection.Listener() {
        /* class android.telecom.ConnectionService.AnonymousClass4 */

        @Override // android.telecom.Connection.Listener
        public void onStateChanged(Connection c, int state) {
            String id = (String) ConnectionService.this.mIdByConnection.get(c);
            Log.d(this, "Adapter set state %s %s", id, Connection.stateToString(state));
            switch (state) {
                case 1:
                case 6:
                default:
                    return;
                case 2:
                    ConnectionService.this.mAdapter.setRinging(id);
                    return;
                case 3:
                    ConnectionService.this.mAdapter.setDialing(id);
                    return;
                case 4:
                    ConnectionService.this.mAdapter.setActive(id);
                    return;
                case 5:
                    ConnectionService.this.mAdapter.setOnHold(id);
                    return;
            }
        }

        @Override // android.telecom.Connection.Listener
        public void onDisconnected(Connection c, DisconnectCause disconnectCause) {
            Log.d(this, "Adapter set disconnected %s", disconnectCause);
            ConnectionService.this.mAdapter.setDisconnected((String) ConnectionService.this.mIdByConnection.get(c), disconnectCause);
        }

        @Override // android.telecom.Connection.Listener
        public void onVideoStateChanged(Connection c, int videoState) {
            Log.d(this, "Adapter set video state %d", Integer.valueOf(videoState));
            ConnectionService.this.mAdapter.setVideoState((String) ConnectionService.this.mIdByConnection.get(c), videoState);
        }

        @Override // android.telecom.Connection.Listener
        public void onAddressChanged(Connection c, Uri address, int presentation) {
            ConnectionService.this.mAdapter.setAddress((String) ConnectionService.this.mIdByConnection.get(c), address, presentation);
        }

        @Override // android.telecom.Connection.Listener
        public void onCallerDisplayNameChanged(Connection c, String callerDisplayName, int presentation) {
            ConnectionService.this.mAdapter.setCallerDisplayName((String) ConnectionService.this.mIdByConnection.get(c), callerDisplayName, presentation);
        }

        @Override // android.telecom.Connection.Listener
        public void onDestroyed(Connection c) {
            ConnectionService.this.removeConnection(c);
        }

        @Override // android.telecom.Connection.Listener
        public void onPostDialWait(Connection c, String remaining) {
            Log.d(this, "Adapter onPostDialWait %s, %s", c, remaining);
            ConnectionService.this.mAdapter.onPostDialWait((String) ConnectionService.this.mIdByConnection.get(c), remaining);
        }

        @Override // android.telecom.Connection.Listener
        public void onRingbackRequested(Connection c, boolean ringback) {
            Log.d(this, "Adapter onRingback %b", Boolean.valueOf(ringback));
            ConnectionService.this.mAdapter.setRingbackRequested((String) ConnectionService.this.mIdByConnection.get(c), ringback);
        }

        @Override // android.telecom.Connection.Listener
        public void onCallCapabilitiesChanged(Connection c, int capabilities) {
            Log.d(this, "capabilities: parcelableconnection: %s", PhoneCapabilities.toString(capabilities));
            ConnectionService.this.mAdapter.setCallCapabilities((String) ConnectionService.this.mIdByConnection.get(c), capabilities);
        }

        @Override // android.telecom.Connection.Listener
        public void onVideoProviderChanged(Connection c, Connection.VideoProvider videoProvider) {
            ConnectionService.this.mAdapter.setVideoProvider((String) ConnectionService.this.mIdByConnection.get(c), videoProvider);
        }

        @Override // android.telecom.Connection.Listener
        public void onAudioModeIsVoipChanged(Connection c, boolean isVoip) {
            ConnectionService.this.mAdapter.setIsVoipAudioMode((String) ConnectionService.this.mIdByConnection.get(c), isVoip);
        }

        @Override // android.telecom.Connection.Listener
        public void onStatusHintsChanged(Connection c, StatusHints statusHints) {
            ConnectionService.this.mAdapter.setStatusHints((String) ConnectionService.this.mIdByConnection.get(c), statusHints);
        }

        @Override // android.telecom.Connection.Listener
        public void onConferenceableConnectionsChanged(Connection connection, List<Connection> conferenceableConnections) {
            ConnectionService.this.mAdapter.setConferenceableConnections((String) ConnectionService.this.mIdByConnection.get(connection), ConnectionService.this.createConnectionIdList(conferenceableConnections));
        }

        @Override // android.telecom.Connection.Listener
        public void onConferenceChanged(Connection connection, Conference conference) {
            String id = (String) ConnectionService.this.mIdByConnection.get(connection);
            if (id != null) {
                String conferenceId = null;
                if (conference != null) {
                    conferenceId = (String) ConnectionService.this.mIdByConference.get(conference);
                }
                ConnectionService.this.mAdapter.setIsConferenced(id, conferenceId);
            }
        }
    };
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        /* class android.telecom.ConnectionService.AnonymousClass2 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ConnectionService.this.mAdapter.addAdapter((IConnectionServiceAdapter) msg.obj);
                    ConnectionService.this.onAdapterAttached();
                    return;
                case 2:
                    SomeArgs args = (SomeArgs) msg.obj;
                    try {
                        final PhoneAccountHandle connectionManagerPhoneAccount = (PhoneAccountHandle) args.arg1;
                        final String id = (String) args.arg2;
                        final ConnectionRequest request = (ConnectionRequest) args.arg3;
                        final boolean isIncoming = args.argi1 == 1;
                        final boolean isUnknown = args.argi2 == 1;
                        if (!ConnectionService.this.mAreAccountsInitialized) {
                            Log.d(this, "Enqueueing pre-init request %s", id);
                            ConnectionService.this.mPreInitializationConnectionRequests.add(new Runnable() {
                                /* class android.telecom.ConnectionService.AnonymousClass2.AnonymousClass1 */

                                public void run() {
                                    ConnectionService.this.createConnection(connectionManagerPhoneAccount, id, request, isIncoming, isUnknown);
                                }
                            });
                        } else {
                            ConnectionService.this.createConnection(connectionManagerPhoneAccount, id, request, isIncoming, isUnknown);
                        }
                        return;
                    } finally {
                        args.recycle();
                    }
                case 3:
                    ConnectionService.this.abort((String) msg.obj);
                    return;
                case 4:
                    ConnectionService.this.answer((String) msg.obj);
                    return;
                case 5:
                    ConnectionService.this.reject((String) msg.obj);
                    return;
                case 6:
                    ConnectionService.this.disconnect((String) msg.obj);
                    return;
                case 7:
                    ConnectionService.this.hold((String) msg.obj);
                    return;
                case 8:
                    ConnectionService.this.unhold((String) msg.obj);
                    return;
                case 9:
                    SomeArgs args2 = (SomeArgs) msg.obj;
                    try {
                        ConnectionService.this.onAudioStateChanged((String) args2.arg1, (AudioState) args2.arg2);
                        return;
                    } finally {
                        args2.recycle();
                    }
                case 10:
                    ConnectionService.this.playDtmfTone((String) msg.obj, (char) msg.arg1);
                    return;
                case 11:
                    ConnectionService.this.stopDtmfTone((String) msg.obj);
                    return;
                case 12:
                    SomeArgs args3 = (SomeArgs) msg.obj;
                    try {
                        ConnectionService.this.conference((String) args3.arg1, (String) args3.arg2);
                        return;
                    } finally {
                        args3.recycle();
                    }
                case 13:
                    ConnectionService.this.splitFromConference((String) msg.obj);
                    return;
                case 14:
                    SomeArgs args4 = (SomeArgs) msg.obj;
                    try {
                        ConnectionService.this.onPostDialContinue((String) args4.arg1, args4.argi1 == 1);
                        return;
                    } finally {
                        args4.recycle();
                    }
                case 15:
                default:
                    return;
                case 16:
                    ConnectionService.this.mAdapter.removeAdapter((IConnectionServiceAdapter) msg.obj);
                    return;
                case 17:
                    SomeArgs args5 = (SomeArgs) msg.obj;
                    try {
                        ConnectionService.this.answerVideo((String) args5.arg1, args5.argi1);
                        return;
                    } finally {
                        args5.recycle();
                    }
                case 18:
                    ConnectionService.this.mergeConference((String) msg.obj);
                    return;
                case 19:
                    ConnectionService.this.swapConference((String) msg.obj);
                    return;
            }
        }
    };
    private final Map<Conference, String> mIdByConference = new ConcurrentHashMap();
    private final Map<Connection, String> mIdByConnection = new ConcurrentHashMap();
    private final List<Runnable> mPreInitializationConnectionRequests = new ArrayList();
    private final RemoteConnectionManager mRemoteConnectionManager = new RemoteConnectionManager(this);
    private Conference sNullConference;

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        endAllConnections();
        return super.onUnbind(intent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createConnection(PhoneAccountHandle callManagerAccount, String callId, ConnectionRequest request, boolean isIncoming, boolean isUnknown) {
        Connection connection;
        IVideoProvider iVideoProvider;
        Log.d(this, "createConnection, callManagerAccount: %s, callId: %s, request: %s, isIncoming: %b, isUnknown: %b", callManagerAccount, callId, request, Boolean.valueOf(isIncoming), Boolean.valueOf(isUnknown));
        if (isUnknown) {
            connection = onCreateUnknownConnection(callManagerAccount, request);
        } else {
            connection = isIncoming ? onCreateIncomingConnection(callManagerAccount, request) : onCreateOutgoingConnection(callManagerAccount, request);
        }
        Log.d(this, "createConnection, connection: %s", connection);
        if (connection == null) {
            connection = Connection.createFailedConnection(new DisconnectCause(1));
        }
        if (connection.getState() != 6) {
            addConnection(callId, connection);
        }
        Uri address = connection.getAddress();
        Log.v(this, "createConnection, number: %s, state: %s, capabilities: %s", Connection.toLogSafePhoneNumber(address == null ? "null" : address.getSchemeSpecificPart()), Connection.stateToString(connection.getState()), PhoneCapabilities.toString(connection.getCallCapabilities()));
        Log.d(this, "createConnection, calling handleCreateConnectionSuccessful %s", callId);
        ConnectionServiceAdapter connectionServiceAdapter = this.mAdapter;
        PhoneAccountHandle accountHandle = request.getAccountHandle();
        int state = connection.getState();
        int callCapabilities = connection.getCallCapabilities();
        Uri address2 = connection.getAddress();
        int addressPresentation = connection.getAddressPresentation();
        String callerDisplayName = connection.getCallerDisplayName();
        int callerDisplayNamePresentation = connection.getCallerDisplayNamePresentation();
        if (connection.getVideoProvider() == null) {
            iVideoProvider = null;
        } else {
            iVideoProvider = connection.getVideoProvider().getInterface();
        }
        connectionServiceAdapter.handleCreateConnectionComplete(callId, request, new ParcelableConnection(accountHandle, state, callCapabilities, address2, addressPresentation, callerDisplayName, callerDisplayNamePresentation, iVideoProvider, connection.getVideoState(), connection.isRingbackRequested(), connection.getAudioModeIsVoip(), connection.getStatusHints(), connection.getDisconnectCause(), createConnectionIdList(connection.getConferenceableConnections())));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void abort(String callId) {
        Log.d(this, "abort %s", callId);
        findConnectionForAction(callId, "abort").onAbort();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void answerVideo(String callId, int videoState) {
        Log.d(this, "answerVideo %s", callId);
        findConnectionForAction(callId, "answer").onAnswer(videoState);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void answer(String callId) {
        Log.d(this, "answer %s", callId);
        findConnectionForAction(callId, "answer").onAnswer();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reject(String callId) {
        Log.d(this, "reject %s", callId);
        findConnectionForAction(callId, "reject").onReject();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disconnect(String callId) {
        Log.d(this, "disconnect %s", callId);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "disconnect").onDisconnect();
        } else {
            findConferenceForAction(callId, "disconnect").onDisconnect();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hold(String callId) {
        Log.d(this, "hold %s", callId);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "hold").onHold();
        } else {
            findConferenceForAction(callId, "hold").onHold();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unhold(String callId) {
        Log.d(this, "unhold %s", callId);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "unhold").onUnhold();
        } else {
            findConferenceForAction(callId, "unhold").onUnhold();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onAudioStateChanged(String callId, AudioState audioState) {
        Log.d(this, "onAudioStateChanged %s %s", callId, audioState);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "onAudioStateChanged").setAudioState(audioState);
        } else {
            findConferenceForAction(callId, "onAudioStateChanged").setAudioState(audioState);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void playDtmfTone(String callId, char digit) {
        Log.d(this, "playDtmfTone %s %c", callId, Character.valueOf(digit));
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "playDtmfTone").onPlayDtmfTone(digit);
        } else {
            findConferenceForAction(callId, "playDtmfTone").onPlayDtmfTone(digit);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopDtmfTone(String callId) {
        Log.d(this, "stopDtmfTone %s", callId);
        if (this.mConnectionById.containsKey(callId)) {
            findConnectionForAction(callId, "stopDtmfTone").onStopDtmfTone();
        } else {
            findConferenceForAction(callId, "stopDtmfTone").onStopDtmfTone();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void conference(String callId1, String callId2) {
        Log.d(this, "conference %s, %s", callId1, callId2);
        Connection connection2 = findConnectionForAction(callId2, ImsCallProfile.EXTRA_CONFERENCE);
        if (connection2 == getNullConnection()) {
            Log.w(this, "Connection2 missing in conference request %s.", callId2);
            return;
        }
        Connection connection1 = findConnectionForAction(callId1, ImsCallProfile.EXTRA_CONFERENCE);
        if (connection1 == getNullConnection()) {
            Conference conference1 = findConferenceForAction(callId1, "addConnection");
            if (conference1 == getNullConference()) {
                Log.w(this, "Connection1 or Conference1 missing in conference request %s.", callId1);
                return;
            }
            conference1.onMerge(connection2);
            return;
        }
        onConference(connection1, connection2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void splitFromConference(String callId) {
        Log.d(this, "splitFromConference(%s)", callId);
        Connection connection = findConnectionForAction(callId, "splitFromConference");
        if (connection == getNullConnection()) {
            Log.w(this, "Connection missing in conference request %s.", callId);
            return;
        }
        Conference conference = connection.getConference();
        if (conference != null) {
            conference.onSeparate(connection);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void mergeConference(String callId) {
        Log.d(this, "mergeConference(%s)", callId);
        Conference conference = findConferenceForAction(callId, "mergeConference");
        if (conference != null) {
            conference.onMerge();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void swapConference(String callId) {
        Log.d(this, "swapConference(%s)", callId);
        Conference conference = findConferenceForAction(callId, "swapConference");
        if (conference != null) {
            conference.onSwap();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPostDialContinue(String callId, boolean proceed) {
        Log.d(this, "onPostDialContinue(%s)", callId);
        findConnectionForAction(callId, "stopDtmfTone").onPostDialContinue(proceed);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onAdapterAttached() {
        if (!this.mAreAccountsInitialized) {
            this.mAdapter.queryRemoteConnectionServices(new RemoteServiceCallback.Stub() {
                /* class android.telecom.ConnectionService.AnonymousClass5 */

                @Override // com.android.internal.telecom.RemoteServiceCallback
                public void onResult(final List<ComponentName> componentNames, final List<IBinder> services) {
                    ConnectionService.this.mHandler.post(new Runnable() {
                        /* class android.telecom.ConnectionService.AnonymousClass5.AnonymousClass1 */

                        public void run() {
                            int i = 0;
                            while (i < componentNames.size() && i < services.size()) {
                                ConnectionService.this.mRemoteConnectionManager.addConnectionService((ComponentName) componentNames.get(i), IConnectionService.Stub.asInterface((IBinder) services.get(i)));
                                i++;
                            }
                            ConnectionService.this.onAccountsInitialized();
                            Log.d(this, "remote connection services found: " + services, new Object[0]);
                        }
                    });
                }

                @Override // com.android.internal.telecom.RemoteServiceCallback
                public void onError() {
                    ConnectionService.this.mHandler.post(new Runnable() {
                        /* class android.telecom.ConnectionService.AnonymousClass5.AnonymousClass2 */

                        public void run() {
                            ConnectionService.this.mAreAccountsInitialized = true;
                        }
                    });
                }
            });
        }
    }

    public final RemoteConnection createRemoteIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return this.mRemoteConnectionManager.createRemoteConnection(connectionManagerPhoneAccount, request, true);
    }

    public final RemoteConnection createRemoteOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return this.mRemoteConnectionManager.createRemoteConnection(connectionManagerPhoneAccount, request, false);
    }

    public final void conferenceRemoteConnections(RemoteConnection a, RemoteConnection b) {
        this.mRemoteConnectionManager.conferenceRemoteConnections(a, b);
    }

    public final void addConference(Conference conference) {
        String id = addConferenceInternal(conference);
        if (id != null) {
            List<String> connectionIds = new ArrayList<>(2);
            for (Connection connection : conference.getConnections()) {
                if (this.mIdByConnection.containsKey(connection)) {
                    connectionIds.add(this.mIdByConnection.get(connection));
                }
            }
            this.mAdapter.addConferenceCall(id, new ParcelableConference(conference.getPhoneAccountHandle(), conference.getState(), conference.getCapabilities(), connectionIds));
            for (Connection connection2 : conference.getConnections()) {
                String connectionId = this.mIdByConnection.get(connection2);
                if (connectionId != null) {
                    this.mAdapter.setIsConferenced(connectionId, id);
                }
            }
        }
    }

    public final Collection<Connection> getAllConnections() {
        return this.mConnectionById.values();
    }

    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return null;
    }

    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return null;
    }

    public Connection onCreateUnknownConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        return null;
    }

    public void onConference(Connection connection1, Connection connection2) {
    }

    public void onRemoteConferenceAdded(RemoteConference conference) {
    }

    public boolean containsConference(Conference conference) {
        return this.mIdByConference.containsKey(conference);
    }

    /* access modifiers changed from: package-private */
    public void addRemoteConference(RemoteConference remoteConference) {
        onRemoteConferenceAdded(remoteConference);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onAccountsInitialized() {
        this.mAreAccountsInitialized = true;
        for (Runnable r : this.mPreInitializationConnectionRequests) {
            r.run();
        }
        this.mPreInitializationConnectionRequests.clear();
    }

    private void addConnection(String callId, Connection connection) {
        this.mConnectionById.put(callId, connection);
        this.mIdByConnection.put(connection, callId);
        connection.addConnectionListener(this.mConnectionListener);
        connection.setConnectionService(this);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeConnection(Connection connection) {
        connection.unsetConnectionService(this);
        connection.removeConnectionListener(this.mConnectionListener);
        this.mConnectionById.remove(this.mIdByConnection.get(connection));
        this.mIdByConnection.remove(connection);
        this.mAdapter.removeCall(this.mIdByConnection.get(connection));
    }

    private String addConferenceInternal(Conference conference) {
        if (this.mIdByConference.containsKey(conference)) {
            Log.w(this, "Re-adding an existing conference: %s.", conference);
        } else if (conference != null) {
            String id = UUID.randomUUID().toString();
            this.mConferenceById.put(id, conference);
            this.mIdByConference.put(conference, id);
            conference.addListener(this.mConferenceListener);
            return id;
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeConference(Conference conference) {
        if (this.mIdByConference.containsKey(conference)) {
            conference.removeListener(this.mConferenceListener);
            String id = this.mIdByConference.get(conference);
            this.mConferenceById.remove(id);
            this.mIdByConference.remove(conference);
            this.mAdapter.removeCall(id);
        }
    }

    private Connection findConnectionForAction(String callId, String action) {
        if (this.mConnectionById.containsKey(callId)) {
            return this.mConnectionById.get(callId);
        }
        Log.w(this, "%s - Cannot find Connection %s", action, callId);
        return getNullConnection();
    }

    static synchronized Connection getNullConnection() {
        Connection connection;
        synchronized (ConnectionService.class) {
            if (sNullConnection == null) {
                sNullConnection = new Connection() {
                    /* class android.telecom.ConnectionService.AnonymousClass6 */
                };
            }
            connection = sNullConnection;
        }
        return connection;
    }

    private Conference findConferenceForAction(String conferenceId, String action) {
        if (this.mConferenceById.containsKey(conferenceId)) {
            return this.mConferenceById.get(conferenceId);
        }
        Log.w(this, "%s - Cannot find conference %s", action, conferenceId);
        return getNullConference();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<String> createConnectionIdList(List<Connection> connections) {
        List<String> ids = new ArrayList<>();
        for (Connection c : connections) {
            if (this.mIdByConnection.containsKey(c)) {
                ids.add(this.mIdByConnection.get(c));
            }
        }
        Collections.sort(ids);
        return ids;
    }

    private Conference getNullConference() {
        if (this.sNullConference == null) {
            this.sNullConference = new Conference(null) {
                /* class android.telecom.ConnectionService.AnonymousClass7 */
            };
        }
        return this.sNullConference;
    }

    private void endAllConnections() {
        for (Connection connection : this.mIdByConnection.keySet()) {
            if (connection.getConference() == null) {
                connection.onDisconnect();
            }
        }
        for (Conference conference : this.mIdByConference.keySet()) {
            conference.onDisconnect();
        }
    }
}
