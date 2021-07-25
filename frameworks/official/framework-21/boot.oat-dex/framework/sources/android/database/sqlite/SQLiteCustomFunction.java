package android.database.sqlite;

import android.database.sqlite.SQLiteDatabase;

public final class SQLiteCustomFunction {
    public final SQLiteDatabase.CustomFunction callback;
    public final String name;
    public final int numArgs;

    public SQLiteCustomFunction(String name2, int numArgs2, SQLiteDatabase.CustomFunction callback2) {
        if (name2 == null) {
            throw new IllegalArgumentException("name must not be null.");
        }
        this.name = name2;
        this.numArgs = numArgs2;
        this.callback = callback2;
    }

    private void dispatchCallback(String[] args) {
        this.callback.callback(args);
    }
}
