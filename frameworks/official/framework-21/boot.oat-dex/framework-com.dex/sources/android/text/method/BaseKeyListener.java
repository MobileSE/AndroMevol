package android.text.method;

import android.text.Editable;
import android.text.Layout;
import android.text.NoCopySpan;
import android.text.Selection;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public abstract class BaseKeyListener extends MetaKeyKeyListener implements KeyListener {
    static final Object OLD_SEL_START = new NoCopySpan.Concrete();

    public boolean backspace(View view, Editable content, int keyCode, KeyEvent event) {
        return backspaceOrForwardDelete(view, content, keyCode, event, false);
    }

    public boolean forwardDelete(View view, Editable content, int keyCode, KeyEvent event) {
        return backspaceOrForwardDelete(view, content, keyCode, event, true);
    }

    private boolean backspaceOrForwardDelete(View view, Editable content, int keyCode, KeyEvent event, boolean isForwardDelete) {
        int end;
        if (!KeyEvent.metaStateHasNoModifiers(event.getMetaState() & -244)) {
            return false;
        }
        if (deleteSelection(view, content)) {
            return true;
        }
        if (getMetaState(content, 2, event) == 1 && deleteLine(view, content)) {
            return true;
        }
        int start = Selection.getSelectionEnd(content);
        if (isForwardDelete || event.isShiftPressed() || getMetaState(content, 1) == 1) {
            end = TextUtils.getOffsetAfter(content, start);
        } else {
            end = TextUtils.getOffsetBefore(content, start);
        }
        if (start == end) {
            return false;
        }
        content.delete(Math.min(start, end), Math.max(start, end));
        return true;
    }

    private boolean deleteSelection(View view, Editable content) {
        int selectionStart = Selection.getSelectionStart(content);
        int selectionEnd = Selection.getSelectionEnd(content);
        if (selectionEnd < selectionStart) {
            selectionEnd = selectionStart;
            selectionStart = selectionEnd;
        }
        if (selectionStart == selectionEnd) {
            return false;
        }
        content.delete(selectionStart, selectionEnd);
        return true;
    }

    private boolean deleteLine(View view, Editable content) {
        Layout layout;
        int line;
        int start;
        int end;
        if (!(view instanceof TextView) || (layout = ((TextView) view).getLayout()) == null || (end = layout.getLineEnd(line)) == (start = layout.getLineStart((line = layout.getLineForOffset(Selection.getSelectionStart(content)))))) {
            return false;
        }
        content.delete(start, end);
        return true;
    }

    /* renamed from: android.text.method.BaseKeyListener$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$text$method$TextKeyListener$Capitalize = new int[TextKeyListener.Capitalize.values().length];

        static {
            try {
                $SwitchMap$android$text$method$TextKeyListener$Capitalize[TextKeyListener.Capitalize.CHARACTERS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$text$method$TextKeyListener$Capitalize[TextKeyListener.Capitalize.WORDS.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$text$method$TextKeyListener$Capitalize[TextKeyListener.Capitalize.SENTENCES.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    static int makeTextContentType(TextKeyListener.Capitalize caps, boolean autoText) {
        int contentType = 1;
        switch (AnonymousClass1.$SwitchMap$android$text$method$TextKeyListener$Capitalize[caps.ordinal()]) {
            case 1:
                contentType = 1 | 4096;
                break;
            case 2:
                contentType = 1 | 8192;
                break;
            case 3:
                contentType = 1 | 16384;
                break;
        }
        if (autoText) {
            return contentType | 32768;
        }
        return contentType;
    }

    @Override // android.text.method.KeyListener, android.text.method.MetaKeyKeyListener
    public boolean onKeyDown(View view, Editable content, int keyCode, KeyEvent event) {
        boolean handled;
        switch (keyCode) {
            case 67:
                handled = backspace(view, content, keyCode, event);
                break;
            case 112:
                handled = forwardDelete(view, content, keyCode, event);
                break;
            default:
                handled = false;
                break;
        }
        if (handled) {
            adjustMetaAfterKeypress(content);
        }
        return super.onKeyDown(view, content, keyCode, event);
    }

    @Override // android.text.method.KeyListener
    public boolean onKeyOther(View view, Editable content, KeyEvent event) {
        if (event.getAction() != 2 || event.getKeyCode() != 0) {
            return false;
        }
        int selectionStart = Selection.getSelectionStart(content);
        int selectionEnd = Selection.getSelectionEnd(content);
        if (selectionEnd < selectionStart) {
            selectionEnd = selectionStart;
            selectionStart = selectionEnd;
        }
        CharSequence text = event.getCharacters();
        if (text == null) {
            return false;
        }
        content.replace(selectionStart, selectionEnd, text);
        return true;
    }
}
