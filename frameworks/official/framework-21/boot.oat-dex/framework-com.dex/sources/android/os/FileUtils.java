package android.os;

import android.net.ProxyInfo;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

public class FileUtils {
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("[\\w%+,./=_-]+");
    public static final int S_IRGRP = 32;
    public static final int S_IROTH = 4;
    public static final int S_IRUSR = 256;
    public static final int S_IRWXG = 56;
    public static final int S_IRWXO = 7;
    public static final int S_IRWXU = 448;
    public static final int S_IWGRP = 16;
    public static final int S_IWOTH = 2;
    public static final int S_IWUSR = 128;
    public static final int S_IXGRP = 8;
    public static final int S_IXOTH = 1;
    public static final int S_IXUSR = 64;
    private static final String TAG = "FileUtils";

    public static int setPermissions(File path, int mode, int uid, int gid) {
        return setPermissions(path.getAbsolutePath(), mode, uid, gid);
    }

    public static int setPermissions(String path, int mode, int uid, int gid) {
        try {
            Os.chmod(path, mode);
            if (uid >= 0 || gid >= 0) {
                try {
                    Os.chown(path, uid, gid);
                } catch (ErrnoException e) {
                    Slog.w(TAG, "Failed to chown(" + path + "): " + e);
                    return e.errno;
                }
            }
            return 0;
        } catch (ErrnoException e2) {
            Slog.w(TAG, "Failed to chmod(" + path + "): " + e2);
            return e2.errno;
        }
    }

    public static int setPermissions(FileDescriptor fd, int mode, int uid, int gid) {
        try {
            Os.fchmod(fd, mode);
            if (uid >= 0 || gid >= 0) {
                try {
                    Os.fchown(fd, uid, gid);
                } catch (ErrnoException e) {
                    Slog.w(TAG, "Failed to fchown(): " + e);
                    return e.errno;
                }
            }
            return 0;
        } catch (ErrnoException e2) {
            Slog.w(TAG, "Failed to fchmod(): " + e2);
            return e2.errno;
        }
    }

    public static int getUid(String path) {
        try {
            return Os.stat(path).st_uid;
        } catch (ErrnoException e) {
            return -1;
        }
    }

    public static boolean sync(FileOutputStream stream) {
        if (stream != null) {
            try {
                stream.getFD().sync();
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean copyFile(File srcFile, File destFile) {
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                return copyToFile(in, destFile);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            return false;
        }
    }

    /* JADX INFO: finally extract failed */
    public static boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            if (destFile.exists()) {
                destFile.delete();
            }
            FileOutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                while (true) {
                    int bytesRead = inputStream.read(buffer);
                    if (bytesRead < 0) {
                        break;
                    }
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
                try {
                    out.getFD().sync();
                } catch (IOException e) {
                }
                out.close();
                return true;
            } catch (Throwable th) {
                out.flush();
                try {
                    out.getFD().sync();
                } catch (IOException e2) {
                }
                out.close();
                throw th;
            }
        } catch (IOException e3) {
            return false;
        }
    }

    public static boolean isFilenameSafe(File file) {
        return SAFE_FILENAME_PATTERN.matcher(file.getPath()).matches();
    }

    public static String readTextFile(File file, int max, String ellipsis) throws IOException {
        int len;
        int len2;
        InputStream input = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(input);
        try {
            long size = file.length();
            if (max > 0 || (size > 0 && max == 0)) {
                if (size > 0 && (max == 0 || size < ((long) max))) {
                    max = (int) size;
                }
                byte[] data = new byte[(max + 1)];
                int length = bis.read(data);
                if (length <= 0) {
                    return ProxyInfo.LOCAL_EXCL_LIST;
                }
                if (length <= max) {
                    String str = new String(data, 0, length);
                    bis.close();
                    input.close();
                    return str;
                } else if (ellipsis == null) {
                    String str2 = new String(data, 0, max);
                    bis.close();
                    input.close();
                    return str2;
                } else {
                    String str3 = new String(data, 0, max) + ellipsis;
                    bis.close();
                    input.close();
                    return str3;
                }
            } else if (max < 0) {
                boolean rolled = false;
                byte[] last = null;
                byte[] data2 = null;
                do {
                    if (last != null) {
                        rolled = true;
                    }
                    last = data2;
                    data2 = last;
                    if (data2 == null) {
                        data2 = new byte[(-max)];
                    }
                    len2 = bis.read(data2);
                } while (len2 == data2.length);
                if (last == null && len2 <= 0) {
                    bis.close();
                    input.close();
                    return ProxyInfo.LOCAL_EXCL_LIST;
                } else if (last == null) {
                    String str4 = new String(data2, 0, len2);
                    bis.close();
                    input.close();
                    return str4;
                } else {
                    if (len2 > 0) {
                        rolled = true;
                        System.arraycopy(last, len2, last, 0, last.length - len2);
                        System.arraycopy(data2, 0, last, last.length - len2, len2);
                    }
                    if (ellipsis == null || !rolled) {
                        String str5 = new String(last);
                        bis.close();
                        input.close();
                        return str5;
                    }
                    String str6 = ellipsis + new String(last);
                    bis.close();
                    input.close();
                    return str6;
                }
            } else {
                ByteArrayOutputStream contents = new ByteArrayOutputStream();
                byte[] data3 = new byte[1024];
                do {
                    len = bis.read(data3);
                    if (len > 0) {
                        contents.write(data3, 0, len);
                    }
                } while (len == data3.length);
                String byteArrayOutputStream = contents.toString();
                bis.close();
                input.close();
                return byteArrayOutputStream;
            }
        } finally {
            bis.close();
            input.close();
        }
    }

    public static void stringToFile(String filename, String string) throws IOException {
        FileWriter out = new FileWriter(filename);
        try {
            out.write(string);
        } finally {
            out.close();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0027 A[SYNTHETIC, Splitter:B:15:0x0027] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long checksumCrc32(java.io.File r7) throws java.io.FileNotFoundException, java.io.IOException {
        /*
            java.util.zip.CRC32 r1 = new java.util.zip.CRC32
            r1.<init>()
            r2 = 0
            java.util.zip.CheckedInputStream r3 = new java.util.zip.CheckedInputStream     // Catch:{ all -> 0x0024 }
            java.io.FileInputStream r4 = new java.io.FileInputStream     // Catch:{ all -> 0x0024 }
            r4.<init>(r7)     // Catch:{ all -> 0x0024 }
            r3.<init>(r4, r1)     // Catch:{ all -> 0x0024 }
            r4 = 128(0x80, float:1.794E-43)
            byte[] r0 = new byte[r4]     // Catch:{ all -> 0x002f }
        L_0x0014:
            int r4 = r3.read(r0)     // Catch:{ all -> 0x002f }
            if (r4 >= 0) goto L_0x0014
            long r4 = r1.getValue()     // Catch:{ all -> 0x002f }
            if (r3 == 0) goto L_0x0023
            r3.close()     // Catch:{ IOException -> 0x002b }
        L_0x0023:
            return r4
        L_0x0024:
            r4 = move-exception
        L_0x0025:
            if (r2 == 0) goto L_0x002a
            r2.close()     // Catch:{ IOException -> 0x002d }
        L_0x002a:
            throw r4
        L_0x002b:
            r6 = move-exception
            goto L_0x0023
        L_0x002d:
            r5 = move-exception
            goto L_0x002a
        L_0x002f:
            r4 = move-exception
            r2 = r3
            goto L_0x0025
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.FileUtils.checksumCrc32(java.io.File):long");
    }

    public static boolean deleteOlderFiles(File dir, int minCount, long minAge) {
        if (minCount < 0 || minAge < 0) {
            throw new IllegalArgumentException("Constraints must be positive or 0");
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return false;
        }
        Arrays.sort(files, new Comparator<File>() {
            /* class android.os.FileUtils.AnonymousClass1 */

            public int compare(File lhs, File rhs) {
                return (int) (rhs.lastModified() - lhs.lastModified());
            }
        });
        boolean deleted = false;
        for (int i = minCount; i < files.length; i++) {
            File file = files[i];
            if (System.currentTimeMillis() - file.lastModified() > minAge && file.delete()) {
                Log.d(TAG, "Deleted old file " + file);
                deleted = true;
            }
        }
        return deleted;
    }

    public static boolean contains(File dir, File file) {
        if (file == null) {
            return false;
        }
        String dirPath = dir.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        if (dirPath.equals(filePath)) {
            return true;
        }
        if (!dirPath.endsWith("/")) {
            dirPath = dirPath + "/";
        }
        return filePath.startsWith(dirPath);
    }

    public static boolean deleteContents(File dir) {
        File[] files = dir.listFiles();
        boolean success = true;
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    success &= deleteContents(file);
                }
                if (!file.delete()) {
                    Log.w(TAG, "Failed to delete " + file);
                    success = false;
                }
            }
        }
        return success;
    }

    public static boolean isValidExtFilename(String name) {
        if (TextUtils.isEmpty(name) || ".".equals(name) || "..".equals(name)) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == 0 || c == '/') {
                return false;
            }
        }
        return true;
    }

    public static String rewriteAfterRename(File beforeDir, File afterDir, String path) {
        File result;
        if (path == null || (result = rewriteAfterRename(beforeDir, afterDir, new File(path))) == null) {
            return null;
        }
        return result.getAbsolutePath();
    }

    public static String[] rewriteAfterRename(File beforeDir, File afterDir, String[] paths) {
        if (paths == null) {
            return null;
        }
        String[] result = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
            result[i] = rewriteAfterRename(beforeDir, afterDir, paths[i]);
        }
        return result;
    }

    public static File rewriteAfterRename(File beforeDir, File afterDir, File file) {
        if (file != null && contains(beforeDir, file)) {
            return new File(afterDir, file.getAbsolutePath().substring(beforeDir.getAbsolutePath().length()));
        }
        return null;
    }
}
