package com.galaxy.youtube.updater;

import android.app.*;
import android.os.*;
import com.galaxy.lib.update.*;
import android.support.v4.content.*;
import android.support.v4.app.*;
import android.content.pm.*;
import android.*;
import android.content.pm.PackageManager.*;
import android.widget.*;
import android.content.*;
import android.view.View.*;
import android.view.*;
import android.graphics.*;

public class MainActivity extends Activity 
{
	private boolean isUpdateAll = false;
	private int updateFinishedCount = 0;
	private boolean isVancedUpdateFound = false;
	private boolean isMicroGUpdateFound = false;
	private boolean isUpdaterUpdateFound = false;
	private String vancedVersionName;
	private String vancedMessage;
	private String vancedUrl;
	private String microgVersionName;
	private String microgMessage;
	private String microgUrl;
	private String updaterVersionName;
	private String updaterMessage;
	private String updaterUrl;
	
    private CheckUpdateManager vancedUpdateManager, microGUpdateManager, updaterUpdateManager;
	private AlarmManager mAlarmManger;
	private PendingIntent mPendingIntent;
	
	private TextView mTxtYoutubeVersion, mTxtMicrogVersion, mTxtUpdaterVersion;
	private Button mBtnYoutube, mBtnMicrog, mBtnUpdater, mBtnCheckAll;
	private ProgressBar mPrgYoutube, mPrgMicrog, mPrgUpdater;
	private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		// request permission and check update
		vancedUpdateManager = new CheckUpdateManager(MainActivity.this);
		microGUpdateManager = new CheckUpdateManager(MainActivity.this);
		updaterUpdateManager = new CheckUpdateManager(MainActivity.this);
		requestPermission();
		
		// init alarm manager
		mAlarmManger = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent it = new Intent(this, AlarmReceiver.class);
		it.setAction(AlarmReceiver.ACTION_CHECK_UPDATE);
		mPendingIntent = PendingIntent.getBroadcast(this, 0, it, 0);
		mAlarmManger.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_HOUR, mPendingIntent);
		
		// register reboot broadcast receiver
		ComponentName receiver = new ComponentName(this, AlarmReceiver.class);
		PackageManager pm = getPackageManager();
		pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		
		// handle ui
		initViews();
		initProgressDialog();
		
		// get versions and assign it
		try {
			String version = getPackageManager().getPackageInfo(Constants.VANCED_PACKAGE_NAME, 0).versionName;
			mTxtYoutubeVersion.setText(String.format(getString(R.string.version), version));
		} catch (PackageManager.NameNotFoundException e) {
			mTxtYoutubeVersion.setText(R.string.not_installed);
			mTxtYoutubeVersion.setTextColor(Color.RED);
		}
		try {
			String version = getPackageManager().getPackageInfo(Constants.MICROG_PACKAGE_NAME, 0).versionName;
			mTxtMicrogVersion.setText(String.format(getString(R.string.version), version));
		} catch (PackageManager.NameNotFoundException e) {
			mTxtMicrogVersion.setText(R.string.not_installed);
			mTxtMicrogVersion.setTextColor(Color.RED);
		}
		try {
			String version = getPackageManager().getPackageInfo(Constants.UPDATER_PACKAGE_NAME, 0).versionName;
			mTxtUpdaterVersion.setText(String.format(getString(R.string.version), version));
		} catch (PackageManager.NameNotFoundException e) {
			mTxtUpdaterVersion.setText(R.string.not_installed);
			mTxtUpdaterVersion.setTextColor(Color.RED);
		}

		// init listeners
		mBtnYoutube.setOnClickListener(onBtnCheckUpdateClick);
		mBtnMicrog.setOnClickListener(onBtnCheckUpdateClick);
		mBtnUpdater.setOnClickListener(onBtnCheckUpdateClick);
		mBtnCheckAll.setOnClickListener(onBtnCheckUpdateClick);
    }

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		switch (requestCode) {
			case 0: {
					if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					} else {
						requestPermission();
					}
					break;
				}
		}
	}

	private void requestPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
		} else {
			// checkVancedUpdate();
			// checkMicroGUpdate();
			// checkUpdaterUpdate();
		}
	}
	
	private View.OnClickListener onBtnCheckUpdateClick = new  View.OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
				case R.id.mainBtnYoutube:
					checkVancedUpdate(false);
					break;
				case R.id.mainBtnMicrog:
					checkMicroGUpdate(false);
					break;
				case R.id.mainBtnUpdater:
					checkUpdaterUpdate(false);
					break;
				case R.id.mainBtnCheckAll:
					isUpdateAll = true;
					updateFinishedCount = 0;
					progressDialog.show();
					checkVancedUpdate(true);
					checkMicroGUpdate(true);
					checkUpdaterUpdate(true);
					break;
			}
		}
	};

	private void checkVancedUpdate(boolean isUpdateAll) {
		String sMessage = getResources().getString(R.string.check_update_progress_message);
		vancedUpdateManager.setPackageName(Constants.VANCED_PACKAGE_NAME);
		vancedUpdateManager.setUrlVersion(Constants.VANCED_VERSION_URL);
		vancedUpdateManager.setPathVersion(Constants.VANCED_VERSION_PATH);
		vancedUpdateManager.setPathUpdate(Constants.VANCED_UPDATE_PATH);
		vancedUpdateManager.setOnCompleteListener(onVancedDownloadComplete);
		vancedUpdateManager.setConnectivityToastEnabled(true);
		vancedUpdateManager.setProgressDialogEnabled(!isUpdateAll);
		vancedUpdateManager.setProgressDialogInfo(sMessage);
		vancedUpdateManager.checkUpdate();
	}

	private CheckUpdateManager.OnCompleteListener onVancedDownloadComplete = new CheckUpdateManager.OnCompleteListener() {
		@Override
		public void onComplete(int versionCode, String versionName, String url, String message)
		{
			if (!vancedUpdateManager.isLatestVersion(versionCode)) {
				if (isUpdateAll) {
					isVancedUpdateFound = true;
					vancedVersionName = versionName;
					vancedMessage = message;
					vancedUrl = url;
					updateFinishedCount++;
					
					if (updateFinishedCount == 3) {
						progressDialog.dismiss();
						isUpdateAll = false;
						if (isUpdaterUpdateFound)
							updaterUpdateManager.showSureUpdateDialog(updaterVersionName, updaterMessage, updaterUrl);
						if (isMicroGUpdateFound)
							microGUpdateManager.showSureUpdateDialog(microgVersionName, microgMessage, microgUrl);
						if (isVancedUpdateFound)
							vancedUpdateManager.showSureUpdateDialog(vancedVersionName, vancedMessage, vancedUrl);
						if (!(isVancedUpdateFound || isMicroGUpdateFound || isUpdaterUpdateFound))
							Toast.makeText(MainActivity.this, R.string.all_all_are_latest_version, Toast.LENGTH_LONG).show();
					}
				} else
					vancedUpdateManager.showSureUpdateDialog(versionName, message, url);
			}
			else {
				if (isUpdateAll) {
					updateFinishedCount++;
					
					if (updateFinishedCount == 3) {
						progressDialog.dismiss();
						isUpdateAll = false;
						if (isUpdaterUpdateFound)
							updaterUpdateManager.showSureUpdateDialog(updaterVersionName, updaterMessage, updaterUrl);
						if (isMicroGUpdateFound)
							microGUpdateManager.showSureUpdateDialog(microgVersionName, microgMessage, microgUrl);
						if (isVancedUpdateFound)
							vancedUpdateManager.showSureUpdateDialog(vancedVersionName, vancedMessage, vancedUrl);
						if (!(isVancedUpdateFound || isMicroGUpdateFound || isUpdaterUpdateFound))
							Toast.makeText(MainActivity.this, R.string.all_all_are_latest_version, Toast.LENGTH_LONG).show();
					}
				} else
					Toast.makeText(MainActivity.this, String.format(getString(R.string.latest_version_toast), "YouTube Vanced"), Toast.LENGTH_LONG).show();
			}
		}
	};
	
	private void checkMicroGUpdate(boolean isUpdateAll) {
		String sMessage = getResources().getString(R.string.check_update_progress_message);
		microGUpdateManager.setPackageName(Constants.MICROG_PACKAGE_NAME);
		microGUpdateManager.setUrlVersion(Constants.MICROG_VERSION_URL);
		microGUpdateManager.setPathVersion(Constants.MICROG_VERSION_PATH);
		microGUpdateManager.setPathUpdate(Constants.MICROG_UPDATE_PATH);
		microGUpdateManager.setOnCompleteListener(onMicroGDownloadComplete);
		microGUpdateManager.setConnectivityToastEnabled(true);
		microGUpdateManager.setProgressDialogEnabled(!isUpdateAll);
		microGUpdateManager.setProgressDialogInfo(sMessage);
		microGUpdateManager.checkUpdate();
	}

	private CheckUpdateManager.OnCompleteListener onMicroGDownloadComplete = new CheckUpdateManager.OnCompleteListener() {
		@Override
		public void onComplete(int versionCode, String versionName, String url, String message)
		{
			if (!microGUpdateManager.isLatestVersion(versionCode)) {
				if (isUpdateAll) {
					isMicroGUpdateFound = true;
					microgVersionName = versionName;
					microgMessage = message;
					microgUrl = url;
					updateFinishedCount++;

					if (updateFinishedCount == 3) {
						progressDialog.dismiss();
						isUpdateAll = false;
						if (isUpdaterUpdateFound)
							updaterUpdateManager.showSureUpdateDialog(updaterVersionName, updaterMessage, updaterUrl);
						if (isMicroGUpdateFound)
							microGUpdateManager.showSureUpdateDialog(microgVersionName, microgMessage, microgUrl);
						if (isVancedUpdateFound)
							vancedUpdateManager.showSureUpdateDialog(vancedVersionName, vancedMessage, vancedUrl);
						if (!(isVancedUpdateFound || isMicroGUpdateFound || isUpdaterUpdateFound))
							Toast.makeText(MainActivity.this, R.string.all_all_are_latest_version, Toast.LENGTH_LONG).show();
					}
				} else
					microGUpdateManager.showSureUpdateDialog(versionName, message, url);
			}
			else {
				if (isUpdateAll) {
					updateFinishedCount++;

					if (updateFinishedCount == 3) {
						progressDialog.dismiss();
						isUpdateAll = false;
						if (isUpdaterUpdateFound)
							updaterUpdateManager.showSureUpdateDialog(updaterVersionName, updaterMessage, updaterUrl);
						if (isMicroGUpdateFound)
							microGUpdateManager.showSureUpdateDialog(microgVersionName, microgMessage, microgUrl);
						if (isVancedUpdateFound)
							vancedUpdateManager.showSureUpdateDialog(vancedVersionName, vancedMessage, vancedUrl);
						if (!(isVancedUpdateFound || isMicroGUpdateFound || isUpdaterUpdateFound))
							Toast.makeText(MainActivity.this, R.string.all_all_are_latest_version, Toast.LENGTH_LONG).show();
					}
				} else
					Toast.makeText(MainActivity.this, String.format(getString(R.string.latest_version_toast), "MicroG Services Core"), Toast.LENGTH_LONG).show();
			}
		}
	};
	
	private void checkUpdaterUpdate(boolean isUpdateAll) {
		String sMessage = getResources().getString(R.string.check_update_progress_message);
		updaterUpdateManager.setPackageName(Constants.UPDATER_PACKAGE_NAME);
		updaterUpdateManager.setUrlVersion(Constants.UPDATER_VERSION_URL);
		updaterUpdateManager.setPathVersion(Constants.UPDATER_VERSION_PATH);
		updaterUpdateManager.setPathUpdate(Constants.UPDATER_UPDATE_PATH);
		updaterUpdateManager.setOnCompleteListener(onUpdaterDownloadComplete);
		updaterUpdateManager.setConnectivityToastEnabled(true);
		updaterUpdateManager.setProgressDialogEnabled(!isUpdateAll);
		updaterUpdateManager.setProgressDialogInfo(sMessage);
		updaterUpdateManager.checkUpdate();
	}

	private CheckUpdateManager.OnCompleteListener onUpdaterDownloadComplete = new CheckUpdateManager.OnCompleteListener() {
		@Override
		public void onComplete(int versionCode, String versionName, String url, String message)
		{
			if (!updaterUpdateManager.isLatestVersion(versionCode)) {
				if (isUpdateAll) {
					isUpdaterUpdateFound = true;
					updaterVersionName = versionName;
					updaterMessage = message;
					updaterUrl = url;
					updateFinishedCount++;

					if (updateFinishedCount == 3) {
						progressDialog.dismiss();
						isUpdateAll = false;
						if (isUpdaterUpdateFound)
							updaterUpdateManager.showSureUpdateDialog(updaterVersionName, updaterMessage, updaterUrl);
						if (isMicroGUpdateFound)
							microGUpdateManager.showSureUpdateDialog(microgVersionName, microgMessage, microgUrl);
						if (isVancedUpdateFound)
							vancedUpdateManager.showSureUpdateDialog(vancedVersionName, vancedMessage, vancedUrl);
						if (!(isVancedUpdateFound || isMicroGUpdateFound || isUpdaterUpdateFound))
							Toast.makeText(MainActivity.this, R.string.all_all_are_latest_version, Toast.LENGTH_LONG).show();
					}
				} else
					updaterUpdateManager.showSureUpdateDialog(versionName, message, url);
			}
			else {
				if (isUpdateAll) {
					updateFinishedCount++;

					if (updateFinishedCount == 3) {
						progressDialog.dismiss();
						isUpdateAll = false;
						if (isUpdaterUpdateFound)
							updaterUpdateManager.showSureUpdateDialog(updaterVersionName, updaterMessage, updaterUrl);
						if (isMicroGUpdateFound)
							microGUpdateManager.showSureUpdateDialog(microgVersionName, microgMessage, microgUrl);
						if (isVancedUpdateFound)
							vancedUpdateManager.showSureUpdateDialog(vancedVersionName, vancedMessage, vancedUrl);
						if (!(isVancedUpdateFound || isMicroGUpdateFound || isUpdaterUpdateFound))
							Toast.makeText(MainActivity.this, R.string.all_all_are_latest_version, Toast.LENGTH_LONG).show();
					}
				} else
					Toast.makeText(MainActivity.this, String.format(getString(R.string.latest_version_toast), "YouTube Updater"), Toast.LENGTH_LONG).show();
			}
		}
	};
	
	private void initProgressDialog() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(false);
		progressDialog.setMessage(getString(R.string.check_update_progress_message));
	}
	
	private void initViews() {
		mTxtYoutubeVersion = findViewById(R.id.mainTxtYoutubeVersion);
		mTxtMicrogVersion = findViewById(R.id.mainTxtMicrogVersion);
		mTxtUpdaterVersion = findViewById(R.id.mainTxtUpdaterVersion);
		mBtnYoutube = findViewById(R.id.mainBtnYoutube);
		mBtnMicrog = findViewById(R.id.mainBtnMicrog);
		mBtnUpdater = findViewById(R.id.mainBtnUpdater);
		mBtnCheckAll = findViewById(R.id.mainBtnCheckAll);
		mPrgYoutube = findViewById(R.id.mainPrgYoutube);
		mPrgMicrog = findViewById(R.id.mainPrgMicrog);
		mPrgUpdater = findViewById(R.id.mainPrgUpdater);
	}
}
