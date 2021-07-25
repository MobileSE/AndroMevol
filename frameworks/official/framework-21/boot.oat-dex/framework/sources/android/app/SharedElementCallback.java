package android.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcelable;
import android.transition.TransitionUtils;
import android.view.View;
import java.util.List;
import java.util.Map;

public abstract class SharedElementCallback {
    static final SharedElementCallback NULL_CALLBACK = new SharedElementCallback() {
        /* class android.app.SharedElementCallback.AnonymousClass1 */
    };
    private Matrix mTempMatrix;

    public void onSharedElementStart(List<String> list, List<View> list2, List<View> list3) {
    }

    public void onSharedElementEnd(List<String> list, List<View> list2, List<View> list3) {
    }

    public void onRejectSharedElements(List<View> list) {
    }

    public void onMapSharedElements(List<String> list, Map<String, View> map) {
    }

    public Parcelable onCaptureSharedElementSnapshot(View sharedElement, Matrix viewToGlobalMatrix, RectF screenBounds) {
        if (this.mTempMatrix == null) {
            this.mTempMatrix = new Matrix(viewToGlobalMatrix);
        } else {
            this.mTempMatrix.set(viewToGlobalMatrix);
        }
        return TransitionUtils.createViewBitmap(sharedElement, this.mTempMatrix, screenBounds);
    }

    public View onCreateSnapshotView(Context context, Parcelable snapshot) {
        if (!(snapshot instanceof Bitmap)) {
            return null;
        }
        View view = new View(context);
        view.setBackground(new BitmapDrawable(context.getResources(), (Bitmap) snapshot));
        return view;
    }
}
