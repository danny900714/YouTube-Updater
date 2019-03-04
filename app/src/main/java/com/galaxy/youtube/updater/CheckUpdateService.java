package com.galaxy.youtube.updater;
import android.app.Service;
import android.content.*;
import android.os.*;
import com.galaxy.lib.update.*;
import android.support.v4.app.*;
import android.app.PendingIntent;
import android.app.NotificationManager;

public class CheckUpdateService extends Service
{
	private static final int VANCED_NOTIFICATION_ID = 8000;
	private static final int MICROG_NOTIFICATION_ID = 8001;
	private static final int UPDATER_NOTIFICATION_ID = 8002;
	
	private CheckUpdateManager vancedUpdateManager, microGUpdateManager, updaterUpdateManager;
	private NotificationManager notificationManager;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		checkVancedUpdate();
		checkMicroGUpdate();
		checkUpdaterUpdate();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent p1) {
		// TODO: Implement this method
		return null;
	}
	
	private void checkVancedUpdate() {
		vancedUpdateManager = new CheckUpdateManager(this);
		vancedUpdateManager.setPackageName(Constants.VANCED_PACKAGE_NAME);
		vancedUpdateManager.setUrlVersion(Constants.VANCED_VERSION_URL);
		vancedUpdateManager.setPathVersion(Constants.VANCED_VERSION_PATH);
		vancedUpdateManager.setPathUpdate(Constants.VANCED_UPDATE_PATH);
		vancedUpdateManager.setOnCompleteListener(onVancedDownloadComplete);
		vancedUpdateManager.setConnectivityToastEnabled(false);
		vancedUpdateManager.setProgressDialogEnabled(false);
		vancedUpdateManager.checkUpdate();
	}
	
	private void checkMicroGUpdate() {
		microGUpdateManager = new CheckUpdateManager(this);
		microGUpdateManager.setPackageName(Constants.MICROG_PACKAGE_NAME);
		microGUpdateManager.setUrlVersion(Constants.MICROG_VERSION_URL);
		microGUpdateManager.setPathVersion(Constants.MICROG_VERSION_PATH);
		microGUpdateManager.setPathUpdate(Constants.MICROG_UPDATE_PATH);
		microGUpdateManager.setOnCompleteListener(onMicroGDownloadComplete);
		microGUpdateManager.setConnectivityToastEnabled(false);
		microGUpdateManager.setProgressDialogEnabled(false);
		microGUpdateManager.checkUpdate();
	}
	
	private void checkUpdaterUpdate() {
		updaterUpdateManager = new CheckUpdateManager(this);
		updaterUpdateManager.setPackageName(Constants.UPDATER_PACKAGE_NAME);
		updaterUpdateManager.setUrlVersion(Constants.UPDATER_VERSION_URL);
		updaterUpdateManager.setPathVersion(Constants.UPDATER_VERSION_PATH);
		updaterUpdateManager.setPathUpdate(Constants.UPDATER_UPDATE_PATH);
		updaterUpdateManager.setOnCompleteListener(onUpdaterDownloadComplete);
		updaterUpdateManager.setConnectivityToastEnabled(false);
		updaterUpdateManager.setProgressDialogEnabled(false);
		updaterUpdateManager.checkUpdate();
	}

	private void showNotification(String appName, String newVersionName, int id) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
			.setSmallIcon(R.mipmap.ic_launcher)
			.setContentTitle(String.format(getString(R.string.update_notification_title), appName))
			.setContentText(String.format(getString(R.string.update_notification_text), appName, newVersionName))
			.setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(getString(R.string.update_notification_text), appName, newVersionName)));

		Intent resultIntent = new Intent(this, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);

		notificationManager.notify(id, builder.build());
	}

	private CheckUpdateManager.OnCompleteListener onVancedDownloadComplete = new CheckUpdateManager.OnCompleteListener() {
		@Override
		public void onComplete(int versionCode, String versionName, String url, String message) {
			if (!vancedUpdateManager.isLatestVersion(versionCode))
				showNotification(getString(R.string.youtube_vanced), versionName, VANCED_NOTIFICATION_ID);
			else
				notificationManager.cancel(VANCED_NOTIFICATION_ID);
		}
	};
	
	private CheckUpdateManager.OnCompleteListener onMicroGDownloadComplete = new CheckUpdateManager.OnCompleteListener() {
		@Override
		public void onComplete(int versionCode, String versionName, String url, String message) {
			if (!microGUpdateManager.isLatestVersion(versionCode))
				showNotification(getString(R.string.microg), versionName, MICROG_NOTIFICATION_ID);
			else
				notificationManager.cancel(MICROG_NOTIFICATION_ID);
		}
	};
	
	private CheckUpdateManager.OnCompleteListener onUpdaterDownloadComplete = new CheckUpdateManager.OnCompleteListener() {
		@Override
		public void onComplete(int versionCode, String versionName, String url, String message) {
			if (!updaterUpdateManager.isLatestVersion(versionCode))
				showNotification(getString(R.string.app_name), versionName, UPDATER_NOTIFICATION_ID);
			else
				notificationManager.cancel(UPDATER_NOTIFICATION_ID);
		}
	};
}
