package com.purplish.wirtualnatablica;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.actionbarsherlock.app.SherlockFragment;

@SuppressWarnings("deprecation")
public class FragmentTab2 extends SherlockFragment {
	
	//private ZoomButtonsController zoom_control = null;
	private WebView webView;

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Get the view from fragmenttab2.xml
		View view = inflater.inflate(R.layout.fragmenttab2, container, false);
		webView = (WebView)view.findViewById(R.id.webview);
		
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setBuiltInZoomControls(true);
		
		if (Build.VERSION.SDK_INT < 14) {
			webView.getSettings().setDefaultZoom(android.webkit.WebSettings.ZoomDensity.FAR);
		} else {
			webView.getSettings().setDefaultZoom(android.webkit.WebSettings.ZoomDensity.FAR);
		}
		
		
		/*
	    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
	    	new Runnable() {
	    		@SuppressLint("NewApi")
	    		public void run() {
	    			webView.getSettings().setDisplayZoomControls(false);
	    		}
	    	}.run();
	    } else {
			try {
				zoom_control = (ZoomButtonsController) webView.getClass().getMethod("getZoomButtonsController").invoke(webView, null);
			} catch (Exception e) { 
				e.printStackTrace(); 
			}
	    }
	    */
		
		//Log.i("FRAGMENT", "Fragment2 onCreateView");
		
		
		
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		//Log.i("FRAGMENT", "Fragment1 onResume");
		webView.loadDataWithBaseURL(null, Container.fragment2, "text/html", "UTF-8", null);
	}
	
}
