package android.hardware.camera2.impl;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.dispatch.Dispatchable;
import android.hardware.camera2.dispatch.MethodNameInvoker;
import android.hardware.camera2.impl.CameraDeviceImpl;
import com.android.internal.util.Preconditions;

public class CallbackProxies {

    public static class DeviceStateCallbackProxy extends CameraDeviceImpl.StateCallbackKK {
        private final MethodNameInvoker<CameraDeviceImpl.StateCallbackKK> mProxy;

        public DeviceStateCallbackProxy(Dispatchable<CameraDeviceImpl.StateCallbackKK> dispatchTarget) {
            this.mProxy = new MethodNameInvoker<>((Dispatchable) Preconditions.checkNotNull(dispatchTarget, "dispatchTarget must not be null"), CameraDeviceImpl.StateCallbackKK.class);
        }

        @Override // android.hardware.camera2.CameraDevice.StateCallback
        public void onOpened(CameraDevice camera) {
            this.mProxy.invoke("onOpened", camera);
        }

        @Override // android.hardware.camera2.CameraDevice.StateCallback
        public void onDisconnected(CameraDevice camera) {
            this.mProxy.invoke("onDisconnected", camera);
        }

        @Override // android.hardware.camera2.CameraDevice.StateCallback
        public void onError(CameraDevice camera, int error) {
            this.mProxy.invoke("onError", camera, Integer.valueOf(error));
        }

        @Override // android.hardware.camera2.impl.CameraDeviceImpl.StateCallbackKK
        public void onUnconfigured(CameraDevice camera) {
            this.mProxy.invoke("onUnconfigured", camera);
        }

        @Override // android.hardware.camera2.impl.CameraDeviceImpl.StateCallbackKK
        public void onActive(CameraDevice camera) {
            this.mProxy.invoke("onActive", camera);
        }

        @Override // android.hardware.camera2.impl.CameraDeviceImpl.StateCallbackKK
        public void onBusy(CameraDevice camera) {
            this.mProxy.invoke("onBusy", camera);
        }

        @Override // android.hardware.camera2.CameraDevice.StateCallback
        public void onClosed(CameraDevice camera) {
            this.mProxy.invoke("onClosed", camera);
        }

        @Override // android.hardware.camera2.impl.CameraDeviceImpl.StateCallbackKK
        public void onIdle(CameraDevice camera) {
            this.mProxy.invoke("onIdle", camera);
        }
    }

    public static class DeviceCaptureCallbackProxy extends CameraDeviceImpl.CaptureCallback {
        private final MethodNameInvoker<CameraDeviceImpl.CaptureCallback> mProxy;

        public DeviceCaptureCallbackProxy(Dispatchable<CameraDeviceImpl.CaptureCallback> dispatchTarget) {
            this.mProxy = new MethodNameInvoker<>((Dispatchable) Preconditions.checkNotNull(dispatchTarget, "dispatchTarget must not be null"), CameraDeviceImpl.CaptureCallback.class);
        }

        @Override // android.hardware.camera2.impl.CameraDeviceImpl.CaptureCallback
        public void onCaptureStarted(CameraDevice camera, CaptureRequest request, long timestamp, long frameNumber) {
            this.mProxy.invoke("onCaptureStarted", camera, request, Long.valueOf(timestamp), Long.valueOf(frameNumber));
        }

        @Override // android.hardware.camera2.impl.CameraDeviceImpl.CaptureCallback
        public void onCapturePartial(CameraDevice camera, CaptureRequest request, CaptureResult result) {
            this.mProxy.invoke("onCapturePartial", camera, request, result);
        }

        @Override // android.hardware.camera2.impl.CameraDeviceImpl.CaptureCallback
        public void onCaptureProgressed(CameraDevice camera, CaptureRequest request, CaptureResult partialResult) {
            this.mProxy.invoke("onCaptureProgressed", camera, request, partialResult);
        }

        @Override // android.hardware.camera2.impl.CameraDeviceImpl.CaptureCallback
        public void onCaptureCompleted(CameraDevice camera, CaptureRequest request, TotalCaptureResult result) {
            this.mProxy.invoke("onCaptureCompleted", camera, request, result);
        }

        @Override // android.hardware.camera2.impl.CameraDeviceImpl.CaptureCallback
        public void onCaptureFailed(CameraDevice camera, CaptureRequest request, CaptureFailure failure) {
            this.mProxy.invoke("onCaptureFailed", camera, request, failure);
        }

        @Override // android.hardware.camera2.impl.CameraDeviceImpl.CaptureCallback
        public void onCaptureSequenceCompleted(CameraDevice camera, int sequenceId, long frameNumber) {
            this.mProxy.invoke("onCaptureSequenceCompleted", camera, Integer.valueOf(sequenceId), Long.valueOf(frameNumber));
        }

        @Override // android.hardware.camera2.impl.CameraDeviceImpl.CaptureCallback
        public void onCaptureSequenceAborted(CameraDevice camera, int sequenceId) {
            this.mProxy.invoke("onCaptureSequenceAborted", camera, Integer.valueOf(sequenceId));
        }
    }

    public static class SessionStateCallbackProxy extends CameraCaptureSession.StateCallback {
        private final MethodNameInvoker<CameraCaptureSession.StateCallback> mProxy;

        public SessionStateCallbackProxy(Dispatchable<CameraCaptureSession.StateCallback> dispatchTarget) {
            this.mProxy = new MethodNameInvoker<>((Dispatchable) Preconditions.checkNotNull(dispatchTarget, "dispatchTarget must not be null"), CameraCaptureSession.StateCallback.class);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onConfigured(CameraCaptureSession session) {
            this.mProxy.invoke("onConfigured", session);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onConfigureFailed(CameraCaptureSession session) {
            this.mProxy.invoke("onConfigureFailed", session);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onReady(CameraCaptureSession session) {
            this.mProxy.invoke("onReady", session);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onActive(CameraCaptureSession session) {
            this.mProxy.invoke("onActive", session);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onClosed(CameraCaptureSession session) {
            this.mProxy.invoke("onClosed", session);
        }
    }

    private CallbackProxies() {
        throw new AssertionError();
    }
}
