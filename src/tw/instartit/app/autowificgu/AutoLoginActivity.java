package tw.instartit.app.autowificgu;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class AutoLoginActivity extends BroadcastReceiver{

	private static final String STATE_CHANGE = "android.net.wifi.STATE_CHANGE";
	public  static final String LOGIN_MANUAL = "tw.instartit.app.autowificgu.ACTION_MANUAL_LOGIN";
	public  static final String DEFAULT_SSID = "cgu-wlan";
	private static final String PREFIX_ENABLE = "autologin_enable";
	private static final String PREFIX_METHOD = "method";
	public  static final String PREFIX_ACTION = "preaction";
	public  static final String PREFIX_MACADDR= "mac_addr";
	public  static final String PREFIX_IP     = "ip_addr";

	
	private static final String TAG = AutoLoginActivity.class.getSimpleName();
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "[*] Receive [" + intent.getAction() + "] action.");
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		if(!sp.getBoolean(PREFIX_ENABLE, false) && !LOGIN_MANUAL.equals(intent.getAction()) && !intent.hasExtra(AuthService.PREFIX_ERROR)) {
			Log.d(TAG, "[-] Service Close. has key[autologin_enable]:"+sp.getBoolean(PREFIX_ENABLE, false));
			return;
			
		}
		
		if(STATE_CHANGE.equals(intent.getAction()) || LOGIN_MANUAL.equals(intent.getAction())) {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if(ni == null || !ni.isConnectedOrConnecting()) {
				Log.d(TAG, "[-] No NetworkInfo.");
				if(LOGIN_MANUAL.equals(intent.getAction()))
					Toast.makeText(context, "暫無網路可供登入", Toast.LENGTH_SHORT).show();
				return;
			}
			if(ni.getType() == ConnectivityManager.TYPE_WIFI) {
				WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				WifiInfo wi = wm.getConnectionInfo();
				if(wi.getSSID() != null && wi.getSSID().contains(DEFAULT_SSID)) {				
					if("mailauth".equals(sp.getString(PREFIX_METHOD, "mailauth"))) {
						Log.d(TAG, "[+] Authentication with E-Mail");
						context.startService(new Intent(AuthService.ACTION_LOGIN_MAIL)
													.putExtra(PREFIX_ACTION, intent.getAction())
													.putExtra(PREFIX_MACADDR, wi.getMacAddress())
													.putExtra(PREFIX_IP, ConnectUtils.getIpAddr(wi.getIpAddress())));
					} else if("userpass".equals(sp.getString(PREFIX_METHOD,"mailauth"))) {
						Log.d(TAG, "[+] Authentication with User / Pass");
						context.startService(new Intent(AuthService.ACTION_LOGIN_USER)
													.putExtra(PREFIX_ACTION, intent.getAction())
													.putExtra(PREFIX_MACADDR, wi.getMacAddress())
													.putExtra(PREFIX_IP, ConnectUtils.getIpAddr(wi.getIpAddress())));
					} else {
							Log.d(TAG, "[-] Not yet select authenticate method.");
					}

					
				} else {
					Log.i(TAG, "[-] Not CGU's Wifi. SSID:" + wi.getSSID().trim());
					if(LOGIN_MANUAL.equals(intent.getAction()))
						Toast.makeText(context, "非學校網路:"+wi.getSSID(), Toast.LENGTH_SHORT).show();
					return;
				}
				
				
			} else {
				Log.i(TAG, "[-] Not Wifi Connection. Type:" + ni.getType());
				return;
			}
		} else if(AuthService.ACTION_LOGIN_SUCCESS.equals(intent.getAction())) {
			Log.i(TAG, "[+] Login Successful.");
			if(LOGIN_MANUAL.equals(intent.getStringExtra(PREFIX_ACTION)) || sp.getBoolean("toast_enable", true))
				doNotification(context, "自動登入(長庚)", "登入網路成功");

		} else if(AuthService.ACTION_LOGIN_FAIL.equals(intent.getAction())) {
			if(intent.hasExtra(AuthService.PREFIX_ERROR)) {
				Log.e(TAG, "[-] Has Error:"+intent.getStringExtra(AuthService.PREFIX_ERROR));
				if(AuthService.TIMEOUT_EXP.equals(intent.getStringExtra(AuthService.PREFIX_ERROR)))
					if(LOGIN_MANUAL.equals(intent.getStringExtra(PREFIX_ACTION)) || sp.getBoolean("toast_enable", true))
						doNotification(context, "自動登入(長庚)", "登入逾時，請重新登入");
				else
					if(LOGIN_MANUAL.equals(intent.getStringExtra(PREFIX_ACTION)) || sp.getBoolean("toast_enable", true))
						doNotification(context, "自動登入(長庚)", "已經登入網路");	
			} else {
				Log.i(TAG, "[-] Login Access Deny.");
				if(LOGIN_MANUAL.equals(intent.getStringExtra(PREFIX_ACTION)) || sp.getBoolean("toast_enable", true))
					doNotification(context, "自動登入(長庚)", "登入網路失敗");
			}
		}
	}
	
	private void doNotification(Context context, String title, String content) {
//		Log.d(TAG, "[+] Notification");
//		Notification notiication = new Notification();
//		notiication.tickerText=content;
//		notiication.defaults = Notification.DEFAULT_ALL;
//		notiication.setLatestEventInfo(context, title, content, null);
//		NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
//		nm.notify(2, notiication);
		Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
	}
	
}
