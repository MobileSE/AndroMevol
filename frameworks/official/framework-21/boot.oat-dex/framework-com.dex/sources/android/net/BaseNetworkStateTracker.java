package android.net;

import android.content.Context;
import android.net.SamplingDataTracker;
import android.os.Handler;
import android.os.Messenger;
import com.android.internal.util.Preconditions;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseNetworkStateTracker implements NetworkStateTracker {
    public static final String PROP_TCP_BUFFER_UNKNOWN = "net.tcp.buffersize.unknown";
    public static final String PROP_TCP_BUFFER_WIFI = "net.tcp.buffersize.wifi";
    protected Context mContext;
    private AtomicBoolean mDefaultRouteSet = new AtomicBoolean(false);
    protected LinkProperties mLinkProperties;
    protected Network mNetwork = new Network(0);
    protected NetworkCapabilities mNetworkCapabilities;
    protected NetworkInfo mNetworkInfo;
    private AtomicBoolean mPrivateDnsRouteSet = new AtomicBoolean(false);
    private Handler mTarget;
    private AtomicBoolean mTeardownRequested = new AtomicBoolean(false);

    public BaseNetworkStateTracker(int networkType) {
        this.mNetworkInfo = new NetworkInfo(networkType, -1, ConnectivityManager.getNetworkTypeName(networkType), null);
        this.mLinkProperties = new LinkProperties();
        this.mNetworkCapabilities = new NetworkCapabilities();
    }

    protected BaseNetworkStateTracker() {
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public Handler getTargetHandler() {
        return this.mTarget;
    }

    /* access modifiers changed from: protected */
    public final void dispatchStateChanged() {
        this.mTarget.obtainMessage(458752, getNetworkInfo()).sendToTarget();
    }

    /* access modifiers changed from: protected */
    public final void dispatchConfigurationChanged() {
        this.mTarget.obtainMessage(NetworkStateTracker.EVENT_CONFIGURATION_CHANGED, getNetworkInfo()).sendToTarget();
    }

    @Override // android.net.NetworkStateTracker
    public void startMonitoring(Context context, Handler target) {
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mTarget = (Handler) Preconditions.checkNotNull(target);
        startMonitoringInternal();
    }

    /* access modifiers changed from: protected */
    public void startMonitoringInternal() {
    }

    @Override // android.net.NetworkStateTracker
    public NetworkInfo getNetworkInfo() {
        return new NetworkInfo(this.mNetworkInfo);
    }

    @Override // android.net.NetworkStateTracker
    public LinkProperties getLinkProperties() {
        return new LinkProperties(this.mLinkProperties);
    }

    @Override // android.net.NetworkStateTracker
    public NetworkCapabilities getNetworkCapabilities() {
        return new NetworkCapabilities(this.mNetworkCapabilities);
    }

    @Override // android.net.NetworkStateTracker
    public LinkQualityInfo getLinkQualityInfo() {
        return null;
    }

    @Override // android.net.NetworkStateTracker
    public void captivePortalCheckCompleted(boolean isCaptivePortal) {
    }

    @Override // android.net.NetworkStateTracker
    public boolean setRadio(boolean turnOn) {
        return true;
    }

    @Override // android.net.NetworkStateTracker
    public boolean isAvailable() {
        return this.mNetworkInfo.isAvailable();
    }

    @Override // android.net.NetworkStateTracker
    public void setUserDataEnable(boolean enabled) {
    }

    @Override // android.net.NetworkStateTracker
    public void setPolicyDataEnable(boolean enabled) {
    }

    @Override // android.net.NetworkStateTracker
    public boolean isPrivateDnsRouteSet() {
        return this.mPrivateDnsRouteSet.get();
    }

    @Override // android.net.NetworkStateTracker
    public void privateDnsRouteSet(boolean enabled) {
        this.mPrivateDnsRouteSet.set(enabled);
    }

    @Override // android.net.NetworkStateTracker
    public boolean isDefaultRouteSet() {
        return this.mDefaultRouteSet.get();
    }

    @Override // android.net.NetworkStateTracker
    public void defaultRouteSet(boolean enabled) {
        this.mDefaultRouteSet.set(enabled);
    }

    @Override // android.net.NetworkStateTracker
    public boolean isTeardownRequested() {
        return this.mTeardownRequested.get();
    }

    @Override // android.net.NetworkStateTracker
    public void setTeardownRequested(boolean isRequested) {
        this.mTeardownRequested.set(isRequested);
    }

    @Override // android.net.NetworkStateTracker
    public void setDependencyMet(boolean met) {
    }

    @Override // android.net.NetworkStateTracker
    public void addStackedLink(LinkProperties link) {
        this.mLinkProperties.addStackedLink(link);
    }

    @Override // android.net.NetworkStateTracker
    public void removeStackedLink(LinkProperties link) {
        this.mLinkProperties.removeStackedLink(link);
    }

    @Override // android.net.NetworkStateTracker
    public void supplyMessenger(Messenger messenger) {
    }

    @Override // android.net.NetworkStateTracker
    public String getNetworkInterfaceName() {
        if (this.mLinkProperties != null) {
            return this.mLinkProperties.getInterfaceName();
        }
        return null;
    }

    @Override // android.net.NetworkStateTracker
    public void startSampling(SamplingDataTracker.SamplingSnapshot s) {
    }

    @Override // android.net.NetworkStateTracker
    public void stopSampling(SamplingDataTracker.SamplingSnapshot s) {
    }

    @Override // android.net.NetworkStateTracker
    public void setNetId(int netId) {
        this.mNetwork = new Network(netId);
    }

    @Override // android.net.NetworkStateTracker
    public Network getNetwork() {
        return this.mNetwork;
    }
}
