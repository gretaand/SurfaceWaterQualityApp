package com.github.gretaand.surfacewaterqualityapp.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.gretaand.surfacewaterqualityapp.R;
import com.github.gretaand.surfacewaterqualityapp.utils.Result;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Custom ArrayAdapter for ListView that displays results
 *
 * @author greta
 */
public class ResultAdapter extends ArrayAdapter<Result> {

    public ResultAdapter(Context context, ArrayList<Result> results) {
        /* Initialize the ArrayAdapter's internal storage for the context and the list.
         * the second argument is used when the ArrayAdapter is populating a single TextView.
         * Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
         * going to use this second argument, so it can be any value. Here, we used 0. */
        super(context, 0, results);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        // Get the data for this position
        Result currentResult = getItem(position);

        // Check if existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_results, parent,
                    false);
        }

        if (currentResult != null) {

            // Find the TextView in list_results.xml the warning level color
            TextView warningLevelTextView = listItemView.findViewById(R.id.warningLevel);
            /* Set the proper background color on the limit circle.
             * Fetch the background from the TextView, which is a GradientDrawable. */
            GradientDrawable limitCircle = (GradientDrawable) warningLevelTextView.getBackground();
            // Determine the warning level and set the color
            int warningLevel = currentResult.getWarningLevel();
            limitCircle.setColor(currentResult.getWarningColor(warningLevel));
            warningLevelTextView.setText(currentResult.getWarningText(warningLevel));

            // Find the TextView in list_results.xml the id activityStartDate and set the text
            Date date = currentResult.getActivityStartDate();
            SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
            String stringDate = formatter.format(date);
            TextView activityStartDateTextView = listItemView.findViewById(R.id.activityStartDate);
            activityStartDateTextView.setText(stringDate);

            // Find the TextView in list_results.xml the id characteristicName and set the text
            TextView characteristicNameTextView = listItemView.findViewById(
                    R.id.characteristicName);
            characteristicNameTextView.setText(currentResult.getCharacteristicName());

            // Find the TextView in list_results.xml the id mediaType and set the text
            TextView mediaTypeTextView = listItemView.findViewById(
                    R.id.mediaType);
            mediaTypeTextView.setText(currentResult.getActivityMediaName());

            // Find the TextView in list_results.xml the id resultMeasureValue
            // and set the text based on what data is available
            TextView measureValueTextView = listItemView.findViewById(
                    R.id.resultMeasureValue);
            TextView measureUnitCodeTextView = listItemView.findViewById(R.id.resultMeasureUnitCode);
            String measureValueString = currentResult.getMeasureValueString();
            String measureUnitCode = currentResult.getMeasureUnitCode();
            double convertedMeasureValue = currentResult.getConvertedMeasureValue();
            String convertedMeasureUnitCode = currentResult.getConvertedMeasureUnitCode();
            if (convertedMeasureUnitCode != null) {
                measureValueTextView.setText(String.format(Locale.US, "%f",
                        convertedMeasureValue));
                measureUnitCodeTextView.setText(convertedMeasureUnitCode);
            } else if (measureValueString != null && measureUnitCode != null) {
                measureValueTextView.setText(measureValueString);
                measureUnitCodeTextView.setText(measureUnitCode);
            } else if (measureValueString != null) {
                measureValueTextView.setText(measureValueString);
                measureUnitCodeTextView.setText(R.string.none_reported);
            } else {
                measureValueTextView.setText(currentResult.getDetectionCondition());
                measureUnitCodeTextView.setText(R.string.none_reported);
            }
        }

      /* Return the whole list item layout (containing 4 TextViews)
       * so that it can be shown in the ListView */
      return listItemView;
    }


}
