package android.widget;

import android.content.Context;
import android.net.ProxyInfo;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.QwertyKeyListener;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AutoCompleteTextView;

public class MultiAutoCompleteTextView extends AutoCompleteTextView {
    private Tokenizer mTokenizer;

    public interface Tokenizer {
        int findTokenEnd(CharSequence charSequence, int i);

        int findTokenStart(CharSequence charSequence, int i);

        CharSequence terminateToken(CharSequence charSequence);
    }

    public MultiAutoCompleteTextView(Context context) {
        this(context, null);
    }

    public MultiAutoCompleteTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842859);
    }

    public MultiAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MultiAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /* access modifiers changed from: package-private */
    public void finishInit() {
    }

    public void setTokenizer(Tokenizer t) {
        this.mTokenizer = t;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AutoCompleteTextView
    public void performFiltering(CharSequence text, int keyCode) {
        if (enoughToFilter()) {
            int end = getSelectionEnd();
            performFiltering(text, this.mTokenizer.findTokenStart(text, end), end, keyCode);
            return;
        }
        dismissDropDown();
        Filter f = getFilter();
        if (f != null) {
            f.filter(null);
        }
    }

    @Override // android.widget.AutoCompleteTextView
    public boolean enoughToFilter() {
        Editable text = getText();
        int end = getSelectionEnd();
        if (end < 0 || this.mTokenizer == null || end - this.mTokenizer.findTokenStart(text, end) < getThreshold()) {
            return false;
        }
        return true;
    }

    @Override // android.widget.AutoCompleteTextView
    public void performValidation() {
        AutoCompleteTextView.Validator v = getValidator();
        if (v != null && this.mTokenizer != null) {
            Editable e = getText();
            int i = getText().length();
            while (i > 0) {
                int start = this.mTokenizer.findTokenStart(e, i);
                CharSequence sub = e.subSequence(start, this.mTokenizer.findTokenEnd(e, start));
                if (TextUtils.isEmpty(sub)) {
                    e.replace(start, i, ProxyInfo.LOCAL_EXCL_LIST);
                } else if (!v.isValid(sub)) {
                    e.replace(start, i, this.mTokenizer.terminateToken(v.fixText(sub)));
                }
                i = start;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void performFiltering(CharSequence text, int start, int end, int keyCode) {
        getFilter().filter(text.subSequence(start, end), this);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AutoCompleteTextView
    public void replaceText(CharSequence text) {
        clearComposingText();
        int end = getSelectionEnd();
        int start = this.mTokenizer.findTokenStart(getText(), end);
        Editable editable = getText();
        QwertyKeyListener.markAsReplaced(editable, start, end, TextUtils.substring(editable, start, end));
        editable.replace(start, end, this.mTokenizer.terminateToken(text));
    }

    @Override // android.widget.TextView, android.view.View, android.widget.EditText
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(MultiAutoCompleteTextView.class.getName());
    }

    @Override // android.widget.TextView, android.view.View, android.widget.EditText
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(MultiAutoCompleteTextView.class.getName());
    }

    public static class CommaTokenizer implements Tokenizer {
        @Override // android.widget.MultiAutoCompleteTextView.Tokenizer
        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;
            while (i > 0 && text.charAt(i - 1) != ',') {
                i--;
            }
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }
            return i;
        }

        @Override // android.widget.MultiAutoCompleteTextView.Tokenizer
        public int findTokenEnd(CharSequence text, int cursor) {
            int len = text.length();
            for (int i = cursor; i < len; i++) {
                if (text.charAt(i) == ',') {
                    return i;
                }
            }
            return len;
        }

        @Override // android.widget.MultiAutoCompleteTextView.Tokenizer
        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();
            while (i > 0 && text.charAt(i - 1) == ' ') {
                i--;
            }
            if (i > 0 && text.charAt(i - 1) == ',') {
                return text;
            }
            if (!(text instanceof Spanned)) {
                return ((Object) text) + ", ";
            }
            SpannableString sp = new SpannableString(((Object) text) + ", ");
            TextUtils.copySpansFrom((Spanned) text, 0, text.length(), Object.class, sp, 0);
            return sp;
        }
    }
}
