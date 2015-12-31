package com.malata.superclean.circularprogressbar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import com.malata.superclean.circularprogressbar.CircularProgressDrawable;
import com.malata.superclean.circularprogressbar.CircularProgressDrawable.Builder;
import com.malata.superclean.R.attr;
import com.malata.superclean.R.styleable;
import com.malata.superclean.R;
import com.malata.superclean.R.dimen;
import com.malata.superclean.R.string;
import com.malata.superclean.R.integer;

public class CircularProgressBar extends ProgressBar {
    public CircularProgressBar(Context context) {
        this(context, (AttributeSet)null);
    }

    public CircularProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, attr.cpbStyle);
    }

    public CircularProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(this.isInEditMode()) {
            this.setIndeterminateDrawable((new Builder(context)).build());
        } else {
            Resources res = context.getResources();
            TypedArray a = context.obtainStyledAttributes(attrs, styleable.CircularProgressBar, defStyle, 0);
            int color = a.getColor(1, res.getColor(R.color.cpb_default_color));
            float strokeWidth = a.getDimension(3, res.getDimension(dimen.cpb_default_stroke_width));
            float sweepSpeed = a.getFloat(6, Float.parseFloat(res.getString(string.cpb_default_sweep_speed)));
            float rotationSpeed = a.getFloat(7, Float.parseFloat(res.getString(string.cpb_default_rotation_speed)));
            int colorsId = a.getResourceId(2, 0);
            int minSweepAngle = a.getInteger(4, res.getInteger(integer.cpb_default_min_sweep_angle));
            int maxSweepAngle = a.getInteger(5, res.getInteger(integer.cpb_default_max_sweep_angle));
            a.recycle();
            int[] colors = null;
            if(colorsId != 0) {
                colors = res.getIntArray(colorsId);
            }

            Builder builder = (new Builder(context)).sweepSpeed(sweepSpeed).rotationSpeed(rotationSpeed).strokeWidth(strokeWidth).minSweepAngle(minSweepAngle).maxSweepAngle(maxSweepAngle);
            if(colors != null && colors.length > 0) {
                builder.colors(colors);
            } else {
                builder.color(color);
            }

            CircularProgressDrawable indeterminateDrawable = builder.build();
            this.setIndeterminateDrawable(indeterminateDrawable);
        }
    }
}
