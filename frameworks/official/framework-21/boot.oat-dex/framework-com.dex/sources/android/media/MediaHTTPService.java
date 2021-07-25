package android.media;

import android.media.IMediaHTTPService;
import android.os.IBinder;

public class MediaHTTPService extends IMediaHTTPService.Stub {
    private static final String TAG = "MediaHTTPService";

    @Override // android.media.IMediaHTTPService
    public IMediaHTTPConnection makeHTTPConnection() {
        return new MediaHTTPConnection();
    }

    static IBinder createHttpServiceBinderIfNecessary(String path) {
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("widevine://")) {
            return new MediaHTTPService().asBinder();
        }
        return null;
    }
}
