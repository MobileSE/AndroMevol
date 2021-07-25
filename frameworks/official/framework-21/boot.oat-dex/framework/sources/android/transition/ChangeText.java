package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.net.ProxyInfo;
import android.text.Spanned;
import android.transition.Transition;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import java.util.Map;

public class ChangeText extends Transition {
    public static final int CHANGE_BEHAVIOR_IN = 2;
    public static final int CHANGE_BEHAVIOR_KEEP = 0;
    public static final int CHANGE_BEHAVIOR_OUT = 1;
    public static final int CHANGE_BEHAVIOR_OUT_IN = 3;
    private static final String LOG_TAG = "TextChange";
    private static final String PROPNAME_TEXT = "android:textchange:text";
    private static final String PROPNAME_TEXT_COLOR = "android:textchange:textColor";
    private static final String PROPNAME_TEXT_SELECTION_END = "android:textchange:textSelectionEnd";
    private static final String PROPNAME_TEXT_SELECTION_START = "android:textchange:textSelectionStart";
    private static final String[] sTransitionProperties = {PROPNAME_TEXT, PROPNAME_TEXT_SELECTION_START, PROPNAME_TEXT_SELECTION_END};
    private int mChangeBehavior = 0;

    public ChangeText setChangeBehavior(int changeBehavior) {
        if (changeBehavior >= 0 && changeBehavior <= 3) {
            this.mChangeBehavior = changeBehavior;
        }
        return this;
    }

    @Override // android.transition.Transition
    public String[] getTransitionProperties() {
        return sTransitionProperties;
    }

    public int getChangeBehavior() {
        return this.mChangeBehavior;
    }

    private void captureValues(TransitionValues transitionValues) {
        if (transitionValues.view instanceof TextView) {
            TextView textview = (TextView) transitionValues.view;
            transitionValues.values.put(PROPNAME_TEXT, textview.getText());
            if (textview instanceof EditText) {
                transitionValues.values.put(PROPNAME_TEXT_SELECTION_START, Integer.valueOf(textview.getSelectionStart()));
                transitionValues.values.put(PROPNAME_TEXT_SELECTION_END, Integer.valueOf(textview.getSelectionEnd()));
            }
            if (this.mChangeBehavior > 0) {
                transitionValues.values.put(PROPNAME_TEXT_COLOR, Integer.valueOf(textview.getCurrentTextColor()));
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

    /* JADX WARN: Multi-variable type inference failed */
    @Override // android.transition.Transition
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        final int endSelectionEnd;
        final int endSelectionStart;
        final int startSelectionEnd;
        final int startSelectionStart;
        final int endColor;
        Animator anim;
        if (startValues == null || endValues == null || !(startValues.view instanceof TextView) || !(endValues.view instanceof TextView)) {
            return null;
        }
        final TextView view = (TextView) endValues.view;
        Map<String, Object> startVals = startValues.values;
        Map<String, Object> endVals = endValues.values;
        final CharSequence startText = startVals.get(PROPNAME_TEXT) != null ? (CharSequence) startVals.get(PROPNAME_TEXT) : ProxyInfo.LOCAL_EXCL_LIST;
        final CharSequence endText = endVals.get(PROPNAME_TEXT) != null ? (CharSequence) endVals.get(PROPNAME_TEXT) : ProxyInfo.LOCAL_EXCL_LIST;
        if (view instanceof EditText) {
            startSelectionStart = startVals.get(PROPNAME_TEXT_SELECTION_START) != null ? ((Integer) startVals.get(PROPNAME_TEXT_SELECTION_START)).intValue() : -1;
            if (startVals.get(PROPNAME_TEXT_SELECTION_END) != null) {
                startSelectionEnd = ((Integer) startVals.get(PROPNAME_TEXT_SELECTION_END)).intValue();
            } else {
                startSelectionEnd = startSelectionStart;
            }
            endSelectionStart = endVals.get(PROPNAME_TEXT_SELECTION_START) != null ? ((Integer) endVals.get(PROPNAME_TEXT_SELECTION_START)).intValue() : -1;
            if (endVals.get(PROPNAME_TEXT_SELECTION_END) != null) {
                endSelectionEnd = ((Integer) endVals.get(PROPNAME_TEXT_SELECTION_END)).intValue();
            } else {
                endSelectionEnd = endSelectionStart;
            }
        } else {
            endSelectionEnd = -1;
            endSelectionStart = -1;
            startSelectionEnd = -1;
            startSelectionStart = -1;
        }
        if (startText.equals(endText)) {
            return null;
        }
        if (this.mChangeBehavior != 2) {
            view.setText(startText);
            if (view instanceof EditText) {
                setSelection((EditText) view, startSelectionStart, startSelectionEnd);
            }
        }
        if (this.mChangeBehavior == 0) {
            endColor = 0;
            anim = ValueAnimator.ofFloat(0.0f, 1.0f);
            anim.addListener(new AnimatorListenerAdapter() {
                /* class android.transition.ChangeText.AnonymousClass1 */

                public void onAnimationEnd(Animator animation) {
                    if (startText.equals(view.getText())) {
                        view.setText(endText);
                        if (view instanceof EditText) {
                            ChangeText.this.setSelection((EditText) view, endSelectionStart, endSelectionEnd);
                        }
                    }
                }
            });
        } else {
            final int startColor = ((Integer) startVals.get(PROPNAME_TEXT_COLOR)).intValue();
            endColor = ((Integer) endVals.get(PROPNAME_TEXT_COLOR)).intValue();
            ValueAnimator outAnim = null;
            ValueAnimator inAnim = null;
            if (this.mChangeBehavior == 3 || this.mChangeBehavior == 1) {
                outAnim = ValueAnimator.ofInt(255, 0);
                outAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    /* class android.transition.ChangeText.AnonymousClass2 */

                    public void onAnimationUpdate(ValueAnimator animation) {
                        view.setTextColor((((Integer) animation.getAnimatedValue()).intValue() << 24) | (startColor & Spanned.SPAN_PRIORITY) | (startColor & 65280) | (startColor & 255));
                    }
                });
                outAnim.addListener(new AnimatorListenerAdapter() {
                    /* class android.transition.ChangeText.AnonymousClass3 */

                    public void onAnimationEnd(Animator animation) {
                        if (startText.equals(view.getText())) {
                            view.setText(endText);
                            if (view instanceof EditText) {
                                ChangeText.this.setSelection((EditText) view, endSelectionStart, endSelectionEnd);
                            }
                        }
                        view.setTextColor(endColor);
                    }
                });
            }
            if (this.mChangeBehavior == 3 || this.mChangeBehavior == 2) {
                inAnim = ValueAnimator.ofInt(0, 255);
                inAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    /* class android.transition.ChangeText.AnonymousClass4 */

                    public void onAnimationUpdate(ValueAnimator animation) {
                        view.setTextColor((((Integer) animation.getAnimatedValue()).intValue() << 24) | (Color.red(endColor) << 16) | (Color.green(endColor) << 8) | Color.red(endColor));
                    }
                });
                inAnim.addListener(new AnimatorListenerAdapter() {
                    /* class android.transition.ChangeText.AnonymousClass5 */

                    public void onAnimationCancel(Animator animation) {
                        view.setTextColor(endColor);
                    }
                });
            }
            if (outAnim != null && inAnim != null) {
                anim = new AnimatorSet();
                ((AnimatorSet) anim).playSequentially(outAnim, inAnim);
            } else if (outAnim != null) {
                anim = outAnim;
            } else {
                anim = inAnim;
            }
        }
        addListener(new Transition.TransitionListenerAdapter() {
            /* class android.transition.ChangeText.AnonymousClass6 */
            int mPausedColor = 0;

            @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
            public void onTransitionPause(Transition transition) {
                if (ChangeText.this.mChangeBehavior != 2) {
                    view.setText(endText);
                    if (view instanceof EditText) {
                        ChangeText.this.setSelection((EditText) view, endSelectionStart, endSelectionEnd);
                    }
                }
                if (ChangeText.this.mChangeBehavior > 0) {
                    this.mPausedColor = view.getCurrentTextColor();
                    view.setTextColor(endColor);
                }
            }

            @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
            public void onTransitionResume(Transition transition) {
                if (ChangeText.this.mChangeBehavior != 2) {
                    view.setText(startText);
                    if (view instanceof EditText) {
                        ChangeText.this.setSelection((EditText) view, startSelectionStart, startSelectionEnd);
                    }
                }
                if (ChangeText.this.mChangeBehavior > 0) {
                    view.setTextColor(this.mPausedColor);
                }
            }
        });
        return anim;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSelection(EditText editText, int start, int end) {
        if (start >= 0 && end >= 0) {
            editText.setSelection(start, end);
        }
    }
}
