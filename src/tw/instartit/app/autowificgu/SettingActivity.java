package tw.instartit.app.autowificgu;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class SettingActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT < 11)
        	addPreferencesFromResource(R.xml.setting);
        else
        	addPreferencesFromResource(R.xml.setting_11);
        
        initSummary(getPreferenceScreen());
        
	}
	
	public void initSummary(PreferenceGroup pfs) {
        Preference pf = null;
        for(int i = 0; i < pfs.getPreferenceCount(); i++) {
        	pf = pfs.getPreference(i);
        	if(pf instanceof EditTextPreference) {
        		if("pass".equals(pf.getKey()))
        			continue;
        		else
        			pf.setSummary(((EditTextPreference) pf).getText());
        	} else if(pf instanceof ListPreference) {
        		pf.setSummary(((ListPreference) pf).getEntry());
        	} else if(pf instanceof PreferenceScreen) {
        		initSummary((PreferenceScreen)pf);
        	} else if(pf instanceof CheckBoxPreference) {
        		//pf.setSummary((((CheckBoxPreference) pf).isChecked())?"開啟":"關閉");
        	} else if(pf instanceof PreferenceCategory) {
        		initSummary((PreferenceCategory)pf);
        	}
        }
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if("logout".equals(preference.getKey())) {
			startService(new Intent(AuthService.ACTION_LOGOUT));
			Toast.makeText(this, "登出網路", Toast.LENGTH_SHORT).show();
		} else if("readme".equals(preference.getKey())){
			startActivity(new Intent(this, ReadmeActivity.class));
		} else if("login".equals(preference.getKey())){
			sendBroadcast(new Intent(AutoLoginActivity.LOGIN_MANUAL));
			Toast.makeText(this, "正在登入網路...", Toast.LENGTH_SHORT).show();
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Preference pf = findPreference(key);
		if("autologin_enable".equals(key)) {
			//pf.setSummary(sharedPreferences.getBoolean(key, false)?"開啟":"關閉");
		} else if("method".equals(key)) {
			if(sharedPreferences.getString(key, "").equals("userpass"))
				pf.setSummary("使用帳密登入");
			else if(sharedPreferences.getString(key, "").equals("mailauth"))
				pf.setSummary("使用訪客登入");
		} else if("pass".equals(key)) {
			return;
		} else if("toast_enable".equals(key)) {
			//pf.setSummary(sharedPreferences.getBoolean(key, false)?"開啟":"關閉");
		} else{
			pf.setSummary(sharedPreferences.getString(key, ""));
		}
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    // Set up a listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    // Unregister the listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}
	
}
