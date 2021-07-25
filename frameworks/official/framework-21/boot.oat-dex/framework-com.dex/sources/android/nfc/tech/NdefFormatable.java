package android.nfc.tech;

import android.nfc.FormatException;
import android.nfc.INfcTag;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;

public final class NdefFormatable extends BasicTagTechnology {
    private static final String TAG = "NFC";

    @Override // android.nfc.tech.BasicTagTechnology, java.io.Closeable, android.nfc.tech.TagTechnology, java.lang.AutoCloseable
    public /* bridge */ /* synthetic */ void close() throws IOException {
        super.close();
    }

    @Override // android.nfc.tech.BasicTagTechnology, android.nfc.tech.TagTechnology
    public /* bridge */ /* synthetic */ void connect() throws IOException {
        super.connect();
    }

    @Override // android.nfc.tech.BasicTagTechnology, android.nfc.tech.TagTechnology
    public /* bridge */ /* synthetic */ Tag getTag() {
        return super.getTag();
    }

    @Override // android.nfc.tech.BasicTagTechnology, android.nfc.tech.TagTechnology
    public /* bridge */ /* synthetic */ boolean isConnected() {
        return super.isConnected();
    }

    @Override // android.nfc.tech.BasicTagTechnology, android.nfc.tech.TagTechnology
    public /* bridge */ /* synthetic */ void reconnect() throws IOException {
        super.reconnect();
    }

    public static NdefFormatable get(Tag tag) {
        if (!tag.hasTech(7)) {
            return null;
        }
        try {
            return new NdefFormatable(tag);
        } catch (RemoteException e) {
            return null;
        }
    }

    public NdefFormatable(Tag tag) throws RemoteException {
        super(tag, 7);
    }

    public void format(NdefMessage firstMessage) throws IOException, FormatException {
        format(firstMessage, false);
    }

    public void formatReadOnly(NdefMessage firstMessage) throws IOException, FormatException {
        format(firstMessage, true);
    }

    /* access modifiers changed from: package-private */
    public void format(NdefMessage firstMessage, boolean makeReadOnly) throws IOException, FormatException {
        checkConnected();
        try {
            int serviceHandle = this.mTag.getServiceHandle();
            INfcTag tagService = this.mTag.getTagService();
            switch (tagService.formatNdef(serviceHandle, MifareClassic.KEY_DEFAULT)) {
                case -8:
                    throw new FormatException();
                case -1:
                    throw new IOException();
                case 0:
                    if (!tagService.isNdef(serviceHandle)) {
                        throw new IOException();
                    }
                    if (firstMessage != null) {
                        switch (tagService.ndefWrite(serviceHandle, firstMessage)) {
                            case -8:
                                throw new FormatException();
                            case -1:
                                throw new IOException();
                            case 0:
                                break;
                            default:
                                throw new IOException();
                        }
                    }
                    if (makeReadOnly) {
                        switch (tagService.ndefMakeReadOnly(serviceHandle)) {
                            case -8:
                                throw new IOException();
                            case -1:
                                throw new IOException();
                            case 0:
                                return;
                            default:
                                throw new IOException();
                        }
                    } else {
                        return;
                    }
                default:
                    throw new IOException();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
        }
    }
}
