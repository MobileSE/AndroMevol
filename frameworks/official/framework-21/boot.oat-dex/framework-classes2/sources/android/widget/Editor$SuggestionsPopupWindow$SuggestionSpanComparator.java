package android.widget;

import android.text.style.SuggestionSpan;
import android.widget.Editor;
import java.util.Comparator;

class Editor$SuggestionsPopupWindow$SuggestionSpanComparator implements Comparator<SuggestionSpan> {
    final /* synthetic */ Editor.SuggestionsPopupWindow this$1;

    private Editor$SuggestionsPopupWindow$SuggestionSpanComparator(Editor.SuggestionsPopupWindow suggestionsPopupWindow) {
        this.this$1 = suggestionsPopupWindow;
    }

    public int compare(SuggestionSpan span1, SuggestionSpan span2) {
        boolean easy2;
        boolean misspelled1;
        boolean misspelled2 = false;
        int flag1 = span1.getFlags();
        int flag2 = span2.getFlags();
        if (flag1 != flag2) {
            boolean easy1 = (flag1 & 1) != 0;
            if ((flag2 & 1) != 0) {
                easy2 = true;
            } else {
                easy2 = false;
            }
            if ((flag1 & 2) != 0) {
                misspelled1 = true;
            } else {
                misspelled1 = false;
            }
            if ((flag2 & 2) != 0) {
                misspelled2 = true;
            }
            if (easy1 && !misspelled1) {
                return -1;
            }
            if (easy2 && !misspelled2) {
                return 1;
            }
            if (misspelled1) {
                return -1;
            }
            if (misspelled2) {
                return 1;
            }
        }
        return ((Integer) Editor.SuggestionsPopupWindow.access$2200(this.this$1).get(span1)).intValue() - ((Integer) Editor.SuggestionsPopupWindow.access$2200(this.this$1).get(span2)).intValue();
    }
}
