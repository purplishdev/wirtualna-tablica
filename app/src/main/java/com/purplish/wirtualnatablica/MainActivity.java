package com.purplish.wirtualnatablica;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity {

	private ActionBar mActionBar = null;
	private ViewPager mPager = null;
    private ViewPagerAdapter viewpageradapter = null;
	
    private Activity activity = null;
    private SharedPreferences sharedPrefs = null;
    private SensorManager mSensorManager = null;
    private MenuItem refreshItem = null;
    private Tablica tablica = null;
    
    private float mAccel = 0.00f;
    private float mAccelCurrent = 9.80665f;
    private float mAccelLast = 9.80665f;
    
    private String[] tabs = {"Aktualności", "Zastępstwa", "Inne"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get the view from activity_main.xml
		setContentView(R.layout.activity_main);
		
		activity = this;
		
		// Activate Navigation Mode Tabs
		mActionBar = getSupportActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// Locate ViewPager in activity_main.xml
		mPager = (ViewPager) findViewById(R.id.pager);
		
		// Activate Fragment Manager
		FragmentManager fm = getSupportFragmentManager();

		// Capture ViewPager page swipes
		ViewPager.SimpleOnPageChangeListener ViewPagerListener = new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				// Find the ViewPager Position
				mActionBar.setSelectedNavigationItem(position);
			}
		};
		
		mPager.setOnPageChangeListener(ViewPagerListener);
		// Locate the adapter class called ViewPagerAdapter.java
		viewpageradapter = new ViewPagerAdapter(fm);
		// Set the View Pager Adapter into ViewPager
		mPager.setAdapter(viewpageradapter);
		
		// Capture tab button clicks
		ActionBar.TabListener tabListener = new ActionBar.TabListener() {

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				// Pass the position on tab click to ViewPager
				mPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
				// TODO Auto-generated method stub
			}
		};
		
		for (String tab : tabs)
		{
			Tab newTab = mActionBar.newTab().setText(tab).setTabListener(tabListener);
			mActionBar.addTab(newTab);
		}
		
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
	    
		if (preferenceGetValue("accelerometer") == true) {
		    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	private final SensorEventListener mSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            if (mAccel > 9) {
                 boardRefresh(true);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
         	return;
        }
    };

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		refreshItem = menu.findItem(R.id.refresh);
		
		// it's here because layout load is needed
		if (preferenceGetValue("autoload") == true) {
			if(tablica == null)
				boardRefresh(false);
			else
				boardLoad();
		}
		
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch(item.getItemId()) {
			case R.id.refresh:
				boardRefresh(true);
				return true;
				
			case R.id.action_settings:
				if (Build.VERSION.SDK_INT < 11) {
				    startActivity(new Intent(this, PrefActivity.class));
				} else {
				    startActivity(new Intent(this, PrefFragment.class));
				}
				return true;
				
			case R.id.action_exit:
				Exit();
				return true;
			
	        default:
	            return super.onOptionsItemSelected(item);
			
		}
    }
	
	private void boardRefresh(boolean isForced) {
		if(tablica != null && tablica.isRunning())
			return;
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		
		final boolean haveInternet = haveInternet(activity);
		final int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		int lastDay = -1;
		
		try {
			lastDay = Integer.parseInt((loadContentFromFile("refreshTime.txt")));
		} catch (NumberFormatException e) { }
		
		if(!haveInternet) {
			boardLoad();
		} else {
			if (lastDay != currentDay) {
				tablica = new Tablica(this, viewpageradapter, refreshItem);
				tablica.execute();
			} else if (isForced) {
				tablica = new Tablica(this, viewpageradapter, refreshItem);
				tablica.execute();
			} else
				boardLoad();
		}
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		System.gc();
	}
	
	private void boardLoad() {
		Container.fragment1 = loadContentFromFile("newsCode.txt");
		Container.fragment2 = loadContentFromFile("replacementsCode.txt");
		Container.fragment3 = loadContentFromFile("othersCode.txt");
		viewpageradapter.notifyDataSetChanged();
	}
	
	private String loadContentFromFile(String file) {
	    FileInputStream in = null;
	    InputStreamReader inputStreamReader = null;
	    BufferedReader bufferedReader = null;
	    StringBuilder str = new StringBuilder();
	    String line;
	    
		try {
			in = openFileInput(file);
		    inputStreamReader = new InputStreamReader(in);
		    bufferedReader = new BufferedReader(inputStreamReader);
			while ((line = bufferedReader.readLine()) != null) {
			    str.append(line);
			}
			in.close();
			inputStreamReader.close();
			bufferedReader.close();
		} catch (Exception e) {
			// exception, file not found!
		} finally {
			in = null;
			inputStreamReader = null;
			bufferedReader = null;
			line = null;
		}
		return str.toString();
	}

	private boolean haveInternet(Context ctx) {
	    NetworkInfo network = (NetworkInfo)((ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

	    if (network != null && network.isConnected()) {
		    	return true;
	    }
	    return false;
	}
	
	private void Cleanup() {    
		Container.fragment1 = null;
		Container.fragment2 = null;
		Container.fragment3 = null;
		sharedPrefs = null;
		mActionBar = null;
		mPager = null;
	    activity = null;
	    mSensorManager = null;
	    viewpageradapter = null;
	    refreshItem = null;
	    tablica = null;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mSensorManager != null && mSensorListener != null) {
			if (preferenceGetValue("accelerometer") == true) {
				mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
			} else {
				mSensorManager.unregisterListener(mSensorListener);
			}
		}
	}
	
	@Override
	protected void onPause() {
		if (mSensorManager != null && mSensorListener != null)
			mSensorManager.unregisterListener(mSensorListener);
		super.onPause();
	}
	
	private boolean preferenceGetValue(String value) {
		if (sharedPrefs != null) {
			return sharedPrefs.getBoolean(value, false);
		}
		return false;
	}
	
	@Override
	protected void onDestroy() {
		Cleanup();
		System.gc();
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
	    new AlertDialog.Builder(this)
	    	   .setTitle("Wirtualna Tablica")
	           .setMessage("Czy na pewno chcesz wyj��?")
	           .setCancelable(false)
	           .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   Exit();
	               }
	           })
	           .setNegativeButton("Anuluj", null)
	           .show();
	}
	
	private void Exit() {
 	   if(tablica != null && tablica.isRunning())
		   tablica.cancel(true);
	   
	   activity.finish();
	}

}
