package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class ParceledListSlice<T extends Parcelable> implements Parcelable {
    public static final Parcelable.ClassLoaderCreator<ParceledListSlice> CREATOR = new Parcelable.ClassLoaderCreator<ParceledListSlice>() {
        /* class android.content.pm.ParceledListSlice.AnonymousClass2 */

        @Override // android.os.Parcelable.Creator
        public ParceledListSlice createFromParcel(Parcel in) {
            return new ParceledListSlice(in, null);
        }

        @Override // android.os.Parcelable.ClassLoaderCreator
        public ParceledListSlice createFromParcel(Parcel in, ClassLoader loader) {
            return new ParceledListSlice(in, loader);
        }

        @Override // android.os.Parcelable.Creator
        public ParceledListSlice[] newArray(int size) {
            return new ParceledListSlice[size];
        }
    };
    private static boolean DEBUG = false;
    private static final int MAX_FIRST_IPC_SIZE = 131072;
    private static final int MAX_IPC_SIZE = 262144;
    private static String TAG = "ParceledListSlice";
    private final List<T> mList;

    public ParceledListSlice(List<T> list) {
        this.mList = list;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r9v7, resolved type: java.util.List<T extends android.os.Parcelable> */
    /* JADX DEBUG: Multi-variable search result rejected for r9v13, resolved type: java.util.List<T extends android.os.Parcelable> */
    /* JADX WARN: Multi-variable type inference failed */
    private ParceledListSlice(Parcel p, ClassLoader loader) {
        int N = p.readInt();
        this.mList = new ArrayList(N);
        if (DEBUG) {
            Log.d(TAG, "Retrieving " + N + " items");
        }
        if (N > 0) {
            Parcelable.Creator<T> creator = p.readParcelableCreator(loader);
            Class<?> listElementClass = null;
            int i = 0;
            while (i < N && p.readInt() != 0) {
                Parcelable readCreator = p.readCreator(creator, loader);
                if (listElementClass == null) {
                    listElementClass = readCreator.getClass();
                } else {
                    verifySameType(listElementClass, readCreator.getClass());
                }
                this.mList.add(readCreator);
                if (DEBUG) {
                    Log.d(TAG, "Read inline #" + i + ": " + this.mList.get(this.mList.size() - 1));
                }
                i++;
            }
            if (i < N) {
                IBinder retriever = p.readStrongBinder();
                while (i < N) {
                    if (DEBUG) {
                        Log.d(TAG, "Reading more @" + i + " of " + N + ": retriever=" + retriever);
                    }
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    data.writeInt(i);
                    try {
                        retriever.transact(1, data, reply, 0);
                        while (i < N && reply.readInt() != 0) {
                            Parcelable readCreator2 = reply.readCreator(creator, loader);
                            verifySameType(listElementClass, readCreator2.getClass());
                            this.mList.add(readCreator2);
                            if (DEBUG) {
                                Log.d(TAG, "Read extra #" + i + ": " + this.mList.get(this.mList.size() - 1));
                            }
                            i++;
                        }
                        reply.recycle();
                        data.recycle();
                    } catch (RemoteException e) {
                        Log.w(TAG, "Failure retrieving array; only received " + i + " of " + N, e);
                        return;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static void verifySameType(Class<?> expected, Class<?> actual) {
        if (!actual.equals(expected)) {
            throw new IllegalArgumentException("Can't unparcel type " + actual.getName() + " in list of type " + expected.getName());
        }
    }

    public List<T> getList() {
        return this.mList;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        int contents = 0;
        for (int i = 0; i < this.mList.size(); i++) {
            contents |= this.mList.get(i).describeContents();
        }
        return contents;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, final int flags) {
        final int N = this.mList.size();
        dest.writeInt(N);
        if (DEBUG) {
            Log.d(TAG, "Writing " + N + " items");
        }
        if (N > 0) {
            final Class<?> listElementClass = this.mList.get(0).getClass();
            dest.writeParcelableCreator(this.mList.get(0));
            int i = 0;
            while (i < N && dest.dataSize() < 131072) {
                dest.writeInt(1);
                T parcelable = this.mList.get(i);
                verifySameType(listElementClass, parcelable.getClass());
                parcelable.writeToParcel(dest, flags);
                if (DEBUG) {
                    Log.d(TAG, "Wrote inline #" + i + ": " + this.mList.get(i));
                }
                i++;
            }
            if (i < N) {
                dest.writeInt(0);
                Binder retriever = new Binder() {
                    /* class android.content.pm.ParceledListSlice.AnonymousClass1 */

                    /* access modifiers changed from: protected */
                    @Override // android.os.Binder
                    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                        if (code != 1) {
                            return super.onTransact(code, data, reply, flags);
                        }
                        int i = data.readInt();
                        if (ParceledListSlice.DEBUG) {
                            Log.d(ParceledListSlice.TAG, "Writing more @" + i + " of " + N);
                        }
                        while (i < N && reply.dataSize() < 262144) {
                            reply.writeInt(1);
                            Parcelable parcelable = (Parcelable) ParceledListSlice.this.mList.get(i);
                            ParceledListSlice.verifySameType(listElementClass, parcelable.getClass());
                            parcelable.writeToParcel(reply, flags);
                            if (ParceledListSlice.DEBUG) {
                                Log.d(ParceledListSlice.TAG, "Wrote extra #" + i + ": " + ParceledListSlice.this.mList.get(i));
                            }
                            i++;
                        }
                        if (i >= N) {
                            return true;
                        }
                        if (ParceledListSlice.DEBUG) {
                            Log.d(ParceledListSlice.TAG, "Breaking @" + i + " of " + N);
                        }
                        reply.writeInt(0);
                        return true;
                    }
                };
                if (DEBUG) {
                    Log.d(TAG, "Breaking @" + i + " of " + N + ": retriever=" + retriever);
                }
                dest.writeStrongBinder(retriever);
            }
        }
    }
}
