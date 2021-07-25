package android.widget;

import android.database.DataSetObserver;
import java.lang.ref.WeakReference;

class AutoCompleteTextView$PopupDataSetObserver extends DataSetObserver {
    private final WeakReference<AutoCompleteTextView> mViewReference;
    private final Runnable updateRunnable;

    /* synthetic */ AutoCompleteTextView$PopupDataSetObserver(AutoCompleteTextView x0, AutoCompleteTextView$1 x1) {
        this(x0);
    }

    private AutoCompleteTextView$PopupDataSetObserver(AutoCompleteTextView view) {
        this.updateRunnable = new 1(this);
        this.mViewReference = new WeakReference<>(view);
    }

    public void onChanged() {
        AutoCompleteTextView textView = this.mViewReference.get();
        if (textView != null && AutoCompleteTextView.access$700(textView) != null) {
            textView.post(this.updateRunnable);
        }
    }
}
