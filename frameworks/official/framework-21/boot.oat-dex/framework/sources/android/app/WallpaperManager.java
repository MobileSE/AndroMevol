package android.app;

import android.app.IWallpaperManager;
import android.app.IWallpaperManagerCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManagerGlobal;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class WallpaperManager {
    public static final String ACTION_CHANGE_LIVE_WALLPAPER = "android.service.wallpaper.CHANGE_LIVE_WALLPAPER";
    public static final String ACTION_CROP_AND_SET_WALLPAPER = "android.service.wallpaper.CROP_AND_SET_WALLPAPER";
    public static final String ACTION_LIVE_WALLPAPER_CHOOSER = "android.service.wallpaper.LIVE_WALLPAPER_CHOOSER";
    public static final String COMMAND_DROP = "android.home.drop";
    public static final String COMMAND_SECONDARY_TAP = "android.wallpaper.secondaryTap";
    public static final String COMMAND_TAP = "android.wallpaper.tap";
    private static boolean DEBUG = false;
    public static final String EXTRA_LIVE_WALLPAPER_COMPONENT = "android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT";
    private static final String PROP_WALLPAPER = "ro.config.wallpaper";
    private static final String PROP_WALLPAPER_COMPONENT = "ro.config.wallpaper_component";
    private static String TAG = "WallpaperManager";
    public static final String WALLPAPER_PREVIEW_META_DATA = "android.wallpaper.preview";
    private static Globals sGlobals;
    private static final Object sSync = new Object[0];
    private final Context mContext;
    private float mWallpaperXStep = -1.0f;
    private float mWallpaperYStep = -1.0f;

    static class FastBitmapDrawable extends Drawable {
        private final Bitmap mBitmap;
        private int mDrawLeft;
        private int mDrawTop;
        private final int mHeight;
        private final Paint mPaint;
        private final int mWidth;

        private FastBitmapDrawable(Bitmap bitmap) {
            this.mBitmap = bitmap;
            this.mWidth = bitmap.getWidth();
            this.mHeight = bitmap.getHeight();
            setBounds(0, 0, this.mWidth, this.mHeight);
            this.mPaint = new Paint();
            this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            canvas.drawBitmap(this.mBitmap, (float) this.mDrawLeft, (float) this.mDrawTop, this.mPaint);
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -1;
        }

        @Override // android.graphics.drawable.Drawable
        public void setBounds(int left, int top, int right, int bottom) {
            this.mDrawLeft = (((right - left) - this.mWidth) / 2) + left;
            this.mDrawTop = (((bottom - top) - this.mHeight) / 2) + top;
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int alpha) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter cf) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        @Override // android.graphics.drawable.Drawable
        public void setDither(boolean dither) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        @Override // android.graphics.drawable.Drawable
        public void setFilterBitmap(boolean filter) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            return this.mWidth;
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            return this.mHeight;
        }

        @Override // android.graphics.drawable.Drawable
        public int getMinimumWidth() {
            return this.mWidth;
        }

        @Override // android.graphics.drawable.Drawable
        public int getMinimumHeight() {
            return this.mHeight;
        }
    }

    /* access modifiers changed from: package-private */
    public static class Globals extends IWallpaperManagerCallback.Stub {
        private static final int MSG_CLEAR_WALLPAPER = 1;
        private Bitmap mDefaultWallpaper;
        private IWallpaperManager mService = IWallpaperManager.Stub.asInterface(ServiceManager.getService(Context.WALLPAPER_SERVICE));
        private Bitmap mWallpaper;

        Globals(Looper looper) {
        }

        @Override // android.app.IWallpaperManagerCallback
        public void onWallpaperChanged() {
            synchronized (this) {
                this.mWallpaper = null;
                this.mDefaultWallpaper = null;
            }
        }

        public Bitmap peekWallpaperBitmap(Context context, boolean returnDefault) {
            Bitmap bitmap;
            synchronized (this) {
                if (this.mWallpaper != null) {
                    bitmap = this.mWallpaper;
                } else if (this.mDefaultWallpaper != null) {
                    bitmap = this.mDefaultWallpaper;
                } else {
                    this.mWallpaper = null;
                    try {
                        this.mWallpaper = getCurrentWallpaperLocked(context);
                    } catch (OutOfMemoryError e) {
                        Log.w(WallpaperManager.TAG, "No memory load current wallpaper", e);
                    }
                    if (returnDefault) {
                        if (this.mWallpaper == null) {
                            this.mDefaultWallpaper = getDefaultWallpaperLocked(context);
                            bitmap = this.mDefaultWallpaper;
                        } else {
                            this.mDefaultWallpaper = null;
                        }
                    }
                    bitmap = this.mWallpaper;
                }
            }
            return bitmap;
        }

        public void forgetLoadedWallpaper() {
            synchronized (this) {
                this.mWallpaper = null;
                this.mDefaultWallpaper = null;
            }
        }

        private Bitmap getCurrentWallpaperLocked(Context context) {
            if (this.mService == null) {
                Log.w(WallpaperManager.TAG, "WallpaperService not running");
                return null;
            }
            try {
                ParcelFileDescriptor fd = this.mService.getWallpaper(this, new Bundle());
                if (fd == null) {
                    return null;
                }
                try {
                    Bitmap decodeFileDescriptor = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, new BitmapFactory.Options());
                    try {
                        fd.close();
                    } catch (IOException e) {
                    }
                    return decodeFileDescriptor;
                } catch (OutOfMemoryError e2) {
                    Log.w(WallpaperManager.TAG, "Can't decode file", e2);
                    try {
                        fd.close();
                        return null;
                    } catch (IOException e3) {
                        return null;
                    }
                } catch (Throwable th) {
                    try {
                        fd.close();
                    } catch (IOException e4) {
                    }
                    throw th;
                }
            } catch (RemoteException e5) {
                return null;
            }
        }

        private Bitmap getDefaultWallpaperLocked(Context context) {
            Bitmap bitmap = null;
            InputStream is = WallpaperManager.openDefaultWallpaper(context);
            if (is != null) {
                try {
                    bitmap = BitmapFactory.decodeStream(is, null, new BitmapFactory.Options());
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                } catch (OutOfMemoryError e2) {
                    Log.w(WallpaperManager.TAG, "Can't decode stream", e2);
                    try {
                        is.close();
                    } catch (IOException e3) {
                    }
                } catch (Throwable th) {
                    try {
                        is.close();
                    } catch (IOException e4) {
                    }
                    throw th;
                }
            }
            return bitmap;
        }
    }

    static void initGlobals(Looper looper) {
        synchronized (sSync) {
            if (sGlobals == null) {
                sGlobals = new Globals(looper);
            }
        }
    }

    WallpaperManager(Context context, Handler handler) {
        this.mContext = context;
        initGlobals(context.getMainLooper());
    }

    public static WallpaperManager getInstance(Context context) {
        return (WallpaperManager) context.getSystemService(Context.WALLPAPER_SERVICE);
    }

    public IWallpaperManager getIWallpaperManager() {
        return sGlobals.mService;
    }

    public Drawable getDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(this.mContext, true);
        if (bm == null) {
            return null;
        }
        Drawable dr = new BitmapDrawable(this.mContext.getResources(), bm);
        dr.setDither(false);
        return dr;
    }

    public Drawable getBuiltInDrawable() {
        return getBuiltInDrawable(0, 0, false, 0.0f, 0.0f);
    }

    public Drawable getBuiltInDrawable(int outWidth, int outHeight, boolean scaleToFit, float horizontalAlignment, float verticalAlignment) {
        RectF cropRectF;
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            return null;
        }
        Resources resources = this.mContext.getResources();
        float horizontalAlignment2 = Math.max(0.0f, Math.min(1.0f, horizontalAlignment));
        float verticalAlignment2 = Math.max(0.0f, Math.min(1.0f, verticalAlignment));
        InputStream is = new BufferedInputStream(openDefaultWallpaper(this.mContext));
        if (is == null) {
            Log.e(TAG, "default wallpaper input stream is null");
            return null;
        } else if (outWidth <= 0 || outHeight <= 0) {
            return new BitmapDrawable(resources, BitmapFactory.decodeStream(is, null, null));
        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            if (options.outWidth == 0 || options.outHeight == 0) {
                Log.e(TAG, "default wallpaper dimensions are 0");
                return null;
            }
            int inWidth = options.outWidth;
            int inHeight = options.outHeight;
            InputStream is2 = new BufferedInputStream(openDefaultWallpaper(this.mContext));
            int outWidth2 = Math.min(inWidth, outWidth);
            int outHeight2 = Math.min(inHeight, outHeight);
            if (scaleToFit) {
                cropRectF = getMaxCropRect(inWidth, inHeight, outWidth2, outHeight2, horizontalAlignment2, verticalAlignment2);
            } else {
                float left = ((float) (inWidth - outWidth2)) * horizontalAlignment2;
                float top = ((float) (inHeight - outHeight2)) * verticalAlignment2;
                cropRectF = new RectF(left, top, left + ((float) outWidth2), top + ((float) outHeight2));
            }
            Rect roundedTrueCrop = new Rect();
            cropRectF.roundOut(roundedTrueCrop);
            if (roundedTrueCrop.width() <= 0 || roundedTrueCrop.height() <= 0) {
                Log.w(TAG, "crop has bad values for full size image");
                return null;
            }
            int scaleDownSampleSize = Math.min(roundedTrueCrop.width() / outWidth2, roundedTrueCrop.height() / outHeight2);
            BitmapRegionDecoder decoder = null;
            try {
                decoder = BitmapRegionDecoder.newInstance(is2, true);
            } catch (IOException e) {
                Log.w(TAG, "cannot open region decoder for default wallpaper");
            }
            Bitmap crop = null;
            if (decoder != null) {
                BitmapFactory.Options options2 = new BitmapFactory.Options();
                if (scaleDownSampleSize > 1) {
                    options2.inSampleSize = scaleDownSampleSize;
                }
                crop = decoder.decodeRegion(roundedTrueCrop, options2);
                decoder.recycle();
            }
            if (crop == null) {
                InputStream is3 = new BufferedInputStream(openDefaultWallpaper(this.mContext));
                Bitmap fullSize = null;
                if (is3 != null) {
                    BitmapFactory.Options options3 = new BitmapFactory.Options();
                    if (scaleDownSampleSize > 1) {
                        options3.inSampleSize = scaleDownSampleSize;
                    }
                    fullSize = BitmapFactory.decodeStream(is3, null, options3);
                }
                if (fullSize != null) {
                    crop = Bitmap.createBitmap(fullSize, roundedTrueCrop.left, roundedTrueCrop.top, roundedTrueCrop.width(), roundedTrueCrop.height());
                }
            }
            if (crop == null) {
                Log.w(TAG, "cannot decode default wallpaper");
                return null;
            }
            if (outWidth2 > 0 && outHeight2 > 0 && !(crop.getWidth() == outWidth2 && crop.getHeight() == outHeight2)) {
                Matrix m = new Matrix();
                RectF cropRect = new RectF(0.0f, 0.0f, (float) crop.getWidth(), (float) crop.getHeight());
                RectF returnRect = new RectF(0.0f, 0.0f, (float) outWidth2, (float) outHeight2);
                m.setRectToRect(cropRect, returnRect, Matrix.ScaleToFit.FILL);
                Bitmap tmp = Bitmap.createBitmap((int) returnRect.width(), (int) returnRect.height(), Bitmap.Config.ARGB_8888);
                if (tmp != null) {
                    Canvas c = new Canvas(tmp);
                    Paint p = new Paint();
                    p.setFilterBitmap(true);
                    c.drawBitmap(crop, m, p);
                    crop = tmp;
                }
            }
            return new BitmapDrawable(resources, crop);
        }
    }

    private static RectF getMaxCropRect(int inWidth, int inHeight, int outWidth, int outHeight, float horizontalAlignment, float verticalAlignment) {
        RectF cropRect = new RectF();
        if (((float) inWidth) / ((float) inHeight) > ((float) outWidth) / ((float) outHeight)) {
            cropRect.top = 0.0f;
            cropRect.bottom = (float) inHeight;
            float cropWidth = ((float) outWidth) * (((float) inHeight) / ((float) outHeight));
            cropRect.left = (((float) inWidth) - cropWidth) * horizontalAlignment;
            cropRect.right = cropRect.left + cropWidth;
        } else {
            cropRect.left = 0.0f;
            cropRect.right = (float) inWidth;
            float cropHeight = ((float) outHeight) * (((float) inWidth) / ((float) outWidth));
            cropRect.top = (((float) inHeight) - cropHeight) * verticalAlignment;
            cropRect.bottom = cropRect.top + cropHeight;
        }
        return cropRect;
    }

    public Drawable peekDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(this.mContext, false);
        if (bm == null) {
            return null;
        }
        Drawable dr = new BitmapDrawable(this.mContext.getResources(), bm);
        dr.setDither(false);
        return dr;
    }

    public Drawable getFastDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(this.mContext, true);
        if (bm != null) {
            return new FastBitmapDrawable(bm);
        }
        return null;
    }

    public Drawable peekFastDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(this.mContext, false);
        if (bm != null) {
            return new FastBitmapDrawable(bm);
        }
        return null;
    }

    public Bitmap getBitmap() {
        return sGlobals.peekWallpaperBitmap(this.mContext, true);
    }

    public void forgetLoadedWallpaper() {
        sGlobals.forgetLoadedWallpaper();
    }

    public WallpaperInfo getWallpaperInfo() {
        try {
            if (sGlobals.mService != null) {
                return sGlobals.mService.getWallpaperInfo();
            }
            Log.w(TAG, "WallpaperService not running");
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0054, code lost:
        if (r3.queryIntentActivities(r0, 0).size() > 0) goto L_0x0056;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.content.Intent getCropAndSetWallpaperIntent(android.net.Uri r9) {
        /*
        // Method dump skipped, instructions count: 110
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.WallpaperManager.getCropAndSetWallpaperIntent(android.net.Uri):android.content.Intent");
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0052  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setResource(int r8) throws java.io.IOException {
        /*
            r7 = this;
            android.app.WallpaperManager$Globals r4 = android.app.WallpaperManager.sGlobals
            android.app.IWallpaperManager r4 = android.app.WallpaperManager.Globals.access$100(r4)
            if (r4 != 0) goto L_0x0010
            java.lang.String r4 = android.app.WallpaperManager.TAG
            java.lang.String r5 = "WallpaperService not running"
            android.util.Log.w(r4, r5)
        L_0x000f:
            return
        L_0x0010:
            android.content.Context r4 = r7.mContext     // Catch:{ RemoteException -> 0x004d }
            android.content.res.Resources r3 = r4.getResources()     // Catch:{ RemoteException -> 0x004d }
            android.app.WallpaperManager$Globals r4 = android.app.WallpaperManager.sGlobals     // Catch:{ RemoteException -> 0x004d }
            android.app.IWallpaperManager r4 = android.app.WallpaperManager.Globals.access$100(r4)     // Catch:{ RemoteException -> 0x004d }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x004d }
            r5.<init>()     // Catch:{ RemoteException -> 0x004d }
            java.lang.String r6 = "res:"
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ RemoteException -> 0x004d }
            java.lang.String r6 = r3.getResourceName(r8)     // Catch:{ RemoteException -> 0x004d }
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ RemoteException -> 0x004d }
            java.lang.String r5 = r5.toString()     // Catch:{ RemoteException -> 0x004d }
            android.os.ParcelFileDescriptor r0 = r4.setWallpaper(r5)     // Catch:{ RemoteException -> 0x004d }
            if (r0 == 0) goto L_0x000f
            r1 = 0
            android.os.ParcelFileDescriptor$AutoCloseOutputStream r2 = new android.os.ParcelFileDescriptor$AutoCloseOutputStream     // Catch:{ all -> 0x004f }
            r2.<init>(r0)     // Catch:{ all -> 0x004f }
            java.io.InputStream r4 = r3.openRawResource(r8)     // Catch:{ all -> 0x0056 }
            r7.setWallpaper(r4, r2)     // Catch:{ all -> 0x0056 }
            if (r2 == 0) goto L_0x000f
            r2.close()
            goto L_0x000f
        L_0x004d:
            r4 = move-exception
            goto L_0x000f
        L_0x004f:
            r4 = move-exception
        L_0x0050:
            if (r1 == 0) goto L_0x0055
            r1.close()
        L_0x0055:
            throw r4
        L_0x0056:
            r4 = move-exception
            r1 = r2
            goto L_0x0050
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.WallpaperManager.setResource(int):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0035  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setBitmap(android.graphics.Bitmap r6) throws java.io.IOException {
        /*
            r5 = this;
            android.app.WallpaperManager$Globals r3 = android.app.WallpaperManager.sGlobals
            android.app.IWallpaperManager r3 = android.app.WallpaperManager.Globals.access$100(r3)
            if (r3 != 0) goto L_0x0010
            java.lang.String r3 = android.app.WallpaperManager.TAG
            java.lang.String r4 = "WallpaperService not running"
            android.util.Log.w(r3, r4)
        L_0x000f:
            return
        L_0x0010:
            android.app.WallpaperManager$Globals r3 = android.app.WallpaperManager.sGlobals     // Catch:{ RemoteException -> 0x0030 }
            android.app.IWallpaperManager r3 = android.app.WallpaperManager.Globals.access$100(r3)     // Catch:{ RemoteException -> 0x0030 }
            r4 = 0
            android.os.ParcelFileDescriptor r0 = r3.setWallpaper(r4)     // Catch:{ RemoteException -> 0x0030 }
            if (r0 == 0) goto L_0x000f
            r1 = 0
            android.os.ParcelFileDescriptor$AutoCloseOutputStream r2 = new android.os.ParcelFileDescriptor$AutoCloseOutputStream     // Catch:{ all -> 0x0032 }
            r2.<init>(r0)     // Catch:{ all -> 0x0032 }
            android.graphics.Bitmap$CompressFormat r3 = android.graphics.Bitmap.CompressFormat.PNG     // Catch:{ all -> 0x0039 }
            r4 = 90
            r6.compress(r3, r4, r2)     // Catch:{ all -> 0x0039 }
            if (r2 == 0) goto L_0x000f
            r2.close()
            goto L_0x000f
        L_0x0030:
            r3 = move-exception
            goto L_0x000f
        L_0x0032:
            r3 = move-exception
        L_0x0033:
            if (r1 == 0) goto L_0x0038
            r1.close()
        L_0x0038:
            throw r3
        L_0x0039:
            r3 = move-exception
            r1 = r2
            goto L_0x0033
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.WallpaperManager.setBitmap(android.graphics.Bitmap):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0031  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setStream(java.io.InputStream r6) throws java.io.IOException {
        /*
            r5 = this;
            android.app.WallpaperManager$Globals r3 = android.app.WallpaperManager.sGlobals
            android.app.IWallpaperManager r3 = android.app.WallpaperManager.Globals.access$100(r3)
            if (r3 != 0) goto L_0x0010
            java.lang.String r3 = android.app.WallpaperManager.TAG
            java.lang.String r4 = "WallpaperService not running"
            android.util.Log.w(r3, r4)
        L_0x000f:
            return
        L_0x0010:
            android.app.WallpaperManager$Globals r3 = android.app.WallpaperManager.sGlobals     // Catch:{ RemoteException -> 0x002c }
            android.app.IWallpaperManager r3 = android.app.WallpaperManager.Globals.access$100(r3)     // Catch:{ RemoteException -> 0x002c }
            r4 = 0
            android.os.ParcelFileDescriptor r0 = r3.setWallpaper(r4)     // Catch:{ RemoteException -> 0x002c }
            if (r0 == 0) goto L_0x000f
            r1 = 0
            android.os.ParcelFileDescriptor$AutoCloseOutputStream r2 = new android.os.ParcelFileDescriptor$AutoCloseOutputStream     // Catch:{ all -> 0x002e }
            r2.<init>(r0)     // Catch:{ all -> 0x002e }
            r5.setWallpaper(r6, r2)     // Catch:{ all -> 0x0035 }
            if (r2 == 0) goto L_0x000f
            r2.close()
            goto L_0x000f
        L_0x002c:
            r3 = move-exception
            goto L_0x000f
        L_0x002e:
            r3 = move-exception
        L_0x002f:
            if (r1 == 0) goto L_0x0034
            r1.close()
        L_0x0034:
            throw r3
        L_0x0035:
            r3 = move-exception
            r1 = r2
            goto L_0x002f
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.WallpaperManager.setStream(java.io.InputStream):void");
    }

    private void setWallpaper(InputStream data, FileOutputStream fos) throws IOException {
        byte[] buffer = new byte[32768];
        while (true) {
            int amt = data.read(buffer);
            if (amt > 0) {
                fos.write(buffer, 0, amt);
            } else {
                return;
            }
        }
    }

    public boolean hasResourceWallpaper(int resid) {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            return false;
        }
        try {
            return sGlobals.mService.hasNamedWallpaper("res:" + this.mContext.getResources().getResourceName(resid));
        } catch (RemoteException e) {
            return false;
        }
    }

    public int getDesiredMinimumWidth() {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            return 0;
        }
        try {
            return sGlobals.mService.getWidthHint();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public int getDesiredMinimumHeight() {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            return 0;
        }
        try {
            return sGlobals.mService.getHeightHint();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public void suggestDesiredDimensions(int minimumWidth, int minimumHeight) {
        int maximumTextureSize;
        try {
            maximumTextureSize = SystemProperties.getInt("sys.max_texture_size", 0);
        } catch (Exception e) {
            maximumTextureSize = 0;
        }
        if (maximumTextureSize > 0 && (minimumWidth > maximumTextureSize || minimumHeight > maximumTextureSize)) {
            float aspect = ((float) minimumHeight) / ((float) minimumWidth);
            if (minimumWidth > minimumHeight) {
                minimumWidth = maximumTextureSize;
                minimumHeight = (int) (((double) (((float) minimumWidth) * aspect)) + 0.5d);
            } else {
                minimumHeight = maximumTextureSize;
                minimumWidth = (int) (((double) (((float) minimumHeight) / aspect)) + 0.5d);
            }
        }
        try {
            if (sGlobals.mService == null) {
                Log.w(TAG, "WallpaperService not running");
            } else {
                sGlobals.mService.setDimensionHints(minimumWidth, minimumHeight);
            }
        } catch (RemoteException e2) {
        }
    }

    public void setDisplayPadding(Rect padding) {
        try {
            if (sGlobals.mService == null) {
                Log.w(TAG, "WallpaperService not running");
            } else {
                sGlobals.mService.setDisplayPadding(padding);
            }
        } catch (RemoteException e) {
        }
    }

    public void setDisplayOffset(IBinder windowToken, int x, int y) {
        try {
            WindowManagerGlobal.getWindowSession().setWallpaperDisplayOffset(windowToken, x, y);
        } catch (RemoteException e) {
        }
    }

    public void setWallpaperOffsets(IBinder windowToken, float xOffset, float yOffset) {
        try {
            WindowManagerGlobal.getWindowSession().setWallpaperPosition(windowToken, xOffset, yOffset, this.mWallpaperXStep, this.mWallpaperYStep);
        } catch (RemoteException e) {
        }
    }

    public void setWallpaperOffsetSteps(float xStep, float yStep) {
        this.mWallpaperXStep = xStep;
        this.mWallpaperYStep = yStep;
    }

    public void sendWallpaperCommand(IBinder windowToken, String action, int x, int y, int z, Bundle extras) {
        try {
            WindowManagerGlobal.getWindowSession().sendWallpaperCommand(windowToken, action, x, y, z, extras, false);
        } catch (RemoteException e) {
        }
    }

    public void clearWallpaperOffsets(IBinder windowToken) {
        try {
            WindowManagerGlobal.getWindowSession().setWallpaperPosition(windowToken, -1.0f, -1.0f, -1.0f, -1.0f);
        } catch (RemoteException e) {
        }
    }

    public void clear() throws IOException {
        setStream(openDefaultWallpaper(this.mContext));
    }

    public static InputStream openDefaultWallpaper(Context context) {
        String path = SystemProperties.get(PROP_WALLPAPER);
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    return new FileInputStream(file);
                } catch (IOException e) {
                }
            }
        }
        return context.getResources().openRawResource(17302161);
    }

    public static ComponentName getDefaultWallpaperComponent(Context context) {
        ComponentName cn;
        ComponentName cn2;
        String flat = SystemProperties.get(PROP_WALLPAPER_COMPONENT);
        if (!TextUtils.isEmpty(flat) && (cn2 = ComponentName.unflattenFromString(flat)) != null) {
            return cn2;
        }
        String flat2 = context.getString(17039390);
        if (TextUtils.isEmpty(flat2) || (cn = ComponentName.unflattenFromString(flat2)) == null) {
            return null;
        }
        return cn;
    }
}
