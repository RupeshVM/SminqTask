package rupvm.sminqtask.view;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import rupvm.sminqtask.R;
import rupvm.sminqtask.adapter.TaskListAdapter;
import rupvm.sminqtask.controller.AppController;
import rupvm.sminqtask.database.TASK_FILES_TABLE;
import rupvm.sminqtask.database.TASK_FILES_TABLEDao;
import rupvm.sminqtask.database.TASK_TABLE;
import rupvm.sminqtask.databaserepo.TaskFilesTableRepo;
import rupvm.sminqtask.databaserepo.TaskTableRepo;
import rupvm.sminqtask.fileupload.MultipartUploadRequest;
import rupvm.sminqtask.fileupload.UploadNotificationConfig;
import rupvm.sminqtask.service.SyncService;
import rupvm.sminqtask.utils.AppConstants;
import rupvm.sminqtask.utils.Utils;

import static rupvm.sminqtask.utils.AppConstants.TASK_REQUEST_CODE;

public class MainActivity extends AppCompatActivity {
    private TaskListAdapter taskListAdapter;
    private List<TASK_TABLE> task_tables = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayout linearLayout;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(AppConstants.UPLOAD_ERROR_UPDATE)) {
                String id = intent.getStringExtra("ID");
                TASK_FILES_TABLE task_files_table = TaskFilesTableRepo.getTaskFileTableById(getApplicationContext(), Long.valueOf(id));
                task_files_table.setUPLOADSTATUS(0);
                TaskFilesTableRepo.insertOrUpdate(getApplicationContext(), task_files_table);
                TASK_FILES_TABLE task_files_table1 = TaskFilesTableRepo.getTaskFileTableById(getApplicationContext(), Long.parseLong(id));
                TASK_TABLE task_table = TaskTableRepo.getTaskTableById(getApplicationContext(), task_files_table1.getTASKID());
                task_table.setUPLOADSTATUS(0);
                TaskTableRepo.insertOrUpdate(getApplicationContext(), task_table);
                task_tables.clear();
                task_tables.addAll(TaskTableRepo.getAllTasks(MainActivity.this));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        taskListAdapter.notifyDataSetChanged();
                        Utils.showMessage("Something went wrong, please try again");
                    }
                }, 2000);
            }
            if (intent.getAction().equalsIgnoreCase(AppConstants.UPLOAD_COMPLETE_UPDATE)) {
                String id = intent.getStringExtra("ID");
                TASK_FILES_TABLE task_files_table = TaskFilesTableRepo.getTaskFileTableById(getApplicationContext(), Long.valueOf(id));
                task_files_table.setFILEID(Long.valueOf(id));
                task_files_table.setUPLOADSTATUS(1);
                TaskFilesTableRepo.insertOrUpdate(getApplicationContext(), task_files_table);

                List<TASK_FILES_TABLE> task_files_tables = TaskFilesTableRepo.getTaskTableDao(getApplicationContext()).queryBuilder().where(TASK_FILES_TABLEDao.Properties.UPLOADSTATUS.eq("0")).where(TASK_FILES_TABLEDao.Properties.UPLOADSTATUS.eq("2")).where(TASK_FILES_TABLEDao.Properties.TASKID.eq(Long.parseLong(id))).list();
                if (task_files_tables.size() == 0) {
                    TASK_FILES_TABLE task_files_table1 = TaskFilesTableRepo.getTaskFileTableById(getApplicationContext(), Long.parseLong(id));
                    TASK_TABLE task_table = TaskTableRepo.getTaskTableById(getApplicationContext(), task_files_table1.getTASKID());
                    task_table.setUPLOADSTATUS(1);
                    TaskTableRepo.insertOrUpdate(getApplicationContext(), task_table);
                }
                task_tables.clear();
                task_tables.addAll(TaskTableRepo.getAllTasks(MainActivity.this));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        taskListAdapter.notifyDataSetChanged();
                    }
                }, 2000);
            }
            if (intent.getAction().equalsIgnoreCase(AppConstants.UPLOAD_START)) {
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
                task_tables.clear();
                task_tables.addAll(TaskTableRepo.getAllTasks(MainActivity.this));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        taskListAdapter.notifyDataSetChanged();
                    }
                }, 2000);
            }
        }
    };


    /**
     * Method contains upload notification configuration
     * @InProgress @
     * @param uploadId
     * @param title
     * @return
     */
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(MainActivity.this, SyncService.class));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstants.UPLOAD_START);
        intentFilter.addAction(AppConstants.DOWNLOAD_ERROR);
        intentFilter.addAction(AppConstants.DOWNLOAD_COMPLETE);
        intentFilter.addAction(AppConstants.UPLOAD_COMPLETE_UPDATE);
        intentFilter.addAction(AppConstants.UPLOAD_ERROR_UPDATE);

        registerReceiver(broadcastReceiver, intentFilter);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        task_tables.addAll(TaskTableRepo.getAllTasks(getApplicationContext()));
        taskListAdapter = new TaskListAdapter(task_tables, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(taskListAdapter);
        if (TaskTableRepo.getAllTasks(getApplicationContext()).size() != 0) {
            recyclerView.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.GONE);

        } else {
            recyclerView.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppController.saveToPreferences(getApplicationContext(), "ISACTIVE", "INACTIVE");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionAdd:
                startActivityForResult(new Intent(this, AddTaskActivity.class), TASK_REQUEST_CODE);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TASK_REQUEST_CODE:
                    long taskId = data.getLongExtra("taskid",0l);
                    String locationName = data.getStringExtra("location_name");
                    List<TASK_FILES_TABLE>task_files_tables = AddTaskActivity.getTask_files_tables();
                    for (TASK_FILES_TABLE task_files_table1 : task_files_tables) {
                        TASK_FILES_TABLE task_files_table = new TASK_FILES_TABLE();
                        TASK_TABLE task_table = new TASK_TABLE();
                        task_table.setTASKID(taskId);
                        if (Utils.isNetworkAvailable()) {
                            task_table.setUPLOADSTATUS(2);
                        } else {
                            task_table.setUPLOADSTATUS(0);
                        }
                        task_files_table.setFILEURL(task_files_table1.getFILEURL());
                        task_files_table.setTASKID(taskId);
                        if (Utils.isNetworkAvailable()) {
                            task_files_table.setUPLOADSTATUS(2);
                        } else {
                            task_files_table.setUPLOADSTATUS(0);
                        }
                        TaskTableRepo.insertOrUpdate(getApplicationContext(), task_table);
                        TaskFilesTableRepo.insertOrUpdate(getApplicationContext(), task_files_table);
                    }

                    if (Utils.isNetworkAvailable()) {
                        sendBroadcast(new Intent().setAction(AppConstants.UPLOAD_START));
                    }
                    TASK_TABLE task_table = TaskTableRepo.getTaskTableById(getApplicationContext(), taskId);
                    task_table.setTASKID(taskId);
                    task_table.setLOCATIONNAME(locationName);
                    TaskTableRepo.insertOrUpdate(getApplicationContext(), task_table);
                    task_tables.clear();
                    task_tables.addAll(TaskTableRepo.getAllTasks(this));

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            taskListAdapter.notifyDataSetChanged();
                        }
                    }, 1000);
                    if (TaskTableRepo.getAllTasks(getApplicationContext()).size() != 0) {
                        recyclerView.setVisibility(View.VISIBLE);
                        linearLayout.setVisibility(View.GONE);

                    } else {
                        recyclerView.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppController.saveToPreferences(getApplicationContext(), "ISACTIVE", "ACTIVE");

    }
}
