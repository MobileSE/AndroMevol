package android.transition;

import android.transition.Transition;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public class TransitionManager {
    private static final String[] EMPTY_STRINGS = new String[0];
    private static String LOG_TAG = "TransitionManager";
    private static Transition sDefaultTransition = new AutoTransition();
    private static ArrayList<ViewGroup> sPendingTransitions = new ArrayList<>();
    private static ThreadLocal<WeakReference<ArrayMap<ViewGroup, ArrayList<Transition>>>> sRunningTransitions = new ThreadLocal<>();
    ArrayMap<Scene, ArrayMap<Scene, Transition>> mScenePairTransitions = new ArrayMap<>();
    ArrayMap<Scene, Transition> mSceneTransitions = new ArrayMap<>();

    public void setDefaultTransition(Transition transition) {
        sDefaultTransition = transition;
    }

    public static Transition getDefaultTransition() {
        return sDefaultTransition;
    }

    public void setTransition(Scene scene, Transition transition) {
        this.mSceneTransitions.put(scene, transition);
    }

    public void setTransition(Scene fromScene, Scene toScene, Transition transition) {
        ArrayMap<Scene, Transition> sceneTransitionMap = this.mScenePairTransitions.get(toScene);
        if (sceneTransitionMap == null) {
            sceneTransitionMap = new ArrayMap<>();
            this.mScenePairTransitions.put(toScene, sceneTransitionMap);
        }
        sceneTransitionMap.put(fromScene, transition);
    }

    private Transition getTransition(Scene scene) {
        Scene currScene;
        ArrayMap<Scene, Transition> sceneTransitionMap;
        Transition transition;
        ViewGroup sceneRoot = scene.getSceneRoot();
        if (sceneRoot != null && (currScene = Scene.getCurrentScene(sceneRoot)) != null && (sceneTransitionMap = this.mScenePairTransitions.get(scene)) != null && (transition = sceneTransitionMap.get(currScene)) != null) {
            return transition;
        }
        Transition transition2 = this.mSceneTransitions.get(scene);
        return transition2 != null ? transition2 : sDefaultTransition;
    }

    private static void changeScene(Scene scene, Transition transition) {
        ViewGroup sceneRoot = scene.getSceneRoot();
        Transition transitionClone = null;
        if (transition != null) {
            transitionClone = transition.clone();
            transitionClone.setSceneRoot(sceneRoot);
        }
        Scene oldScene = Scene.getCurrentScene(sceneRoot);
        if (!(oldScene == null || transitionClone == null || !oldScene.isCreatedFromLayoutResource())) {
            transitionClone.setCanRemoveViews(true);
        }
        sceneChangeSetup(sceneRoot, transitionClone);
        scene.enter();
        sceneChangeRunTransition(sceneRoot, transitionClone);
    }

    /* access modifiers changed from: private */
    public static ArrayMap<ViewGroup, ArrayList<Transition>> getRunningTransitions() {
        WeakReference<ArrayMap<ViewGroup, ArrayList<Transition>>> runningTransitions = sRunningTransitions.get();
        if (runningTransitions == null || runningTransitions.get() == null) {
            runningTransitions = new WeakReference<>(new ArrayMap<>());
            sRunningTransitions.set(runningTransitions);
        }
        return runningTransitions.get();
    }

    private static void sceneChangeRunTransition(ViewGroup sceneRoot, Transition transition) {
        if (transition != null && sceneRoot != null) {
            MultiListener listener = new MultiListener(transition, sceneRoot);
            sceneRoot.addOnAttachStateChangeListener(listener);
            sceneRoot.getViewTreeObserver().addOnPreDrawListener(listener);
        }
    }

    /* access modifiers changed from: private */
    public static class MultiListener implements ViewTreeObserver.OnPreDrawListener, View.OnAttachStateChangeListener {
        ViewGroup mSceneRoot;
        Transition mTransition;

        MultiListener(Transition transition, ViewGroup sceneRoot) {
            this.mTransition = transition;
            this.mSceneRoot = sceneRoot;
        }

        private void removeListeners() {
            this.mSceneRoot.getViewTreeObserver().removeOnPreDrawListener(this);
            this.mSceneRoot.removeOnAttachStateChangeListener(this);
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewAttachedToWindow(View v) {
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewDetachedFromWindow(View v) {
            removeListeners();
            TransitionManager.sPendingTransitions.remove(this.mSceneRoot);
            ArrayList<Transition> runningTransitions = (ArrayList) TransitionManager.getRunningTransitions().get(this.mSceneRoot);
            if (runningTransitions != null && runningTransitions.size() > 0) {
                Iterator i$ = runningTransitions.iterator();
                while (i$.hasNext()) {
                    i$.next().resume(this.mSceneRoot);
                }
            }
            this.mTransition.clearValues(true);
        }

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            removeListeners();
            TransitionManager.sPendingTransitions.remove(this.mSceneRoot);
            final ArrayMap<ViewGroup, ArrayList<Transition>> runningTransitions = TransitionManager.getRunningTransitions();
            ArrayList<Transition> currentTransitions = runningTransitions.get(this.mSceneRoot);
            ArrayList<Transition> previousRunningTransitions = null;
            if (currentTransitions == null) {
                currentTransitions = new ArrayList<>();
                runningTransitions.put(this.mSceneRoot, currentTransitions);
            } else if (currentTransitions.size() > 0) {
                previousRunningTransitions = new ArrayList<>(currentTransitions);
            }
            currentTransitions.add(this.mTransition);
            this.mTransition.addListener(new Transition.TransitionListenerAdapter() {
                /* class android.transition.TransitionManager.MultiListener.AnonymousClass1 */

                @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
                public void onTransitionEnd(Transition transition) {
                    ((ArrayList) runningTransitions.get(MultiListener.this.mSceneRoot)).remove(transition);
                }
            });
            this.mTransition.captureValues(this.mSceneRoot, false);
            if (previousRunningTransitions != null) {
                Iterator i$ = previousRunningTransitions.iterator();
                while (i$.hasNext()) {
                    i$.next().resume(this.mSceneRoot);
                }
            }
            this.mTransition.playTransition(this.mSceneRoot);
            return true;
        }
    }

    private static void sceneChangeSetup(ViewGroup sceneRoot, Transition transition) {
        ArrayList<Transition> runningTransitions = getRunningTransitions().get(sceneRoot);
        if (runningTransitions != null && runningTransitions.size() > 0) {
            Iterator i$ = runningTransitions.iterator();
            while (i$.hasNext()) {
                i$.next().pause(sceneRoot);
            }
        }
        if (transition != null) {
            transition.captureValues(sceneRoot, true);
        }
        Scene previousScene = Scene.getCurrentScene(sceneRoot);
        if (previousScene != null) {
            previousScene.exit();
        }
    }

    public void transitionTo(Scene scene) {
        changeScene(scene, getTransition(scene));
    }

    public static void go(Scene scene) {
        changeScene(scene, sDefaultTransition);
    }

    public static void go(Scene scene, Transition transition) {
        changeScene(scene, transition);
    }

    public static void beginDelayedTransition(ViewGroup sceneRoot) {
        beginDelayedTransition(sceneRoot, null);
    }

    public static void beginDelayedTransition(ViewGroup sceneRoot, Transition transition) {
        if (!sPendingTransitions.contains(sceneRoot) && sceneRoot.isLaidOut()) {
            sPendingTransitions.add(sceneRoot);
            if (transition == null) {
                transition = sDefaultTransition;
            }
            Transition transitionClone = transition.clone();
            sceneChangeSetup(sceneRoot, transitionClone);
            Scene.setCurrentScene(sceneRoot, null);
            sceneChangeRunTransition(sceneRoot, transitionClone);
        }
    }
}
