package android.webkit;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class PluginList {
    private ArrayList<Plugin> mPlugins = new ArrayList<>();

    @Deprecated
    public synchronized List getList() {
        return this.mPlugins;
    }

    @Deprecated
    public synchronized void addPlugin(Plugin plugin) {
        if (!this.mPlugins.contains(plugin)) {
            this.mPlugins.add(plugin);
        }
    }

    @Deprecated
    public synchronized void removePlugin(Plugin plugin) {
        int location = this.mPlugins.indexOf(plugin);
        if (location != -1) {
            this.mPlugins.remove(location);
        }
    }

    @Deprecated
    public synchronized void clear() {
        this.mPlugins.clear();
    }

    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    @java.lang.Deprecated
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void pluginClicked(android.content.Context r3, int r4) {
        /*
            r2 = this;
            monitor-enter(r2)
            java.util.ArrayList<android.webkit.Plugin> r1 = r2.mPlugins     // Catch:{ IndexOutOfBoundsException -> 0x0011, all -> 0x000e }
            java.lang.Object r0 = r1.get(r4)     // Catch:{ IndexOutOfBoundsException -> 0x0011, all -> 0x000e }
            android.webkit.Plugin r0 = (android.webkit.Plugin) r0     // Catch:{ IndexOutOfBoundsException -> 0x0011, all -> 0x000e }
            r0.dispatchClickEvent(r3)     // Catch:{ IndexOutOfBoundsException -> 0x0011, all -> 0x000e }
        L_0x000c:
            monitor-exit(r2)
            return
        L_0x000e:
            r1 = move-exception
            monitor-exit(r2)
            throw r1
        L_0x0011:
            r1 = move-exception
            goto L_0x000c
        */
        throw new UnsupportedOperationException("Method not decompiled: android.webkit.PluginList.pluginClicked(android.content.Context, int):void");
    }
}
