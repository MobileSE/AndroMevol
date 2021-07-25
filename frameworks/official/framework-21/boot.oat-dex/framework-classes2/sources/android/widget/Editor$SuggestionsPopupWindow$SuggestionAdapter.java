package android.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Editor;

class Editor$SuggestionsPopupWindow$SuggestionAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    final /* synthetic */ Editor.SuggestionsPopupWindow this$1;

    private Editor$SuggestionsPopupWindow$SuggestionAdapter(Editor.SuggestionsPopupWindow suggestionsPopupWindow) {
        this.this$1 = suggestionsPopupWindow;
        this.mInflater = (LayoutInflater) this.this$1.this$0.mTextView.getContext().getSystemService("layout_inflater");
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return Editor.SuggestionsPopupWindow.access$2000(this.this$1);
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        return Editor.SuggestionsPopupWindow.access$2100(this.this$1)[position];
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        return (long) position;
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) convertView;
        if (textView == null) {
            textView = (TextView) this.mInflater.inflate(this.this$1.this$0.mTextView.mTextEditSuggestionItemLayout, parent, false);
        }
        Editor.SuggestionsPopupWindow.SuggestionInfo suggestionInfo = Editor.SuggestionsPopupWindow.access$2100(this.this$1)[position];
        textView.setText(suggestionInfo.text);
        if (suggestionInfo.suggestionIndex == -1 || suggestionInfo.suggestionIndex == -2) {
            textView.setBackgroundColor(0);
        } else {
            textView.setBackgroundColor(-1);
        }
        return textView;
    }
}
