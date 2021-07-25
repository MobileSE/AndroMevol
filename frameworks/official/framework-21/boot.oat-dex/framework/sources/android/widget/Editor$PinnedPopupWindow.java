package android.widget;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Editor;

/* access modifiers changed from: private */
public abstract class Editor$PinnedPopupWindow implements Editor.TextViewPositionListener {
    protected ViewGroup mContentView;
    protected PopupWindow mPopupWindow;
    int mPositionX;
    int mPositionY;
    final /* synthetic */ Editor this$0;

    /* access modifiers changed from: protected */
    public abstract int clipVertically(int i);

    /* access modifiers changed from: protected */
    public abstract void createPopupWindow();

    /* access modifiers changed from: protected */
    public abstract int getTextOffset();

    /* access modifiers changed from: protected */
    public abstract int getVerticalLocalPosition(int i);

    /* access modifiers changed from: protected */
    public abstract void initContentView();

    public Editor$PinnedPopupWindow(Editor editor) {
        this.this$0 = editor;
        createPopupWindow();
        this.mPopupWindow.setWindowLayoutType(1002);
        this.mPopupWindow.setWidth(-2);
        this.mPopupWindow.setHeight(-2);
        initContentView();
        this.mContentView.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        this.mPopupWindow.setContentView(this.mContentView);
    }

    public void show() {
        Editor.access$1400(this.this$0).addSubscriber(this, false);
        computeLocalPosition();
        Editor$PositionListener positionListener = Editor.access$1400(this.this$0);
        updatePosition(positionListener.getPositionX(), positionListener.getPositionY());
    }

    /* access modifiers changed from: protected */
    public void measureContent() {
        DisplayMetrics displayMetrics = Editor.access$700(this.this$0).getResources().getDisplayMetrics();
        this.mContentView.measure(View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, Integer.MIN_VALUE));
    }

    private void computeLocalPosition() {
        measureContent();
        int width = this.mContentView.getMeasuredWidth();
        int offset = getTextOffset();
        this.mPositionX = (int) (Editor.access$700(this.this$0).getLayout().getPrimaryHorizontal(offset) - (((float) width) / 2.0f));
        this.mPositionX += Editor.access$700(this.this$0).viewportToContentHorizontalOffset();
        this.mPositionY = getVerticalLocalPosition(Editor.access$700(this.this$0).getLayout().getLineForOffset(offset));
        this.mPositionY += Editor.access$700(this.this$0).viewportToContentVerticalOffset();
    }

    private void updatePosition(int parentPositionX, int parentPositionY) {
        int positionX = parentPositionX + this.mPositionX;
        int positionY = clipVertically(parentPositionY + this.mPositionY);
        DisplayMetrics displayMetrics = Editor.access$700(this.this$0).getResources().getDisplayMetrics();
        int positionX2 = Math.max(0, Math.min(displayMetrics.widthPixels - this.mContentView.getMeasuredWidth(), positionX));
        if (isShowing()) {
            this.mPopupWindow.update(positionX2, positionY, -1, -1);
        } else {
            this.mPopupWindow.showAtLocation(Editor.access$700(this.this$0), 0, positionX2, positionY);
        }
    }

    public void hide() {
        this.mPopupWindow.dismiss();
        Editor.access$1400(this.this$0).removeSubscriber(this);
    }

    public void updatePosition(int parentPositionX, int parentPositionY, boolean parentPositionChanged, boolean parentScrolled) {
        if (!isShowing() || !Editor.access$1500(this.this$0, getTextOffset())) {
            hide();
            return;
        }
        if (parentScrolled) {
            computeLocalPosition();
        }
        updatePosition(parentPositionX, parentPositionY);
    }

    public boolean isShowing() {
        return this.mPopupWindow.isShowing();
    }
}
