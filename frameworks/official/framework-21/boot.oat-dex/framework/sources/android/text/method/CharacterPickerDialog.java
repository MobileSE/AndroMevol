package android.text.method;

import android.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListAdapter;

public class CharacterPickerDialog extends Dialog implements AdapterView.OnItemClickListener, View.OnClickListener {
    private Button mCancelButton;
    private LayoutInflater mInflater;
    private boolean mInsert;
    private String mOptions;
    private Editable mText;
    private View mView;

    public CharacterPickerDialog(Context context, View view, Editable text, String options, boolean insert) {
        super(context, R.style.Theme_Panel);
        this.mView = view;
        this.mText = text;
        this.mOptions = options;
        this.mInsert = insert;
        this.mInflater = LayoutInflater.from(context);
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Dialog
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.token = this.mView.getApplicationWindowToken();
        params.type = 1003;
        params.flags |= 1;
        setContentView(17367098);
        GridView grid = (GridView) findViewById(16909022);
        grid.setAdapter((ListAdapter) new OptionsAdapter(getContext()));
        grid.setOnItemClickListener(this);
        this.mCancelButton = (Button) findViewById(16909023);
        this.mCancelButton.setOnClickListener(this);
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        replaceCharacterAndClose(String.valueOf(this.mOptions.charAt(position)));
    }

    private void replaceCharacterAndClose(CharSequence replace) {
        int selEnd = Selection.getSelectionEnd(this.mText);
        if (this.mInsert || selEnd == 0) {
            this.mText.insert(selEnd, replace);
        } else {
            this.mText.replace(selEnd - 1, selEnd, replace);
        }
        dismiss();
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        if (v == this.mCancelButton) {
            dismiss();
        } else if (v instanceof Button) {
            replaceCharacterAndClose(((Button) v).getText());
        }
    }

    private class OptionsAdapter extends BaseAdapter {
        public OptionsAdapter(Context context) {
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Button b = (Button) CharacterPickerDialog.this.mInflater.inflate(17367099, (ViewGroup) null);
            b.setText(String.valueOf(CharacterPickerDialog.this.mOptions.charAt(position)));
            b.setOnClickListener(CharacterPickerDialog.this);
            return b;
        }

        public final int getCount() {
            return CharacterPickerDialog.this.mOptions.length();
        }

        public final Object getItem(int position) {
            return String.valueOf(CharacterPickerDialog.this.mOptions.charAt(position));
        }

        public final long getItemId(int position) {
            return (long) position;
        }
    }
}
