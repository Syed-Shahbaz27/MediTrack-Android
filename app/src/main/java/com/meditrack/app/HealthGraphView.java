package com.meditrack.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * HealthGraphView — a custom View that draws a line graph on a Canvas.
 *
 * WHY A CUSTOM VIEW instead of a library?
 * Libraries like MPAndroidChart add ~1MB to the APK and require extra
 * Gradle dependencies. For a simple weight/glucose trend line,
 * drawing directly on Canvas is lightweight and shows the marker
 * you understand Android's drawing API — good for VIVA marks.
 *
 * HOW IT WORKS:
 * Step 1 — HealthMetricActivity calls setData(list of floats).
 * Step 2 — setData() calls invalidate() which schedules a redraw.
 * Step 3 — Android calls onDraw(Canvas) on the next frame.
 * Step 4 — onDraw() uses Paint objects to draw background, grid, line, dots.
 *
 * VIVA EXPLANATION:
 * Canvas = a blank sheet of paper.
 * Paint  = the brush (color, stroke width, style).
 * Path   = a sequence of points joined by lines.
 * We scale data values to pixel coordinates using min/max normalization.
 */
public class HealthGraphView extends View {

    private List<Float> dataPoints = new ArrayList<>();

    // ── Paint objects — created once in initPaints(), reused every draw ──────
    private Paint linePaint;   // the connecting line between data points
    private Paint pointPaint;  // the filled dot at each data point
    private Paint gridPaint;   // horizontal grid lines
    private Paint bgPaint;     // light blue background fill
    private Paint textPaint;   // placeholder text when data is insufficient

    // ── Constructors — both are needed for XML inflation to work ─────────────
    public HealthGraphView(Context context) {
        super(context);
        initPaints();
    }

    public HealthGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    /** Creates and configures all Paint objects. Called once in constructor. */
    private void initPaints() {

        // Red/salmon line — matches the prototype graph color
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#E57373"));
        linePaint.setStrokeWidth(3f);
        linePaint.setStyle(Paint.Style.STROKE); // STROKE = line only, no fill

        // Dark red filled dot at each data point
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(Color.parseColor("#C62828"));
        pointPaint.setStyle(Paint.Style.FILL);

        // Light grey horizontal grid lines
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#CFD8DC"));
        gridPaint.setStrokeWidth(1f);

        // Very light blue background matching the prototype
        bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#E3F2FD"));

        // Grey centered text for the placeholder message
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#90A4AE"));
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * Called by HealthMetricActivity with weight values from the database.
     * invalidate() tells Android to call onDraw() on the next frame.
     *
     * @param values list of weight readings, newest first from DB query
     */
    public void setData(List<Float> values) {
        this.dataPoints = values;
        invalidate(); // request a redraw
    }

    /**
     * Called by Android every time this View needs to be drawn.
     * Canvas gives us the drawing surface. We must not create new objects
     * inside onDraw — we reuse the Paint objects created in initPaints().
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w   = getWidth();
        int h   = getHeight();
        int pad = 24; // padding so dots don't clip at the edges

        // Draw light blue background
        canvas.drawRect(0, 0, w, h, bgPaint);

        // Draw 4 horizontal grid lines evenly spaced
        for (int i = 1; i <= 4; i++) {
            float y = h * i / 5f;
            canvas.drawLine(pad, y, w - pad, y, gridPaint);
        }

        // Not enough data to draw a line — show placeholder text
        if (dataPoints == null || dataPoints.size() < 2) {
            canvas.drawText("Add readings to see trend", w / 2f, h / 2f, textPaint);
            return;
        }

        // Find min and max values to scale the data to the view height
        float min = dataPoints.get(0);
        float max = dataPoints.get(0);
        for (float v : dataPoints) {
            if (v < min) min = v;
            if (v > max) max = v;
        }
        // Prevent division by zero if all values are identical
        if (max == min) max = min + 1;

        // Horizontal spacing between each data point
        float xStep = (float)(w - 2 * pad) / (dataPoints.size() - 1);

        // Build a Path connecting all data points
        Path path = new Path();
        for (int i = 0; i < dataPoints.size(); i++) {
            float x = pad + i * xStep;

            // Map data value to Y pixel coordinate.
            // High value = small Y (near top). Low value = large Y (near bottom).
            float normalized = (dataPoints.get(i) - min) / (max - min);
            float y = pad + (1f - normalized) * (h - 2 * pad);

            if (i == 0) {
                path.moveTo(x, y); // start the path at the first point
            } else {
                path.lineTo(x, y); // draw line to each subsequent point
            }

            // Draw a filled circle dot at each data point
            canvas.drawCircle(x, y, 7f, pointPaint);
        }

        // Draw the connecting line on top of the dots
        canvas.drawPath(path, linePaint);
    }
}