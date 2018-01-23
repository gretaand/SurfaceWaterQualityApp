package com.github.gretaand.surfacewaterqualityapp.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.View;

import com.github.gretaand.surfacewaterqualityapp.R;

/**
 * @author greta
 */
public class ClusterMarkerView extends View {

    private final Paint mPaint;
    private final Context mContext;
    private float mGreenSweepAngle;
    private float mYellowSweepAngle;
    private float mOrangeSweepAngle;
    private float mRedSweepAngle;
    private float mGraySweepAngle;

    /**
     * sets up the view and initialize paint object
     *
     * @param context context
     * @param attr view attributes
     */
    public ClusterMarkerView(Context context, AttributeSet attr) {
        super(context, attr);
        mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

    }

    /**
     * sets the sweep angles for the circle arcs for each color based on the proportion of
     * marker color represented by the cluster
     *
     * @param clusterWarningLevels map of warning level categories and the number of markers in each
     * @param clusterSize number of markers in the cluster
     */
    public void setSweepAngles(SparseIntArray clusterWarningLevels, int clusterSize) {
        for (int i = 0; i < clusterWarningLevels.size(); i++) {
            int key = clusterWarningLevels.keyAt(i);
            float value = (float) clusterWarningLevels.valueAt(i);
            float sweepAngle = value/(float) clusterSize * 360;
            switch (key) {
                case 4:
                    mRedSweepAngle = sweepAngle;
                    break;
                case 3:
                    mOrangeSweepAngle = sweepAngle;
                    break;
                case 2:
                    mYellowSweepAngle = sweepAngle;
                    break;
                case 1:
                    mGreenSweepAngle = sweepAngle;
                    break;
                case -1:
                    mGraySweepAngle = sweepAngle;
                    break;
            }
        }
    }

    /**
     * draws the view
     *
     * @param canvas canvas to draw
     */
    @Override
    protected void onDraw(Canvas canvas) {

        float dimen = getResources().getDimension(R.dimen.cluster_radius);
        float startAngle = 18;
        mPaint.setStyle(Paint.Style.FILL);

        // green
        mPaint.setColor(ContextCompat.getColor(mContext, R.color.markerGreen));
        canvas.drawArc(0, 0, dimen * 2, dimen * 2, startAngle, mGreenSweepAngle,
                true, mPaint);
        startAngle += mGreenSweepAngle;

        // yellow
        mPaint.setColor(ContextCompat.getColor(mContext, R.color.markerYellow));
        canvas.drawArc(0, 0, dimen * 2, dimen * 2, startAngle, mYellowSweepAngle,
                true, mPaint);
        startAngle += mYellowSweepAngle;

        // orange
        mPaint.setColor(ContextCompat.getColor(mContext, R.color.markerOrange));
        canvas.drawArc(0, 0, dimen * 2, dimen * 2, startAngle, mOrangeSweepAngle,
                true, mPaint);
        startAngle += mOrangeSweepAngle;

        // red
        mPaint.setColor(ContextCompat.getColor(mContext, R.color.markerRed));
        canvas.drawArc(0, 0, dimen * 2, dimen * 2, startAngle, mRedSweepAngle,
                true, mPaint);
        startAngle += mRedSweepAngle;

        // gray
        mPaint.setColor(ContextCompat.getColor(mContext, R.color.markerGray));
        canvas.drawArc(0, 0, dimen * 2, dimen * 2, startAngle, mGraySweepAngle,
                true, mPaint);

        // border
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
        float strokeWidth = getResources().getDimension(R.dimen.marker_strokeWidth);
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        canvas.drawCircle(dimen, dimen, dimen - ( strokeWidth / 2 ), mPaint);

    }

}
