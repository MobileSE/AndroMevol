package com.android.internal.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.ActionMenuPresenter;
import android.widget.ActionMenuView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.view.menu.MenuBuilder;

public class ActionBarContextView extends AbsActionBarView implements Animator.AnimatorListener {
    private static final int ANIMATE_IDLE = 0;
    private static final int ANIMATE_IN = 1;
    private static final int ANIMATE_OUT = 2;
    private static final String TAG = "ActionBarContextView";
    private boolean mAnimateInOnLayout;
    private int mAnimationMode;
    private View mClose;
    private int mCloseItemLayout;
    private Animator mCurrentAnimation;
    private View mCustomView;
    private Drawable mSplitBackground;
    private CharSequence mSubtitle;
    private int mSubtitleStyleRes;
    private TextView mSubtitleView;
    private CharSequence mTitle;
    private LinearLayout mTitleLayout;
    private boolean mTitleOptional;
    private int mTitleStyleRes;
    private TextView mTitleView;

    public ActionBarContextView(Context context) {
        this(context, null);
    }

    public ActionBarContextView(Context context, AttributeSet attrs) {
        this(context, attrs, 16843668);
    }

    public ActionBarContextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ActionBarContextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionMode, defStyleAttr, defStyleRes);
        setBackground(a.getDrawable(0));
        this.mTitleStyleRes = a.getResourceId(2, 0);
        this.mSubtitleStyleRes = a.getResourceId(3, 0);
        this.mContentHeight = a.getLayoutDimension(1, 0);
        this.mSplitBackground = a.getDrawable(4);
        this.mCloseItemLayout = a.getResourceId(5, R.layout.action_mode_close_item);
        a.recycle();
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.hideOverflowMenu();
            this.mActionMenuPresenter.hideSubMenus();
        }
    }

    @Override // com.android.internal.widget.AbsActionBarView
    public void setSplitToolbar(boolean split) {
        if (this.mSplitActionBar != split) {
            if (this.mActionMenuPresenter != null) {
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-2, -1);
                if (!split) {
                    this.mMenuView = (ActionMenuView) this.mActionMenuPresenter.getMenuView(this);
                    this.mMenuView.setBackground(null);
                    ViewGroup oldParent = (ViewGroup) this.mMenuView.getParent();
                    if (oldParent != null) {
                        oldParent.removeView(this.mMenuView);
                    }
                    addView(this.mMenuView, layoutParams);
                } else {
                    this.mActionMenuPresenter.setWidthLimit(getContext().getResources().getDisplayMetrics().widthPixels, true);
                    this.mActionMenuPresenter.setItemLimit(Integer.MAX_VALUE);
                    layoutParams.width = -1;
                    layoutParams.height = this.mContentHeight;
                    this.mMenuView = (ActionMenuView) this.mActionMenuPresenter.getMenuView(this);
                    this.mMenuView.setBackground(this.mSplitBackground);
                    ViewGroup oldParent2 = (ViewGroup) this.mMenuView.getParent();
                    if (oldParent2 != null) {
                        oldParent2.removeView(this.mMenuView);
                    }
                    this.mSplitView.addView(this.mMenuView, layoutParams);
                }
            }
            super.setSplitToolbar(split);
        }
    }

    @Override // com.android.internal.widget.AbsActionBarView
    public void setContentHeight(int height) {
        this.mContentHeight = height;
    }

    public void setCustomView(View view) {
        if (this.mCustomView != null) {
            removeView(this.mCustomView);
        }
        this.mCustomView = view;
        if (this.mTitleLayout != null) {
            removeView(this.mTitleLayout);
            this.mTitleLayout = null;
        }
        if (view != null) {
            addView(view);
        }
        requestLayout();
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
        initTitle();
    }

    public void setSubtitle(CharSequence subtitle) {
        this.mSubtitle = subtitle;
        initTitle();
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public CharSequence getSubtitle() {
        return this.mSubtitle;
    }

    private void initTitle() {
        boolean hasTitle;
        boolean hasSubtitle;
        int i;
        int i2 = 8;
        if (this.mTitleLayout == null) {
            LayoutInflater.from(getContext()).inflate(R.layout.action_bar_title_item, this);
            this.mTitleLayout = (LinearLayout) getChildAt(getChildCount() - 1);
            this.mTitleView = (TextView) this.mTitleLayout.findViewById(R.id.action_bar_title);
            this.mSubtitleView = (TextView) this.mTitleLayout.findViewById(R.id.action_bar_subtitle);
            if (this.mTitleStyleRes != 0) {
                this.mTitleView.setTextAppearance(this.mContext, this.mTitleStyleRes);
            }
            if (this.mSubtitleStyleRes != 0) {
                this.mSubtitleView.setTextAppearance(this.mContext, this.mSubtitleStyleRes);
            }
        }
        this.mTitleView.setText(this.mTitle);
        this.mSubtitleView.setText(this.mSubtitle);
        if (!TextUtils.isEmpty(this.mTitle)) {
            hasTitle = true;
        } else {
            hasTitle = false;
        }
        if (!TextUtils.isEmpty(this.mSubtitle)) {
            hasSubtitle = true;
        } else {
            hasSubtitle = false;
        }
        TextView textView = this.mSubtitleView;
        if (hasSubtitle) {
            i = 0;
        } else {
            i = 8;
        }
        textView.setVisibility(i);
        LinearLayout linearLayout = this.mTitleLayout;
        if (hasTitle || hasSubtitle) {
            i2 = 0;
        }
        linearLayout.setVisibility(i2);
        if (this.mTitleLayout.getParent() == null) {
            addView(this.mTitleLayout);
        }
    }

    public void initForMode(final ActionMode mode) {
        if (this.mClose == null) {
            this.mClose = LayoutInflater.from(this.mContext).inflate(this.mCloseItemLayout, (ViewGroup) this, false);
            addView(this.mClose);
        } else if (this.mClose.getParent() == null) {
            addView(this.mClose);
        }
        this.mClose.findViewById(R.id.action_mode_close_button).setOnClickListener(new View.OnClickListener() {
            /* class com.android.internal.widget.ActionBarContextView.AnonymousClass1 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                mode.finish();
            }
        });
        MenuBuilder menu = (MenuBuilder) mode.getMenu();
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.dismissPopupMenus();
        }
        this.mActionMenuPresenter = new ActionMenuPresenter(this.mContext);
        this.mActionMenuPresenter.setReserveOverflow(true);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-2, -1);
        if (!this.mSplitActionBar) {
            menu.addMenuPresenter(this.mActionMenuPresenter, this.mPopupContext);
            this.mMenuView = (ActionMenuView) this.mActionMenuPresenter.getMenuView(this);
            this.mMenuView.setBackgroundDrawable(null);
            addView(this.mMenuView, layoutParams);
        } else {
            this.mActionMenuPresenter.setWidthLimit(getContext().getResources().getDisplayMetrics().widthPixels, true);
            this.mActionMenuPresenter.setItemLimit(Integer.MAX_VALUE);
            layoutParams.width = -1;
            layoutParams.height = this.mContentHeight;
            menu.addMenuPresenter(this.mActionMenuPresenter, this.mPopupContext);
            this.mMenuView = (ActionMenuView) this.mActionMenuPresenter.getMenuView(this);
            this.mMenuView.setBackgroundDrawable(this.mSplitBackground);
            this.mSplitView.addView(this.mMenuView, layoutParams);
        }
        this.mAnimateInOnLayout = true;
    }

    public void closeMode() {
        if (this.mAnimationMode != 2) {
            if (this.mClose == null) {
                killMode();
                return;
            }
            finishAnimation();
            this.mAnimationMode = 2;
            this.mCurrentAnimation = makeOutAnimation();
            this.mCurrentAnimation.start();
        }
    }

    private void finishAnimation() {
        Animator a = this.mCurrentAnimation;
        if (a != null) {
            this.mCurrentAnimation = null;
            a.end();
        }
    }

    public void killMode() {
        finishAnimation();
        removeAllViews();
        if (this.mSplitView != null) {
            this.mSplitView.removeView(this.mMenuView);
        }
        this.mCustomView = null;
        this.mMenuView = null;
        this.mAnimateInOnLayout = false;
    }

    @Override // com.android.internal.widget.AbsActionBarView
    public boolean showOverflowMenu() {
        if (this.mActionMenuPresenter != null) {
            return this.mActionMenuPresenter.showOverflowMenu();
        }
        return false;
    }

    @Override // com.android.internal.widget.AbsActionBarView
    public boolean hideOverflowMenu() {
        if (this.mActionMenuPresenter != null) {
            return this.mActionMenuPresenter.hideOverflowMenu();
        }
        return false;
    }

    @Override // com.android.internal.widget.AbsActionBarView
    public boolean isOverflowMenuShowing() {
        if (this.mActionMenuPresenter != null) {
            return this.mActionMenuPresenter.isOverflowMenuShowing();
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new ViewGroup.MarginLayoutParams(-1, -2);
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ViewGroup.MarginLayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int customWidth;
        int customHeight;
        if (View.MeasureSpec.getMode(widthMeasureSpec) != 1073741824) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used " + "with android:layout_width=\"match_parent\" (or fill_parent)");
        } else if (View.MeasureSpec.getMode(heightMeasureSpec) == 0) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used " + "with android:layout_height=\"wrap_content\"");
        } else {
            int contentWidth = View.MeasureSpec.getSize(widthMeasureSpec);
            int maxHeight = this.mContentHeight > 0 ? this.mContentHeight : View.MeasureSpec.getSize(heightMeasureSpec);
            int verticalPadding = getPaddingTop() + getPaddingBottom();
            int availableWidth = (contentWidth - getPaddingLeft()) - getPaddingRight();
            int height = maxHeight - verticalPadding;
            int childSpecHeight = View.MeasureSpec.makeMeasureSpec(height, Integer.MIN_VALUE);
            if (this.mClose != null) {
                int availableWidth2 = measureChildView(this.mClose, availableWidth, childSpecHeight, 0);
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) this.mClose.getLayoutParams();
                availableWidth = availableWidth2 - (lp.leftMargin + lp.rightMargin);
            }
            if (this.mMenuView != null && this.mMenuView.getParent() == this) {
                availableWidth = measureChildView(this.mMenuView, availableWidth, childSpecHeight, 0);
            }
            if (this.mTitleLayout != null && this.mCustomView == null) {
                if (this.mTitleOptional) {
                    this.mTitleLayout.measure(View.MeasureSpec.makeMeasureSpec(0, 0), childSpecHeight);
                    int titleWidth = this.mTitleLayout.getMeasuredWidth();
                    boolean titleFits = titleWidth <= availableWidth;
                    if (titleFits) {
                        availableWidth -= titleWidth;
                    }
                    this.mTitleLayout.setVisibility(titleFits ? 0 : 8);
                } else {
                    availableWidth = measureChildView(this.mTitleLayout, availableWidth, childSpecHeight, 0);
                }
            }
            if (this.mCustomView != null) {
                ViewGroup.LayoutParams lp2 = this.mCustomView.getLayoutParams();
                int customWidthMode = lp2.width != -2 ? 1073741824 : Integer.MIN_VALUE;
                if (lp2.width >= 0) {
                    customWidth = Math.min(lp2.width, availableWidth);
                } else {
                    customWidth = availableWidth;
                }
                int customHeightMode = lp2.height != -2 ? 1073741824 : Integer.MIN_VALUE;
                if (lp2.height >= 0) {
                    customHeight = Math.min(lp2.height, height);
                } else {
                    customHeight = height;
                }
                this.mCustomView.measure(View.MeasureSpec.makeMeasureSpec(customWidth, customWidthMode), View.MeasureSpec.makeMeasureSpec(customHeight, customHeightMode));
            }
            if (this.mContentHeight <= 0) {
                int measuredHeight = 0;
                int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    int paddedViewHeight = getChildAt(i).getMeasuredHeight() + verticalPadding;
                    if (paddedViewHeight > measuredHeight) {
                        measuredHeight = paddedViewHeight;
                    }
                }
                setMeasuredDimension(contentWidth, measuredHeight);
                return;
            }
            setMeasuredDimension(contentWidth, maxHeight);
        }
    }

    private Animator makeInAnimation() {
        int count;
        this.mClose.setTranslationX((float) ((-this.mClose.getWidth()) - ((ViewGroup.MarginLayoutParams) this.mClose.getLayoutParams()).leftMargin));
        ObjectAnimator buttonAnimator = ObjectAnimator.ofFloat(this.mClose, "translationX", 0.0f);
        buttonAnimator.setDuration(200L);
        buttonAnimator.addListener(this);
        buttonAnimator.setInterpolator(new DecelerateInterpolator());
        AnimatorSet set = new AnimatorSet();
        AnimatorSet.Builder b = set.play(buttonAnimator);
        if (this.mMenuView != null && (count = this.mMenuView.getChildCount()) > 0) {
            int i = count - 1;
            int j = 0;
            while (i >= 0) {
                View child = this.mMenuView.getChildAt(i);
                child.setScaleY(0.0f);
                ObjectAnimator a = ObjectAnimator.ofFloat(child, "scaleY", 0.0f, 1.0f);
                a.setDuration(300L);
                b.with(a);
                i--;
                j++;
            }
        }
        return set;
    }

    private Animator makeOutAnimation() {
        ObjectAnimator buttonAnimator = ObjectAnimator.ofFloat(this.mClose, "translationX", (float) ((-this.mClose.getWidth()) - ((ViewGroup.MarginLayoutParams) this.mClose.getLayoutParams()).leftMargin));
        buttonAnimator.setDuration(200L);
        buttonAnimator.addListener(this);
        buttonAnimator.setInterpolator(new DecelerateInterpolator());
        AnimatorSet set = new AnimatorSet();
        AnimatorSet.Builder b = set.play(buttonAnimator);
        if (this.mMenuView != null && this.mMenuView.getChildCount() > 0) {
            for (int i = 0; i < 0; i++) {
                View child = this.mMenuView.getChildAt(i);
                child.setScaleY(0.0f);
                ObjectAnimator a = ObjectAnimator.ofFloat(child, "scaleY", 0.0f);
                a.setDuration(300L);
                b.with(a);
            }
        }
        return set;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        boolean isLayoutRtl = isLayoutRtl();
        int x = isLayoutRtl ? (r - l) - getPaddingRight() : getPaddingLeft();
        int y = getPaddingTop();
        int contentHeight = ((b - t) - getPaddingTop()) - getPaddingBottom();
        if (!(this.mClose == null || this.mClose.getVisibility() == 8)) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) this.mClose.getLayoutParams();
            int startMargin = isLayoutRtl ? lp.rightMargin : lp.leftMargin;
            int endMargin = isLayoutRtl ? lp.leftMargin : lp.rightMargin;
            int x2 = next(x, startMargin, isLayoutRtl);
            x = next(x2 + positionChild(this.mClose, x2, y, contentHeight, isLayoutRtl), endMargin, isLayoutRtl);
            if (this.mAnimateInOnLayout) {
                this.mAnimationMode = 1;
                this.mCurrentAnimation = makeInAnimation();
                this.mCurrentAnimation.start();
                this.mAnimateInOnLayout = false;
            }
        }
        if (!(this.mTitleLayout == null || this.mCustomView != null || this.mTitleLayout.getVisibility() == 8)) {
            x += positionChild(this.mTitleLayout, x, y, contentHeight, isLayoutRtl);
        }
        if (this.mCustomView != null) {
            int x3 = x + positionChild(this.mCustomView, x, y, contentHeight, isLayoutRtl);
        }
        int x4 = isLayoutRtl ? getPaddingLeft() : (r - l) - getPaddingRight();
        if (this.mMenuView != null) {
            int x5 = x4 + positionChild(this.mMenuView, x4, y, contentHeight, !isLayoutRtl);
        }
    }

    @Override // android.animation.Animator.AnimatorListener
    public void onAnimationStart(Animator animation) {
    }

    @Override // android.animation.Animator.AnimatorListener
    public void onAnimationEnd(Animator animation) {
        if (this.mAnimationMode == 2) {
            killMode();
        }
        this.mAnimationMode = 0;
    }

    @Override // android.animation.Animator.AnimatorListener
    public void onAnimationCancel(Animator animation) {
    }

    @Override // android.animation.Animator.AnimatorListener
    public void onAnimationRepeat(Animator animation) {
    }

    @Override // android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == 32) {
            event.setSource(this);
            event.setClassName(getClass().getName());
            event.setPackageName(getContext().getPackageName());
            event.setContentDescription(this.mTitle);
            return;
        }
        super.onInitializeAccessibilityEvent(event);
    }

    public void setTitleOptional(boolean titleOptional) {
        if (titleOptional != this.mTitleOptional) {
            requestLayout();
        }
        this.mTitleOptional = titleOptional;
    }

    public boolean isTitleOptional() {
        return this.mTitleOptional;
    }
}
