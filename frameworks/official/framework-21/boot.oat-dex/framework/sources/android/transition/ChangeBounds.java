package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.RectEvaluator;
import android.animation.TypeConverter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.transition.Transition;
import android.util.AttributeSet;
import android.util.IntProperty;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import java.util.Map;

public class ChangeBounds extends Transition {
    private static final Property<Drawable, PointF> DRAWABLE_ORIGIN_PROPERTY = new Property<Drawable, PointF>(PointF.class, "boundsOrigin") {
        /* class android.transition.ChangeBounds.AnonymousClass1 */
        private Rect mBounds = new Rect();

        public void set(Drawable object, PointF value) {
            object.copyBounds(this.mBounds);
            this.mBounds.offsetTo(Math.round(value.x), Math.round(value.y));
            object.setBounds(this.mBounds);
        }

        public PointF get(Drawable object) {
            object.copyBounds(this.mBounds);
            return new PointF((float) this.mBounds.left, (float) this.mBounds.top);
        }
    };
    private static final String LOG_TAG = "ChangeBounds";
    private static final String PROPNAME_BOUNDS = "android:changeBounds:bounds";
    private static final String PROPNAME_PARENT = "android:changeBounds:parent";
    private static final String PROPNAME_WINDOW_X = "android:changeBounds:windowX";
    private static final String PROPNAME_WINDOW_Y = "android:changeBounds:windowY";
    private static RectEvaluator sRectEvaluator = new RectEvaluator();
    private static final String[] sTransitionProperties = {PROPNAME_BOUNDS, PROPNAME_PARENT, PROPNAME_WINDOW_X, PROPNAME_WINDOW_Y};
    boolean mReparent;
    boolean mResizeClip;
    int[] tempLocation;

    public ChangeBounds() {
        this.tempLocation = new int[2];
        this.mResizeClip = false;
        this.mReparent = false;
    }

    public ChangeBounds(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.tempLocation = new int[2];
        this.mResizeClip = false;
        this.mReparent = false;
    }

    @Override // android.transition.Transition
    public String[] getTransitionProperties() {
        return sTransitionProperties;
    }

    public void setResizeClip(boolean resizeClip) {
        this.mResizeClip = resizeClip;
    }

    public void setReparent(boolean reparent) {
        this.mReparent = reparent;
    }

    private void captureValues(TransitionValues values) {
        View view = values.view;
        if (view.isLaidOut() || view.getWidth() != 0 || view.getHeight() != 0) {
            values.values.put(PROPNAME_BOUNDS, new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom()));
            values.values.put(PROPNAME_PARENT, values.view.getParent());
            if (this.mReparent) {
                values.view.getLocationInWindow(this.tempLocation);
                values.values.put(PROPNAME_WINDOW_X, Integer.valueOf(this.tempLocation[0]));
                values.values.put(PROPNAME_WINDOW_Y, Integer.valueOf(this.tempLocation[1]));
            }
        }
    }

    @Override // android.transition.Transition
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override // android.transition.Transition
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private boolean parentMatches(View startParent, View endParent) {
        if (!this.mReparent) {
            return true;
        }
        TransitionValues endValues = getMatchedTransitionValues(startParent, true);
        return endValues == null ? startParent == endParent : endParent == endValues.view;
    }

    @Override // android.transition.Transition
    public Animator createAnimator(final ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        Animator anim;
        if (startValues == null || endValues == null) {
            return null;
        }
        Map<String, Object> startParentVals = startValues.values;
        Map<String, Object> endParentVals = endValues.values;
        ViewGroup startParent = (ViewGroup) startParentVals.get(PROPNAME_PARENT);
        ViewGroup endParent = (ViewGroup) endParentVals.get(PROPNAME_PARENT);
        if (startParent == null || endParent == null) {
            return null;
        }
        final View view = endValues.view;
        if (parentMatches(startParent, endParent)) {
            Rect startBounds = (Rect) startValues.values.get(PROPNAME_BOUNDS);
            Rect endBounds = (Rect) endValues.values.get(PROPNAME_BOUNDS);
            int startLeft = startBounds.left;
            int endLeft = endBounds.left;
            int startTop = startBounds.top;
            int endTop = endBounds.top;
            int startRight = startBounds.right;
            int endRight = endBounds.right;
            int startBottom = startBounds.bottom;
            int endBottom = endBounds.bottom;
            int startWidth = startRight - startLeft;
            int startHeight = startBottom - startTop;
            int endWidth = endRight - endLeft;
            int endHeight = endBottom - endTop;
            int numChanges = 0;
            if (!((startWidth == 0 || startHeight == 0) && (endWidth == 0 || endHeight == 0))) {
                if (!(startLeft == endLeft && startTop == endTop)) {
                    numChanges = 0 + 1;
                }
                if (!(startRight == endRight && startBottom == endBottom)) {
                    numChanges++;
                }
            }
            if (numChanges > 0) {
                if (!this.mResizeClip) {
                    if (startWidth == endWidth && startHeight == endHeight) {
                        view.offsetLeftAndRight(startLeft - view.getLeft());
                        view.offsetTopAndBottom(startTop - view.getTop());
                        anim = ObjectAnimator.ofInt(view, new HorizontalOffsetProperty(), new VerticalOffsetProperty(), getPathMotion().getPath(0.0f, 0.0f, (float) (endLeft - startLeft), (float) (endTop - startTop)));
                    } else {
                        if (startLeft != endLeft) {
                            view.setLeft(startLeft);
                        }
                        if (startTop != endTop) {
                            view.setTop(startTop);
                        }
                        if (startRight != endRight) {
                            view.setRight(startRight);
                        }
                        if (startBottom != endBottom) {
                            view.setBottom(startBottom);
                        }
                        ObjectAnimator topLeftAnimator = null;
                        if (!(startLeft == endLeft && startTop == endTop)) {
                            topLeftAnimator = ObjectAnimator.ofInt(view, "left", "top", getPathMotion().getPath((float) startLeft, (float) startTop, (float) endLeft, (float) endTop));
                        }
                        ObjectAnimator bottomRightAnimator = null;
                        if (!(startRight == endRight && startBottom == endBottom)) {
                            bottomRightAnimator = ObjectAnimator.ofInt(view, "right", "bottom", getPathMotion().getPath((float) startRight, (float) startBottom, (float) endRight, (float) endBottom));
                        }
                        anim = TransitionUtils.mergeAnimators(topLeftAnimator, bottomRightAnimator);
                    }
                    if (!(view.getParent() instanceof ViewGroup)) {
                        return anim;
                    }
                    final ViewGroup parent = (ViewGroup) view.getParent();
                    parent.suppressLayout(true);
                    addListener(new Transition.TransitionListenerAdapter() {
                        /* class android.transition.ChangeBounds.AnonymousClass2 */
                        boolean mCanceled = false;

                        @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
                        public void onTransitionCancel(Transition transition) {
                            parent.suppressLayout(false);
                            this.mCanceled = true;
                        }

                        @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
                        public void onTransitionEnd(Transition transition) {
                            if (!this.mCanceled) {
                                parent.suppressLayout(false);
                            }
                        }

                        @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
                        public void onTransitionPause(Transition transition) {
                            parent.suppressLayout(false);
                        }

                        @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
                        public void onTransitionResume(Transition transition) {
                            parent.suppressLayout(true);
                        }
                    });
                    return anim;
                }
                if (startWidth != endWidth) {
                    view.setRight(Math.max(startWidth, endWidth) + endLeft);
                }
                if (startHeight != endHeight) {
                    view.setBottom(Math.max(startHeight, endHeight) + endTop);
                }
                if (startLeft != endLeft) {
                    view.setTranslationX((float) (startLeft - endLeft));
                }
                if (startTop != endTop) {
                    view.setTranslationY((float) (startTop - endTop));
                }
                float transXDelta = (float) (endLeft - startLeft);
                float transYDelta = (float) (endTop - startTop);
                int widthDelta = endWidth - startWidth;
                int heightDelta = endHeight - startHeight;
                int numChanges2 = 0;
                if (transXDelta != 0.0f) {
                    numChanges2 = 0 + 1;
                }
                if (transYDelta != 0.0f) {
                    numChanges2++;
                }
                if (!(widthDelta == 0 && heightDelta == 0)) {
                    int numChanges3 = numChanges2 + 1;
                }
                ObjectAnimator translationAnimator = null;
                if (!(transXDelta == 0.0f && transYDelta == 0.0f)) {
                    translationAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, View.TRANSLATION_Y, getPathMotion().getPath(0.0f, 0.0f, transXDelta, transYDelta));
                }
                ObjectAnimator clipAnimator = null;
                if (!(widthDelta == 0 && heightDelta == 0)) {
                    Rect tempStartBounds = new Rect(0, 0, startWidth, startHeight);
                    Rect tempEndBounds = new Rect(0, 0, endWidth, endHeight);
                    clipAnimator = ObjectAnimator.ofObject(view, "clipBounds", sRectEvaluator, tempStartBounds, tempEndBounds);
                }
                Animator anim2 = TransitionUtils.mergeAnimators(translationAnimator, clipAnimator);
                if (view.getParent() instanceof ViewGroup) {
                    final ViewGroup parent2 = (ViewGroup) view.getParent();
                    parent2.suppressLayout(true);
                    addListener(new Transition.TransitionListenerAdapter() {
                        /* class android.transition.ChangeBounds.AnonymousClass3 */
                        boolean mCanceled = false;

                        @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
                        public void onTransitionCancel(Transition transition) {
                            parent2.suppressLayout(false);
                            this.mCanceled = true;
                        }

                        @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
                        public void onTransitionEnd(Transition transition) {
                            if (!this.mCanceled) {
                                parent2.suppressLayout(false);
                            }
                        }

                        @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
                        public void onTransitionPause(Transition transition) {
                            parent2.suppressLayout(false);
                        }

                        @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
                        public void onTransitionResume(Transition transition) {
                            parent2.suppressLayout(true);
                        }
                    });
                }
                anim2.addListener(new AnimatorListenerAdapter() {
                    /* class android.transition.ChangeBounds.AnonymousClass4 */

                    public void onAnimationEnd(Animator animation) {
                        view.setClipBounds(null);
                    }
                });
                return anim2;
            }
        } else {
            int startX = ((Integer) startValues.values.get(PROPNAME_WINDOW_X)).intValue();
            int startY = ((Integer) startValues.values.get(PROPNAME_WINDOW_Y)).intValue();
            int endX = ((Integer) endValues.values.get(PROPNAME_WINDOW_X)).intValue();
            int endY = ((Integer) endValues.values.get(PROPNAME_WINDOW_Y)).intValue();
            if (!(startX == endX && startY == endY)) {
                sceneRoot.getLocationInWindow(this.tempLocation);
                Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                view.draw(new Canvas(bitmap));
                final BitmapDrawable drawable = new BitmapDrawable(bitmap);
                final float transitionAlpha = view.getTransitionAlpha();
                view.setTransitionAlpha(0.0f);
                sceneRoot.getOverlay().add(drawable);
                ObjectAnimator anim3 = ObjectAnimator.ofPropertyValuesHolder(drawable, PropertyValuesHolder.ofObject(DRAWABLE_ORIGIN_PROPERTY, (TypeConverter) null, getPathMotion().getPath((float) (startX - this.tempLocation[0]), (float) (startY - this.tempLocation[1]), (float) (endX - this.tempLocation[0]), (float) (endY - this.tempLocation[1]))));
                anim3.addListener(new AnimatorListenerAdapter() {
                    /* class android.transition.ChangeBounds.AnonymousClass5 */

                    public void onAnimationEnd(Animator animation) {
                        sceneRoot.getOverlay().remove(drawable);
                        view.setTransitionAlpha(transitionAlpha);
                    }
                });
                return anim3;
            }
        }
        return null;
    }

    private static abstract class OffsetProperty extends IntProperty<View> {
        int mPreviousValue;

        /* access modifiers changed from: protected */
        public abstract void offsetBy(View view, int i);

        public OffsetProperty(String name) {
            super(name);
        }

        public void setValue(View view, int value) {
            offsetBy(view, value - this.mPreviousValue);
            this.mPreviousValue = value;
        }

        public Integer get(View object) {
            return null;
        }
    }

    private static class HorizontalOffsetProperty extends OffsetProperty {
        public HorizontalOffsetProperty() {
            super("offsetLeftAndRight");
        }

        /* access modifiers changed from: protected */
        @Override // android.transition.ChangeBounds.OffsetProperty
        public void offsetBy(View view, int by) {
            view.offsetLeftAndRight(by);
        }
    }

    private static class VerticalOffsetProperty extends OffsetProperty {
        public VerticalOffsetProperty() {
            super("offsetTopAndBottom");
        }

        /* access modifiers changed from: protected */
        @Override // android.transition.ChangeBounds.OffsetProperty
        public void offsetBy(View view, int by) {
            view.offsetTopAndBottom(by);
        }
    }
}
