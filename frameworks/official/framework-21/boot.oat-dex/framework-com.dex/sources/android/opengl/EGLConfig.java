package android.opengl;

public class EGLConfig extends EGLObjectHandle {
    private EGLConfig(long handle) {
        super(handle);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EGLConfig)) {
            return false;
        }
        return getNativeHandle() == ((EGLConfig) o).getNativeHandle();
    }
}
