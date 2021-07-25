package android.app;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.util.ArrayMap;
import android.view.GhostView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.Collection;

/* access modifiers changed from: package-private */
public abstract class ActivityTransitionCoordinator extends ResultReceiver {
    protected static final String KEY_ELEVATION = "shared_element:elevation";
    protected static final String KEY_IMAGE_MATRIX = "shared_element:imageMatrix";
    static final String KEY_REMOTE_RECEIVER = "android:remoteReceiver";
    protected static final String KEY_SCALE_TYPE = "shared_element:scaleType";
    protected static final String KEY_SCREEN_BOTTOM = "shared_element:screenBottom";
    protected static final String KEY_SCREEN_LEFT = "shared_element:screenLeft";
    protected static final String KEY_SCREEN_RIGHT = "shared_element:screenRight";
    protected static final String KEY_SCREEN_TOP = "shared_element:screenTop";
    protected static final String KEY_SNAPSHOT = "shared_element:bitmap";
    protected static final String KEY_TRANSLATION_Z = "shared_element:translationZ";
    public static final int MSG_CANCEL = 106;
    public static final int MSG_EXIT_TRANSITION_COMPLETE = 104;
    public static final int MSG_HIDE_SHARED_ELEMENTS = 101;
    public static final int MSG_SEND_SHARED_ELEMENT_DESTINATION = 108;
    public static final int MSG_SET_REMOTE_RECEIVER = 100;
    public static final int MSG_SHARED_ELEMENT_DESTINATION = 107;
    public static final int MSG_START_EXIT_TRANSITION = 105;
    public static final int MSG_TAKE_SHARED_ELEMENTS = 103;
    protected static final ImageView.ScaleType[] SCALE_TYPE_VALUES = ImageView.ScaleType.values();
    private static final String TAG = "ActivityTransitionCoordinator";
    protected final ArrayList<String> mAllSharedElementNames;
    private final FixedEpicenterCallback mEpicenterCallback = new FixedEpicenterCallback();
    private ArrayList<GhostViewListeners> mGhostViewListeners = new ArrayList<>();
    protected final boolean mIsReturning;
    private boolean mIsStartingTransition;
    protected SharedElementCallback mListener;
    private ArrayMap<View, Float> mOriginalAlphas = new ArrayMap<>();
    private Runnable mPendingTransition;
    protected ResultReceiver mResultReceiver;
    protected final ArrayList<String> mSharedElementNames = new ArrayList<>();
    protected final ArrayList<View> mSharedElements = new ArrayList<>();
    protected ArrayList<View> mTransitioningViews = new ArrayList<>();
    private Window mWindow;

    /* access modifiers changed from: protected */
    public abstract Transition getViewsTransition();

    public ActivityTransitionCoordinator(Window window, ArrayList<String> allSharedElementNames, SharedElementCallback listener, boolean isReturning) {
        super(new Handler());
        this.mWindow = window;
        this.mListener = listener;
        this.mAllSharedElementNames = allSharedElementNames;
        this.mIsReturning = isReturning;
    }

    /* access modifiers changed from: protected */
    public void viewsReady(ArrayMap<String, View> sharedElements) {
        sharedElements.retainAll(this.mAllSharedElementNames);
        if (this.mListener != null) {
            this.mListener.onMapSharedElements(this.mAllSharedElementNames, sharedElements);
        }
        this.mSharedElementNames.addAll(sharedElements.keySet());
        this.mSharedElements.addAll(sharedElements.values());
        if (!(getViewsTransition() == null || this.mTransitioningViews == null)) {
            ViewGroup decorView = getDecor();
            if (decorView != null) {
                decorView.captureTransitioningViews(this.mTransitioningViews);
            }
            this.mTransitioningViews.removeAll(this.mSharedElements);
        }
        setEpicenter();
    }

    /* access modifiers changed from: protected */
    public void stripOffscreenViews() {
        if (this.mTransitioningViews != null) {
            Rect r = new Rect();
            for (int i = this.mTransitioningViews.size() - 1; i >= 0; i--) {
                View view = this.mTransitioningViews.get(i);
                if (!view.getGlobalVisibleRect(r)) {
                    this.mTransitioningViews.remove(i);
                    showView(view, true);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public Window getWindow() {
        return this.mWindow;
    }

    public ViewGroup getDecor() {
        if (this.mWindow == null) {
            return null;
        }
        return (ViewGroup) this.mWindow.getDecorView();
    }

    /* access modifiers changed from: protected */
    public void setEpicenter() {
        int index;
        View epicenter = null;
        if (!this.mAllSharedElementNames.isEmpty() && !this.mSharedElementNames.isEmpty() && (index = this.mSharedElementNames.indexOf(this.mAllSharedElementNames.get(0))) >= 0) {
            epicenter = this.mSharedElements.get(index);
        }
        setEpicenter(epicenter);
    }

    private void setEpicenter(View view) {
        if (view == null) {
            this.mEpicenterCallback.setEpicenter(null);
            return;
        }
        Rect epicenter = new Rect();
        view.getBoundsOnScreen(epicenter);
        this.mEpicenterCallback.setEpicenter(epicenter);
    }

    public ArrayList<String> getAcceptedNames() {
        return this.mSharedElementNames;
    }

    public ArrayList<String> getMappedNames() {
        ArrayList<String> names = new ArrayList<>(this.mSharedElements.size());
        for (int i = 0; i < this.mSharedElements.size(); i++) {
            names.add(this.mSharedElements.get(i).getTransitionName());
        }
        return names;
    }

    public ArrayList<View> copyMappedViews() {
        return new ArrayList<>(this.mSharedElements);
    }

    public ArrayList<String> getAllSharedElementNames() {
        return this.mAllSharedElementNames;
    }

    /* access modifiers changed from: protected */
    public Transition setTargets(Transition transition, boolean add) {
        if (transition == null || (add && (this.mTransitioningViews == null || this.mTransitioningViews.isEmpty()))) {
            return null;
        }
        TransitionSet set = new TransitionSet();
        if (this.mTransitioningViews != null) {
            for (int i = this.mTransitioningViews.size() - 1; i >= 0; i--) {
                View view = this.mTransitioningViews.get(i);
                if (add) {
                    set.addTarget(view);
                } else {
                    set.excludeTarget(view, true);
                }
            }
        }
        set.addTransition(transition);
        if (add || this.mTransitioningViews == null || this.mTransitioningViews.isEmpty()) {
            return set;
        }
        return new TransitionSet().addTransition(set);
    }

    /* access modifiers changed from: protected */
    public Transition configureTransition(Transition transition, boolean includeTransitioningViews) {
        if (transition == null) {
            return transition;
        }
        Transition transition2 = transition.clone();
        transition2.setEpicenterCallback(this.mEpicenterCallback);
        return setTargets(transition2, includeTransitioningViews);
    }

    protected static Transition mergeTransitions(Transition transition1, Transition transition2) {
        if (transition1 == null) {
            return transition2;
        }
        if (transition2 == null) {
            return transition1;
        }
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(transition1);
        transitionSet.addTransition(transition2);
        return transitionSet;
    }

    /* access modifiers changed from: protected */
    public ArrayMap<String, View> mapSharedElements(ArrayList<String> accepted, ArrayList<View> localViews) {
        ArrayMap<String, View> sharedElements = new ArrayMap<>();
        if (accepted != null) {
            for (int i = 0; i < accepted.size(); i++) {
                sharedElements.put(accepted.get(i), localViews.get(i));
            }
        } else {
            ViewGroup decorView = getDecor();
            if (decorView != null) {
                decorView.findNamedViews(sharedElements);
            }
        }
        return sharedElements;
    }

    /* access modifiers changed from: protected */
    public void setResultReceiver(ResultReceiver resultReceiver) {
        this.mResultReceiver = resultReceiver;
    }

    private void setSharedElementState(View view, String name, Bundle transitionArgs, Matrix tempMatrix, RectF tempRect, int[] decorLoc) {
        float left;
        float top;
        float right;
        float bottom;
        int scaleTypeInt;
        Bundle sharedElementBundle = transitionArgs.getBundle(name);
        if (sharedElementBundle != null) {
            if ((view instanceof ImageView) && (scaleTypeInt = sharedElementBundle.getInt(KEY_SCALE_TYPE, -1)) >= 0) {
                ImageView imageView = (ImageView) view;
                ImageView.ScaleType scaleType = SCALE_TYPE_VALUES[scaleTypeInt];
                imageView.setScaleType(scaleType);
                if (scaleType == ImageView.ScaleType.MATRIX) {
                    tempMatrix.setValues(sharedElementBundle.getFloatArray(KEY_IMAGE_MATRIX));
                    imageView.setImageMatrix(tempMatrix);
                }
            }
            view.setTranslationZ(sharedElementBundle.getFloat(KEY_TRANSLATION_Z));
            view.setElevation(sharedElementBundle.getFloat(KEY_ELEVATION));
            float left2 = sharedElementBundle.getFloat(KEY_SCREEN_LEFT);
            float top2 = sharedElementBundle.getFloat(KEY_SCREEN_TOP);
            float right2 = sharedElementBundle.getFloat(KEY_SCREEN_RIGHT);
            float bottom2 = sharedElementBundle.getFloat(KEY_SCREEN_BOTTOM);
            if (decorLoc != null) {
                left = left2 - ((float) decorLoc[0]);
                top = top2 - ((float) decorLoc[1]);
                right = right2 - ((float) decorLoc[0]);
                bottom = bottom2 - ((float) decorLoc[1]);
            } else {
                getSharedElementParentMatrix(view, tempMatrix);
                tempRect.set(left2, top2, right2, bottom2);
                tempMatrix.mapRect(tempRect);
                float leftInParent = tempRect.left;
                float topInParent = tempRect.top;
                view.getInverseMatrix().mapRect(tempRect);
                float width = tempRect.width();
                float height = tempRect.height();
                view.setLeft(0);
                view.setTop(0);
                view.setRight(Math.round(width));
                view.setBottom(Math.round(height));
                tempRect.set(0.0f, 0.0f, width, height);
                view.getMatrix().mapRect(tempRect);
                ViewGroup parent = (ViewGroup) view.getParent();
                left = (leftInParent - tempRect.left) + ((float) parent.getScrollX());
                top = (topInParent - tempRect.top) + ((float) parent.getScrollY());
                right = left + width;
                bottom = top + height;
            }
            int x = Math.round(left);
            int y = Math.round(top);
            int width2 = Math.round(right) - x;
            int height2 = Math.round(bottom) - y;
            view.measure(View.MeasureSpec.makeMeasureSpec(width2, 1073741824), View.MeasureSpec.makeMeasureSpec(height2, 1073741824));
            view.layout(x, y, x + width2, y + height2);
        }
    }

    /* access modifiers changed from: protected */
    public void getSharedElementParentMatrix(View view, Matrix matrix) {
        matrix.reset();
        ((ViewGroup) view.getParent()).transformMatrixToLocal(matrix);
    }

    /* access modifiers changed from: protected */
    public ArrayList<SharedElementOriginalState> setSharedElementState(Bundle sharedElementState, ArrayList<View> snapshots) {
        ArrayList<SharedElementOriginalState> originalImageState = new ArrayList<>();
        if (sharedElementState != null) {
            Matrix tempMatrix = new Matrix();
            RectF tempRect = new RectF();
            int numSharedElements = this.mSharedElements.size();
            for (int i = 0; i < numSharedElements; i++) {
                View sharedElement = this.mSharedElements.get(i);
                String name = this.mSharedElementNames.get(i);
                originalImageState.add(getOldSharedElementState(sharedElement, name, sharedElementState));
                setSharedElementState(sharedElement, name, sharedElementState, tempMatrix, tempRect, null);
            }
        }
        if (this.mListener != null) {
            this.mListener.onSharedElementStart(this.mSharedElementNames, this.mSharedElements, snapshots);
        }
        return originalImageState;
    }

    /* access modifiers changed from: protected */
    public void notifySharedElementEnd(ArrayList<View> snapshots) {
        if (this.mListener != null) {
            this.mListener.onSharedElementEnd(this.mSharedElementNames, this.mSharedElements, snapshots);
        }
    }

    /* access modifiers changed from: protected */
    public void scheduleSetSharedElementEnd(final ArrayList<View> snapshots) {
        final View decorView = getDecor();
        if (decorView != null) {
            decorView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                /* class android.app.ActivityTransitionCoordinator.AnonymousClass1 */

                @Override // android.view.ViewTreeObserver.OnPreDrawListener
                public boolean onPreDraw() {
                    decorView.getViewTreeObserver().removeOnPreDrawListener(this);
                    ActivityTransitionCoordinator.this.notifySharedElementEnd(snapshots);
                    return true;
                }
            });
        }
    }

    private static SharedElementOriginalState getOldSharedElementState(View view, String name, Bundle transitionArgs) {
        Bundle bundle;
        SharedElementOriginalState state = new SharedElementOriginalState();
        state.mLeft = view.getLeft();
        state.mTop = view.getTop();
        state.mRight = view.getRight();
        state.mBottom = view.getBottom();
        state.mMeasuredWidth = view.getMeasuredWidth();
        state.mMeasuredHeight = view.getMeasuredHeight();
        state.mTranslationZ = view.getTranslationZ();
        state.mElevation = view.getElevation();
        if ((view instanceof ImageView) && (bundle = transitionArgs.getBundle(name)) != null && bundle.getInt(KEY_SCALE_TYPE, -1) >= 0) {
            ImageView imageView = (ImageView) view;
            state.mScaleType = imageView.getScaleType();
            if (state.mScaleType == ImageView.ScaleType.MATRIX) {
                state.mMatrix = new Matrix(imageView.getImageMatrix());
            }
        }
        return state;
    }

    /* access modifiers changed from: protected */
    public ArrayList<View> createSnapshots(Bundle state, Collection<String> names) {
        int numSharedElements = names.size();
        if (numSharedElements == 0) {
            return null;
        }
        ArrayList<View> snapshots = new ArrayList<>(numSharedElements);
        Context context = getWindow().getContext();
        int[] decorLoc = new int[2];
        ViewGroup decorView = getDecor();
        if (decorView != null) {
            decorView.getLocationOnScreen(decorLoc);
        }
        for (String name : names) {
            Bundle sharedElementBundle = state.getBundle(name);
            if (sharedElementBundle != null) {
                Parcelable parcelable = sharedElementBundle.getParcelable(KEY_SNAPSHOT);
                View snapshot = null;
                if (!(parcelable == null || this.mListener == null)) {
                    snapshot = this.mListener.onCreateSnapshotView(context, parcelable);
                }
                if (snapshot != null) {
                    setSharedElementState(snapshot, name, state, null, null, decorLoc);
                }
                snapshots.add(snapshot);
            }
        }
        return snapshots;
    }

    protected static void setOriginalSharedElementState(ArrayList<View> sharedElements, ArrayList<SharedElementOriginalState> originalState) {
        for (int i = 0; i < originalState.size(); i++) {
            View view = sharedElements.get(i);
            SharedElementOriginalState state = originalState.get(i);
            if ((view instanceof ImageView) && state.mScaleType != null) {
                ImageView imageView = (ImageView) view;
                imageView.setScaleType(state.mScaleType);
                if (state.mScaleType == ImageView.ScaleType.MATRIX) {
                    imageView.setImageMatrix(state.mMatrix);
                }
            }
            view.setElevation(state.mElevation);
            view.setTranslationZ(state.mTranslationZ);
            view.measure(View.MeasureSpec.makeMeasureSpec(state.mMeasuredWidth, 1073741824), View.MeasureSpec.makeMeasureSpec(state.mMeasuredHeight, 1073741824));
            view.layout(state.mLeft, state.mTop, state.mRight, state.mBottom);
        }
    }

    /* access modifiers changed from: protected */
    public Bundle captureSharedElementState() {
        Bundle bundle = new Bundle();
        RectF tempBounds = new RectF();
        Matrix tempMatrix = new Matrix();
        for (int i = 0; i < this.mSharedElements.size(); i++) {
            captureSharedElementState(this.mSharedElements.get(i), this.mSharedElementNames.get(i), bundle, tempMatrix, tempBounds);
        }
        return bundle;
    }

    /* access modifiers changed from: protected */
    public void clearState() {
        this.mWindow = null;
        this.mSharedElements.clear();
        this.mTransitioningViews = null;
        this.mOriginalAlphas.clear();
        this.mResultReceiver = null;
        this.mPendingTransition = null;
        this.mListener = null;
    }

    /* access modifiers changed from: protected */
    public long getFadeDuration() {
        return getWindow().getTransitionBackgroundFadeDuration();
    }

    /* access modifiers changed from: protected */
    public void hideViews(ArrayList<View> views) {
        int count = views.size();
        for (int i = 0; i < count; i++) {
            View view = views.get(i);
            if (!this.mOriginalAlphas.containsKey(view)) {
                this.mOriginalAlphas.put(view, Float.valueOf(view.getAlpha()));
            }
            view.setAlpha(0.0f);
        }
    }

    /* access modifiers changed from: protected */
    public void showViews(ArrayList<View> views, boolean setTransitionAlpha) {
        int count = views.size();
        for (int i = 0; i < count; i++) {
            showView(views.get(i), setTransitionAlpha);
        }
    }

    private void showView(View view, boolean setTransitionAlpha) {
        Float alpha = this.mOriginalAlphas.remove(view);
        if (alpha != null) {
            view.setAlpha(alpha.floatValue());
        }
        if (setTransitionAlpha) {
            view.setTransitionAlpha(1.0f);
        }
    }

    /* access modifiers changed from: protected */
    public void captureSharedElementState(View view, String name, Bundle transitionArgs, Matrix tempMatrix, RectF tempBounds) {
        Bundle sharedElementBundle = new Bundle();
        tempMatrix.reset();
        view.transformMatrixToGlobal(tempMatrix);
        tempBounds.set(0.0f, 0.0f, (float) view.getWidth(), (float) view.getHeight());
        tempMatrix.mapRect(tempBounds);
        sharedElementBundle.putFloat(KEY_SCREEN_LEFT, tempBounds.left);
        sharedElementBundle.putFloat(KEY_SCREEN_RIGHT, tempBounds.right);
        sharedElementBundle.putFloat(KEY_SCREEN_TOP, tempBounds.top);
        sharedElementBundle.putFloat(KEY_SCREEN_BOTTOM, tempBounds.bottom);
        sharedElementBundle.putFloat(KEY_TRANSLATION_Z, view.getTranslationZ());
        sharedElementBundle.putFloat(KEY_ELEVATION, view.getElevation());
        Parcelable bitmap = null;
        if (this.mListener != null) {
            bitmap = this.mListener.onCaptureSharedElementSnapshot(view, tempMatrix, tempBounds);
        }
        if (bitmap != null) {
            sharedElementBundle.putParcelable(KEY_SNAPSHOT, bitmap);
        }
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            sharedElementBundle.putInt(KEY_SCALE_TYPE, scaleTypeToInt(imageView.getScaleType()));
            if (imageView.getScaleType() == ImageView.ScaleType.MATRIX) {
                float[] matrix = new float[9];
                imageView.getImageMatrix().getValues(matrix);
                sharedElementBundle.putFloatArray(KEY_IMAGE_MATRIX, matrix);
            }
        }
        transitionArgs.putBundle(name, sharedElementBundle);
    }

    /* access modifiers changed from: protected */
    public void startTransition(Runnable runnable) {
        if (this.mIsStartingTransition) {
            this.mPendingTransition = runnable;
            return;
        }
        this.mIsStartingTransition = true;
        runnable.run();
    }

    /* access modifiers changed from: protected */
    public void transitionStarted() {
        this.mIsStartingTransition = false;
    }

    /* access modifiers changed from: protected */
    public void moveSharedElementsToOverlay() {
        if (this.mWindow.getSharedElementsUseOverlay()) {
            int numSharedElements = this.mSharedElements.size();
            ViewGroup decor = getDecor();
            if (decor != null) {
                boolean moveWithParent = moveSharedElementWithParent();
                for (int i = 0; i < numSharedElements; i++) {
                    View view = this.mSharedElements.get(i);
                    GhostView.addGhost(view, decor);
                    ViewGroup parent = (ViewGroup) view.getParent();
                    if (moveWithParent && !isInTransitionGroup(parent, decor)) {
                        GhostViewListeners listener = new GhostViewListeners(view, parent, decor);
                        parent.getViewTreeObserver().addOnPreDrawListener(listener);
                        this.mGhostViewListeners.add(listener);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean moveSharedElementWithParent() {
        return true;
    }

    public static boolean isInTransitionGroup(ViewParent viewParent, ViewGroup decor) {
        if (viewParent == decor || !(viewParent instanceof ViewGroup)) {
            return false;
        }
        ViewGroup parent = (ViewGroup) viewParent;
        if (parent.isTransitionGroup()) {
            return true;
        }
        return isInTransitionGroup(parent.getParent(), decor);
    }

    /* access modifiers changed from: protected */
    public void moveSharedElementsFromOverlay() {
        ViewGroup decor;
        int numListeners = this.mGhostViewListeners.size();
        for (int i = 0; i < numListeners; i++) {
            GhostViewListeners listener = this.mGhostViewListeners.get(i);
            ((ViewGroup) listener.getView().getParent()).getViewTreeObserver().removeOnPreDrawListener(listener);
        }
        this.mGhostViewListeners.clear();
        if (!(this.mWindow == null || !this.mWindow.getSharedElementsUseOverlay() || (decor = getDecor()) == null)) {
            decor.getOverlay();
            int count = this.mSharedElements.size();
            for (int i2 = 0; i2 < count; i2++) {
                GhostView.removeGhost(this.mSharedElements.get(i2));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setGhostVisibility(int visibility) {
        int numSharedElements = this.mSharedElements.size();
        for (int i = 0; i < numSharedElements; i++) {
            GhostView ghostView = GhostView.getGhost(this.mSharedElements.get(i));
            if (ghostView != null) {
                ghostView.setVisibility(visibility);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void scheduleGhostVisibilityChange(final int visibility) {
        final View decorView = getDecor();
        if (decorView != null) {
            decorView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                /* class android.app.ActivityTransitionCoordinator.AnonymousClass2 */

                @Override // android.view.ViewTreeObserver.OnPreDrawListener
                public boolean onPreDraw() {
                    decorView.getViewTreeObserver().removeOnPreDrawListener(this);
                    ActivityTransitionCoordinator.this.setGhostVisibility(visibility);
                    return true;
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public class ContinueTransitionListener extends Transition.TransitionListenerAdapter {
        protected ContinueTransitionListener() {
        }

        @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
        public void onTransitionStart(Transition transition) {
            ActivityTransitionCoordinator.this.mIsStartingTransition = false;
            Runnable pending = ActivityTransitionCoordinator.this.mPendingTransition;
            ActivityTransitionCoordinator.this.mPendingTransition = null;
            if (pending != null) {
                ActivityTransitionCoordinator.this.startTransition(pending);
            }
        }
    }

    private static int scaleTypeToInt(ImageView.ScaleType scaleType) {
        for (int i = 0; i < SCALE_TYPE_VALUES.length; i++) {
            if (scaleType == SCALE_TYPE_VALUES[i]) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public static class FixedEpicenterCallback extends Transition.EpicenterCallback {
        private Rect mEpicenter;

        private FixedEpicenterCallback() {
        }

        public void setEpicenter(Rect epicenter) {
            this.mEpicenter = epicenter;
        }

        @Override // android.transition.Transition.EpicenterCallback
        public Rect onGetEpicenter(Transition transition) {
            return this.mEpicenter;
        }
    }

    private static class GhostViewListeners implements ViewTreeObserver.OnPreDrawListener {
        private ViewGroup mDecor;
        private Matrix mMatrix = new Matrix();
        private View mParent;
        private View mView;

        public GhostViewListeners(View view, View parent, ViewGroup decor) {
            this.mView = view;
            this.mParent = parent;
            this.mDecor = decor;
        }

        public View getView() {
            return this.mView;
        }

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            GhostView ghostView = GhostView.getGhost(this.mView);
            if (ghostView == null) {
                this.mParent.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
            GhostView.calculateMatrix(this.mView, this.mDecor, this.mMatrix);
            ghostView.setMatrix(this.mMatrix);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public static class SharedElementOriginalState {
        int mBottom;
        float mElevation;
        int mLeft;
        Matrix mMatrix;
        int mMeasuredHeight;
        int mMeasuredWidth;
        int mRight;
        ImageView.ScaleType mScaleType;
        int mTop;
        float mTranslationZ;

        SharedElementOriginalState() {
        }
    }
}
