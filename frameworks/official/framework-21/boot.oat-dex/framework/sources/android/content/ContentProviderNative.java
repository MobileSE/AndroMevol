package android.content;

import android.os.Binder;
import android.os.IBinder;

public abstract class ContentProviderNative extends Binder implements IContentProvider {
    public abstract String getProviderName();

    public ContentProviderNative() {
        attachInterface(this, IContentProvider.descriptor);
    }

    public static IContentProvider asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IContentProvider in = (IContentProvider) obj.queryLocalInterface(IContentProvider.descriptor);
        return in == null ? new ContentProviderProxy(obj) : in;
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x00af  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00b4  */
    @Override // android.os.Binder
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTransact(int r45, android.os.Parcel r46, android.os.Parcel r47, int r48) throws android.os.RemoteException {
        /*
        // Method dump skipped, instructions count: 1002
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.ContentProviderNative.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
    }

    @Override // android.os.IInterface
    public IBinder asBinder() {
        return this;
    }
}
