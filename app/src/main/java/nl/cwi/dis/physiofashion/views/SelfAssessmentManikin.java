package nl.cwi.dis.physiofashion.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        int size = attrs.getDimensionPixelSize(R.styleable.SelfAssessmentManikin_size, 0);

        attrs.recycle();

        TextView titleView = (TextView) getChildAt(0);
        titleView.setText(title);

        for (int i=1; i<=5; i++) {
            ImageView manikin = (ImageView) getChildAt(i);
            ViewGroup.LayoutParams params = manikin.getLayoutParams();

            params.width = size;
            params.height = size;

            manikin.setLayoutParams(params);
            manikin.requestLayout();
        }
    }
    }
}
