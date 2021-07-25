package android.text;

import android.text.Layout;

/* access modifiers changed from: package-private */
public class AndroidBidi {
    private static native int runBidi(int i, char[] cArr, byte[] bArr, int i2, boolean z);

    AndroidBidi() {
    }

    public static int bidi(int dir, char[] chs, byte[] chInfo, int n, boolean haveInfo) {
        int dir2;
        if (chs == null || chInfo == null) {
            throw new NullPointerException();
        } else if (n < 0 || chs.length < n || chInfo.length < n) {
            throw new IndexOutOfBoundsException();
        } else {
            switch (dir) {
                case -2:
                    dir2 = -1;
                    break;
                case -1:
                    dir2 = 1;
                    break;
                case 0:
                default:
                    dir2 = 0;
                    break;
                case 1:
                    dir2 = 0;
                    break;
                case 2:
                    dir2 = -2;
                    break;
            }
            return (runBidi(dir2, chs, chInfo, n, haveInfo) & 1) == 0 ? 1 : -1;
        }
    }

    public static Layout.Directions directions(int dir, byte[] levels, int lstart, char[] chars, int cstart, int len) {
        boolean swap;
        if (len == 0) {
            return Layout.DIRS_ALL_LEFT_TO_RIGHT;
        }
        int baseLevel = dir == 1 ? 0 : 1;
        byte b = levels[lstart];
        int minLevel = b;
        int runCount = 1;
        int e = lstart + len;
        for (int i = lstart + 1; i < e; i++) {
            byte b2 = levels[i];
            if (b2 != b) {
                b = b2;
                runCount++;
            }
        }
        int visLen = len;
        if ((b & 1) != (baseLevel & 1)) {
            while (true) {
                visLen--;
                if (visLen < 0) {
                    break;
                }
                char ch = chars[cstart + visLen];
                if (ch == '\n') {
                    visLen--;
                    break;
                }
                if (!(ch == ' ' || ch == '\t')) {
                    break;
                }
            }
            visLen++;
            if (visLen != len) {
                runCount++;
            }
        }
        if (runCount != 1 || minLevel != baseLevel) {
            int[] ld = new int[(runCount * 2)];
            int maxLevel = minLevel;
            int levelBits = minLevel << 26;
            int prev = lstart;
            int curLevel = minLevel;
            int i2 = lstart;
            int e2 = lstart + visLen;
            int n = 1;
            while (i2 < e2) {
                byte b3 = levels[i2];
                if (b3 != curLevel) {
                    curLevel = b3;
                    if (b3 > maxLevel) {
                        maxLevel = b3;
                    } else if (b3 < minLevel) {
                        minLevel = b3;
                    }
                    int n2 = n + 1;
                    ld[n] = (i2 - prev) | levelBits;
                    n = n2 + 1;
                    ld[n2] = i2 - lstart;
                    levelBits = curLevel << 26;
                    prev = i2;
                }
                i2++;
                n = n;
            }
            ld[n] = ((lstart + visLen) - prev) | levelBits;
            if (visLen < len) {
                int n3 = n + 1;
                ld[n3] = visLen;
                ld[n3 + 1] = (len - visLen) | (baseLevel << 26);
            }
            if ((minLevel & 1) == baseLevel) {
                minLevel++;
                swap = maxLevel > minLevel;
            } else {
                swap = runCount > 1;
            }
            if (swap) {
                int level = maxLevel - 1;
                while (level >= minLevel) {
                    int i3 = 0;
                    while (i3 < ld.length) {
                        if (levels[ld[i3]] >= level) {
                            int e3 = i3 + 2;
                            while (e3 < ld.length && levels[ld[e3]] >= level) {
                                e3 += 2;
                            }
                            int low = i3;
                            for (int hi = e3 - 2; low < hi; hi -= 2) {
                                int x = ld[low];
                                ld[low] = ld[hi];
                                ld[hi] = x;
                                int x2 = ld[low + 1];
                                ld[low + 1] = ld[hi + 1];
                                ld[hi + 1] = x2;
                                low += 2;
                            }
                            i3 = e3 + 2;
                        }
                        i3 += 2;
                    }
                    level--;
                }
            }
            return new Layout.Directions(ld);
        } else if ((minLevel & 1) != 0) {
            return Layout.DIRS_ALL_RIGHT_TO_LEFT;
        } else {
            return Layout.DIRS_ALL_LEFT_TO_RIGHT;
        }
    }
}
