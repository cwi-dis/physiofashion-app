package nl.cwi.dis.physiofashion.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import nl.cwi.dis.physiofashion.R;

public class SelfAssessmentManikin extends ConstraintLayout {
    public SelfAssessmentManikin(Context context) {
        super(context, null);
    }

    public SelfAssessmentManikin(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_self_assessment_manikin, this, true);

        TypedArray attrs = context.obtainStyledAttributes(attributeSet, R.styleable.SelfAssessmentManikin);

        String title = attrs.getString(R.styleable.SelfAssessmentManikin_title);
        int scaleType = attrs.getInt(R.styleable.SelfAssessmentManikin_scaleType, 9);
        String leftLabel = attrs.getString(R.styleable.SelfAssessmentManikin_leftLabel);
        String rightLabel = attrs.getString(R.styleable.SelfAssessmentManikin_rightLabel);

        attrs.recycle();

        TextView titleView = (TextView) getChildAt(0);
        titleView.setText(title);
    }
}
