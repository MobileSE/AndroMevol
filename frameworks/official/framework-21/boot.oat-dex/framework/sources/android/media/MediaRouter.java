package android.media;

import android.Manifest;
import android.app.ActivityThread;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplay;
import android.hardware.display.WifiDisplayStatus;
import android.media.AudioAttributes;
import android.media.IAudioRoutesObserver;
import android.media.IAudioService;
import android.media.IMediaRouterClient;
import android.media.IMediaRouterService;
import android.media.IRemoteVolumeObserver;
import android.media.MediaRouterClientState;
import android.media.session.MediaSession;
import android.os.Handler;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class MediaRouter {
    public static final int AVAILABILITY_FLAG_IGNORE_DEFAULT_ROUTE = 1;
    public static final int CALLBACK_FLAG_PASSIVE_DISCOVERY = 8;
    public static final int CALLBACK_FLAG_PERFORM_ACTIVE_SCAN = 1;
    public static final int CALLBACK_FLAG_REQUEST_DISCOVERY = 4;
    public static final int CALLBACK_FLAG_UNFILTERED_EVENTS = 2;
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    static final int ROUTE_TYPE_ANY = 8388615;
    public static final int ROUTE_TYPE_LIVE_AUDIO = 1;
    public static final int ROUTE_TYPE_LIVE_VIDEO = 2;
    public static final int ROUTE_TYPE_REMOTE_DISPLAY = 4;
    public static final int ROUTE_TYPE_USER = 8388608;
    private static final String TAG = "MediaRouter";
    static final HashMap<Context, MediaRouter> sRouters = new HashMap<>();
    static Static sStatic;

    public static abstract class VolumeCallback {
        public abstract void onVolumeSetRequest(RouteInfo routeInfo, int i);

        public abstract void onVolumeUpdateRequest(RouteInfo routeInfo, int i);
    }

    /* access modifiers changed from: package-private */
    public static class Static implements DisplayManager.DisplayListener {
        boolean mActivelyScanningWifiDisplays;
        final Context mAppContext;
        final IAudioRoutesObserver.Stub mAudioRoutesObserver = new IAudioRoutesObserver.Stub() {
            /* class android.media.MediaRouter.Static.AnonymousClass1 */

            @Override // android.media.IAudioRoutesObserver
            public void dispatchAudioRoutesChanged(final AudioRoutesInfo newRoutes) {
                Static.this.mHandler.post(new Runnable() {
                    /* class android.media.MediaRouter.Static.AnonymousClass1.AnonymousClass1 */

                    public void run() {
                        Static.this.updateAudioRoutes(newRoutes);
                    }
                });
            }
        };
        final IAudioService mAudioService;
        RouteInfo mBluetoothA2dpRoute;
        final CopyOnWriteArrayList<CallbackInfo> mCallbacks = new CopyOnWriteArrayList<>();
        final boolean mCanConfigureWifiDisplays;
        final ArrayList<RouteCategory> mCategories = new ArrayList<>();
        IMediaRouterClient mClient;
        MediaRouterClientState mClientState;
        final AudioRoutesInfo mCurAudioRoutesInfo = new AudioRoutesInfo();
        int mCurrentUserId = -1;
        RouteInfo mDefaultAudioVideo;
        boolean mDiscoverRequestActiveScan;
        int mDiscoveryRequestRouteTypes;
        final DisplayManager mDisplayService;
        final Handler mHandler;
        final IMediaRouterService mMediaRouterService;
        String mPreviousActiveWifiDisplayAddress;
        final Resources mResources;
        final ArrayList<RouteInfo> mRoutes = new ArrayList<>();
        RouteInfo mSelectedRoute;
        final RouteCategory mSystemCategory;

        Static(Context appContext) {
            boolean z;
            this.mAppContext = appContext;
            this.mResources = Resources.getSystem();
            this.mHandler = new Handler(appContext.getMainLooper());
            this.mAudioService = IAudioService.Stub.asInterface(ServiceManager.getService(Context.AUDIO_SERVICE));
            this.mDisplayService = (DisplayManager) appContext.getSystemService(Context.DISPLAY_SERVICE);
            this.mMediaRouterService = IMediaRouterService.Stub.asInterface(ServiceManager.getService(Context.MEDIA_ROUTER_SERVICE));
            this.mSystemCategory = new RouteCategory(17040829, 3, false);
            this.mSystemCategory.mIsSystem = true;
            if (appContext.checkPermission(Manifest.permission.CONFIGURE_WIFI_DISPLAY, Process.myPid(), Process.myUid()) == 0) {
                z = true;
            } else {
                z = false;
            }
            this.mCanConfigureWifiDisplays = z;
        }

        /* access modifiers changed from: package-private */
        public void startMonitoringRoutes(Context appContext) {
            this.mDefaultAudioVideo = new RouteInfo(this.mSystemCategory);
            this.mDefaultAudioVideo.mNameResId = 17040825;
            this.mDefaultAudioVideo.mSupportedTypes = 3;
            this.mDefaultAudioVideo.updatePresentationDisplay();
            MediaRouter.addRouteStatic(this.mDefaultAudioVideo);
            MediaRouter.updateWifiDisplayStatus(this.mDisplayService.getWifiDisplayStatus());
            appContext.registerReceiver(new WifiDisplayStatusChangedReceiver(), new IntentFilter(DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED));
            appContext.registerReceiver(new VolumeChangeReceiver(), new IntentFilter(AudioManager.VOLUME_CHANGED_ACTION));
            this.mDisplayService.registerDisplayListener(this, this.mHandler);
            AudioRoutesInfo newAudioRoutes = null;
            try {
                newAudioRoutes = this.mAudioService.startWatchingRoutes(this.mAudioRoutesObserver);
            } catch (RemoteException e) {
            }
            if (newAudioRoutes != null) {
                updateAudioRoutes(newAudioRoutes);
            }
            rebindAsUser(UserHandle.myUserId());
            if (this.mSelectedRoute == null) {
                MediaRouter.selectDefaultRouteStatic();
            }
        }

        /* access modifiers changed from: package-private */
        public void updateAudioRoutes(AudioRoutesInfo newRoutes) {
            int name;
            if (newRoutes.mMainType != this.mCurAudioRoutesInfo.mMainType) {
                this.mCurAudioRoutesInfo.mMainType = newRoutes.mMainType;
                if ((newRoutes.mMainType & 2) != 0 || (newRoutes.mMainType & 1) != 0) {
                    name = 17040826;
                } else if ((newRoutes.mMainType & 4) != 0) {
                    name = 17040827;
                } else if ((newRoutes.mMainType & 8) != 0) {
                    name = 17040828;
                } else {
                    name = 17040825;
                }
                MediaRouter.sStatic.mDefaultAudioVideo.mNameResId = name;
                MediaRouter.dispatchRouteChanged(MediaRouter.sStatic.mDefaultAudioVideo);
            }
            int mainType = this.mCurAudioRoutesInfo.mMainType;
            if (!TextUtils.equals(newRoutes.mBluetoothName, this.mCurAudioRoutesInfo.mBluetoothName)) {
                this.mCurAudioRoutesInfo.mBluetoothName = newRoutes.mBluetoothName;
                if (this.mCurAudioRoutesInfo.mBluetoothName != null) {
                    if (MediaRouter.sStatic.mBluetoothA2dpRoute == null) {
                        RouteInfo info = new RouteInfo(MediaRouter.sStatic.mSystemCategory);
                        info.mName = this.mCurAudioRoutesInfo.mBluetoothName;
                        info.mDescription = MediaRouter.sStatic.mResources.getText(17040830);
                        info.mSupportedTypes = 1;
                        MediaRouter.sStatic.mBluetoothA2dpRoute = info;
                        MediaRouter.addRouteStatic(MediaRouter.sStatic.mBluetoothA2dpRoute);
                    } else {
                        MediaRouter.sStatic.mBluetoothA2dpRoute.mName = this.mCurAudioRoutesInfo.mBluetoothName;
                        MediaRouter.dispatchRouteChanged(MediaRouter.sStatic.mBluetoothA2dpRoute);
                    }
                } else if (MediaRouter.sStatic.mBluetoothA2dpRoute != null) {
                    MediaRouter.removeRouteStatic(MediaRouter.sStatic.mBluetoothA2dpRoute);
                    MediaRouter.sStatic.mBluetoothA2dpRoute = null;
                }
            }
            if (this.mBluetoothA2dpRoute != null) {
                boolean a2dpEnabled = isBluetoothA2dpOn();
                if (mainType != 0 && this.mSelectedRoute == this.mBluetoothA2dpRoute && !a2dpEnabled) {
                    MediaRouter.selectRouteStatic(1, this.mDefaultAudioVideo, false);
                } else if ((this.mSelectedRoute == this.mDefaultAudioVideo || this.mSelectedRoute == null) && a2dpEnabled) {
                    MediaRouter.selectRouteStatic(1, this.mBluetoothA2dpRoute, false);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isBluetoothA2dpOn() {
            try {
                return this.mAudioService.isBluetoothA2dpOn();
            } catch (RemoteException e) {
                Log.e(MediaRouter.TAG, "Error querying Bluetooth A2DP state", e);
                return false;
            }
        }

        /* access modifiers changed from: package-private */
        public void updateDiscoveryRequest() {
            int routeTypes = 0;
            int passiveRouteTypes = 0;
            boolean activeScan = false;
            boolean activeScanWifiDisplay = false;
            int count = this.mCallbacks.size();
            for (int i = 0; i < count; i++) {
                CallbackInfo cbi = this.mCallbacks.get(i);
                if ((cbi.flags & 5) != 0) {
                    routeTypes |= cbi.type;
                } else if ((cbi.flags & 8) != 0) {
                    passiveRouteTypes |= cbi.type;
                } else {
                    routeTypes |= cbi.type;
                }
                if ((cbi.flags & 1) != 0) {
                    activeScan = true;
                    if ((cbi.type & 4) != 0) {
                        activeScanWifiDisplay = true;
                    }
                }
            }
            if (routeTypes != 0 || activeScan) {
                routeTypes |= passiveRouteTypes;
            }
            if (this.mCanConfigureWifiDisplays) {
                if (this.mSelectedRoute != null && this.mSelectedRoute.matchesTypes(4)) {
                    activeScanWifiDisplay = false;
                }
                if (activeScanWifiDisplay) {
                    if (!this.mActivelyScanningWifiDisplays) {
                        this.mActivelyScanningWifiDisplays = true;
                        this.mDisplayService.startWifiDisplayScan();
                    }
                } else if (this.mActivelyScanningWifiDisplays) {
                    this.mActivelyScanningWifiDisplays = false;
                    this.mDisplayService.stopWifiDisplayScan();
                }
            }
            if (!(routeTypes == this.mDiscoveryRequestRouteTypes && activeScan == this.mDiscoverRequestActiveScan)) {
                this.mDiscoveryRequestRouteTypes = routeTypes;
                this.mDiscoverRequestActiveScan = activeScan;
                publishClientDiscoveryRequest();
            }
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayAdded(int displayId) {
            updatePresentationDisplays(displayId);
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayChanged(int displayId) {
            updatePresentationDisplays(displayId);
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayRemoved(int displayId) {
            updatePresentationDisplays(displayId);
        }

        public Display[] getAllPresentationDisplays() {
            return this.mDisplayService.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        }

        private void updatePresentationDisplays(int changedDisplayId) {
            int count = this.mRoutes.size();
            for (int i = 0; i < count; i++) {
                RouteInfo route = this.mRoutes.get(i);
                if (route.updatePresentationDisplay() || (route.mPresentationDisplay != null && route.mPresentationDisplay.getDisplayId() == changedDisplayId)) {
                    MediaRouter.dispatchRoutePresentationDisplayChanged(route);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void setSelectedRoute(RouteInfo info, boolean explicit) {
            this.mSelectedRoute = info;
            publishClientSelectedRoute(explicit);
        }

        /* access modifiers changed from: package-private */
        public void rebindAsUser(int userId) {
            if (this.mCurrentUserId != userId || userId < 0 || this.mClient == null) {
                if (this.mClient != null) {
                    try {
                        this.mMediaRouterService.unregisterClient(this.mClient);
                    } catch (RemoteException ex) {
                        Log.e(MediaRouter.TAG, "Unable to unregister media router client.", ex);
                    }
                    this.mClient = null;
                }
                this.mCurrentUserId = userId;
                try {
                    Client client = new Client();
                    this.mMediaRouterService.registerClientAsUser(client, this.mAppContext.getPackageName(), userId);
                    this.mClient = client;
                } catch (RemoteException ex2) {
                    Log.e(MediaRouter.TAG, "Unable to register media router client.", ex2);
                }
                publishClientDiscoveryRequest();
                publishClientSelectedRoute(false);
                updateClientState();
            }
        }

        /* access modifiers changed from: package-private */
        public void publishClientDiscoveryRequest() {
            if (this.mClient != null) {
                try {
                    this.mMediaRouterService.setDiscoveryRequest(this.mClient, this.mDiscoveryRequestRouteTypes, this.mDiscoverRequestActiveScan);
                } catch (RemoteException ex) {
                    Log.e(MediaRouter.TAG, "Unable to publish media router client discovery request.", ex);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void publishClientSelectedRoute(boolean explicit) {
            if (this.mClient != null) {
                try {
                    this.mMediaRouterService.setSelectedRoute(this.mClient, this.mSelectedRoute != null ? this.mSelectedRoute.mGlobalRouteId : null, explicit);
                } catch (RemoteException ex) {
                    Log.e(MediaRouter.TAG, "Unable to publish media router client selected route.", ex);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void updateClientState() {
            this.mClientState = null;
            if (this.mClient != null) {
                try {
                    this.mClientState = this.mMediaRouterService.getState(this.mClient);
                } catch (RemoteException ex) {
                    Log.e(MediaRouter.TAG, "Unable to retrieve media router client state.", ex);
                }
            }
            ArrayList<MediaRouterClientState.RouteInfo> globalRoutes = this.mClientState != null ? this.mClientState.routes : null;
            String globallySelectedRouteId = this.mClientState != null ? this.mClientState.globallySelectedRouteId : null;
            int globalRouteCount = globalRoutes != null ? globalRoutes.size() : 0;
            for (int i = 0; i < globalRouteCount; i++) {
                MediaRouterClientState.RouteInfo globalRoute = globalRoutes.get(i);
                RouteInfo route = findGlobalRoute(globalRoute.id);
                if (route == null) {
                    MediaRouter.addRouteStatic(makeGlobalRoute(globalRoute));
                } else {
                    updateGlobalRoute(route, globalRoute);
                }
            }
            if (globallySelectedRouteId != null) {
                RouteInfo route2 = findGlobalRoute(globallySelectedRouteId);
                if (route2 == null) {
                    Log.w(MediaRouter.TAG, "Could not find new globally selected route: " + globallySelectedRouteId);
                } else if (route2 != this.mSelectedRoute) {
                    if (MediaRouter.DEBUG) {
                        Log.d(MediaRouter.TAG, "Selecting new globally selected route: " + route2);
                    }
                    MediaRouter.selectRouteStatic(route2.mSupportedTypes, route2, false);
                }
            } else if (!(this.mSelectedRoute == null || this.mSelectedRoute.mGlobalRouteId == null)) {
                if (MediaRouter.DEBUG) {
                    Log.d(MediaRouter.TAG, "Unselecting previous globally selected route: " + this.mSelectedRoute);
                }
                MediaRouter.selectDefaultRouteStatic();
            }
            int i2 = this.mRoutes.size();
            while (true) {
                i2--;
                if (i2 > 0) {
                    RouteInfo route3 = this.mRoutes.get(i2);
                    String globalRouteId = route3.mGlobalRouteId;
                    if (globalRouteId != null) {
                        for (int j = 0; j < globalRouteCount; j++) {
                            if (globalRouteId.equals(globalRoutes.get(j).id)) {
                                break;
                            }
                        }
                        MediaRouter.removeRouteStatic(route3);
                    }
                } else {
                    return;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void requestSetVolume(RouteInfo route, int volume) {
            if (route.mGlobalRouteId != null && this.mClient != null) {
                try {
                    this.mMediaRouterService.requestSetVolume(this.mClient, route.mGlobalRouteId, volume);
                } catch (RemoteException ex) {
                    Log.w(MediaRouter.TAG, "Unable to request volume change.", ex);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void requestUpdateVolume(RouteInfo route, int direction) {
            if (route.mGlobalRouteId != null && this.mClient != null) {
                try {
                    this.mMediaRouterService.requestUpdateVolume(this.mClient, route.mGlobalRouteId, direction);
                } catch (RemoteException ex) {
                    Log.w(MediaRouter.TAG, "Unable to request volume change.", ex);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public RouteInfo makeGlobalRoute(MediaRouterClientState.RouteInfo globalRoute) {
            RouteInfo route = new RouteInfo(MediaRouter.sStatic.mSystemCategory);
            route.mGlobalRouteId = globalRoute.id;
            route.mName = globalRoute.name;
            route.mDescription = globalRoute.description;
            route.mSupportedTypes = globalRoute.supportedTypes;
            route.mEnabled = globalRoute.enabled;
            route.setRealStatusCode(globalRoute.statusCode);
            route.mPlaybackType = globalRoute.playbackType;
            route.mPlaybackStream = globalRoute.playbackStream;
            route.mVolume = globalRoute.volume;
            route.mVolumeMax = globalRoute.volumeMax;
            route.mVolumeHandling = globalRoute.volumeHandling;
            route.mPresentationDisplayId = globalRoute.presentationDisplayId;
            route.updatePresentationDisplay();
            return route;
        }

        /* access modifiers changed from: package-private */
        public void updateGlobalRoute(RouteInfo route, MediaRouterClientState.RouteInfo globalRoute) {
            boolean changed = false;
            boolean volumeChanged = false;
            boolean presentationDisplayChanged = false;
            if (!Objects.equals(route.mName, globalRoute.name)) {
                route.mName = globalRoute.name;
                changed = true;
            }
            if (!Objects.equals(route.mDescription, globalRoute.description)) {
                route.mDescription = globalRoute.description;
                changed = true;
            }
            int oldSupportedTypes = route.mSupportedTypes;
            if (oldSupportedTypes != globalRoute.supportedTypes) {
                route.mSupportedTypes = globalRoute.supportedTypes;
                changed = true;
            }
            if (route.mEnabled != globalRoute.enabled) {
                route.mEnabled = globalRoute.enabled;
                changed = true;
            }
            if (route.mRealStatusCode != globalRoute.statusCode) {
                route.setRealStatusCode(globalRoute.statusCode);
                changed = true;
            }
            if (route.mPlaybackType != globalRoute.playbackType) {
                route.mPlaybackType = globalRoute.playbackType;
                changed = true;
            }
            if (route.mPlaybackStream != globalRoute.playbackStream) {
                route.mPlaybackStream = globalRoute.playbackStream;
                changed = true;
            }
            if (route.mVolume != globalRoute.volume) {
                route.mVolume = globalRoute.volume;
                changed = true;
                volumeChanged = true;
            }
            if (route.mVolumeMax != globalRoute.volumeMax) {
                route.mVolumeMax = globalRoute.volumeMax;
                changed = true;
                volumeChanged = true;
            }
            if (route.mVolumeHandling != globalRoute.volumeHandling) {
                route.mVolumeHandling = globalRoute.volumeHandling;
                changed = true;
                volumeChanged = true;
            }
            if (route.mPresentationDisplayId != globalRoute.presentationDisplayId) {
                route.mPresentationDisplayId = globalRoute.presentationDisplayId;
                route.updatePresentationDisplay();
                changed = true;
                presentationDisplayChanged = true;
            }
            if (changed) {
                MediaRouter.dispatchRouteChanged(route, oldSupportedTypes);
            }
            if (volumeChanged) {
                MediaRouter.dispatchRouteVolumeChanged(route);
            }
            if (presentationDisplayChanged) {
                MediaRouter.dispatchRoutePresentationDisplayChanged(route);
            }
        }

        /* access modifiers changed from: package-private */
        public RouteInfo findGlobalRoute(String globalRouteId) {
            int count = this.mRoutes.size();
            for (int i = 0; i < count; i++) {
                RouteInfo route = this.mRoutes.get(i);
                if (globalRouteId.equals(route.mGlobalRouteId)) {
                    return route;
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public final class Client extends IMediaRouterClient.Stub {
            Client() {
            }

            @Override // android.media.IMediaRouterClient
            public void onStateChanged() {
                Static.this.mHandler.post(new Runnable() {
                    /* class android.media.MediaRouter.Static.Client.AnonymousClass1 */

                    public void run() {
                        if (Client.this == Static.this.mClient) {
                            Static.this.updateClientState();
                        }
                    }
                });
            }
        }
    }

    static String typesToString(int types) {
        StringBuilder result = new StringBuilder();
        if ((types & 1) != 0) {
            result.append("ROUTE_TYPE_LIVE_AUDIO ");
        }
        if ((types & 2) != 0) {
            result.append("ROUTE_TYPE_LIVE_VIDEO ");
        }
        if ((types & 4) != 0) {
            result.append("ROUTE_TYPE_REMOTE_DISPLAY ");
        }
        if ((8388608 & types) != 0) {
            result.append("ROUTE_TYPE_USER ");
        }
        return result.toString();
    }

    public MediaRouter(Context context) {
        synchronized (Static.class) {
            if (sStatic == null) {
                Context appContext = context.getApplicationContext();
                sStatic = new Static(appContext);
                sStatic.startMonitoringRoutes(appContext);
            }
        }
    }

    public RouteInfo getDefaultRoute() {
        return sStatic.mDefaultAudioVideo;
    }

    public RouteCategory getSystemCategory() {
        return sStatic.mSystemCategory;
    }

    public RouteInfo getSelectedRoute() {
        return getSelectedRoute(8388615);
    }

    public RouteInfo getSelectedRoute(int type) {
        if (sStatic.mSelectedRoute != null && (sStatic.mSelectedRoute.mSupportedTypes & type) != 0) {
            return sStatic.mSelectedRoute;
        }
        if (type == 8388608) {
            return null;
        }
        return sStatic.mDefaultAudioVideo;
    }

    public boolean isRouteAvailable(int types, int flags) {
        int count = sStatic.mRoutes.size();
        for (int i = 0; i < count; i++) {
            RouteInfo route = sStatic.mRoutes.get(i);
            if (route.matchesTypes(types) && ((flags & 1) == 0 || route != sStatic.mDefaultAudioVideo)) {
                return true;
            }
        }
        return false;
    }

    public void addCallback(int types, Callback cb) {
        addCallback(types, cb, 0);
    }

    public void addCallback(int types, Callback cb, int flags) {
        int index = findCallbackInfo(cb);
        if (index >= 0) {
            CallbackInfo info = sStatic.mCallbacks.get(index);
            info.type |= types;
            info.flags |= flags;
        } else {
            sStatic.mCallbacks.add(new CallbackInfo(cb, types, flags, this));
        }
        sStatic.updateDiscoveryRequest();
    }

    public void removeCallback(Callback cb) {
        int index = findCallbackInfo(cb);
        if (index >= 0) {
            sStatic.mCallbacks.remove(index);
            sStatic.updateDiscoveryRequest();
            return;
        }
        Log.w(TAG, "removeCallback(" + cb + "): callback not registered");
    }

    private int findCallbackInfo(Callback cb) {
        int count = sStatic.mCallbacks.size();
        for (int i = 0; i < count; i++) {
            if (sStatic.mCallbacks.get(i).cb == cb) {
                return i;
            }
        }
        return -1;
    }

    public void selectRoute(int types, RouteInfo route) {
        selectRouteStatic(types, route, true);
    }

    public void selectRouteInt(int types, RouteInfo route, boolean explicit) {
        selectRouteStatic(types, route, explicit);
    }

    static void selectRouteStatic(int types, RouteInfo route, boolean explicit) {
        boolean oldRouteHasAddress;
        boolean z;
        boolean newRouteHasAddress = true;
        RouteInfo oldRoute = sStatic.mSelectedRoute;
        if (oldRoute != route) {
            if (!route.matchesTypes(types)) {
                Log.w(TAG, "selectRoute ignored; cannot select route with supported types " + typesToString(route.getSupportedTypes()) + " into route types " + typesToString(types));
                return;
            }
            RouteInfo btRoute = sStatic.mBluetoothA2dpRoute;
            if (!(btRoute == null || (types & 1) == 0 || (route != btRoute && route != sStatic.mDefaultAudioVideo))) {
                try {
                    IAudioService iAudioService = sStatic.mAudioService;
                    if (route == btRoute) {
                        z = true;
                    } else {
                        z = false;
                    }
                    iAudioService.setBluetoothA2dpOn(z);
                } catch (RemoteException e) {
                    Log.e(TAG, "Error changing Bluetooth A2DP state", e);
                }
            }
            WifiDisplay activeDisplay = sStatic.mDisplayService.getWifiDisplayStatus().getActiveDisplay();
            if (oldRoute == null || oldRoute.mDeviceAddress == null) {
                oldRouteHasAddress = false;
            } else {
                oldRouteHasAddress = true;
            }
            if (route == null || route.mDeviceAddress == null) {
                newRouteHasAddress = false;
            }
            if (activeDisplay != null || oldRouteHasAddress || newRouteHasAddress) {
                if (!newRouteHasAddress || matchesDeviceAddress(activeDisplay, route)) {
                    if (activeDisplay != null && !newRouteHasAddress) {
                        sStatic.mDisplayService.disconnectWifiDisplay();
                    }
                } else if (sStatic.mCanConfigureWifiDisplays) {
                    sStatic.mDisplayService.connectWifiDisplay(route.mDeviceAddress);
                } else {
                    Log.e(TAG, "Cannot connect to wifi displays because this process is not allowed to do so.");
                }
            }
            sStatic.setSelectedRoute(route, explicit);
            if (oldRoute != null) {
                dispatchRouteUnselected(oldRoute.getSupportedTypes() & types, oldRoute);
                if (oldRoute.resolveStatusCode()) {
                    dispatchRouteChanged(oldRoute);
                }
            }
            if (route != null) {
                if (route.resolveStatusCode()) {
                    dispatchRouteChanged(route);
                }
                dispatchRouteSelected(route.getSupportedTypes() & types, route);
            }
            sStatic.updateDiscoveryRequest();
        }
    }

    static void selectDefaultRouteStatic() {
        if (sStatic.mSelectedRoute == sStatic.mBluetoothA2dpRoute || sStatic.mBluetoothA2dpRoute == null || !sStatic.isBluetoothA2dpOn()) {
            selectRouteStatic(8388615, sStatic.mDefaultAudioVideo, false);
        } else {
            selectRouteStatic(8388615, sStatic.mBluetoothA2dpRoute, false);
        }
    }

    static boolean matchesDeviceAddress(WifiDisplay display, RouteInfo info) {
        boolean routeHasAddress;
        if (info == null || info.mDeviceAddress == null) {
            routeHasAddress = false;
        } else {
            routeHasAddress = true;
        }
        if (display == null && !routeHasAddress) {
            return true;
        }
        if (display == null || !routeHasAddress) {
            return false;
        }
        return display.getDeviceAddress().equals(info.mDeviceAddress);
    }

    public void addUserRoute(UserRouteInfo info) {
        addRouteStatic(info);
    }

    public void addRouteInt(RouteInfo info) {
        addRouteStatic(info);
    }

    static void addRouteStatic(RouteInfo info) {
        RouteCategory cat = info.getCategory();
        if (!sStatic.mCategories.contains(cat)) {
            sStatic.mCategories.add(cat);
        }
        if (!cat.isGroupable() || (info instanceof RouteGroup)) {
            sStatic.mRoutes.add(info);
            dispatchRouteAdded(info);
            return;
        }
        RouteGroup group = new RouteGroup(info.getCategory());
        group.mSupportedTypes = info.mSupportedTypes;
        sStatic.mRoutes.add(group);
        dispatchRouteAdded(group);
        group.addRoute(info);
    }

    public void removeUserRoute(UserRouteInfo info) {
        removeRouteStatic(info);
    }

    public void clearUserRoutes() {
        int i = 0;
        while (i < sStatic.mRoutes.size()) {
            RouteInfo info = sStatic.mRoutes.get(i);
            if ((info instanceof UserRouteInfo) || (info instanceof RouteGroup)) {
                removeRouteStatic(info);
                i--;
            }
            i++;
        }
    }

    public void removeRouteInt(RouteInfo info) {
        removeRouteStatic(info);
    }

    static void removeRouteStatic(RouteInfo info) {
        if (sStatic.mRoutes.remove(info)) {
            RouteCategory removingCat = info.getCategory();
            int count = sStatic.mRoutes.size();
            boolean found = false;
            int i = 0;
            while (true) {
                if (i >= count) {
                    break;
                } else if (removingCat == sStatic.mRoutes.get(i).getCategory()) {
                    found = true;
                    break;
                } else {
                    i++;
                }
            }
            if (info.isSelected()) {
                selectDefaultRouteStatic();
            }
            if (!found) {
                sStatic.mCategories.remove(removingCat);
            }
            dispatchRouteRemoved(info);
        }
    }

    public int getCategoryCount() {
        return sStatic.mCategories.size();
    }

    public RouteCategory getCategoryAt(int index) {
        return sStatic.mCategories.get(index);
    }

    public int getRouteCount() {
        return sStatic.mRoutes.size();
    }

    public RouteInfo getRouteAt(int index) {
        return sStatic.mRoutes.get(index);
    }

    static int getRouteCountStatic() {
        return sStatic.mRoutes.size();
    }

    static RouteInfo getRouteAtStatic(int index) {
        return sStatic.mRoutes.get(index);
    }

    public UserRouteInfo createUserRoute(RouteCategory category) {
        return new UserRouteInfo(category);
    }

    public RouteCategory createRouteCategory(CharSequence name, boolean isGroupable) {
        return new RouteCategory(name, 8388608, isGroupable);
    }

    public RouteCategory createRouteCategory(int nameResId, boolean isGroupable) {
        return new RouteCategory(nameResId, 8388608, isGroupable);
    }

    public void rebindAsUser(int userId) {
        sStatic.rebindAsUser(userId);
    }

    static void updateRoute(RouteInfo info) {
        dispatchRouteChanged(info);
    }

    static void dispatchRouteSelected(int type, RouteInfo info) {
        Iterator i$ = sStatic.mCallbacks.iterator();
        while (i$.hasNext()) {
            CallbackInfo cbi = i$.next();
            if (cbi.filterRouteEvent(info)) {
                cbi.cb.onRouteSelected(cbi.router, type, info);
            }
        }
    }

    static void dispatchRouteUnselected(int type, RouteInfo info) {
        Iterator i$ = sStatic.mCallbacks.iterator();
        while (i$.hasNext()) {
            CallbackInfo cbi = i$.next();
            if (cbi.filterRouteEvent(info)) {
                cbi.cb.onRouteUnselected(cbi.router, type, info);
            }
        }
    }

    static void dispatchRouteChanged(RouteInfo info) {
        dispatchRouteChanged(info, info.mSupportedTypes);
    }

    static void dispatchRouteChanged(RouteInfo info, int oldSupportedTypes) {
        int newSupportedTypes = info.mSupportedTypes;
        Iterator i$ = sStatic.mCallbacks.iterator();
        while (i$.hasNext()) {
            CallbackInfo cbi = i$.next();
            boolean oldVisibility = cbi.filterRouteEvent(oldSupportedTypes);
            boolean newVisibility = cbi.filterRouteEvent(newSupportedTypes);
            if (!oldVisibility && newVisibility) {
                cbi.cb.onRouteAdded(cbi.router, info);
                if (info.isSelected()) {
                    cbi.cb.onRouteSelected(cbi.router, newSupportedTypes, info);
                }
            }
            if (oldVisibility || newVisibility) {
                cbi.cb.onRouteChanged(cbi.router, info);
            }
            if (oldVisibility && !newVisibility) {
                if (info.isSelected()) {
                    cbi.cb.onRouteUnselected(cbi.router, oldSupportedTypes, info);
                }
                cbi.cb.onRouteRemoved(cbi.router, info);
            }
        }
    }

    static void dispatchRouteAdded(RouteInfo info) {
        Iterator i$ = sStatic.mCallbacks.iterator();
        while (i$.hasNext()) {
            CallbackInfo cbi = i$.next();
            if (cbi.filterRouteEvent(info)) {
                cbi.cb.onRouteAdded(cbi.router, info);
            }
        }
    }

    static void dispatchRouteRemoved(RouteInfo info) {
        Iterator i$ = sStatic.mCallbacks.iterator();
        while (i$.hasNext()) {
            CallbackInfo cbi = i$.next();
            if (cbi.filterRouteEvent(info)) {
                cbi.cb.onRouteRemoved(cbi.router, info);
            }
        }
    }

    static void dispatchRouteGrouped(RouteInfo info, RouteGroup group, int index) {
        Iterator i$ = sStatic.mCallbacks.iterator();
        while (i$.hasNext()) {
            CallbackInfo cbi = i$.next();
            if (cbi.filterRouteEvent(group)) {
                cbi.cb.onRouteGrouped(cbi.router, info, group, index);
            }
        }
    }

    static void dispatchRouteUngrouped(RouteInfo info, RouteGroup group) {
        Iterator i$ = sStatic.mCallbacks.iterator();
        while (i$.hasNext()) {
            CallbackInfo cbi = i$.next();
            if (cbi.filterRouteEvent(group)) {
                cbi.cb.onRouteUngrouped(cbi.router, info, group);
            }
        }
    }

    static void dispatchRouteVolumeChanged(RouteInfo info) {
        Iterator i$ = sStatic.mCallbacks.iterator();
        while (i$.hasNext()) {
            CallbackInfo cbi = i$.next();
            if (cbi.filterRouteEvent(info)) {
                cbi.cb.onRouteVolumeChanged(cbi.router, info);
            }
        }
    }

    static void dispatchRoutePresentationDisplayChanged(RouteInfo info) {
        Iterator i$ = sStatic.mCallbacks.iterator();
        while (i$.hasNext()) {
            CallbackInfo cbi = i$.next();
            if (cbi.filterRouteEvent(info)) {
                cbi.cb.onRoutePresentationDisplayChanged(cbi.router, info);
            }
        }
    }

    static void systemVolumeChanged(int newValue) {
        RouteInfo selectedRoute = sStatic.mSelectedRoute;
        if (selectedRoute != null) {
            if (selectedRoute == sStatic.mBluetoothA2dpRoute || selectedRoute == sStatic.mDefaultAudioVideo) {
                dispatchRouteVolumeChanged(selectedRoute);
            } else if (sStatic.mBluetoothA2dpRoute != null) {
                try {
                    dispatchRouteVolumeChanged(sStatic.mAudioService.isBluetoothA2dpOn() ? sStatic.mBluetoothA2dpRoute : sStatic.mDefaultAudioVideo);
                } catch (RemoteException e) {
                    Log.e(TAG, "Error checking Bluetooth A2DP state to report volume change", e);
                }
            } else {
                dispatchRouteVolumeChanged(sStatic.mDefaultAudioVideo);
            }
        }
    }

    static void updateWifiDisplayStatus(WifiDisplayStatus status) {
        WifiDisplay[] displays;
        WifiDisplay activeDisplay;
        WifiDisplay d;
        if (status.getFeatureState() == 3) {
            displays = status.getDisplays();
            activeDisplay = status.getActiveDisplay();
            if (!sStatic.mCanConfigureWifiDisplays) {
                displays = activeDisplay != null ? new WifiDisplay[]{activeDisplay} : WifiDisplay.EMPTY_ARRAY;
            }
        } else {
            displays = WifiDisplay.EMPTY_ARRAY;
            activeDisplay = null;
        }
        String activeDisplayAddress = activeDisplay != null ? activeDisplay.getDeviceAddress() : null;
        for (WifiDisplay d2 : displays) {
            if (shouldShowWifiDisplay(d2, activeDisplay)) {
                RouteInfo route = findWifiDisplayRoute(d2);
                if (route == null) {
                    route = makeWifiDisplayRoute(d2, status);
                    addRouteStatic(route);
                } else {
                    String address = d2.getDeviceAddress();
                    updateWifiDisplayRoute(route, d2, status, !address.equals(activeDisplayAddress) && address.equals(sStatic.mPreviousActiveWifiDisplayAddress));
                }
                if (d2.equals(activeDisplay)) {
                    selectRouteStatic(route.getSupportedTypes(), route, false);
                }
            }
        }
        int i = sStatic.mRoutes.size();
        while (true) {
            i--;
            if (i > 0) {
                RouteInfo route2 = sStatic.mRoutes.get(i);
                if (route2.mDeviceAddress != null && ((d = findWifiDisplay(displays, route2.mDeviceAddress)) == null || !shouldShowWifiDisplay(d, activeDisplay))) {
                    removeRouteStatic(route2);
                }
            } else {
                sStatic.mPreviousActiveWifiDisplayAddress = activeDisplayAddress;
                return;
            }
        }
    }

    private static boolean shouldShowWifiDisplay(WifiDisplay d, WifiDisplay activeDisplay) {
        return d.isRemembered() || d.equals(activeDisplay);
    }

    static int getWifiDisplayStatusCode(WifiDisplay d, WifiDisplayStatus wfdStatus) {
        int newStatus;
        if (wfdStatus.getScanState() == 1) {
            newStatus = 1;
        } else if (d.isAvailable()) {
            newStatus = d.canConnect() ? 3 : 5;
        } else {
            newStatus = 4;
        }
        if (!d.equals(wfdStatus.getActiveDisplay())) {
            return newStatus;
        }
        switch (wfdStatus.getActiveDisplayState()) {
            case 0:
                Log.e(TAG, "Active display is not connected!");
                return newStatus;
            case 1:
                return 2;
            case 2:
                return 6;
            default:
                return newStatus;
        }
    }

    static boolean isWifiDisplayEnabled(WifiDisplay d, WifiDisplayStatus wfdStatus) {
        return d.isAvailable() && (d.canConnect() || d.equals(wfdStatus.getActiveDisplay()));
    }

    static RouteInfo makeWifiDisplayRoute(WifiDisplay display, WifiDisplayStatus wfdStatus) {
        RouteInfo newRoute = new RouteInfo(sStatic.mSystemCategory);
        newRoute.mDeviceAddress = display.getDeviceAddress();
        newRoute.mSupportedTypes = 7;
        newRoute.mVolumeHandling = 0;
        newRoute.mPlaybackType = 1;
        newRoute.setRealStatusCode(getWifiDisplayStatusCode(display, wfdStatus));
        newRoute.mEnabled = isWifiDisplayEnabled(display, wfdStatus);
        newRoute.mName = display.getFriendlyDisplayName();
        newRoute.mDescription = sStatic.mResources.getText(17040831);
        newRoute.updatePresentationDisplay();
        return newRoute;
    }

    private static void updateWifiDisplayRoute(RouteInfo route, WifiDisplay display, WifiDisplayStatus wfdStatus, boolean disconnected) {
        boolean changed = false;
        String newName = display.getFriendlyDisplayName();
        if (!route.getName().equals(newName)) {
            route.mName = newName;
            changed = true;
        }
        boolean enabled = isWifiDisplayEnabled(display, wfdStatus);
        boolean z = route.mEnabled != enabled;
        route.mEnabled = enabled;
        if ((changed | z) || route.setRealStatusCode(getWifiDisplayStatusCode(display, wfdStatus))) {
            dispatchRouteChanged(route);
        }
        if ((!enabled || disconnected) && route.isSelected()) {
            selectDefaultRouteStatic();
        }
    }

    private static WifiDisplay findWifiDisplay(WifiDisplay[] displays, String deviceAddress) {
        for (WifiDisplay d : displays) {
            if (d.getDeviceAddress().equals(deviceAddress)) {
                return d;
            }
        }
        return null;
    }

    private static RouteInfo findWifiDisplayRoute(WifiDisplay d) {
        int count = sStatic.mRoutes.size();
        for (int i = 0; i < count; i++) {
            RouteInfo info = sStatic.mRoutes.get(i);
            if (d.getDeviceAddress().equals(info.mDeviceAddress)) {
                return info;
            }
        }
        return null;
    }

    public static class RouteInfo {
        public static final int PLAYBACK_TYPE_LOCAL = 0;
        public static final int PLAYBACK_TYPE_REMOTE = 1;
        public static final int PLAYBACK_VOLUME_FIXED = 0;
        public static final int PLAYBACK_VOLUME_VARIABLE = 1;
        public static final int STATUS_AVAILABLE = 3;
        public static final int STATUS_CONNECTED = 6;
        public static final int STATUS_CONNECTING = 2;
        public static final int STATUS_IN_USE = 5;
        public static final int STATUS_NONE = 0;
        public static final int STATUS_NOT_AVAILABLE = 4;
        public static final int STATUS_SCANNING = 1;
        final RouteCategory mCategory;
        CharSequence mDescription;
        String mDeviceAddress;
        boolean mEnabled = true;
        String mGlobalRouteId;
        RouteGroup mGroup;
        Drawable mIcon;
        CharSequence mName;
        int mNameResId;
        int mPlaybackStream = 3;
        int mPlaybackType = 0;
        Display mPresentationDisplay;
        int mPresentationDisplayId = -1;
        private int mRealStatusCode;
        final IRemoteVolumeObserver.Stub mRemoteVolObserver = new IRemoteVolumeObserver.Stub() {
            /* class android.media.MediaRouter.RouteInfo.AnonymousClass1 */

            @Override // android.media.IRemoteVolumeObserver
            public void dispatchRemoteVolumeUpdate(final int direction, final int value) {
                MediaRouter.sStatic.mHandler.post(new Runnable() {
                    /* class android.media.MediaRouter.RouteInfo.AnonymousClass1.AnonymousClass1 */

                    public void run() {
                        if (RouteInfo.this.mVcb == null) {
                            return;
                        }
                        if (direction != 0) {
                            RouteInfo.this.mVcb.vcb.onVolumeUpdateRequest(RouteInfo.this.mVcb.route, direction);
                        } else {
                            RouteInfo.this.mVcb.vcb.onVolumeSetRequest(RouteInfo.this.mVcb.route, value);
                        }
                    }
                });
            }
        };
        private int mResolvedStatusCode;
        private CharSequence mStatus;
        int mSupportedTypes;
        private Object mTag;
        VolumeCallbackInfo mVcb;
        int mVolume = 15;
        int mVolumeHandling = 1;
        int mVolumeMax = 15;

        RouteInfo(RouteCategory category) {
            this.mCategory = category;
        }

        public CharSequence getName() {
            return getName(MediaRouter.sStatic.mResources);
        }

        public CharSequence getName(Context context) {
            return getName(context.getResources());
        }

        /* access modifiers changed from: package-private */
        public CharSequence getName(Resources res) {
            if (this.mNameResId == 0) {
                return this.mName;
            }
            CharSequence text = res.getText(this.mNameResId);
            this.mName = text;
            return text;
        }

        public CharSequence getDescription() {
            return this.mDescription;
        }

        public CharSequence getStatus() {
            return this.mStatus;
        }

        /* access modifiers changed from: package-private */
        public boolean setRealStatusCode(int statusCode) {
            if (this.mRealStatusCode == statusCode) {
                return false;
            }
            this.mRealStatusCode = statusCode;
            return resolveStatusCode();
        }

        /* access modifiers changed from: package-private */
        public boolean resolveStatusCode() {
            int resId;
            int statusCode = this.mRealStatusCode;
            if (isSelected()) {
                switch (statusCode) {
                    case 1:
                    case 3:
                        statusCode = 2;
                        break;
                }
            }
            if (this.mResolvedStatusCode == statusCode) {
                return false;
            }
            this.mResolvedStatusCode = statusCode;
            switch (statusCode) {
                case 1:
                    resId = 17040838;
                    break;
                case 2:
                    resId = 17040839;
                    break;
                case 3:
                    resId = 17040840;
                    break;
                case 4:
                    resId = 17040841;
                    break;
                case 5:
                    resId = 17040842;
                    break;
                default:
                    resId = 0;
                    break;
            }
            this.mStatus = resId != 0 ? MediaRouter.sStatic.mResources.getText(resId) : null;
            return true;
        }

        public int getStatusCode() {
            return this.mResolvedStatusCode;
        }

        public int getSupportedTypes() {
            return this.mSupportedTypes;
        }

        public boolean matchesTypes(int types) {
            return (this.mSupportedTypes & types) != 0;
        }

        public RouteGroup getGroup() {
            return this.mGroup;
        }

        public RouteCategory getCategory() {
            return this.mCategory;
        }

        public Drawable getIconDrawable() {
            return this.mIcon;
        }

        public void setTag(Object tag) {
            this.mTag = tag;
            routeUpdated();
        }

        public Object getTag() {
            return this.mTag;
        }

        public int getPlaybackType() {
            return this.mPlaybackType;
        }

        public int getPlaybackStream() {
            return this.mPlaybackStream;
        }

        public int getVolume() {
            if (this.mPlaybackType != 0) {
                return this.mVolume;
            }
            try {
                return MediaRouter.sStatic.mAudioService.getStreamVolume(this.mPlaybackStream);
            } catch (RemoteException e) {
                Log.e(MediaRouter.TAG, "Error getting local stream volume", e);
                return 0;
            }
        }

        public void requestSetVolume(int volume) {
            if (this.mPlaybackType == 0) {
                try {
                    MediaRouter.sStatic.mAudioService.setStreamVolume(this.mPlaybackStream, volume, 0, ActivityThread.currentPackageName());
                } catch (RemoteException e) {
                    Log.e(MediaRouter.TAG, "Error setting local stream volume", e);
                }
            } else {
                MediaRouter.sStatic.requestSetVolume(this, volume);
            }
        }

        public void requestUpdateVolume(int direction) {
            if (this.mPlaybackType == 0) {
                try {
                    MediaRouter.sStatic.mAudioService.setStreamVolume(this.mPlaybackStream, Math.max(0, Math.min(getVolume() + direction, getVolumeMax())), 0, ActivityThread.currentPackageName());
                } catch (RemoteException e) {
                    Log.e(MediaRouter.TAG, "Error setting local stream volume", e);
                }
            } else {
                MediaRouter.sStatic.requestUpdateVolume(this, direction);
            }
        }

        public int getVolumeMax() {
            if (this.mPlaybackType != 0) {
                return this.mVolumeMax;
            }
            try {
                return MediaRouter.sStatic.mAudioService.getStreamMaxVolume(this.mPlaybackStream);
            } catch (RemoteException e) {
                Log.e(MediaRouter.TAG, "Error getting local stream volume", e);
                return 0;
            }
        }

        public int getVolumeHandling() {
            return this.mVolumeHandling;
        }

        public Display getPresentationDisplay() {
            return this.mPresentationDisplay;
        }

        /* access modifiers changed from: package-private */
        public boolean updatePresentationDisplay() {
            Display display = choosePresentationDisplay();
            if (this.mPresentationDisplay == display) {
                return false;
            }
            this.mPresentationDisplay = display;
            return true;
        }

        private Display choosePresentationDisplay() {
            if ((this.mSupportedTypes & 2) != 0) {
                Display[] displays = MediaRouter.sStatic.getAllPresentationDisplays();
                if (this.mPresentationDisplayId >= 0) {
                    for (Display display : displays) {
                        if (display.getDisplayId() == this.mPresentationDisplayId) {
                            return display;
                        }
                    }
                    return null;
                } else if (this.mDeviceAddress != null) {
                    for (Display display2 : displays) {
                        if (display2.getType() == 3 && this.mDeviceAddress.equals(display2.getAddress())) {
                            return display2;
                        }
                    }
                    return null;
                } else if (this == MediaRouter.sStatic.mDefaultAudioVideo && displays.length > 0) {
                    return displays[0];
                }
            }
            return null;
        }

        public String getDeviceAddress() {
            return this.mDeviceAddress;
        }

        public boolean isEnabled() {
            return this.mEnabled;
        }

        public boolean isConnecting() {
            return this.mResolvedStatusCode == 2;
        }

        public boolean isSelected() {
            return this == MediaRouter.sStatic.mSelectedRoute;
        }

        public boolean isDefault() {
            return this == MediaRouter.sStatic.mDefaultAudioVideo;
        }

        public void select() {
            MediaRouter.selectRouteStatic(this.mSupportedTypes, this, true);
        }

        /* access modifiers changed from: package-private */
        public void setStatusInt(CharSequence status) {
            if (!status.equals(this.mStatus)) {
                this.mStatus = status;
                if (this.mGroup != null) {
                    this.mGroup.memberStatusChanged(this, status);
                }
                routeUpdated();
            }
        }

        /* access modifiers changed from: package-private */
        public void routeUpdated() {
            MediaRouter.updateRoute(this);
        }

        public String toString() {
            return getClass().getSimpleName() + "{ name=" + ((Object) getName()) + ", description=" + ((Object) getDescription()) + ", status=" + ((Object) getStatus()) + ", category=" + getCategory() + ", supportedTypes=" + MediaRouter.typesToString(getSupportedTypes()) + ", presentationDisplay=" + this.mPresentationDisplay + " }";
        }
    }

    public static class UserRouteInfo extends RouteInfo {
        RemoteControlClient mRcc;
        SessionVolumeProvider mSvp;

        UserRouteInfo(RouteCategory category) {
            super(category);
            this.mSupportedTypes = 8388608;
            this.mPlaybackType = 1;
            this.mVolumeHandling = 0;
        }

        public void setName(CharSequence name) {
            this.mName = name;
            routeUpdated();
        }

        public void setName(int resId) {
            this.mNameResId = resId;
            this.mName = null;
            routeUpdated();
        }

        public void setDescription(CharSequence description) {
            this.mDescription = description;
            routeUpdated();
        }

        public void setStatus(CharSequence status) {
            setStatusInt(status);
        }

        public void setRemoteControlClient(RemoteControlClient rcc) {
            this.mRcc = rcc;
            updatePlaybackInfoOnRcc();
        }

        public RemoteControlClient getRemoteControlClient() {
            return this.mRcc;
        }

        public void setIconDrawable(Drawable icon) {
            this.mIcon = icon;
        }

        public void setIconResource(int resId) {
            setIconDrawable(MediaRouter.sStatic.mResources.getDrawable(resId));
        }

        public void setVolumeCallback(VolumeCallback vcb) {
            this.mVcb = new VolumeCallbackInfo(vcb, this);
        }

        public void setPlaybackType(int type) {
            if (this.mPlaybackType != type) {
                this.mPlaybackType = type;
                configureSessionVolume();
            }
        }

        public void setVolumeHandling(int volumeHandling) {
            if (this.mVolumeHandling != volumeHandling) {
                this.mVolumeHandling = volumeHandling;
                configureSessionVolume();
            }
        }

        public void setVolume(int volume) {
            int volume2 = Math.max(0, Math.min(volume, getVolumeMax()));
            if (this.mVolume != volume2) {
                this.mVolume = volume2;
                if (this.mSvp != null) {
                    this.mSvp.setCurrentVolume(this.mVolume);
                }
                MediaRouter.dispatchRouteVolumeChanged(this);
                if (this.mGroup != null) {
                    this.mGroup.memberVolumeChanged(this);
                }
            }
        }

        @Override // android.media.MediaRouter.RouteInfo
        public void requestSetVolume(int volume) {
            if (this.mVolumeHandling != 1) {
                return;
            }
            if (this.mVcb == null) {
                Log.e(MediaRouter.TAG, "Cannot requestSetVolume on user route - no volume callback set");
            } else {
                this.mVcb.vcb.onVolumeSetRequest(this, volume);
            }
        }

        @Override // android.media.MediaRouter.RouteInfo
        public void requestUpdateVolume(int direction) {
            if (this.mVolumeHandling != 1) {
                return;
            }
            if (this.mVcb == null) {
                Log.e(MediaRouter.TAG, "Cannot requestChangeVolume on user route - no volumec callback set");
            } else {
                this.mVcb.vcb.onVolumeUpdateRequest(this, direction);
            }
        }

        public void setVolumeMax(int volumeMax) {
            if (this.mVolumeMax != volumeMax) {
                this.mVolumeMax = volumeMax;
                configureSessionVolume();
            }
        }

        public void setPlaybackStream(int stream) {
            if (this.mPlaybackStream != stream) {
                this.mPlaybackStream = stream;
                configureSessionVolume();
            }
        }

        private void updatePlaybackInfoOnRcc() {
            configureSessionVolume();
        }

        private void configureSessionVolume() {
            if (this.mRcc != null) {
                MediaSession session = this.mRcc.getMediaSession();
                if (session == null) {
                    if (MediaRouter.DEBUG) {
                        Log.d(MediaRouter.TAG, "Rcc has no session to configure volume");
                    }
                } else if (this.mPlaybackType == 1) {
                    int volumeControl = 0;
                    switch (this.mVolumeHandling) {
                        case 1:
                            volumeControl = 2;
                            break;
                    }
                    if (this.mSvp == null || this.mSvp.getVolumeControl() != volumeControl || this.mSvp.getMaxVolume() != this.mVolumeMax) {
                        this.mSvp = new SessionVolumeProvider(volumeControl, this.mVolumeMax, this.mVolume);
                        session.setPlaybackToRemote(this.mSvp);
                    }
                } else {
                    AudioAttributes.Builder bob = new AudioAttributes.Builder();
                    bob.setLegacyStreamType(this.mPlaybackStream);
                    session.setPlaybackToLocal(bob.build());
                    this.mSvp = null;
                }
            } else if (MediaRouter.DEBUG) {
                Log.d(MediaRouter.TAG, "No Rcc to configure volume for route " + ((Object) this.mName));
            }
        }

        /* access modifiers changed from: package-private */
        public class SessionVolumeProvider extends VolumeProvider {
            public SessionVolumeProvider(int volumeControl, int maxVolume, int currentVolume) {
                super(volumeControl, maxVolume, currentVolume);
            }

            @Override // android.media.VolumeProvider
            public void onSetVolumeTo(final int volume) {
                MediaRouter.sStatic.mHandler.post(new Runnable() {
                    /* class android.media.MediaRouter.UserRouteInfo.SessionVolumeProvider.AnonymousClass1 */

                    public void run() {
                        if (UserRouteInfo.this.mVcb != null) {
                            UserRouteInfo.this.mVcb.vcb.onVolumeSetRequest(UserRouteInfo.this.mVcb.route, volume);
                        }
                    }
                });
            }

            @Override // android.media.VolumeProvider
            public void onAdjustVolume(final int direction) {
                MediaRouter.sStatic.mHandler.post(new Runnable() {
                    /* class android.media.MediaRouter.UserRouteInfo.SessionVolumeProvider.AnonymousClass2 */

                    public void run() {
                        if (UserRouteInfo.this.mVcb != null) {
                            UserRouteInfo.this.mVcb.vcb.onVolumeUpdateRequest(UserRouteInfo.this.mVcb.route, direction);
                        }
                    }
                });
            }
        }
    }

    public static class RouteGroup extends RouteInfo {
        final ArrayList<RouteInfo> mRoutes = new ArrayList<>();
        private boolean mUpdateName;

        RouteGroup(RouteCategory category) {
            super(category);
            this.mGroup = this;
            this.mVolumeHandling = 0;
        }

        /* access modifiers changed from: package-private */
        @Override // android.media.MediaRouter.RouteInfo
        public CharSequence getName(Resources res) {
            if (this.mUpdateName) {
                updateName();
            }
            return super.getName(res);
        }

        public void addRoute(RouteInfo route) {
            if (route.getGroup() != null) {
                throw new IllegalStateException("Route " + route + " is already part of a group.");
            } else if (route.getCategory() != this.mCategory) {
                throw new IllegalArgumentException("Route cannot be added to a group with a different category. (Route category=" + route.getCategory() + " group category=" + this.mCategory + ")");
            } else {
                int at = this.mRoutes.size();
                this.mRoutes.add(route);
                route.mGroup = this;
                this.mUpdateName = true;
                updateVolume();
                routeUpdated();
                MediaRouter.dispatchRouteGrouped(route, this, at);
            }
        }

        public void addRoute(RouteInfo route, int insertAt) {
            if (route.getGroup() != null) {
                throw new IllegalStateException("Route " + route + " is already part of a group.");
            } else if (route.getCategory() != this.mCategory) {
                throw new IllegalArgumentException("Route cannot be added to a group with a different category. (Route category=" + route.getCategory() + " group category=" + this.mCategory + ")");
            } else {
                this.mRoutes.add(insertAt, route);
                route.mGroup = this;
                this.mUpdateName = true;
                updateVolume();
                routeUpdated();
                MediaRouter.dispatchRouteGrouped(route, this, insertAt);
            }
        }

        public void removeRoute(RouteInfo route) {
            if (route.getGroup() != this) {
                throw new IllegalArgumentException("Route " + route + " is not a member of this group.");
            }
            this.mRoutes.remove(route);
            route.mGroup = null;
            this.mUpdateName = true;
            updateVolume();
            MediaRouter.dispatchRouteUngrouped(route, this);
            routeUpdated();
        }

        public void removeRoute(int index) {
            RouteInfo route = this.mRoutes.remove(index);
            route.mGroup = null;
            this.mUpdateName = true;
            updateVolume();
            MediaRouter.dispatchRouteUngrouped(route, this);
            routeUpdated();
        }

        public int getRouteCount() {
            return this.mRoutes.size();
        }

        public RouteInfo getRouteAt(int index) {
            return this.mRoutes.get(index);
        }

        public void setIconDrawable(Drawable icon) {
            this.mIcon = icon;
        }

        public void setIconResource(int resId) {
            setIconDrawable(MediaRouter.sStatic.mResources.getDrawable(resId));
        }

        @Override // android.media.MediaRouter.RouteInfo
        public void requestSetVolume(int volume) {
            int maxVol = getVolumeMax();
            if (maxVol != 0) {
                float scaledVolume = ((float) volume) / ((float) maxVol);
                int routeCount = getRouteCount();
                for (int i = 0; i < routeCount; i++) {
                    RouteInfo route = getRouteAt(i);
                    route.requestSetVolume((int) (((float) route.getVolumeMax()) * scaledVolume));
                }
                if (volume != this.mVolume) {
                    this.mVolume = volume;
                    MediaRouter.dispatchRouteVolumeChanged(this);
                }
            }
        }

        @Override // android.media.MediaRouter.RouteInfo
        public void requestUpdateVolume(int direction) {
            if (getVolumeMax() != 0) {
                int routeCount = getRouteCount();
                int volume = 0;
                for (int i = 0; i < routeCount; i++) {
                    RouteInfo route = getRouteAt(i);
                    route.requestUpdateVolume(direction);
                    int routeVol = route.getVolume();
                    if (routeVol > volume) {
                        volume = routeVol;
                    }
                }
                if (volume != this.mVolume) {
                    this.mVolume = volume;
                    MediaRouter.dispatchRouteVolumeChanged(this);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void memberNameChanged(RouteInfo info, CharSequence name) {
            this.mUpdateName = true;
            routeUpdated();
        }

        /* access modifiers changed from: package-private */
        public void memberStatusChanged(RouteInfo info, CharSequence status) {
            setStatusInt(status);
        }

        /* access modifiers changed from: package-private */
        public void memberVolumeChanged(RouteInfo info) {
            updateVolume();
        }

        /* access modifiers changed from: package-private */
        public void updateVolume() {
            int routeCount = getRouteCount();
            int volume = 0;
            for (int i = 0; i < routeCount; i++) {
                int routeVol = getRouteAt(i).getVolume();
                if (routeVol > volume) {
                    volume = routeVol;
                }
            }
            if (volume != this.mVolume) {
                this.mVolume = volume;
                MediaRouter.dispatchRouteVolumeChanged(this);
            }
        }

        /* access modifiers changed from: package-private */
        @Override // android.media.MediaRouter.RouteInfo
        public void routeUpdated() {
            int types = 0;
            int count = this.mRoutes.size();
            if (count == 0) {
                MediaRouter.removeRouteStatic(this);
                return;
            }
            int maxVolume = 0;
            boolean isLocal = true;
            boolean isFixedVolume = true;
            for (int i = 0; i < count; i++) {
                RouteInfo route = this.mRoutes.get(i);
                types |= route.mSupportedTypes;
                int routeMaxVolume = route.getVolumeMax();
                if (routeMaxVolume > maxVolume) {
                    maxVolume = routeMaxVolume;
                }
                isLocal &= route.getPlaybackType() == 0;
                isFixedVolume &= route.getVolumeHandling() == 0;
            }
            this.mPlaybackType = isLocal ? 0 : 1;
            this.mVolumeHandling = isFixedVolume ? 0 : 1;
            this.mSupportedTypes = types;
            this.mVolumeMax = maxVolume;
            this.mIcon = count == 1 ? this.mRoutes.get(0).getIconDrawable() : null;
            super.routeUpdated();
        }

        /* access modifiers changed from: package-private */
        public void updateName() {
            StringBuilder sb = new StringBuilder();
            int count = this.mRoutes.size();
            for (int i = 0; i < count; i++) {
                RouteInfo info = this.mRoutes.get(i);
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(info.mName);
            }
            this.mName = sb.toString();
            this.mUpdateName = false;
        }

        @Override // android.media.MediaRouter.RouteInfo
        public String toString() {
            StringBuilder sb = new StringBuilder(super.toString());
            sb.append('[');
            int count = this.mRoutes.size();
            for (int i = 0; i < count; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(this.mRoutes.get(i));
            }
            sb.append(']');
            return sb.toString();
        }
    }

    public static class RouteCategory {
        final boolean mGroupable;
        boolean mIsSystem;
        CharSequence mName;
        int mNameResId;
        int mTypes;

        RouteCategory(CharSequence name, int types, boolean groupable) {
            this.mName = name;
            this.mTypes = types;
            this.mGroupable = groupable;
        }

        RouteCategory(int nameResId, int types, boolean groupable) {
            this.mNameResId = nameResId;
            this.mTypes = types;
            this.mGroupable = groupable;
        }

        public CharSequence getName() {
            return getName(MediaRouter.sStatic.mResources);
        }

        public CharSequence getName(Context context) {
            return getName(context.getResources());
        }

        /* access modifiers changed from: package-private */
        public CharSequence getName(Resources res) {
            if (this.mNameResId != 0) {
                return res.getText(this.mNameResId);
            }
            return this.mName;
        }

        public List<RouteInfo> getRoutes(List<RouteInfo> out) {
            if (out == null) {
                out = new ArrayList<>();
            } else {
                out.clear();
            }
            int count = MediaRouter.getRouteCountStatic();
            for (int i = 0; i < count; i++) {
                RouteInfo route = MediaRouter.getRouteAtStatic(i);
                if (route.mCategory == this) {
                    out.add(route);
                }
            }
            return out;
        }

        public int getSupportedTypes() {
            return this.mTypes;
        }

        public boolean isGroupable() {
            return this.mGroupable;
        }

        public boolean isSystem() {
            return this.mIsSystem;
        }

        public String toString() {
            return "RouteCategory{ name=" + ((Object) this.mName) + " types=" + MediaRouter.typesToString(this.mTypes) + " groupable=" + this.mGroupable + " }";
        }
    }

    /* access modifiers changed from: package-private */
    public static class CallbackInfo {
        public final Callback cb;
        public int flags;
        public final MediaRouter router;
        public int type;

        public CallbackInfo(Callback cb2, int type2, int flags2, MediaRouter router2) {
            this.cb = cb2;
            this.type = type2;
            this.flags = flags2;
            this.router = router2;
        }

        public boolean filterRouteEvent(RouteInfo route) {
            return filterRouteEvent(route.mSupportedTypes);
        }

        public boolean filterRouteEvent(int supportedTypes) {
            return ((this.flags & 2) == 0 && (this.type & supportedTypes) == 0) ? false : true;
        }
    }

    public static abstract class Callback {
        public abstract void onRouteAdded(MediaRouter mediaRouter, RouteInfo routeInfo);

        public abstract void onRouteChanged(MediaRouter mediaRouter, RouteInfo routeInfo);

        public abstract void onRouteGrouped(MediaRouter mediaRouter, RouteInfo routeInfo, RouteGroup routeGroup, int i);

        public abstract void onRouteRemoved(MediaRouter mediaRouter, RouteInfo routeInfo);

        public abstract void onRouteSelected(MediaRouter mediaRouter, int i, RouteInfo routeInfo);

        public abstract void onRouteUngrouped(MediaRouter mediaRouter, RouteInfo routeInfo, RouteGroup routeGroup);

        public abstract void onRouteUnselected(MediaRouter mediaRouter, int i, RouteInfo routeInfo);

        public abstract void onRouteVolumeChanged(MediaRouter mediaRouter, RouteInfo routeInfo);

        public void onRoutePresentationDisplayChanged(MediaRouter router, RouteInfo info) {
        }
    }

    public static class SimpleCallback extends Callback {
        @Override // android.media.MediaRouter.Callback
        public void onRouteSelected(MediaRouter router, int type, RouteInfo info) {
        }

        @Override // android.media.MediaRouter.Callback
        public void onRouteUnselected(MediaRouter router, int type, RouteInfo info) {
        }

        @Override // android.media.MediaRouter.Callback
        public void onRouteAdded(MediaRouter router, RouteInfo info) {
        }

        @Override // android.media.MediaRouter.Callback
        public void onRouteRemoved(MediaRouter router, RouteInfo info) {
        }

        @Override // android.media.MediaRouter.Callback
        public void onRouteChanged(MediaRouter router, RouteInfo info) {
        }

        @Override // android.media.MediaRouter.Callback
        public void onRouteGrouped(MediaRouter router, RouteInfo info, RouteGroup group, int index) {
        }

        @Override // android.media.MediaRouter.Callback
        public void onRouteUngrouped(MediaRouter router, RouteInfo info, RouteGroup group) {
        }

        @Override // android.media.MediaRouter.Callback
        public void onRouteVolumeChanged(MediaRouter router, RouteInfo info) {
        }
    }

    static class VolumeCallbackInfo {
        public final RouteInfo route;
        public final VolumeCallback vcb;

        public VolumeCallbackInfo(VolumeCallback vcb2, RouteInfo route2) {
            this.vcb = vcb2;
            this.route = route2;
        }
    }

    /* access modifiers changed from: package-private */
    public static class VolumeChangeReceiver extends BroadcastReceiver {
        VolumeChangeReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int newVolume;
            if (intent.getAction().equals(AudioManager.VOLUME_CHANGED_ACTION) && intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, -1) == 3 && (newVolume = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_VALUE, 0)) != intent.getIntExtra(AudioManager.EXTRA_PREV_VOLUME_STREAM_VALUE, 0)) {
                MediaRouter.systemVolumeChanged(newVolume);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class WifiDisplayStatusChangedReceiver extends BroadcastReceiver {
        WifiDisplayStatusChangedReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED)) {
                MediaRouter.updateWifiDisplayStatus((WifiDisplayStatus) intent.getParcelableExtra(DisplayManager.EXTRA_WIFI_DISPLAY_STATUS));
            }
        }
    }
}
