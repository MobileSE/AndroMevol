package android.widget;

class ListPopupWindow$ResizePopupRunnable implements Runnable {
    final /* synthetic */ ListPopupWindow this$0;

    private ListPopupWindow$ResizePopupRunnable(ListPopupWindow listPopupWindow) {
        this.this$0 = listPopupWindow;
    }

    public void run() {
        if (ListPopupWindow.access$600(this.this$0) != null && ListPopupWindow.access$600(this.this$0).getCount() > ListPopupWindow.access$600(this.this$0).getChildCount() && ListPopupWindow.access$600(this.this$0).getChildCount() <= this.this$0.mListItemExpandMaximum) {
            ListPopupWindow.access$1100(this.this$0).setInputMethodMode(2);
            this.this$0.show();
        }
    }
}
