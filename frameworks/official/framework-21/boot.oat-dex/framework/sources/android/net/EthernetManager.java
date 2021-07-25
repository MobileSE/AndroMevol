package android.net;

import android.content.Context;
import android.os.RemoteException;

public class EthernetManager {
    private static final String TAG = "EthernetManager";
    private final Context mContext;
    private final IEthernetManager mService;

    public EthernetManager(Context context, IEthernetManager service) {
        this.mContext = context;
        this.mService = service;
    }

    public IpConfiguration getConfiguration() {
        if (this.mService == null) {
            return new IpConfiguration();
        }
        try {
            return this.mService.getConfiguration();
        } catch (RemoteException e) {
            return new IpConfiguration();
        }
    }

    public void setConfiguration(IpConfiguration config) {
        if (this.mService != null) {
            try {
                this.mService.setConfiguration(config);
            } catch (RemoteException e) {
            }
        }
    }
}
