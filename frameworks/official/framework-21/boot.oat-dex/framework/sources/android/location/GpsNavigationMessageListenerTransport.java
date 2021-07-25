package android.location;

import android.content.Context;
import android.location.GpsNavigationMessageEvent;
import android.location.IGpsNavigationMessageListener;
import android.location.LocalListenerHelper;
import android.os.RemoteException;

/* access modifiers changed from: package-private */
public class GpsNavigationMessageListenerTransport extends LocalListenerHelper<GpsNavigationMessageEvent.Listener> {
    private final Context mContext;
    private final IGpsNavigationMessageListener mListenerTransport = new ListenerTransport();
    private final ILocationManager mLocationManager;

    public GpsNavigationMessageListenerTransport(Context context, ILocationManager locationManager) {
        super("GpsNavigationMessageListenerTransport");
        this.mContext = context;
        this.mLocationManager = locationManager;
    }

    /* access modifiers changed from: protected */
    @Override // android.location.LocalListenerHelper
    public boolean registerWithServer() throws RemoteException {
        return this.mLocationManager.addGpsNavigationMessageListener(this.mListenerTransport, this.mContext.getPackageName());
    }

    /* access modifiers changed from: protected */
    @Override // android.location.LocalListenerHelper
    public void unregisterFromServer() throws RemoteException {
        this.mLocationManager.removeGpsNavigationMessageListener(this.mListenerTransport);
    }

    private class ListenerTransport extends IGpsNavigationMessageListener.Stub {
        private ListenerTransport() {
        }

        @Override // android.location.IGpsNavigationMessageListener
        public void onGpsNavigationMessageReceived(final GpsNavigationMessageEvent event) {
            GpsNavigationMessageListenerTransport.this.foreach(new LocalListenerHelper.ListenerOperation<GpsNavigationMessageEvent.Listener>() {
                /* class android.location.GpsNavigationMessageListenerTransport.ListenerTransport.AnonymousClass1 */

                public void execute(GpsNavigationMessageEvent.Listener listener) throws RemoteException {
                    listener.onGpsNavigationMessageReceived(event);
                }
            });
        }
    }
}
