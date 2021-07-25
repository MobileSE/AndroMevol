package android.widget;

import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.UndoManager;
import android.content.UndoOperation;
import android.content.UndoOwner;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.ExtractEditText;
import android.net.ProxyInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.UserDictionary;
import android.text.DynamicLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.ParcelableSpan;
import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.method.KeyListener;
import android.text.method.MetaKeyKeyListener;
import android.text.method.MovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.WordIterator;
import android.text.style.EasyEditSpan;
import android.text.style.SuggestionRangeSpan;
import android.text.style.SuggestionSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.DragEvent;
import android.view.HardwareCanvas;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.RenderNode;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.widget.EditableInputConnection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class Editor {
    static final int BLINK = 500;
    static final boolean DEBUG_UNDO = false;
    private static int DRAG_SHADOW_MAX_TEXT_LENGTH = 20;
    static final int EXTRACT_NOTHING = -2;
    static final int EXTRACT_UNKNOWN = -1;
    private static final String TAG = "Editor";
    private static final float[] TEMP_POSITION = new float[2];
    Blink mBlink;
    CorrectionHighlighter mCorrectionHighlighter;
    boolean mCreatedWithASelection;
    final CursorAnchorInfoNotifier mCursorAnchorInfoNotifier = new CursorAnchorInfoNotifier();
    int mCursorCount;
    final Drawable[] mCursorDrawable = new Drawable[2];
    boolean mCursorVisible = true;
    ActionMode.Callback mCustomSelectionActionModeCallback;
    boolean mDiscardNextActionUp;
    CharSequence mError;
    ErrorPopup mErrorPopup;
    boolean mErrorWasChanged;
    boolean mFrozenWithFocus;
    boolean mIgnoreActionUpEvent;
    boolean mInBatchEditControllers;
    InputContentType mInputContentType;
    InputMethodState mInputMethodState;
    int mInputType = 0;
    boolean mInsertionControllerEnabled;
    InsertionPointCursorController mInsertionPointCursorController;
    KeyListener mKeyListener;
    float mLastDownPositionX;
    float mLastDownPositionY;
    private PositionListener mPositionListener;
    boolean mPreserveDetachedSelection;
    boolean mSelectAllOnFocus;
    private Drawable mSelectHandleCenter;
    private Drawable mSelectHandleLeft;
    private Drawable mSelectHandleRight;
    ActionMode mSelectionActionMode;
    boolean mSelectionControllerEnabled;
    SelectionModifierCursorController mSelectionModifierCursorController;
    boolean mSelectionMoved;
    long mShowCursor;
    boolean mShowErrorAfterAttach;
    boolean mShowSoftInputOnFocus = true;
    Runnable mShowSuggestionRunnable;
    private SpanController mSpanController;
    SpellChecker mSpellChecker;
    SuggestionRangeSpan mSuggestionRangeSpan;
    SuggestionsPopupWindow mSuggestionsPopupWindow;
    private Rect mTempRect;
    boolean mTemporaryDetach;
    TextDisplayList[] mTextDisplayLists;
    boolean mTextIsSelectable;
    private TextView mTextView;
    boolean mTouchFocusSelected;
    InputFilter mUndoInputFilter;
    UndoManager mUndoManager;
    UndoOwner mUndoOwner;
    WordIterator mWordIterator;

    private interface CursorController extends ViewTreeObserver.OnTouchModeChangeListener {
        void hide();

        void onDetached();

        void show();
    }

    /* access modifiers changed from: private */
    public interface EasyEditDeleteListener {
        void onDeleteClick(EasyEditSpan easyEditSpan);
    }

    /* access modifiers changed from: private */
    public interface TextViewPositionListener {
        void updatePosition(int i, int i2, boolean z, boolean z2);
    }

    /* access modifiers changed from: private */
    public static class TextDisplayList {
        RenderNode displayList;
        boolean isDirty = true;

        public TextDisplayList(String name) {
            this.displayList = RenderNode.create(name, null);
        }

        /* access modifiers changed from: package-private */
        public boolean needsRecord() {
            return this.isDirty || !this.displayList.isValid();
        }
    }

    Editor(TextView textView) {
        this.mTextView = textView;
    }

    /* access modifiers changed from: package-private */
    public void onAttachedToWindow() {
        if (this.mShowErrorAfterAttach) {
            showError();
            this.mShowErrorAfterAttach = false;
        }
        this.mTemporaryDetach = false;
        ViewTreeObserver observer = this.mTextView.getViewTreeObserver();
        if (this.mInsertionPointCursorController != null) {
            observer.addOnTouchModeChangeListener(this.mInsertionPointCursorController);
        }
        if (this.mSelectionModifierCursorController != null) {
            this.mSelectionModifierCursorController.resetTouchOffsets();
            observer.addOnTouchModeChangeListener(this.mSelectionModifierCursorController);
        }
        updateSpellCheckSpans(0, this.mTextView.getText().length(), true);
        if (this.mTextView.hasTransientState() && this.mTextView.getSelectionStart() != this.mTextView.getSelectionEnd()) {
            this.mTextView.setHasTransientState(false);
            startSelectionActionMode();
        }
        getPositionListener().addSubscriber(this.mCursorAnchorInfoNotifier, true);
    }

    /* access modifiers changed from: package-private */
    public void onDetachedFromWindow() {
        getPositionListener().removeSubscriber(this.mCursorAnchorInfoNotifier);
        if (this.mError != null) {
            hideError();
        }
        if (this.mBlink != null) {
            this.mBlink.removeCallbacks(this.mBlink);
        }
        if (this.mInsertionPointCursorController != null) {
            this.mInsertionPointCursorController.onDetached();
        }
        if (this.mSelectionModifierCursorController != null) {
            this.mSelectionModifierCursorController.onDetached();
        }
        if (this.mShowSuggestionRunnable != null) {
            this.mTextView.removeCallbacks(this.mShowSuggestionRunnable);
        }
        destroyDisplayListsData();
        if (this.mSpellChecker != null) {
            this.mSpellChecker.closeSession();
            this.mSpellChecker = null;
        }
        this.mPreserveDetachedSelection = true;
        hideControllers();
        this.mPreserveDetachedSelection = false;
        this.mTemporaryDetach = false;
    }

    private void destroyDisplayListsData() {
        if (this.mTextDisplayLists != null) {
            for (int i = 0; i < this.mTextDisplayLists.length; i++) {
                RenderNode displayList = this.mTextDisplayLists[i] != null ? this.mTextDisplayLists[i].displayList : null;
                if (displayList != null && displayList.isValid()) {
                    displayList.destroyDisplayListData();
                }
            }
        }
    }

    private void showError() {
        if (this.mTextView.getWindowToken() == null) {
            this.mShowErrorAfterAttach = true;
            return;
        }
        if (this.mErrorPopup == null) {
            float scale = this.mTextView.getResources().getDisplayMetrics().density;
            this.mErrorPopup = new ErrorPopup((TextView) LayoutInflater.from(this.mTextView.getContext()).inflate(R.layout.textview_hint, (ViewGroup) null), (int) ((200.0f * scale) + 0.5f), (int) ((50.0f * scale) + 0.5f));
            this.mErrorPopup.setFocusable(false);
            this.mErrorPopup.setInputMethodMode(1);
        }
        TextView tv = (TextView) this.mErrorPopup.getContentView();
        chooseSize(this.mErrorPopup, this.mError, tv);
        tv.setText(this.mError);
        this.mErrorPopup.showAsDropDown(this.mTextView, getErrorX(), getErrorY());
        this.mErrorPopup.fixDirection(this.mErrorPopup.isAboveAnchor());
    }

    public void setError(CharSequence error, Drawable icon) {
        this.mError = TextUtils.stringOrSpannedString(error);
        this.mErrorWasChanged = true;
        if (this.mError == null) {
            setErrorIcon(null);
            if (this.mErrorPopup != null) {
                if (this.mErrorPopup.isShowing()) {
                    this.mErrorPopup.dismiss();
                }
                this.mErrorPopup = null;
            }
            this.mShowErrorAfterAttach = false;
            return;
        }
        setErrorIcon(icon);
        if (this.mTextView.isFocused()) {
            showError();
        }
    }

    private void setErrorIcon(Drawable icon) {
        TextView.Drawables dr = this.mTextView.mDrawables;
        if (dr == null) {
            TextView textView = this.mTextView;
            dr = new TextView.Drawables(this.mTextView.getContext());
            textView.mDrawables = dr;
        }
        dr.setErrorDrawable(icon, this.mTextView);
        this.mTextView.resetResolvedDrawables();
        this.mTextView.invalidate();
        this.mTextView.requestLayout();
    }

    private void hideError() {
        if (this.mErrorPopup != null && this.mErrorPopup.isShowing()) {
            this.mErrorPopup.dismiss();
        }
        this.mShowErrorAfterAttach = false;
    }

    private int getErrorX() {
        int i = 0;
        float scale = this.mTextView.getResources().getDisplayMetrics().density;
        TextView.Drawables dr = this.mTextView.mDrawables;
        switch (this.mTextView.getLayoutDirection()) {
            case 1:
                if (dr != null) {
                    i = dr.mDrawableSizeLeft;
                }
                return this.mTextView.getPaddingLeft() + ((i / 2) - ((int) ((25.0f * scale) + 0.5f)));
            default:
                if (dr != null) {
                    i = dr.mDrawableSizeRight;
                }
                return ((this.mTextView.getWidth() - this.mErrorPopup.getWidth()) - this.mTextView.getPaddingRight()) + ((-i) / 2) + ((int) ((25.0f * scale) + 0.5f));
        }
    }

    private int getErrorY() {
        int height = 0;
        int compoundPaddingTop = this.mTextView.getCompoundPaddingTop();
        int vspace = ((this.mTextView.getBottom() - this.mTextView.getTop()) - this.mTextView.getCompoundPaddingBottom()) - compoundPaddingTop;
        TextView.Drawables dr = this.mTextView.mDrawables;
        switch (this.mTextView.getLayoutDirection()) {
            case 1:
                if (dr != null) {
                    height = dr.mDrawableHeightLeft;
                    break;
                }
                break;
            default:
                if (dr != null) {
                    height = dr.mDrawableHeightRight;
                    break;
                }
                break;
        }
        return (((compoundPaddingTop + ((vspace - height) / 2)) + height) - this.mTextView.getHeight()) - ((int) ((2.0f * this.mTextView.getResources().getDisplayMetrics().density) + 0.5f));
    }

    /* access modifiers changed from: package-private */
    public void createInputContentTypeIfNeeded() {
        if (this.mInputContentType == null) {
            this.mInputContentType = new InputContentType();
        }
    }

    /* access modifiers changed from: package-private */
    public void createInputMethodStateIfNeeded() {
        if (this.mInputMethodState == null) {
            this.mInputMethodState = new InputMethodState();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isCursorVisible() {
        return this.mCursorVisible && this.mTextView.isTextEditable();
    }

    /* access modifiers changed from: package-private */
    public void prepareCursorControllers() {
        boolean enabled;
        boolean z;
        boolean z2 = true;
        boolean windowSupportsHandles = false;
        ViewGroup.LayoutParams params = this.mTextView.getRootView().getLayoutParams();
        if (params instanceof WindowManager.LayoutParams) {
            WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) params;
            if (windowParams.type < 1000 || windowParams.type > 1999) {
                windowSupportsHandles = true;
            } else {
                windowSupportsHandles = false;
            }
        }
        if (!windowSupportsHandles || this.mTextView.getLayout() == null) {
            enabled = false;
        } else {
            enabled = true;
        }
        if (!enabled || !isCursorVisible()) {
            z = false;
        } else {
            z = true;
        }
        this.mInsertionControllerEnabled = z;
        if (!enabled || !this.mTextView.textCanBeSelected()) {
            z2 = false;
        }
        this.mSelectionControllerEnabled = z2;
        if (!this.mInsertionControllerEnabled) {
            hideInsertionPointCursorController();
            if (this.mInsertionPointCursorController != null) {
                this.mInsertionPointCursorController.onDetached();
                this.mInsertionPointCursorController = null;
            }
        }
        if (!this.mSelectionControllerEnabled) {
            stopSelectionActionMode();
            if (this.mSelectionModifierCursorController != null) {
                this.mSelectionModifierCursorController.onDetached();
                this.mSelectionModifierCursorController = null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideInsertionPointCursorController() {
        if (this.mInsertionPointCursorController != null) {
            this.mInsertionPointCursorController.hide();
        }
    }

    /* access modifiers changed from: package-private */
    public void hideControllers() {
        hideCursorControllers();
        hideSpanControllers();
    }

    private void hideSpanControllers() {
        if (this.mSpanController != null) {
            this.mSpanController.hide();
        }
    }

    private void hideCursorControllers() {
        if (this.mSuggestionsPopupWindow != null && !this.mSuggestionsPopupWindow.isShowingUp()) {
            this.mSuggestionsPopupWindow.hide();
        }
        hideInsertionPointCursorController();
        stopSelectionActionMode();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSpellCheckSpans(int start, int end, boolean createSpellChecker) {
        this.mTextView.removeAdjacentSuggestionSpans(start);
        this.mTextView.removeAdjacentSuggestionSpans(end);
        if (this.mTextView.isTextEditable() && this.mTextView.isSuggestionsEnabled() && !(this.mTextView instanceof ExtractEditText)) {
            if (this.mSpellChecker == null && createSpellChecker) {
                this.mSpellChecker = new SpellChecker(this.mTextView);
            }
            if (this.mSpellChecker != null) {
                this.mSpellChecker.spellCheck(start, end);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onScreenStateChanged(int screenState) {
        switch (screenState) {
            case 0:
                suspendBlink();
                return;
            case 1:
                resumeBlink();
                return;
            default:
                return;
        }
    }

    private void suspendBlink() {
        if (this.mBlink != null) {
            this.mBlink.cancel();
        }
    }

    private void resumeBlink() {
        if (this.mBlink != null) {
            this.mBlink.uncancel();
            makeBlink();
        }
    }

    /* access modifiers changed from: package-private */
    public void adjustInputType(boolean password, boolean passwordInputType, boolean webPasswordInputType, boolean numberPasswordInputType) {
        if ((this.mInputType & 15) == 1) {
            if (password || passwordInputType) {
                this.mInputType = (this.mInputType & -4081) | 128;
            }
            if (webPasswordInputType) {
                this.mInputType = (this.mInputType & -4081) | 224;
            }
        } else if ((this.mInputType & 15) == 2 && numberPasswordInputType) {
            this.mInputType = (this.mInputType & -4081) | 16;
        }
    }

    private void chooseSize(PopupWindow pop, CharSequence text, TextView tv) {
        int wid = tv.getPaddingLeft() + tv.getPaddingRight();
        int ht = tv.getPaddingTop() + tv.getPaddingBottom();
        Layout l = new StaticLayout(text, tv.getPaint(), this.mTextView.getResources().getDimensionPixelSize(R.dimen.textview_error_popup_default_width), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
        float max = 0.0f;
        for (int i = 0; i < l.getLineCount(); i++) {
            max = Math.max(max, l.getLineWidth(i));
        }
        pop.setWidth(((int) Math.ceil((double) max)) + wid);
        pop.setHeight(l.getHeight() + ht);
    }

    /* access modifiers changed from: package-private */
    public void setFrame() {
        if (this.mErrorPopup != null) {
            chooseSize(this.mErrorPopup, this.mError, (TextView) this.mErrorPopup.getContentView());
            this.mErrorPopup.update(this.mTextView, getErrorX(), getErrorY(), this.mErrorPopup.getWidth(), this.mErrorPopup.getHeight());
        }
    }

    private boolean canSelectText() {
        return hasSelectionController() && this.mTextView.getText().length() != 0;
    }

    private boolean hasPasswordTransformationMethod() {
        return this.mTextView.getTransformationMethod() instanceof PasswordTransformationMethod;
    }

    private boolean selectCurrentWord() {
        int selectionStart;
        int selectionEnd;
        if (!canSelectText()) {
            return false;
        }
        if (hasPasswordTransformationMethod()) {
            return this.mTextView.selectAllText();
        }
        int inputType = this.mTextView.getInputType();
        int klass = inputType & 15;
        int variation = inputType & InputType.TYPE_MASK_VARIATION;
        if (klass == 2 || klass == 3 || klass == 4 || variation == 16 || variation == 32 || variation == 208 || variation == 176) {
            return this.mTextView.selectAllText();
        }
        long lastTouchOffsets = getLastTouchOffsets();
        int minOffset = TextUtils.unpackRangeStartFromLong(lastTouchOffsets);
        int maxOffset = TextUtils.unpackRangeEndFromLong(lastTouchOffsets);
        if (minOffset < 0 || minOffset >= this.mTextView.getText().length() || maxOffset < 0 || maxOffset >= this.mTextView.getText().length()) {
            return false;
        }
        URLSpan[] urlSpans = (URLSpan[]) ((Spanned) this.mTextView.getText()).getSpans(minOffset, maxOffset, URLSpan.class);
        if (urlSpans.length >= 1) {
            URLSpan urlSpan = urlSpans[0];
            selectionStart = ((Spanned) this.mTextView.getText()).getSpanStart(urlSpan);
            selectionEnd = ((Spanned) this.mTextView.getText()).getSpanEnd(urlSpan);
        } else {
            WordIterator wordIterator = getWordIterator();
            wordIterator.setCharSequence(this.mTextView.getText(), minOffset, maxOffset);
            selectionStart = wordIterator.getBeginning(minOffset);
            selectionEnd = wordIterator.getEnd(maxOffset);
            if (selectionStart == -1 || selectionEnd == -1 || selectionStart == selectionEnd) {
                long range = getCharRange(minOffset);
                selectionStart = TextUtils.unpackRangeStartFromLong(range);
                selectionEnd = TextUtils.unpackRangeEndFromLong(range);
            }
        }
        Selection.setSelection((Spannable) this.mTextView.getText(), selectionStart, selectionEnd);
        if (selectionEnd > selectionStart) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void onLocaleChanged() {
        this.mWordIterator = null;
    }

    public WordIterator getWordIterator() {
        if (this.mWordIterator == null) {
            this.mWordIterator = new WordIterator(this.mTextView.getTextServicesLocale());
        }
        return this.mWordIterator;
    }

    private long getCharRange(int offset) {
        int textLength = this.mTextView.getText().length();
        if (offset + 1 < textLength && Character.isSurrogatePair(this.mTextView.getText().charAt(offset), this.mTextView.getText().charAt(offset + 1))) {
            return TextUtils.packRangeInLong(offset, offset + 2);
        }
        if (offset < textLength) {
            return TextUtils.packRangeInLong(offset, offset + 1);
        }
        if (offset - 2 >= 0) {
            if (Character.isSurrogatePair(this.mTextView.getText().charAt(offset - 2), this.mTextView.getText().charAt(offset - 1))) {
                return TextUtils.packRangeInLong(offset - 2, offset);
            }
        }
        if (offset - 1 >= 0) {
            return TextUtils.packRangeInLong(offset - 1, offset);
        }
        return TextUtils.packRangeInLong(offset, offset);
    }

    private boolean touchPositionIsInSelection() {
        int selectionStart = this.mTextView.getSelectionStart();
        int selectionEnd = this.mTextView.getSelectionEnd();
        if (selectionStart == selectionEnd) {
            return false;
        }
        if (selectionStart > selectionEnd) {
            selectionStart = selectionEnd;
            selectionEnd = selectionStart;
            Selection.setSelection((Spannable) this.mTextView.getText(), selectionStart, selectionEnd);
        }
        SelectionModifierCursorController selectionController = getSelectionController();
        return selectionController.getMinTouchOffset() >= selectionStart && selectionController.getMaxTouchOffset() < selectionEnd;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private PositionListener getPositionListener() {
        if (this.mPositionListener == null) {
            this.mPositionListener = new PositionListener();
        }
        return this.mPositionListener;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPositionVisible(float positionX, float positionY) {
        synchronized (TEMP_POSITION) {
            float[] position = TEMP_POSITION;
            position[0] = positionX;
            position[1] = positionY;
            View view = this.mTextView;
            while (view != null) {
                if (view != this.mTextView) {
                    position[0] = position[0] - ((float) view.getScrollX());
                    position[1] = position[1] - ((float) view.getScrollY());
                }
                if (position[0] < 0.0f || position[1] < 0.0f || position[0] > ((float) view.getWidth()) || position[1] > ((float) view.getHeight())) {
                    return false;
                }
                if (!view.getMatrix().isIdentity()) {
                    view.getMatrix().mapPoints(position);
                }
                position[0] = position[0] + ((float) view.getLeft());
                position[1] = position[1] + ((float) view.getTop());
                ViewParent parent = view.getParent();
                if (parent instanceof View) {
                    view = (View) parent;
                } else {
                    view = null;
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isOffsetVisible(int offset) {
        Layout layout = this.mTextView.getLayout();
        if (layout == null) {
            return false;
        }
        int lineBottom = layout.getLineBottom(layout.getLineForOffset(offset));
        return isPositionVisible((float) (this.mTextView.viewportToContentHorizontalOffset() + ((int) layout.getPrimaryHorizontal(offset))), (float) (this.mTextView.viewportToContentVerticalOffset() + lineBottom));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPositionOnText(float x, float y) {
        Layout layout = this.mTextView.getLayout();
        if (layout == null) {
            return false;
        }
        int line = this.mTextView.getLineAtCoordinate(y);
        float x2 = this.mTextView.convertToLocalHorizontalCoordinate(x);
        if (x2 < layout.getLineLeft(line) || x2 > layout.getLineRight(line)) {
            return false;
        }
        return true;
    }

    public boolean performLongClick(boolean handled) {
        if (!handled && !isPositionOnText(this.mLastDownPositionX, this.mLastDownPositionY) && this.mInsertionControllerEnabled) {
            int offset = this.mTextView.getOffsetForPosition(this.mLastDownPositionX, this.mLastDownPositionY);
            stopSelectionActionMode();
            Selection.setSelection((Spannable) this.mTextView.getText(), offset);
            getInsertionController().showWithActionPopup();
            handled = true;
        }
        if (!handled && this.mSelectionActionMode != null) {
            if (touchPositionIsInSelection()) {
                int start = this.mTextView.getSelectionStart();
                int end = this.mTextView.getSelectionEnd();
                CharSequence selectedText = this.mTextView.getTransformedText(start, end);
                this.mTextView.startDrag(ClipData.newPlainText(null, selectedText), getTextThumbnailBuilder(selectedText), new DragLocalState(this.mTextView, start, end), 0);
                stopSelectionActionMode();
            } else {
                getSelectionController().hide();
                selectCurrentWord();
                getSelectionController().show();
            }
            handled = true;
        }
        if (!handled) {
            return startSelectionActionMode();
        }
        return handled;
    }

    private long getLastTouchOffsets() {
        SelectionModifierCursorController selectionController = getSelectionController();
        return TextUtils.packRangeInLong(selectionController.getMinTouchOffset(), selectionController.getMaxTouchOffset());
    }

    /* access modifiers changed from: package-private */
    public void onFocusChanged(boolean focused, int direction) {
        boolean isFocusHighlighted;
        boolean z;
        this.mShowCursor = SystemClock.uptimeMillis();
        ensureEndedBatchEdit();
        if (focused) {
            int selStart = this.mTextView.getSelectionStart();
            int selEnd = this.mTextView.getSelectionEnd();
            if (this.mSelectAllOnFocus && selStart == 0 && selEnd == this.mTextView.getText().length()) {
                isFocusHighlighted = true;
            } else {
                isFocusHighlighted = false;
            }
            if (!this.mFrozenWithFocus || !this.mTextView.hasSelection() || isFocusHighlighted) {
                z = false;
            } else {
                z = true;
            }
            this.mCreatedWithASelection = z;
            if (!this.mFrozenWithFocus || selStart < 0 || selEnd < 0) {
                int lastTapPosition = getLastTapPosition();
                if (lastTapPosition >= 0) {
                    Selection.setSelection((Spannable) this.mTextView.getText(), lastTapPosition);
                }
                MovementMethod mMovement = this.mTextView.getMovementMethod();
                if (mMovement != null) {
                    mMovement.onTakeFocus(this.mTextView, (Spannable) this.mTextView.getText(), direction);
                }
                if (((this.mTextView instanceof ExtractEditText) || this.mSelectionMoved) && selStart >= 0 && selEnd >= 0) {
                    Selection.setSelection((Spannable) this.mTextView.getText(), selStart, selEnd);
                }
                if (this.mSelectAllOnFocus) {
                    this.mTextView.selectAllText();
                }
                this.mTouchFocusSelected = true;
            }
            this.mFrozenWithFocus = false;
            this.mSelectionMoved = false;
            if (this.mError != null) {
                showError();
            }
            makeBlink();
            return;
        }
        if (this.mError != null) {
            hideError();
        }
        this.mTextView.onEndBatchEdit();
        if (this.mTextView instanceof ExtractEditText) {
            int selStart2 = this.mTextView.getSelectionStart();
            int selEnd2 = this.mTextView.getSelectionEnd();
            hideControllers();
            Selection.setSelection((Spannable) this.mTextView.getText(), selStart2, selEnd2);
        } else {
            if (this.mTemporaryDetach) {
                this.mPreserveDetachedSelection = true;
            }
            hideControllers();
            if (this.mTemporaryDetach) {
                this.mPreserveDetachedSelection = false;
            }
            downgradeEasyCorrectionSpans();
        }
        if (this.mSelectionModifierCursorController != null) {
            this.mSelectionModifierCursorController.resetTouchOffsets();
        }
    }

    private void downgradeEasyCorrectionSpans() {
        CharSequence text = this.mTextView.getText();
        if (text instanceof Spannable) {
            Spannable spannable = (Spannable) text;
            SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) spannable.getSpans(0, spannable.length(), SuggestionSpan.class);
            for (int i = 0; i < suggestionSpans.length; i++) {
                int flags = suggestionSpans[i].getFlags();
                if ((flags & 1) != 0 && (flags & 2) == 0) {
                    suggestionSpans[i].setFlags(flags & -2);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void sendOnTextChanged(int start, int after) {
        updateSpellCheckSpans(start, start + after, false);
        hideCursorControllers();
    }

    private int getLastTapPosition() {
        int lastTapPosition;
        if (this.mSelectionModifierCursorController == null || (lastTapPosition = this.mSelectionModifierCursorController.getMinTouchOffset()) < 0) {
            return -1;
        }
        if (lastTapPosition > this.mTextView.getText().length()) {
            return this.mTextView.getText().length();
        }
        return lastTapPosition;
    }

    /* access modifiers changed from: package-private */
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) {
            if (this.mBlink != null) {
                this.mBlink.cancel();
            }
            if (this.mInputContentType != null) {
                this.mInputContentType.enterDown = false;
            }
            hideControllers();
            if (this.mSuggestionsPopupWindow != null) {
                this.mSuggestionsPopupWindow.onParentLostFocus();
            }
            ensureEndedBatchEdit();
        } else if (this.mBlink != null) {
            this.mBlink.uncancel();
            makeBlink();
        }
    }

    /* access modifiers changed from: package-private */
    public void onTouchEvent(MotionEvent event) {
        if (hasSelectionController()) {
            getSelectionController().onTouchEvent(event);
        }
        if (this.mShowSuggestionRunnable != null) {
            this.mTextView.removeCallbacks(this.mShowSuggestionRunnable);
            this.mShowSuggestionRunnable = null;
        }
        if (event.getActionMasked() == 0) {
            this.mLastDownPositionX = event.getX();
            this.mLastDownPositionY = event.getY();
            this.mTouchFocusSelected = false;
            this.mIgnoreActionUpEvent = false;
        }
    }

    public void beginBatchEdit() {
        this.mInBatchEditControllers = true;
        InputMethodState ims = this.mInputMethodState;
        if (ims != null) {
            int nesting = ims.mBatchEditNesting + 1;
            ims.mBatchEditNesting = nesting;
            if (nesting == 1) {
                ims.mCursorChanged = false;
                ims.mChangedDelta = 0;
                if (ims.mContentChanged) {
                    ims.mChangedStart = 0;
                    ims.mChangedEnd = this.mTextView.getText().length();
                } else {
                    ims.mChangedStart = -1;
                    ims.mChangedEnd = -1;
                    ims.mContentChanged = false;
                }
                this.mTextView.onBeginBatchEdit();
            }
        }
    }

    public void endBatchEdit() {
        this.mInBatchEditControllers = false;
        InputMethodState ims = this.mInputMethodState;
        if (ims != null) {
            int nesting = ims.mBatchEditNesting - 1;
            ims.mBatchEditNesting = nesting;
            if (nesting == 0) {
                finishBatchEdit(ims);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void ensureEndedBatchEdit() {
        InputMethodState ims = this.mInputMethodState;
        if (ims != null && ims.mBatchEditNesting != 0) {
            ims.mBatchEditNesting = 0;
            finishBatchEdit(ims);
        }
    }

    /* access modifiers changed from: package-private */
    public void finishBatchEdit(InputMethodState ims) {
        this.mTextView.onEndBatchEdit();
        if (ims.mContentChanged || ims.mSelectionModeChanged) {
            this.mTextView.updateAfterEdit();
            reportExtractedText();
        } else if (ims.mCursorChanged) {
            this.mTextView.invalidateCursor();
        }
        sendUpdateSelection();
    }

    /* access modifiers changed from: package-private */
    public boolean extractText(ExtractedTextRequest request, ExtractedText outText) {
        return extractTextInternal(request, -1, -1, -1, outText);
    }

    private boolean extractTextInternal(ExtractedTextRequest request, int partialStartOffset, int partialEndOffset, int delta, ExtractedText outText) {
        int partialEndOffset2;
        CharSequence content = this.mTextView.getText();
        if (content == null) {
            return false;
        }
        if (partialStartOffset != -2) {
            int N = content.length();
            if (partialStartOffset < 0) {
                outText.partialEndOffset = -1;
                outText.partialStartOffset = -1;
                partialStartOffset = 0;
                partialEndOffset2 = N;
            } else {
                partialEndOffset2 = partialEndOffset + delta;
                if (content instanceof Spanned) {
                    Spanned spanned = (Spanned) content;
                    Object[] spans = spanned.getSpans(partialStartOffset, partialEndOffset2, ParcelableSpan.class);
                    int i = spans.length;
                    while (i > 0) {
                        i--;
                        int j = spanned.getSpanStart(spans[i]);
                        if (j < partialStartOffset) {
                            partialStartOffset = j;
                        }
                        int j2 = spanned.getSpanEnd(spans[i]);
                        if (j2 > partialEndOffset2) {
                            partialEndOffset2 = j2;
                        }
                    }
                }
                outText.partialStartOffset = partialStartOffset;
                outText.partialEndOffset = partialEndOffset2 - delta;
                if (partialStartOffset > N) {
                    partialStartOffset = N;
                } else if (partialStartOffset < 0) {
                    partialStartOffset = 0;
                }
                if (partialEndOffset2 > N) {
                    partialEndOffset2 = N;
                } else if (partialEndOffset2 < 0) {
                    partialEndOffset2 = 0;
                }
            }
            if ((request.flags & 1) != 0) {
                outText.text = content.subSequence(partialStartOffset, partialEndOffset2);
            } else {
                outText.text = TextUtils.substring(content, partialStartOffset, partialEndOffset2);
            }
        } else {
            outText.partialStartOffset = 0;
            outText.partialEndOffset = 0;
            outText.text = ProxyInfo.LOCAL_EXCL_LIST;
        }
        outText.flags = 0;
        if (MetaKeyKeyListener.getMetaState(content, 2048) != 0) {
            outText.flags |= 2;
        }
        if (this.mTextView.isSingleLine()) {
            outText.flags |= 1;
        }
        outText.startOffset = 0;
        outText.selectionStart = this.mTextView.getSelectionStart();
        outText.selectionEnd = this.mTextView.getSelectionEnd();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean reportExtractedText() {
        boolean contentChanged;
        InputMethodManager imm;
        InputMethodState ims = this.mInputMethodState;
        if (ims != null && ((contentChanged = ims.mContentChanged) || ims.mSelectionModeChanged)) {
            ims.mContentChanged = false;
            ims.mSelectionModeChanged = false;
            ExtractedTextRequest req = ims.mExtractedTextRequest;
            if (!(req == null || (imm = InputMethodManager.peekInstance()) == null)) {
                if (ims.mChangedStart < 0 && !contentChanged) {
                    ims.mChangedStart = -2;
                }
                if (extractTextInternal(req, ims.mChangedStart, ims.mChangedEnd, ims.mChangedDelta, ims.mExtractedText)) {
                    imm.updateExtractedText(this.mTextView, req.token, ims.mExtractedText);
                    ims.mChangedStart = -1;
                    ims.mChangedEnd = -1;
                    ims.mChangedDelta = 0;
                    ims.mContentChanged = false;
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendUpdateSelection() {
        InputMethodManager imm;
        if (this.mInputMethodState != null && this.mInputMethodState.mBatchEditNesting <= 0 && (imm = InputMethodManager.peekInstance()) != null) {
            int selectionStart = this.mTextView.getSelectionStart();
            int selectionEnd = this.mTextView.getSelectionEnd();
            int candStart = -1;
            int candEnd = -1;
            if (this.mTextView.getText() instanceof Spannable) {
                Spannable sp = (Spannable) this.mTextView.getText();
                candStart = EditableInputConnection.getComposingSpanStart(sp);
                candEnd = EditableInputConnection.getComposingSpanEnd(sp);
            }
            imm.updateSelection(this.mTextView, selectionStart, selectionEnd, candStart, candEnd);
        }
    }

    /* access modifiers changed from: package-private */
    public void onDraw(Canvas canvas, Layout layout, Path highlight, Paint highlightPaint, int cursorOffsetVertical) {
        InputMethodManager imm;
        int selectionStart = this.mTextView.getSelectionStart();
        int selectionEnd = this.mTextView.getSelectionEnd();
        InputMethodState ims = this.mInputMethodState;
        if (ims != null && ims.mBatchEditNesting == 0 && (imm = InputMethodManager.peekInstance()) != null && imm.isActive(this.mTextView) && (ims.mContentChanged || ims.mSelectionModeChanged)) {
            reportExtractedText();
        }
        if (this.mCorrectionHighlighter != null) {
            this.mCorrectionHighlighter.draw(canvas, cursorOffsetVertical);
        }
        if (highlight != null && selectionStart == selectionEnd && this.mCursorCount > 0) {
            drawCursor(canvas, cursorOffsetVertical);
            highlight = null;
        }
        if (!this.mTextView.canHaveDisplayList() || !canvas.isHardwareAccelerated()) {
            layout.draw(canvas, highlight, highlightPaint, cursorOffsetVertical);
        } else {
            drawHardwareAccelerated(canvas, layout, highlight, highlightPaint, cursorOffsetVertical);
        }
    }

    private void drawHardwareAccelerated(Canvas canvas, Layout layout, Path highlight, Paint highlightPaint, int cursorOffsetVertical) {
        long lineRange = layout.getLineRangeForDraw(canvas);
        int firstLine = TextUtils.unpackRangeStartFromLong(lineRange);
        int lastLine = TextUtils.unpackRangeEndFromLong(lineRange);
        if (lastLine >= 0) {
            layout.drawBackground(canvas, highlight, highlightPaint, cursorOffsetVertical, firstLine, lastLine);
            if (layout instanceof DynamicLayout) {
                if (this.mTextDisplayLists == null) {
                    this.mTextDisplayLists = (TextDisplayList[]) ArrayUtils.emptyArray(TextDisplayList.class);
                }
                DynamicLayout dynamicLayout = (DynamicLayout) layout;
                int[] blockEndLines = dynamicLayout.getBlockEndLines();
                int[] blockIndices = dynamicLayout.getBlockIndices();
                int numberOfBlocks = dynamicLayout.getNumberOfBlocks();
                int indexFirstChangedBlock = dynamicLayout.getIndexFirstChangedBlock();
                int endOfPreviousBlock = -1;
                int searchStartIndex = 0;
                for (int i = 0; i < numberOfBlocks; i++) {
                    int blockEndLine = blockEndLines[i];
                    int blockIndex = blockIndices[i];
                    if (blockIndex == -1) {
                        blockIndex = getAvailableDisplayListIndex(blockIndices, numberOfBlocks, searchStartIndex);
                        blockIndices[i] = blockIndex;
                        searchStartIndex = blockIndex + 1;
                    }
                    if (this.mTextDisplayLists[blockIndex] == null) {
                        this.mTextDisplayLists[blockIndex] = new TextDisplayList("Text " + blockIndex);
                    }
                    boolean blockDisplayListIsInvalid = this.mTextDisplayLists[blockIndex].needsRecord();
                    RenderNode blockDisplayList = this.mTextDisplayLists[blockIndex].displayList;
                    if (i >= indexFirstChangedBlock || blockDisplayListIsInvalid) {
                        int blockBeginLine = endOfPreviousBlock + 1;
                        int top = layout.getLineTop(blockBeginLine);
                        int bottom = layout.getLineBottom(blockEndLine);
                        int left = 0;
                        int right = this.mTextView.getWidth();
                        if (this.mTextView.getHorizontallyScrolling()) {
                            float min = Float.MAX_VALUE;
                            float max = Float.MIN_VALUE;
                            for (int line = blockBeginLine; line <= blockEndLine; line++) {
                                min = Math.min(min, layout.getLineLeft(line));
                                max = Math.max(max, layout.getLineRight(line));
                            }
                            left = (int) min;
                            right = (int) (0.5f + max);
                        }
                        if (blockDisplayListIsInvalid) {
                            HardwareCanvas hardwareCanvas = blockDisplayList.start(right - left, bottom - top);
                            try {
                                hardwareCanvas.translate((float) (-left), (float) (-top));
                                layout.drawText(hardwareCanvas, blockBeginLine, blockEndLine);
                            } finally {
                                blockDisplayList.end(hardwareCanvas);
                                blockDisplayList.setClipToBounds(false);
                            }
                        }
                        blockDisplayList.setLeftTopRightBottom(left, top, right, bottom);
                    }
                    ((HardwareCanvas) canvas).drawRenderNode(blockDisplayList, null, 0);
                    endOfPreviousBlock = blockEndLine;
                }
                dynamicLayout.setIndexFirstChangedBlock(numberOfBlocks);
                return;
            }
            layout.drawText(canvas, firstLine, lastLine);
        }
    }

    private int getAvailableDisplayListIndex(int[] blockIndices, int numberOfBlocks, int searchStartIndex) {
        int length = this.mTextDisplayLists.length;
        for (int i = searchStartIndex; i < length; i++) {
            boolean blockIndexFound = false;
            int j = 0;
            while (true) {
                if (j >= numberOfBlocks) {
                    break;
                } else if (blockIndices[j] == i) {
                    blockIndexFound = true;
                    break;
                } else {
                    j++;
                }
            }
            if (!blockIndexFound) {
                return i;
            }
        }
        this.mTextDisplayLists = (TextDisplayList[]) GrowingArrayUtils.append(this.mTextDisplayLists, length, (Object) null);
        return length;
    }

    private void drawCursor(Canvas canvas, int cursorOffsetVertical) {
        boolean translate = cursorOffsetVertical != 0;
        if (translate) {
            canvas.translate(0.0f, (float) cursorOffsetVertical);
        }
        for (int i = 0; i < this.mCursorCount; i++) {
            this.mCursorDrawable[i].draw(canvas);
        }
        if (translate) {
            canvas.translate(0.0f, (float) (-cursorOffsetVertical));
        }
    }

    /* access modifiers changed from: package-private */
    public void invalidateTextDisplayList(Layout layout, int start, int end) {
        if (this.mTextDisplayLists != null && (layout instanceof DynamicLayout)) {
            int firstLine = layout.getLineForOffset(start);
            int lastLine = layout.getLineForOffset(end);
            DynamicLayout dynamicLayout = (DynamicLayout) layout;
            int[] blockEndLines = dynamicLayout.getBlockEndLines();
            int[] blockIndices = dynamicLayout.getBlockIndices();
            int numberOfBlocks = dynamicLayout.getNumberOfBlocks();
            int i = 0;
            while (i < numberOfBlocks && blockEndLines[i] < firstLine) {
                i++;
            }
            while (i < numberOfBlocks) {
                int blockIndex = blockIndices[i];
                if (blockIndex != -1) {
                    this.mTextDisplayLists[blockIndex].isDirty = true;
                }
                if (blockEndLines[i] < lastLine) {
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void invalidateTextDisplayList() {
        if (this.mTextDisplayLists != null) {
            for (int i = 0; i < this.mTextDisplayLists.length; i++) {
                if (this.mTextDisplayLists[i] != null) {
                    this.mTextDisplayLists[i].isDirty = true;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateCursorsPositions() {
        int i;
        if (this.mTextView.mCursorDrawableRes == 0) {
            this.mCursorCount = 0;
            return;
        }
        Layout layout = this.mTextView.getLayout();
        Layout hintLayout = this.mTextView.getHintLayout();
        int offset = this.mTextView.getSelectionStart();
        int line = layout.getLineForOffset(offset);
        int top = layout.getLineTop(line);
        int bottom = layout.getLineTop(line + 1);
        if (layout.isLevelBoundary(offset)) {
            i = 2;
        } else {
            i = 1;
        }
        this.mCursorCount = i;
        int middle = bottom;
        if (this.mCursorCount == 2) {
            middle = (top + bottom) >> 1;
        }
        boolean clamped = layout.shouldClampCursor(line);
        updateCursorPosition(0, top, middle, getPrimaryHorizontal(layout, hintLayout, offset, clamped));
        if (this.mCursorCount == 2) {
            updateCursorPosition(1, middle, bottom, layout.getSecondaryHorizontal(offset, clamped));
        }
    }

    private float getPrimaryHorizontal(Layout layout, Layout hintLayout, int offset, boolean clamped) {
        if (!TextUtils.isEmpty(layout.getText()) || hintLayout == null || TextUtils.isEmpty(hintLayout.getText())) {
            return layout.getPrimaryHorizontal(offset, clamped);
        }
        return hintLayout.getPrimaryHorizontal(offset, clamped);
    }

    /* access modifiers changed from: package-private */
    public boolean startSelectionActionMode() {
        boolean selectionStarted;
        InputMethodManager imm;
        if (this.mSelectionActionMode != null) {
            return false;
        }
        if (!canSelectText() || !this.mTextView.requestFocus()) {
            Log.w("TextView", "TextView does not support text selection. Action mode cancelled.");
            return false;
        } else if (!this.mTextView.hasSelection() && !selectCurrentWord()) {
            return false;
        } else {
            boolean willExtract = extractedTextModeWillBeStarted();
            if (!willExtract) {
                this.mSelectionActionMode = this.mTextView.startActionMode(new SelectionActionModeCallback());
            }
            if (this.mSelectionActionMode != null || willExtract) {
                selectionStarted = true;
            } else {
                selectionStarted = false;
            }
            if (!selectionStarted || this.mTextView.isTextSelectable() || !this.mShowSoftInputOnFocus || (imm = InputMethodManager.peekInstance()) == null) {
                return selectionStarted;
            }
            imm.showSoftInput(this.mTextView, 0, null);
            return selectionStarted;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean extractedTextModeWillBeStarted() {
        InputMethodManager imm;
        if ((this.mTextView instanceof ExtractEditText) || (imm = InputMethodManager.peekInstance()) == null || !imm.isFullscreenMode()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isCursorInsideSuggestionSpan() {
        CharSequence text = this.mTextView.getText();
        if ((text instanceof Spannable) && ((SuggestionSpan[]) ((Spannable) text).getSpans(this.mTextView.getSelectionStart(), this.mTextView.getSelectionEnd(), SuggestionSpan.class)).length > 0) {
            return true;
        }
        return false;
    }

    private boolean isCursorInsideEasyCorrectionSpan() {
        SuggestionSpan[] suggestionSpans;
        for (SuggestionSpan suggestionSpan : (SuggestionSpan[]) ((Spannable) this.mTextView.getText()).getSpans(this.mTextView.getSelectionStart(), this.mTextView.getSelectionEnd(), SuggestionSpan.class)) {
            if ((suggestionSpan.getFlags() & 1) != 0) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void onTouchUpEvent(MotionEvent event) {
        boolean selectAllGotFocus = this.mSelectAllOnFocus && this.mTextView.didTouchFocusSelect();
        hideControllers();
        CharSequence text = this.mTextView.getText();
        if (!selectAllGotFocus && text.length() > 0) {
            Selection.setSelection((Spannable) text, this.mTextView.getOffsetForPosition(event.getX(), event.getY()));
            if (this.mSpellChecker != null) {
                this.mSpellChecker.onSelectionChanged();
            }
            if (extractedTextModeWillBeStarted()) {
                return;
            }
            if (isCursorInsideEasyCorrectionSpan()) {
                this.mShowSuggestionRunnable = new Runnable() {
                    /* class android.widget.Editor.AnonymousClass1 */

                    public void run() {
                        Editor.this.showSuggestions();
                    }
                };
                this.mTextView.postDelayed(this.mShowSuggestionRunnable, (long) ViewConfiguration.getDoubleTapTimeout());
            } else if (hasInsertionController()) {
                getInsertionController().show();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void stopSelectionActionMode() {
        if (this.mSelectionActionMode != null) {
            this.mSelectionActionMode.finish();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasInsertionController() {
        return this.mInsertionControllerEnabled;
    }

    /* access modifiers changed from: package-private */
    public boolean hasSelectionController() {
        return this.mSelectionControllerEnabled;
    }

    /* access modifiers changed from: package-private */
    public InsertionPointCursorController getInsertionController() {
        if (!this.mInsertionControllerEnabled) {
            return null;
        }
        if (this.mInsertionPointCursorController == null) {
            this.mInsertionPointCursorController = new InsertionPointCursorController();
            this.mTextView.getViewTreeObserver().addOnTouchModeChangeListener(this.mInsertionPointCursorController);
        }
        return this.mInsertionPointCursorController;
    }

    /* access modifiers changed from: package-private */
    public SelectionModifierCursorController getSelectionController() {
        if (!this.mSelectionControllerEnabled) {
            return null;
        }
        if (this.mSelectionModifierCursorController == null) {
            this.mSelectionModifierCursorController = new SelectionModifierCursorController();
            this.mTextView.getViewTreeObserver().addOnTouchModeChangeListener(this.mSelectionModifierCursorController);
        }
        return this.mSelectionModifierCursorController;
    }

    private void updateCursorPosition(int cursorIndex, int top, int bottom, float horizontal) {
        if (this.mCursorDrawable[cursorIndex] == null) {
            this.mCursorDrawable[cursorIndex] = this.mTextView.getContext().getDrawable(this.mTextView.mCursorDrawableRes);
        }
        if (this.mTempRect == null) {
            this.mTempRect = new Rect();
        }
        this.mCursorDrawable[cursorIndex].getPadding(this.mTempRect);
        int width = this.mCursorDrawable[cursorIndex].getIntrinsicWidth();
        int left = ((int) Math.max(0.5f, horizontal - 0.5f)) - this.mTempRect.left;
        this.mCursorDrawable[cursorIndex].setBounds(left, top - this.mTempRect.top, left + width, this.mTempRect.bottom + bottom);
    }

    public void onCommitCorrection(CorrectionInfo info) {
        if (this.mCorrectionHighlighter == null) {
            this.mCorrectionHighlighter = new CorrectionHighlighter();
        } else {
            this.mCorrectionHighlighter.invalidate(false);
        }
        this.mCorrectionHighlighter.highlight(info);
    }

    /* access modifiers changed from: package-private */
    public void showSuggestions() {
        if (this.mSuggestionsPopupWindow == null) {
            this.mSuggestionsPopupWindow = new SuggestionsPopupWindow();
        }
        hideControllers();
        this.mSuggestionsPopupWindow.show();
    }

    /* access modifiers changed from: package-private */
    public boolean areSuggestionsShown() {
        return this.mSuggestionsPopupWindow != null && this.mSuggestionsPopupWindow.isShowing();
    }

    /* access modifiers changed from: package-private */
    public void onScrollChanged() {
        if (this.mPositionListener != null) {
            this.mPositionListener.onScrollChanged();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldBlink() {
        int start;
        int end;
        if (!isCursorVisible() || !this.mTextView.isFocused() || (start = this.mTextView.getSelectionStart()) < 0 || (end = this.mTextView.getSelectionEnd()) < 0 || start != end) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void makeBlink() {
        if (shouldBlink()) {
            this.mShowCursor = SystemClock.uptimeMillis();
            if (this.mBlink == null) {
                this.mBlink = new Blink();
            }
            this.mBlink.removeCallbacks(this.mBlink);
            this.mBlink.postAtTime(this.mBlink, this.mShowCursor + 500);
        } else if (this.mBlink != null) {
            this.mBlink.removeCallbacks(this.mBlink);
        }
    }

    /* access modifiers changed from: private */
    public class Blink extends Handler implements Runnable {
        private boolean mCancelled;

        private Blink() {
        }

        public void run() {
            if (!this.mCancelled) {
                removeCallbacks(this);
                if (Editor.this.shouldBlink()) {
                    if (Editor.this.mTextView.getLayout() != null) {
                        Editor.this.mTextView.invalidateCursorPath();
                    }
                    postAtTime(this, SystemClock.uptimeMillis() + 500);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void cancel() {
            if (!this.mCancelled) {
                removeCallbacks(this);
                this.mCancelled = true;
            }
        }

        /* access modifiers changed from: package-private */
        public void uncancel() {
            this.mCancelled = false;
        }
    }

    private View.DragShadowBuilder getTextThumbnailBuilder(CharSequence text) {
        TextView shadowView = (TextView) View.inflate(this.mTextView.getContext(), R.layout.text_drag_thumbnail, null);
        if (shadowView == null) {
            throw new IllegalArgumentException("Unable to inflate text drag thumbnail");
        }
        if (text.length() > DRAG_SHADOW_MAX_TEXT_LENGTH) {
            text = text.subSequence(0, DRAG_SHADOW_MAX_TEXT_LENGTH);
        }
        shadowView.setText(text);
        shadowView.setTextColor(this.mTextView.getTextColors());
        shadowView.setTextAppearance(this.mTextView.getContext(), 16);
        shadowView.setGravity(17);
        shadowView.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        int size = View.MeasureSpec.makeMeasureSpec(0, 0);
        shadowView.measure(size, size);
        shadowView.layout(0, 0, shadowView.getMeasuredWidth(), shadowView.getMeasuredHeight());
        shadowView.invalidate();
        return new View.DragShadowBuilder(shadowView);
    }

    /* access modifiers changed from: private */
    public static class DragLocalState {
        public int end;
        public TextView sourceTextView;
        public int start;

        public DragLocalState(TextView sourceTextView2, int start2, int end2) {
            this.sourceTextView = sourceTextView2;
            this.start = start2;
            this.end = end2;
        }
    }

    /* access modifiers changed from: package-private */
    public void onDrop(DragEvent event) {
        StringBuilder content = new StringBuilder(ProxyInfo.LOCAL_EXCL_LIST);
        ClipData clipData = event.getClipData();
        int itemCount = clipData.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            content.append(clipData.getItemAt(i).coerceToStyledText(this.mTextView.getContext()));
        }
        int offset = this.mTextView.getOffsetForPosition(event.getX(), event.getY());
        Object localState = event.getLocalState();
        DragLocalState dragLocalState = null;
        if (localState instanceof DragLocalState) {
            dragLocalState = (DragLocalState) localState;
        }
        boolean dragDropIntoItself = dragLocalState != null && dragLocalState.sourceTextView == this.mTextView;
        if (!dragDropIntoItself || offset < dragLocalState.start || offset >= dragLocalState.end) {
            int originalLength = this.mTextView.getText().length();
            Selection.setSelection((Spannable) this.mTextView.getText(), offset);
            this.mTextView.replaceText_internal(offset, offset, content);
            if (dragDropIntoItself) {
                int dragSourceStart = dragLocalState.start;
                int dragSourceEnd = dragLocalState.end;
                if (offset <= dragSourceStart) {
                    int shift = this.mTextView.getText().length() - originalLength;
                    dragSourceStart += shift;
                    dragSourceEnd += shift;
                }
                this.mTextView.deleteText_internal(dragSourceStart, dragSourceEnd);
                int prevCharIdx = Math.max(0, dragSourceStart - 1);
                int nextCharIdx = Math.min(this.mTextView.getText().length(), dragSourceStart + 1);
                if (nextCharIdx > prevCharIdx + 1) {
                    CharSequence t = this.mTextView.getTransformedText(prevCharIdx, nextCharIdx);
                    if (Character.isSpaceChar(t.charAt(0)) && Character.isSpaceChar(t.charAt(1))) {
                        this.mTextView.deleteText_internal(prevCharIdx, prevCharIdx + 1);
                    }
                }
            }
        }
    }

    public void addSpanWatchers(Spannable text) {
        int textLength = text.length();
        if (this.mKeyListener != null) {
            text.setSpan(this.mKeyListener, 0, textLength, 18);
        }
        if (this.mSpanController == null) {
            this.mSpanController = new SpanController();
        }
        text.setSpan(this.mSpanController, 0, textLength, 18);
    }

    /* access modifiers changed from: package-private */
    public class SpanController implements SpanWatcher {
        private static final int DISPLAY_TIMEOUT_MS = 3000;
        private Runnable mHidePopup;
        private EasyEditPopupWindow mPopupWindow;

        SpanController() {
        }

        private boolean isNonIntermediateSelectionSpan(Spannable text, Object span) {
            return (Selection.SELECTION_START == span || Selection.SELECTION_END == span) && (text.getSpanFlags(span) & 512) == 0;
        }

        @Override // android.text.SpanWatcher
        public void onSpanAdded(Spannable text, Object span, int start, int end) {
            if (isNonIntermediateSelectionSpan(text, span)) {
                Editor.this.sendUpdateSelection();
            } else if (span instanceof EasyEditSpan) {
                if (this.mPopupWindow == null) {
                    this.mPopupWindow = new EasyEditPopupWindow();
                    this.mHidePopup = new Runnable() {
                        /* class android.widget.Editor.SpanController.AnonymousClass1 */

                        public void run() {
                            SpanController.this.hide();
                        }
                    };
                }
                if (this.mPopupWindow.mEasyEditSpan != null) {
                    this.mPopupWindow.mEasyEditSpan.setDeleteEnabled(false);
                }
                this.mPopupWindow.setEasyEditSpan((EasyEditSpan) span);
                this.mPopupWindow.setOnDeleteListener(new EasyEditDeleteListener() {
                    /* class android.widget.Editor.SpanController.AnonymousClass2 */

                    @Override // android.widget.Editor.EasyEditDeleteListener
                    public void onDeleteClick(EasyEditSpan span) {
                        Editable editable = (Editable) Editor.this.mTextView.getText();
                        int start = editable.getSpanStart(span);
                        int end = editable.getSpanEnd(span);
                        if (start >= 0 && end >= 0) {
                            SpanController.this.sendEasySpanNotification(1, span);
                            Editor.this.mTextView.deleteText_internal(start, end);
                        }
                        editable.removeSpan(span);
                    }
                });
                if (Editor.this.mTextView.getWindowVisibility() == 0 && Editor.this.mTextView.getLayout() != null && !Editor.this.extractedTextModeWillBeStarted()) {
                    this.mPopupWindow.show();
                    Editor.this.mTextView.removeCallbacks(this.mHidePopup);
                    Editor.this.mTextView.postDelayed(this.mHidePopup, 3000);
                }
            }
        }

        @Override // android.text.SpanWatcher
        public void onSpanRemoved(Spannable text, Object span, int start, int end) {
            if (isNonIntermediateSelectionSpan(text, span)) {
                Editor.this.sendUpdateSelection();
            } else if (this.mPopupWindow != null && span == this.mPopupWindow.mEasyEditSpan) {
                hide();
            }
        }

        @Override // android.text.SpanWatcher
        public void onSpanChanged(Spannable text, Object span, int previousStart, int previousEnd, int newStart, int newEnd) {
            if (isNonIntermediateSelectionSpan(text, span)) {
                Editor.this.sendUpdateSelection();
            } else if (this.mPopupWindow != null && (span instanceof EasyEditSpan)) {
                EasyEditSpan easyEditSpan = (EasyEditSpan) span;
                sendEasySpanNotification(2, easyEditSpan);
                text.removeSpan(easyEditSpan);
            }
        }

        public void hide() {
            if (this.mPopupWindow != null) {
                this.mPopupWindow.hide();
                Editor.this.mTextView.removeCallbacks(this.mHidePopup);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void sendEasySpanNotification(int textChangedType, EasyEditSpan span) {
            try {
                PendingIntent pendingIntent = span.getPendingIntent();
                if (pendingIntent != null) {
                    Intent intent = new Intent();
                    intent.putExtra(EasyEditSpan.EXTRA_TEXT_CHANGED_TYPE, textChangedType);
                    pendingIntent.send(Editor.this.mTextView.getContext(), 0, intent);
                }
            } catch (PendingIntent.CanceledException e) {
                Log.w(Editor.TAG, "PendingIntent for notification cannot be sent", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public class EasyEditPopupWindow extends PinnedPopupWindow implements View.OnClickListener {
        private static final int POPUP_TEXT_LAYOUT = 17367254;
        private TextView mDeleteTextView;
        private EasyEditSpan mEasyEditSpan;
        private EasyEditDeleteListener mOnDeleteListener;

        private EasyEditPopupWindow() {
            super();
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public void createPopupWindow() {
            this.mPopupWindow = new PopupWindow(Editor.this.mTextView.getContext(), (AttributeSet) null, 16843464);
            this.mPopupWindow.setInputMethodMode(2);
            this.mPopupWindow.setClippingEnabled(true);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public void initContentView() {
            LinearLayout linearLayout = new LinearLayout(Editor.this.mTextView.getContext());
            linearLayout.setOrientation(0);
            this.mContentView = linearLayout;
            this.mContentView.setBackgroundResource(R.drawable.text_edit_side_paste_window);
            ViewGroup.LayoutParams wrapContent = new ViewGroup.LayoutParams(-2, -2);
            this.mDeleteTextView = (TextView) ((LayoutInflater) Editor.this.mTextView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(17367254, (ViewGroup) null);
            this.mDeleteTextView.setLayoutParams(wrapContent);
            this.mDeleteTextView.setText(R.string.delete);
            this.mDeleteTextView.setOnClickListener(this);
            this.mContentView.addView(this.mDeleteTextView);
        }

        public void setEasyEditSpan(EasyEditSpan easyEditSpan) {
            this.mEasyEditSpan = easyEditSpan;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setOnDeleteListener(EasyEditDeleteListener listener) {
            this.mOnDeleteListener = listener;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (view == this.mDeleteTextView && this.mEasyEditSpan != null && this.mEasyEditSpan.isDeleteEnabled() && this.mOnDeleteListener != null) {
                this.mOnDeleteListener.onDeleteClick(this.mEasyEditSpan);
            }
        }

        @Override // android.widget.Editor.PinnedPopupWindow
        public void hide() {
            if (this.mEasyEditSpan != null) {
                this.mEasyEditSpan.setDeleteEnabled(false);
            }
            this.mOnDeleteListener = null;
            super.hide();
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public int getTextOffset() {
            return ((Editable) Editor.this.mTextView.getText()).getSpanEnd(this.mEasyEditSpan);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public int getVerticalLocalPosition(int line) {
            return Editor.this.mTextView.getLayout().getLineBottom(line);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public int clipVertically(int positionY) {
            return positionY;
        }
    }

    /* access modifiers changed from: private */
    public class PositionListener implements ViewTreeObserver.OnPreDrawListener {
        private final int MAXIMUM_NUMBER_OF_LISTENERS;
        private boolean[] mCanMove;
        private int mNumberOfListeners;
        private boolean mPositionHasChanged;
        private TextViewPositionListener[] mPositionListeners;
        private int mPositionX;
        private int mPositionY;
        private boolean mScrollHasChanged;
        final int[] mTempCoords;

        private PositionListener() {
            this.MAXIMUM_NUMBER_OF_LISTENERS = 7;
            this.mPositionListeners = new TextViewPositionListener[7];
            this.mCanMove = new boolean[7];
            this.mPositionHasChanged = true;
            this.mTempCoords = new int[2];
        }

        public void addSubscriber(TextViewPositionListener positionListener, boolean canMove) {
            if (this.mNumberOfListeners == 0) {
                updatePosition();
                Editor.this.mTextView.getViewTreeObserver().addOnPreDrawListener(this);
            }
            int emptySlotIndex = -1;
            for (int i = 0; i < 7; i++) {
                TextViewPositionListener listener = this.mPositionListeners[i];
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

        public void removeSubscriber(TextViewPositionListener positionListener) {
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
                Editor.this.mTextView.getViewTreeObserver().removeOnPreDrawListener(this);
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
            TextViewPositionListener positionListener;
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
            Editor.this.mTextView.getLocationInWindow(this.mTempCoords);
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

    /* access modifiers changed from: private */
    public abstract class PinnedPopupWindow implements TextViewPositionListener {
        protected ViewGroup mContentView;
        protected PopupWindow mPopupWindow;
        int mPositionX;
        int mPositionY;

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

        public PinnedPopupWindow() {
            createPopupWindow();
            this.mPopupWindow.setWindowLayoutType(1002);
            this.mPopupWindow.setWidth(-2);
            this.mPopupWindow.setHeight(-2);
            initContentView();
            this.mContentView.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
            this.mPopupWindow.setContentView(this.mContentView);
        }

        public void show() {
            Editor.this.getPositionListener().addSubscriber(this, false);
            computeLocalPosition();
            PositionListener positionListener = Editor.this.getPositionListener();
            updatePosition(positionListener.getPositionX(), positionListener.getPositionY());
        }

        /* access modifiers changed from: protected */
        public void measureContent() {
            DisplayMetrics displayMetrics = Editor.this.mTextView.getResources().getDisplayMetrics();
            this.mContentView.measure(View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, Integer.MIN_VALUE));
        }

        private void computeLocalPosition() {
            measureContent();
            int width = this.mContentView.getMeasuredWidth();
            int offset = getTextOffset();
            this.mPositionX = (int) (Editor.this.mTextView.getLayout().getPrimaryHorizontal(offset) - (((float) width) / 2.0f));
            this.mPositionX += Editor.this.mTextView.viewportToContentHorizontalOffset();
            this.mPositionY = getVerticalLocalPosition(Editor.this.mTextView.getLayout().getLineForOffset(offset));
            this.mPositionY += Editor.this.mTextView.viewportToContentVerticalOffset();
        }

        private void updatePosition(int parentPositionX, int parentPositionY) {
            int positionX = parentPositionX + this.mPositionX;
            int positionY = clipVertically(parentPositionY + this.mPositionY);
            DisplayMetrics displayMetrics = Editor.this.mTextView.getResources().getDisplayMetrics();
            int positionX2 = Math.max(0, Math.min(displayMetrics.widthPixels - this.mContentView.getMeasuredWidth(), positionX));
            if (isShowing()) {
                this.mPopupWindow.update(positionX2, positionY, -1, -1);
            } else {
                this.mPopupWindow.showAtLocation(Editor.this.mTextView, 0, positionX2, positionY);
            }
        }

        public void hide() {
            this.mPopupWindow.dismiss();
            Editor.this.getPositionListener().removeSubscriber(this);
        }

        @Override // android.widget.Editor.TextViewPositionListener
        public void updatePosition(int parentPositionX, int parentPositionY, boolean parentPositionChanged, boolean parentScrolled) {
            if (!isShowing() || !Editor.this.isOffsetVisible(getTextOffset())) {
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

    /* access modifiers changed from: private */
    public class SuggestionsPopupWindow extends PinnedPopupWindow implements AdapterView.OnItemClickListener {
        private static final int ADD_TO_DICTIONARY = -1;
        private static final int DELETE_TEXT = -2;
        private static final int MAX_NUMBER_SUGGESTIONS = 5;
        private boolean mCursorWasVisibleBeforeSuggestions;
        private boolean mIsShowingUp = false;
        private int mNumberOfSuggestions;
        private final HashMap<SuggestionSpan, Integer> mSpansLengths;
        private SuggestionInfo[] mSuggestionInfos;
        private final Comparator<SuggestionSpan> mSuggestionSpanComparator;
        private SuggestionAdapter mSuggestionsAdapter;

        private class CustomPopupWindow extends PopupWindow {
            public CustomPopupWindow(Context context, int defStyleAttr) {
                super(context, (AttributeSet) null, defStyleAttr);
            }

            @Override // android.widget.PopupWindow
            public void dismiss() {
                super.dismiss();
                Editor.this.getPositionListener().removeSubscriber(SuggestionsPopupWindow.this);
                ((Spannable) Editor.this.mTextView.getText()).removeSpan(Editor.this.mSuggestionRangeSpan);
                Editor.this.mTextView.setCursorVisible(SuggestionsPopupWindow.this.mCursorWasVisibleBeforeSuggestions);
                if (Editor.this.hasInsertionController()) {
                    Editor.this.getInsertionController().show();
                }
            }
        }

        public SuggestionsPopupWindow() {
            super();
            this.mCursorWasVisibleBeforeSuggestions = Editor.this.mCursorVisible;
            this.mSuggestionSpanComparator = new SuggestionSpanComparator();
            this.mSpansLengths = new HashMap<>();
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public void createPopupWindow() {
            this.mPopupWindow = new CustomPopupWindow(Editor.this.mTextView.getContext(), 16843635);
            this.mPopupWindow.setInputMethodMode(2);
            this.mPopupWindow.setFocusable(true);
            this.mPopupWindow.setClippingEnabled(false);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public void initContentView() {
            ListView listView = new ListView(Editor.this.mTextView.getContext());
            this.mSuggestionsAdapter = new SuggestionAdapter();
            listView.setAdapter((ListAdapter) this.mSuggestionsAdapter);
            listView.setOnItemClickListener(this);
            this.mContentView = listView;
            this.mSuggestionInfos = new SuggestionInfo[7];
            for (int i = 0; i < this.mSuggestionInfos.length; i++) {
                this.mSuggestionInfos[i] = new SuggestionInfo();
            }
        }

        public boolean isShowingUp() {
            return this.mIsShowingUp;
        }

        public void onParentLostFocus() {
            this.mIsShowingUp = false;
        }

        /* access modifiers changed from: private */
        public class SuggestionInfo {
            TextAppearanceSpan highlightSpan;
            int suggestionEnd;
            int suggestionIndex;
            SuggestionSpan suggestionSpan;
            int suggestionStart;
            SpannableStringBuilder text;

            private SuggestionInfo() {
                this.text = new SpannableStringBuilder();
                this.highlightSpan = new TextAppearanceSpan(Editor.this.mTextView.getContext(), 16974104);
            }
        }

        /* access modifiers changed from: private */
        public class SuggestionAdapter extends BaseAdapter {
            private LayoutInflater mInflater;

            private SuggestionAdapter() {
                this.mInflater = (LayoutInflater) Editor.this.mTextView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            @Override // android.widget.Adapter
            public int getCount() {
                return SuggestionsPopupWindow.this.mNumberOfSuggestions;
            }

            @Override // android.widget.Adapter
            public Object getItem(int position) {
                return SuggestionsPopupWindow.this.mSuggestionInfos[position];
            }

            @Override // android.widget.Adapter
            public long getItemId(int position) {
                return (long) position;
            }

            @Override // android.widget.Adapter
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) convertView;
                if (textView == null) {
                    textView = (TextView) this.mInflater.inflate(Editor.this.mTextView.mTextEditSuggestionItemLayout, parent, false);
                }
                SuggestionInfo suggestionInfo = SuggestionsPopupWindow.this.mSuggestionInfos[position];
                textView.setText(suggestionInfo.text);
                if (suggestionInfo.suggestionIndex == -1 || suggestionInfo.suggestionIndex == -2) {
                    textView.setBackgroundColor(0);
                } else {
                    textView.setBackgroundColor(-1);
                }
                return textView;
            }
        }

        private class SuggestionSpanComparator implements Comparator<SuggestionSpan> {
            private SuggestionSpanComparator() {
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
                return ((Integer) SuggestionsPopupWindow.this.mSpansLengths.get(span1)).intValue() - ((Integer) SuggestionsPopupWindow.this.mSpansLengths.get(span2)).intValue();
            }
        }

        private SuggestionSpan[] getSuggestionSpans() {
            int pos = Editor.this.mTextView.getSelectionStart();
            Spannable spannable = (Spannable) Editor.this.mTextView.getText();
            SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) spannable.getSpans(pos, pos, SuggestionSpan.class);
            this.mSpansLengths.clear();
            for (SuggestionSpan suggestionSpan : suggestionSpans) {
                this.mSpansLengths.put(suggestionSpan, Integer.valueOf(spannable.getSpanEnd(suggestionSpan) - spannable.getSpanStart(suggestionSpan)));
            }
            Arrays.sort(suggestionSpans, this.mSuggestionSpanComparator);
            return suggestionSpans;
        }

        @Override // android.widget.Editor.PinnedPopupWindow
        public void show() {
            if ((Editor.this.mTextView.getText() instanceof Editable) && updateSuggestions()) {
                this.mCursorWasVisibleBeforeSuggestions = Editor.this.mCursorVisible;
                Editor.this.mTextView.setCursorVisible(false);
                this.mIsShowingUp = true;
                super.show();
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public void measureContent() {
            DisplayMetrics displayMetrics = Editor.this.mTextView.getResources().getDisplayMetrics();
            int horizontalMeasure = View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, Integer.MIN_VALUE);
            int verticalMeasure = View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, Integer.MIN_VALUE);
            int width = 0;
            View view = null;
            for (int i = 0; i < this.mNumberOfSuggestions; i++) {
                view = this.mSuggestionsAdapter.getView(i, view, this.mContentView);
                view.getLayoutParams().width = -2;
                view.measure(horizontalMeasure, verticalMeasure);
                width = Math.max(width, view.getMeasuredWidth());
            }
            this.mContentView.measure(View.MeasureSpec.makeMeasureSpec(width, 1073741824), verticalMeasure);
            Drawable popupBackground = this.mPopupWindow.getBackground();
            if (popupBackground != null) {
                if (Editor.this.mTempRect == null) {
                    Editor.this.mTempRect = new Rect();
                }
                popupBackground.getPadding(Editor.this.mTempRect);
                width += Editor.this.mTempRect.left + Editor.this.mTempRect.right;
            }
            this.mPopupWindow.setWidth(width);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public int getTextOffset() {
            return Editor.this.mTextView.getSelectionStart();
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public int getVerticalLocalPosition(int line) {
            return Editor.this.mTextView.getLayout().getLineBottom(line);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public int clipVertically(int positionY) {
            return Math.min(positionY, Editor.this.mTextView.getResources().getDisplayMetrics().heightPixels - this.mContentView.getMeasuredHeight());
        }

        @Override // android.widget.Editor.PinnedPopupWindow
        public void hide() {
            super.hide();
        }

        private boolean updateSuggestions() {
            Spannable spannable = (Spannable) Editor.this.mTextView.getText();
            SuggestionSpan[] suggestionSpans = getSuggestionSpans();
            int nbSpans = suggestionSpans.length;
            if (nbSpans == 0) {
                return false;
            }
            this.mNumberOfSuggestions = 0;
            int spanUnionStart = Editor.this.mTextView.getText().length();
            int spanUnionEnd = 0;
            SuggestionSpan misspelledSpan = null;
            int underlineColor = 0;
            int spanIndex = 0;
            while (spanIndex < nbSpans) {
                SuggestionSpan suggestionSpan = suggestionSpans[spanIndex];
                int spanStart = spannable.getSpanStart(suggestionSpan);
                int spanEnd = spannable.getSpanEnd(suggestionSpan);
                spanUnionStart = Math.min(spanStart, spanUnionStart);
                spanUnionEnd = Math.max(spanEnd, spanUnionEnd);
                if ((suggestionSpan.getFlags() & 2) != 0) {
                    misspelledSpan = suggestionSpan;
                }
                if (spanIndex == 0) {
                    underlineColor = suggestionSpan.getUnderlineColor();
                }
                String[] suggestions = suggestionSpan.getSuggestions();
                int nbSuggestions = suggestions.length;
                int suggestionIndex = 0;
                while (true) {
                    if (suggestionIndex >= nbSuggestions) {
                        break;
                    }
                    String suggestion = suggestions[suggestionIndex];
                    boolean suggestionIsDuplicate = false;
                    int i = 0;
                    while (true) {
                        if (i >= this.mNumberOfSuggestions) {
                            break;
                        }
                        if (this.mSuggestionInfos[i].text.toString().equals(suggestion)) {
                            SuggestionSpan otherSuggestionSpan = this.mSuggestionInfos[i].suggestionSpan;
                            int otherSpanStart = spannable.getSpanStart(otherSuggestionSpan);
                            int otherSpanEnd = spannable.getSpanEnd(otherSuggestionSpan);
                            if (spanStart == otherSpanStart && spanEnd == otherSpanEnd) {
                                suggestionIsDuplicate = true;
                                break;
                            }
                        }
                        i++;
                    }
                    if (!suggestionIsDuplicate) {
                        SuggestionInfo suggestionInfo = this.mSuggestionInfos[this.mNumberOfSuggestions];
                        suggestionInfo.suggestionSpan = suggestionSpan;
                        suggestionInfo.suggestionIndex = suggestionIndex;
                        suggestionInfo.text.replace(0, suggestionInfo.text.length(), (CharSequence) suggestion);
                        this.mNumberOfSuggestions++;
                        if (this.mNumberOfSuggestions == 5) {
                            spanIndex = nbSpans;
                            break;
                        }
                    }
                    suggestionIndex++;
                }
                spanIndex++;
            }
            for (int i2 = 0; i2 < this.mNumberOfSuggestions; i2++) {
                highlightTextDifferences(this.mSuggestionInfos[i2], spanUnionStart, spanUnionEnd);
            }
            if (misspelledSpan != null) {
                int misspelledStart = spannable.getSpanStart(misspelledSpan);
                int misspelledEnd = spannable.getSpanEnd(misspelledSpan);
                if (misspelledStart >= 0 && misspelledEnd > misspelledStart) {
                    SuggestionInfo suggestionInfo2 = this.mSuggestionInfos[this.mNumberOfSuggestions];
                    suggestionInfo2.suggestionSpan = misspelledSpan;
                    suggestionInfo2.suggestionIndex = -1;
                    suggestionInfo2.text.replace(0, suggestionInfo2.text.length(), (CharSequence) Editor.this.mTextView.getContext().getString(R.string.addToDictionary));
                    suggestionInfo2.text.setSpan(suggestionInfo2.highlightSpan, 0, 0, 33);
                    this.mNumberOfSuggestions++;
                }
            }
            SuggestionInfo suggestionInfo3 = this.mSuggestionInfos[this.mNumberOfSuggestions];
            suggestionInfo3.suggestionSpan = null;
            suggestionInfo3.suggestionIndex = -2;
            suggestionInfo3.text.replace(0, suggestionInfo3.text.length(), (CharSequence) Editor.this.mTextView.getContext().getString(R.string.deleteText));
            suggestionInfo3.text.setSpan(suggestionInfo3.highlightSpan, 0, 0, 33);
            this.mNumberOfSuggestions++;
            if (Editor.this.mSuggestionRangeSpan == null) {
                Editor.this.mSuggestionRangeSpan = new SuggestionRangeSpan();
            }
            if (underlineColor == 0) {
                Editor.this.mSuggestionRangeSpan.setBackgroundColor(Editor.this.mTextView.mHighlightColor);
            } else {
                Editor.this.mSuggestionRangeSpan.setBackgroundColor((16777215 & underlineColor) + (((int) (((float) Color.alpha(underlineColor)) * 0.4f)) << 24));
            }
            spannable.setSpan(Editor.this.mSuggestionRangeSpan, spanUnionStart, spanUnionEnd, 33);
            this.mSuggestionsAdapter.notifyDataSetChanged();
            return true;
        }

        private void highlightTextDifferences(SuggestionInfo suggestionInfo, int unionStart, int unionEnd) {
            Spannable text = (Spannable) Editor.this.mTextView.getText();
            int spanStart = text.getSpanStart(suggestionInfo.suggestionSpan);
            int spanEnd = text.getSpanEnd(suggestionInfo.suggestionSpan);
            suggestionInfo.suggestionStart = spanStart - unionStart;
            suggestionInfo.suggestionEnd = suggestionInfo.suggestionStart + suggestionInfo.text.length();
            suggestionInfo.text.setSpan(suggestionInfo.highlightSpan, 0, suggestionInfo.text.length(), 33);
            String textAsString = text.toString();
            suggestionInfo.text.insert(0, (CharSequence) textAsString.substring(unionStart, spanStart));
            suggestionInfo.text.append((CharSequence) textAsString.substring(spanEnd, unionEnd));
        }

        @Override // android.widget.AdapterView.OnItemClickListener
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Editable editable = (Editable) Editor.this.mTextView.getText();
            SuggestionInfo suggestionInfo = this.mSuggestionInfos[position];
            if (suggestionInfo.suggestionIndex == -2) {
                int spanUnionStart = editable.getSpanStart(Editor.this.mSuggestionRangeSpan);
                int spanUnionEnd = editable.getSpanEnd(Editor.this.mSuggestionRangeSpan);
                if (spanUnionStart >= 0 && spanUnionEnd > spanUnionStart) {
                    if (spanUnionEnd < editable.length() && Character.isSpaceChar(editable.charAt(spanUnionEnd)) && (spanUnionStart == 0 || Character.isSpaceChar(editable.charAt(spanUnionStart - 1)))) {
                        spanUnionEnd++;
                    }
                    Editor.this.mTextView.deleteText_internal(spanUnionStart, spanUnionEnd);
                }
                hide();
                return;
            }
            int spanStart = editable.getSpanStart(suggestionInfo.suggestionSpan);
            int spanEnd = editable.getSpanEnd(suggestionInfo.suggestionSpan);
            if (spanStart < 0 || spanEnd <= spanStart) {
                hide();
                return;
            }
            String originalText = editable.toString().substring(spanStart, spanEnd);
            if (suggestionInfo.suggestionIndex == -1) {
                Intent intent = new Intent(Settings.ACTION_USER_DICTIONARY_INSERT);
                intent.putExtra(UserDictionary.Words.WORD, originalText);
                intent.putExtra(UserDictionary.Words.LOCALE, Editor.this.mTextView.getTextServicesLocale().toString());
                intent.setFlags(intent.getFlags() | 268435456);
                Editor.this.mTextView.getContext().startActivity(intent);
                editable.removeSpan(suggestionInfo.suggestionSpan);
                Selection.setSelection(editable, spanEnd);
                Editor.this.updateSpellCheckSpans(spanStart, spanEnd, false);
            } else {
                SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) editable.getSpans(spanStart, spanEnd, SuggestionSpan.class);
                int length = suggestionSpans.length;
                int[] suggestionSpansStarts = new int[length];
                int[] suggestionSpansEnds = new int[length];
                int[] suggestionSpansFlags = new int[length];
                for (int i = 0; i < length; i++) {
                    SuggestionSpan suggestionSpan = suggestionSpans[i];
                    suggestionSpansStarts[i] = editable.getSpanStart(suggestionSpan);
                    suggestionSpansEnds[i] = editable.getSpanEnd(suggestionSpan);
                    suggestionSpansFlags[i] = editable.getSpanFlags(suggestionSpan);
                    int suggestionSpanFlags = suggestionSpan.getFlags();
                    if ((suggestionSpanFlags & 2) > 0) {
                        suggestionSpan.setFlags(suggestionSpanFlags & -3 & -2);
                    }
                }
                String suggestion = suggestionInfo.text.subSequence(suggestionInfo.suggestionStart, suggestionInfo.suggestionEnd).toString();
                Editor.this.mTextView.replaceText_internal(spanStart, spanEnd, suggestion);
                suggestionInfo.suggestionSpan.notifySelection(Editor.this.mTextView.getContext(), originalText, suggestionInfo.suggestionIndex);
                suggestionInfo.suggestionSpan.getSuggestions()[suggestionInfo.suggestionIndex] = originalText;
                int lengthDifference = suggestion.length() - (spanEnd - spanStart);
                for (int i2 = 0; i2 < length; i2++) {
                    if (suggestionSpansStarts[i2] <= spanStart && suggestionSpansEnds[i2] >= spanEnd) {
                        Editor.this.mTextView.setSpan_internal(suggestionSpans[i2], suggestionSpansStarts[i2], suggestionSpansEnds[i2] + lengthDifference, suggestionSpansFlags[i2]);
                    }
                }
                int newCursorPosition = spanEnd + lengthDifference;
                Editor.this.mTextView.setCursorPosition_internal(newCursorPosition, newCursorPosition);
            }
            hide();
        }
    }

    /* access modifiers changed from: private */
    public class SelectionActionModeCallback implements ActionMode.Callback {
        private SelectionActionModeCallback() {
        }

        @Override // android.view.ActionMode.Callback
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            boolean legacy;
            Context context;
            if (Editor.this.mTextView.getContext().getApplicationInfo().targetSdkVersion < 21) {
                legacy = true;
            } else {
                legacy = false;
            }
            if (legacy || !(menu instanceof MenuBuilder)) {
                context = Editor.this.mTextView.getContext();
            } else {
                context = ((MenuBuilder) menu).getContext();
            }
            TypedArray styledAttributes = context.obtainStyledAttributes(R.styleable.SelectionModeDrawables);
            mode.setTitle(Editor.this.mTextView.getContext().getString(R.string.textSelectionCABTitle));
            mode.setSubtitle((CharSequence) null);
            mode.setTitleOptionalHint(true);
            menu.add(0, 16908319, 0, 17039373).setIcon(styledAttributes.getResourceId(3, 0)).setAlphabeticShortcut(DateFormat.AM_PM).setShowAsAction(6);
            if (Editor.this.mTextView.canCut()) {
                menu.add(0, 16908320, 0, 17039363).setIcon(styledAttributes.getResourceId(0, 0)).setAlphabeticShortcut('x').setShowAsAction(6);
            }
            if (Editor.this.mTextView.canCopy()) {
                menu.add(0, 16908321, 0, 17039361).setIcon(styledAttributes.getResourceId(1, 0)).setAlphabeticShortcut('c').setShowAsAction(6);
            }
            if (Editor.this.mTextView.canPaste()) {
                menu.add(0, 16908322, 0, 17039371).setIcon(styledAttributes.getResourceId(2, 0)).setAlphabeticShortcut('v').setShowAsAction(6);
            }
            styledAttributes.recycle();
            if (Editor.this.mCustomSelectionActionModeCallback != null && !Editor.this.mCustomSelectionActionModeCallback.onCreateActionMode(mode, menu)) {
                return false;
            }
            if (!menu.hasVisibleItems() && mode.getCustomView() == null) {
                return false;
            }
            Editor.this.getSelectionController().show();
            Editor.this.mTextView.setHasTransientState(true);
            return true;
        }

        @Override // android.view.ActionMode.Callback
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (Editor.this.mCustomSelectionActionModeCallback != null) {
                return Editor.this.mCustomSelectionActionModeCallback.onPrepareActionMode(mode, menu);
            }
            return true;
        }

        @Override // android.view.ActionMode.Callback
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (Editor.this.mCustomSelectionActionModeCallback == null || !Editor.this.mCustomSelectionActionModeCallback.onActionItemClicked(mode, item)) {
                return Editor.this.mTextView.onTextContextMenuItem(item.getItemId());
            }
            return true;
        }

        @Override // android.view.ActionMode.Callback
        public void onDestroyActionMode(ActionMode mode) {
            if (Editor.this.mCustomSelectionActionModeCallback != null) {
                Editor.this.mCustomSelectionActionModeCallback.onDestroyActionMode(mode);
            }
            if (!Editor.this.mPreserveDetachedSelection) {
                Selection.setSelection((Spannable) Editor.this.mTextView.getText(), Editor.this.mTextView.getSelectionEnd());
                Editor.this.mTextView.setHasTransientState(false);
            }
            if (Editor.this.mSelectionModifierCursorController != null) {
                Editor.this.mSelectionModifierCursorController.hide();
            }
            Editor.this.mSelectionActionMode = null;
        }
    }

    /* access modifiers changed from: private */
    public class ActionPopupWindow extends PinnedPopupWindow implements View.OnClickListener {
        private static final int POPUP_TEXT_LAYOUT = 17367254;
        private TextView mPasteTextView;
        private TextView mReplaceTextView;

        private ActionPopupWindow() {
            super();
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public void createPopupWindow() {
            this.mPopupWindow = new PopupWindow(Editor.this.mTextView.getContext(), (AttributeSet) null, 16843464);
            this.mPopupWindow.setClippingEnabled(true);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public void initContentView() {
            LinearLayout linearLayout = new LinearLayout(Editor.this.mTextView.getContext());
            linearLayout.setOrientation(0);
            this.mContentView = linearLayout;
            this.mContentView.setBackgroundResource(R.drawable.text_edit_paste_window);
            LayoutInflater inflater = (LayoutInflater) Editor.this.mTextView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup.LayoutParams wrapContent = new ViewGroup.LayoutParams(-2, -2);
            this.mPasteTextView = (TextView) inflater.inflate(17367254, (ViewGroup) null);
            this.mPasteTextView.setLayoutParams(wrapContent);
            this.mContentView.addView(this.mPasteTextView);
            this.mPasteTextView.setText(17039371);
            this.mPasteTextView.setOnClickListener(this);
            this.mReplaceTextView = (TextView) inflater.inflate(17367254, (ViewGroup) null);
            this.mReplaceTextView.setLayoutParams(wrapContent);
            this.mContentView.addView(this.mReplaceTextView);
            this.mReplaceTextView.setText(R.string.replace);
            this.mReplaceTextView.setOnClickListener(this);
        }

        @Override // android.widget.Editor.PinnedPopupWindow
        public void show() {
            boolean canSuggest;
            int i;
            int i2 = 0;
            boolean canPaste = Editor.this.mTextView.canPaste();
            if (!Editor.this.mTextView.isSuggestionsEnabled() || !Editor.this.isCursorInsideSuggestionSpan()) {
                canSuggest = false;
            } else {
                canSuggest = true;
            }
            TextView textView = this.mPasteTextView;
            if (canPaste) {
                i = 0;
            } else {
                i = 8;
            }
            textView.setVisibility(i);
            TextView textView2 = this.mReplaceTextView;
            if (!canSuggest) {
                i2 = 8;
            }
            textView2.setVisibility(i2);
            if (canPaste || canSuggest) {
                super.show();
            }
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (view == this.mPasteTextView && Editor.this.mTextView.canPaste()) {
                Editor.this.mTextView.onTextContextMenuItem(16908322);
                hide();
            } else if (view == this.mReplaceTextView) {
                int middle = (Editor.this.mTextView.getSelectionStart() + Editor.this.mTextView.getSelectionEnd()) / 2;
                Editor.this.stopSelectionActionMode();
                Selection.setSelection((Spannable) Editor.this.mTextView.getText(), middle);
                Editor.this.showSuggestions();
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public int getTextOffset() {
            return (Editor.this.mTextView.getSelectionStart() + Editor.this.mTextView.getSelectionEnd()) / 2;
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public int getVerticalLocalPosition(int line) {
            return Editor.this.mTextView.getLayout().getLineTop(line) - this.mContentView.getMeasuredHeight();
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.PinnedPopupWindow
        public int clipVertically(int positionY) {
            if (positionY >= 0) {
                return positionY;
            }
            int offset = getTextOffset();
            Layout layout = Editor.this.mTextView.getLayout();
            int line = layout.getLineForOffset(offset);
            return positionY + (layout.getLineBottom(line) - layout.getLineTop(line)) + this.mContentView.getMeasuredHeight() + Editor.this.mTextView.getContext().getDrawable(Editor.this.mTextView.mTextSelectHandleRes).getIntrinsicHeight();
        }
    }

    /* access modifiers changed from: private */
    public final class CursorAnchorInfoNotifier implements TextViewPositionListener {
        final CursorAnchorInfo.Builder mSelectionInfoBuilder;
        final int[] mTmpIntOffset;
        final Matrix mViewToScreenMatrix;

        private CursorAnchorInfoNotifier() {
            this.mSelectionInfoBuilder = new CursorAnchorInfo.Builder();
            this.mTmpIntOffset = new int[2];
            this.mViewToScreenMatrix = new Matrix();
        }

        @Override // android.widget.Editor.TextViewPositionListener
        public void updatePosition(int parentPositionX, int parentPositionY, boolean parentPositionChanged, boolean parentScrolled) {
            InputMethodManager imm;
            Layout layout;
            float left;
            float right;
            InputMethodState ims = Editor.this.mInputMethodState;
            if (ims != null && ims.mBatchEditNesting <= 0 && (imm = InputMethodManager.peekInstance()) != null && imm.isActive(Editor.this.mTextView) && imm.isCursorAnchorInfoEnabled() && (layout = Editor.this.mTextView.getLayout()) != null) {
                CursorAnchorInfo.Builder builder = this.mSelectionInfoBuilder;
                builder.reset();
                int selectionStart = Editor.this.mTextView.getSelectionStart();
                builder.setSelectionRange(selectionStart, Editor.this.mTextView.getSelectionEnd());
                this.mViewToScreenMatrix.set(Editor.this.mTextView.getMatrix());
                Editor.this.mTextView.getLocationOnScreen(this.mTmpIntOffset);
                this.mViewToScreenMatrix.postTranslate((float) this.mTmpIntOffset[0], (float) this.mTmpIntOffset[1]);
                builder.setMatrix(this.mViewToScreenMatrix);
                float viewportToContentHorizontalOffset = (float) Editor.this.mTextView.viewportToContentHorizontalOffset();
                float viewportToContentVerticalOffset = (float) Editor.this.mTextView.viewportToContentVerticalOffset();
                CharSequence text = Editor.this.mTextView.getText();
                if (text instanceof Spannable) {
                    Spannable sp = (Spannable) text;
                    int composingTextStart = EditableInputConnection.getComposingSpanStart(sp);
                    int composingTextEnd = EditableInputConnection.getComposingSpanEnd(sp);
                    if (composingTextEnd < composingTextStart) {
                        composingTextEnd = composingTextStart;
                        composingTextStart = composingTextEnd;
                    }
                    if (composingTextStart >= 0 && composingTextStart < composingTextEnd) {
                        builder.setComposingText(composingTextStart, text.subSequence(composingTextStart, composingTextEnd));
                        int minLine = layout.getLineForOffset(composingTextStart);
                        int maxLine = layout.getLineForOffset(composingTextEnd - 1);
                        for (int line = minLine; line <= maxLine; line++) {
                            int lineStart = layout.getLineStart(line);
                            int lineEnd = layout.getLineEnd(line);
                            int offsetStart = Math.max(lineStart, composingTextStart);
                            int offsetEnd = Math.min(lineEnd, composingTextEnd);
                            boolean ltrLine = layout.getParagraphDirection(line) == 1;
                            float[] widths = new float[(offsetEnd - offsetStart)];
                            layout.getPaint().getTextWidths(text, offsetStart, offsetEnd, widths);
                            float top = (float) layout.getLineTop(line);
                            float bottom = (float) layout.getLineBottom(line);
                            for (int offset = offsetStart; offset < offsetEnd; offset++) {
                                float charWidth = widths[offset - offsetStart];
                                boolean isRtl = layout.isRtlCharAt(offset);
                                float primary = layout.getPrimaryHorizontal(offset);
                                float secondary = layout.getSecondaryHorizontal(offset);
                                if (ltrLine) {
                                    if (isRtl) {
                                        left = secondary - charWidth;
                                        right = secondary;
                                    } else {
                                        left = primary;
                                        right = primary + charWidth;
                                    }
                                } else if (!isRtl) {
                                    left = secondary;
                                    right = secondary + charWidth;
                                } else {
                                    left = primary - charWidth;
                                    right = primary;
                                }
                                float localLeft = left + viewportToContentHorizontalOffset;
                                float localRight = right + viewportToContentHorizontalOffset;
                                float localTop = top + viewportToContentVerticalOffset;
                                float localBottom = bottom + viewportToContentVerticalOffset;
                                boolean isTopLeftVisible = Editor.this.isPositionVisible(localLeft, localTop);
                                boolean isBottomRightVisible = Editor.this.isPositionVisible(localRight, localBottom);
                                int characterBoundsFlags = 0;
                                if (isTopLeftVisible || isBottomRightVisible) {
                                    characterBoundsFlags = 0 | 1;
                                }
                                if (!isTopLeftVisible || !isTopLeftVisible) {
                                    characterBoundsFlags |= 2;
                                }
                                if (isRtl) {
                                    characterBoundsFlags |= 4;
                                }
                                builder.addCharacterBounds(offset, localLeft, localTop, localRight, localBottom, characterBoundsFlags);
                            }
                        }
                    }
                }
                if (selectionStart >= 0) {
                    int line2 = layout.getLineForOffset(selectionStart);
                    float insertionMarkerX = layout.getPrimaryHorizontal(selectionStart) + viewportToContentHorizontalOffset;
                    float insertionMarkerTop = ((float) layout.getLineTop(line2)) + viewportToContentVerticalOffset;
                    float insertionMarkerBaseline = ((float) layout.getLineBaseline(line2)) + viewportToContentVerticalOffset;
                    float insertionMarkerBottom = ((float) layout.getLineBottom(line2)) + viewportToContentVerticalOffset;
                    boolean isTopVisible = Editor.this.isPositionVisible(insertionMarkerX, insertionMarkerTop);
                    boolean isBottomVisible = Editor.this.isPositionVisible(insertionMarkerX, insertionMarkerBottom);
                    int insertionMarkerFlags = 0;
                    if (isTopVisible || isBottomVisible) {
                        insertionMarkerFlags = 0 | 1;
                    }
                    if (!isTopVisible || !isBottomVisible) {
                        insertionMarkerFlags |= 2;
                    }
                    if (layout.isRtlCharAt(selectionStart)) {
                        insertionMarkerFlags |= 4;
                    }
                    builder.setInsertionMarkerLocation(insertionMarkerX, insertionMarkerTop, insertionMarkerBaseline, insertionMarkerBottom, insertionMarkerFlags);
                }
                imm.updateCursorAnchorInfo(Editor.this.mTextView, builder.build());
            }
        }
    }

    /* access modifiers changed from: private */
    public abstract class HandleView extends View implements TextViewPositionListener {
        private static final int HISTORY_SIZE = 5;
        private static final int TOUCH_UP_FILTER_DELAY_AFTER = 150;
        private static final int TOUCH_UP_FILTER_DELAY_BEFORE = 350;
        private Runnable mActionPopupShower;
        protected ActionPopupWindow mActionPopupWindow;
        private final PopupWindow mContainer;
        protected Drawable mDrawable;
        protected Drawable mDrawableLtr;
        protected Drawable mDrawableRtl;
        protected int mHorizontalGravity;
        protected int mHotspotX;
        private float mIdealVerticalOffset;
        private boolean mIsDragging;
        private int mLastParentX;
        private int mLastParentY;
        private int mMinSize;
        private int mNumberPreviousOffsets = 0;
        private boolean mPositionHasChanged = true;
        private int mPositionX;
        private int mPositionY;
        private int mPreviousOffset = -1;
        private int mPreviousOffsetIndex = 0;
        private final int[] mPreviousOffsets = new int[5];
        private final long[] mPreviousOffsetsTimes = new long[5];
        private float mTouchOffsetY;
        private float mTouchToWindowOffsetX;
        private float mTouchToWindowOffsetY;

        public abstract int getCurrentCursorOffset();

        /* access modifiers changed from: protected */
        public abstract int getHorizontalGravity(boolean z);

        /* access modifiers changed from: protected */
        public abstract int getHotspotX(Drawable drawable, boolean z);

        public abstract void updatePosition(float f, float f2);

        /* access modifiers changed from: protected */
        public abstract void updateSelection(int i);

        public HandleView(Drawable drawableLtr, Drawable drawableRtl) {
            super(Editor.this.mTextView.getContext());
            this.mContainer = new PopupWindow(Editor.this.mTextView.getContext(), (AttributeSet) null, 16843464);
            this.mContainer.setSplitTouchEnabled(true);
            this.mContainer.setClippingEnabled(false);
            this.mContainer.setWindowLayoutType(1002);
            this.mContainer.setContentView(this);
            this.mDrawableLtr = drawableLtr;
            this.mDrawableRtl = drawableRtl;
            this.mMinSize = Editor.this.mTextView.getContext().getResources().getDimensionPixelSize(R.dimen.text_handle_min_size);
            updateDrawable();
            int handleHeight = getPreferredHeight();
            this.mTouchOffsetY = -0.3f * ((float) handleHeight);
            this.mIdealVerticalOffset = 0.7f * ((float) handleHeight);
        }

        /* access modifiers changed from: protected */
        public void updateDrawable() {
            boolean isRtlCharAtOffset = Editor.this.mTextView.getLayout().isRtlCharAt(getCurrentCursorOffset());
            this.mDrawable = isRtlCharAtOffset ? this.mDrawableRtl : this.mDrawableLtr;
            this.mHotspotX = getHotspotX(this.mDrawable, isRtlCharAtOffset);
            this.mHorizontalGravity = getHorizontalGravity(isRtlCharAtOffset);
        }

        private void startTouchUpFilter(int offset) {
            this.mNumberPreviousOffsets = 0;
            addPositionToTouchUpFilter(offset);
        }

        private void addPositionToTouchUpFilter(int offset) {
            this.mPreviousOffsetIndex = (this.mPreviousOffsetIndex + 1) % 5;
            this.mPreviousOffsets[this.mPreviousOffsetIndex] = offset;
            this.mPreviousOffsetsTimes[this.mPreviousOffsetIndex] = SystemClock.uptimeMillis();
            this.mNumberPreviousOffsets++;
        }

        private void filterOnTouchUp() {
            long now = SystemClock.uptimeMillis();
            int i = 0;
            int index = this.mPreviousOffsetIndex;
            int iMax = Math.min(this.mNumberPreviousOffsets, 5);
            while (i < iMax && now - this.mPreviousOffsetsTimes[index] < 150) {
                i++;
                index = ((this.mPreviousOffsetIndex - i) + 5) % 5;
            }
            if (i > 0 && i < iMax && now - this.mPreviousOffsetsTimes[index] > 350) {
                positionAtCursorOffset(this.mPreviousOffsets[index], false);
            }
        }

        public boolean offsetHasBeenChanged() {
            return this.mNumberPreviousOffsets > 1;
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(getPreferredWidth(), getPreferredHeight());
        }

        private int getPreferredWidth() {
            return Math.max(this.mDrawable.getIntrinsicWidth(), this.mMinSize);
        }

        private int getPreferredHeight() {
            return Math.max(this.mDrawable.getIntrinsicHeight(), this.mMinSize);
        }

        public void show() {
            if (!isShowing()) {
                Editor.this.getPositionListener().addSubscriber(this, true);
                this.mPreviousOffset = -1;
                positionAtCursorOffset(getCurrentCursorOffset(), false);
                hideActionPopupWindow();
            }
        }

        /* access modifiers changed from: protected */
        public void dismiss() {
            this.mIsDragging = false;
            this.mContainer.dismiss();
            onDetached();
        }

        public void hide() {
            dismiss();
            Editor.this.getPositionListener().removeSubscriber(this);
        }

        /* access modifiers changed from: package-private */
        public void showActionPopupWindow(int delay) {
            if (this.mActionPopupWindow == null) {
                this.mActionPopupWindow = new ActionPopupWindow();
            }
            if (this.mActionPopupShower == null) {
                this.mActionPopupShower = new Runnable() {
                    /* class android.widget.Editor.HandleView.AnonymousClass1 */

                    public void run() {
                        HandleView.this.mActionPopupWindow.show();
                    }
                };
            } else {
                Editor.this.mTextView.removeCallbacks(this.mActionPopupShower);
            }
            Editor.this.mTextView.postDelayed(this.mActionPopupShower, (long) delay);
        }

        /* access modifiers changed from: protected */
        public void hideActionPopupWindow() {
            if (this.mActionPopupShower != null) {
                Editor.this.mTextView.removeCallbacks(this.mActionPopupShower);
            }
            if (this.mActionPopupWindow != null) {
                this.mActionPopupWindow.hide();
            }
        }

        public boolean isShowing() {
            return this.mContainer.isShowing();
        }

        private boolean isVisible() {
            if (this.mIsDragging) {
                return true;
            }
            if (Editor.this.mTextView.isInBatchEditMode()) {
                return false;
            }
            return Editor.this.isPositionVisible((float) (this.mPositionX + this.mHotspotX), (float) this.mPositionY);
        }

        /* access modifiers changed from: protected */
        public void positionAtCursorOffset(int offset, boolean parentScrolled) {
            Layout layout = Editor.this.mTextView.getLayout();
            if (layout == null) {
                Editor.this.prepareCursorControllers();
                return;
            }
            boolean offsetChanged = offset != this.mPreviousOffset;
            if (offsetChanged || parentScrolled) {
                if (offsetChanged) {
                    updateSelection(offset);
                    addPositionToTouchUpFilter(offset);
                }
                int line = layout.getLineForOffset(offset);
                this.mPositionX = (int) ((((layout.getPrimaryHorizontal(offset) - 0.5f) - ((float) this.mHotspotX)) - ((float) getHorizontalOffset())) + ((float) getCursorOffset()));
                this.mPositionY = layout.getLineBottom(line);
                this.mPositionX += Editor.this.mTextView.viewportToContentHorizontalOffset();
                this.mPositionY += Editor.this.mTextView.viewportToContentVerticalOffset();
                this.mPreviousOffset = offset;
                this.mPositionHasChanged = true;
            }
        }

        @Override // android.widget.Editor.TextViewPositionListener
        public void updatePosition(int parentPositionX, int parentPositionY, boolean parentPositionChanged, boolean parentScrolled) {
            positionAtCursorOffset(getCurrentCursorOffset(), parentScrolled);
            if (parentPositionChanged || this.mPositionHasChanged) {
                if (this.mIsDragging) {
                    if (!(parentPositionX == this.mLastParentX && parentPositionY == this.mLastParentY)) {
                        this.mTouchToWindowOffsetX += (float) (parentPositionX - this.mLastParentX);
                        this.mTouchToWindowOffsetY += (float) (parentPositionY - this.mLastParentY);
                        this.mLastParentX = parentPositionX;
                        this.mLastParentY = parentPositionY;
                    }
                    onHandleMoved();
                }
                if (isVisible()) {
                    int positionX = parentPositionX + this.mPositionX;
                    int positionY = parentPositionY + this.mPositionY;
                    if (isShowing()) {
                        this.mContainer.update(positionX, positionY, -1, -1);
                    } else {
                        this.mContainer.showAtLocation(Editor.this.mTextView, 0, positionX, positionY);
                    }
                } else if (isShowing()) {
                    dismiss();
                }
                this.mPositionHasChanged = false;
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View
        public void onDraw(Canvas c) {
            int drawWidth = this.mDrawable.getIntrinsicWidth();
            int left = getHorizontalOffset();
            this.mDrawable.setBounds(left, 0, left + drawWidth, this.mDrawable.getIntrinsicHeight());
            this.mDrawable.draw(c);
        }

        private int getHorizontalOffset() {
            int width = getPreferredWidth();
            int drawWidth = this.mDrawable.getIntrinsicWidth();
            switch (this.mHorizontalGravity) {
                case 3:
                    return 0;
                case 4:
                default:
                    return (width - drawWidth) / 2;
                case 5:
                    return width - drawWidth;
            }
        }

        /* access modifiers changed from: protected */
        public int getCursorOffset() {
            return 0;
        }

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent ev) {
            float newVerticalOffset;
            switch (ev.getActionMasked()) {
                case 0:
                    startTouchUpFilter(getCurrentCursorOffset());
                    this.mTouchToWindowOffsetX = ev.getRawX() - ((float) this.mPositionX);
                    this.mTouchToWindowOffsetY = ev.getRawY() - ((float) this.mPositionY);
                    PositionListener positionListener = Editor.this.getPositionListener();
                    this.mLastParentX = positionListener.getPositionX();
                    this.mLastParentY = positionListener.getPositionY();
                    this.mIsDragging = true;
                    break;
                case 1:
                    filterOnTouchUp();
                    this.mIsDragging = false;
                    break;
                case 2:
                    float rawX = ev.getRawX();
                    float rawY = ev.getRawY();
                    float previousVerticalOffset = this.mTouchToWindowOffsetY - ((float) this.mLastParentY);
                    float currentVerticalOffset = (rawY - ((float) this.mPositionY)) - ((float) this.mLastParentY);
                    if (previousVerticalOffset < this.mIdealVerticalOffset) {
                        newVerticalOffset = Math.max(Math.min(currentVerticalOffset, this.mIdealVerticalOffset), previousVerticalOffset);
                    } else {
                        newVerticalOffset = Math.min(Math.max(currentVerticalOffset, this.mIdealVerticalOffset), previousVerticalOffset);
                    }
                    this.mTouchToWindowOffsetY = ((float) this.mLastParentY) + newVerticalOffset;
                    updatePosition((rawX - this.mTouchToWindowOffsetX) + ((float) this.mHotspotX), (rawY - this.mTouchToWindowOffsetY) + this.mTouchOffsetY);
                    break;
                case 3:
                    this.mIsDragging = false;
                    break;
            }
            return true;
        }

        public boolean isDragging() {
            return this.mIsDragging;
        }

        /* access modifiers changed from: package-private */
        public void onHandleMoved() {
            hideActionPopupWindow();
        }

        public void onDetached() {
            hideActionPopupWindow();
        }
    }

    /* access modifiers changed from: private */
    public class InsertionHandleView extends HandleView {
        private static final int DELAY_BEFORE_HANDLE_FADES_OUT = 4000;
        private static final int RECENT_CUT_COPY_DURATION = 15000;
        private float mDownPositionX;
        private float mDownPositionY;
        private Runnable mHider;

        public InsertionHandleView(Drawable drawable) {
            super(drawable, drawable);
        }

        @Override // android.widget.Editor.HandleView
        public void show() {
            super.show();
            if (SystemClock.uptimeMillis() - TextView.LAST_CUT_OR_COPY_TIME < 15000) {
                showActionPopupWindow(0);
            }
            hideAfterDelay();
        }

        public void showWithActionPopup() {
            show();
            showActionPopupWindow(0);
        }

        private void hideAfterDelay() {
            if (this.mHider == null) {
                this.mHider = new Runnable() {
                    /* class android.widget.Editor.InsertionHandleView.AnonymousClass1 */

                    public void run() {
                        InsertionHandleView.this.hide();
                    }
                };
            } else {
                removeHiderCallback();
            }
            Editor.this.mTextView.postDelayed(this.mHider, 4000);
        }

        private void removeHiderCallback() {
            if (this.mHider != null) {
                Editor.this.mTextView.removeCallbacks(this.mHider);
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public int getHotspotX(Drawable drawable, boolean isRtlRun) {
            return drawable.getIntrinsicWidth() / 2;
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public int getHorizontalGravity(boolean isRtlRun) {
            return 1;
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public int getCursorOffset() {
            int offset = super.getCursorOffset();
            Drawable cursor = Editor.this.mCursorCount > 0 ? Editor.this.mCursorDrawable[0] : null;
            if (cursor == null) {
                return offset;
            }
            cursor.getPadding(Editor.this.mTempRect);
            return offset + (((cursor.getIntrinsicWidth() - Editor.this.mTempRect.left) - Editor.this.mTempRect.right) / 2);
        }

        @Override // android.view.View, android.widget.Editor.HandleView
        public boolean onTouchEvent(MotionEvent ev) {
            boolean result = super.onTouchEvent(ev);
            switch (ev.getActionMasked()) {
                case 0:
                    this.mDownPositionX = ev.getRawX();
                    this.mDownPositionY = ev.getRawY();
                    break;
                case 1:
                    if (!offsetHasBeenChanged()) {
                        float deltaX = this.mDownPositionX - ev.getRawX();
                        float deltaY = this.mDownPositionY - ev.getRawY();
                        float distanceSquared = (deltaX * deltaX) + (deltaY * deltaY);
                        int touchSlop = ViewConfiguration.get(Editor.this.mTextView.getContext()).getScaledTouchSlop();
                        if (distanceSquared < ((float) (touchSlop * touchSlop))) {
                            if (this.mActionPopupWindow == null || !this.mActionPopupWindow.isShowing()) {
                                showWithActionPopup();
                            } else {
                                this.mActionPopupWindow.hide();
                            }
                        }
                    }
                    hideAfterDelay();
                    break;
                case 3:
                    hideAfterDelay();
                    break;
            }
            return result;
        }

        @Override // android.widget.Editor.HandleView
        public int getCurrentCursorOffset() {
            return Editor.this.mTextView.getSelectionStart();
        }

        @Override // android.widget.Editor.HandleView
        public void updateSelection(int offset) {
            Selection.setSelection((Spannable) Editor.this.mTextView.getText(), offset);
        }

        @Override // android.widget.Editor.HandleView
        public void updatePosition(float x, float y) {
            positionAtCursorOffset(Editor.this.mTextView.getOffsetForPosition(x, y), false);
        }

        /* access modifiers changed from: package-private */
        @Override // android.widget.Editor.HandleView
        public void onHandleMoved() {
            super.onHandleMoved();
            removeHiderCallback();
        }

        @Override // android.widget.Editor.HandleView
        public void onDetached() {
            super.onDetached();
            removeHiderCallback();
        }
    }

    /* access modifiers changed from: private */
    public class SelectionStartHandleView extends HandleView {
        public SelectionStartHandleView(Drawable drawableLtr, Drawable drawableRtl) {
            super(drawableLtr, drawableRtl);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public int getHotspotX(Drawable drawable, boolean isRtlRun) {
            if (isRtlRun) {
                return drawable.getIntrinsicWidth() / 4;
            }
            return (drawable.getIntrinsicWidth() * 3) / 4;
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public int getHorizontalGravity(boolean isRtlRun) {
            return isRtlRun ? 5 : 3;
        }

        @Override // android.widget.Editor.HandleView
        public int getCurrentCursorOffset() {
            return Editor.this.mTextView.getSelectionStart();
        }

        @Override // android.widget.Editor.HandleView
        public void updateSelection(int offset) {
            Selection.setSelection((Spannable) Editor.this.mTextView.getText(), offset, Editor.this.mTextView.getSelectionEnd());
            updateDrawable();
        }

        @Override // android.widget.Editor.HandleView
        public void updatePosition(float x, float y) {
            int offset = Editor.this.mTextView.getOffsetForPosition(x, y);
            int selectionEnd = Editor.this.mTextView.getSelectionEnd();
            if (offset >= selectionEnd) {
                offset = Math.max(0, selectionEnd - 1);
            }
            positionAtCursorOffset(offset, false);
        }

        public ActionPopupWindow getActionPopupWindow() {
            return this.mActionPopupWindow;
        }
    }

    /* access modifiers changed from: private */
    public class SelectionEndHandleView extends HandleView {
        public SelectionEndHandleView(Drawable drawableLtr, Drawable drawableRtl) {
            super(drawableLtr, drawableRtl);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public int getHotspotX(Drawable drawable, boolean isRtlRun) {
            if (isRtlRun) {
                return (drawable.getIntrinsicWidth() * 3) / 4;
            }
            return drawable.getIntrinsicWidth() / 4;
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Editor.HandleView
        public int getHorizontalGravity(boolean isRtlRun) {
            return isRtlRun ? 3 : 5;
        }

        @Override // android.widget.Editor.HandleView
        public int getCurrentCursorOffset() {
            return Editor.this.mTextView.getSelectionEnd();
        }

        @Override // android.widget.Editor.HandleView
        public void updateSelection(int offset) {
            Selection.setSelection((Spannable) Editor.this.mTextView.getText(), Editor.this.mTextView.getSelectionStart(), offset);
            updateDrawable();
        }

        @Override // android.widget.Editor.HandleView
        public void updatePosition(float x, float y) {
            int offset = Editor.this.mTextView.getOffsetForPosition(x, y);
            int selectionStart = Editor.this.mTextView.getSelectionStart();
            if (offset <= selectionStart) {
                offset = Math.min(selectionStart + 1, Editor.this.mTextView.getText().length());
            }
            positionAtCursorOffset(offset, false);
        }

        public void setActionPopupWindow(ActionPopupWindow actionPopupWindow) {
            this.mActionPopupWindow = actionPopupWindow;
        }
    }

    /* access modifiers changed from: private */
    public class InsertionPointCursorController implements CursorController {
        private InsertionHandleView mHandle;

        private InsertionPointCursorController() {
        }

        @Override // android.widget.Editor.CursorController
        public void show() {
            getHandle().show();
        }

        public void showWithActionPopup() {
            getHandle().showWithActionPopup();
        }

        @Override // android.widget.Editor.CursorController
        public void hide() {
            if (this.mHandle != null) {
                this.mHandle.hide();
            }
        }

        @Override // android.view.ViewTreeObserver.OnTouchModeChangeListener
        public void onTouchModeChanged(boolean isInTouchMode) {
            if (!isInTouchMode) {
                hide();
            }
        }

        private InsertionHandleView getHandle() {
            if (Editor.this.mSelectHandleCenter == null) {
                Editor.this.mSelectHandleCenter = Editor.this.mTextView.getContext().getDrawable(Editor.this.mTextView.mTextSelectHandleRes);
            }
            if (this.mHandle == null) {
                this.mHandle = new InsertionHandleView(Editor.this.mSelectHandleCenter);
            }
            return this.mHandle;
        }

        @Override // android.widget.Editor.CursorController
        public void onDetached() {
            Editor.this.mTextView.getViewTreeObserver().removeOnTouchModeChangeListener(this);
            if (this.mHandle != null) {
                this.mHandle.onDetached();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class SelectionModifierCursorController implements CursorController {
        private static final int DELAY_BEFORE_REPLACE_ACTION = 200;
        private float mDownPositionX;
        private float mDownPositionY;
        private SelectionEndHandleView mEndHandle;
        private boolean mGestureStayedInTapRegion;
        private int mMaxTouchOffset;
        private int mMinTouchOffset;
        private long mPreviousTapUpTime = 0;
        private SelectionStartHandleView mStartHandle;

        SelectionModifierCursorController() {
            resetTouchOffsets();
        }

        @Override // android.widget.Editor.CursorController
        public void show() {
            if (!Editor.this.mTextView.isInBatchEditMode()) {
                initDrawables();
                initHandles();
                Editor.this.hideInsertionPointCursorController();
            }
        }

        private void initDrawables() {
            if (Editor.this.mSelectHandleLeft == null) {
                Editor.this.mSelectHandleLeft = Editor.this.mTextView.getContext().getDrawable(Editor.this.mTextView.mTextSelectHandleLeftRes);
            }
            if (Editor.this.mSelectHandleRight == null) {
                Editor.this.mSelectHandleRight = Editor.this.mTextView.getContext().getDrawable(Editor.this.mTextView.mTextSelectHandleRightRes);
            }
        }

        private void initHandles() {
            if (this.mStartHandle == null) {
                this.mStartHandle = new SelectionStartHandleView(Editor.this.mSelectHandleLeft, Editor.this.mSelectHandleRight);
            }
            if (this.mEndHandle == null) {
                this.mEndHandle = new SelectionEndHandleView(Editor.this.mSelectHandleRight, Editor.this.mSelectHandleLeft);
            }
            this.mStartHandle.show();
            this.mEndHandle.show();
            this.mStartHandle.showActionPopupWindow(200);
            this.mEndHandle.setActionPopupWindow(this.mStartHandle.getActionPopupWindow());
            Editor.this.hideInsertionPointCursorController();
        }

        @Override // android.widget.Editor.CursorController
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
                    int offsetForPosition = Editor.this.mTextView.getOffsetForPosition(x, y);
                    this.mMaxTouchOffset = offsetForPosition;
                    this.mMinTouchOffset = offsetForPosition;
                    if (this.mGestureStayedInTapRegion && SystemClock.uptimeMillis() - this.mPreviousTapUpTime <= ((long) ViewConfiguration.getDoubleTapTimeout())) {
                        float deltaX = x - this.mDownPositionX;
                        float deltaY = y - this.mDownPositionY;
                        float distanceSquared = (deltaX * deltaX) + (deltaY * deltaY);
                        int doubleTapSlop = ViewConfiguration.get(Editor.this.mTextView.getContext()).getScaledDoubleTapSlop();
                        if ((distanceSquared < ((float) (doubleTapSlop * doubleTapSlop))) && Editor.this.isPositionOnText(x, y)) {
                            Editor.this.startSelectionActionMode();
                            Editor.this.mDiscardNextActionUp = true;
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
                        int doubleTapTouchSlop = ViewConfiguration.get(Editor.this.mTextView.getContext()).getScaledDoubleTapTouchSlop();
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
                    if (Editor.this.mTextView.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT)) {
                        updateMinAndMaxOffsets(event);
                        return;
                    }
                    return;
            }
        }

        private void updateMinAndMaxOffsets(MotionEvent event) {
            int pointerCount = event.getPointerCount();
            for (int index = 0; index < pointerCount; index++) {
                int offset = Editor.this.mTextView.getOffsetForPosition(event.getX(index), event.getY(index));
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

        @Override // android.view.ViewTreeObserver.OnTouchModeChangeListener
        public void onTouchModeChanged(boolean isInTouchMode) {
            if (!isInTouchMode) {
                hide();
            }
        }

        @Override // android.widget.Editor.CursorController
        public void onDetached() {
            Editor.this.mTextView.getViewTreeObserver().removeOnTouchModeChangeListener(this);
            if (this.mStartHandle != null) {
                this.mStartHandle.onDetached();
            }
            if (this.mEndHandle != null) {
                this.mEndHandle.onDetached();
            }
        }
    }

    /* access modifiers changed from: private */
    public class CorrectionHighlighter {
        private static final int FADE_OUT_DURATION = 400;
        private int mEnd;
        private long mFadingStartTime;
        private final Paint mPaint = new Paint(1);
        private final Path mPath = new Path();
        private int mStart;
        private RectF mTempRectF;

        public CorrectionHighlighter() {
            this.mPaint.setCompatibilityScaling(Editor.this.mTextView.getResources().getCompatibilityInfo().applicationScale);
            this.mPaint.setStyle(Paint.Style.FILL);
        }

        public void highlight(CorrectionInfo info) {
            this.mStart = info.getOffset();
            this.mEnd = this.mStart + info.getNewText().length();
            this.mFadingStartTime = SystemClock.uptimeMillis();
            if (this.mStart < 0 || this.mEnd < 0) {
                stopAnimation();
            }
        }

        public void draw(Canvas canvas, int cursorOffsetVertical) {
            if (!updatePath() || !updatePaint()) {
                stopAnimation();
                invalidate(false);
                return;
            }
            if (cursorOffsetVertical != 0) {
                canvas.translate(0.0f, (float) cursorOffsetVertical);
            }
            canvas.drawPath(this.mPath, this.mPaint);
            if (cursorOffsetVertical != 0) {
                canvas.translate(0.0f, (float) (-cursorOffsetVertical));
            }
            invalidate(true);
        }

        private boolean updatePaint() {
            long duration = SystemClock.uptimeMillis() - this.mFadingStartTime;
            if (duration > 400) {
                return false;
            }
            this.mPaint.setColor((Editor.this.mTextView.mHighlightColor & 16777215) + (((int) (((float) Color.alpha(Editor.this.mTextView.mHighlightColor)) * (1.0f - (((float) duration) / 400.0f)))) << 24));
            return true;
        }

        private boolean updatePath() {
            Layout layout = Editor.this.mTextView.getLayout();
            if (layout == null) {
                return false;
            }
            int length = Editor.this.mTextView.getText().length();
            int start = Math.min(length, this.mStart);
            int end = Math.min(length, this.mEnd);
            this.mPath.reset();
            layout.getSelectionPath(start, end, this.mPath);
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void invalidate(boolean delayed) {
            if (Editor.this.mTextView.getLayout() != null) {
                if (this.mTempRectF == null) {
                    this.mTempRectF = new RectF();
                }
                this.mPath.computeBounds(this.mTempRectF, false);
                int left = Editor.this.mTextView.getCompoundPaddingLeft();
                int top = Editor.this.mTextView.getExtendedPaddingTop() + Editor.this.mTextView.getVerticalOffset(true);
                if (delayed) {
                    Editor.this.mTextView.postInvalidateOnAnimation(((int) this.mTempRectF.left) + left, ((int) this.mTempRectF.top) + top, ((int) this.mTempRectF.right) + left, ((int) this.mTempRectF.bottom) + top);
                } else {
                    Editor.this.mTextView.postInvalidate((int) this.mTempRectF.left, (int) this.mTempRectF.top, (int) this.mTempRectF.right, (int) this.mTempRectF.bottom);
                }
            }
        }

        private void stopAnimation() {
            Editor.this.mCorrectionHighlighter = null;
        }
    }

    /* access modifiers changed from: private */
    public static class ErrorPopup extends PopupWindow {
        private boolean mAbove = false;
        private int mPopupInlineErrorAboveBackgroundId = 0;
        private int mPopupInlineErrorBackgroundId = 0;
        private final TextView mView;

        ErrorPopup(TextView v, int width, int height) {
            super(v, width, height);
            this.mView = v;
            this.mPopupInlineErrorBackgroundId = getResourceId(this.mPopupInlineErrorBackgroundId, 230);
            this.mView.setBackgroundResource(this.mPopupInlineErrorBackgroundId);
        }

        /* access modifiers changed from: package-private */
        public void fixDirection(boolean above) {
            this.mAbove = above;
            if (above) {
                this.mPopupInlineErrorAboveBackgroundId = getResourceId(this.mPopupInlineErrorAboveBackgroundId, 231);
            } else {
                this.mPopupInlineErrorBackgroundId = getResourceId(this.mPopupInlineErrorBackgroundId, 230);
            }
            this.mView.setBackgroundResource(above ? this.mPopupInlineErrorAboveBackgroundId : this.mPopupInlineErrorBackgroundId);
        }

        private int getResourceId(int currentId, int index) {
            if (currentId != 0) {
                return currentId;
            }
            TypedArray styledAttributes = this.mView.getContext().obtainStyledAttributes(android.R.styleable.Theme);
            int currentId2 = styledAttributes.getResourceId(index, 0);
            styledAttributes.recycle();
            return currentId2;
        }

        @Override // android.widget.PopupWindow
        public void update(int x, int y, int w, int h, boolean force) {
            super.update(x, y, w, h, force);
            boolean above = isAboveAnchor();
            if (above != this.mAbove) {
                fixDirection(above);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class InputContentType {
        boolean enterDown;
        Bundle extras;
        int imeActionId;
        CharSequence imeActionLabel;
        int imeOptions = 0;
        TextView.OnEditorActionListener onEditorActionListener;
        String privateImeOptions;

        InputContentType() {
        }
    }

    /* access modifiers changed from: package-private */
    public static class InputMethodState {
        int mBatchEditNesting;
        int mChangedDelta;
        int mChangedEnd;
        int mChangedStart;
        boolean mContentChanged;
        boolean mCursorChanged;
        Rect mCursorRectInWindow = new Rect();
        final ExtractedText mExtractedText = new ExtractedText();
        ExtractedTextRequest mExtractedTextRequest;
        boolean mSelectionModeChanged;
        float[] mTmpOffset = new float[2];

        InputMethodState() {
        }
    }

    public static class UndoInputFilter implements InputFilter {
        final Editor mEditor;

        public UndoInputFilter(Editor editor) {
            this.mEditor = editor;
        }

        @Override // android.text.InputFilter
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            SpannableStringBuilder str;
            UndoManager um = this.mEditor.mUndoManager;
            if (!um.isInUndo()) {
                um.beginUpdate("Edit text");
                TextModifyOperation op = (TextModifyOperation) um.getLastOperation(TextModifyOperation.class, this.mEditor.mUndoOwner, 1);
                if (op != null) {
                    if (op.mOldText == null) {
                        if (start < end && ((dstart >= op.mRangeStart && dend <= op.mRangeEnd) || (dstart == op.mRangeEnd && dend == op.mRangeEnd))) {
                            op.mRangeEnd = (end - start) + dstart;
                            um.endUpdate();
                        }
                    } else if (start == end && dend == op.mRangeStart - 1) {
                        if (op.mOldText instanceof SpannableString) {
                            str = (SpannableStringBuilder) op.mOldText;
                        } else {
                            str = new SpannableStringBuilder(op.mOldText);
                        }
                        str.insert(0, (CharSequence) dest, dstart, dend);
                        op.mRangeStart = dstart;
                        op.mOldText = str;
                        um.endUpdate();
                    }
                    um.commitState(null);
                    um.setUndoLabel("Edit text");
                }
                TextModifyOperation op2 = new TextModifyOperation(this.mEditor.mUndoOwner);
                op2.mRangeStart = dstart;
                if (start < end) {
                    op2.mRangeEnd = (end - start) + dstart;
                } else {
                    op2.mRangeEnd = dstart;
                }
                if (dstart < dend) {
                    op2.mOldText = dest.subSequence(dstart, dend);
                }
                um.addOperation(op2, 0);
                um.endUpdate();
            }
            return null;
        }
    }

    public static class TextModifyOperation extends UndoOperation<TextView> {
        public static final Parcelable.ClassLoaderCreator<TextModifyOperation> CREATOR = new Parcelable.ClassLoaderCreator<TextModifyOperation>() {
            /* class android.widget.Editor.TextModifyOperation.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public TextModifyOperation createFromParcel(Parcel in) {
                return new TextModifyOperation(in, null);
            }

            @Override // android.os.Parcelable.ClassLoaderCreator
            public TextModifyOperation createFromParcel(Parcel in, ClassLoader loader) {
                return new TextModifyOperation(in, loader);
            }

            @Override // android.os.Parcelable.Creator
            public TextModifyOperation[] newArray(int size) {
                return new TextModifyOperation[size];
            }
        };
        CharSequence mOldText;
        int mRangeEnd;
        int mRangeStart;

        public TextModifyOperation(UndoOwner owner) {
            super(owner);
        }

        public TextModifyOperation(Parcel src, ClassLoader loader) {
            super(src, loader);
            this.mRangeStart = src.readInt();
            this.mRangeEnd = src.readInt();
            this.mOldText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(src);
        }

        @Override // android.content.UndoOperation
        public void commit() {
        }

        @Override // android.content.UndoOperation
        public void undo() {
            swapText();
        }

        @Override // android.content.UndoOperation
        public void redo() {
            swapText();
        }

        private void swapText() {
            CharSequence curText;
            Editable editable = (Editable) ((TextView) getOwnerData()).getText();
            if (this.mRangeStart >= this.mRangeEnd) {
                curText = null;
            } else {
                curText = editable.subSequence(this.mRangeStart, this.mRangeEnd);
            }
            if (this.mOldText == null) {
                editable.delete(this.mRangeStart, this.mRangeEnd);
                this.mRangeEnd = this.mRangeStart;
            } else {
                editable.replace(this.mRangeStart, this.mRangeEnd, this.mOldText);
                this.mRangeEnd = this.mRangeStart + this.mOldText.length();
            }
            this.mOldText = curText;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mRangeStart);
            dest.writeInt(this.mRangeEnd);
            TextUtils.writeToParcel(this.mOldText, dest, flags);
        }
    }
}
