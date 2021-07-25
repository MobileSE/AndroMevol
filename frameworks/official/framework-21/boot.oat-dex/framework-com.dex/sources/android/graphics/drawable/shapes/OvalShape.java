package android.graphics.drawable.shapes;

import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.RectF;

public class OvalShape extends RectShape {
    @Override // android.graphics.drawable.shapes.Shape, android.graphics.drawable.shapes.RectShape
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawOval(rect(), paint);
    }

    @Override // android.graphics.drawable.shapes.Shape, android.graphics.drawable.shapes.RectShape
    public void getOutline(Outline outline) {
        RectF rect = rect();
        outline.setOval((int) Math.ceil((double) rect.left), (int) Math.ceil((double) rect.top), (int) Math.floor((double) rect.right), (int) Math.floor((double) rect.bottom));
    }
}
