package android.widget;

import android.util.FloatProperty;

class Switch$1 extends FloatProperty<Switch> {
    Switch$1(String x0) {
        super(x0);
    }

    public Float get(Switch object) {
        return Float.valueOf(Switch.access$000(object));
    }

    public void setValue(Switch object, float value) {
        Switch.access$100(object, value);
    }
}
