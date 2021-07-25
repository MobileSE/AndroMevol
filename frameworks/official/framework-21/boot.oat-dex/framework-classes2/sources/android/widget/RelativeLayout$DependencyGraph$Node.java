package android.widget;

import android.util.ArrayMap;
import android.util.Pools;
import android.util.SparseArray;
import android.view.View;
import android.widget.RelativeLayout;

/* access modifiers changed from: package-private */
public class RelativeLayout$DependencyGraph$Node {
    private static final int POOL_LIMIT = 100;
    private static final Pools.SynchronizedPool<RelativeLayout$DependencyGraph$Node> sPool = new Pools.SynchronizedPool<>(100);
    final SparseArray<RelativeLayout$DependencyGraph$Node> dependencies = new SparseArray<>();
    final ArrayMap<RelativeLayout$DependencyGraph$Node, RelativeLayout.DependencyGraph> dependents = new ArrayMap<>();
    View view;

    RelativeLayout$DependencyGraph$Node() {
    }

    static RelativeLayout$DependencyGraph$Node acquire(View view2) {
        RelativeLayout$DependencyGraph$Node node = (RelativeLayout$DependencyGraph$Node) sPool.acquire();
        if (node == null) {
            node = new RelativeLayout$DependencyGraph$Node();
        }
        node.view = view2;
        return node;
    }

    /* access modifiers changed from: package-private */
    public void release() {
        this.view = null;
        this.dependents.clear();
        this.dependencies.clear();
        sPool.release(this);
    }
}
