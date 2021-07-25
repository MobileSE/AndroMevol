package android.graphics.drawable.shapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class PathShape extends Shape {
    private Path mPath;
    private float mScaleX;
    private float mScaleY;
    private float mStdHeight;
    private float mStdWidth;

    public PathShape(Path path, float stdWidth, float stdHeight) {
        this.mPath = path;
        this.mStdWidth = stdWidth;
        this.mStdHeight = stdHeight;
    }

    @Override // android.graphics.drawable.shapes.Shape
    public void draw(Canvas canvas, Paint paint) {
        canvas.save();
        canvas.scale(this.mScaleX, this.mScaleY);
        canvas.drawPath(this.mPath, paint);
        canvas.restore();
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.shapes.Shape
    public void onResize(float width, float height) {
        this.mScaleX = width / this.mStdWidth;
        this.mScaleY = height / this.mStdHeight;
    }

    @Override // android.graphics.drawable.shapes.Shape, android.graphics.drawable.shapes.Shape, java.lang.Object
    public PathShape clone() throws CloneNotSupportedException {
        PathShape shape = (PathShape) super.clone();
        shape.mPath = new Path(this.mPath);
        return shape;
    }
}
