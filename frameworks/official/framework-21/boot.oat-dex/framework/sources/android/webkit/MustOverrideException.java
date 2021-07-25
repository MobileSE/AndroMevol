package android.webkit;

/* access modifiers changed from: package-private */
public class MustOverrideException extends RuntimeException {
    MustOverrideException() {
        super("abstract function called: must be overriden!");
    }
}
