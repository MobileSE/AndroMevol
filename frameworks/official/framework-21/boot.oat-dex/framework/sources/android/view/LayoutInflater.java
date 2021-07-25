package android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.media.TtmlUtils;
import android.os.Handler;
import android.os.Message;
import android.os.Trace;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.internal.R;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class LayoutInflater {
    private static final int[] ATTRS_THEME = {16842752};
    private static final boolean DEBUG = false;
    private static final String TAG = LayoutInflater.class.getSimpleName();
    private static final String TAG_1995 = "blink";
    private static final String TAG_INCLUDE = "include";
    private static final String TAG_MERGE = "merge";
    private static final String TAG_REQUEST_FOCUS = "requestFocus";
    private static final String TAG_TAG = "tag";
    static final Class<?>[] mConstructorSignature = {Context.class, AttributeSet.class};
    private static final HashMap<String, Constructor<? extends View>> sConstructorMap = new HashMap<>();
    final Object[] mConstructorArgs;
    protected final Context mContext;
    private Factory mFactory;
    private Factory2 mFactory2;
    private boolean mFactorySet;
    private Filter mFilter;
    private HashMap<String, Boolean> mFilterMap;
    private Factory2 mPrivateFactory;

    public interface Factory {
        View onCreateView(String str, Context context, AttributeSet attributeSet);
    }

    public interface Factory2 extends Factory {
        View onCreateView(View view, String str, Context context, AttributeSet attributeSet);
    }

    public interface Filter {
        boolean onLoadClass(Class cls);
    }

    public abstract LayoutInflater cloneInContext(Context context);

    /* access modifiers changed from: private */
    public static class FactoryMerger implements Factory2 {
        private final Factory mF1;
        private final Factory2 mF12;
        private final Factory mF2;
        private final Factory2 mF22;

        FactoryMerger(Factory f1, Factory2 f12, Factory f2, Factory2 f22) {
            this.mF1 = f1;
            this.mF2 = f2;
            this.mF12 = f12;
            this.mF22 = f22;
        }

        @Override // android.view.LayoutInflater.Factory
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            View v = this.mF1.onCreateView(name, context, attrs);
            return v != null ? v : this.mF2.onCreateView(name, context, attrs);
        }

        @Override // android.view.LayoutInflater.Factory2
        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
            View v = this.mF12 != null ? this.mF12.onCreateView(parent, name, context, attrs) : this.mF1.onCreateView(name, context, attrs);
            if (v != null) {
                return v;
            }
            return this.mF22 != null ? this.mF22.onCreateView(parent, name, context, attrs) : this.mF2.onCreateView(name, context, attrs);
        }
    }

    protected LayoutInflater(Context context) {
        this.mConstructorArgs = new Object[2];
        this.mContext = context;
    }

    protected LayoutInflater(LayoutInflater original, Context newContext) {
        this.mConstructorArgs = new Object[2];
        this.mContext = newContext;
        this.mFactory = original.mFactory;
        this.mFactory2 = original.mFactory2;
        this.mPrivateFactory = original.mPrivateFactory;
        setFilter(original.mFilter);
    }

    public static LayoutInflater from(Context context) {
        LayoutInflater LayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (LayoutInflater != null) {
            return LayoutInflater;
        }
        throw new AssertionError("LayoutInflater not found.");
    }

    public Context getContext() {
        return this.mContext;
    }

    public final Factory getFactory() {
        return this.mFactory;
    }

    public final Factory2 getFactory2() {
        return this.mFactory2;
    }

    public void setFactory(Factory factory) {
        if (this.mFactorySet) {
            throw new IllegalStateException("A factory has already been set on this LayoutInflater");
        } else if (factory == null) {
            throw new NullPointerException("Given factory can not be null");
        } else {
            this.mFactorySet = true;
            if (this.mFactory == null) {
                this.mFactory = factory;
            } else {
                this.mFactory = new FactoryMerger(factory, null, this.mFactory, this.mFactory2);
            }
        }
    }

    public void setFactory2(Factory2 factory) {
        if (this.mFactorySet) {
            throw new IllegalStateException("A factory has already been set on this LayoutInflater");
        } else if (factory == null) {
            throw new NullPointerException("Given factory can not be null");
        } else {
            this.mFactorySet = true;
            if (this.mFactory == null) {
                this.mFactory2 = factory;
                this.mFactory = factory;
                return;
            }
            FactoryMerger factoryMerger = new FactoryMerger(factory, factory, this.mFactory, this.mFactory2);
            this.mFactory2 = factoryMerger;
            this.mFactory = factoryMerger;
        }
    }

    public void setPrivateFactory(Factory2 factory) {
        if (this.mPrivateFactory == null) {
            this.mPrivateFactory = factory;
        } else {
            this.mPrivateFactory = new FactoryMerger(factory, factory, this.mPrivateFactory, this.mPrivateFactory);
        }
    }

    public Filter getFilter() {
        return this.mFilter;
    }

    public void setFilter(Filter filter) {
        this.mFilter = filter;
        if (filter != null) {
            this.mFilterMap = new HashMap<>();
        }
    }

    public View inflate(int resource, ViewGroup root) {
        return inflate(resource, root, root != null);
    }

    public View inflate(XmlPullParser parser, ViewGroup root) {
        return inflate(parser, root, root != null);
    }

    public View inflate(int resource, ViewGroup root, boolean attachToRoot) {
        XmlResourceParser parser = getContext().getResources().getLayout(resource);
        try {
            return inflate(parser, root, attachToRoot);
        } finally {
            parser.close();
        }
    }

    public View inflate(XmlPullParser parser, ViewGroup root, boolean attachToRoot) {
        View result;
        int type;
        synchronized (this.mConstructorArgs) {
            Trace.traceBegin(8, "inflate");
            AttributeSet attrs = Xml.asAttributeSet(parser);
            Context lastContext = (Context) this.mConstructorArgs[0];
            this.mConstructorArgs[0] = this.mContext;
            result = root;
            do {
                try {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } catch (XmlPullParserException e) {
                    InflateException ex = new InflateException(e.getMessage());
                    ex.initCause(e);
                    throw ex;
                } catch (IOException e2) {
                    InflateException ex2 = new InflateException(parser.getPositionDescription() + ": " + e2.getMessage());
                    ex2.initCause(e2);
                    throw ex2;
                } catch (Throwable th) {
                    this.mConstructorArgs[0] = lastContext;
                    this.mConstructorArgs[1] = null;
                    throw th;
                }
            } while (type != 1);
            if (type != 2) {
                throw new InflateException(parser.getPositionDescription() + ": No start tag found!");
            }
            String name = parser.getName();
            if (!TAG_MERGE.equals(name)) {
                View temp = createViewFromTag(root, name, attrs, false);
                ViewGroup.LayoutParams params = null;
                if (root != null) {
                    params = root.generateLayoutParams(attrs);
                    if (!attachToRoot) {
                        temp.setLayoutParams(params);
                    }
                }
                rInflate(parser, temp, attrs, true, true);
                if (root != null && attachToRoot) {
                    root.addView(temp, params);
                }
                if (root == null || !attachToRoot) {
                    result = temp;
                }
            } else if (root == null || !attachToRoot) {
                throw new InflateException("<merge /> can be used only with a valid ViewGroup root and attachToRoot=true");
            } else {
                rInflate(parser, root, attrs, false, false);
            }
            this.mConstructorArgs[0] = lastContext;
            this.mConstructorArgs[1] = null;
            Trace.traceEnd(8);
        }
        return result;
    }

    public final View createView(String name, String prefix, AttributeSet attrs) throws ClassNotFoundException, InflateException {
        String str;
        String str2;
        Constructor<? extends View> constructor = sConstructorMap.get(name);
        Class<? extends View> clazz = null;
        try {
            Trace.traceBegin(8, name);
            if (constructor == null) {
                ClassLoader classLoader = this.mContext.getClassLoader();
                if (prefix != null) {
                    str2 = prefix + name;
                } else {
                    str2 = name;
                }
                Class<? extends View> clazz2 = classLoader.loadClass(str2).asSubclass(View.class);
                if (!(this.mFilter == null || clazz2 == null || this.mFilter.onLoadClass(clazz2))) {
                    failNotAllowed(name, prefix, attrs);
                }
                constructor = clazz2.getConstructor(mConstructorSignature);
                sConstructorMap.put(name, constructor);
            } else if (this.mFilter != null) {
                Boolean allowedState = this.mFilterMap.get(name);
                if (allowedState == null) {
                    ClassLoader classLoader2 = this.mContext.getClassLoader();
                    if (prefix != null) {
                        str = prefix + name;
                    } else {
                        str = name;
                    }
                    Class<? extends View> clazz3 = classLoader2.loadClass(str).asSubclass(View.class);
                    boolean allowed = clazz3 != null && this.mFilter.onLoadClass(clazz3);
                    this.mFilterMap.put(name, Boolean.valueOf(allowed));
                    if (!allowed) {
                        failNotAllowed(name, prefix, attrs);
                    }
                } else if (allowedState.equals(Boolean.FALSE)) {
                    failNotAllowed(name, prefix, attrs);
                }
            }
            Object[] args = this.mConstructorArgs;
            args[1] = attrs;
            constructor.setAccessible(true);
            View view = (View) constructor.newInstance(args);
            if (view instanceof ViewStub) {
                ((ViewStub) view).setLayoutInflater(cloneInContext((Context) args[0]));
            }
            Trace.traceEnd(8);
            return view;
        } catch (NoSuchMethodException e) {
            StringBuilder append = new StringBuilder().append(attrs.getPositionDescription()).append(": Error inflating class ");
            if (prefix != null) {
                name = prefix + name;
            }
            InflateException ie = new InflateException(append.append(name).toString());
            ie.initCause(e);
            throw ie;
        } catch (ClassCastException e2) {
            StringBuilder append2 = new StringBuilder().append(attrs.getPositionDescription()).append(": Class is not a View ");
            if (prefix != null) {
                name = prefix + name;
            }
            InflateException ie2 = new InflateException(append2.append(name).toString());
            ie2.initCause(e2);
            throw ie2;
        } catch (ClassNotFoundException e3) {
            throw e3;
        } catch (Exception e4) {
            InflateException ie3 = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + (0 == 0 ? MediaStore.UNKNOWN_STRING : clazz.getName()));
            ie3.initCause(e4);
            throw ie3;
        } catch (Throwable th) {
            Trace.traceEnd(8);
            throw th;
        }
    }

    private void failNotAllowed(String name, String prefix, AttributeSet attrs) {
        StringBuilder append = new StringBuilder().append(attrs.getPositionDescription()).append(": Class not allowed to be inflated ");
        if (prefix != null) {
            name = prefix + name;
        }
        throw new InflateException(append.append(name).toString());
    }

    /* access modifiers changed from: protected */
    public View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        return createView(name, "android.view.", attrs);
    }

    /* access modifiers changed from: protected */
    public View onCreateView(View parent, String name, AttributeSet attrs) throws ClassNotFoundException {
        return onCreateView(name, attrs);
    }

    /* access modifiers changed from: package-private */
    public View createViewFromTag(View parent, String name, AttributeSet attrs, boolean inheritContext) {
        Context viewContext;
        View view;
        View view2;
        if (name.equals("view")) {
            name = attrs.getAttributeValue(null, "class");
        }
        if (parent == null || !inheritContext) {
            viewContext = this.mContext;
        } else {
            viewContext = parent.getContext();
        }
        TypedArray ta = viewContext.obtainStyledAttributes(attrs, ATTRS_THEME);
        int themeResId = ta.getResourceId(0, 0);
        if (themeResId != 0) {
            viewContext = new ContextThemeWrapper(viewContext, themeResId);
        }
        ta.recycle();
        if (name.equals(TAG_1995)) {
            return new BlinkLayout(viewContext, attrs);
        }
        try {
            if (this.mFactory2 != null) {
                view = this.mFactory2.onCreateView(parent, name, viewContext, attrs);
            } else if (this.mFactory != null) {
                view = this.mFactory.onCreateView(name, viewContext, attrs);
            } else {
                view = null;
            }
            if (view == null && this.mPrivateFactory != null) {
                view = this.mPrivateFactory.onCreateView(parent, name, viewContext, attrs);
            }
            if (view != null) {
                return view;
            }
            Object lastContext = this.mConstructorArgs[0];
            this.mConstructorArgs[0] = viewContext;
            try {
                if (-1 == name.indexOf(46)) {
                    view2 = onCreateView(parent, name, attrs);
                } else {
                    view2 = createView(name, null, attrs);
                }
                return view2;
            } finally {
                this.mConstructorArgs[0] = lastContext;
            }
        } catch (InflateException e) {
            throw e;
        } catch (ClassNotFoundException e2) {
            InflateException ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name);
            ie.initCause(e2);
            throw ie;
        } catch (Exception e3) {
            InflateException ie2 = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name);
            ie2.initCause(e3);
            throw ie2;
        }
    }

    /* access modifiers changed from: package-private */
    public void rInflate(XmlPullParser parser, View parent, AttributeSet attrs, boolean finishInflate, boolean inheritContext) throws XmlPullParserException, IOException {
        int depth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if ((type != 3 || parser.getDepth() > depth) && type != 1) {
                if (type == 2) {
                    String name = parser.getName();
                    if (TAG_REQUEST_FOCUS.equals(name)) {
                        parseRequestFocus(parser, parent);
                    } else if ("tag".equals(name)) {
                        parseViewTag(parser, parent, attrs);
                    } else if (TAG_INCLUDE.equals(name)) {
                        if (parser.getDepth() == 0) {
                            throw new InflateException("<include /> cannot be the root element");
                        }
                        parseInclude(parser, parent, attrs, inheritContext);
                    } else if (TAG_MERGE.equals(name)) {
                        throw new InflateException("<merge /> must be the root element");
                    } else {
                        View view = createViewFromTag(parent, name, attrs, inheritContext);
                        ViewGroup viewGroup = (ViewGroup) parent;
                        ViewGroup.LayoutParams params = viewGroup.generateLayoutParams(attrs);
                        rInflate(parser, view, attrs, true, true);
                        viewGroup.addView(view, params);
                    }
                }
            }
        }
        if (finishInflate) {
            parent.onFinishInflate();
        }
    }

    private void parseRequestFocus(XmlPullParser parser, View view) throws XmlPullParserException, IOException {
        int type;
        view.requestFocus();
        int currentDepth = parser.getDepth();
        do {
            type = parser.next();
            if (type == 3 && parser.getDepth() <= currentDepth) {
                return;
            }
        } while (type != 1);
    }

    private void parseViewTag(XmlPullParser parser, View view, AttributeSet attrs) throws XmlPullParserException, IOException {
        int type;
        TypedArray ta = this.mContext.obtainStyledAttributes(attrs, R.styleable.ViewTag);
        view.setTag(ta.getResourceId(1, 0), ta.getText(0));
        ta.recycle();
        int currentDepth = parser.getDepth();
        do {
            type = parser.next();
            if (type == 3 && parser.getDepth() <= currentDepth) {
                return;
            }
        } while (type != 1);
    }

    /* JADX INFO: finally extract failed */
    private void parseInclude(XmlPullParser parser, View parent, AttributeSet attrs, boolean inheritContext) throws XmlPullParserException, IOException {
        int type;
        int type2;
        if (parent instanceof ViewGroup) {
            int layout = attrs.getAttributeResourceValue(null, TtmlUtils.TAG_LAYOUT, 0);
            if (layout == 0) {
                String value = attrs.getAttributeValue(null, TtmlUtils.TAG_LAYOUT);
                if (value == null) {
                    throw new InflateException("You must specifiy a layout in the include tag: <include layout=\"@layout/layoutID\" />");
                }
                throw new InflateException("You must specifiy a valid layout reference. The layout ID " + value + " is not valid.");
            }
            XmlResourceParser childParser = getContext().getResources().getLayout(layout);
            try {
                AttributeSet childAttrs = Xml.asAttributeSet(childParser);
                do {
                    type = childParser.next();
                    if (type == 2) {
                        break;
                    }
                } while (type != 1);
                if (type != 2) {
                    throw new InflateException(childParser.getPositionDescription() + ": No start tag found!");
                }
                String childName = childParser.getName();
                if (TAG_MERGE.equals(childName)) {
                    rInflate(childParser, parent, childAttrs, false, inheritContext);
                } else {
                    View view = createViewFromTag(parent, childName, childAttrs, inheritContext);
                    ViewGroup group = (ViewGroup) parent;
                    try {
                        ViewGroup.LayoutParams params = group.generateLayoutParams(attrs);
                        if (params != null) {
                            view.setLayoutParams(params);
                        }
                    } catch (RuntimeException e) {
                        ViewGroup.LayoutParams params2 = group.generateLayoutParams(childAttrs);
                        if (params2 != null) {
                            view.setLayoutParams(params2);
                        }
                    } catch (Throwable th) {
                        if (0 != 0) {
                            view.setLayoutParams(null);
                        }
                        throw th;
                    }
                    rInflate(childParser, view, childAttrs, true, true);
                    TypedArray a = this.mContext.obtainStyledAttributes(attrs, R.styleable.View, 0, 0);
                    int id = a.getResourceId(9, -1);
                    int visibility = a.getInt(21, -1);
                    a.recycle();
                    if (id != -1) {
                        view.setId(id);
                    }
                    switch (visibility) {
                        case 0:
                            view.setVisibility(0);
                            break;
                        case 1:
                            view.setVisibility(4);
                            break;
                        case 2:
                            view.setVisibility(8);
                            break;
                    }
                    group.addView(view);
                }
                childParser.close();
                int currentDepth = parser.getDepth();
                do {
                    type2 = parser.next();
                    if (type2 == 3 && parser.getDepth() <= currentDepth) {
                        return;
                    }
                } while (type2 != 1);
            } catch (Throwable th2) {
                childParser.close();
                throw th2;
            }
        } else {
            throw new InflateException("<include /> can only be used inside of a ViewGroup");
        }
    }

    /* access modifiers changed from: private */
    public static class BlinkLayout extends FrameLayout {
        private static final int BLINK_DELAY = 500;
        private static final int MESSAGE_BLINK = 66;
        private boolean mBlink;
        private boolean mBlinkState;
        private final Handler mHandler = new Handler(new Handler.Callback() {
            /* class android.view.LayoutInflater.BlinkLayout.AnonymousClass1 */

            @Override // android.os.Handler.Callback
            public boolean handleMessage(Message msg) {
                boolean z = false;
                if (msg.what != 66) {
                    return false;
                }
                if (BlinkLayout.this.mBlink) {
                    BlinkLayout blinkLayout = BlinkLayout.this;
                    if (!BlinkLayout.this.mBlinkState) {
                        z = true;
                    }
                    blinkLayout.mBlinkState = z;
                    BlinkLayout.this.makeBlink();
                }
                BlinkLayout.this.invalidate();
                return true;
            }
        });

        public BlinkLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void makeBlink() {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(66), 500);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View, android.view.ViewGroup
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            this.mBlink = true;
            this.mBlinkState = true;
            makeBlink();
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View, android.view.ViewGroup
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            this.mBlink = false;
            this.mBlinkState = true;
            this.mHandler.removeMessages(66);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View, android.view.ViewGroup
        public void dispatchDraw(Canvas canvas) {
            if (this.mBlinkState) {
                super.dispatchDraw(canvas);
            }
        }
    }
}
