package com.android.internal.util;

import java.io.IOException;
import java.io.InputStream;
import libcore.io.Streams;

public class SizedInputStream extends InputStream {
    private long mLength;
    private final InputStream mWrapped;

    public SizedInputStream(InputStream wrapped, long length) {
        this.mWrapped = wrapped;
        this.mLength = length;
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable, java.io.InputStream
    public void close() throws IOException {
        super.close();
        this.mWrapped.close();
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        return Streams.readSingleByte(this);
    }

    @Override // java.io.InputStream
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        if (this.mLength <= 0) {
            return -1;
        }
        if (((long) byteCount) > this.mLength) {
            byteCount = (int) this.mLength;
        }
        int n = this.mWrapped.read(buffer, byteOffset, byteCount);
        if (n != -1) {
            this.mLength -= (long) n;
            return n;
        } else if (this.mLength <= 0) {
            return n;
        } else {
            throw new IOException("Unexpected EOF; expected " + this.mLength + " more bytes");
        }
    }
}
