package android.alsa;

import android.net.ProxyInfo;

public class LineTokenizer {
    public static final int kTokenNotFound = -1;
    private String mDelimiters = ProxyInfo.LOCAL_EXCL_LIST;

    public LineTokenizer(String delimiters) {
        this.mDelimiters = delimiters;
    }

    /* access modifiers changed from: package-private */
    public int nextToken(String line, int startIndex) {
        int len = line.length();
        int offset = startIndex;
        while (offset < len && this.mDelimiters.indexOf(line.charAt(offset)) != -1) {
            offset++;
        }
        if (offset < len) {
            return offset;
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public int nextDelimiter(String line, int startIndex) {
        int len = line.length();
        int offset = startIndex;
        while (offset < len && this.mDelimiters.indexOf(line.charAt(offset)) == -1) {
            offset++;
        }
        if (offset < len) {
            return offset;
        }
        return -1;
    }
}
