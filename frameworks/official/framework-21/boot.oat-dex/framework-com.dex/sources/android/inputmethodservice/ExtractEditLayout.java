package android.inputmethodservice;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.android.internal.R;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuPopupHelper;

public class ExtractEditLayout extends LinearLayout {
    ExtractActionMode mActionMode;
    Button mEditButton;
    Button mExtractActionButton;

    public ExtractEditLayout(Context context) {
        super(context);
    }

    public ExtractEditLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public ActionMode startActionModeForChild(View sourceView, ActionMode.Callback cb) {
        ExtractActionMode mode = new ExtractActionMode(cb);
        if (!mode.dispatchOnCreate()) {
            return null;
        }
        mode.invalidate();
        this.mExtractActionButton.setVisibility(4);
        this.mEditButton.setVisibility(0);
        this.mActionMode = mode;
        sendAccessibilityEvent(32);
        return mode;
    }

    public boolean isActionModeStarted() {
        return this.mActionMode != null;
    }

    public void finishActionMode() {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
    }

    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mExtractActionButton = (Button) findViewById(R.id.inputExtractAction);
        this.mEditButton = (Button) findViewById(R.id.inputExtractEditButton);
        this.mEditButton.setOnClickListener(new View.OnClickListener() {
            /* class android.inputmethodservice.ExtractEditLayout.AnonymousClass1 */

            @Override // android.view.View.OnClickListener
            public void onClick(View clicked) {
                if (ExtractEditLayout.this.mActionMode != null) {
                    new MenuPopupHelper(ExtractEditLayout.this.getContext(), ExtractEditLayout.this.mActionMode.mMenu, clicked).show();
                }
            }
        });
    }

    private class ExtractActionMode extends ActionMode implements MenuBuilder.Callback {
        private ActionMode.Callback mCallback;
        MenuBuilder mMenu;

        public ExtractActionMode(ActionMode.Callback cb) {
            this.mMenu = new MenuBuilder(ExtractEditLayout.this.getContext());
            this.mMenu.setCallback(this);
            this.mCallback = cb;
        }

        @Override // android.view.ActionMode
        public void setTitle(CharSequence title) {
        }

        @Override // android.view.ActionMode
        public void setTitle(int resId) {
        }

        @Override // android.view.ActionMode
        public void setSubtitle(CharSequence subtitle) {
        }

        @Override // android.view.ActionMode
        public void setSubtitle(int resId) {
        }

        @Override // android.view.ActionMode
        public boolean isTitleOptional() {
            return true;
        }

        @Override // android.view.ActionMode
        public void setCustomView(View view) {
        }

        @Override // android.view.ActionMode
        public void invalidate() {
            this.mMenu.stopDispatchingItemsChanged();
            try {
                this.mCallback.onPrepareActionMode(this, this.mMenu);
            } finally {
                this.mMenu.startDispatchingItemsChanged();
            }
        }

        public boolean dispatchOnCreate() {
            this.mMenu.stopDispatchingItemsChanged();
            try {
                return this.mCallback.onCreateActionMode(this, this.mMenu);
            } finally {
                this.mMenu.startDispatchingItemsChanged();
            }
        }

        @Override // android.view.ActionMode
        public void finish() {
            if (ExtractEditLayout.this.mActionMode == this) {
                this.mCallback.onDestroyActionMode(this);
                this.mCallback = null;
                ExtractEditLayout.this.mExtractActionButton.setVisibility(0);
                ExtractEditLayout.this.mEditButton.setVisibility(4);
                ExtractEditLayout.this.sendAccessibilityEvent(32);
                ExtractEditLayout.this.mActionMode = null;
            }
        }

        @Override // android.view.ActionMode
        public Menu getMenu() {
            return this.mMenu;
        }

        @Override // android.view.ActionMode
        public CharSequence getTitle() {
            return null;
        }

        @Override // android.view.ActionMode
        public CharSequence getSubtitle() {
            return null;
        }

        @Override // android.view.ActionMode
        public View getCustomView() {
            return null;
        }

        @Override // android.view.ActionMode
        public MenuInflater getMenuInflater() {
            return new MenuInflater(ExtractEditLayout.this.getContext());
        }

        @Override // com.android.internal.view.menu.MenuBuilder.Callback
        public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            if (this.mCallback != null) {
                return this.mCallback.onActionItemClicked(this, item);
            }
            return false;
        }

        @Override // com.android.internal.view.menu.MenuBuilder.Callback
        public void onMenuModeChange(MenuBuilder menu) {
        }
    }
}
