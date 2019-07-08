package nl.cwi.dis.physiofashion;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.SeekBar;
import android.widget.TextView;

public class LikertScale extends ConstraintLayout {
    public LikertScale(Context context) {
        super(context, null);
    }

    public LikertScale(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_likert_scale, this, true);

        TypedArray attrs = context.obtainStyledAttributes(attributeSet, R.styleable.LikertScale);

        String title = attrs.getString(R.styleable.LikertScale_title);
        String leftLabel = attrs.getString(R.styleable.LikertScale_leftLabel);
        String rightLabel = attrs.getString(R.styleable.LikertScale_rightLabel);

        attrs.recycle();

        TextView titleText = (TextView) getChildAt(1);
        titleText.setText(title);

        TextView leftText = (TextView) getChildAt(2);
        leftText.setText(leftLabel);

        TextView rightText = (TextView) getChildAt(3);
        rightText.setText(rightLabel);
    }

    public int getProgress() {
        SeekBar seekBar = (SeekBar) getChildAt(0);
        return seekBar.getProgress();
    }
}
