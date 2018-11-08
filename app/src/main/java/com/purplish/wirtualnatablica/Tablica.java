package com.purplish.wirtualnatablica;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;

public class Tablica extends AsyncTask<Void, String, Void> {
	
    private Activity activity;
    private boolean isError = false;
    private Exception exception = null;
    private ViewPagerAdapter adapter = null;
    private MenuItem refreshItem = null;
    private boolean running = false;
    
    public boolean isRunning() {
		return running;
	}

	private List<String> contentArray = new ArrayList<String>();
	
    public Tablica(Activity activity, ViewPagerAdapter adapter, MenuItem refresh) {
    	this.refreshItem = refresh;
    	this.adapter = adapter;
    	this.activity = activity;
    }
 
    @Override
    protected void onPreExecute() {
    	super.onPreExecute();
    	running = true;
    	refreshItem.setActionView(R.layout.progress);
    	refreshItem.expandActionView();
    }

    @Override
    protected Void doInBackground(Void... voids) {
    	String phone = null;
    	final int api = Build.VERSION.SDK_INT;

    	try {
			phone = URLEncoder.encode(Build.MODEL, "UTF-8");
		} catch (UnsupportedEncodingException e) { }
    	
    	downloadContent("http://www.purplish.cba.pl/receiver/core.php?action=log&app=wirtualnatablica&type=android&phone=" + phone + "&api=" + api, false);
		downloadContent("http://www.1lo.swidnik.pl/tablica/data_numerek.php", true);
		downloadContent("http://www.1lo.swidnik.pl/tablica/response2.php?okno=22", true);
		downloadContent("http://www.1lo.swidnik.pl/tablica/response2.php?okno=20", true);
		
		if (!isError) {
			saveContent();
		} else {
			displayErrors();
		}

		return null;
    }
    
    @Override
    protected void onProgressUpdate(String... values) {
    	super.onProgressUpdate(values);
        Toast.makeText(this.activity, values[0], Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onPostExecute(Void result) {
    	super.onPostExecute(result);
    	
    	if (!isError) {
    		Toast.makeText(this.activity, "Tablica zaktualizowana!", Toast.LENGTH_SHORT).show();
    	} else {
    		Toast.makeText(this.activity, "Tablica nie zosta�a zapisana...", Toast.LENGTH_LONG).show();
    	}
    	
    	// refresh indicator off
    	refreshItem.collapseActionView();
    	refreshItem.setActionView(null);
    	
		try {
			adapter.notifyDataSetChanged();
		} catch (Exception e) { }
    	
    	Cleanup();
    	System.gc();
    }
    
    private void Cleanup() {
        adapter = null;
        refreshItem = null;
    	activity = null;
    	exception = null;
    	running = false;
    }

	private void saveContentToFile(String file, String data) {
		FileOutputStream fos = null;
		try {
			fos = activity.openFileOutput(file, Context.MODE_PRIVATE);
			fos.write(data.getBytes());
		} catch (Exception e) {
			// exception
		} finally {
			try {
				fos.close();
			} catch (IOException e) { }
			fos = null;
		}
	}

    @SuppressLint("SimpleDateFormat")
	private void saveContent() {
		StringBuilder newsCode = new StringBuilder();
		StringBuilder replacementsCode = new StringBuilder();
		StringBuilder othersCode = new StringBuilder();
	
		final String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()); 
		final int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		
		// start tag
		newsCode.append("<p align=\"center\">Tablica z dnia: " + formattedDate + "</p>");
		
		// header
		newsCode.append("<p>" + contentArray.get(0) + "</p>");
		
		String content;
		for(int i = 1; i < contentArray.size(); i++)
		{
			content = "<p>" + contentArray.get(i) + "</p>";
			if(content.contains("Zastępstwa"))
				replacementsCode.append(content);
			else if(content.contains("Aktualności"))
				newsCode.append(content);
			else
				othersCode.append(content);
		}
		
		if(replacementsCode.length() == 0)
			replacementsCode.append("<br><font size=\"4\" color=\"#e50000\"><p align=\"center\">Brak zastępstw</p></font>");
		
		if(othersCode.length() == 0)
			othersCode.append("<br><font size=\"4\" color=\"#e50000\"><p align=\"center\">Brak innych wiadomości</p></font>");
		
		final String nCode = newsCode.toString();
		final String rCode = replacementsCode.toString();
		final String oCode = othersCode.toString();
		
		Container.fragment1 = nCode;
		Container.fragment2 = rCode;
		Container.fragment3 = oCode;
		
		saveContentToFile("newsCode.txt", nCode.toString());
		saveContentToFile("replacementsCode.txt", rCode);
		saveContentToFile("othersCode.txt", oCode);
		saveContentToFile("refreshTime.txt", String.valueOf(currentDay));
		
    	contentArray = null;
    }
    
	private void downloadContent(String website, boolean add) {
		HttpURLConnection urlConnection = null;
		BufferedReader in = null;
		StringBuilder str = new StringBuilder();
		for(;;) {
			try {
				urlConnection = (HttpURLConnection) new URL(website).openConnection();
				urlConnection.setUseCaches(false);
				urlConnection.setConnectTimeout(1500);
				urlConnection.setReadTimeout(5000);

				in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
				
				String line, content;

				// read stream
				while((line = in.readLine()) != null) {
					str.append(line);
				}
				
				if (in != null)
					in.close();
				
				if (urlConnection != null)
					urlConnection.disconnect();
				
				content = str.toString();
				
				//android.util.Log.i("Wirtualna Tablica v2", "Content: " + content);
				
				if (content.length() < 0 || !add)
					break;
				
			    if (contentArray.contains(content)) {
			    	break;
			    } else {
			    	contentArray.add(content);
			    }
			} catch (Exception e) {
				if(!add)
					break;
				
				//android.util.Log.i("Wirtualna Tablica v2", "Exception " + e.toString());
				isError = true;
				exception = e;
				break;
			} finally {
				str.delete(0, str.length());
			}
		}
	}
	
	private void displayErrors()
	{
		publishProgress(exception.toString());
		Container.fragment1 = String.format("<p align=\"center\">Wystąpił wyjątek:<br><font size=\"3\" color=\"#e50000\">%s</font></p>", exception);
	}

}
