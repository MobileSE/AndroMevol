package android.widget;

import android.view.ViewTreeObserver;
import android.widget.Editor;

/* access modifiers changed from: private */
public class Editor$PositionListener implements ViewTreeObserver.OnPreDrawListener {
    private final int MAXIMUM_NUMBER_OF_LISTENERS;
    private boolean[] mCanMove;
    private int mNumberOfListeners;
    private boolean mPositionHasChanged;
    private Editor.TextViewPositionListener[] mPositionListeners;
    private int mPositionX;
    private int mPositionY;
    private boolean mScrollHasChanged;
    final int[] mTempCoords;
    final /* synthetic */ Editor this$0;

    private Editor$PositionListener(Editor editor) {
        this.this$0 = editor;
        this.MAXIMUM_NUMBER_OF_LISTENERS = 7;
        this.mPositionListeners = new Editor.TextViewPositionListener[7];
        this.mCanMove = new boolean[7];
        this.mPositionHasChanged = true;
        this.mTempCoords = new int[2];
    }

    public void addSubscriber(Editor.TextViewPositionListener positionListener, boolean canMove) {
        if (this.mNumberOfListeners == 0) {
            updatePosition();
            Editor.access$700(this.this$0).getViewTreeObserver().addOnPreDrawListener(this);
        }
        int emptySlotIndex = -1;
        for (int i = 0; i < 7; i++) {
            Editor.TextViewPositionListener listener = this.mPositionListeners[i];
            if (listener != positionListener) {
                if (emptySlotIndex < 0 && listener == null) {
                    emptySlotIndex = i;
                }
            } else {
                return;
            }
        }
        this.mPositionListeners[emptySlotIndex] = positionListener;
        this.mCanMove[emptySlotIndex] = canMove;
        this.mNumberOfListeners++;
    }

    public void removeSubscriber(Editor.TextViewPositionListener positionListener) {
        int i = 0;
        while (true) {
            if (i >= 7) {
                break;
            } else if (this.mPositionListeners[i] == positionListener) {
                this.mPositionListeners[i] = null;
                this.mNumberOfListeners--;
                break;
            } else {
                i++;
            }
        }
        if (this.mNumberOfListeners == 0) {
            Editor.access$700(this.this$0).getViewTreeObserver().removeOnPreDrawListener(this);
        }
    }

    public int getPositionX() {
        return this.mPositionX;
    }

    public int getPositionY() {
        return this.mPositionY;
    }

    @Override // android.view.ViewTreeObserver.OnPreDrawListener
    public boolean onPreDraw() {
        Editor.TextViewPositionListener positionListener;
        updatePosition();
        for (int i = 0; i < 7; i++) {
            if ((this.mPositionHasChanged || this.mScrollHasChanged || this.mCanMove[i]) && (positionListener = this.mPositionListeners[i]) != null) {
                positionListener.updatePosition(this.mPositionX, this.mPositionY, this.mPositionHasChanged, this.mScrollHasChanged);
            }
        }
        this.mScrollHasChanged = false;
        return true;
    }

    private void updatePosition() {
        boolean z;
        Editor.access$700(this.this$0).getLocationInWindow(this.mTempCoords);
        if (this.mTempCoords[0] == this.mPositionX && this.mTempCoords[1] == this.mPositionY) {
            z = false;
        } else {
            z = true;
        }
        this.mPositionHasChanged = z;
        this.mPositionX = this.mTempCoords[0];
        this.mPositionY = this.mTempCoords[1];
    }

    public void onScrollChanged() {
        this.mScrollHasChanged = true;
    }
}
