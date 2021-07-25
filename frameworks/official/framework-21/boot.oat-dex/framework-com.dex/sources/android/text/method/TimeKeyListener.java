package android.text.method;

import android.text.format.DateFormat;

public class TimeKeyListener extends NumberKeyListener {
    public static final char[] CHARACTERS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', DateFormat.AM_PM, DateFormat.MINUTE, 'p', ':'};
    private static TimeKeyListener sInstance;

    @Override // android.text.method.KeyListener
    public int getInputType() {
        return 36;
    }

    /* access modifiers changed from: protected */
    @Override // android.text.method.NumberKeyListener
    public char[] getAcceptedChars() {
        return CHARACTERS;
    }

    public static TimeKeyListener getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        sInstance = new TimeKeyListener();
        return sInstance;
    }
}
