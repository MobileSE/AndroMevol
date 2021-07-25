package android.renderscript;

public class Path extends BaseObj {
    boolean mCoverageToAlpha;
    Allocation mLoopBuffer;
    Primitive mPrimitive;
    float mQuality;
    Allocation mVertexBuffer;

    public enum Primitive {
        QUADRATIC_BEZIER(0),
        CUBIC_BEZIER(1);
        
        int mID;

        private Primitive(int id) {
            this.mID = id;
        }
    }

    Path(long id, RenderScript rs, Primitive p, Allocation vtx, Allocation loop, float q) {
        super(id, rs);
        this.mVertexBuffer = vtx;
        this.mLoopBuffer = loop;
        this.mPrimitive = p;
        this.mQuality = q;
    }

    public Allocation getVertexAllocation() {
        return this.mVertexBuffer;
    }

    public Allocation getLoopAllocation() {
        return this.mLoopBuffer;
    }

    public Primitive getPrimitive() {
        return this.mPrimitive;
    }

    /* access modifiers changed from: package-private */
    @Override // android.renderscript.BaseObj
    public void updateFromNative() {
    }

    public static Path createStaticPath(RenderScript rs, Primitive p, float quality, Allocation vtx) {
        return new Path(rs.nPathCreate(p.mID, false, vtx.getID(rs), 0, quality), rs, p, null, null, quality);
    }

    public static Path createStaticPath(RenderScript rs, Primitive p, float quality, Allocation vtx, Allocation loops) {
        return null;
    }

    public static Path createDynamicPath(RenderScript rs, Primitive p, float quality, Allocation vtx) {
        return null;
    }

    public static Path createDynamicPath(RenderScript rs, Primitive p, float quality, Allocation vtx, Allocation loops) {
        return null;
    }
}
