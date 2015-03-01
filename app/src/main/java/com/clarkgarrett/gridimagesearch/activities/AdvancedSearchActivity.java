package com.clarkgarrett.gridimagesearch.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.clarkgarrett.gridimagesearch.R;

public class AdvancedSearchActivity extends ActionBarActivity {

    Spinner spnImageSize, spnColorFilter, spnImageType;
    EditText etSiteFilter;
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefsEditor;
    public static final String IMAGE_SIZE = "imagesize";
    public static final String COLOR_FILTER = "colorfilter";
    public static final String IMAGE_TYPE = "imagetype";
    public static final String SITE_FILTER = "sitefilter";
    public static final String PREFS_NAME = "pref_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_search);

        spnImageSize= (Spinner)findViewById(R.id.spnImageSize);
        ArrayAdapter<CharSequence> imageSizeAdapter = ArrayAdapter.createFromResource(this, R.array.image_size, R.layout.custom_spinner_layout);
        imageSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnImageSize.setAdapter(imageSizeAdapter);

        spnColorFilter= (Spinner)findViewById(R.id.spnColorFilter);
        ArrayAdapter<CharSequence> colorFilterAdapter = ArrayAdapter.createFromResource(this, R.array.image_color, R.layout.custom_spinner_layout);
        colorFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnColorFilter.setAdapter(colorFilterAdapter);

        spnImageType= (Spinner)findViewById(R.id.spnImageType);
        ArrayAdapter<CharSequence> imageTypeAdapter = ArrayAdapter.createFromResource(this, R.array.image_type, R.layout.custom_spinner_layout);
        imageTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnImageType.setAdapter(imageTypeAdapter);

        etSiteFilter = (EditText)findViewById(R.id.etSiteFilter);
        setSelectedItems();
    }

    private void setSelectedItems(){
        prefs = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        prefsEditor= prefs.edit();
        spnImageSize.setSelection(prefs.getInt(IMAGE_SIZE , 0));
        spnColorFilter.setSelection(prefs.getInt(COLOR_FILTER , 0));
        spnImageType.setSelection(prefs.getInt(IMAGE_TYPE , 0));
        etSiteFilter.setText(prefs.getString(SITE_FILTER,""));
    }

    public void onSave(View v){
		/* Save buttom was pressed. put the value from each Spinner and
		 * and the EditText in an intent extra and pass the intent back to
		 * the initiating activity (SearchActivity). Then call finish() to
		 * end the activity.
		 */
        Log.d("DEBUG", "selected position = " + spnImageSize.getSelectedItemPosition());

        prefsEditor.putInt(IMAGE_SIZE, spnImageSize.getSelectedItemPosition());
        prefsEditor.putInt(COLOR_FILTER, spnColorFilter.getSelectedItemPosition());
        prefsEditor.putInt(IMAGE_TYPE , spnImageType.getSelectedItemPosition());
        prefsEditor.putString(SITE_FILTER,etSiteFilter.getText().toString());
        prefsEditor.commit();

        Intent i = new Intent();
        String size = spnImageSize.getSelectedItem().toString();
        if (size.equals("large")){
            size="xxlarge";
        }
        i.putExtra(IMAGE_SIZE, size);
        i.putExtra(COLOR_FILTER, spnColorFilter.getSelectedItem().toString());

        String type = spnImageType.getSelectedItem().toString();
        if (type.equals("clip art")) {
            type = "clipart";
        }
        if (type.equals("line art")) {
            type = "lineart";
        }
        i.putExtra(IMAGE_TYPE, type);

        i.putExtra(SITE_FILTER, etSiteFilter.getText().toString());
        setResult(Activity.RESULT_OK, i);
        finish();
    }

}
