package android.location;

import android.content.Context;
import android.location.GpsMeasurementsEvent;
import android.location.IGpsMeasurementsListener;
import android.location.LocalListenerHelper;
import android.os.RemoteException;

/* access modifiers changed from: package-private */
public class GpsMeasurementListenerTransport extends LocalListenerHelper<GpsMeasurementsEvent.Listener> {
    private final Context mContext;
    private final IGpsMeasurementsListener mListenerTransport = new ListenerTransport();
    private final ILocationManager mLocationManager;

    public GpsMeasurementListenerTransport(Context context, ILocationManager locationManager) {
        super("GpsMeasurementListenerTransport");
        this.mContext = context;
        this.mLocationManager = locationManager;
    }

    /* access modifiers changed from: protected */
    @Override // android.location.LocalListenerHelper
    public boolean registerWithServer() throws RemoteException {
        return this.mLocationManager.addGpsMeasurementsListener(this.mListenerTransport, this.mContext.getPackageName());
    }

    /* access modifiers changed from: protected */
    @Override // android.location.LocalListenerHelper
    public void unregisterFromServer() throws RemoteException {
        this.mLocationManager.removeGpsMeasurementsListener(this.mListenerTransport);
    }

    private class ListenerTransport extends IGpsMeasurementsListener.Stub {
        private ListenerTransport() {
        }

        @Override // android.location.IGpsMeasurementsListener
        public void onGpsMeasurementsReceived(final GpsMeasurementsEvent event) {
            GpsMeasurementListenerTransport.this.foreach(new LocalListenerHelper.ListenerOperation<GpsMeasurementsEvent.Listener>() {
                /* class android.location.GpsMeasurementListenerTransport.ListenerTransport.AnonymousClass1 */

                public void execute(GpsMeasurementsEvent.Listener listener) throws RemoteException {
                    listener.onGpsMeasurementsReceived(event);
                }
            });
        }
    }
}
