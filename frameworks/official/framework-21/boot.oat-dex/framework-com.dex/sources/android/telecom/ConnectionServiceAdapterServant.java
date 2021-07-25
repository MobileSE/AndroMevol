package android.telecom;

import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import com.android.internal.os.SomeArgs;
import com.android.internal.telecom.IConnectionServiceAdapter;
import com.android.internal.telecom.IVideoProvider;
import com.android.internal.telecom.RemoteServiceCallback;
import java.util.List;

/* access modifiers changed from: package-private */
public final class ConnectionServiceAdapterServant {
    private static final int MSG_ADD_CONFERENCE_CALL = 10;
    private static final int MSG_HANDLE_CREATE_CONNECTION_COMPLETE = 1;
    private static final int MSG_ON_POST_DIAL_WAIT = 12;
    private static final int MSG_QUERY_REMOTE_CALL_SERVICES = 13;
    private static final int MSG_REMOVE_CALL = 11;
    private static final int MSG_SET_ACTIVE = 2;
    private static final int MSG_SET_ADDRESS = 18;
    private static final int MSG_SET_CALLER_DISPLAY_NAME = 19;
    private static final int MSG_SET_CALL_CAPABILITIES = 8;
    private static final int MSG_SET_CONFERENCEABLE_CONNECTIONS = 20;
    private static final int MSG_SET_DIALING = 4;
    private static final int MSG_SET_DISCONNECTED = 5;
    private static final int MSG_SET_IS_CONFERENCED = 9;
    private static final int MSG_SET_IS_VOIP_AUDIO_MODE = 16;
    private static final int MSG_SET_ON_HOLD = 6;
    private static final int MSG_SET_RINGBACK_REQUESTED = 7;
    private static final int MSG_SET_RINGING = 3;
    private static final int MSG_SET_STATUS_HINTS = 17;
    private static final int MSG_SET_VIDEO_CALL_PROVIDER = 15;
    private static final int MSG_SET_VIDEO_STATE = 14;
    private final IConnectionServiceAdapter mDelegate;
    private final Handler mHandler = new Handler() {
        /* class android.telecom.ConnectionServiceAdapterServant.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                internalHandleMessage(msg);
            } catch (RemoteException e) {
            }
        }

        private void internalHandleMessage(Message msg) throws RemoteException {
            boolean z = true;
            switch (msg.what) {
                case 1:
                    SomeArgs args = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.handleCreateConnectionComplete((String) args.arg1, (ConnectionRequest) args.arg2, (ParcelableConnection) args.arg3);
                        return;
                    } finally {
                        args.recycle();
                    }
                case 2:
                    ConnectionServiceAdapterServant.this.mDelegate.setActive((String) msg.obj);
                    return;
                case 3:
                    ConnectionServiceAdapterServant.this.mDelegate.setRinging((String) msg.obj);
                    return;
                case 4:
                    ConnectionServiceAdapterServant.this.mDelegate.setDialing((String) msg.obj);
                    return;
                case 5:
                    SomeArgs args2 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.setDisconnected((String) args2.arg1, (DisconnectCause) args2.arg2);
                        return;
                    } finally {
                        args2.recycle();
                    }
                case 6:
                    ConnectionServiceAdapterServant.this.mDelegate.setOnHold((String) msg.obj);
                    return;
                case 7:
                    IConnectionServiceAdapter iConnectionServiceAdapter = ConnectionServiceAdapterServant.this.mDelegate;
                    String str = (String) msg.obj;
                    if (msg.arg1 != 1) {
                        z = false;
                    }
                    iConnectionServiceAdapter.setRingbackRequested(str, z);
                    return;
                case 8:
                    ConnectionServiceAdapterServant.this.mDelegate.setCallCapabilities((String) msg.obj, msg.arg1);
                    return;
                case 9:
                    SomeArgs args3 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.setIsConferenced((String) args3.arg1, (String) args3.arg2);
                        return;
                    } finally {
                        args3.recycle();
                    }
                case 10:
                    SomeArgs args4 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.addConferenceCall((String) args4.arg1, (ParcelableConference) args4.arg2);
                        return;
                    } finally {
                        args4.recycle();
                    }
                case 11:
                    ConnectionServiceAdapterServant.this.mDelegate.removeCall((String) msg.obj);
                    return;
                case 12:
                    SomeArgs args5 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.onPostDialWait((String) args5.arg1, (String) args5.arg2);
                        return;
                    } finally {
                        args5.recycle();
                    }
                case 13:
                    ConnectionServiceAdapterServant.this.mDelegate.queryRemoteConnectionServices((RemoteServiceCallback) msg.obj);
                    return;
                case 14:
                    ConnectionServiceAdapterServant.this.mDelegate.setVideoState((String) msg.obj, msg.arg1);
                    return;
                case 15:
                    SomeArgs args6 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.setVideoProvider((String) args6.arg1, (IVideoProvider) args6.arg2);
                        return;
                    } finally {
                        args6.recycle();
                    }
                case 16:
                    IConnectionServiceAdapter iConnectionServiceAdapter2 = ConnectionServiceAdapterServant.this.mDelegate;
                    String str2 = (String) msg.obj;
                    if (msg.arg1 != 1) {
                        z = false;
                    }
                    iConnectionServiceAdapter2.setIsVoipAudioMode(str2, z);
                    return;
                case 17:
                    SomeArgs args7 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.setStatusHints((String) args7.arg1, (StatusHints) args7.arg2);
                        return;
                    } finally {
                        args7.recycle();
                    }
                case 18:
                    SomeArgs args8 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.setAddress((String) args8.arg1, (Uri) args8.arg2, args8.argi1);
                        return;
                    } finally {
                        args8.recycle();
                    }
                case 19:
                    SomeArgs args9 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.setCallerDisplayName((String) args9.arg1, (String) args9.arg2, args9.argi1);
                        return;
                    } finally {
                        args9.recycle();
                    }
                case 20:
                    SomeArgs args10 = (SomeArgs) msg.obj;
                    try {
                        ConnectionServiceAdapterServant.this.mDelegate.setConferenceableConnections((String) args10.arg1, (List) args10.arg2);
                        return;
                    } finally {
                        args10.recycle();
                    }
                default:
                    return;
            }
        }
    };
    private final IConnectionServiceAdapter mStub = new IConnectionServiceAdapter.Stub() {
        /* class android.telecom.ConnectionServiceAdapterServant.AnonymousClass2 */

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void handleCreateConnectionComplete(String id, ConnectionRequest request, ParcelableConnection connection) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = id;
            args.arg2 = request;
            args.arg3 = connection;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(1, args).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setActive(String connectionId) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(2, connectionId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setRinging(String connectionId) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(3, connectionId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setDialing(String connectionId) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(4, connectionId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setDisconnected(String connectionId, DisconnectCause disconnectCause) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = disconnectCause;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(5, args).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setOnHold(String connectionId) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(6, connectionId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setRingbackRequested(String connectionId, boolean ringback) {
            int i;
            Handler handler = ConnectionServiceAdapterServant.this.mHandler;
            if (ringback) {
                i = 1;
            } else {
                i = 0;
            }
            handler.obtainMessage(7, i, 0, connectionId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setCallCapabilities(String connectionId, int callCapabilities) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(8, callCapabilities, 0, connectionId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setIsConferenced(String callId, String conferenceCallId) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = callId;
            args.arg2 = conferenceCallId;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(9, args).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void addConferenceCall(String callId, ParcelableConference parcelableConference) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = callId;
            args.arg2 = parcelableConference;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(10, args).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void removeCall(String connectionId) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(11, connectionId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void onPostDialWait(String connectionId, String remainingDigits) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = remainingDigits;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(12, args).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void queryRemoteConnectionServices(RemoteServiceCallback callback) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(13, callback).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setVideoState(String connectionId, int videoState) {
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(14, videoState, 0, connectionId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public void setVideoProvider(String connectionId, IVideoProvider videoProvider) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = videoProvider;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(15, args).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public final void setIsVoipAudioMode(String connectionId, boolean isVoip) {
            int i;
            Handler handler = ConnectionServiceAdapterServant.this.mHandler;
            if (isVoip) {
                i = 1;
            } else {
                i = 0;
            }
            handler.obtainMessage(16, i, 0, connectionId).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public final void setStatusHints(String connectionId, StatusHints statusHints) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = statusHints;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(17, args).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public final void setAddress(String connectionId, Uri address, int presentation) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = address;
            args.argi1 = presentation;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(18, args).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public final void setCallerDisplayName(String connectionId, String callerDisplayName, int presentation) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = callerDisplayName;
            args.argi1 = presentation;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(19, args).sendToTarget();
        }

        @Override // com.android.internal.telecom.IConnectionServiceAdapter
        public final void setConferenceableConnections(String connectionId, List<String> conferenceableConnectionIds) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = connectionId;
            args.arg2 = conferenceableConnectionIds;
            ConnectionServiceAdapterServant.this.mHandler.obtainMessage(20, args).sendToTarget();
        }
    };

    public ConnectionServiceAdapterServant(IConnectionServiceAdapter delegate) {
        this.mDelegate = delegate;
    }

    public IConnectionServiceAdapter getStub() {
        return this.mStub;
    }
}
