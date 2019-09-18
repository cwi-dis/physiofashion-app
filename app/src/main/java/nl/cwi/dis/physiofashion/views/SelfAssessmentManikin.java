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

/**
 * This class represents a custom view through which the user can either rate their arousal or
 * valence using so-called self-assessment Manikins. The view can specify Manikin type (arousal or
 * valence), a value which is selected by default, a title for the view and a size for the rendered
 * manikins. This class should not be instantiated directly and is usually instantiated by the view
 * system from XML.
 */
public class SelfAssessmentManikin extends ConstraintLayout {
    /**
     * Defines legal Manikin types
     */
    private enum ManikinType {
        AROUSAL, VALENCE
    }

    /**
     * Callback function for `onValueSelected`. Receives the selected value as argument
     */
    @FunctionalInterface
    public interface CallbackFunction {
        void apply(int value);
    }

    // Resource names for VALENCE Manikins. AROUSAL Manikins are used by default
    private static final int[] VALENCE_MANIKINS = new int[] {
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

    /**
     * Initialise new view without attributes.
     *
     * @param context Application context
     */
    public SelfAssessmentManikin(Context context) {
        super(context, null);
    }

    /**
     * Initialise new view with the given attributes.
     *
     * @param context Application context
     * @param attributeSet View attributes
     */
    public SelfAssessmentManikin(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        // Inflate the layout defined in the XML file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_self_assessment_manikin, this, true);

        // Obtain an array for the view attributes
        TypedArray attrs = context.obtainStyledAttributes(attributeSet, R.styleable.SelfAssessmentManikin);

        // Extract view attributes
        String title = attrs.getString(R.styleable.SelfAssessmentManikin_title);
        int size = attrs.getDimensionPixelSize(R.styleable.SelfAssessmentManikin_size, 0);
        ManikinType type = getManikinType(attrs.getInt(R.styleable.SelfAssessmentManikin_manikinType,0));
        int defaultValue = attrs.getInt(R.styleable.SelfAssessmentManikin_defaultValue, -1);

        // Recycle attributes
        attrs.recycle();

        // Set view title label
        TextView titleView = (TextView) getChildAt(0);
        titleView.setText(title);

        // Iterate over all 9 Manikin positions
        for (int i=1; i<=9; i++) {
            final int value = i - 1;

            // Obtain Manikin image view
            ImageView manikin = (ImageView) getChildAt(i);
            ViewGroup.LayoutParams params = manikin.getLayoutParams();

            // Set alpha of selected Manikin to 1, make all others semi-transparent
            if (value == defaultValue) {
                manikin.setAlpha(1.0f);
                this.selectedValue = value;
            } else {
                manikin.setAlpha(0.25f);
            }

            // Use valence Manikins if `type` is set to VALENCE, otherwise use default AROUSAL
            if (type == ManikinType.VALENCE) {
                manikin.setImageResource(VALENCE_MANIKINS[i-1]);
            }

            // Add click listener to each Manikin image view
            manikin.setOnClickListener(v -> {
                // Make all Manikins semi-transparent
                for (int j=1; j<=9; j++) {
                    ImageView otherManikin = (ImageView) getChildAt(j);
                    otherManikin.setAlpha(0.25f);
                }

                // Set selected value to current value
                this.selectedValue = value;
                // Make clicked Manikin completely opaque
                v.setAlpha(1);

                // Call `onValueSelected` callback if available
                if (this.callback != null) {
                    this.callback.apply(value);
                }
            });

            // Adjust size of Manikin image view
            params.width = (int)Math.floor(size * (100 / 107.0));
            params.height = size;

            // Reset layout params an re-render
            manikin.setLayoutParams(params);
            manikin.requestLayout();
        }
    }

    /**
     * Invokes the passed callback once the user selects new value. The callback receives the
     * selected value as argument.
     *
     * @param callback The callback to invoke when a new value is selected
     */
    public void onValueSelected(CallbackFunction callback) {
        this.callback = callback;
    }

    /**
     * Returns the value that is currently selected. Returns -1 if no value is selected.
     *
     * @return The selected value or -1
     */
    public int getSelectedValue() {
        return selectedValue;
    }

    /**
     * Get the Manikin type associated to a number.
     *
     * @param value A number which should correspond to a Manikin type
     * @return The Manikin type associated to the given number
     */
    private ManikinType getManikinType(int value) {
        if (value == 1) {
            return ManikinType.VALENCE;
        }

        return ManikinType.AROUSAL;
    }
}
