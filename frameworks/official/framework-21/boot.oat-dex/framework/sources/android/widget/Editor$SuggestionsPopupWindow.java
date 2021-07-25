package android.widget;

import android.R;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.provider.UserDictionary;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.SuggestionRangeSpan;
import android.text.style.SuggestionSpan;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Editor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

class Editor$SuggestionsPopupWindow extends Editor$PinnedPopupWindow implements AdapterView.OnItemClickListener {
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
    final /* synthetic */ Editor this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public Editor$SuggestionsPopupWindow(Editor editor) {
        super(editor);
        this.this$0 = editor;
        this.mCursorWasVisibleBeforeSuggestions = editor.mCursorVisible;
        this.mSuggestionSpanComparator = new SuggestionSpanComparator(this, (Editor.1) null);
        this.mSpansLengths = new HashMap<>();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.Editor$PinnedPopupWindow
    public void createPopupWindow() {
        this.mPopupWindow = new CustomPopupWindow(this, Editor.access$700(this.this$0).getContext(), 16843635);
        this.mPopupWindow.setInputMethodMode(2);
        this.mPopupWindow.setFocusable(true);
        this.mPopupWindow.setClippingEnabled(false);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.Editor$PinnedPopupWindow
    public void initContentView() {
        ListView listView = new ListView(Editor.access$700(this.this$0).getContext());
        this.mSuggestionsAdapter = new SuggestionAdapter(this, (Editor.1) null);
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
            this.highlightSpan = new TextAppearanceSpan(Editor.access$700(Editor$SuggestionsPopupWindow.this.this$0).getContext(), R.style.TextAppearance_SuggestionHighlight);
        }
    }

    private SuggestionSpan[] getSuggestionSpans() {
        int pos = Editor.access$700(this.this$0).getSelectionStart();
        Spannable spannable = (Spannable) Editor.access$700(this.this$0).getText();
        SuggestionSpan[] suggestionSpans = (SuggestionSpan[]) spannable.getSpans(pos, pos, SuggestionSpan.class);
        this.mSpansLengths.clear();
        for (SuggestionSpan suggestionSpan : suggestionSpans) {
            this.mSpansLengths.put(suggestionSpan, Integer.valueOf(spannable.getSpanEnd(suggestionSpan) - spannable.getSpanStart(suggestionSpan)));
        }
        Arrays.sort(suggestionSpans, this.mSuggestionSpanComparator);
        return suggestionSpans;
    }

    @Override // android.widget.Editor$PinnedPopupWindow
    public void show() {
        if ((Editor.access$700(this.this$0).getText() instanceof Editable) && updateSuggestions()) {
            this.mCursorWasVisibleBeforeSuggestions = this.this$0.mCursorVisible;
            Editor.access$700(this.this$0).setCursorVisible(false);
            this.mIsShowingUp = true;
            super.show();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.Editor$PinnedPopupWindow
    public void measureContent() {
        DisplayMetrics displayMetrics = Editor.access$700(this.this$0).getResources().getDisplayMetrics();
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
            if (Editor.access$2300(this.this$0) == null) {
                Editor.access$2302(this.this$0, new Rect());
            }
            popupBackground.getPadding(Editor.access$2300(this.this$0));
            width += Editor.access$2300(this.this$0).left + Editor.access$2300(this.this$0).right;
        }
        this.mPopupWindow.setWidth(width);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.Editor$PinnedPopupWindow
    public int getTextOffset() {
        return Editor.access$700(this.this$0).getSelectionStart();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.Editor$PinnedPopupWindow
    public int getVerticalLocalPosition(int line) {
        return Editor.access$700(this.this$0).getLayout().getLineBottom(line);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.Editor$PinnedPopupWindow
    public int clipVertically(int positionY) {
        return Math.min(positionY, Editor.access$700(this.this$0).getResources().getDisplayMetrics().heightPixels - this.mContentView.getMeasuredHeight());
    }

    @Override // android.widget.Editor$PinnedPopupWindow
    public void hide() {
        super.hide();
    }

    private boolean updateSuggestions() {
        Spannable spannable = (Spannable) Editor.access$700(this.this$0).getText();
        SuggestionSpan[] suggestionSpans = getSuggestionSpans();
        int nbSpans = suggestionSpans.length;
        if (nbSpans == 0) {
            return false;
        }
        this.mNumberOfSuggestions = 0;
        int spanUnionStart = Editor.access$700(this.this$0).getText().length();
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
                suggestionInfo2.text.replace(0, suggestionInfo2.text.length(), (CharSequence) Editor.access$700(this.this$0).getContext().getString(17040468));
                suggestionInfo2.text.setSpan(suggestionInfo2.highlightSpan, 0, 0, 33);
                this.mNumberOfSuggestions++;
            }
        }
        SuggestionInfo suggestionInfo3 = this.mSuggestionInfos[this.mNumberOfSuggestions];
        suggestionInfo3.suggestionSpan = null;
        suggestionInfo3.suggestionIndex = -2;
        suggestionInfo3.text.replace(0, suggestionInfo3.text.length(), (CharSequence) Editor.access$700(this.this$0).getContext().getString(17040469));
        suggestionInfo3.text.setSpan(suggestionInfo3.highlightSpan, 0, 0, 33);
        this.mNumberOfSuggestions++;
        if (this.this$0.mSuggestionRangeSpan == null) {
            this.this$0.mSuggestionRangeSpan = new SuggestionRangeSpan();
        }
        if (underlineColor == 0) {
            this.this$0.mSuggestionRangeSpan.setBackgroundColor(Editor.access$700(this.this$0).mHighlightColor);
        } else {
            this.this$0.mSuggestionRangeSpan.setBackgroundColor((16777215 & underlineColor) + (((int) (((float) Color.alpha(underlineColor)) * 0.4f)) << 24));
        }
        spannable.setSpan(this.this$0.mSuggestionRangeSpan, spanUnionStart, spanUnionEnd, 33);
        this.mSuggestionsAdapter.notifyDataSetChanged();
        return true;
    }

    private void highlightTextDifferences(SuggestionInfo suggestionInfo, int unionStart, int unionEnd) {
        Spannable text = (Spannable) Editor.access$700(this.this$0).getText();
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
        Editable editable = (Editable) Editor.access$700(this.this$0).getText();
        SuggestionInfo suggestionInfo = this.mSuggestionInfos[position];
        if (suggestionInfo.suggestionIndex == -2) {
            int spanUnionStart = editable.getSpanStart(this.this$0.mSuggestionRangeSpan);
            int spanUnionEnd = editable.getSpanEnd(this.this$0.mSuggestionRangeSpan);
            if (spanUnionStart >= 0 && spanUnionEnd > spanUnionStart) {
                if (spanUnionEnd < editable.length() && Character.isSpaceChar(editable.charAt(spanUnionEnd)) && (spanUnionStart == 0 || Character.isSpaceChar(editable.charAt(spanUnionStart - 1)))) {
                    spanUnionEnd++;
                }
                Editor.access$700(this.this$0).deleteText_internal(spanUnionStart, spanUnionEnd);
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
            intent.putExtra(UserDictionary.Words.LOCALE, Editor.access$700(this.this$0).getTextServicesLocale().toString());
            intent.setFlags(intent.getFlags() | 268435456);
            Editor.access$700(this.this$0).getContext().startActivity(intent);
            editable.removeSpan(suggestionInfo.suggestionSpan);
            Selection.setSelection(editable, spanEnd);
            Editor.access$2400(this.this$0, spanStart, spanEnd, false);
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
            Editor.access$700(this.this$0).replaceText_internal(spanStart, spanEnd, suggestion);
            suggestionInfo.suggestionSpan.notifySelection(Editor.access$700(this.this$0).getContext(), originalText, suggestionInfo.suggestionIndex);
            suggestionInfo.suggestionSpan.getSuggestions()[suggestionInfo.suggestionIndex] = originalText;
            int lengthDifference = suggestion.length() - (spanEnd - spanStart);
            for (int i2 = 0; i2 < length; i2++) {
                if (suggestionSpansStarts[i2] <= spanStart && suggestionSpansEnds[i2] >= spanEnd) {
                    Editor.access$700(this.this$0).setSpan_internal(suggestionSpans[i2], suggestionSpansStarts[i2], suggestionSpansEnds[i2] + lengthDifference, suggestionSpansFlags[i2]);
                }
            }
            int newCursorPosition = spanEnd + lengthDifference;
            Editor.access$700(this.this$0).setCursorPosition_internal(newCursorPosition, newCursorPosition);
        }
        hide();
    }
}
