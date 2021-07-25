package android.nfc;

import android.content.Intent;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.BatteryStats;
import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.WebView;
import android.widget.ExpandableListView;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public final class NdefRecord implements Parcelable {
    public static final Parcelable.Creator<NdefRecord> CREATOR = new Parcelable.Creator<NdefRecord>() {
        /* class android.nfc.NdefRecord.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NdefRecord createFromParcel(Parcel in) {
            byte[] type = new byte[in.readInt()];
            in.readByteArray(type);
            byte[] id = new byte[in.readInt()];
            in.readByteArray(id);
            byte[] payload = new byte[in.readInt()];
            in.readByteArray(payload);
            return new NdefRecord((short) in.readInt(), type, id, payload);
        }

        @Override // android.os.Parcelable.Creator
        public NdefRecord[] newArray(int size) {
            return new NdefRecord[size];
        }
    };
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final byte FLAG_CF = 32;
    private static final byte FLAG_IL = 8;
    private static final byte FLAG_MB = Byte.MIN_VALUE;
    private static final byte FLAG_ME = 64;
    private static final byte FLAG_SR = 16;
    private static final int MAX_PAYLOAD_SIZE = 10485760;
    public static final byte[] RTD_ALTERNATIVE_CARRIER = {97, 99};
    public static final byte[] RTD_ANDROID_APP = "android.com:pkg".getBytes();
    public static final byte[] RTD_HANDOVER_CARRIER = {72, 99};
    public static final byte[] RTD_HANDOVER_REQUEST = {72, 114};
    public static final byte[] RTD_HANDOVER_SELECT = {72, 115};
    public static final byte[] RTD_SMART_POSTER = {83, 112};
    public static final byte[] RTD_TEXT = {84};
    public static final byte[] RTD_URI = {85};
    public static final short TNF_ABSOLUTE_URI = 3;
    public static final short TNF_EMPTY = 0;
    public static final short TNF_EXTERNAL_TYPE = 4;
    public static final short TNF_MIME_MEDIA = 2;
    public static final short TNF_RESERVED = 7;
    public static final short TNF_UNCHANGED = 6;
    public static final short TNF_UNKNOWN = 5;
    public static final short TNF_WELL_KNOWN = 1;
    private static final String[] URI_PREFIX_MAP = {ProxyInfo.LOCAL_EXCL_LIST, "http://www.", "https://www.", "http://", "https://", WebView.SCHEME_TEL, "mailto:", "ftp://anonymous:anonymous@", "ftp://ftp.", "ftps://", "sftp://", "smb://", "nfs://", "ftp://", "dav://", "news:", "telnet://", "imap:", "rtsp://", "urn:", "pop:", "sip:", "sips:", "tftp:", "btspp://", "btl2cap://", "btgoep://", "tcpobex://", "irdaobex://", "file://", "urn:epc:id:", "urn:epc:tag:", "urn:epc:pat:", "urn:epc:raw:", "urn:epc:", "urn:nfc:"};
    private final byte[] mId;
    private final byte[] mPayload;
    private final short mTnf;
    private final byte[] mType;

    public static NdefRecord createApplicationRecord(String packageName) {
        if (packageName == null) {
            throw new NullPointerException("packageName is null");
        } else if (packageName.length() != 0) {
            return new NdefRecord(4, RTD_ANDROID_APP, null, packageName.getBytes(StandardCharsets.UTF_8));
        } else {
            throw new IllegalArgumentException("packageName is empty");
        }
    }

    public static NdefRecord createUri(Uri uri) {
        if (uri == null) {
            throw new NullPointerException("uri is null");
        }
        String uriString = uri.normalizeScheme().toString();
        if (uriString.length() == 0) {
            throw new IllegalArgumentException("uri is empty");
        }
        byte prefix = 0;
        int i = 1;
        while (true) {
            if (i >= URI_PREFIX_MAP.length) {
                break;
            } else if (uriString.startsWith(URI_PREFIX_MAP[i])) {
                prefix = (byte) i;
                uriString = uriString.substring(URI_PREFIX_MAP[i].length());
                break;
            } else {
                i++;
            }
        }
        byte[] uriBytes = uriString.getBytes(StandardCharsets.UTF_8);
        byte[] recordBytes = new byte[(uriBytes.length + 1)];
        recordBytes[0] = prefix;
        System.arraycopy(uriBytes, 0, recordBytes, 1, uriBytes.length);
        return new NdefRecord(1, RTD_URI, null, recordBytes);
    }

    public static NdefRecord createUri(String uriString) {
        return createUri(Uri.parse(uriString));
    }

    public static NdefRecord createMime(String mimeType, byte[] mimeData) {
        if (mimeType == null) {
            throw new NullPointerException("mimeType is null");
        }
        String mimeType2 = Intent.normalizeMimeType(mimeType);
        if (mimeType2.length() == 0) {
            throw new IllegalArgumentException("mimeType is empty");
        }
        int slashIndex = mimeType2.indexOf(47);
        if (slashIndex == 0) {
            throw new IllegalArgumentException("mimeType must have major type");
        } else if (slashIndex != mimeType2.length() - 1) {
            return new NdefRecord(2, mimeType2.getBytes(StandardCharsets.US_ASCII), null, mimeData);
        } else {
            throw new IllegalArgumentException("mimeType must have minor type");
        }
    }

    public static NdefRecord createExternal(String domain, String type, byte[] data) {
        if (domain == null) {
            throw new NullPointerException("domain is null");
        } else if (type == null) {
            throw new NullPointerException("type is null");
        } else {
            String domain2 = domain.trim().toLowerCase(Locale.ROOT);
            String type2 = type.trim().toLowerCase(Locale.ROOT);
            if (domain2.length() == 0) {
                throw new IllegalArgumentException("domain is empty");
            } else if (type2.length() == 0) {
                throw new IllegalArgumentException("type is empty");
            } else {
                byte[] byteDomain = domain2.getBytes(StandardCharsets.UTF_8);
                byte[] byteType = type2.getBytes(StandardCharsets.UTF_8);
                byte[] b = new byte[(byteDomain.length + 1 + byteType.length)];
                System.arraycopy(byteDomain, 0, b, 0, byteDomain.length);
                b[byteDomain.length] = 58;
                System.arraycopy(byteType, 0, b, byteDomain.length + 1, byteType.length);
                return new NdefRecord(4, b, null, data);
            }
        }
    }

    public static NdefRecord createTextRecord(String languageCode, String text) {
        byte[] languageCodeBytes;
        if (text == null) {
            throw new NullPointerException("text is null");
        }
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        if (languageCode == null || languageCode.isEmpty()) {
            languageCodeBytes = Locale.getDefault().getLanguage().getBytes(StandardCharsets.US_ASCII);
        } else {
            languageCodeBytes = languageCode.getBytes(StandardCharsets.US_ASCII);
        }
        if (languageCodeBytes.length >= 64) {
            throw new IllegalArgumentException("language code is too long, must be <64 bytes.");
        }
        ByteBuffer buffer = ByteBuffer.allocate(languageCodeBytes.length + 1 + textBytes.length);
        buffer.put((byte) (languageCodeBytes.length & 255));
        buffer.put(languageCodeBytes);
        buffer.put(textBytes);
        return new NdefRecord(1, RTD_TEXT, null, buffer.array());
    }

    public NdefRecord(short tnf, byte[] type, byte[] id, byte[] payload) {
        type = type == null ? EMPTY_BYTE_ARRAY : type;
        id = id == null ? EMPTY_BYTE_ARRAY : id;
        payload = payload == null ? EMPTY_BYTE_ARRAY : payload;
        String message = validateTnf(tnf, type, id, payload);
        if (message != null) {
            throw new IllegalArgumentException(message);
        }
        this.mTnf = tnf;
        this.mType = type;
        this.mId = id;
        this.mPayload = payload;
    }

    @Deprecated
    public NdefRecord(byte[] data) throws FormatException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        NdefRecord[] rs = parse(buffer, true);
        if (buffer.remaining() > 0) {
            throw new FormatException("data too long");
        }
        this.mTnf = rs[0].mTnf;
        this.mType = rs[0].mType;
        this.mId = rs[0].mId;
        this.mPayload = rs[0].mPayload;
    }

    public short getTnf() {
        return this.mTnf;
    }

    public byte[] getType() {
        return (byte[]) this.mType.clone();
    }

    public byte[] getId() {
        return (byte[]) this.mId.clone();
    }

    public byte[] getPayload() {
        return (byte[]) this.mPayload.clone();
    }

    @Deprecated
    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(getByteLength());
        writeToByteBuffer(buffer, true, true);
        return buffer.array();
    }

    public String toMimeType() {
        switch (this.mTnf) {
            case 2:
                return Intent.normalizeMimeType(new String(this.mType, StandardCharsets.US_ASCII));
            case 1:
                if (Arrays.equals(this.mType, RTD_TEXT)) {
                    return "text/plain";
                }
                break;
        }
        return null;
    }

    public Uri toUri() {
        return toUri(false);
    }

    private Uri toUri(boolean inSmartPoster) {
        Uri wktUri;
        switch (this.mTnf) {
            case 1:
                if (Arrays.equals(this.mType, RTD_SMART_POSTER) && !inSmartPoster) {
                    try {
                        for (NdefRecord nestedRecord : new NdefMessage(this.mPayload).getRecords()) {
                            Uri uri = nestedRecord.toUri(true);
                            if (uri != null) {
                                return uri;
                            }
                        }
                        return null;
                    } catch (FormatException e) {
                        return null;
                    }
                } else if (!Arrays.equals(this.mType, RTD_URI) || (wktUri = parseWktUri()) == null) {
                    return null;
                } else {
                    return wktUri.normalizeScheme();
                }
            case 2:
            default:
                return null;
            case 3:
                return Uri.parse(new String(this.mType, StandardCharsets.UTF_8)).normalizeScheme();
            case 4:
                if (!inSmartPoster) {
                    return Uri.parse("vnd.android.nfc://ext/" + new String(this.mType, StandardCharsets.US_ASCII));
                }
                return null;
        }
    }

    private Uri parseWktUri() {
        int prefixIndex;
        if (this.mPayload.length < 2 || (prefixIndex = this.mPayload[0] & -1) < 0 || prefixIndex >= URI_PREFIX_MAP.length) {
            return null;
        }
        return Uri.parse(URI_PREFIX_MAP[prefixIndex] + new String(Arrays.copyOfRange(this.mPayload, 1, this.mPayload.length), StandardCharsets.UTF_8));
    }

    static NdefRecord[] parse(ByteBuffer buffer, boolean ignoreMbMe) throws FormatException {
        List<NdefRecord> records = new ArrayList<>();
        byte[] type = null;
        byte[] id = null;
        try {
            ArrayList<byte[]> chunks = new ArrayList<>();
            boolean inChunk = false;
            short chunkTnf = -1;
            boolean me = false;
            while (!me) {
                byte flag = buffer.get();
                boolean mb = (flag & FLAG_MB) != 0;
                me = (flag & FLAG_ME) != 0;
                boolean cf = (flag & FLAG_CF) != 0;
                boolean sr = (flag & FLAG_SR) != 0;
                boolean il = (flag & FLAG_IL) != 0;
                short tnf = (short) (flag & 7);
                if (!mb && records.size() == 0 && !inChunk && !ignoreMbMe) {
                    throw new FormatException("expected MB flag");
                } else if (mb && records.size() != 0 && !ignoreMbMe) {
                    throw new FormatException("unexpected MB flag");
                } else if (inChunk && il) {
                    throw new FormatException("unexpected IL flag in non-leading chunk");
                } else if (cf && me) {
                    throw new FormatException("unexpected ME flag in non-trailing chunk");
                } else if (inChunk && tnf != 6) {
                    throw new FormatException("expected TNF_UNCHANGED in non-leading chunk");
                } else if (inChunk || tnf != 6) {
                    int typeLength = buffer.get() & BatteryStats.HistoryItem.CMD_NULL;
                    long payloadLength = sr ? (long) (buffer.get() & BatteryStats.HistoryItem.CMD_NULL) : ((long) buffer.getInt()) & ExpandableListView.PACKED_POSITION_VALUE_NULL;
                    int idLength = il ? buffer.get() & BatteryStats.HistoryItem.CMD_NULL : 0;
                    if (!inChunk || typeLength == 0) {
                        if (!inChunk) {
                            type = typeLength > 0 ? new byte[typeLength] : EMPTY_BYTE_ARRAY;
                            id = idLength > 0 ? new byte[idLength] : EMPTY_BYTE_ARRAY;
                            buffer.get(type);
                            buffer.get(id);
                        }
                        ensureSanePayloadSize(payloadLength);
                        byte[] payload = payloadLength > 0 ? new byte[((int) payloadLength)] : EMPTY_BYTE_ARRAY;
                        buffer.get(payload);
                        if (cf && !inChunk) {
                            chunks.clear();
                            chunkTnf = tnf;
                        }
                        if (cf || inChunk) {
                            chunks.add(payload);
                        }
                        if (!cf && inChunk) {
                            long payloadLength2 = 0;
                            Iterator i$ = chunks.iterator();
                            while (i$.hasNext()) {
                                payloadLength2 += (long) i$.next().length;
                            }
                            ensureSanePayloadSize(payloadLength2);
                            payload = new byte[((int) payloadLength2)];
                            int i = 0;
                            Iterator i$2 = chunks.iterator();
                            while (i$2.hasNext()) {
                                byte[] p = i$2.next();
                                System.arraycopy(p, 0, payload, i, p.length);
                                i += p.length;
                            }
                            tnf = chunkTnf;
                        }
                        if (cf) {
                            inChunk = true;
                        } else {
                            inChunk = false;
                            String error = validateTnf(tnf, type, id, payload);
                            if (error != null) {
                                throw new FormatException(error);
                            }
                            records.add(new NdefRecord(tnf, type, id, payload));
                            if (ignoreMbMe) {
                                break;
                            }
                        }
                    } else {
                        throw new FormatException("expected zero-length type in non-leading chunk");
                    }
                } else {
                    throw new FormatException("unexpected TNF_UNCHANGED in first chunk or unchunked record");
                }
            }
            return (NdefRecord[]) records.toArray(new NdefRecord[records.size()]);
        } catch (BufferUnderflowException e) {
            throw new FormatException("expected more data", e);
        }
    }

    private static void ensureSanePayloadSize(long size) throws FormatException {
        if (size > 10485760) {
            throw new FormatException("payload above max limit: " + size + " > " + MAX_PAYLOAD_SIZE);
        }
    }

    static String validateTnf(short tnf, byte[] type, byte[] id, byte[] payload) {
        switch (tnf) {
            case 0:
                if (type.length == 0 && id.length == 0 && payload.length == 0) {
                    return null;
                }
                return "unexpected data in TNF_EMPTY record";
            case 1:
            case 2:
            case 3:
            case 4:
                return null;
            case 5:
            case 7:
                if (type.length != 0) {
                    return "unexpected type field in TNF_UNKNOWN or TNF_RESERVEd record";
                }
                return null;
            case 6:
                return "unexpected TNF_UNCHANGED in first chunk or logical record";
            default:
                return String.format("unexpected tnf value: 0x%02x", Short.valueOf(tnf));
        }
    }

    /* access modifiers changed from: package-private */
    public void writeToByteBuffer(ByteBuffer buffer, boolean mb, boolean me) {
        boolean sr;
        boolean il;
        int i;
        int i2 = 0;
        if (this.mPayload.length < 256) {
            sr = true;
        } else {
            sr = false;
        }
        if (this.mId.length > 0) {
            il = true;
        } else {
            il = false;
        }
        if (mb) {
            i = -128;
        } else {
            i = 0;
        }
        int i3 = (sr ? 16 : 0) | i | (me ? 64 : 0);
        if (il) {
            i2 = 8;
        }
        buffer.put((byte) (i3 | i2 | this.mTnf));
        buffer.put((byte) this.mType.length);
        if (sr) {
            buffer.put((byte) this.mPayload.length);
        } else {
            buffer.putInt(this.mPayload.length);
        }
        if (il) {
            buffer.put((byte) this.mId.length);
        }
        buffer.put(this.mType);
        buffer.put(this.mId);
        buffer.put(this.mPayload);
    }

    /* access modifiers changed from: package-private */
    public int getByteLength() {
        boolean sr;
        boolean il;
        int length = this.mType.length + 3 + this.mId.length + this.mPayload.length;
        if (this.mPayload.length < 256) {
            sr = true;
        } else {
            sr = false;
        }
        if (this.mId.length > 0) {
            il = true;
        } else {
            il = false;
        }
        if (!sr) {
            length += 3;
        }
        if (il) {
            return length + 1;
        }
        return length;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mTnf);
        dest.writeInt(this.mType.length);
        dest.writeByteArray(this.mType);
        dest.writeInt(this.mId.length);
        dest.writeByteArray(this.mId);
        dest.writeInt(this.mPayload.length);
        dest.writeByteArray(this.mPayload);
    }

    public int hashCode() {
        return ((((((Arrays.hashCode(this.mId) + 31) * 31) + Arrays.hashCode(this.mPayload)) * 31) + this.mTnf) * 31) + Arrays.hashCode(this.mType);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        NdefRecord other = (NdefRecord) obj;
        if (!Arrays.equals(this.mId, other.mId) || !Arrays.equals(this.mPayload, other.mPayload) || this.mTnf != other.mTnf) {
            return false;
        }
        return Arrays.equals(this.mType, other.mType);
    }

    public String toString() {
        StringBuilder b = new StringBuilder(String.format("NdefRecord tnf=%X", Short.valueOf(this.mTnf)));
        if (this.mType.length > 0) {
            b.append(" type=").append((CharSequence) bytesToString(this.mType));
        }
        if (this.mId.length > 0) {
            b.append(" id=").append((CharSequence) bytesToString(this.mId));
        }
        if (this.mPayload.length > 0) {
            b.append(" payload=").append((CharSequence) bytesToString(this.mPayload));
        }
        return b.toString();
    }

    private static StringBuilder bytesToString(byte[] bs) {
        StringBuilder s = new StringBuilder();
        int len$ = bs.length;
        for (int i$ = 0; i$ < len$; i$++) {
            s.append(String.format("%02X", Byte.valueOf(bs[i$])));
        }
        return s;
    }
}
