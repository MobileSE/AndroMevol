package android.widget;

import android.content.Context;
import android.net.ProxyInfo;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimpleAdapter extends BaseAdapter implements Filterable {
    private List<? extends Map<String, ?>> mData;
    private int mDropDownResource;
    private SimpleFilter mFilter;
    private String[] mFrom;
    private LayoutInflater mInflater;
    private int mResource;
    private int[] mTo;
    private ArrayList<Map<String, ?>> mUnfilteredData;
    private ViewBinder mViewBinder;

    public interface ViewBinder {
        boolean setViewValue(View view, Object obj, String str);
    }

    public SimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        this.mData = data;
        this.mDropDownResource = resource;
        this.mResource = resource;
        this.mFrom = from;
        this.mTo = to;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.mData.size();
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        return this.mData.get(position);
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        return (long) position;
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, this.mResource);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
        View v;
        if (convertView == null) {
            v = this.mInflater.inflate(resource, parent, false);
        } else {
            v = convertView;
        }
        bindView(position, v);
        return v;
    }

    public void setDropDownViewResource(int resource) {
        this.mDropDownResource = resource;
    }

    @Override // android.widget.SpinnerAdapter, android.widget.BaseAdapter
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, this.mDropDownResource);
    }

    private void bindView(int position, View view) {
        Map dataSet = (Map) this.mData.get(position);
        if (dataSet != null) {
            ViewBinder binder = this.mViewBinder;
            String[] from = this.mFrom;
            int[] to = this.mTo;
            int count = to.length;
            for (int i = 0; i < count; i++) {
                View v = view.findViewById(to[i]);
                if (v != null) {
                    Object data = dataSet.get(from[i]);
                    String text = data == null ? ProxyInfo.LOCAL_EXCL_LIST : data.toString();
                    if (text == null) {
                        text = ProxyInfo.LOCAL_EXCL_LIST;
                    }
                    boolean bound = false;
                    if (binder != null) {
                        bound = binder.setViewValue(v, data, text);
                    }
                    if (bound) {
                        continue;
                    } else if (v instanceof Checkable) {
                        if (data instanceof Boolean) {
                            ((Checkable) v).setChecked(((Boolean) data).booleanValue());
                        } else if (v instanceof TextView) {
                            setViewText((TextView) v, text);
                        } else {
                            throw new IllegalStateException(v.getClass().getName() + " should be bound to a Boolean, not a " + (data == null ? "<unknown type>" : data.getClass()));
                        }
                    } else if (v instanceof TextView) {
                        setViewText((TextView) v, text);
                    } else if (!(v instanceof ImageView)) {
                        throw new IllegalStateException(v.getClass().getName() + " is not a " + " view that can be bounds by this SimpleAdapter");
                    } else if (data instanceof Integer) {
                        setViewImage((ImageView) v, ((Integer) data).intValue());
                    } else {
                        setViewImage((ImageView) v, text);
                    }
                }
            }
        }
    }

    public ViewBinder getViewBinder() {
        return this.mViewBinder;
    }

    public void setViewBinder(ViewBinder viewBinder) {
        this.mViewBinder = viewBinder;
    }

    public void setViewImage(ImageView v, int value) {
        v.setImageResource(value);
    }

    public void setViewImage(ImageView v, String value) {
        try {
            v.setImageResource(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            v.setImageURI(Uri.parse(value));
        }
    }

    public void setViewText(TextView v, String text) {
        v.setText(text);
    }

    @Override // android.widget.Filterable
    public Filter getFilter() {
        if (this.mFilter == null) {
            this.mFilter = new SimpleFilter();
        }
        return this.mFilter;
    }

    private class SimpleFilter extends Filter {
        private SimpleFilter() {
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.Filter
        public Filter.FilterResults performFiltering(CharSequence prefix) {
            Filter.FilterResults results = new Filter.FilterResults();
            if (SimpleAdapter.this.mUnfilteredData == null) {
                SimpleAdapter.this.mUnfilteredData = new ArrayList(SimpleAdapter.this.mData);
            }
            if (prefix == null || prefix.length() == 0) {
                ArrayList<Map<String, ?>> list = SimpleAdapter.this.mUnfilteredData;
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();
                ArrayList<Map<String, ?>> unfilteredValues = SimpleAdapter.this.mUnfilteredData;
                int count = unfilteredValues.size();
                ArrayList<Map<String, ?>> newValues = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    Map<String, ?> h = unfilteredValues.get(i);
                    if (h != null) {
                        int len = SimpleAdapter.this.mTo.length;
                        for (int j = 0; j < len; j++) {
                            String[] words = ((String) h.get(SimpleAdapter.this.mFrom[j])).split(" ");
                            int wordCount = words.length;
                            int k = 0;
                            while (true) {
                                if (k >= wordCount) {
                                    break;
                                } else if (words[k].toLowerCase().startsWith(prefixString)) {
                                    newValues.add(h);
                                    break;
                                } else {
                                    k++;
                                }
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
            SimpleAdapter.this.mData = (List) results.values;
            if (results.count > 0) {
                SimpleAdapter.this.notifyDataSetChanged();
            } else {
                SimpleAdapter.this.notifyDataSetInvalidated();
            }
        }
    }
}
