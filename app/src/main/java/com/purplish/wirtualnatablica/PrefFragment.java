package com.purplish.wirtualnatablica;

import java.util.List;

import android.annotation.SuppressLint;
import android.preference.PreferenceActivity;

@SuppressLint("NewApi")
public class PrefFragment extends PreferenceActivity {

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return PrefFragment.class.getName().equals(fragmentName) ||
                PrefFragmentActivity.class.getName().equals(fragmentName);
    }

}
