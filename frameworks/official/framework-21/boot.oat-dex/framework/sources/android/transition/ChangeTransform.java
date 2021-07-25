package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.transition.Transition;
import android.transition.TransitionUtils;
import android.util.AttributeSet;
import android.util.Property;
import android.view.GhostView;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.R;

public class ChangeTransform extends Transition {
    private static final Property<View, Matrix> ANIMATION_MATRIX_PROPERTY = new Property<View, Matrix>(Matrix.class, "animationMatrix") {
        /* class android.transition.ChangeTransform.AnonymousClass1 */

        public Matrix get(View object) {
            return null;
        }

        public void set(View object, Matrix value) {
            object.setAnimationMatrix(value);
        }
    };
    private static final String PROPNAME_INTERMEDIATE_MATRIX = "android:changeTransform:intermediateMatrix";
    private static final String PROPNAME_INTERMEDIATE_PARENT_MATRIX = "android:changeTransform:intermediateParentMatrix";
    private static final String PROPNAME_MATRIX = "android:changeTransform:matrix";
    private static final String PROPNAME_PARENT = "android:changeTransform:parent";
    private static final String PROPNAME_PARENT_MATRIX = "android:changeTransform:parentMatrix";
    private static final String PROPNAME_TRANSFORMS = "android:changeTransform:transforms";
    private static final String TAG = "ChangeTransform";
    private static final String[] sTransitionProperties = {PROPNAME_MATRIX, PROPNAME_TRANSFORMS, PROPNAME_PARENT_MATRIX};
    private boolean mReparent = true;
    private Matrix mTempMatrix = new Matrix();
    private boolean mUseOverlay = true;

    public ChangeTransform() {
    }

    public ChangeTransform(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ChangeTransform);
        this.mUseOverlay = a.getBoolean(1, true);
        this.mReparent = a.getBoolean(0, true);
        a.recycle();
    }

    public boolean getReparentWithOverlay() {
        return this.mUseOverlay;
    }

    public void setReparentWithOverlay(boolean reparentWithOverlay) {
        this.mUseOverlay = reparentWithOverlay;
    }

    public boolean getReparent() {
        return this.mReparent;
    }

    public void setReparent(boolean reparent) {
        this.mReparent = reparent;
    }

    @Override // android.transition.Transition
    public String[] getTransitionProperties() {
        return sTransitionProperties;
    }

    private void captureValues(TransitionValues transitionValues) {
        Matrix matrix;
        View view = transitionValues.view;
        if (view.getVisibility() != 8) {
            transitionValues.values.put(PROPNAME_PARENT, view.getParent());
            transitionValues.values.put(PROPNAME_TRANSFORMS, new Transforms(view));
            Matrix matrix2 = view.getMatrix();
            if (matrix2 == null || matrix2.isIdentity()) {
                matrix = null;
            } else {
                matrix = new Matrix(matrix2);
            }
            transitionValues.values.put(PROPNAME_MATRIX, matrix);
            if (this.mReparent) {
                Matrix parentMatrix = new Matrix();
                ViewGroup parent = (ViewGroup) view.getParent();
                parent.transformMatrixToGlobal(parentMatrix);
                parentMatrix.preTranslate((float) (-parent.getScrollX()), (float) (-parent.getScrollY()));
                transitionValues.values.put(PROPNAME_PARENT_MATRIX, parentMatrix);
                transitionValues.values.put(PROPNAME_INTERMEDIATE_MATRIX, view.getTag(16908358));
                transitionValues.values.put(PROPNAME_INTERMEDIATE_PARENT_MATRIX, view.getTag(16908359));
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

    @Override // android.transition.Transition
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null || !startValues.values.containsKey(PROPNAME_PARENT) || !endValues.values.containsKey(PROPNAME_PARENT)) {
            return null;
        }
        boolean handleParentChange = this.mReparent && !parentsMatch((ViewGroup) startValues.values.get(PROPNAME_PARENT), (ViewGroup) endValues.values.get(PROPNAME_PARENT));
        Matrix startMatrix = (Matrix) startValues.values.get(PROPNAME_INTERMEDIATE_MATRIX);
        if (startMatrix != null) {
            startValues.values.put(PROPNAME_MATRIX, startMatrix);
        }
        Matrix startParentMatrix = (Matrix) startValues.values.get(PROPNAME_INTERMEDIATE_PARENT_MATRIX);
        if (startParentMatrix != null) {
            startValues.values.put(PROPNAME_PARENT_MATRIX, startParentMatrix);
        }
        if (handleParentChange) {
            setMatricesForParent(startValues, endValues);
        }
        ObjectAnimator transformAnimator = createTransformAnimator(startValues, endValues, handleParentChange);
        if (!handleParentChange || transformAnimator == null || !this.mUseOverlay) {
            return transformAnimator;
        }
        createGhostView(sceneRoot, startValues, endValues);
        return transformAnimator;
    }

    private ObjectAnimator createTransformAnimator(TransitionValues startValues, TransitionValues endValues, final boolean handleParentChange) {
        Matrix startMatrix = (Matrix) startValues.values.get(PROPNAME_MATRIX);
        final Matrix endMatrix = (Matrix) endValues.values.get(PROPNAME_MATRIX);
        if (startMatrix == null) {
            startMatrix = Matrix.IDENTITY_MATRIX;
        }
        if (endMatrix == null) {
            endMatrix = Matrix.IDENTITY_MATRIX;
        }
        if (startMatrix.equals(endMatrix)) {
            return null;
        }
        final Transforms transforms = (Transforms) endValues.values.get(PROPNAME_TRANSFORMS);
        final View view = endValues.view;
        setIdentityTransforms(view);
        ObjectAnimator animator = ObjectAnimator.ofObject(view, ANIMATION_MATRIX_PROPERTY, new TransitionUtils.MatrixEvaluator(), startMatrix, endMatrix);
        AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
            /* class android.transition.ChangeTransform.AnonymousClass2 */
            private boolean mIsCanceled;
            private Matrix mTempMatrix = new Matrix();

            public void onAnimationCancel(Animator animation) {
                this.mIsCanceled = true;
            }

            public void onAnimationEnd(Animator animation) {
                if (!this.mIsCanceled) {
                    if (!handleParentChange || !ChangeTransform.this.mUseOverlay) {
                        view.setTagInternal(16908358, null);
                        view.setTagInternal(16908359, null);
                    } else {
                        setCurrentMatrix(endMatrix);
                    }
                }
                ChangeTransform.ANIMATION_MATRIX_PROPERTY.set(view, null);
                transforms.restore(view);
            }

            public void onAnimationPause(Animator animation) {
                setCurrentMatrix((Matrix) ((ValueAnimator) animation).getAnimatedValue());
            }

            public void onAnimationResume(Animator animation) {
                ChangeTransform.setIdentityTransforms(view);
            }

            private void setCurrentMatrix(Matrix currentMatrix) {
                this.mTempMatrix.set(currentMatrix);
                view.setTagInternal(16908358, this.mTempMatrix);
                transforms.restore(view);
            }
        };
        animator.addListener(listener);
        animator.addPauseListener(listener);
        return animator;
    }

    private boolean parentsMatch(ViewGroup startParent, ViewGroup endParent) {
        if (!isValidTarget(startParent) || !isValidTarget(endParent)) {
            return startParent == endParent;
        }
        TransitionValues endValues = getMatchedTransitionValues(startParent, true);
        if (endValues != null) {
            return endParent == endValues.view;
        }
        return false;
    }

    private void createGhostView(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        View view = endValues.view;
        Matrix endMatrix = (Matrix) endValues.values.get(PROPNAME_PARENT_MATRIX);
        Matrix localEndMatrix = new Matrix(endMatrix);
        sceneRoot.transformMatrixToLocal(localEndMatrix);
        GhostView ghostView = GhostView.addGhost(view, sceneRoot, localEndMatrix);
        Transition outerTransition = this;
        while (outerTransition.mParent != null) {
            outerTransition = outerTransition.mParent;
        }
        outerTransition.addListener(new GhostListener(view, ghostView, endMatrix));
        if (startValues.view != endValues.view) {
            startValues.view.setTransitionAlpha(0.0f);
        }
        view.setTransitionAlpha(1.0f);
    }

    private void setMatricesForParent(TransitionValues startValues, TransitionValues endValues) {
        Matrix endParentMatrix = (Matrix) endValues.values.get(PROPNAME_PARENT_MATRIX);
        endValues.view.setTagInternal(16908359, endParentMatrix);
        Matrix toLocal = this.mTempMatrix;
        toLocal.reset();
        endParentMatrix.invert(toLocal);
        Matrix startLocal = (Matrix) startValues.values.get(PROPNAME_MATRIX);
        if (startLocal == null) {
            startLocal = new Matrix();
            startValues.values.put(PROPNAME_MATRIX, startLocal);
        }
        startLocal.postConcat((Matrix) startValues.values.get(PROPNAME_PARENT_MATRIX));
        startLocal.postConcat(toLocal);
    }

    /* access modifiers changed from: private */
    public static void setIdentityTransforms(View view) {
        setTransforms(view, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f);
    }

    /* access modifiers changed from: private */
    public static void setTransforms(View view, float translationX, float translationY, float translationZ, float scaleX, float scaleY, float rotationX, float rotationY, float rotationZ) {
        view.setTranslationX(translationX);
        view.setTranslationY(translationY);
        view.setTranslationZ(translationZ);
        view.setScaleX(scaleX);
        view.setScaleY(scaleY);
        view.setRotationX(rotationX);
        view.setRotationY(rotationY);
        view.setRotation(rotationZ);
    }

    /* access modifiers changed from: private */
    public static class Transforms {
        public final float rotationX;
        public final float rotationY;
        public final float rotationZ;
        public final float scaleX;
        public final float scaleY;
        public final float translationX;
        public final float translationY;
        public final float translationZ;

        public Transforms(View view) {
            this.translationX = view.getTranslationX();
            this.translationY = view.getTranslationY();
            this.translationZ = view.getTranslationZ();
            this.scaleX = view.getScaleX();
            this.scaleY = view.getScaleY();
            this.rotationX = view.getRotationX();
            this.rotationY = view.getRotationY();
            this.rotationZ = view.getRotation();
        }

        public void restore(View view) {
            ChangeTransform.setTransforms(view, this.translationX, this.translationY, this.translationZ, this.scaleX, this.scaleY, this.rotationX, this.rotationY, this.rotationZ);
        }

        public boolean equals(Object that) {
            if (!(that instanceof Transforms)) {
                return false;
            }
            Transforms thatTransform = (Transforms) that;
            if (thatTransform.translationX == this.translationX && thatTransform.translationY == this.translationY && thatTransform.translationZ == this.translationZ && thatTransform.scaleX == this.scaleX && thatTransform.scaleY == this.scaleY && thatTransform.rotationX == this.rotationX && thatTransform.rotationY == this.rotationY && thatTransform.rotationZ == this.rotationZ) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class GhostListener extends Transition.TransitionListenerAdapter {
        private Matrix mEndMatrix;
        private GhostView mGhostView;
        private View mView;

        public GhostListener(View view, GhostView ghostView, Matrix endMatrix) {
            this.mView = view;
            this.mGhostView = ghostView;
            this.mEndMatrix = endMatrix;
        }

        @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
        public void onTransitionEnd(Transition transition) {
            transition.removeListener(this);
            GhostView.removeGhost(this.mView);
            this.mView.setTagInternal(16908358, null);
            this.mView.setTagInternal(16908359, null);
        }

        @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
        public void onTransitionPause(Transition transition) {
            this.mGhostView.setVisibility(4);
        }

        @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
        public void onTransitionResume(Transition transition) {
            this.mGhostView.setVisibility(0);
        }
    }
}
