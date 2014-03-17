package tw.instartit.app.autowificgu;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * 連線相關函式
 * Permission Required.
 * @author NAGI
 *
 */
public class ConnectUtils {	
	
	protected static int TIMEOUT = 30000;	//Timeout 30 sec.
	protected Context context = null;
	
	private static String TAG = ConnectUtils.class.getSimpleName();
	
	public ConnectUtils(Context context) {
		this.context = context;
	}
	
	public ConnectUtils() {
	}

	public static String METHOD_GET = "GET";
	public static String METHOD_POST = "POST";
	public static String METHOD_PUT = "PUT";
	
	/**
	 * 檢查網路是否有連線能力
	 */
	public static boolean isInternetReady() {
		return isInternetReady("http://www.google.com/");
	}
	
	/**
	 * 檢查網路是否有連線能力
	 * @param context
	 */
	public static boolean isInternetReady(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null && info.isConnectedOrConnecting()) {
	        return true;
	    }
		return false;
	}
	
	/**
	 * 檢查網路是否有連線能力
	 * @param target 連線網址
	 */
	public static boolean isInternetReady(String target) {
		try {
			HttpURLConnection urlConnection = null;
			URL url = new URL(target);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod(METHOD_GET);
			urlConnection.setConnectTimeout(TIMEOUT); // 連線遇時時間：5秒
			urlConnection.connect();
			if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	
	
	/**
	 * 取得網路資源
	 * @param url 網址
	 * @return 資源字串
	 * @throws IOException
	 */
	public static String getResourceFromUrl(String url, String method) throws IOException {
		String rtnStr = "";
		HttpURLConnection urlConnection = null;
		try{
		
			URL target = new URL(url);
			
			if(target.getProtocol().toLowerCase().equals("https")) {
				
				trustAllHosts();
				HttpsURLConnection https = (HttpsURLConnection) target.openConnection();
				https.setHostnameVerifier(DO_NOT_VERIFY);
				urlConnection = https;
			} else {
				
				urlConnection = (HttpURLConnection) target
						.openConnection();
			}
			
			urlConnection.setRequestMethod(method);
			urlConnection.setConnectTimeout(TIMEOUT);
			urlConnection.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream(), "utf-8"));
			rtnStr = reader.readLine();
			reader.close();
		} finally {
			if(urlConnection != null)
				urlConnection.disconnect();
		}
		return rtnStr;
	}
	public static String getResourceFromUrlUsedHttpClient(String url, String method) throws IOException {
		HttpClient httpclient = new ConnectUtils().getNewHttpClient();
		HttpRequestBase httpmethod = null;
		if(METHOD_GET.equals(method)) {
			httpmethod = new HttpGet(url);
		}else if(METHOD_POST.equals(method)) {
			httpmethod = new HttpPost(url);
		}
		String result = null;
		
		try
        {
			
            HttpResponse response = httpclient.execute(httpmethod);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            response.getEntity().writeTo(byteArrayOutputStream);
            
            
            result = byteArrayOutputStream.toString();
        } catch(Exception e) {
        	e.printStackTrace();
        
        }
		
		
		return result;
	}
	
	public static boolean isNeedAuth() throws ClientProtocolException, IOException {
		
		HttpClient httpclient = new ConnectUtils().getNewHttpClient();
		HttpGet httpget = new HttpGet("http://www.bing.com");
		
		HttpResponse hr = httpclient.execute(httpget);
		Log.d("ConnectUtils","[*] ResponseCode:"+hr.getStatusLine().getStatusCode());
		if(hr.getStatusLine().getStatusCode() == 302) {
			return true;
		}
		return false;
	}
	
	public static void doLogout() throws ClientProtocolException, IOException {
		HttpClient httpclient = new ConnectUtils().getNewHttpClient();
		HttpPost httpget = new HttpPost("https://cgu.edu.tw/cgi-bin/login?cmd=logout");
		httpclient.execute(httpget);
	}
	
	public static void doAuthEmail(String url, String mail) throws ClientProtocolException, IOException {
		HttpClient httpclient = new ConnectUtils().getNewHttpClient();
		HttpPost httppost = new HttpPost(url);
		Log.d("Connection", "[*] connect to:"+url);
		List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("email", mail));
		data.add(new BasicNameValuePair("cmd", "authenticate"));
		data.add(new BasicNameValuePair("Login", "Log+In"));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(data, "UTF-8");
		httppost.setEntity(entity);
		httpclient.execute(httppost);
	}
	
	public static void doAuthUser(String url, String user, String pass) throws ClientProtocolException, IOException {
		HttpClient httpclient = new ConnectUtils().getNewHttpClient();
		HttpPost httppost = new HttpPost(url);
		Log.d("Connection", "[*] connect to:"+url);
		List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("user", user));
		data.add(new BasicNameValuePair("password", pass));
		data.add(new BasicNameValuePair("cmd", "authenticate"));
		data.add(new BasicNameValuePair("Login", "Log+In"));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(data, "UTF-8");
		httppost.setEntity(entity);
		httpclient.execute(httppost);
	}
	
	public HttpClient getNewHttpClient() {
	    try {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
	        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
	        HttpClientParams.setRedirecting(params, false);
	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 443));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	        return new DefaultHttpClient(ccm, params);
	    } catch (Exception e) {
	        return new DefaultHttpClient();
	    }
	}
	class MySSLSocketFactory extends SSLSocketFactory {
		public MySSLSocketFactory(KeyStore truststore)
				throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);
			TrustManager tm = new X509TrustManager() {

				@Override
				public void checkClientTrusted(X509Certificate[] arg0,
						String arg1) throws CertificateException {				
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				
			};
			sslContext.init(null, new TrustManager[] { tm }, null);
		}
		
		@Override
	    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
	        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	    }

	    @Override
	    public Socket createSocket() throws IOException {
	        return sslContext.getSocketFactory().createSocket();
	    }

		SSLContext sslContext = SSLContext.getInstance("TLS");
	}
	
	
	
	
	
	
	
	
	
	public static String post(String url, String data, String token) {
		HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url+"?token="+token);
        String result = null;
        try
        {
        	trustAllHosts();
        	
        	
        	if(data != null) {
        	   StringEntity entity = new StringEntity(data);
        	   httppost.setEntity(entity);
        	}
        	
            HttpResponse response = httpclient.execute(httppost);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            response.getEntity().writeTo(byteArrayOutputStream);

            
            result = byteArrayOutputStream.toString();
        } catch(Exception e) {
        	e.printStackTrace();
        
        }
		return result;
	}
	// always verify the host - dont check for certificate
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	/**
	 * Trust every server - dont check for any certificate
	 */
	public static void trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getIpAddr(long ip) {
		if( ip != 0 )
            return String.format( "%d.%d.%d.%d",
                   (ip & 0xff),
                   (ip >> 8 & 0xff),
                   (ip >> 16 & 0xff),
                   (ip >> 24 & 0xff));
		else return "0.0.0.0";
	}
}
