package com.galaxy.lib.update;
import android.Manifest;
import android.content.*;
import android.support.v4.content.*;
import android.support.v4.app.*;
import android.content.pm.*;
import android.support.v7.app.AlertDialog;
import android.app.*;
import android.util.*;
import android.net.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import android.os.*;
import java.nio.channels.*;
import java.nio.*;
import com.galaxy.youtube.updater.*;
import java.nio.charset.*;

public class CheckUpdateManager {
	private Activity activity;
	private Context context;
	private OnCompleteListener mOnCompleteListener;
	private boolean isProgressBarEnabled = false;
	private boolean isConnectivityToastEnabled;
	private ProgressDialog progressDialog;
	private String sProgressMessage;
	private long downloadId;
	private DownloadType currentDownloadType;
	
	// define your version file info
	private String packageName;
	private String urlVersion;
	private String pathVersion;
	private String pathUpdate;
	private static final String FILENAME_VERSION = "version.json";
	private static final String FILENAME_UPDATE = "update.apk";
	private static final boolean IS_CHECK_UPDATE_NOTIFICATION_ENABLED = false;
	private static final boolean IS_DOWNLOAD_UPDATE_NOTIFICATION_ENABLED = true;
	private static final boolean IS_DOWNLOAD_UPDATE_PROGRESS_DIALOG_ENABLED = false;
	
	public CheckUpdateManager(Activity activity) {
		this.activity = activity;
		this.context = activity;
	}
	
	public CheckUpdateManager(Context context) {
		this.context = context;
	}
	
	public void checkUpdate() {
		// check permission
		// make it comment due to changing download place to internal storage
		if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			Log.e(CheckUpdateManager.class.getName(), Manifest.permission.WRITE_EXTERNAL_STORAGE.toString() + " permission denied");
			return;
		}
		
		// check network state
		if (!isConnected()) {
			Log.w(CheckUpdateManager.class.getName(), "Internet Disconnect");
			if (isConnectivityToastEnabled)
				Toast.makeText(context, R.string.internet_disconnect, Toast.LENGTH_LONG).show();
			return;
		}
		
		defineProgressDialog();
		
		// start download
		DownloadFileAsyncTask.DownloadPack pack = new DownloadFileAsyncTask.DownloadPack(urlVersion, pathVersion, FILENAME_VERSION);
		DownloadFileAsyncTask downloadTask = new DownloadFileAsyncTask(context, this, isProgressBarEnabled, IS_CHECK_UPDATE_NOTIFICATION_ENABLED);
		if (isProgressBarEnabled)
			downloadTask.setProgressDialog(progressDialog);
		downloadTask.execute(pack);
		
		// register download finish receiver
		context.registerReceiver(downloadCompleteListener, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		
		// set download type
		currentDownloadType = DownloadType.VERSION;
	}
	
	public void downloadUpdate(String url) {
		// check permission
		if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			Log.e(CheckUpdateManager.class.getName(), Manifest.permission.WRITE_EXTERNAL_STORAGE.toString() + " permission denied");
			return;
		}

		// check network state
		if (!isConnected()) {
			Log.w(CheckUpdateManager.class.getName(), "Internet Disconnect");
			if (isConnectivityToastEnabled)
				Toast.makeText(context, R.string.internet_disconnect, Toast.LENGTH_LONG).show();
			return;
		}
		
		// start download
		DownloadFileAsyncTask.DownloadPack pack = new DownloadFileAsyncTask.DownloadPack(url, pathUpdate, FILENAME_UPDATE);
		DownloadFileAsyncTask task = new DownloadFileAsyncTask(context, this, IS_DOWNLOAD_UPDATE_PROGRESS_DIALOG_ENABLED, IS_DOWNLOAD_UPDATE_NOTIFICATION_ENABLED);
		if (IS_DOWNLOAD_UPDATE_NOTIFICATION_ENABLED) {
			String sTitle = context.getResources().getString(R.string.app_name);
			task.setNotificationTitle(sTitle);
		}
		task.execute(pack);
		
		// register download finish receiver
		context.registerReceiver(downloadCompleteListener, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		
		// set download type
		currentDownloadType = DownloadType.APK;
	}
	
	public void close() {
		context.unregisterReceiver(downloadCompleteListener);
	}
	
	public boolean isLatestVersion(int versionCode) {
		int currentVersionCode = 0;
		try {
			currentVersionCode = context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (versionCode > currentVersionCode) {
				return false;
			} else {
				return true;
			}
		}
	}
	
	public void setPathUpdate(String pathUpdate) {
		this.pathUpdate = pathUpdate;
	}

	public void setPathVersion(String pathVersion) {
		this.pathVersion = pathVersion;
	}

	public void setUrlVersion(String urlVersion) {
		this.urlVersion = urlVersion;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	public void setConnectivityToastEnabled(boolean isEnabled) {
		isConnectivityToastEnabled = isEnabled;
	}
	
	public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
		if (onCompleteListener == null) {
			Log.e(CheckUpdateManager.class.getName(), "Cannot set OnCompleteListener null");
			return;
		}
		mOnCompleteListener = onCompleteListener;
	}
	
	public void setProgressDialogEnabled(boolean isEnabled) {
		if (activity == null) {
			Log.e(CheckUpdateManager.class.getName(), "Cannot show progress dialog in context except for activity");
			return;
		}
		isProgressBarEnabled = isEnabled;
	}
	
	public void setProgressDialogInfo(String message) {
		sProgressMessage = message;
	}
	
	public void showSureUpdateDialog(String versionName, String changeLog, final String url) {
		if (activity == null) {
			Log.w(CheckUpdateManager.class.getName(), "Cannot show dialog in non-activity class");
			return;
		}
		String sUpdateLog = context.getResources().getString(R.string.new_feature);
		String sTtile = context.getResources().getString(R.string.check_update_dialog_title);
		String sMessage = String.format(context.getResources().getString(R.string.check_update_dialog_message), versionName) + "\n\n" + sUpdateLog + "\n\n" + changeLog;
		new AlertDialog.Builder(context).setTitle(sTtile).setMessage(sMessage + "\n\n" + context.getString(R.string.uninstall_app_first)).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					// TODO: Implement this method
					downloadUpdate(url);
				}
			}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					// TODO: Implement this method
				}
			}).show();
	}
	
	protected void setDownloadId(long id) {
		downloadId = id;
	}
	
	private boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
		return isConnected;
	}
	
	private ProgressDialog defineProgressDialog() {
		progressDialog = new ProgressDialog(context);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(false);
		progressDialog.setMessage(sProgressMessage);
		return progressDialog;
	}
	
	private JSONObject getJsonObject() {
		try {
			File file = new File(Environment.getExternalStorageDirectory(), pathVersion + "/" + FILENAME_VERSION);
			FileInputStream stream = new FileInputStream(file);

			String jsonStr = null;

			try {
				FileChannel fileChannel = stream.getChannel();
				MappedByteBuffer bytebuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());

				jsonStr = Charset.defaultCharset().decode(bytebuffer).toString();
			} catch (IOException e) {
				Log.e(CheckUpdateManager.class.getName(), e.toString());
				e.printStackTrace();
			} finally {
				try {
					stream.close();
				} catch (IOException e) {
					Log.e(CheckUpdateManager.class.getName(), e.toString());
					e.printStackTrace();
				}
			}
			
			try {
				JSONObject jsonObject = new JSONObject(jsonStr);
				return jsonObject;
			} catch (JSONException e) {
				Log.e(CheckUpdateManager.class.getName(), e.toString());
				e.printStackTrace();
			}
		}
 		catch (FileNotFoundException e) {
			Log.e(CheckUpdateManager.class.getName(), e.toString());
			e.printStackTrace();
		}
		return null;
	}
	
	private BroadcastReceiver downloadCompleteListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// TODO: Implement this method
			long completedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			if (downloadId == completedId) {
				if (currentDownloadType == DownloadType.VERSION) {
					try {
						JSONObject rootObject = getJsonObject();
						int versionCode = rootObject.getInt("version_code");
						String versionName = rootObject.getString("version_name");
						String url = rootObject.getString("url");
						String message = rootObject.getString("message");
						
						if (isProgressBarEnabled)
							progressDialog.dismiss();
						
						mOnCompleteListener.onComplete(versionCode, versionName, url, message);
					} catch (JSONException e) {
						Log.e(CheckUpdateManager.class.getName(), e.toString());
						e.printStackTrace();
					}
				} else if (currentDownloadType == DownloadType.APK) {
					File file = new File(Environment.getExternalStorageDirectory(), pathUpdate + "/" + FILENAME_UPDATE);
					Intent install = new Intent(Intent.ACTION_VIEW);
					install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					install.setDataAndType(Uri.fromFile(file),  "application/vnd.android.package-archive");
					context.startActivity(install);
					context.unregisterReceiver(downloadCompleteListener);
				}
			}
		}
	};
	
	private enum DownloadType {
		VERSION, APK;
	}
	
	public interface OnCompleteListener {
		public void onComplete(int versionCode, String versionName, String url, String message);
	}
}
