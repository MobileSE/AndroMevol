package android.renderscript;

import android.renderscript.Script;

public final class ScriptIntrinsicBlur extends ScriptIntrinsic {
    private Allocation mInput;
    private final float[] mValues = new float[9];

    private ScriptIntrinsicBlur(long id, RenderScript rs) {
        super(id, rs);
    }

    public static ScriptIntrinsicBlur create(RenderScript rs, Element e) {
        if (e.isCompatible(Element.U8_4(rs)) || e.isCompatible(Element.U8(rs))) {
            ScriptIntrinsicBlur sib = new ScriptIntrinsicBlur(rs.nScriptIntrinsicCreate(5, e.getID(rs)), rs);
            sib.setRadius(5.0f);
            return sib;
        }
        throw new RSIllegalArgumentException("Unsuported element type.");
    }

    public void setInput(Allocation ain) {
        this.mInput = ain;
        setVar(1, ain);
    }

    public void setRadius(float radius) {
        if (radius <= 0.0f || radius > 25.0f) {
            throw new RSIllegalArgumentException("Radius out of range (0 < r <= 25).");
        }
        setVar(0, radius);
    }

    public void forEach(Allocation aout) {
        forEach(0, (Allocation) null, aout, (FieldPacker) null);
    }

    public void forEach(Allocation aout, Script.LaunchOptions opt) {
        forEach(0, (Allocation) null, aout, (FieldPacker) null, opt);
    }

    public Script.KernelID getKernelID() {
        return createKernelID(0, 2, null, null);
    }

    public Script.FieldID getFieldID_Input() {
        return createFieldID(1, null);
    }
}
