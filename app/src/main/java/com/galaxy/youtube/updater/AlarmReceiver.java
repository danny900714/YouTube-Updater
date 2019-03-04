package com.galaxy.youtube.updater;
import android.content.*;
import com.galaxy.lib.update.*;
import android.app.*;
import android.os.*;
import android.util.*;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.*;

public class AlarmReceiver extends BroadcastReceiver {
	
	public static final String ACTION_CHECK_UPDATE = "com.galaxy.youtube.updater.CHECKUPDATE";
	private static final int NOTIFICATION_ID = 8000;

	private CheckUpdateManager checkUpdateManager;
	private Context mContext;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION_CHECK_UPDATE)) {
			Intent it = new Intent(context, CheckUpdateService.class);
			context.startService(it);
			
			/*mContext = context;
			checkUpdate(context);
			Log.d(AlarmReceiver.class.getSimpleName(), ACTION_CHECK_UPDATE);*/
		}
	}
	
	private void checkUpdate(Context context) {
		checkUpdateManager = new CheckUpdateManager(context);
		checkUpdateManager.setOnCompleteListener(onCompleteListener);
		checkUpdateManager.setConnectivityToastEnabled(false);
		checkUpdateManager.setProgressDialogEnabled(false);
		checkUpdateManager.checkUpdate();
	}
	
	private void showNotification(String newVersionName) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
			.setSmallIcon(R.mipmap.ic_launcher)
			.setContentTitle(mContext.getString(R.string.update_notification_title))
			.setContentText(mContext.getString(R.string.update_notification_text));
		
		Intent resultIntent = new Intent(mContext, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);
		
		NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(NOTIFICATION_ID, builder.build());
	}
	
	private CheckUpdateManager.OnCompleteListener onCompleteListener = new CheckUpdateManager.OnCompleteListener() {
		@Override
		public void onComplete(int versionCode, String versionName, String url, String message) {
			if (!checkUpdateManager.isLatestVersion(versionCode))
				showNotification(versionName);
		}
	};
}
