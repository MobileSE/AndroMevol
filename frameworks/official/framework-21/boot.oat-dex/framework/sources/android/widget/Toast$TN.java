package android.widget;

import android.R;
import android.app.ITransientNotification;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

class Toast$TN extends ITransientNotification.Stub {
    int mGravity;
    final Handler mHandler = new Handler();
    final Runnable mHide = new 2(this);
    float mHorizontalMargin;
    View mNextView;
    private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
    final Runnable mShow = new 1(this);
    float mVerticalMargin;
    View mView;
    WindowManager mWM;
    int mX;
    int mY;

    Toast$TN() {
        WindowManager.LayoutParams params = this.mParams;
        params.height = -2;
        params.width = -2;
        params.format = -3;
        params.windowAnimations = R.style.Animation_Toast;
        params.type = 2005;
        params.setTitle("Toast");
        params.flags = 152;
    }

    @Override // android.app.ITransientNotification
    public void show() {
        this.mHandler.post(this.mShow);
    }

    @Override // android.app.ITransientNotification
    public void hide() {
        this.mHandler.post(this.mHide);
    }

    public void handleShow() {
        if (this.mView != this.mNextView) {
            handleHide();
            this.mView = this.mNextView;
            Context context = this.mView.getContext().getApplicationContext();
            String packageName = this.mView.getContext().getOpPackageName();
            if (context == null) {
                context = this.mView.getContext();
            }
            this.mWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            int gravity = Gravity.getAbsoluteGravity(this.mGravity, this.mView.getContext().getResources().getConfiguration().getLayoutDirection());
            this.mParams.gravity = gravity;
            if ((gravity & 7) == 7) {
                this.mParams.horizontalWeight = 1.0f;
            }
            if ((gravity & 112) == 112) {
                this.mParams.verticalWeight = 1.0f;
            }
            this.mParams.x = this.mX;
            this.mParams.y = this.mY;
            this.mParams.verticalMargin = this.mVerticalMargin;
            this.mParams.horizontalMargin = this.mHorizontalMargin;
            this.mParams.packageName = packageName;
            if (this.mView.getParent() != null) {
                this.mWM.removeView(this.mView);
            }
            this.mWM.addView(this.mView, this.mParams);
            trySendAccessibilityEvent();
        }
    }

    private void trySendAccessibilityEvent() {
        AccessibilityManager accessibilityManager = AccessibilityManager.getInstance(this.mView.getContext());
        if (accessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(64);
            event.setClassName(getClass().getName());
            event.setPackageName(this.mView.getContext().getPackageName());
            this.mView.dispatchPopulateAccessibilityEvent(event);
            accessibilityManager.sendAccessibilityEvent(event);
        }
    }

    public void handleHide() {
        if (this.mView != null) {
            if (this.mView.getParent() != null) {
                this.mWM.removeView(this.mView);
            }
            this.mView = null;
        }
    }
}
