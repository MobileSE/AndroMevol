package android.content.pm;

import java.util.HashSet;

public class PackageUserState {
    public boolean blockUninstall;
    public HashSet<String> disabledComponents;
    public int enabled;
    public HashSet<String> enabledComponents;
    public boolean hidden;
    public boolean installed;
    public String lastDisableAppCaller;
    public boolean notLaunched;
    public boolean stopped;

    public PackageUserState() {
        this.installed = true;
        this.hidden = false;
        this.enabled = 0;
    }

    public PackageUserState(PackageUserState o) {
        HashSet<String> hashSet = null;
        this.installed = o.installed;
        this.stopped = o.stopped;
        this.notLaunched = o.notLaunched;
        this.enabled = o.enabled;
        this.hidden = o.hidden;
        this.lastDisableAppCaller = o.lastDisableAppCaller;
        this.disabledComponents = o.disabledComponents != null ? new HashSet<>(o.disabledComponents) : null;
        this.enabledComponents = o.enabledComponents != null ? new HashSet<>(o.enabledComponents) : hashSet;
        this.blockUninstall = o.blockUninstall;
    }
}
