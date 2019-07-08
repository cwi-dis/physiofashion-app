package nl.cwi.dis.physiofashion.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import nl.cwi.dis.physiofashion.R;

public class SelfAssessmentManikin extends ConstraintLayout {
    private enum ManikinType {
        AROUSAL, VALENCE
    }

    private final int[] VALENCE_MANIKINS = new int[] {
            R.drawable.ic_valence_1,
            R.drawable.ic_valence_2,
            R.drawable.ic_valence_3,
            R.drawable.ic_valence_4,
            R.drawable.ic_valence_5,
    };

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
        ManikinType type = getManikinType(attrs.getInt(R.styleable.SelfAssessmentManikin_manikinType,0));

        attrs.recycle();

        TextView titleView = (TextView) getChildAt(0);
        titleView.setText(title);

        for (int i=1; i<=5; i++) {
            ImageView manikin = (ImageView) getChildAt(i);
            manikin.setAlpha(0.25f);
            ViewGroup.LayoutParams params = manikin.getLayoutParams();

            if (type == ManikinType.VALENCE) {
                manikin.setImageResource(VALENCE_MANIKINS[i-1]);
            }

            manikin.setOnClickListener(v -> {
                for (int j=1; j<=5; j++) {
                    ImageView otherManikin = (ImageView) getChildAt(j);
                    otherManikin.setAlpha(0.25f);
                }

                v.setAlpha(1);
            });

            params.width = size;
            params.height = size;

            manikin.setLayoutParams(params);
            manikin.requestLayout();
        }
    }

    private ManikinType getManikinType(int value) {
        if (value == 1) {
            return ManikinType.VALENCE;
        }

        return ManikinType.AROUSAL;
    }
}
