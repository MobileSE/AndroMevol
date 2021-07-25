package android.util;

import android.os.BatteryStats;
import java.io.UnsupportedEncodingException;

public class Base64 {
    static final /* synthetic */ boolean $assertionsDisabled = (!Base64.class.desiredAssertionStatus());
    public static final int CRLF = 4;
    public static final int DEFAULT = 0;
    public static final int NO_CLOSE = 16;
    public static final int NO_PADDING = 1;
    public static final int NO_WRAP = 2;
    public static final int URL_SAFE = 8;

    static abstract class Coder {
        public int op;
        public byte[] output;

        public abstract int maxOutputSize(int i);

        public abstract boolean process(byte[] bArr, int i, int i2, boolean z);

        Coder() {
        }
    }

    public static byte[] decode(String str, int flags) {
        return decode(str.getBytes(), flags);
    }

    public static byte[] decode(byte[] input, int flags) {
        return decode(input, 0, input.length, flags);
    }

    public static byte[] decode(byte[] input, int offset, int len, int flags) {
        Decoder decoder = new Decoder(flags, new byte[((len * 3) / 4)]);
        if (!decoder.process(input, offset, len, true)) {
            throw new IllegalArgumentException("bad base-64");
        } else if (decoder.op == decoder.output.length) {
            return decoder.output;
        } else {
            byte[] temp = new byte[decoder.op];
            System.arraycopy(decoder.output, 0, temp, 0, decoder.op);
            return temp;
        }
    }

    /* access modifiers changed from: package-private */
    public static class Decoder extends Coder {
        private static final int[] DECODE = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        private static final int[] DECODE_WEBSAFE = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        private static final int EQUALS = -2;
        private static final int SKIP = -1;
        private final int[] alphabet;
        private int state;
        private int value;

        public Decoder(int flags, byte[] output) {
            this.output = output;
            this.alphabet = (flags & 8) == 0 ? DECODE : DECODE_WEBSAFE;
            this.state = 0;
            this.value = 0;
        }

        @Override // android.util.Base64.Coder
        public int maxOutputSize(int len) {
            return ((len * 3) / 4) + 10;
        }

        @Override // android.util.Base64.Coder
        public boolean process(byte[] input, int offset, int len, boolean finish) {
            int op;
            int op2;
            if (this.state == 6) {
                return false;
            }
            int p = offset;
            int len2 = len + offset;
            int state2 = this.state;
            int value2 = this.value;
            int op3 = 0;
            byte[] output = this.output;
            int[] alphabet2 = this.alphabet;
            while (true) {
                if (p < len2) {
                    if (state2 == 0) {
                        while (p + 4 <= len2 && (value2 = (alphabet2[input[p] & BatteryStats.HistoryItem.CMD_NULL] << 18) | (alphabet2[input[p + 1] & BatteryStats.HistoryItem.CMD_NULL] << 12) | (alphabet2[input[p + 2] & BatteryStats.HistoryItem.CMD_NULL] << 6) | alphabet2[input[p + 3] & BatteryStats.HistoryItem.CMD_NULL]) >= 0) {
                            output[op3 + 2] = (byte) value2;
                            output[op3 + 1] = (byte) (value2 >> 8);
                            output[op3] = (byte) (value2 >> 16);
                            op3 += 3;
                            p += 4;
                        }
                        if (p >= len2) {
                            op = op3;
                        }
                    }
                    int p2 = p + 1;
                    int d = alphabet2[input[p] & BatteryStats.HistoryItem.CMD_NULL];
                    switch (state2) {
                        case 0:
                            if (d < 0) {
                                if (d == -1) {
                                    break;
                                } else {
                                    this.state = 6;
                                    return false;
                                }
                            } else {
                                value2 = d;
                                state2++;
                                break;
                            }
                        case 1:
                            if (d < 0) {
                                if (d == -1) {
                                    break;
                                } else {
                                    this.state = 6;
                                    return false;
                                }
                            } else {
                                value2 = (value2 << 6) | d;
                                state2++;
                                break;
                            }
                        case 2:
                            if (d < 0) {
                                if (d != -2) {
                                    if (d == -1) {
                                        break;
                                    } else {
                                        this.state = 6;
                                        return false;
                                    }
                                } else {
                                    output[op3] = (byte) (value2 >> 4);
                                    state2 = 4;
                                    op3++;
                                    break;
                                }
                            } else {
                                value2 = (value2 << 6) | d;
                                state2++;
                                break;
                            }
                        case 3:
                            if (d < 0) {
                                if (d != -2) {
                                    if (d == -1) {
                                        break;
                                    } else {
                                        this.state = 6;
                                        return false;
                                    }
                                } else {
                                    output[op3 + 1] = (byte) (value2 >> 2);
                                    output[op3] = (byte) (value2 >> 10);
                                    op3 += 2;
                                    state2 = 5;
                                    break;
                                }
                            } else {
                                value2 = (value2 << 6) | d;
                                output[op3 + 2] = (byte) value2;
                                output[op3 + 1] = (byte) (value2 >> 8);
                                output[op3] = (byte) (value2 >> 16);
                                op3 += 3;
                                state2 = 0;
                                break;
                            }
                        case 4:
                            if (d != -2) {
                                if (d == -1) {
                                    break;
                                } else {
                                    this.state = 6;
                                    return false;
                                }
                            } else {
                                state2++;
                                break;
                            }
                        case 5:
                            if (d == -1) {
                                break;
                            } else {
                                this.state = 6;
                                return false;
                            }
                    }
                    p = p2;
                } else {
                    op = op3;
                }
            }
            if (!finish) {
                this.state = state2;
                this.value = value2;
                this.op = op;
                return true;
            }
            switch (state2) {
                case 0:
                    op2 = op;
                    break;
                case 1:
                    this.state = 6;
                    return false;
                case 2:
                    op2 = op + 1;
                    output[op] = (byte) (value2 >> 4);
                    break;
                case 3:
                    int op4 = op + 1;
                    output[op] = (byte) (value2 >> 10);
                    output[op4] = (byte) (value2 >> 2);
                    op2 = op4 + 1;
                    break;
                case 4:
                    this.state = 6;
                    return false;
                default:
                    op2 = op;
                    break;
            }
            this.state = state2;
            this.op = op2;
            return true;
        }
    }

    public static String encodeToString(byte[] input, int flags) {
        try {
            return new String(encode(input, flags), "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    public static String encodeToString(byte[] input, int offset, int len, int flags) {
        try {
            return new String(encode(input, offset, len, flags), "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    public static byte[] encode(byte[] input, int flags) {
        return encode(input, 0, input.length, flags);
    }

    public static byte[] encode(byte[] input, int offset, int len, int flags) {
        int i;
        Encoder encoder = new Encoder(flags, null);
        int output_len = (len / 3) * 4;
        if (!encoder.do_padding) {
            switch (len % 3) {
                case 1:
                    output_len += 2;
                    break;
                case 2:
                    output_len += 3;
                    break;
            }
        } else if (len % 3 > 0) {
            output_len += 4;
        }
        if (encoder.do_newline && len > 0) {
            int i2 = ((len - 1) / 57) + 1;
            if (encoder.do_cr) {
                i = 2;
            } else {
                i = 1;
            }
            output_len += i * i2;
        }
        encoder.output = new byte[output_len];
        encoder.process(input, offset, len, true);
        if ($assertionsDisabled || encoder.op == output_len) {
            return encoder.output;
        }
        throw new AssertionError();
    }

    /* access modifiers changed from: package-private */
    public static class Encoder extends Coder {
        static final /* synthetic */ boolean $assertionsDisabled;
        private static final byte[] ENCODE = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};
        private static final byte[] ENCODE_WEBSAFE = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 45, 95};
        public static final int LINE_GROUPS = 19;
        private final byte[] alphabet;
        private int count;
        public final boolean do_cr;
        public final boolean do_newline;
        public final boolean do_padding;
        private final byte[] tail;
        int tailLen;

        static {
            boolean z;
            if (!Base64.class.desiredAssertionStatus()) {
                z = true;
            } else {
                z = false;
            }
            $assertionsDisabled = z;
        }

        public Encoder(int flags, byte[] output) {
            boolean z;
            boolean z2 = true;
            this.output = output;
            this.do_padding = (flags & 1) == 0;
            if ((flags & 2) == 0) {
                z = true;
            } else {
                z = false;
            }
            this.do_newline = z;
            this.do_cr = (flags & 4) == 0 ? false : z2;
            this.alphabet = (flags & 8) == 0 ? ENCODE : ENCODE_WEBSAFE;
            this.tail = new byte[2];
            this.tailLen = 0;
            this.count = this.do_newline ? 19 : -1;
        }

        @Override // android.util.Base64.Coder
        public int maxOutputSize(int len) {
            return ((len * 8) / 5) + 10;
        }

        /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
            jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
            	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
            	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
            	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
            */
        /* JADX WARNING: Removed duplicated region for block: B:12:0x0058  */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x00fe  */
        /* JADX WARNING: Removed duplicated region for block: B:42:0x0159  */
        /* JADX WARNING: Removed duplicated region for block: B:82:0x020c  */
        /* JADX WARNING: Removed duplicated region for block: B:93:0x00fc A[SYNTHETIC] */
        @Override // android.util.Base64.Coder
        public boolean process(byte[] r15, int r16, int r17, boolean r18) {
            /*
            // Method dump skipped, instructions count: 598
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.Base64.Encoder.process(byte[], int, int, boolean):boolean");
        }
    }

    private Base64() {
    }
}
