package android.net.http;

import android.content.Context;
import android.util.Log;

public class ErrorStrings {
    private static final String LOGTAG = "Http";

    private ErrorStrings() {
    }

    public static String getString(int errorCode, Context context) {
        return context.getText(getResource(errorCode)).toString();
    }

    public static int getResource(int errorCode) {
        switch (errorCode) {
            case -15:
                return 17039578;
            case -14:
                return 17039577;
            case -13:
                return 17039576;
            case -12:
                return 17039367;
            case -11:
                return 17039575;
            case -10:
                return 17039368;
            case -9:
                return 17039574;
            case -8:
                return 17039573;
            case -7:
                return 17039572;
            case -6:
                return 17039571;
            case -5:
                return 17039570;
            case -4:
                return 17039569;
            case -3:
                return 17039568;
            case -2:
                return 17039567;
            case -1:
                return 17039566;
            case 0:
                return 17039565;
            default:
                Log.w(LOGTAG, "Using generic message for unknown error code: " + errorCode);
                return 17039566;
        }
    }
}
