package android.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseConfiguration;
import android.util.Log;
import java.io.File;

public final class DefaultDatabaseErrorHandler implements DatabaseErrorHandler {
    private static final String TAG = "DefaultDatabaseErrorHandler";

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0054, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0056, code lost:
        if (r0 != null) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0058, code lost:
        r1 = r0.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0060, code lost:
        if (r1.hasNext() != false) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0062, code lost:
        deleteDatabaseFile(r1.next().second);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0070, code lost:
        deleteDatabaseFile(r7.getPath());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0077, code lost:
        throw r3;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0054 A[ExcHandler: all (r3v3 'th' java.lang.Throwable A[CUSTOM_DECLARE]), PHI: r0 
      PHI: (r0v2 'attachedDbs' java.util.List<android.util.Pair<java.lang.String, java.lang.String>>) = (r0v0 'attachedDbs' java.util.List<android.util.Pair<java.lang.String, java.lang.String>>), (r0v3 'attachedDbs' java.util.List<android.util.Pair<java.lang.String, java.lang.String>>), (r0v3 'attachedDbs' java.util.List<android.util.Pair<java.lang.String, java.lang.String>>) binds: [B:4:0x002b, B:6:0x002f, B:7:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:4:0x002b] */
    @Override // android.database.DatabaseErrorHandler
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onCorruption(android.database.sqlite.SQLiteDatabase r7) {
        /*
        // Method dump skipped, instructions count: 124
        */
        throw new UnsupportedOperationException("Method not decompiled: android.database.DefaultDatabaseErrorHandler.onCorruption(android.database.sqlite.SQLiteDatabase):void");
    }

    private void deleteDatabaseFile(String fileName) {
        if (!fileName.equalsIgnoreCase(SQLiteDatabaseConfiguration.MEMORY_DB_PATH) && fileName.trim().length() != 0) {
            Log.e(TAG, "deleting the database file: " + fileName);
            try {
                SQLiteDatabase.deleteDatabase(new File(fileName));
            } catch (Exception e) {
                Log.w(TAG, "delete failed: " + e.getMessage());
            }
        }
    }
}
