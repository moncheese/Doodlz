package com.teamproject.doodlz.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;

public class DrawingView extends View {

    public static final float TOUCH_TOLERANCE = 10;

    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Paint paintScreen;
    private Paint brush;
    private HashMap<Integer, Path> pathHashMap;
    private HashMap<Integer, Point> previousPointHashMap;

    public DrawingView(Context context) {
        super(context);

        paintScreen = new Paint();

        brush = new Paint();
        brush.setAntiAlias(true);
        brush.setColor(Color.BLACK);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeWidth(8);
        brush.setStrokeCap(Paint.Cap.ROUND);

        pathHashMap = new HashMap<>();
        previousPointHashMap = new HashMap<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);

        bitmap.eraseColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paintScreen);

        for (int key : pathHashMap.keySet()) {
            canvas.drawPath(pathHashMap.get(key), brush);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            touchStarted(event.getX(), event.getY(), event.getPointerId(actionIndex));

        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            touchEnded(event.getPointerId(actionIndex));
            Log.println(Log.INFO, "Test", "" + actionIndex);
        } else {
            touchMoved(event);
        }

        invalidate();

        return true;
    }

    private void touchMoved(MotionEvent event) {

        for (int i = 0; i < event.getPointerCount(); i++) {
            int pointerId = event.getPointerId(i);
            int pointerIndex = event.findPointerIndex(pointerId);

            if (pathHashMap.containsKey(pointerId)) {
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);

                Path path = pathHashMap.get(pointerId);
                Point point = previousPointHashMap.get(pointerId);

                assert point != null;
                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY - point.x);

                if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
                    assert path != null;
                    path.quadTo(point.x, point.y, (newX + point.x) / 2, (newY + point.y) / 2);

                    point.x = (int) newX;
                    point.y = (int) newY;
                }
            }
        }

    }

    private void touchEnded(int pointerId) {
        Path path = pathHashMap.get(pointerId);
        assert path != null;
        bitmapCanvas.drawPath(path, brush);
        path.reset();
    }

    private void touchStarted(float x, float y, int pointerId) {
        Path path;
        Point point;

        if (pathHashMap.containsKey(pointerId)) {
            path = pathHashMap.get(pointerId);
            point = previousPointHashMap.get(pointerId);
        } else {
            path = new Path();
            pathHashMap.put(pointerId, path);
            point = new Point();
            previousPointHashMap.put(pointerId, point);
        }

        assert path != null;

        path.moveTo(x, y);

        assert point != null;

        point.x = (int) x;
        point.y = (int) y;
    }

    public void clear() {
        pathHashMap.clear();
        previousPointHashMap.clear();
        bitmap.eraseColor(Color.WHITE);
        invalidate();
    }

    public void setDrawingColor(int color) {
        brush.setColor(color);
    }

    public int getDrawingColor() {
        return brush.getColor();
    }


    public void setLineWidth(int width) {
        brush.setStrokeWidth(width);
    }

    public float getLineWidth(int width) {
        return brush.getStrokeWidth();
    }

    public void saveImage() {
        final String name = "Doodlz" + System.currentTimeMillis() + ".jpg";
        String description = "Doodlz Drawing";

        String location = MediaStore.Images.Media.insertImage(getContext().getContentResolver(),
                bitmap, name, description);

        if (location != null) {
            Toast.makeText(getContext(), "Image saved!", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_LONG).show();
        }
    }

    public void loadBitmap(Bitmap bitmap, int appliedRotation) {
        this.bitmap = transformBitmap(bitmap, appliedRotation).copy(Bitmap.Config.ARGB_8888, true);
        bitmapCanvas = new Canvas(this.bitmap);
    }

    private Bitmap transformBitmap(Bitmap bitmap, int appliedRotation) {
        Matrix matrix = new Matrix();
        matrix.postRotate(appliedRotation);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
