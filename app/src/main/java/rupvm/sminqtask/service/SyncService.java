package rupvm.sminqtask.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.List;

import rupvm.sminqtask.R;
import rupvm.sminqtask.controller.AppController;
import rupvm.sminqtask.database.TASK_FILES_TABLE;
import rupvm.sminqtask.database.TASK_FILES_TABLEDao;
import rupvm.sminqtask.database.TASK_TABLE;
import rupvm.sminqtask.databaserepo.TaskFilesTableRepo;
import rupvm.sminqtask.databaserepo.TaskTableRepo;
import rupvm.sminqtask.fileupload.BroadcastData;
import rupvm.sminqtask.fileupload.Logger;
import rupvm.sminqtask.fileupload.MultipartUploadRequest;
import rupvm.sminqtask.fileupload.ServerResponse;
import rupvm.sminqtask.fileupload.UploadInfo;
import rupvm.sminqtask.fileupload.UploadNotificationConfig;
import rupvm.sminqtask.fileupload.UploadService;
import rupvm.sminqtask.utils.AppConstants;
import rupvm.sminqtask.utils.Utils;
import rupvm.sminqtask.view.MainActivity;

/**
 * Created by rup on 8/9/17.
 */

public class SyncService extends Service {

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (UploadService.getActionBroadcast().equals(intent.getAction())) {

                BroadcastData data = intent.getParcelableExtra(UploadService.PARAM_BROADCAST_DATA);

                if (data == null) {
                    Logger.error(getClass().getSimpleName(), "Missing intent parameter: " + UploadService.PARAM_BROADCAST_DATA);
                    return;
                }

                UploadInfo uploadInfo = data.getUploadInfo();
                String isActive = AppController.getStringPreference(getApplicationContext(), "ISACTIVE");

                switch (data.getStatus()) {

                    case ERROR:
                        if (isActive.equalsIgnoreCase("INACTIVE")) {
                            onError(context, uploadInfo, data.getServerResponse(), data.getException());
                        } else {
                            sendBroadcast(new Intent().setAction(AppConstants.UPLOAD_ERROR_UPDATE).putExtra("ID",uploadInfo.getUploadId()));
                        }
                        break;

                    case COMPLETED:
                        if (isActive.equalsIgnoreCase("INACTIVE")) {
                            onCompleted(context, uploadInfo, data.getServerResponse());
                        } else {
                            sendBroadcast(new Intent().setAction(AppConstants.UPLOAD_COMPLETE_UPDATE).putExtra("ID",uploadInfo.getUploadId()));
                        }
                        break;

                    case IN_PROGRESS:
//                    onProgress(context, uploadInfo);
                        break;

                    case CANCELLED:
//                    onCancelled(context, uploadInfo);
                        break;

                    default:
                        break;
                }
            }
                if (intent.getAction().equalsIgnoreCase(AppConstants.UPLOAD_START)) {
                List<TASK_FILES_TABLE> task_files_tables = TaskFilesTableRepo.getTaskTableDao(getApplicationContext()).queryBuilder().where(TASK_FILES_TABLEDao.Properties.UPLOADSTATUS.eq("2")).list();
                for (TASK_FILES_TABLE task_files_table : task_files_tables) {
                    TASK_TABLE task_table = TaskTableRepo.getTaskTableById(getApplicationContext(), task_files_table.getTASKID());
                    try {
                        MultipartUploadRequest multipartUploadRequest = new MultipartUploadRequest(getApplicationContext(), String.valueOf(task_files_table.getFILEID()), AppConstants.UPLOAD_FILE_URL);
                        multipartUploadRequest.setNotificationConfig(getNotificationConfig(String.valueOf(task_files_table.getFILEID()), task_table.getLOCATIONNAME()));
                        multipartUploadRequest.addParameter("id", String.valueOf(task_table.getTASKID()));
                        multipartUploadRequest.addFileToUpload(task_files_table.getFILEURL(), "file", "Hello");
                        multipartUploadRequest.addParameter("location", task_table.getLOCATIONNAME());
                        multipartUploadRequest.startUpload();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (intent.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE")) {
                if (Utils.isNetworkAvailable()) {
                    String isActive = AppController.getStringPreference(getApplicationContext(), "ISACTIVE");

                    if (isActive.equalsIgnoreCase("INACTIVE")) {
                        List<TASK_FILES_TABLE> task_files_tables = TaskFilesTableRepo.getTaskTableDao(getApplicationContext()).queryBuilder().where(TASK_FILES_TABLEDao.Properties.UPLOADSTATUS.eq("0")).list();
                        for (TASK_FILES_TABLE task_files_table : task_files_tables) {
                            TASK_TABLE task_table = TaskTableRepo.getTaskTableById(getApplicationContext(), task_files_table.getTASKID());
                            task_table.setUPLOADSTATUS(2);
                            TaskTableRepo.insertOrUpdate(getApplicationContext(), task_table);
                            try {
                                MultipartUploadRequest multipartUploadRequest = new MultipartUploadRequest(getApplicationContext(), String.valueOf(task_files_table.getFILEID()), AppConstants.UPLOAD_FILE_URL);
                                multipartUploadRequest.setNotificationConfig(getNotificationConfig(String.valueOf(task_files_table.getFILEID()), task_table.getLOCATIONNAME()));
                                multipartUploadRequest.addFileToUpload(task_files_table.getFILEURL(), "file", "Hello");
                                multipartUploadRequest.addParameter("id", String.valueOf(task_table.getTASKID()));
                                multipartUploadRequest.addParameter("location", task_table.getLOCATIONNAME());
                                multipartUploadRequest.startUpload();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }else{
                        sendBroadcast(new Intent().setAction(AppConstants.UPLOAD_START));
                    }
                } else {
                    UploadService.stopAllUploads();
                }
            }
        }
    };

    /**
     * Called when some error occure
     *
     * @param context
     * @param uploadInfo
     * @param serverResponse
     * @param exception
     */
    private void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
        TASK_FILES_TABLE task_files_table = TaskFilesTableRepo.getTaskFileTableById(getApplicationContext(), Long.valueOf(uploadInfo.getUploadId()));
        task_files_table.setUPLOADSTATUS(0);
        TaskFilesTableRepo.insertOrUpdate(getApplicationContext(), task_files_table);
        TASK_FILES_TABLE task_files_table1 = TaskFilesTableRepo.getTaskFileTableById(getApplicationContext(), Long.parseLong(uploadInfo.getUploadId()));
        TASK_TABLE task_table = TaskTableRepo.getTaskTableById(getApplicationContext(), task_files_table1.getTASKID());
        task_table.setUPLOADSTATUS(0);
        TaskTableRepo.insertOrUpdate(getApplicationContext(), task_table);
        sendBroadcast(new Intent().setAction(AppConstants.DOWNLOAD_ERROR));

    }

    /**
     * Called when file successfully uploaded
     *
     * @param context
     * @param uploadInfo     contains upload information
     * @param serverResponse contains server response
     */
    private void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
        TASK_FILES_TABLE task_files_table = TaskFilesTableRepo.getTaskFileTableById(getApplicationContext(), Long.valueOf(uploadInfo.getUploadId()));
        task_files_table.setFILEID(Long.valueOf(uploadInfo.getUploadId()));
        task_files_table.setUPLOADSTATUS(1);
        TaskFilesTableRepo.insertOrUpdate(getApplicationContext(), task_files_table);

        List<TASK_FILES_TABLE> task_files_tables = TaskFilesTableRepo.getTaskTableDao(getApplicationContext()).queryBuilder().where(TASK_FILES_TABLEDao.Properties.UPLOADSTATUS.eq("0")).where(TASK_FILES_TABLEDao.Properties.UPLOADSTATUS.eq("2")).where(TASK_FILES_TABLEDao.Properties.TASKID.eq(Long.parseLong(uploadInfo.getUploadId()))).list();
        if (task_files_tables.size() == 0) {
            TASK_FILES_TABLE task_files_table1 = TaskFilesTableRepo.getTaskFileTableById(getApplicationContext(), Long.parseLong(uploadInfo.getUploadId()));
            TASK_TABLE task_table = TaskTableRepo.getTaskTableById(getApplicationContext(), task_files_table1.getTASKID());
            task_table.setUPLOADSTATUS(1);
            TaskTableRepo.insertOrUpdate(getApplicationContext(), task_table);
        }
        sendBroadcast(new Intent().setAction(AppConstants.DOWNLOAD_COMPLETE));
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstants.UPLOAD_START);
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction(UploadService.getActionBroadcast());
        registerReceiver(receiver, intentFilter);
    }


    protected UploadNotificationConfig getNotificationConfig(final String uploadId, String title) {
        UploadNotificationConfig config = new UploadNotificationConfig();

        PendingIntent clickIntent = PendingIntent.getActivity(
                this, 1, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        config.setTitleForAllStatuses(title)
                .setRingToneEnabled(true)
                .setClickIntentForAllStatuses(clickIntent)
                .setRingToneEnabled(true)
                .setClearOnActionForAllStatuses(false);

        config.getProgress().message = getString(R.string.uploading);
        config.getProgress().iconResourceID = R.drawable.ic_cloud_upload_yellow_900_24dp;
        config.getProgress().iconColorResourceID = Color.BLUE;


        config.getCompleted().message = getString(R.string.upload_success);
        config.getCompleted().iconResourceID = R.drawable.ic_cloud_upload_green_600_24dp;
        config.getCompleted().iconColorResourceID = Color.GREEN;

        config.getError().message = getString(R.string.upload_error);
        config.getError().iconResourceID = R.drawable.ic_error_red_500_24dp;
        config.getError().iconColorResourceID = Color.RED;

        config.getCancelled().message = getString(R.string.upload_cancelled);
        config.getCancelled().iconResourceID = R.drawable.ic_cancel_red_700_24dp;
        config.getCancelled().iconColorResourceID = Color.YELLOW;

        return config;
    }

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceTask = new Intent(getApplicationContext(), this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);
        super.onTaskRemoved(rootIntent);
        startService(
                new Intent(this, SyncService.class)
        );
    }
}
