package android.view.animation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AnimationUtils {
    private static final int SEQUENTIALLY = 1;
    private static final int TOGETHER = 0;

    public static long currentAnimationTimeMillis() {
        return SystemClock.uptimeMillis();
    }

    public static Animation loadAnimation(Context context, int id) throws Resources.NotFoundException {
        XmlResourceParser parser = null;
        try {
            parser = context.getResources().getAnimation(id);
            Animation createAnimationFromXml = createAnimationFromXml(context, parser);
            if (parser != null) {
                parser.close();
            }
            return createAnimationFromXml;
        } catch (XmlPullParserException ex) {
            Resources.NotFoundException rnf = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } catch (IOException ex2) {
            Resources.NotFoundException rnf2 = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));
            rnf2.initCause(ex2);
            throw rnf2;
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
            throw th;
        }
    }

    private static Animation createAnimationFromXml(Context c, XmlPullParser parser) throws XmlPullParserException, IOException {
        return createAnimationFromXml(c, parser, null, Xml.asAttributeSet(parser));
    }

    private static Animation createAnimationFromXml(Context c, XmlPullParser parser, AnimationSet parent, AttributeSet attrs) throws XmlPullParserException, IOException {
        Animation anim = null;
        int depth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if ((type != 3 || parser.getDepth() > depth) && type != 1) {
                if (type == 2) {
                    String name = parser.getName();
                    if (name.equals("set")) {
                        anim = new AnimationSet(c, attrs);
                        createAnimationFromXml(c, parser, (AnimationSet) anim, attrs);
                    } else if (name.equals("alpha")) {
                        anim = new AlphaAnimation(c, attrs);
                    } else if (name.equals(BatteryManager.EXTRA_SCALE)) {
                        anim = new ScaleAnimation(c, attrs);
                    } else if (name.equals("rotate")) {
                        anim = new RotateAnimation(c, attrs);
                    } else if (name.equals("translate")) {
                        anim = new TranslateAnimation(c, attrs);
                    } else {
                        throw new RuntimeException("Unknown animation name: " + parser.getName());
                    }
                    if (parent != null) {
                        parent.addAnimation(anim);
                    }
                }
            }
        }
        return anim;
    }

    public static LayoutAnimationController loadLayoutAnimation(Context context, int id) throws Resources.NotFoundException {
        XmlResourceParser parser = null;
        try {
            parser = context.getResources().getAnimation(id);
            LayoutAnimationController createLayoutAnimationFromXml = createLayoutAnimationFromXml(context, parser);
            if (parser != null) {
                parser.close();
            }
            return createLayoutAnimationFromXml;
        } catch (XmlPullParserException ex) {
            Resources.NotFoundException rnf = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } catch (IOException ex2) {
            Resources.NotFoundException rnf2 = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));
            rnf2.initCause(ex2);
            throw rnf2;
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
            throw th;
        }
    }

    private static LayoutAnimationController createLayoutAnimationFromXml(Context c, XmlPullParser parser) throws XmlPullParserException, IOException {
        return createLayoutAnimationFromXml(c, parser, Xml.asAttributeSet(parser));
    }

    private static LayoutAnimationController createLayoutAnimationFromXml(Context c, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
        LayoutAnimationController controller = null;
        int depth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if ((type != 3 || parser.getDepth() > depth) && type != 1) {
                if (type == 2) {
                    String name = parser.getName();
                    if ("layoutAnimation".equals(name)) {
                        controller = new LayoutAnimationController(c, attrs);
                    } else if ("gridLayoutAnimation".equals(name)) {
                        controller = new GridLayoutAnimationController(c, attrs);
                    } else {
                        throw new RuntimeException("Unknown layout animation name: " + name);
                    }
                }
            }
        }
        return controller;
    }

    public static Animation makeInAnimation(Context c, boolean fromLeft) {
        Animation a;
        if (fromLeft) {
            a = loadAnimation(c, 17432578);
        } else {
            a = loadAnimation(c, R.anim.slide_in_right);
        }
        a.setInterpolator(new DecelerateInterpolator());
        a.setStartTime(currentAnimationTimeMillis());
        return a;
    }

    public static Animation makeOutAnimation(Context c, boolean toRight) {
        Animation a;
        if (toRight) {
            a = loadAnimation(c, 17432579);
        } else {
            a = loadAnimation(c, R.anim.slide_out_left);
        }
        a.setInterpolator(new AccelerateInterpolator());
        a.setStartTime(currentAnimationTimeMillis());
        return a;
    }

    public static Animation makeInChildBottomAnimation(Context c) {
        Animation a = loadAnimation(c, R.anim.slide_in_child_bottom);
        a.setInterpolator(new AccelerateInterpolator());
        a.setStartTime(currentAnimationTimeMillis());
        return a;
    }

    public static Interpolator loadInterpolator(Context context, int id) throws Resources.NotFoundException {
        XmlResourceParser parser = null;
        try {
            parser = context.getResources().getAnimation(id);
            Interpolator createInterpolatorFromXml = createInterpolatorFromXml(context.getResources(), context.getTheme(), parser);
            if (parser != null) {
                parser.close();
            }
            return createInterpolatorFromXml;
        } catch (XmlPullParserException ex) {
            Resources.NotFoundException rnf = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } catch (IOException ex2) {
            Resources.NotFoundException rnf2 = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));
            rnf2.initCause(ex2);
            throw rnf2;
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
            throw th;
        }
    }

    public static Interpolator loadInterpolator(Resources res, Resources.Theme theme, int id) throws Resources.NotFoundException {
        XmlResourceParser parser = null;
        try {
            parser = res.getAnimation(id);
            Interpolator createInterpolatorFromXml = createInterpolatorFromXml(res, theme, parser);
            if (parser != null) {
                parser.close();
            }
            return createInterpolatorFromXml;
        } catch (XmlPullParserException ex) {
            Resources.NotFoundException rnf = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } catch (IOException ex2) {
            Resources.NotFoundException rnf2 = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));
            rnf2.initCause(ex2);
            throw rnf2;
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
            throw th;
        }
    }

    private static Interpolator createInterpolatorFromXml(Resources res, Resources.Theme theme, XmlPullParser parser) throws XmlPullParserException, IOException {
        Interpolator interpolator = null;
        int depth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if ((type != 3 || parser.getDepth() > depth) && type != 1) {
                if (type == 2) {
                    AttributeSet attrs = Xml.asAttributeSet(parser);
                    String name = parser.getName();
                    if (name.equals("linearInterpolator")) {
                        interpolator = new LinearInterpolator();
                    } else if (name.equals("accelerateInterpolator")) {
                        interpolator = new AccelerateInterpolator(res, theme, attrs);
                    } else if (name.equals("decelerateInterpolator")) {
                        interpolator = new DecelerateInterpolator(res, theme, attrs);
                    } else if (name.equals("accelerateDecelerateInterpolator")) {
                        interpolator = new AccelerateDecelerateInterpolator();
                    } else if (name.equals("cycleInterpolator")) {
                        interpolator = new CycleInterpolator(res, theme, attrs);
                    } else if (name.equals("anticipateInterpolator")) {
                        interpolator = new AnticipateInterpolator(res, theme, attrs);
                    } else if (name.equals("overshootInterpolator")) {
                        interpolator = new OvershootInterpolator(res, theme, attrs);
                    } else if (name.equals("anticipateOvershootInterpolator")) {
                        interpolator = new AnticipateOvershootInterpolator(res, theme, attrs);
                    } else if (name.equals("bounceInterpolator")) {
                        interpolator = new BounceInterpolator();
                    } else if (name.equals("pathInterpolator")) {
                        interpolator = new PathInterpolator(res, theme, attrs);
                    } else {
                        throw new RuntimeException("Unknown interpolator name: " + parser.getName());
                    }
                }
            }
        }
        return interpolator;
    }
}
