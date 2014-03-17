package tw.instartit.app.autowificgu;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.http.client.ClientProtocolException;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class AuthService extends IntentService {

	public static final String ACTION_LOGIN_MAIL = "tw.instartit.app.autowificgu.LOGIN_MAIL";
	public static final String ACTION_LOGIN_USER = "tw.instartit.app.autowificgu.LOGIN_USER";
	public static final String ACTION_LOGOUT = "tw.instartit.app.autowificgu.LOGOUT";
	public static final String ACTION_LOGIN_SUCCESS = "tw.instartit.app.autowificgu.LOGIN_SUCCESS";
	public static final String ACTION_LOGIN_FAIL = "tw.instartit.app.autowificgu.LOGIN_FAIL";
	
	
	private static final String PREFIX_MAIL = "mail";
	private static final String PREFIX_USER = "user";
	private static final String PREFIX_PASS = "pass";
	public  static final String PREFIX_ERROR = "error";
	public  static final String TIMEOUT_EXP = "timeout";
	private static final String BASIC_URL = "https://cgu.edu.tw/cgi-bin/login";
	private static final String TAG = "AuthService";
	public AuthService(String name) {
		super(name);
	}
	public AuthService() {
		super(TAG);
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "[+] Receive action:" + intent.getAction());
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Intent result = new Intent();
		try {
			
			if(intent.hasExtra(AutoLoginActivity.PREFIX_ACTION)) {
				result.putExtra(AutoLoginActivity.PREFIX_ACTION, intent.getStringExtra(AutoLoginActivity.PREFIX_ACTION));
				if(AutoLoginActivity.LOGIN_MANUAL.equals(intent.getStringExtra(AutoLoginActivity.PREFIX_ACTION))) {
					result.putExtra(PREFIX_ERROR, "手動登入");
				}
				
			}
			if(!intent.getAction().equals(ACTION_LOGOUT) && !ConnectUtils.isNeedAuth()){
				Log.i("AuthService", "[-] There is no need to auth login.");		
				sendBroadcast(result.setAction(ACTION_LOGIN_FAIL).putExtra(PREFIX_ERROR, "no_need"));
				return;
			}
			if (ACTION_LOGIN_MAIL.equals(intent.getAction())) {
				Log.i("AuthService", "[+] Login with email");
				ConnectUtils
						.doAuthEmail(
								urlCombine(
										BASIC_URL,
										intent.getStringExtra(AutoLoginActivity.PREFIX_MACADDR),
										intent.getStringExtra(AutoLoginActivity.PREFIX_IP),
										AutoLoginActivity.DEFAULT_SSID),
								sp.getString(PREFIX_MAIL, "default@login"));
				if (!ConnectUtils.isNeedAuth()) {
					sendBroadcast(result.setAction(ACTION_LOGIN_SUCCESS));
				} else {
					sendBroadcast(result.setAction(ACTION_LOGIN_FAIL));
				}
			} else if (ACTION_LOGIN_USER.equals(intent.getAction())) {
				Log.i("AuthService", "[+] Login with user/pass.");
				ConnectUtils
						.doAuthUser(
								urlCombine(
										BASIC_URL,
										intent.getStringExtra(AutoLoginActivity.PREFIX_MACADDR),
										intent.getStringExtra(AutoLoginActivity.PREFIX_IP),
										AutoLoginActivity.DEFAULT_SSID), sp
										.getString(PREFIX_USER, ""), sp
										.getString(PREFIX_PASS, ""));
				if (!ConnectUtils.isNeedAuth()) {
					sendBroadcast(result.setAction(ACTION_LOGIN_SUCCESS));
				} else {
					sendBroadcast(result.setAction(ACTION_LOGIN_FAIL));
				}
			} else if(ACTION_LOGOUT.equals(intent.getAction())) {
				Log.i("AuthService","[-] Logout wifi.");
				ConnectUtils.doLogout();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if(e instanceof SocketTimeoutException) {
				sendBroadcast(result.setAction(ACTION_LOGIN_FAIL).putExtra(PREFIX_ERROR, TIMEOUT_EXP));
			}
			e.printStackTrace();
		}	
		
	}
	public String urlCombine(String base, String mac, String ip, String ssid) {
		base += "?mac="+mac;
		base += "&ip="+ip;
		base += "&essid="+ssid;
		return base;
	}

}
