package nl.cwi.dis.physiofashion.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.SeekBar;
import android.widget.TextView;

import nl.cwi.dis.physiofashion.R;

/**
 * This class represents a custom view and is essentially a wrapper around a standard SeekBar, but
 * adds a title label and labels at either end of the scale to it. This class should not be
 * instantiated directly and is usually instantiated by the view system from XML.
 */
public class LikertScale extends ConstraintLayout {
    /**
     * Initialise new view without attributes.
     *
     * @param context Application context
     */
    public LikertScale(Context context) {
        super(context, null);
    }

    /**
     * Initialise new view with the given attributes.
     *
     * @param context Application context
     * @param attributeSet View attributes
     */
    public LikertScale(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        // Inflate the layout defined in the XML file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_likert_scale, this, true);

        // Obtain an array for the view attributes
        TypedArray attrs = context.obtainStyledAttributes(attributeSet, R.styleable.LikertScale);

        // Extract view attributes
        String title = attrs.getString(R.styleable.LikertScale_title);
        String leftLabel = attrs.getString(R.styleable.LikertScale_leftLabel);
        String rightLabel = attrs.getString(R.styleable.LikertScale_rightLabel);

        // Recycle attributes
        attrs.recycle();

        // Set Likert scale title
        TextView titleText = (TextView) getChildAt(1);
        titleText.setText(title);

        // Set left label of scale
        TextView leftText = (TextView) getChildAt(2);
        leftText.setText(leftLabel);

        // Set right label of scale
        TextView rightText = (TextView) getChildAt(3);
        rightText.setText(rightLabel);
    }

    /**
     * Gets the progress of the encapsulated seek bar.
     *
     * @return The progress (i.e. current value) of the seek bar
     */
    public int getProgress() {
        SeekBar seekBar = (SeekBar) getChildAt(0);
        return seekBar.getProgress();
    }
}
