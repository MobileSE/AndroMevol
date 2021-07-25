package android.content.pm;

import android.accounts.GrantCredentialsPermissionActivity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Environment;
import android.os.Handler;
import android.os.UserHandle;
import android.util.AtomicFile;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.FastXmlSerializer;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public abstract class RegisteredServicesCache<V> {
    private static final boolean DEBUG = false;
    private static final String TAG = "PackageManager";
    private final String mAttributesName;
    public final Context mContext;
    private final BroadcastReceiver mExternalReceiver = new BroadcastReceiver() {
        /* class android.content.pm.RegisteredServicesCache.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            RegisteredServicesCache.this.handlePackageEvent(intent, 0);
        }
    };
    private Handler mHandler;
    private final String mInterfaceName;
    private RegisteredServicesCacheListener<V> mListener;
    private final String mMetaDataName;
    private final BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {
        /* class android.content.pm.RegisteredServicesCache.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
            if (uid != -1) {
                RegisteredServicesCache.this.handlePackageEvent(intent, UserHandle.getUserId(uid));
            }
        }
    };
    private final AtomicFile mPersistentServicesFile;
    @GuardedBy("mServicesLock")
    private boolean mPersistentServicesFileDidNotExist;
    private final XmlSerializerAndParser<V> mSerializerAndParser;
    private final Object mServicesLock = new Object();
    @GuardedBy("mServicesLock")
    private final SparseArray<UserServices<V>> mUserServices = new SparseArray<>(2);

    public abstract V parseServiceAttributes(Resources resources, String str, AttributeSet attributeSet);

    /* access modifiers changed from: private */
    public static class UserServices<V> {
        @GuardedBy("mServicesLock")
        public final Map<V, Integer> persistentServices;
        @GuardedBy("mServicesLock")
        public Map<V, ServiceInfo<V>> services;

        private UserServices() {
            this.persistentServices = Maps.newHashMap();
            this.services = null;
        }
    }

    private UserServices<V> findOrCreateUserLocked(int userId) {
        UserServices<V> services = this.mUserServices.get(userId);
        if (services != null) {
            return services;
        }
        UserServices<V> services2 = new UserServices<>();
        this.mUserServices.put(userId, services2);
        return services2;
    }

    public RegisteredServicesCache(Context context, String interfaceName, String metaDataName, String attributeName, XmlSerializerAndParser<V> serializerAndParser) {
        this.mContext = context;
        this.mInterfaceName = interfaceName;
        this.mMetaDataName = metaDataName;
        this.mAttributesName = attributeName;
        this.mSerializerAndParser = serializerAndParser;
        this.mPersistentServicesFile = new AtomicFile(new File(new File(new File(Environment.getDataDirectory(), "system"), "registered_services"), interfaceName + ".xml"));
        readPersistentServicesLocked();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this.mPackageReceiver, UserHandle.ALL, intentFilter, null, null);
        IntentFilter sdFilter = new IntentFilter();
        sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        this.mContext.registerReceiver(this.mExternalReceiver, sdFilter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void handlePackageEvent(Intent intent, int userId) {
        boolean isRemoval;
        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_REMOVED.equals(action) || Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
            isRemoval = true;
        } else {
            isRemoval = false;
        }
        boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
        if (!isRemoval || !replacing) {
            generateServicesMap(userId);
        }
    }

    public void invalidateCache(int userId) {
        synchronized (this.mServicesLock) {
            findOrCreateUserLocked(userId).services = null;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter fout, String[] args, int userId) {
        synchronized (this.mServicesLock) {
            UserServices<V> user = findOrCreateUserLocked(userId);
            if (user.services != null) {
                fout.println("RegisteredServicesCache: " + user.services.size() + " services");
                Iterator i$ = user.services.values().iterator();
                while (i$.hasNext()) {
                    fout.println("  " + i$.next());
                }
            } else {
                fout.println("RegisteredServicesCache: services not loaded");
            }
        }
    }

    public RegisteredServicesCacheListener<V> getListener() {
        RegisteredServicesCacheListener<V> registeredServicesCacheListener;
        synchronized (this) {
            registeredServicesCacheListener = this.mListener;
        }
        return registeredServicesCacheListener;
    }

    public void setListener(RegisteredServicesCacheListener<V> listener, Handler handler) {
        if (handler == null) {
            handler = new Handler(this.mContext.getMainLooper());
        }
        synchronized (this) {
            this.mHandler = handler;
            this.mListener = listener;
        }
    }

    private void notifyListener(final V type, final int userId, final boolean removed) {
        final RegisteredServicesCacheListener<V> listener;
        Handler handler;
        synchronized (this) {
            listener = this.mListener;
            handler = this.mHandler;
        }
        if (listener != null) {
            handler.post(new Runnable() {
                /* class android.content.pm.RegisteredServicesCache.AnonymousClass3 */

                /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: android.content.pm.RegisteredServicesCacheListener */
                /* JADX WARN: Multi-variable type inference failed */
                public void run() {
                    listener.onServiceChanged(type, userId, removed);
                }
            });
        }
    }

    public static class ServiceInfo<V> {
        public final ComponentName componentName;
        public final V type;
        public final int uid;

        public ServiceInfo(V type2, ComponentName componentName2, int uid2) {
            this.type = type2;
            this.componentName = componentName2;
            this.uid = uid2;
        }

        public String toString() {
            return "ServiceInfo: " + ((Object) this.type) + ", " + this.componentName + ", uid " + this.uid;
        }
    }

    public ServiceInfo<V> getServiceInfo(V type, int userId) {
        ServiceInfo<V> serviceInfo;
        synchronized (this.mServicesLock) {
            UserServices<V> user = findOrCreateUserLocked(userId);
            if (user.services == null) {
                generateServicesMap(userId);
            }
            serviceInfo = user.services.get(type);
        }
        return serviceInfo;
    }

    public Collection<ServiceInfo<V>> getAllServices(int userId) {
        Collection<ServiceInfo<V>> unmodifiableCollection;
        synchronized (this.mServicesLock) {
            UserServices<V> user = findOrCreateUserLocked(userId);
            if (user.services == null) {
                generateServicesMap(userId);
            }
            unmodifiableCollection = Collections.unmodifiableCollection(new ArrayList(user.services.values()));
        }
        return unmodifiableCollection;
    }

    private boolean inSystemImage(int callerUid) {
        for (String name : this.mContext.getPackageManager().getPackagesForUid(callerUid)) {
            try {
                if ((this.mContext.getPackageManager().getPackageInfo(name, 0).applicationInfo.flags & 1) != 0) {
                    return true;
                }
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }
        return false;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r22v0, resolved type: android.content.pm.RegisteredServicesCache<V> */
    /* JADX WARN: Multi-variable type inference failed */
    private void generateServicesMap(int userId) {
        PackageManager pm = this.mContext.getPackageManager();
        ArrayList<ServiceInfo<V>> serviceInfos = new ArrayList<>();
        for (ResolveInfo resolveInfo : pm.queryIntentServicesAsUser(new Intent(this.mInterfaceName), 128, userId)) {
            try {
                ServiceInfo<V> info = parseServiceInfo(resolveInfo);
                if (info == null) {
                    Log.w(TAG, "Unable to load service info " + resolveInfo.toString());
                } else {
                    serviceInfos.add(info);
                }
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Unable to load service info " + resolveInfo.toString(), e);
            } catch (IOException e2) {
                Log.w(TAG, "Unable to load service info " + resolveInfo.toString(), e2);
            }
        }
        synchronized (this.mServicesLock) {
            UserServices<V> user = findOrCreateUserLocked(userId);
            boolean firstScan = user.services == null;
            if (firstScan) {
                user.services = Maps.newHashMap();
            } else {
                user.services.clear();
            }
            new StringBuilder();
            boolean changed = false;
            Iterator i$ = serviceInfos.iterator();
            while (i$.hasNext()) {
                ServiceInfo<V> info2 = i$.next();
                Integer previousUid = user.persistentServices.get(info2.type);
                if (previousUid == null) {
                    changed = true;
                    user.services.put(info2.type, info2);
                    user.persistentServices.put(info2.type, Integer.valueOf(info2.uid));
                    if (!this.mPersistentServicesFileDidNotExist || !firstScan) {
                        notifyListener(info2.type, userId, false);
                    }
                } else if (previousUid.intValue() == info2.uid) {
                    user.services.put(info2.type, info2);
                } else if (inSystemImage(info2.uid) || !containsTypeAndUid(serviceInfos, info2.type, previousUid.intValue())) {
                    changed = true;
                    user.services.put(info2.type, info2);
                    user.persistentServices.put(info2.type, Integer.valueOf(info2.uid));
                    notifyListener(info2.type, userId, false);
                }
            }
            ArrayList<V> toBeRemoved = Lists.newArrayList();
            for (V v1 : user.persistentServices.keySet()) {
                if (!containsType(serviceInfos, v1)) {
                    toBeRemoved.add(v1);
                }
            }
            Iterator i$2 = toBeRemoved.iterator();
            while (i$2.hasNext()) {
                V v12 = i$2.next();
                changed = true;
                user.persistentServices.remove(v12);
                notifyListener(v12, userId, true);
            }
            if (changed) {
                writePersistentServicesLocked();
            }
        }
    }

    private boolean containsType(ArrayList<ServiceInfo<V>> serviceInfos, V type) {
        int N = serviceInfos.size();
        for (int i = 0; i < N; i++) {
            if (serviceInfos.get(i).type.equals(type)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsTypeAndUid(ArrayList<ServiceInfo<V>> serviceInfos, V type, int uid) {
        int N = serviceInfos.size();
        for (int i = 0; i < N; i++) {
            ServiceInfo<V> serviceInfo = serviceInfos.get(i);
            if (serviceInfo.type.equals(type) && serviceInfo.uid == uid) {
                return true;
            }
        }
        return false;
    }

    private ServiceInfo<V> parseServiceInfo(ResolveInfo service) throws XmlPullParserException, IOException {
        int type;
        ServiceInfo<V> serviceInfo;
        ServiceInfo si = service.serviceInfo;
        ComponentName componentName = new ComponentName(si.packageName, si.name);
        PackageManager pm = this.mContext.getPackageManager();
        XmlResourceParser parser = null;
        try {
            XmlResourceParser parser2 = si.loadXmlMetaData(pm, this.mMetaDataName);
            if (parser2 == null) {
                throw new XmlPullParserException("No " + this.mMetaDataName + " meta-data");
            }
            AttributeSet attrs = Xml.asAttributeSet(parser2);
            do {
                type = parser2.next();
                if (type == 1) {
                    break;
                }
            } while (type != 2);
            if (!this.mAttributesName.equals(parser2.getName())) {
                throw new XmlPullParserException("Meta-data does not start with " + this.mAttributesName + " tag");
            }
            V v = parseServiceAttributes(pm.getResourcesForApplication(si.applicationInfo), si.packageName, attrs);
            if (v == null) {
                serviceInfo = null;
                if (parser2 != null) {
                    parser2.close();
                }
            } else {
                serviceInfo = new ServiceInfo<>(v, componentName, service.serviceInfo.applicationInfo.uid);
                if (parser2 != null) {
                    parser2.close();
                }
            }
            return serviceInfo;
        } catch (PackageManager.NameNotFoundException e) {
            throw new XmlPullParserException("Unable to load resources for pacakge " + si.packageName);
        } catch (Throwable th) {
            if (0 != 0) {
                parser.close();
            }
            throw th;
        }
    }

    private void readPersistentServicesLocked() {
        this.mUserServices.clear();
        if (this.mSerializerAndParser != null) {
            FileInputStream fis = null;
            try {
                this.mPersistentServicesFileDidNotExist = !this.mPersistentServicesFile.getBaseFile().exists();
                if (!this.mPersistentServicesFileDidNotExist) {
                    FileInputStream fis2 = this.mPersistentServicesFile.openRead();
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(fis2, null);
                    int eventType = parser.getEventType();
                    while (eventType != 2 && eventType != 1) {
                        eventType = parser.next();
                    }
                    if ("services".equals(parser.getName())) {
                        int eventType2 = parser.next();
                        do {
                            if (eventType2 == 2 && parser.getDepth() == 2 && Notification.CATEGORY_SERVICE.equals(parser.getName())) {
                                V service = this.mSerializerAndParser.createFromXml(parser);
                                if (service == null) {
                                    break;
                                }
                                int uid = Integer.parseInt(parser.getAttributeValue(null, GrantCredentialsPermissionActivity.EXTRAS_REQUESTING_UID));
                                findOrCreateUserLocked(UserHandle.getUserId(uid)).persistentServices.put(service, Integer.valueOf(uid));
                            }
                            eventType2 = parser.next();
                        } while (eventType2 != 1);
                    }
                    if (fis2 != null) {
                        try {
                            fis2.close();
                        } catch (IOException e) {
                        }
                    }
                } else if (0 != 0) {
                    try {
                        fis.close();
                    } catch (IOException e2) {
                    }
                }
            } catch (Exception e3) {
                Log.w(TAG, "Error reading persistent services, starting from scratch", e3);
                if (0 != 0) {
                    try {
                        fis.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        fis.close();
                    } catch (IOException e5) {
                    }
                }
                throw th;
            }
        }
    }

    private void writePersistentServicesLocked() {
        if (this.mSerializerAndParser != null) {
            try {
                FileOutputStream fos = this.mPersistentServicesFile.startWrite();
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(fos, "utf-8");
                out.startDocument(null, true);
                out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                out.startTag(null, "services");
                for (int i = 0; i < this.mUserServices.size(); i++) {
                    for (Map.Entry<V, Integer> service : this.mUserServices.valueAt(i).persistentServices.entrySet()) {
                        out.startTag(null, Notification.CATEGORY_SERVICE);
                        out.attribute(null, GrantCredentialsPermissionActivity.EXTRAS_REQUESTING_UID, Integer.toString(service.getValue().intValue()));
                        this.mSerializerAndParser.writeAsXml(service.getKey(), out);
                        out.endTag(null, Notification.CATEGORY_SERVICE);
                    }
                }
                out.endTag(null, "services");
                out.endDocument();
                this.mPersistentServicesFile.finishWrite(fos);
            } catch (IOException e1) {
                Log.w(TAG, "Error writing accounts", e1);
                if (0 != 0) {
                    this.mPersistentServicesFile.failWrite(null);
                }
            }
        }
    }
}
