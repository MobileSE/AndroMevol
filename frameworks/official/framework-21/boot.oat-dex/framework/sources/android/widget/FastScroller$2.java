package android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

class FastScroller$2 extends AnimatorListenerAdapter {
    final /* synthetic */ FastScroller this$0;

    FastScroller$2(FastScroller fastScroller) {
        this.this$0 = fastScroller;
    }

    public void onAnimationEnd(Animator animation) {
        FastScroller.access$102(this.this$0, !FastScroller.access$100(this.this$0));
    }
}
