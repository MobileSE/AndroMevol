package android.widget;

import android.util.ArrayMap;
import android.util.SparseArray;
import android.view.View;
import android.widget.RelativeLayout;
import java.util.ArrayDeque;
import java.util.ArrayList;

class RelativeLayout$DependencyGraph {
    private SparseArray<Node> mKeyNodes;
    private ArrayList<Node> mNodes;
    private ArrayDeque<Node> mRoots;

    private RelativeLayout$DependencyGraph() {
        this.mNodes = new ArrayList<>();
        this.mKeyNodes = new SparseArray<>();
        this.mRoots = new ArrayDeque<>();
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        ArrayList<Node> nodes = this.mNodes;
        int count = nodes.size();
        for (int i = 0; i < count; i++) {
            nodes.get(i).release();
        }
        nodes.clear();
        this.mKeyNodes.clear();
        this.mRoots.clear();
    }

    /* access modifiers changed from: package-private */
    public void add(View view) {
        int id = view.getId();
        Node node = Node.acquire(view);
        if (id != -1) {
            this.mKeyNodes.put(id, node);
        }
        this.mNodes.add(node);
    }

    /* access modifiers changed from: package-private */
    public void getSortedViews(View[] sorted, int... rules) {
        ArrayDeque<Node> roots = findRoots(rules);
        int index = 0;
        while (true) {
            Node node = roots.pollLast();
            if (node == null) {
                break;
            }
            View view = node.view;
            int key = view.getId();
            int index2 = index + 1;
            sorted[index] = view;
            ArrayMap<Node, RelativeLayout$DependencyGraph> dependents = node.dependents;
            int count = dependents.size();
            for (int i = 0; i < count; i++) {
                Node dependent = dependents.keyAt(i);
                SparseArray<Node> dependencies = dependent.dependencies;
                dependencies.remove(key);
                if (dependencies.size() == 0) {
                    roots.add(dependent);
                }
            }
            index = index2;
        }
        if (index < sorted.length) {
            throw new IllegalStateException("Circular dependencies cannot exist in RelativeLayout");
        }
    }

    private ArrayDeque<Node> findRoots(int[] rulesFilter) {
        Node dependency;
        SparseArray<Node> keyNodes = this.mKeyNodes;
        ArrayList<Node> nodes = this.mNodes;
        int count = nodes.size();
        for (int i = 0; i < count; i++) {
            Node node = nodes.get(i);
            node.dependents.clear();
            node.dependencies.clear();
        }
        for (int i2 = 0; i2 < count; i2++) {
            Node node2 = nodes.get(i2);
            int[] rules = RelativeLayout.LayoutParams.access$700((RelativeLayout.LayoutParams) node2.view.getLayoutParams());
            for (int i3 : rulesFilter) {
                int rule = rules[i3];
                if (!(rule <= 0 || (dependency = keyNodes.get(rule)) == null || dependency == node2)) {
                    dependency.dependents.put(node2, this);
                    node2.dependencies.put(rule, dependency);
                }
            }
        }
        ArrayDeque<Node> roots = this.mRoots;
        roots.clear();
        for (int i4 = 0; i4 < count; i4++) {
            Node node3 = nodes.get(i4);
            if (node3.dependencies.size() == 0) {
                roots.addLast(node3);
            }
        }
        return roots;
    }
}
