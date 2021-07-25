package android.hardware.camera2.legacy;

import android.hardware.Camera;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.utils.ParamsUtils;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.Objects;

public class LegacyFocusStateMapper {
    private static String TAG = "LegacyFocusStateMapper";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private String mAfModePrevious = null;
    private int mAfRun = 0;
    private int mAfState = 0;
    private int mAfStatePrevious = 0;
    private final Camera mCamera;
    private final Object mLock = new Object();

    public LegacyFocusStateMapper(Camera camera) {
        this.mCamera = (Camera) Preconditions.checkNotNull(camera, "camera must not be null");
    }

    public void processRequestTriggers(CaptureRequest captureRequest, Camera.Parameters parameters) {
        final int currentAfRun;
        int updatedAfRun;
        int afStateAfterStart;
        final int currentAfRun2;
        Preconditions.checkNotNull(captureRequest, "captureRequest must not be null");
        int afTrigger = ((Integer) ParamsUtils.getOrDefault(captureRequest, CaptureRequest.CONTROL_AF_TRIGGER, 0)).intValue();
        final String afMode = parameters.getFocusMode();
        if (!Objects.equals(this.mAfModePrevious, afMode)) {
            if (VERBOSE) {
                Log.v(TAG, "processRequestTriggers - AF mode switched from " + this.mAfModePrevious + " to " + afMode);
            }
            synchronized (this.mLock) {
                this.mAfRun++;
                this.mAfState = 0;
            }
            this.mCamera.cancelAutoFocus();
        }
        this.mAfModePrevious = afMode;
        synchronized (this.mLock) {
            currentAfRun = this.mAfRun;
        }
        Camera.AutoFocusMoveCallback afMoveCallback = new Camera.AutoFocusMoveCallback() {
            /* class android.hardware.camera2.legacy.LegacyFocusStateMapper.AnonymousClass1 */

            @Override // android.hardware.Camera.AutoFocusMoveCallback
            public void onAutoFocusMoving(boolean start, Camera camera) {
                synchronized (LegacyFocusStateMapper.this.mLock) {
                    int latestAfRun = LegacyFocusStateMapper.this.mAfRun;
                    if (LegacyFocusStateMapper.VERBOSE) {
                        Log.v(LegacyFocusStateMapper.TAG, "onAutoFocusMoving - start " + start + " latest AF run " + latestAfRun + ", last AF run " + currentAfRun);
                    }
                    if (currentAfRun != latestAfRun) {
                        Log.d(LegacyFocusStateMapper.TAG, "onAutoFocusMoving - ignoring move callbacks from old af run" + currentAfRun);
                        return;
                    }
                    int newAfState = start ? 1 : 2;
                    String str = afMode;
                    char c = 65535;
                    switch (str.hashCode()) {
                        case -194628547:
                            if (str.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                                c = 1;
                                break;
                            }
                            break;
                        case 910005312:
                            if (str.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                                c = 0;
                                break;
                            }
                            break;
                    }
                    switch (c) {
                        case 0:
                        case 1:
                            break;
                        default:
                            Log.w(LegacyFocusStateMapper.TAG, "onAutoFocus - got unexpected onAutoFocus in mode " + afMode);
                            break;
                    }
                    LegacyFocusStateMapper.this.mAfState = newAfState;
                }
            }
        };
        char c = 65535;
        switch (afMode.hashCode()) {
            case -194628547:
                if (afMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    c = 3;
                    break;
                }
                break;
            case 3005871:
                if (afMode.equals("auto")) {
                    c = 0;
                    break;
                }
                break;
            case 103652300:
                if (afMode.equals(Camera.Parameters.FOCUS_MODE_MACRO)) {
                    c = 1;
                    break;
                }
                break;
            case 910005312:
                if (afMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    c = 2;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
                this.mCamera.setAutoFocusMoveCallback(afMoveCallback);
                break;
        }
        switch (afTrigger) {
            case 0:
                return;
            case 1:
                char c2 = 65535;
                switch (afMode.hashCode()) {
                    case -194628547:
                        if (afMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                            c2 = 3;
                            break;
                        }
                        break;
                    case 3005871:
                        if (afMode.equals("auto")) {
                            c2 = 0;
                            break;
                        }
                        break;
                    case 103652300:
                        if (afMode.equals(Camera.Parameters.FOCUS_MODE_MACRO)) {
                            c2 = 1;
                            break;
                        }
                        break;
                    case 910005312:
                        if (afMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                            c2 = 2;
                            break;
                        }
                        break;
                }
                switch (c2) {
                    case 0:
                    case 1:
                        afStateAfterStart = 3;
                        break;
                    case 2:
                    case 3:
                        afStateAfterStart = 1;
                        break;
                    default:
                        afStateAfterStart = 0;
                        break;
                }
                synchronized (this.mLock) {
                    currentAfRun2 = this.mAfRun + 1;
                    this.mAfRun = currentAfRun2;
                    this.mAfState = afStateAfterStart;
                }
                if (VERBOSE) {
                    Log.v(TAG, "processRequestTriggers - got AF_TRIGGER_START, new AF run is " + currentAfRun2);
                }
                if (afStateAfterStart != 0) {
                    this.mCamera.autoFocus(new Camera.AutoFocusCallback() {
                        /* class android.hardware.camera2.legacy.LegacyFocusStateMapper.AnonymousClass2 */

                        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
                        /* JADX WARNING: Code restructure failed: missing block: B:25:0x00ae, code lost:
                            if (r7.equals("auto") != false) goto L_0x007c;
                         */
                        @Override // android.hardware.Camera.AutoFocusCallback
                        /* Code decompiled incorrectly, please refer to instructions dump. */
                        public void onAutoFocus(boolean r10, android.hardware.Camera r11) {
                            /*
                            // Method dump skipped, instructions count: 238
                            */
                            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyFocusStateMapper.AnonymousClass2.onAutoFocus(boolean, android.hardware.Camera):void");
                        }
                    });
                    return;
                }
                return;
            case 2:
                synchronized (this.mLock) {
                    synchronized (this.mLock) {
                        updatedAfRun = this.mAfRun + 1;
                        this.mAfRun = updatedAfRun;
                        this.mAfState = 0;
                    }
                    this.mCamera.cancelAutoFocus();
                    if (VERBOSE) {
                        Log.v(TAG, "processRequestTriggers - got AF_TRIGGER_CANCEL, new AF run is " + updatedAfRun);
                    }
                }
                return;
            default:
                Log.w(TAG, "processRequestTriggers - ignoring unknown control.afTrigger = " + afTrigger);
                return;
        }
    }

    public void mapResultTriggers(CameraMetadataNative result) {
        int newAfState;
        Preconditions.checkNotNull(result, "result must not be null");
        synchronized (this.mLock) {
            newAfState = this.mAfState;
        }
        if (VERBOSE && newAfState != this.mAfStatePrevious) {
            Log.v(TAG, String.format("mapResultTriggers - afState changed from %s to %s", afStateToString(this.mAfStatePrevious), afStateToString(newAfState)));
        }
        result.set(CaptureResult.CONTROL_AF_STATE, Integer.valueOf(newAfState));
        this.mAfStatePrevious = newAfState;
    }

    private static String afStateToString(int afState) {
        switch (afState) {
            case 0:
                return "INACTIVE";
            case 1:
                return "PASSIVE_SCAN";
            case 2:
                return "PASSIVE_FOCUSED";
            case 3:
                return "ACTIVE_SCAN";
            case 4:
                return "FOCUSED_LOCKED";
            case 5:
                return "NOT_FOCUSED_LOCKED";
            case 6:
                return "PASSIVE_UNFOCUSED";
            default:
                return "UNKNOWN(" + afState + ")";
        }
    }
}
