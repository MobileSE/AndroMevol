package android.widget;

import android.widget.SpellChecker;

class SpellChecker$1 implements Runnable {
    final /* synthetic */ SpellChecker this$0;

    SpellChecker$1(SpellChecker spellChecker) {
        this.this$0 = spellChecker;
    }

    public void run() {
        int length = SpellChecker.access$100(this.this$0).length;
        for (int i = 0; i < length; i++) {
            SpellChecker.SpellParser spellParser = SpellChecker.access$100(this.this$0)[i];
            if (!spellParser.isFinished()) {
                spellParser.parse();
                return;
            }
        }
    }
}
