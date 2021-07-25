package com.android.internal.view;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.view.DragEvent;
import android.view.IWindow;
import android.view.IWindowSession;

public class BaseIWindow extends IWindow.Stub {
    public int mSeq;
    private IWindowSession mSession;

    public void setSession(IWindowSession session) {
        this.mSession = session;
    }

    @Override // android.view.IWindow
    public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, boolean reportDraw, Configuration newConfig) {
        if (reportDraw) {
            try {
                this.mSession.finishDrawing(this);
            } catch (RemoteException e) {
            }
        }
    }

    @Override // android.view.IWindow
    public void moved(int newX, int newY) {
    }

    @Override // android.view.IWindow
    public void dispatchAppVisibility(boolean visible) {
    }

    @Override // android.view.IWindow
    public void dispatchGetNewSurface() {
    }

    @Override // android.view.IWindow
    public void windowFocusChanged(boolean hasFocus, boolean touchEnabled) {
    }

    @Override // android.view.IWindow
    public void executeCommand(String command, String parameters, ParcelFileDescriptor out) {
    }

    @Override // android.view.IWindow
    public void closeSystemDialogs(String reason) {
    }

    @Override // android.view.IWindow
    public void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep, boolean sync) {
        if (sync) {
            try {
                this.mSession.wallpaperOffsetsComplete(asBinder());
            } catch (RemoteException e) {
            }
        }
    }

    @Override // android.view.IWindow
    public void dispatchDragEvent(DragEvent event) {
    }

    @Override // android.view.IWindow
    public void dispatchSystemUiVisibilityChanged(int seq, int globalUi, int localValue, int localChanges) {
        this.mSeq = seq;
    }

    @Override // android.view.IWindow
    public void dispatchWallpaperCommand(String action, int x, int y, int z, Bundle extras, boolean sync) {
        if (sync) {
            try {
                this.mSession.wallpaperCommandComplete(asBinder(), null);
            } catch (RemoteException e) {
            }
        }
    }

    @Override // android.view.IWindow
    public void doneAnimating() {
    }
}
