package android.widget;

import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Editor;

class Editor$SelectionModifierCursorController implements Editor.CursorController {
    private static final int DELAY_BEFORE_REPLACE_ACTION = 200;
    private float mDownPositionX;
    private float mDownPositionY;
    private Editor$SelectionEndHandleView mEndHandle;
    private boolean mGestureStayedInTapRegion;
    private int mMaxTouchOffset;
    private int mMinTouchOffset;
    private long mPreviousTapUpTime = 0;
    private Editor.SelectionStartHandleView mStartHandle;
    final /* synthetic */ Editor this$0;

    Editor$SelectionModifierCursorController(Editor editor) {
        this.this$0 = editor;
        resetTouchOffsets();
    }

    public void show() {
        if (!Editor.access$700(this.this$0).isInBatchEditMode()) {
            initDrawables();
            initHandles();
            Editor.access$2900(this.this$0);
        }
    }

    private void initDrawables() {
        if (Editor.access$3000(this.this$0) == null) {
            Editor.access$3002(this.this$0, Editor.access$700(this.this$0).getContext().getDrawable(Editor.access$700(this.this$0).mTextSelectHandleLeftRes));
        }
        if (Editor.access$3100(this.this$0) == null) {
            Editor.access$3102(this.this$0, Editor.access$700(this.this$0).getContext().getDrawable(Editor.access$700(this.this$0).mTextSelectHandleRightRes));
        }
    }

    private void initHandles() {
        if (this.mStartHandle == null) {
            this.mStartHandle = new Editor.SelectionStartHandleView(this.this$0, Editor.access$3000(this.this$0), Editor.access$3100(this.this$0));
        }
        if (this.mEndHandle == null) {
            this.mEndHandle = new Editor$SelectionEndHandleView(this.this$0, Editor.access$3100(this.this$0), Editor.access$3000(this.this$0));
        }
        this.mStartHandle.show();
        this.mEndHandle.show();
        this.mStartHandle.showActionPopupWindow(200);
        this.mEndHandle.setActionPopupWindow(this.mStartHandle.getActionPopupWindow());
        Editor.access$2900(this.this$0);
    }

    public void hide() {
        if (this.mStartHandle != null) {
            this.mStartHandle.hide();
        }
        if (this.mEndHandle != null) {
            this.mEndHandle.hide();
        }
    }

    public void onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case 0:
                float x = event.getX();
                float y = event.getY();
                int offsetForPosition = Editor.access$700(this.this$0).getOffsetForPosition(x, y);
                this.mMaxTouchOffset = offsetForPosition;
                this.mMinTouchOffset = offsetForPosition;
                if (this.mGestureStayedInTapRegion && SystemClock.uptimeMillis() - this.mPreviousTapUpTime <= ((long) ViewConfiguration.getDoubleTapTimeout())) {
                    float deltaX = x - this.mDownPositionX;
                    float deltaY = y - this.mDownPositionY;
                    float distanceSquared = (deltaX * deltaX) + (deltaY * deltaY);
                    int doubleTapSlop = ViewConfiguration.get(Editor.access$700(this.this$0).getContext()).getScaledDoubleTapSlop();
                    if ((distanceSquared < ((float) (doubleTapSlop * doubleTapSlop))) && Editor.access$3200(this.this$0, x, y)) {
                        this.this$0.startSelectionActionMode();
                        this.this$0.mDiscardNextActionUp = true;
                    }
                }
                this.mDownPositionX = x;
                this.mDownPositionY = y;
                this.mGestureStayedInTapRegion = true;
                return;
            case 1:
                this.mPreviousTapUpTime = SystemClock.uptimeMillis();
                return;
            case 2:
                if (this.mGestureStayedInTapRegion) {
                    float deltaX2 = event.getX() - this.mDownPositionX;
                    float deltaY2 = event.getY() - this.mDownPositionY;
                    float distanceSquared2 = (deltaX2 * deltaX2) + (deltaY2 * deltaY2);
                    int doubleTapTouchSlop = ViewConfiguration.get(Editor.access$700(this.this$0).getContext()).getScaledDoubleTapTouchSlop();
                    if (distanceSquared2 > ((float) (doubleTapTouchSlop * doubleTapTouchSlop))) {
                        this.mGestureStayedInTapRegion = false;
                        return;
                    }
                    return;
                }
                return;
            case 3:
            case 4:
            default:
                return;
            case 5:
            case 6:
                if (Editor.access$700(this.this$0).getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT)) {
                    updateMinAndMaxOffsets(event);
                    return;
                }
                return;
        }
    }

    private void updateMinAndMaxOffsets(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        for (int index = 0; index < pointerCount; index++) {
            int offset = Editor.access$700(this.this$0).getOffsetForPosition(event.getX(index), event.getY(index));
            if (offset < this.mMinTouchOffset) {
                this.mMinTouchOffset = offset;
            }
            if (offset > this.mMaxTouchOffset) {
                this.mMaxTouchOffset = offset;
            }
        }
    }

    public int getMinTouchOffset() {
        return this.mMinTouchOffset;
    }

    public int getMaxTouchOffset() {
        return this.mMaxTouchOffset;
    }

    public void resetTouchOffsets() {
        this.mMaxTouchOffset = -1;
        this.mMinTouchOffset = -1;
    }

    public boolean isSelectionStartDragged() {
        return this.mStartHandle != null && this.mStartHandle.isDragging();
    }

    public void onTouchModeChanged(boolean isInTouchMode) {
        if (!isInTouchMode) {
            hide();
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: android.widget.Editor$SelectionModifierCursorController */
    /* JADX WARN: Multi-variable type inference failed */
    public void onDetached() {
        Editor.access$700(this.this$0).getViewTreeObserver().removeOnTouchModeChangeListener(this);
        if (this.mStartHandle != null) {
            this.mStartHandle.onDetached();
        }
        if (this.mEndHandle != null) {
            this.mEndHandle.onDetached();
        }
    }
}
