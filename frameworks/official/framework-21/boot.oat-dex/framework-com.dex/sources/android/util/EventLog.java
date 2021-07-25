package android.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashMap;

public class EventLog {
    private static final String COMMENT_PATTERN = "^\\s*(#.*)?$";
    private static final String TAG = "EventLog";
    private static final String TAGS_FILE = "/system/etc/event-log-tags";
    private static final String TAG_PATTERN = "^\\s*(\\d+)\\s+(\\w+)\\s*(\\(.*\\))?\\s*$";
    private static HashMap<String, Integer> sTagCodes = null;
    private static HashMap<Integer, String> sTagNames = null;

    public static native void readEvents(int[] iArr, Collection<Event> collection) throws IOException;

    public static native int writeEvent(int i, int i2);

    public static native int writeEvent(int i, long j);

    public static native int writeEvent(int i, String str);

    public static native int writeEvent(int i, Object... objArr);

    public static final class Event {
        private static final int DATA_OFFSET = 4;
        private static final int HEADER_SIZE_OFFSET = 2;
        private static final byte INT_TYPE = 0;
        private static final int LENGTH_OFFSET = 0;
        private static final byte LIST_TYPE = 3;
        private static final byte LONG_TYPE = 1;
        private static final int NANOSECONDS_OFFSET = 16;
        private static final int PROCESS_OFFSET = 4;
        private static final int SECONDS_OFFSET = 12;
        private static final byte STRING_TYPE = 2;
        private static final int THREAD_OFFSET = 8;
        private static final int V1_PAYLOAD_START = 20;
        private final ByteBuffer mBuffer;

        Event(byte[] data) {
            this.mBuffer = ByteBuffer.wrap(data);
            this.mBuffer.order(ByteOrder.nativeOrder());
        }

        public int getProcessId() {
            return this.mBuffer.getInt(4);
        }

        public int getThreadId() {
            return this.mBuffer.getInt(8);
        }

        public long getTimeNanos() {
            return (((long) this.mBuffer.getInt(12)) * 1000000000) + ((long) this.mBuffer.getInt(16));
        }

        public int getTag() {
            int offset = this.mBuffer.getShort(2);
            if (offset == 0) {
                offset = 20;
            }
            return this.mBuffer.getInt(offset);
        }

        public synchronized Object getData() {
            Object obj = null;
            synchronized (this) {
                try {
                    int offset = this.mBuffer.getShort(2);
                    if (offset == 0) {
                        offset = 20;
                    }
                    this.mBuffer.limit(this.mBuffer.getShort(0) + offset);
                    this.mBuffer.position(offset + 4);
                    obj = decodeObject();
                } catch (IllegalArgumentException e) {
                    Log.wtf(EventLog.TAG, "Illegal entry payload: tag=" + getTag(), e);
                } catch (BufferUnderflowException e2) {
                    Log.wtf(EventLog.TAG, "Truncated entry payload: tag=" + getTag(), e2);
                }
            }
            return obj;
        }

        private Object decodeObject() {
            byte type = this.mBuffer.get();
            switch (type) {
                case 0:
                    return Integer.valueOf(this.mBuffer.getInt());
                case 1:
                    return Long.valueOf(this.mBuffer.getLong());
                case 2:
                    try {
                        int length = this.mBuffer.getInt();
                        int start = this.mBuffer.position();
                        this.mBuffer.position(start + length);
                        return new String(this.mBuffer.array(), start, length, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        Log.wtf(EventLog.TAG, "UTF-8 is not supported", e);
                        return null;
                    }
                case 3:
                    int length2 = this.mBuffer.get();
                    if (length2 < 0) {
                        length2 += 256;
                    }
                    Object[] array = new Object[length2];
                    for (int i = 0; i < length2; i++) {
                        array[i] = decodeObject();
                    }
                    return array;
                default:
                    throw new IllegalArgumentException("Unknown entry type: " + ((int) type));
            }
        }
    }

    public static String getTagName(int tag) {
        readTagsFile();
        return sTagNames.get(Integer.valueOf(tag));
    }

    public static int getTagCode(String name) {
        readTagsFile();
        Integer code = sTagCodes.get(name);
        if (code != null) {
            return code.intValue();
        }
        return -1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0074 A[SYNTHETIC, Splitter:B:26:0x0074] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00b9 A[SYNTHETIC, Splitter:B:37:0x00b9] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static synchronized void readTagsFile() {
        /*
        // Method dump skipped, instructions count: 210
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.EventLog.readTagsFile():void");
    }
}
