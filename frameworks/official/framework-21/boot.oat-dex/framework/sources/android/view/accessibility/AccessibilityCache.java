package android.view.accessibility;

import android.os.Build;
import android.util.ArraySet;
import android.util.Log;
import android.util.LongArray;
import android.util.LongSparseArray;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public final class AccessibilityCache {
    private static final boolean CHECK_INTEGRITY = "eng".equals(Build.TYPE);
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "AccessibilityCache";
    private final Object mLock = new Object();
    private final SparseArray<LongSparseArray<AccessibilityNodeInfo>> mNodeCache = new SparseArray<>();
    private final SparseArray<AccessibilityWindowInfo> mTempWindowArray = new SparseArray<>();
    private final SparseArray<AccessibilityWindowInfo> mWindowCache = new SparseArray<>();

    AccessibilityCache() {
    }

    public void addWindow(AccessibilityWindowInfo window) {
        synchronized (this.mLock) {
            int windowId = window.getId();
            AccessibilityWindowInfo oldWindow = this.mWindowCache.get(windowId);
            if (oldWindow != null) {
                oldWindow.recycle();
            }
            this.mWindowCache.put(windowId, AccessibilityWindowInfo.obtain(window));
        }
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
        synchronized (this.mLock) {
            switch (event.getEventType()) {
                case 4:
                case 8:
                case 16:
                case 8192:
                case 32768:
                case 65536:
                    refreshCachedNodeLocked(event.getWindowId(), event.getSourceNodeId());
                    break;
                case 32:
                case 4194304:
                    clear();
                    break;
                case 2048:
                    synchronized (this.mLock) {
                        int windowId = event.getWindowId();
                        long sourceId = event.getSourceNodeId();
                        if ((event.getContentChangeTypes() & 1) != 0) {
                            clearSubTreeLocked(windowId, sourceId);
                        } else {
                            refreshCachedNodeLocked(windowId, sourceId);
                        }
                    }
                    break;
                case 4096:
                    clearSubTreeLocked(event.getWindowId(), event.getSourceNodeId());
                    break;
            }
        }
        if (CHECK_INTEGRITY) {
            checkIntegrity();
        }
    }

    private void refreshCachedNodeLocked(int windowId, long sourceId) {
        AccessibilityNodeInfo cachedInfo;
        LongSparseArray<AccessibilityNodeInfo> nodes = this.mNodeCache.get(windowId);
        if (nodes != null && (cachedInfo = nodes.get(sourceId)) != null && !cachedInfo.refresh(true)) {
            clearSubTreeLocked(windowId, sourceId);
        }
    }

    public AccessibilityNodeInfo getNode(int windowId, long accessibilityNodeId) {
        AccessibilityNodeInfo info;
        synchronized (this.mLock) {
            LongSparseArray<AccessibilityNodeInfo> nodes = this.mNodeCache.get(windowId);
            if (nodes == null) {
                info = null;
            } else {
                info = nodes.get(accessibilityNodeId);
                if (info != null) {
                    info = AccessibilityNodeInfo.obtain(info);
                }
            }
        }
        return info;
    }

    public List<AccessibilityWindowInfo> getWindows() {
        List<AccessibilityWindowInfo> windows;
        synchronized (this.mLock) {
            int windowCount = this.mWindowCache.size();
            if (windowCount > 0) {
                SparseArray<AccessibilityWindowInfo> sortedWindows = this.mTempWindowArray;
                sortedWindows.clear();
                for (int i = 0; i < windowCount; i++) {
                    AccessibilityWindowInfo window = this.mWindowCache.valueAt(i);
                    sortedWindows.put(window.getLayer(), window);
                }
                windows = new ArrayList<>(windowCount);
                for (int i2 = windowCount - 1; i2 >= 0; i2--) {
                    windows.add(AccessibilityWindowInfo.obtain(sortedWindows.valueAt(i2)));
                    sortedWindows.removeAt(i2);
                }
            } else {
                windows = null;
            }
        }
        return windows;
    }

    public AccessibilityWindowInfo getWindow(int windowId) {
        AccessibilityWindowInfo accessibilityWindowInfo;
        synchronized (this.mLock) {
            AccessibilityWindowInfo window = this.mWindowCache.get(windowId);
            if (window != null) {
                accessibilityWindowInfo = AccessibilityWindowInfo.obtain(window);
            } else {
                accessibilityWindowInfo = null;
            }
        }
        return accessibilityWindowInfo;
    }

    public void add(AccessibilityNodeInfo info) {
        synchronized (this.mLock) {
            int windowId = info.getWindowId();
            LongSparseArray<AccessibilityNodeInfo> nodes = this.mNodeCache.get(windowId);
            if (nodes == null) {
                nodes = new LongSparseArray<>();
                this.mNodeCache.put(windowId, nodes);
            }
            long sourceId = info.getSourceNodeId();
            AccessibilityNodeInfo oldInfo = nodes.get(sourceId);
            if (oldInfo != null) {
                LongArray newChildrenIds = info.getChildNodeIds();
                int oldChildCount = oldInfo.getChildCount();
                for (int i = 0; i < oldChildCount; i++) {
                    long oldChildId = oldInfo.getChildId(i);
                    if (newChildrenIds == null || newChildrenIds.indexOf(oldChildId) < 0) {
                        clearSubTreeLocked(windowId, oldChildId);
                    }
                }
                long oldParentId = oldInfo.getParentNodeId();
                if (info.getParentNodeId() != oldParentId) {
                    clearSubTreeLocked(windowId, oldParentId);
                }
            }
            nodes.put(sourceId, AccessibilityNodeInfo.obtain(info));
        }
    }

    public void clear() {
        synchronized (this.mLock) {
            for (int i = this.mWindowCache.size() - 1; i >= 0; i--) {
                this.mWindowCache.valueAt(i).recycle();
                this.mWindowCache.removeAt(i);
            }
            int nodesForWindowCount = this.mNodeCache.size();
            for (int i2 = 0; i2 < nodesForWindowCount; i2++) {
                clearNodesForWindowLocked(this.mNodeCache.keyAt(i2));
            }
        }
    }

    private void clearNodesForWindowLocked(int windowId) {
        LongSparseArray<AccessibilityNodeInfo> nodes = this.mNodeCache.get(windowId);
        if (nodes != null) {
            for (int i = nodes.size() - 1; i >= 0; i--) {
                nodes.removeAt(i);
                nodes.valueAt(i).recycle();
            }
            this.mNodeCache.remove(windowId);
        }
    }

    private void clearSubTreeLocked(int windowId, long rootNodeId) {
        LongSparseArray<AccessibilityNodeInfo> nodes = this.mNodeCache.get(windowId);
        if (nodes != null) {
            clearSubTreeRecursiveLocked(nodes, rootNodeId);
        }
    }

    private void clearSubTreeRecursiveLocked(LongSparseArray<AccessibilityNodeInfo> nodes, long rootNodeId) {
        AccessibilityNodeInfo current = nodes.get(rootNodeId);
        if (current != null) {
            nodes.remove(rootNodeId);
            int childCount = current.getChildCount();
            for (int i = 0; i < childCount; i++) {
                clearSubTreeRecursiveLocked(nodes, current.getChildId(i));
            }
        }
    }

    public void checkIntegrity() {
        synchronized (this.mLock) {
            if (this.mWindowCache.size() > 0 || this.mNodeCache.size() != 0) {
                AccessibilityWindowInfo focusedWindow = null;
                AccessibilityWindowInfo activeWindow = null;
                int windowCount = this.mWindowCache.size();
                for (int i = 0; i < windowCount; i++) {
                    AccessibilityWindowInfo window = this.mWindowCache.valueAt(i);
                    if (window.isActive()) {
                        if (activeWindow != null) {
                            Log.e(LOG_TAG, "Duplicate active window:" + window);
                        } else {
                            activeWindow = window;
                        }
                    }
                    if (window.isFocused()) {
                        if (focusedWindow != null) {
                            Log.e(LOG_TAG, "Duplicate focused window:" + window);
                        } else {
                            focusedWindow = window;
                        }
                    }
                }
                AccessibilityNodeInfo accessFocus = null;
                AccessibilityNodeInfo inputFocus = null;
                int nodesForWindowCount = this.mNodeCache.size();
                for (int i2 = 0; i2 < nodesForWindowCount; i2++) {
                    LongSparseArray<AccessibilityNodeInfo> nodes = this.mNodeCache.valueAt(i2);
                    if (nodes.size() > 0) {
                        ArraySet<AccessibilityNodeInfo> seen = new ArraySet<>();
                        int windowId = this.mNodeCache.keyAt(i2);
                        int nodeCount = nodes.size();
                        for (int j = 0; j < nodeCount; j++) {
                            AccessibilityNodeInfo node = nodes.valueAt(j);
                            if (!seen.add(node)) {
                                Log.e(LOG_TAG, "Duplicate node: " + node + " in window:" + windowId);
                            } else {
                                if (node.isAccessibilityFocused()) {
                                    if (accessFocus != null) {
                                        Log.e(LOG_TAG, "Duplicate accessibility focus:" + node + " in window:" + windowId);
                                    } else {
                                        accessFocus = node;
                                    }
                                }
                                if (node.isFocused()) {
                                    if (inputFocus != null) {
                                        Log.e(LOG_TAG, "Duplicate input focus: " + node + " in window:" + windowId);
                                    } else {
                                        inputFocus = node;
                                    }
                                }
                                AccessibilityNodeInfo nodeParent = nodes.get(node.getParentNodeId());
                                if (nodeParent != null) {
                                    boolean childOfItsParent = false;
                                    int childCount = nodeParent.getChildCount();
                                    int k = 0;
                                    while (true) {
                                        if (k >= childCount) {
                                            break;
                                        } else if (nodes.get(nodeParent.getChildId(k)) == node) {
                                            childOfItsParent = true;
                                            break;
                                        } else {
                                            k++;
                                        }
                                    }
                                    if (!childOfItsParent) {
                                        Log.e(LOG_TAG, "Invalid parent-child relation between parent: " + nodeParent + " and child: " + node);
                                    }
                                }
                                int childCount2 = node.getChildCount();
                                for (int k2 = 0; k2 < childCount2; k2++) {
                                    AccessibilityNodeInfo child = nodes.get(node.getChildId(k2));
                                    if (!(child == null || nodes.get(child.getParentNodeId()) == node)) {
                                        Log.e(LOG_TAG, "Invalid child-parent relation between child: " + node + " and parent: " + nodeParent);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
