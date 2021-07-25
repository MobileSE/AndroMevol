package android.widget;

import android.graphics.drawable.Drawable;
import android.text.Selection;
import android.text.Spannable;
import android.widget.Editor;

/* access modifiers changed from: private */
public class Editor$SelectionEndHandleView extends Editor.HandleView {
    final /* synthetic */ Editor this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public Editor$SelectionEndHandleView(Editor editor, Drawable drawableLtr, Drawable drawableRtl) {
        super(editor, drawableLtr, drawableRtl);
        this.this$0 = editor;
    }

    /* access modifiers changed from: protected */
    public int getHotspotX(Drawable drawable, boolean isRtlRun) {
        if (isRtlRun) {
            return (drawable.getIntrinsicWidth() * 3) / 4;
        }
        return drawable.getIntrinsicWidth() / 4;
    }

    /* access modifiers changed from: protected */
    public int getHorizontalGravity(boolean isRtlRun) {
        return isRtlRun ? 3 : 5;
    }

    public int getCurrentCursorOffset() {
        return Editor.access$700(this.this$0).getSelectionEnd();
    }

    public void updateSelection(int offset) {
        Selection.setSelection((Spannable) Editor.access$700(this.this$0).getText(), Editor.access$700(this.this$0).getSelectionStart(), offset);
        updateDrawable();
    }

    public void updatePosition(float x, float y) {
        int offset = Editor.access$700(this.this$0).getOffsetForPosition(x, y);
        int selectionStart = Editor.access$700(this.this$0).getSelectionStart();
        if (offset <= selectionStart) {
            offset = Math.min(selectionStart + 1, Editor.access$700(this.this$0).getText().length());
        }
        positionAtCursorOffset(offset, false);
    }

    public void setActionPopupWindow(Editor.ActionPopupWindow actionPopupWindow) {
        this.mActionPopupWindow = actionPopupWindow;
    }
}
