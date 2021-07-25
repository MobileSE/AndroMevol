package android.widget;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

class TextClock$1 extends ContentObserver {
    final /* synthetic */ TextClock this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    TextClock$1(TextClock textClock, Handler x0) {
        super(x0);
        this.this$0 = textClock;
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean selfChange) {
        TextClock.access$000(this.this$0);
        TextClock.access$100(this.this$0);
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean selfChange, Uri uri) {
        TextClock.access$000(this.this$0);
        TextClock.access$100(this.this$0);
    }
}
