package com.galaxy.lib.update;
import android.os.*;
import android.content.*;
import android.util.*;
import android.net.*;
import android.widget.*;
import java.io.*;
import android.app.*;

public class DownloadFileAsyncTask extends AsyncTask<DownloadFileAsyncTask.DownloadPack, Double, Long>
{
	private Context context;
	private CheckUpdateManager checkUpdateUtils;
	private boolean isProgressDialogEnabled;
	private ProgressDialog progressDialog;
	private boolean isNotificationEnabled;
	private String sNotificationTitle;
	private String sNotificationDescription;
	
	public DownloadFileAsyncTask(Context context,CheckUpdateManager checkUpdateUtils ,boolean isProgressDialogEnabled, boolean isNotificationEnabled){
		this.context = context;
		this.checkUpdateUtils = checkUpdateUtils;
		this.isProgressDialogEnabled = isProgressDialogEnabled;
		this.isNotificationEnabled = isNotificationEnabled;
	}
	
	@Override
	protected void onPreExecute()
	{
		// TODO: Implement this method
		super.onPreExecute();
		if (isProgressDialogEnabled)
			progressDialog.show();
	}

	@Override
	protected Long doInBackground(DownloadFileAsyncTask.DownloadPack[] params) {
		// TODO: Implement this method
		long downloadId = -1;
		for (int i = 0; i < params.length; i++) {
			// check and add download location
			File direct = new File(Environment.getExternalStorageDirectory() + params[i].getLocation());
			if (!direct.exists()) {
				direct.mkdirs();
			}

			File targetFile = new File(Environment.getExternalStorageDirectory() + params[i].getLocation() + "/" + params[i].getFileName());
			if (targetFile.exists())
				targetFile.delete();

			// start download manager
			DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

			// start request
			Uri downloadUri = Uri.parse(params[i].getUrl());
			DownloadManager.Request request = new DownloadManager.Request(downloadUri);

			// set request
			request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
			request.setAllowedOverRoaming(false);
			if (isNotificationEnabled) {
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
				if (sNotificationTitle != null)
					request.setTitle(sNotificationTitle);
				if (sNotificationDescription != null)
					request.setDescription(sNotificationDescription);
			}
			else
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
			request.setDestinationInExternalPublicDir(params[i].getLocation(), params[i].getFileName());

			// start download
			downloadId = downloadManager.enqueue(request);
		}
		return downloadId;
	}

	@Override
	protected void onProgressUpdate(Double[] values)
	{
		// TODO: Implement this method
		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(Long result)
	{
		// TODO: Implement this method
		super.onPostExecute(result);
		checkUpdateUtils.setDownloadId(result);
	}
	
	protected void setNotificationDescription(String description) {
		sNotificationDescription = description;
	}
	
	protected void setNotificationTitle(String title) {
		sNotificationTitle = title;
	}
	
	protected void setProgressDialog(ProgressDialog progressDialog) {
		if (!isProgressDialogEnabled) {
			Log.w(DownloadFileAsyncTask.class.getName(), "Cannot set progress dialog");
			return;
		}
		this.progressDialog = progressDialog;
	}
	
	public static class DownloadPack {
		private String url;
		private String location;
		private String fileName;
		
		public DownloadPack() {
			
		}
		
		public DownloadPack(String url, String location, String fileName) {
			this.url = url;
			this.location = location;
			this.fileName = fileName;
		}

		public void setUrl(String url) {
			if (url == null) {
				Log.e(DownloadPack.class.getName(), "url cannot be null");
				return;
			}
			this.url = url;
		}

		public String getUrl() {
			return url;
		}

		public void setLocation(String location) {
			if (location == null) {
				Log.e(DownloadPack.class.getName(), "location cannot be null");
				return;
			}
			this.location = location;
		}

		public String getLocation() {
			return location;
		}

		public void setFileName(String fileName) {
			if (fileName == null) {
				Log.e(DownloadPack.class.getName(), "fileName cannot be null");
				return;
			}
			this.fileName = fileName;
		}

		public String getFileName(){
			return fileName;
		}
	}
}
