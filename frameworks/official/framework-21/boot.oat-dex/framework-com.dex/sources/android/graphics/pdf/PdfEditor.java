package android.graphics.pdf;

import android.os.ParcelFileDescriptor;
import android.system.ErrnoException;
import android.system.OsConstants;
import dalvik.system.CloseGuard;
import java.io.IOException;
import libcore.io.IoUtils;
import libcore.io.Libcore;

public final class PdfEditor {
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private ParcelFileDescriptor mInput;
    private final long mNativeDocument;
    private int mPageCount;

    private static native void nativeClose(long j);

    private static native int nativeGetPageCount(long j);

    private static native long nativeOpen(int i, long j);

    private static native int nativeRemovePage(long j, int i);

    private static native void nativeWrite(long j, int i);

    public PdfEditor(ParcelFileDescriptor input) throws IOException {
        if (input == null) {
            throw new NullPointerException("input cannot be null");
        }
        try {
            Libcore.os.lseek(input.getFileDescriptor(), 0, OsConstants.SEEK_SET);
            long size = Libcore.os.fstat(input.getFileDescriptor()).st_size;
            this.mInput = input;
            this.mNativeDocument = nativeOpen(this.mInput.getFd(), size);
            this.mPageCount = nativeGetPageCount(this.mNativeDocument);
            this.mCloseGuard.open("close");
        } catch (ErrnoException e) {
            throw new IllegalArgumentException("file descriptor not seekable");
        }
    }

    public int getPageCount() {
        throwIfClosed();
        return this.mPageCount;
    }

    public void removePage(int pageIndex) {
        throwIfClosed();
        throwIfPageNotInDocument(pageIndex);
        this.mPageCount = nativeRemovePage(this.mNativeDocument, pageIndex);
    }

    public void write(ParcelFileDescriptor output) throws IOException {
        try {
            throwIfClosed();
            nativeWrite(this.mNativeDocument, output.getFd());
        } finally {
            IoUtils.closeQuietly(output);
        }
    }

    public void close() {
        throwIfClosed();
        doClose();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            this.mCloseGuard.warnIfOpen();
            if (this.mInput != null) {
                doClose();
            }
        } finally {
            super.finalize();
        }
    }

    private void doClose() {
        nativeClose(this.mNativeDocument);
        IoUtils.closeQuietly(this.mInput);
        this.mInput = null;
        this.mCloseGuard.close();
    }

    private void throwIfClosed() {
        if (this.mInput == null) {
            throw new IllegalStateException("Already closed");
        }
    }

    private void throwIfPageNotInDocument(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= this.mPageCount) {
            throw new IllegalArgumentException("Invalid page index");
        }
    }
}
