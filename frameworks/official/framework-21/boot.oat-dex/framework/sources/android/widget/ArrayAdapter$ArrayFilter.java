package android.widget;

import android.widget.Filter;
import java.util.ArrayList;
import java.util.List;

class ArrayAdapter$ArrayFilter extends Filter {
    final /* synthetic */ ArrayAdapter this$0;

    private ArrayAdapter$ArrayFilter(ArrayAdapter arrayAdapter) {
        this.this$0 = arrayAdapter;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.Filter
    public Filter.FilterResults performFiltering(CharSequence prefix) {
        ArrayList<T> list;
        ArrayList<T> values;
        Filter.FilterResults results = new Filter.FilterResults();
        if (ArrayAdapter.access$100(this.this$0) == null) {
            synchronized (ArrayAdapter.access$200(this.this$0)) {
                ArrayAdapter.access$102(this.this$0, new ArrayList(ArrayAdapter.access$300(this.this$0)));
            }
        }
        if (prefix == null || prefix.length() == 0) {
            synchronized (ArrayAdapter.access$200(this.this$0)) {
                list = new ArrayList<>(ArrayAdapter.access$100(this.this$0));
            }
            results.values = list;
            results.count = list.size();
        } else {
            String prefixString = prefix.toString().toLowerCase();
            synchronized (ArrayAdapter.access$200(this.this$0)) {
                values = new ArrayList<>(ArrayAdapter.access$100(this.this$0));
            }
            int count = values.size();
            ArrayList<T> newValues = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                T value = values.get(i);
                String valueText = value.toString().toLowerCase();
                if (valueText.startsWith(prefixString)) {
                    newValues.add(value);
                } else {
                    String[] words = valueText.split(" ");
                    int wordCount = words.length;
                    int k = 0;
                    while (true) {
                        if (k >= wordCount) {
                            break;
                        } else if (words[k].startsWith(prefixString)) {
                            newValues.add(value);
                            break;
                        } else {
                            k++;
                        }
                    }
                }
            }
            results.values = newValues;
            results.count = newValues.size();
        }
        return results;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.Filter
    public void publishResults(CharSequence constraint, Filter.FilterResults results) {
        ArrayAdapter.access$302(this.this$0, (List) results.values);
        if (results.count > 0) {
            this.this$0.notifyDataSetChanged();
        } else {
            this.this$0.notifyDataSetInvalidated();
        }
    }
}
