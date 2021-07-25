package android.print;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PrintDocumentAdapter;
import android.util.Log;
import com.android.internal.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import libcore.io.IoUtils;

public class PrintFileDocumentAdapter extends PrintDocumentAdapter {
    private static final String LOG_TAG = "PrintedFileDocumentAdapter";
    private final Context mContext;
    private final PrintDocumentInfo mDocumentInfo;
    private final File mFile;
    private WriteFileAsyncTask mWriteFileAsyncTask;

    public PrintFileDocumentAdapter(Context context, File file, PrintDocumentInfo documentInfo) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null!");
        } else if (documentInfo == null) {
            throw new IllegalArgumentException("documentInfo cannot be null!");
        } else {
            this.mContext = context;
            this.mFile = file;
            this.mDocumentInfo = documentInfo;
        }
    }

    @Override // android.print.PrintDocumentAdapter
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, PrintDocumentAdapter.LayoutResultCallback callback, Bundle metadata) {
        callback.onLayoutFinished(this.mDocumentInfo, false);
    }

    @Override // android.print.PrintDocumentAdapter
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, PrintDocumentAdapter.WriteResultCallback callback) {
        this.mWriteFileAsyncTask = new WriteFileAsyncTask(destination, cancellationSignal, callback);
        this.mWriteFileAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
    }

    private final class WriteFileAsyncTask extends AsyncTask<Void, Void, Void> {
        private final CancellationSignal mCancellationSignal;
        private final ParcelFileDescriptor mDestination;
        private final PrintDocumentAdapter.WriteResultCallback mResultCallback;

        public WriteFileAsyncTask(ParcelFileDescriptor destination, CancellationSignal cancellationSignal, PrintDocumentAdapter.WriteResultCallback callback) {
            this.mDestination = destination;
            this.mResultCallback = callback;
            this.mCancellationSignal = cancellationSignal;
            this.mCancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener(PrintFileDocumentAdapter.this) {
                /* class android.print.PrintFileDocumentAdapter.WriteFileAsyncTask.AnonymousClass1 */

                @Override // android.os.CancellationSignal.OnCancelListener
                public void onCancel() {
                    WriteFileAsyncTask.this.cancel(true);
                }
            });
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... params) {
            Throwable th;
            int readByteCount;
            InputStream in = null;
            OutputStream out = new FileOutputStream(this.mDestination.getFileDescriptor());
            byte[] buffer = new byte[8192];
            try {
                InputStream in2 = new FileInputStream(PrintFileDocumentAdapter.this.mFile);
                while (!isCancelled() && (readByteCount = in2.read(buffer)) >= 0) {
                    try {
                        out.write(buffer, 0, readByteCount);
                    } catch (IOException e) {
                        ioe = e;
                        in = in2;
                        try {
                            Log.e(PrintFileDocumentAdapter.LOG_TAG, "Error writing data!", ioe);
                            this.mResultCallback.onWriteFailed(PrintFileDocumentAdapter.this.mContext.getString(R.string.write_fail_reason_cannot_write));
                            IoUtils.closeQuietly(in);
                            IoUtils.closeQuietly(out);
                            return null;
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(in);
                            IoUtils.closeQuietly(out);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        in = in2;
                        IoUtils.closeQuietly(in);
                        IoUtils.closeQuietly(out);
                        throw th;
                    }
                }
                IoUtils.closeQuietly(in2);
                IoUtils.closeQuietly(out);
                return null;
            } catch (IOException e2) {
                ioe = e2;
                Log.e(PrintFileDocumentAdapter.LOG_TAG, "Error writing data!", ioe);
                this.mResultCallback.onWriteFailed(PrintFileDocumentAdapter.this.mContext.getString(R.string.write_fail_reason_cannot_write));
                IoUtils.closeQuietly(in);
                IoUtils.closeQuietly(out);
                return null;
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void result) {
            this.mResultCallback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
        }

        /* access modifiers changed from: protected */
        public void onCancelled(Void result) {
            this.mResultCallback.onWriteFailed(PrintFileDocumentAdapter.this.mContext.getString(R.string.write_fail_reason_cancelled));
        }
    }
}
