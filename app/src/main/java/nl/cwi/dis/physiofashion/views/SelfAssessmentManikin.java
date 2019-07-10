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
    private enum ManikinType {
        AROUSAL, VALENCE
    }

    @FunctionalInterface
    public interface CallbackFunction {
        void apply(int value);
    }

    private final int[] VALENCE_MANIKINS = new int[] {
            R.drawable.ic_valence_1,
            R.drawable.ic_valence_2,
            R.drawable.ic_valence_3,
            R.drawable.ic_valence_4,
            R.drawable.ic_valence_5,
            R.drawable.ic_valence_6,
            R.drawable.ic_valence_7,
            R.drawable.ic_valence_8,
            R.drawable.ic_valence_9,
    };

    private CallbackFunction callback = null;
    private int selectedValue = -1;

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
        int defaultValue = attrs.getInt(R.styleable.SelfAssessmentManikin_defaultValue, -1);

        attrs.recycle();

        TextView titleView = (TextView) getChildAt(0);
        titleView.setText(title);

        for (int i=1; i<=9; i++) {
            final int value = i - 1;

            ImageView manikin = (ImageView) getChildAt(i);
            ViewGroup.LayoutParams params = manikin.getLayoutParams();

            if (value == defaultValue) {
                manikin.setAlpha(1.0f);
                this.selectedValue = value;
            } else {
                manikin.setAlpha(0.25f);
            }

            if (type == ManikinType.VALENCE) {
                manikin.setImageResource(VALENCE_MANIKINS[i-1]);
            }

            manikin.setOnClickListener(v -> {
                for (int j=1; j<=9; j++) {
                    ImageView otherManikin = (ImageView) getChildAt(j);
                    otherManikin.setAlpha(0.25f);
                }

                this.selectedValue = value;
                v.setAlpha(1);

                if (this.callback != null) {
                    this.callback.apply(value);
                }
            });

            params.width = (int)Math.floor(size * (100 / 107.0));
            params.height = size;

            manikin.setLayoutParams(params);
            manikin.requestLayout();
        }
    }

    public void onValueSelected(CallbackFunction callback) {
        this.callback = callback;
    }

    public int getSelectedValue() {
        return selectedValue;
    }

    private ManikinType getManikinType(int value) {
        if (value == 1) {
            return ManikinType.VALENCE;
        }

        return ManikinType.AROUSAL;
    }
}
