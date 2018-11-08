package com.purplish.wirtualnatablica;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;

public class PrefActivity extends PreferenceActivity {
	
	private Activity activity;
	
    private CheckBoxPreference autoload_checkbox;
    private CheckBoxPreference accelerometer_checkbox;
    private SharedPreferences preferences;
	
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		activity = this;
		
        autoload_checkbox = (CheckBoxPreference) findPreference("autoload");
        accelerometer_checkbox = (CheckBoxPreference) findPreference("accelerometer");
		
        preferences = activity.getPreferences(0);
        getPreferences();
	}
	
    @Override
    public void onPause() {
    	super.onPause();
    	setPreferences();
    	this.finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    	activity = null;
        autoload_checkbox = null;
        accelerometer_checkbox = null;
        preferences = null;
    }

    private void getPreferences() {
    	autoload_checkbox.setChecked(preferences.getBoolean("autoload", false));
    	accelerometer_checkbox.setChecked(preferences.getBoolean("accelerometer", false));
    }
    
    private void setPreferences() {
    	SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("autoload", autoload_checkbox.isChecked());
        editor.putBoolean("accelerometer", accelerometer_checkbox.isChecked());
        editor.commit();
    }


}
