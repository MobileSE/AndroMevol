package com.android.internal.app;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.internal.R;

public class PlatLogoActivity extends Activity {
    static final int[] FLAVORS = {-6543440, -4560696, -26624, -18611, -1023342, -476208, -5262293, -3285959, -5317, -3722, -8825528, -6190977};
    PathInterpolator mInterpolator = new PathInterpolator(0.0f, 0.0f, 0.5f, 1.0f);
    int mKeyCount;
    FrameLayout mLayout;
    int mTapCount;

    static int newColorIndex() {
        return ((int) ((Math.random() * ((double) FLAVORS.length)) / 2.0d)) * 2;
    }

    /* access modifiers changed from: package-private */
    public Drawable makeRipple() {
        int idx = newColorIndex();
        ShapeDrawable popbg = new ShapeDrawable(new OvalShape());
        popbg.getPaint().setColor(FLAVORS[idx]);
        return new RippleDrawable(ColorStateList.valueOf(FLAVORS[idx + 1]), popbg, (Drawable) null);
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mLayout = new FrameLayout(this);
        setContentView(this.mLayout);
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onAttachedToWindow() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float dp = dm.density;
        final int size = (int) (Math.min((float) Math.min(dm.widthPixels, dm.heightPixels), 600.0f * dp) - (100.0f * dp));
        final View stick = new View(this) {
            /* class com.android.internal.app.PlatLogoActivity.AnonymousClass1 */
            Paint mPaint = new Paint();
            Path mShadow = new Path();

            @Override // android.view.View
            public void onAttachedToWindow() {
                super.onAttachedToWindow();
                setWillNotDraw(false);
                setOutlineProvider(new ViewOutlineProvider() {
                    /* class com.android.internal.app.PlatLogoActivity.AnonymousClass1.AnonymousClass1 */

                    @Override // android.view.ViewOutlineProvider
                    public void getOutline(View view, Outline outline) {
                        outline.setRect(0, AnonymousClass1.this.getHeight() / 2, AnonymousClass1.this.getWidth(), AnonymousClass1.this.getHeight());
                    }
                });
            }

            @Override // android.view.View
            public void onDraw(Canvas c) {
                int w = c.getWidth();
                int h = c.getHeight() / 2;
                c.translate(0.0f, (float) h);
                GradientDrawable g = new GradientDrawable();
                g.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                g.setGradientCenter(((float) w) * 0.75f, 0.0f);
                g.setColors(new int[]{-1, -5592406});
                g.setBounds(0, 0, w, h);
                g.draw(c);
                this.mPaint.setColor(-5592406);
                this.mShadow.reset();
                this.mShadow.moveTo(0.0f, 0.0f);
                this.mShadow.lineTo((float) w, 0.0f);
                this.mShadow.lineTo((float) w, ((float) (size / 2)) + (1.5f * ((float) w)));
                this.mShadow.lineTo(0.0f, (float) (size / 2));
                this.mShadow.close();
                c.drawPath(this.mShadow, this.mPaint);
            }
        };
        this.mLayout.addView(stick, new FrameLayout.LayoutParams((int) (32.0f * dp), -1, 1));
        stick.setAlpha(0.0f);
        final ImageView im = new ImageView(this);
        im.setTranslationZ(20.0f);
        im.setScaleX(0.0f);
        im.setScaleY(0.0f);
        final Drawable platlogo = getDrawable(R.drawable.platlogo);
        platlogo.setAlpha(0);
        im.setImageDrawable(platlogo);
        im.setBackground(makeRipple());
        im.setClickable(true);
        ShapeDrawable highlight = new ShapeDrawable(new OvalShape());
        highlight.getPaint().setColor(285212671);
        highlight.setBounds((int) (((float) size) * 0.15f), (int) (((float) size) * 0.15f), (int) (((float) size) * 0.6f), (int) (((float) size) * 0.6f));
        im.getOverlay().add(highlight);
        im.setOnClickListener(new View.OnClickListener() {
            /* class com.android.internal.app.PlatLogoActivity.AnonymousClass2 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (PlatLogoActivity.this.mTapCount == 0) {
                    im.animate().translationZ(40.0f).scaleX(1.0f).scaleY(1.0f).setInterpolator(PlatLogoActivity.this.mInterpolator).setDuration(700).setStartDelay(500).start();
                    ObjectAnimator a = ObjectAnimator.ofInt(platlogo, "alpha", 0, 255);
                    a.setInterpolator(PlatLogoActivity.this.mInterpolator);
                    a.setStartDelay(1000);
                    a.start();
                    stick.animate().translationZ(20.0f).alpha(1.0f).setInterpolator(PlatLogoActivity.this.mInterpolator).setDuration(700).setStartDelay(750).start();
                    im.setOnLongClickListener(new View.OnLongClickListener() {
                        /* class com.android.internal.app.PlatLogoActivity.AnonymousClass2.AnonymousClass1 */

                        @Override // android.view.View.OnLongClickListener
                        public boolean onLongClick(View v) {
                            if (PlatLogoActivity.this.mTapCount < 5) {
                                return false;
                            }
                            ContentResolver cr = PlatLogoActivity.this.getContentResolver();
                            if (Settings.System.getLong(cr, Settings.System.EGG_MODE, 0) == 0) {
                                Settings.System.putLong(cr, Settings.System.EGG_MODE, System.currentTimeMillis());
                            }
                            im.post(new Runnable() {
                                /* class com.android.internal.app.PlatLogoActivity.AnonymousClass2.AnonymousClass1.AnonymousClass1 */

                                public void run() {
                                    try {
                                        PlatLogoActivity.this.startActivity(new Intent(Intent.ACTION_MAIN).setFlags(276856832).addCategory("com.android.internal.category.PLATLOGO"));
                                    } catch (ActivityNotFoundException e) {
                                        Log.e("PlatLogoActivity", "No more eggs.");
                                    }
                                    PlatLogoActivity.this.finish();
                                }
                            });
                            return true;
                        }
                    });
                } else {
                    im.setBackground(PlatLogoActivity.this.makeRipple());
                }
                PlatLogoActivity.this.mTapCount++;
            }
        });
        im.setFocusable(true);
        im.requestFocus();
        im.setOnKeyListener(new View.OnKeyListener() {
            /* class com.android.internal.app.PlatLogoActivity.AnonymousClass3 */

            @Override // android.view.View.OnKeyListener
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == 4 || event.getAction() != 0) {
                    return false;
                }
                PlatLogoActivity.this.mKeyCount++;
                if (PlatLogoActivity.this.mKeyCount > 2) {
                    if (PlatLogoActivity.this.mTapCount > 5) {
                        im.performLongClick();
                    } else {
                        im.performClick();
                    }
                }
                return true;
            }
        });
        this.mLayout.addView(im, new FrameLayout.LayoutParams(size, size, 17));
        im.animate().scaleX(0.3f).scaleY(0.3f).setInterpolator(this.mInterpolator).setDuration(500).setStartDelay(800).start();
    }
}
