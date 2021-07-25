package android.text.method;

import android.graphics.Rect;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;

public class ArrowKeyMovementMethod extends BaseMovementMethod implements MovementMethod {
    private static final Object LAST_TAP_DOWN = new Object();
    private static ArrowKeyMovementMethod sInstance;

    private static boolean isSelecting(Spannable buffer) {
        return MetaKeyKeyListener.getMetaState(buffer, 1) == 1 || MetaKeyKeyListener.getMetaState(buffer, 2048) != 0;
    }

    private static int getCurrentLineTop(Spannable buffer, Layout layout) {
        return layout.getLineTop(layout.getLineForOffset(Selection.getSelectionEnd(buffer)));
    }

    private static int getPageHeight(TextView widget) {
        Rect rect = new Rect();
        if (widget.getGlobalVisibleRect(rect)) {
            return rect.height();
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean handleMovementKey(TextView widget, Spannable buffer, int keyCode, int movementMetaState, KeyEvent event) {
        switch (keyCode) {
            case 23:
                if (KeyEvent.metaStateHasNoModifiers(movementMetaState) && event.getAction() == 0 && event.getRepeatCount() == 0 && MetaKeyKeyListener.getMetaState(buffer, 2048, event) != 0) {
                    return widget.showContextMenu();
                }
        }
        return super.handleMovementKey(widget, buffer, keyCode, movementMetaState, event);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean left(TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        if (isSelecting(buffer)) {
            return Selection.extendLeft(buffer, layout);
        }
        return Selection.moveLeft(buffer, layout);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean right(TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        if (isSelecting(buffer)) {
            return Selection.extendRight(buffer, layout);
        }
        return Selection.moveRight(buffer, layout);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean up(TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        if (isSelecting(buffer)) {
            return Selection.extendUp(buffer, layout);
        }
        return Selection.moveUp(buffer, layout);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean down(TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        if (isSelecting(buffer)) {
            return Selection.extendDown(buffer, layout);
        }
        return Selection.moveDown(buffer, layout);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean pageUp(TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        boolean selecting = isSelecting(buffer);
        int targetY = getCurrentLineTop(buffer, layout) - getPageHeight(widget);
        boolean handled = false;
        do {
            int previousSelectionEnd = Selection.getSelectionEnd(buffer);
            if (selecting) {
                Selection.extendUp(buffer, layout);
            } else {
                Selection.moveUp(buffer, layout);
            }
            if (Selection.getSelectionEnd(buffer) == previousSelectionEnd) {
                break;
            }
            handled = true;
        } while (getCurrentLineTop(buffer, layout) > targetY);
        return handled;
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean pageDown(TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        boolean selecting = isSelecting(buffer);
        int targetY = getCurrentLineTop(buffer, layout) + getPageHeight(widget);
        boolean handled = false;
        do {
            int previousSelectionEnd = Selection.getSelectionEnd(buffer);
            if (selecting) {
                Selection.extendDown(buffer, layout);
            } else {
                Selection.moveDown(buffer, layout);
            }
            if (Selection.getSelectionEnd(buffer) == previousSelectionEnd) {
                break;
            }
            handled = true;
        } while (getCurrentLineTop(buffer, layout) < targetY);
        return handled;
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean top(TextView widget, Spannable buffer) {
        if (isSelecting(buffer)) {
            Selection.extendSelection(buffer, 0);
            return true;
        }
        Selection.setSelection(buffer, 0);
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean bottom(TextView widget, Spannable buffer) {
        if (isSelecting(buffer)) {
            Selection.extendSelection(buffer, buffer.length());
            return true;
        }
        Selection.setSelection(buffer, buffer.length());
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean lineStart(TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        if (isSelecting(buffer)) {
            return Selection.extendToLeftEdge(buffer, layout);
        }
        return Selection.moveToLeftEdge(buffer, layout);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean lineEnd(TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        if (isSelecting(buffer)) {
            return Selection.extendToRightEdge(buffer, layout);
        }
        return Selection.moveToRightEdge(buffer, layout);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean leftWord(TextView widget, Spannable buffer) {
        int selectionEnd = widget.getSelectionEnd();
        WordIterator wordIterator = widget.getWordIterator();
        wordIterator.setCharSequence(buffer, selectionEnd, selectionEnd);
        return Selection.moveToPreceding(buffer, wordIterator, isSelecting(buffer));
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean rightWord(TextView widget, Spannable buffer) {
        int selectionEnd = widget.getSelectionEnd();
        WordIterator wordIterator = widget.getWordIterator();
        wordIterator.setCharSequence(buffer, selectionEnd, selectionEnd);
        return Selection.moveToFollowing(buffer, wordIterator, isSelecting(buffer));
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean home(TextView widget, Spannable buffer) {
        return lineStart(widget, buffer);
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.BaseMovementMethod
    public boolean end(TextView widget, Spannable buffer) {
        return lineEnd(widget, buffer);
    }

    private static boolean isTouchSelecting(boolean isMouse, Spannable buffer) {
        return isMouse ? Touch.isActivelySelecting(buffer) : isSelecting(buffer);
    }

    @Override // android.text.method.BaseMovementMethod, android.text.method.MovementMethod
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int initialScrollX = -1;
        int initialScrollY = -1;
        int action = event.getAction();
        boolean isMouse = event.isFromSource(8194);
        if (action == 1) {
            initialScrollX = Touch.getInitialScrollX(widget, buffer);
            initialScrollY = Touch.getInitialScrollY(widget, buffer);
        }
        boolean handled = Touch.onTouchEvent(widget, buffer, event);
        if (!widget.isFocused() || widget.didTouchFocusSelect()) {
            return handled;
        }
        if (action == 0) {
            if (!isMouse && !isTouchSelecting(isMouse, buffer)) {
                return handled;
            }
            int offset = widget.getOffsetForPosition(event.getX(), event.getY());
            buffer.setSpan(LAST_TAP_DOWN, offset, offset, 34);
            widget.getParent().requestDisallowInterceptTouchEvent(true);
            return handled;
        } else if (action == 2) {
            if (isMouse && Touch.isSelectionStarted(buffer)) {
                Selection.setSelection(buffer, buffer.getSpanStart(LAST_TAP_DOWN));
            }
            if (!isTouchSelecting(isMouse, buffer) || !handled) {
                return handled;
            }
            widget.cancelLongPress();
            Selection.extendSelection(buffer, widget.getOffsetForPosition(event.getX(), event.getY()));
            return true;
        } else if (action != 1) {
            return handled;
        } else {
            if ((initialScrollY < 0 || initialScrollY == widget.getScrollY()) && (initialScrollX < 0 || initialScrollX == widget.getScrollX())) {
                int offset2 = widget.getOffsetForPosition(event.getX(), event.getY());
                if (isTouchSelecting(isMouse, buffer)) {
                    buffer.removeSpan(LAST_TAP_DOWN);
                    Selection.extendSelection(buffer, offset2);
                }
                MetaKeyKeyListener.adjustMetaAfterKeypress(buffer);
                MetaKeyKeyListener.resetLockedMeta(buffer);
                return true;
            }
            widget.moveCursorToVisibleOffset();
            return true;
        }
    }

    @Override // android.text.method.BaseMovementMethod, android.text.method.MovementMethod
    public boolean canSelectArbitrarily() {
        return true;
    }

    @Override // android.text.method.BaseMovementMethod, android.text.method.MovementMethod
    public void initialize(TextView widget, Spannable text) {
        Selection.setSelection(text, 0);
    }

    @Override // android.text.method.BaseMovementMethod, android.text.method.MovementMethod
    public void onTakeFocus(TextView view, Spannable text, int dir) {
        if ((dir & 130) == 0) {
            Selection.setSelection(text, text.length());
        } else if (view.getLayout() == null) {
            Selection.setSelection(text, text.length());
        }
    }

    public static MovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new ArrowKeyMovementMethod();
        }
        return sInstance;
    }
}
