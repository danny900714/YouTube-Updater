package com.galaxy.youtube.updater;
import android.content.*;
import android.app.*;
import android.os.*;

public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			registerAlarm(context);
		}
	}
	
	private void registerAlarm(Context context) {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(AlarmReceiver.ACTION_CHECK_UPDATE), PendingIntent.FLAG_NO_CREATE);

		// If alarm doesn't exist, register one
		if (pendingIntent == null) {
			AlarmManager alarmMgr = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
			Intent checkUpdateIt = new Intent(context.getApplicationContext(), AlarmReceiver.class);
			PendingIntent checkUpdatePendIt = PendingIntent.getBroadcast(context.getApplicationContext(), 0, checkUpdateIt, 0);
			alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_HOUR, checkUpdatePendIt);
		}
	}

}
