package android.provider;

import android.content.Context;

public class SearchIndexableResource extends SearchIndexableData {
    public int xmlResId;

    public SearchIndexableResource(int rank, int xmlResId2, String className, int iconResId) {
        this.rank = rank;
        this.xmlResId = xmlResId2;
        this.className = className;
        this.iconResId = iconResId;
    }

    public SearchIndexableResource(Context context) {
        super(context);
    }

    @Override // android.provider.SearchIndexableData
    public String toString() {
        return "SearchIndexableResource[" + super.toString() + ", " + "xmlResId: " + this.xmlResId + "]";
    }
}
