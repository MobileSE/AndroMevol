package android.widget;

import android.content.Context;
import android.text.Spannable;
import android.util.AttributeSet;
import android.widget.Editor;

class Editor$SuggestionsPopupWindow$CustomPopupWindow extends PopupWindow {
    final /* synthetic */ Editor.SuggestionsPopupWindow this$1;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public Editor$SuggestionsPopupWindow$CustomPopupWindow(Editor.SuggestionsPopupWindow suggestionsPopupWindow, Context context, int defStyleAttr) {
        super(context, (AttributeSet) null, defStyleAttr);
        this.this$1 = suggestionsPopupWindow;
    }

    @Override // android.widget.PopupWindow
    public void dismiss() {
        super.dismiss();
        this.this$1.this$0.getPositionListener().removeSubscriber(this.this$1);
        ((Spannable) this.this$1.this$0.mTextView.getText()).removeSpan(this.this$1.this$0.mSuggestionRangeSpan);
        this.this$1.this$0.mTextView.setCursorVisible(Editor.SuggestionsPopupWindow.access$1600(this.this$1));
        if (this.this$1.this$0.hasInsertionController()) {
            this.this$1.this$0.getInsertionController().show();
        }
    }
}
