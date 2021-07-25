package android.view.inputmethod;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.Trace;
import android.text.style.SuggestionSpan;
import android.util.Log;
import android.util.Pools;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.SparseArray;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventSender;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewRootImpl;
import com.android.internal.os.SomeArgs;
import com.android.internal.view.IInputConnectionWrapper;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodManager;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.InputBindResult;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class InputMethodManager {
    public static final int CONTROL_START_INITIAL = 256;
    public static final int CONTROL_WINDOW_FIRST = 4;
    public static final int CONTROL_WINDOW_IS_TEXT_EDITOR = 2;
    public static final int CONTROL_WINDOW_VIEW_HAS_FOCUS = 1;
    static final boolean DEBUG = false;
    public static final int DISPATCH_HANDLED = 1;
    public static final int DISPATCH_IN_PROGRESS = -1;
    public static final int DISPATCH_NOT_HANDLED = 0;
    public static final int HIDE_IMPLICIT_ONLY = 1;
    public static final int HIDE_NOT_ALWAYS = 2;
    static final long INPUT_METHOD_NOT_RESPONDING_TIMEOUT = 2500;
    static final int MSG_BIND = 2;
    static final int MSG_DUMP = 1;
    static final int MSG_FLUSH_INPUT_EVENT = 7;
    static final int MSG_SEND_INPUT_EVENT = 5;
    static final int MSG_SET_ACTIVE = 4;
    static final int MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER = 9;
    static final int MSG_TIMEOUT_INPUT_EVENT = 6;
    static final int MSG_UNBIND = 3;
    private static final int NOT_AN_ACTION_NOTIFICATION_SEQUENCE_NUMBER = -1;
    static final String PENDING_EVENT_COUNTER = "aq:imm";
    private static final int REQUEST_UPDATE_CURSOR_ANCHOR_INFO_NONE = 0;
    public static final int RESULT_HIDDEN = 3;
    public static final int RESULT_SHOWN = 2;
    public static final int RESULT_UNCHANGED_HIDDEN = 1;
    public static final int RESULT_UNCHANGED_SHOWN = 0;
    public static final int SHOW_FORCED = 2;
    public static final int SHOW_IMPLICIT = 1;
    static final String TAG = "InputMethodManager";
    static InputMethodManager sInstance;
    boolean mActive = false;
    int mBindSequence = -1;
    final IInputMethodClient.Stub mClient = new IInputMethodClient.Stub() {
        /* class android.view.inputmethod.InputMethodManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
            CountDownLatch latch = new CountDownLatch(1);
            SomeArgs sargs = SomeArgs.obtain();
            sargs.arg1 = fd;
            sargs.arg2 = fout;
            sargs.arg3 = args;
            sargs.arg4 = latch;
            InputMethodManager.this.mH.sendMessage(InputMethodManager.this.mH.obtainMessage(1, sargs));
            try {
                if (!latch.await(5, TimeUnit.SECONDS)) {
                    fout.println("Timeout waiting for dump");
                }
            } catch (InterruptedException e) {
                fout.println("Interrupted waiting for dump");
            }
        }

        public void setUsingInputMethod(boolean state) {
        }

        public void onBindMethod(InputBindResult res) {
            InputMethodManager.this.mH.sendMessage(InputMethodManager.this.mH.obtainMessage(2, res));
        }

        public void onUnbindMethod(int sequence) {
            InputMethodManager.this.mH.sendMessage(InputMethodManager.this.mH.obtainMessage(3, sequence, 0));
        }

        public void setActive(boolean active) {
            int i;
            H h = InputMethodManager.this.mH;
            H h2 = InputMethodManager.this.mH;
            if (active) {
                i = 1;
            } else {
                i = 0;
            }
            h.sendMessage(h2.obtainMessage(4, i, 0));
        }

        public void setUserActionNotificationSequenceNumber(int sequenceNumber) {
            InputMethodManager.this.mH.sendMessage(InputMethodManager.this.mH.obtainMessage(9, sequenceNumber, 0));
        }
    };
    CompletionInfo[] mCompletions;
    InputChannel mCurChannel;
    String mCurId;
    IInputMethodSession mCurMethod;
    View mCurRootView;
    ImeInputEventSender mCurSender;
    EditorInfo mCurrentTextBoxAttribute;
    private CursorAnchorInfo mCursorAnchorInfo = null;
    int mCursorCandEnd;
    int mCursorCandStart;
    Rect mCursorRect = new Rect();
    int mCursorSelEnd;
    int mCursorSelStart;
    final InputConnection mDummyInputConnection = new BaseInputConnection(this, false);
    boolean mFullscreenMode;
    final H mH;
    boolean mHasBeenInactive = true;
    final IInputContext mIInputContext;
    private int mLastSentUserActionNotificationSequenceNumber = -1;
    final Looper mMainLooper;
    View mNextServedView;
    private int mNextUserActionNotificationSequenceNumber = -1;
    final Pools.Pool<PendingEvent> mPendingEventPool = new Pools.SimplePool(20);
    final SparseArray<PendingEvent> mPendingEvents = new SparseArray<>(20);
    private int mRequestUpdateCursorAnchorInfoMonitorMode = 0;
    boolean mServedConnecting;
    InputConnection mServedInputConnection;
    ControlledInputConnectionWrapper mServedInputConnectionWrapper;
    View mServedView;
    final IInputMethodManager mService;
    Rect mTmpCursorRect = new Rect();
    private final Matrix mViewToScreenMatrix = new Matrix();
    private final int[] mViewTopLeft = new int[2];

    public interface FinishedInputEventCallback {
        void onFinishedInputEvent(Object obj, boolean z);
    }

    /* access modifiers changed from: package-private */
    public class H extends Handler {
        H(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            boolean active = true;
            switch (msg.what) {
                case 1:
                    SomeArgs args = (SomeArgs) msg.obj;
                    try {
                        InputMethodManager.this.doDump((FileDescriptor) args.arg1, (PrintWriter) args.arg2, (String[]) args.arg3);
                    } catch (RuntimeException e) {
                        ((PrintWriter) args.arg2).println("Exception: " + e);
                    }
                    synchronized (args.arg4) {
                        ((CountDownLatch) args.arg4).countDown();
                    }
                    args.recycle();
                    return;
                case 2:
                    InputBindResult res = (InputBindResult) msg.obj;
                    synchronized (InputMethodManager.this.mH) {
                        if (InputMethodManager.this.mBindSequence < 0 || InputMethodManager.this.mBindSequence != res.sequence) {
                            Log.w(InputMethodManager.TAG, "Ignoring onBind: cur seq=" + InputMethodManager.this.mBindSequence + ", given seq=" + res.sequence);
                            if (!(res.channel == null || res.channel == InputMethodManager.this.mCurChannel)) {
                                res.channel.dispose();
                            }
                            return;
                        }
                        InputMethodManager.this.mRequestUpdateCursorAnchorInfoMonitorMode = 0;
                        InputMethodManager.this.setInputChannelLocked(res.channel);
                        InputMethodManager.this.mCurMethod = res.method;
                        InputMethodManager.this.mCurId = res.id;
                        InputMethodManager.this.mBindSequence = res.sequence;
                        InputMethodManager.this.startInputInner(null, 0, 0, 0);
                        return;
                    }
                case 3:
                    int sequence = msg.arg1;
                    boolean startInput = false;
                    synchronized (InputMethodManager.this.mH) {
                        if (InputMethodManager.this.mBindSequence == sequence) {
                            InputMethodManager.this.clearBindingLocked();
                            if (InputMethodManager.this.mServedView != null && InputMethodManager.this.mServedView.isFocused()) {
                                InputMethodManager.this.mServedConnecting = true;
                            }
                            if (InputMethodManager.this.mActive) {
                                startInput = true;
                            }
                        }
                    }
                    if (startInput) {
                        InputMethodManager.this.startInputInner(null, 0, 0, 0);
                        return;
                    }
                    return;
                case 4:
                    if (msg.arg1 == 0) {
                        active = false;
                    }
                    synchronized (InputMethodManager.this.mH) {
                        InputMethodManager.this.mActive = active;
                        InputMethodManager.this.mFullscreenMode = false;
                        if (!active) {
                            InputMethodManager.this.mHasBeenInactive = true;
                            try {
                                InputMethodManager.this.mIInputContext.finishComposingText();
                            } catch (RemoteException e2) {
                            }
                            if (InputMethodManager.this.mServedView != null && InputMethodManager.this.mServedView.hasWindowFocus() && InputMethodManager.this.checkFocusNoStartInput(InputMethodManager.this.mHasBeenInactive, false)) {
                                InputMethodManager.this.startInputInner(null, 0, 0, 0);
                            }
                        }
                    }
                    return;
                case 5:
                    InputMethodManager.this.sendInputEventAndReportResultOnMainLooper((PendingEvent) msg.obj);
                    return;
                case 6:
                    InputMethodManager.this.finishedInputEvent(msg.arg1, false, true);
                    return;
                case 7:
                    InputMethodManager.this.finishedInputEvent(msg.arg1, false, false);
                    return;
                case 8:
                default:
                    return;
                case 9:
                    synchronized (InputMethodManager.this.mH) {
                        InputMethodManager.this.mNextUserActionNotificationSequenceNumber = msg.arg1;
                    }
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class ControlledInputConnectionWrapper extends IInputConnectionWrapper {
        private boolean mActive = true;
        private final InputMethodManager mParentInputMethodManager;

        public ControlledInputConnectionWrapper(Looper mainLooper, InputConnection conn, InputMethodManager inputMethodManager) {
            super(mainLooper, conn);
            this.mParentInputMethodManager = inputMethodManager;
        }

        public boolean isActive() {
            return this.mParentInputMethodManager.mActive && this.mActive;
        }

        /* access modifiers changed from: package-private */
        public void deactivate() {
            this.mActive = false;
        }
    }

    InputMethodManager(IInputMethodManager service, Looper looper) {
        this.mService = service;
        this.mMainLooper = looper;
        this.mH = new H(looper);
        this.mIInputContext = new ControlledInputConnectionWrapper(looper, this.mDummyInputConnection, this);
    }

    public static InputMethodManager getInstance() {
        InputMethodManager inputMethodManager;
        synchronized (InputMethodManager.class) {
            if (sInstance == null) {
                sInstance = new InputMethodManager(IInputMethodManager.Stub.asInterface(ServiceManager.getService(Context.INPUT_METHOD_SERVICE)), Looper.getMainLooper());
            }
            inputMethodManager = sInstance;
        }
        return inputMethodManager;
    }

    public static InputMethodManager peekInstance() {
        return sInstance;
    }

    public IInputMethodClient getClient() {
        return this.mClient;
    }

    public IInputContext getInputContext() {
        return this.mIInputContext;
    }

    public List<InputMethodInfo> getInputMethodList() {
        try {
            return this.mService.getInputMethodList();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public List<InputMethodInfo> getEnabledInputMethodList() {
        try {
            return this.mService.getEnabledInputMethodList();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public List<InputMethodSubtype> getEnabledInputMethodSubtypeList(InputMethodInfo imi, boolean allowsImplicitlySelectedSubtypes) {
        try {
            return this.mService.getEnabledInputMethodSubtypeList(imi == null ? null : imi.getId(), allowsImplicitlySelectedSubtypes);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void showStatusIcon(IBinder imeToken, String packageName, int iconId) {
        try {
            this.mService.updateStatusIcon(imeToken, packageName, iconId);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void hideStatusIcon(IBinder imeToken) {
        try {
            this.mService.updateStatusIcon(imeToken, (String) null, 0);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void setImeWindowStatus(IBinder imeToken, int vis, int backDisposition) {
        try {
            this.mService.setImeWindowStatus(imeToken, vis, backDisposition);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFullscreenMode(boolean fullScreen) {
        this.mFullscreenMode = fullScreen;
    }

    public void registerSuggestionSpansForNotification(SuggestionSpan[] spans) {
        try {
            this.mService.registerSuggestionSpansForNotification(spans);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void notifySuggestionPicked(SuggestionSpan span, String originalString, int index) {
        try {
            this.mService.notifySuggestionPicked(span, originalString, index);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isFullscreenMode() {
        return this.mFullscreenMode;
    }

    public boolean isActive(View view) {
        boolean z;
        checkFocus();
        synchronized (this.mH) {
            z = (this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null;
        }
        return z;
    }

    public boolean isActive() {
        boolean z;
        checkFocus();
        synchronized (this.mH) {
            z = (this.mServedView == null || this.mCurrentTextBoxAttribute == null) ? false : true;
        }
        return z;
    }

    public boolean isAcceptingText() {
        checkFocus();
        return this.mServedInputConnection != null;
    }

    /* access modifiers changed from: package-private */
    public void clearBindingLocked() {
        clearConnectionLocked();
        setInputChannelLocked(null);
        this.mBindSequence = -1;
        this.mCurId = null;
        this.mCurMethod = null;
    }

    /* access modifiers changed from: package-private */
    public void setInputChannelLocked(InputChannel channel) {
        if (this.mCurChannel != channel) {
            if (this.mCurSender != null) {
                flushPendingEventsLocked();
                this.mCurSender.dispose();
                this.mCurSender = null;
            }
            if (this.mCurChannel != null) {
                this.mCurChannel.dispose();
            }
            this.mCurChannel = channel;
        }
    }

    /* access modifiers changed from: package-private */
    public void clearConnectionLocked() {
        this.mCurrentTextBoxAttribute = null;
        this.mServedInputConnection = null;
        if (this.mServedInputConnectionWrapper != null) {
            this.mServedInputConnectionWrapper.deactivate();
            this.mServedInputConnectionWrapper = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void finishInputLocked() {
        this.mCurRootView = null;
        this.mNextServedView = null;
        if (this.mServedView != null) {
            if (this.mCurrentTextBoxAttribute != null) {
                try {
                    this.mService.finishInput(this.mClient);
                } catch (RemoteException e) {
                }
            }
            notifyInputConnectionFinished();
            this.mServedView = null;
            this.mCompletions = null;
            this.mServedConnecting = false;
            clearConnectionLocked();
        }
    }

    private void notifyInputConnectionFinished() {
        ViewRootImpl viewRootImpl;
        if (this.mServedView != null && this.mServedInputConnection != null && (viewRootImpl = this.mServedView.getViewRootImpl()) != null) {
            viewRootImpl.dispatchFinishInputConnection(this.mServedInputConnection);
        }
    }

    public void reportFinishInputConnection(InputConnection ic) {
        if (this.mServedInputConnection != ic) {
            ic.finishComposingText();
            if (ic instanceof BaseInputConnection) {
                ((BaseInputConnection) ic).reportFinish();
            }
        }
    }

    public void displayCompletions(View view, CompletionInfo[] completions) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) {
                this.mCompletions = completions;
                if (this.mCurMethod != null) {
                    try {
                        this.mCurMethod.displayCompletions(this.mCompletions);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    public void updateExtractedText(View view, int token, ExtractedText text) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) {
                if (this.mCurMethod != null) {
                    try {
                        this.mCurMethod.updateExtractedText(token, text);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    public boolean showSoftInput(View view, int flags) {
        return showSoftInput(view, flags, null);
    }

    public boolean showSoftInput(View view, int flags, ResultReceiver resultReceiver) {
        boolean z = false;
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) {
                try {
                    z = this.mService.showSoftInput(this.mClient, flags, resultReceiver);
                } catch (RemoteException e) {
                }
            }
        }
        return z;
    }

    public void showSoftInputUnchecked(int flags, ResultReceiver resultReceiver) {
        try {
            this.mService.showSoftInput(this.mClient, flags, resultReceiver);
        } catch (RemoteException e) {
        }
    }

    public boolean hideSoftInputFromWindow(IBinder windowToken, int flags) {
        return hideSoftInputFromWindow(windowToken, flags, null);
    }

    public boolean hideSoftInputFromWindow(IBinder windowToken, int flags, ResultReceiver resultReceiver) {
        boolean z = false;
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView != null && this.mServedView.getWindowToken() == windowToken) {
                try {
                    z = this.mService.hideSoftInput(this.mClient, flags, resultReceiver);
                } catch (RemoteException e) {
                }
            }
        }
        return z;
    }

    public void toggleSoftInputFromWindow(IBinder windowToken, int showFlags, int hideFlags) {
        synchronized (this.mH) {
            if (this.mServedView != null && this.mServedView.getWindowToken() == windowToken) {
                if (this.mCurMethod != null) {
                    try {
                        this.mCurMethod.toggleSoftInput(showFlags, hideFlags);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    public void toggleSoftInput(int showFlags, int hideFlags) {
        if (this.mCurMethod != null) {
            try {
                this.mCurMethod.toggleSoftInput(showFlags, hideFlags);
            } catch (RemoteException e) {
            }
        }
    }

    public void restartInput(View view) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) {
                this.mServedConnecting = true;
                startInputInner(null, 0, 0, 0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0011, code lost:
        closeCurrentInput();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0021, code lost:
        if (r12.getLooper() == android.os.Looper.myLooper()) goto L_0x002d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0023, code lost:
        r12.post(new android.view.inputmethod.InputMethodManager.AnonymousClass2(r15));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002d, code lost:
        r7 = new android.view.inputmethod.EditorInfo();
        r7.packageName = r13.getContext().getPackageName();
        r7.fieldId = r13.getId();
        r10 = r13.onCreateInputConnection(r7);
        r14 = r15.mH;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0048, code lost:
        monitor-enter(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004b, code lost:
        if (r15.mServedView != r13) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004f, code lost:
        if (r15.mServedConnecting != false) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0052, code lost:
        monitor-exit(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0059, code lost:
        if (r15.mCurrentTextBoxAttribute != null) goto L_0x0061;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005b, code lost:
        r17 = r17 | 256;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0061, code lost:
        r15.mCurrentTextBoxAttribute = r7;
        r15.mServedConnecting = false;
        notifyInputConnectionFinished();
        r15.mServedInputConnection = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x006b, code lost:
        if (r10 == null) goto L_0x00d7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x006d, code lost:
        r15.mCursorSelStart = r7.initialSelStart;
        r15.mCursorSelEnd = r7.initialSelEnd;
        r15.mCursorCandStart = -1;
        r15.mCursorCandEnd = -1;
        r15.mCursorRect.setEmpty();
        r15.mCursorAnchorInfo = null;
        r8 = new android.view.inputmethod.InputMethodManager.ControlledInputConnectionWrapper(r12.getLooper(), r10, r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x008e, code lost:
        if (r15.mServedInputConnectionWrapper == null) goto L_0x0095;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0090, code lost:
        r15.mServedInputConnectionWrapper.deactivate();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0095, code lost:
        r15.mServedInputConnectionWrapper = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0097, code lost:
        if (r16 == null) goto L_0x00d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
        r11 = r15.mService.windowGainedFocus(r15.mClient, r16, r17, r18, r19, r7, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00a9, code lost:
        if (r11 == null) goto L_0x00c4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00ad, code lost:
        if (r11.id == null) goto L_0x00e4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00af, code lost:
        setInputChannelLocked(r11.channel);
        r15.mBindSequence = r11.sequence;
        r15.mCurMethod = r11.method;
        r15.mCurId = r11.id;
        r15.mNextUserActionNotificationSequenceNumber = r11.userActionNotificationSequenceNumber;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00c6, code lost:
        if (r15.mCurMethod == null) goto L_0x00d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00ca, code lost:
        if (r15.mCompletions == null) goto L_0x00d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r15.mCurMethod.displayCompletions(r15.mCompletions);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00d3, code lost:
        monitor-exit(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00d4, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00d7, code lost:
        r8 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00d9, code lost:
        r11 = r15.mService.startInput(r15.mClient, r8, r7, r17);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00e6, code lost:
        if (r11.channel == null) goto L_0x00f3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00ec, code lost:
        if (r11.channel == r15.mCurChannel) goto L_0x00f3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00ee, code lost:
        r11.channel.dispose();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00f5, code lost:
        if (r15.mCurMethod != null) goto L_0x00c4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x00f8, code lost:
        monitor-exit(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00fb, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00fc, code lost:
        android.util.Log.w(android.view.inputmethod.InputMethodManager.TAG, "IME died: " + r15.mCurId, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:?, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:?, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:?, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:?, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000b, code lost:
        r12 = r13.getHandler();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x000f, code lost:
        if (r12 != null) goto L_0x0019;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean startInputInner(android.os.IBinder r16, int r17, int r18, int r19) {
        /*
        // Method dump skipped, instructions count: 281
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.inputmethod.InputMethodManager.startInputInner(android.os.IBinder, int, int, int):boolean");
    }

    public void windowDismissed(IBinder appWindowToken) {
        checkFocus();
        synchronized (this.mH) {
            if (this.mServedView != null && this.mServedView.getWindowToken() == appWindowToken) {
                finishInputLocked();
            }
        }
    }

    public void focusIn(View view) {
        synchronized (this.mH) {
            focusInLocked(view);
        }
    }

    /* access modifiers changed from: package-private */
    public void focusInLocked(View view) {
        if (this.mCurRootView == view.getRootView()) {
            this.mNextServedView = view;
            scheduleCheckFocusLocked(view);
        }
    }

    public void focusOut(View view) {
        synchronized (this.mH) {
            if (this.mServedView != view) {
            }
        }
    }

    static void scheduleCheckFocusLocked(View view) {
        ViewRootImpl viewRootImpl = view.getViewRootImpl();
        if (viewRootImpl != null) {
            viewRootImpl.dispatchCheckFocus();
        }
    }

    public void checkFocus() {
        if (checkFocusNoStartInput(false, true)) {
            startInputInner(null, 0, 0, 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0038, code lost:
        if (r8 == false) goto L_0x003f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003a, code lost:
        if (r0 == null) goto L_0x003f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003c, code lost:
        r0.finishComposingText();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean checkFocusNoStartInput(boolean r7, boolean r8) {
        /*
            r6 = this;
            r2 = 1
            r1 = 0
            android.view.View r3 = r6.mServedView
            android.view.View r4 = r6.mNextServedView
            if (r3 != r4) goto L_0x000b
            if (r7 != 0) goto L_0x000b
        L_0x000a:
            return r1
        L_0x000b:
            r0 = 0
            android.view.inputmethod.InputMethodManager$H r3 = r6.mH
            monitor-enter(r3)
            android.view.View r4 = r6.mServedView     // Catch:{ all -> 0x0019 }
            android.view.View r5 = r6.mNextServedView     // Catch:{ all -> 0x0019 }
            if (r4 != r5) goto L_0x001c
            if (r7 != 0) goto L_0x001c
            monitor-exit(r3)     // Catch:{ all -> 0x0019 }
            goto L_0x000a
        L_0x0019:
            r1 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x0019 }
            throw r1
        L_0x001c:
            android.view.View r4 = r6.mNextServedView
            if (r4 != 0) goto L_0x0028
            r6.finishInputLocked()
            r6.closeCurrentInput()
            monitor-exit(r3)
            goto L_0x000a
        L_0x0028:
            android.view.inputmethod.InputConnection r0 = r6.mServedInputConnection
            android.view.View r1 = r6.mNextServedView
            r6.mServedView = r1
            r1 = 0
            r6.mCurrentTextBoxAttribute = r1
            r1 = 0
            r6.mCompletions = r1
            r1 = 1
            r6.mServedConnecting = r1
            monitor-exit(r3)
            if (r8 == 0) goto L_0x003f
            if (r0 == 0) goto L_0x003f
            r0.finishComposingText()
        L_0x003f:
            r1 = r2
            goto L_0x000a
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.inputmethod.InputMethodManager.checkFocusNoStartInput(boolean, boolean):boolean");
    }

    /* access modifiers changed from: package-private */
    public void closeCurrentInput() {
        try {
            this.mService.hideSoftInput(this.mClient, 2, (ResultReceiver) null);
        } catch (RemoteException e) {
        }
    }

    public void onWindowFocus(View rootView, View focusedView, int softInputMode, boolean first, int windowFlags) {
        View view;
        boolean forceNewFocus = false;
        synchronized (this.mH) {
            if (this.mHasBeenInactive) {
                this.mHasBeenInactive = false;
                forceNewFocus = true;
            }
            if (focusedView != null) {
                view = focusedView;
            } else {
                view = rootView;
            }
            focusInLocked(view);
        }
        int controlFlags = 0;
        if (focusedView != null) {
            controlFlags = 0 | 1;
            if (focusedView.onCheckIsTextEditor()) {
                controlFlags |= 2;
            }
        }
        if (first) {
            controlFlags |= 4;
        }
        if (!checkFocusNoStartInput(forceNewFocus, true) || !startInputInner(rootView.getWindowToken(), controlFlags, softInputMode, windowFlags)) {
            synchronized (this.mH) {
                try {
                    this.mService.windowGainedFocus(this.mClient, rootView.getWindowToken(), controlFlags, softInputMode, windowFlags, (EditorInfo) null, (IInputContext) null);
                } catch (RemoteException e) {
                }
            }
        }
    }

    public void startGettingWindowFocus(View rootView) {
        synchronized (this.mH) {
            this.mCurRootView = rootView;
        }
    }

    public void updateSelection(View view, int selStart, int selEnd, int candidatesStart, int candidatesEnd) {
        checkFocus();
        synchronized (this.mH) {
            if ((this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null && this.mCurMethod != null) {
                if (!(this.mCursorSelStart == selStart && this.mCursorSelEnd == selEnd && this.mCursorCandStart == candidatesStart && this.mCursorCandEnd == candidatesEnd)) {
                    try {
                        int oldSelStart = this.mCursorSelStart;
                        int oldSelEnd = this.mCursorSelEnd;
                        this.mCursorSelStart = selStart;
                        this.mCursorSelEnd = selEnd;
                        this.mCursorCandStart = candidatesStart;
                        this.mCursorCandEnd = candidatesEnd;
                        this.mCurMethod.updateSelection(oldSelStart, oldSelEnd, selStart, selEnd, candidatesStart, candidatesEnd);
                    } catch (RemoteException e) {
                        Log.w(TAG, "IME died: " + this.mCurId, e);
                    }
                }
            }
        }
    }

    public void viewClicked(View view) {
        boolean focusChanged = this.mServedView != this.mNextServedView;
        checkFocus();
        synchronized (this.mH) {
            if ((this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null && this.mCurMethod != null) {
                try {
                    this.mCurMethod.viewClicked(focusChanged);
                } catch (RemoteException e) {
                    Log.w(TAG, "IME died: " + this.mCurId, e);
                }
            }
        }
    }

    @Deprecated
    public boolean isWatchingCursor(View view) {
        return false;
    }

    public boolean isCursorAnchorInfoEnabled() {
        boolean isImmediate;
        boolean isMonitoring;
        boolean z = false;
        synchronized (this.mH) {
            if ((this.mRequestUpdateCursorAnchorInfoMonitorMode & 1) != 0) {
                isImmediate = true;
            } else {
                isImmediate = false;
            }
            if ((this.mRequestUpdateCursorAnchorInfoMonitorMode & 2) != 0) {
                isMonitoring = true;
            } else {
                isMonitoring = false;
            }
            if (isImmediate || isMonitoring) {
                z = true;
            }
        }
        return z;
    }

    public void setUpdateCursorAnchorInfoMode(int flags) {
        synchronized (this.mH) {
            this.mRequestUpdateCursorAnchorInfoMonitorMode = flags;
        }
    }

    @Deprecated
    public void updateCursor(View view, int left, int top, int right, int bottom) {
        checkFocus();
        synchronized (this.mH) {
            if ((this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null && this.mCurMethod != null) {
                this.mTmpCursorRect.set(left, top, right, bottom);
                if (!this.mCursorRect.equals(this.mTmpCursorRect)) {
                    try {
                        this.mCurMethod.updateCursor(this.mTmpCursorRect);
                        this.mCursorRect.set(this.mTmpCursorRect);
                    } catch (RemoteException e) {
                        Log.w(TAG, "IME died: " + this.mCurId, e);
                    }
                }
            }
        }
    }

    public void updateCursorAnchorInfo(View view, CursorAnchorInfo cursorAnchorInfo) {
        if (view != null && cursorAnchorInfo != null) {
            checkFocus();
            synchronized (this.mH) {
                if ((this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null && this.mCurMethod != null) {
                    if (((this.mRequestUpdateCursorAnchorInfoMonitorMode & 1) != 0) || !Objects.equals(this.mCursorAnchorInfo, cursorAnchorInfo)) {
                        try {
                            this.mCurMethod.updateCursorAnchorInfo(cursorAnchorInfo);
                            this.mCursorAnchorInfo = cursorAnchorInfo;
                            this.mRequestUpdateCursorAnchorInfoMonitorMode &= -2;
                        } catch (RemoteException e) {
                            Log.w(TAG, "IME died: " + this.mCurId, e);
                        }
                    }
                }
            }
        }
    }

    public void sendAppPrivateCommand(View view, String action, Bundle data) {
        checkFocus();
        synchronized (this.mH) {
            if ((this.mServedView == view || (this.mServedView != null && this.mServedView.checkInputConnectionProxy(view))) && this.mCurrentTextBoxAttribute != null && this.mCurMethod != null) {
                try {
                    this.mCurMethod.appPrivateCommand(action, data);
                } catch (RemoteException e) {
                    Log.w(TAG, "IME died: " + this.mCurId, e);
                }
            }
        }
    }

    public void setInputMethod(IBinder token, String id) {
        try {
            this.mService.setInputMethod(token, id);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void setInputMethodAndSubtype(IBinder token, String id, InputMethodSubtype subtype) {
        try {
            this.mService.setInputMethodAndSubtype(token, id, subtype);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void hideSoftInputFromInputMethod(IBinder token, int flags) {
        try {
            this.mService.hideMySoftInput(token, flags);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void showSoftInputFromInputMethod(IBinder token, int flags) {
        try {
            this.mService.showMySoftInput(token, flags);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public int dispatchInputEvent(InputEvent event, Object token, FinishedInputEventCallback callback, Handler handler) {
        synchronized (this.mH) {
            if (this.mCurMethod == null) {
                return 0;
            }
            if (event instanceof KeyEvent) {
                KeyEvent keyEvent = (KeyEvent) event;
                if (keyEvent.getAction() == 0 && keyEvent.getKeyCode() == 63 && keyEvent.getRepeatCount() == 0) {
                    showInputMethodPickerLocked();
                    return 1;
                }
            }
            PendingEvent p = obtainPendingEventLocked(event, token, this.mCurId, callback, handler);
            if (this.mMainLooper.isCurrentThread()) {
                return sendInputEventOnMainLooperLocked(p);
            }
            Message msg = this.mH.obtainMessage(5, p);
            msg.setAsynchronous(true);
            this.mH.sendMessage(msg);
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public void sendInputEventAndReportResultOnMainLooper(PendingEvent p) {
        boolean handled = true;
        synchronized (this.mH) {
            int result = sendInputEventOnMainLooperLocked(p);
            if (result != -1) {
                if (result != 1) {
                    handled = false;
                }
                invokeFinishedInputEventCallback(p, handled);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int sendInputEventOnMainLooperLocked(PendingEvent p) {
        if (this.mCurChannel != null) {
            if (this.mCurSender == null) {
                this.mCurSender = new ImeInputEventSender(this.mCurChannel, this.mH.getLooper());
            }
            InputEvent event = p.mEvent;
            int seq = event.getSequenceNumber();
            if (this.mCurSender.sendInputEvent(seq, event)) {
                this.mPendingEvents.put(seq, p);
                Trace.traceCounter(4, PENDING_EVENT_COUNTER, this.mPendingEvents.size());
                Message msg = this.mH.obtainMessage(6, p);
                msg.setAsynchronous(true);
                this.mH.sendMessageDelayed(msg, INPUT_METHOD_NOT_RESPONDING_TIMEOUT);
                return -1;
            }
            Log.w(TAG, "Unable to send input event to IME: " + this.mCurId + " dropping: " + event);
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void finishedInputEvent(int seq, boolean handled, boolean timeout) {
        synchronized (this.mH) {
            int index = this.mPendingEvents.indexOfKey(seq);
            if (index >= 0) {
                PendingEvent p = this.mPendingEvents.valueAt(index);
                this.mPendingEvents.removeAt(index);
                Trace.traceCounter(4, PENDING_EVENT_COUNTER, this.mPendingEvents.size());
                if (timeout) {
                    Log.w(TAG, "Timeout waiting for IME to handle input event after 2500 ms: " + p.mInputMethodId);
                } else {
                    this.mH.removeMessages(6, p);
                }
                invokeFinishedInputEventCallback(p, handled);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void invokeFinishedInputEventCallback(PendingEvent p, boolean handled) {
        p.mHandled = handled;
        if (p.mHandler.getLooper().isCurrentThread()) {
            p.run();
            return;
        }
        Message msg = Message.obtain(p.mHandler, p);
        msg.setAsynchronous(true);
        msg.sendToTarget();
    }

    private void flushPendingEventsLocked() {
        this.mH.removeMessages(7);
        int count = this.mPendingEvents.size();
        for (int i = 0; i < count; i++) {
            Message msg = this.mH.obtainMessage(7, this.mPendingEvents.keyAt(i), 0);
            msg.setAsynchronous(true);
            msg.sendToTarget();
        }
    }

    private PendingEvent obtainPendingEventLocked(InputEvent event, Object token, String inputMethodId, FinishedInputEventCallback callback, Handler handler) {
        PendingEvent p = this.mPendingEventPool.acquire();
        if (p == null) {
            p = new PendingEvent();
        }
        p.mEvent = event;
        p.mToken = token;
        p.mInputMethodId = inputMethodId;
        p.mCallback = callback;
        p.mHandler = handler;
        return p;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void recyclePendingEventLocked(PendingEvent p) {
        p.recycle();
        this.mPendingEventPool.release(p);
    }

    public void showInputMethodPicker() {
        synchronized (this.mH) {
            showInputMethodPickerLocked();
        }
    }

    private void showInputMethodPickerLocked() {
        try {
            this.mService.showInputMethodPickerFromClient(this.mClient);
        } catch (RemoteException e) {
            Log.w(TAG, "IME died: " + this.mCurId, e);
        }
    }

    public void showInputMethodAndSubtypeEnabler(String imiId) {
        synchronized (this.mH) {
            try {
                this.mService.showInputMethodAndSubtypeEnablerFromClient(this.mClient, imiId);
            } catch (RemoteException e) {
                Log.w(TAG, "IME died: " + this.mCurId, e);
            }
        }
    }

    public InputMethodSubtype getCurrentInputMethodSubtype() {
        InputMethodSubtype inputMethodSubtype;
        synchronized (this.mH) {
            try {
                inputMethodSubtype = this.mService.getCurrentInputMethodSubtype();
            } catch (RemoteException e) {
                Log.w(TAG, "IME died: " + this.mCurId, e);
                inputMethodSubtype = null;
            }
        }
        return inputMethodSubtype;
    }

    public boolean setCurrentInputMethodSubtype(InputMethodSubtype subtype) {
        boolean z;
        synchronized (this.mH) {
            try {
                z = this.mService.setCurrentInputMethodSubtype(subtype);
            } catch (RemoteException e) {
                Log.w(TAG, "IME died: " + this.mCurId, e);
                z = false;
            }
        }
        return z;
    }

    public void notifyUserAction() {
        synchronized (this.mH) {
            if (this.mLastSentUserActionNotificationSequenceNumber != this.mNextUserActionNotificationSequenceNumber) {
                try {
                    this.mService.notifyUserAction(this.mNextUserActionNotificationSequenceNumber);
                    this.mLastSentUserActionNotificationSequenceNumber = this.mNextUserActionNotificationSequenceNumber;
                } catch (RemoteException e) {
                    Log.w(TAG, "IME died: " + this.mCurId, e);
                }
            }
        }
    }

    public Map<InputMethodInfo, List<InputMethodSubtype>> getShortcutInputMethodsAndSubtypes() {
        HashMap<InputMethodInfo, List<InputMethodSubtype>> ret;
        synchronized (this.mH) {
            ret = new HashMap<>();
            try {
                List<Object> info = this.mService.getShortcutInputMethodsAndSubtypes();
                ArrayList<InputMethodSubtype> subtypes = null;
                int N = info.size();
                if (info != null && N > 0) {
                    int i = 0;
                    while (true) {
                        if (i >= N) {
                            break;
                        }
                        Object o = info.get(i);
                        if (o instanceof InputMethodInfo) {
                            if (ret.containsKey(o)) {
                                Log.e(TAG, "IMI list already contains the same InputMethod.");
                                break;
                            }
                            subtypes = new ArrayList<>();
                            ret.put((InputMethodInfo) o, subtypes);
                        } else if (subtypes != null && (o instanceof InputMethodSubtype)) {
                            subtypes.add((InputMethodSubtype) o);
                        }
                        i++;
                    }
                }
            } catch (RemoteException e) {
                Log.w(TAG, "IME died: " + this.mCurId, e);
            }
        }
        return ret;
    }

    public int getInputMethodWindowVisibleHeight() {
        int i;
        synchronized (this.mH) {
            try {
                i = this.mService.getInputMethodWindowVisibleHeight();
            } catch (RemoteException e) {
                Log.w(TAG, "IME died: " + this.mCurId, e);
                i = 0;
            }
        }
        return i;
    }

    public boolean switchToLastInputMethod(IBinder imeToken) {
        boolean z;
        synchronized (this.mH) {
            try {
                z = this.mService.switchToLastInputMethod(imeToken);
            } catch (RemoteException e) {
                Log.w(TAG, "IME died: " + this.mCurId, e);
                z = false;
            }
        }
        return z;
    }

    public boolean switchToNextInputMethod(IBinder imeToken, boolean onlyCurrentIme) {
        boolean z;
        synchronized (this.mH) {
            try {
                z = this.mService.switchToNextInputMethod(imeToken, onlyCurrentIme);
            } catch (RemoteException e) {
                Log.w(TAG, "IME died: " + this.mCurId, e);
                z = false;
            }
        }
        return z;
    }

    public boolean shouldOfferSwitchingToNextInputMethod(IBinder imeToken) {
        boolean z;
        synchronized (this.mH) {
            try {
                z = this.mService.shouldOfferSwitchingToNextInputMethod(imeToken);
            } catch (RemoteException e) {
                Log.w(TAG, "IME died: " + this.mCurId, e);
                z = false;
            }
        }
        return z;
    }

    public void setAdditionalInputMethodSubtypes(String imiId, InputMethodSubtype[] subtypes) {
        synchronized (this.mH) {
            try {
                this.mService.setAdditionalInputMethodSubtypes(imiId, subtypes);
            } catch (RemoteException e) {
                Log.w(TAG, "IME died: " + this.mCurId, e);
            }
        }
    }

    public InputMethodSubtype getLastInputMethodSubtype() {
        InputMethodSubtype inputMethodSubtype;
        synchronized (this.mH) {
            try {
                inputMethodSubtype = this.mService.getLastInputMethodSubtype();
            } catch (RemoteException e) {
                Log.w(TAG, "IME died: " + this.mCurId, e);
                inputMethodSubtype = null;
            }
        }
        return inputMethodSubtype;
    }

    /* access modifiers changed from: package-private */
    public void doDump(FileDescriptor fd, PrintWriter fout, String[] args) {
        Printer p = new PrintWriterPrinter(fout);
        p.println("Input method client state for " + this + ":");
        p.println("  mService=" + this.mService);
        p.println("  mMainLooper=" + this.mMainLooper);
        p.println("  mIInputContext=" + this.mIInputContext);
        p.println("  mActive=" + this.mActive + " mHasBeenInactive=" + this.mHasBeenInactive + " mBindSequence=" + this.mBindSequence + " mCurId=" + this.mCurId);
        p.println("  mCurMethod=" + this.mCurMethod);
        p.println("  mCurRootView=" + this.mCurRootView);
        p.println("  mServedView=" + this.mServedView);
        p.println("  mNextServedView=" + this.mNextServedView);
        p.println("  mServedConnecting=" + this.mServedConnecting);
        if (this.mCurrentTextBoxAttribute != null) {
            p.println("  mCurrentTextBoxAttribute:");
            this.mCurrentTextBoxAttribute.dump(p, "    ");
        } else {
            p.println("  mCurrentTextBoxAttribute: null");
        }
        p.println("  mServedInputConnection=" + this.mServedInputConnection);
        p.println("  mCompletions=" + this.mCompletions);
        p.println("  mCursorRect=" + this.mCursorRect);
        p.println("  mCursorSelStart=" + this.mCursorSelStart + " mCursorSelEnd=" + this.mCursorSelEnd + " mCursorCandStart=" + this.mCursorCandStart + " mCursorCandEnd=" + this.mCursorCandEnd);
        p.println("  mNextUserActionNotificationSequenceNumber=" + this.mNextUserActionNotificationSequenceNumber + " mLastSentUserActionNotificationSequenceNumber=" + this.mLastSentUserActionNotificationSequenceNumber);
    }

    /* access modifiers changed from: private */
    public final class ImeInputEventSender extends InputEventSender {
        public ImeInputEventSender(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        @Override // android.view.InputEventSender
        public void onInputEventFinished(int seq, boolean handled) {
            InputMethodManager.this.finishedInputEvent(seq, handled, false);
        }
    }

    /* access modifiers changed from: private */
    public final class PendingEvent implements Runnable {
        public FinishedInputEventCallback mCallback;
        public InputEvent mEvent;
        public boolean mHandled;
        public Handler mHandler;
        public String mInputMethodId;
        public Object mToken;

        private PendingEvent() {
        }

        public void recycle() {
            this.mEvent = null;
            this.mToken = null;
            this.mInputMethodId = null;
            this.mCallback = null;
            this.mHandler = null;
            this.mHandled = false;
        }

        public void run() {
            this.mCallback.onFinishedInputEvent(this.mToken, this.mHandled);
            synchronized (InputMethodManager.this.mH) {
                InputMethodManager.this.recyclePendingEventLocked(this);
            }
        }
    }
}
