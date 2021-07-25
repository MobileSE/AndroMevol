package com.android.internal.widget;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.ArrayMap;
import com.android.internal.widget.ILockSettingsObserver;

public class LockPatternUtilsCache implements ILockSettings {
    private static final String HAS_LOCK_PASSWORD_CACHE_KEY = "LockPatternUtils.Cache.HasLockPasswordCacheKey";
    private static final String HAS_LOCK_PATTERN_CACHE_KEY = "LockPatternUtils.Cache.HasLockPatternCacheKey";
    private static LockPatternUtilsCache sInstance;
    private final ArrayMap<CacheKey, Object> mCache = new ArrayMap<>();
    private final CacheKey mCacheKey = new CacheKey();
    private final ILockSettingsObserver mObserver = new ILockSettingsObserver.Stub() {
        /* class com.android.internal.widget.LockPatternUtilsCache.AnonymousClass1 */

        @Override // com.android.internal.widget.ILockSettingsObserver
        public void onLockSettingChanged(String key, int userId) throws RemoteException {
            LockPatternUtilsCache.this.invalidateCache(key, userId);
        }
    };
    private final ILockSettings mService;

    public static synchronized LockPatternUtilsCache getInstance(ILockSettings service) {
        LockPatternUtilsCache lockPatternUtilsCache;
        synchronized (LockPatternUtilsCache.class) {
            if (sInstance == null) {
                sInstance = new LockPatternUtilsCache(service);
            }
            lockPatternUtilsCache = sInstance;
        }
        return lockPatternUtilsCache;
    }

    private LockPatternUtilsCache(ILockSettings service) {
        this.mService = service;
        try {
            service.registerObserver(this.mObserver);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override // com.android.internal.widget.ILockSettings
    public void setBoolean(String key, boolean value, int userId) throws RemoteException {
        invalidateCache(key, userId);
        this.mService.setBoolean(key, value, userId);
        putCache(key, userId, Boolean.valueOf(value));
    }

    @Override // com.android.internal.widget.ILockSettings
    public void setLong(String key, long value, int userId) throws RemoteException {
        invalidateCache(key, userId);
        this.mService.setLong(key, value, userId);
        putCache(key, userId, Long.valueOf(value));
    }

    @Override // com.android.internal.widget.ILockSettings
    public void setString(String key, String value, int userId) throws RemoteException {
        invalidateCache(key, userId);
        this.mService.setString(key, value, userId);
        putCache(key, userId, value);
    }

    @Override // com.android.internal.widget.ILockSettings
    public long getLong(String key, long defaultValue, int userId) throws RemoteException {
        Object value = peekCache(key, userId);
        if (value instanceof Long) {
            return ((Long) value).longValue();
        }
        long result = this.mService.getLong(key, defaultValue, userId);
        putCache(key, userId, Long.valueOf(result));
        return result;
    }

    @Override // com.android.internal.widget.ILockSettings
    public String getString(String key, String defaultValue, int userId) throws RemoteException {
        Object value = peekCache(key, userId);
        if (value instanceof String) {
            return (String) value;
        }
        String result = this.mService.getString(key, defaultValue, userId);
        putCache(key, userId, result);
        return result;
    }

    @Override // com.android.internal.widget.ILockSettings
    public boolean getBoolean(String key, boolean defaultValue, int userId) throws RemoteException {
        Object value = peekCache(key, userId);
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        boolean result = this.mService.getBoolean(key, defaultValue, userId);
        putCache(key, userId, Boolean.valueOf(result));
        return result;
    }

    @Override // com.android.internal.widget.ILockSettings
    public void setLockPattern(String pattern, int userId) throws RemoteException {
        invalidateCache(HAS_LOCK_PATTERN_CACHE_KEY, userId);
        this.mService.setLockPattern(pattern, userId);
        putCache(HAS_LOCK_PATTERN_CACHE_KEY, userId, Boolean.valueOf(pattern != null));
    }

    @Override // com.android.internal.widget.ILockSettings
    public boolean checkPattern(String pattern, int userId) throws RemoteException {
        return this.mService.checkPattern(pattern, userId);
    }

    @Override // com.android.internal.widget.ILockSettings
    public void setLockPassword(String password, int userId) throws RemoteException {
        invalidateCache(HAS_LOCK_PASSWORD_CACHE_KEY, userId);
        this.mService.setLockPassword(password, userId);
        putCache(HAS_LOCK_PASSWORD_CACHE_KEY, userId, Boolean.valueOf(password != null));
    }

    @Override // com.android.internal.widget.ILockSettings
    public boolean checkPassword(String password, int userId) throws RemoteException {
        return this.mService.checkPassword(password, userId);
    }

    @Override // com.android.internal.widget.ILockSettings
    public boolean checkVoldPassword(int userId) throws RemoteException {
        return this.mService.checkVoldPassword(userId);
    }

    @Override // com.android.internal.widget.ILockSettings
    public boolean havePattern(int userId) throws RemoteException {
        Object value = peekCache(HAS_LOCK_PATTERN_CACHE_KEY, userId);
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        boolean result = this.mService.havePattern(userId);
        putCache(HAS_LOCK_PATTERN_CACHE_KEY, userId, Boolean.valueOf(result));
        return result;
    }

    @Override // com.android.internal.widget.ILockSettings
    public boolean havePassword(int userId) throws RemoteException {
        Object value = peekCache(HAS_LOCK_PASSWORD_CACHE_KEY, userId);
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        boolean result = this.mService.havePassword(userId);
        putCache(HAS_LOCK_PASSWORD_CACHE_KEY, userId, Boolean.valueOf(result));
        return result;
    }

    @Override // com.android.internal.widget.ILockSettings
    public void removeUser(int userId) throws RemoteException {
        this.mService.removeUser(userId);
    }

    @Override // com.android.internal.widget.ILockSettings
    public void registerObserver(ILockSettingsObserver observer) throws RemoteException {
        this.mService.registerObserver(observer);
    }

    @Override // com.android.internal.widget.ILockSettings
    public void unregisterObserver(ILockSettingsObserver observer) throws RemoteException {
        this.mService.unregisterObserver(observer);
    }

    public IBinder asBinder() {
        return this.mService.asBinder();
    }

    private Object peekCache(String key, int userId) {
        Object obj;
        synchronized (this.mCache) {
            obj = this.mCache.get(this.mCacheKey.set(key, userId));
        }
        return obj;
    }

    private void putCache(String key, int userId, Object value) {
        synchronized (this.mCache) {
            this.mCache.put(new CacheKey().set(key, userId), value);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void invalidateCache(String key, int userId) {
        synchronized (this.mCache) {
            this.mCache.remove(this.mCacheKey.set(key, userId));
        }
    }

    /* access modifiers changed from: private */
    public static final class CacheKey {
        String key;
        int userId;

        private CacheKey() {
        }

        public CacheKey set(String key2, int userId2) {
            this.key = key2;
            this.userId = userId2;
            return this;
        }

        public CacheKey copy() {
            return new CacheKey().set(this.key, this.userId);
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof CacheKey)) {
                return false;
            }
            CacheKey o = (CacheKey) obj;
            if (this.userId != o.userId || !this.key.equals(o.key)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return this.key.hashCode() ^ this.userId;
        }
    }
}
