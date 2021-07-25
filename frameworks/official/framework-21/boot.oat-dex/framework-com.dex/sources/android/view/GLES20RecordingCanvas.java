package android.view;

import android.util.Pools;

/* access modifiers changed from: package-private */
public class GLES20RecordingCanvas extends GLES20Canvas {
    private static final int POOL_LIMIT = 25;
    private static final Pools.SynchronizedPool<GLES20RecordingCanvas> sPool = new Pools.SynchronizedPool<>(25);
    RenderNode mNode;

    private GLES20RecordingCanvas() {
    }

    static GLES20RecordingCanvas obtain(RenderNode node) {
        if (node == null) {
            throw new IllegalArgumentException("node cannot be null");
        }
        GLES20RecordingCanvas canvas = sPool.acquire();
        if (canvas == null) {
            canvas = new GLES20RecordingCanvas();
        }
        canvas.mNode = node;
        return canvas;
    }

    /* access modifiers changed from: package-private */
    public void recycle() {
        this.mNode = null;
        sPool.release(this);
    }

    /* access modifiers changed from: package-private */
    public long finishRecording() {
        return nFinishRecording(this.mRenderer);
    }

    @Override // android.graphics.Canvas
    public boolean isRecordingFor(Object o) {
        return o == this.mNode;
    }
}
