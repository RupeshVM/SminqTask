package rupvm.sminqtask.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rupvm.sminqtask.R;
import rupvm.sminqtask.adapter.ImageAdapter;
import rupvm.sminqtask.custom.MyEditText;
import rupvm.sminqtask.custom.MyTextView;
import rupvm.sminqtask.database.TASK_FILES_TABLE;
import rupvm.sminqtask.database.TASK_FILES_TABLEDao;
import rupvm.sminqtask.database.TASK_TABLE;
import rupvm.sminqtask.databaserepo.TaskFilesTableRepo;
import rupvm.sminqtask.databaserepo.TaskTableRepo;
import rupvm.sminqtask.utils.AppConstants;
import rupvm.sminqtask.utils.Utils;

import static android.support.v4.content.FileProvider.getUriForFile;
import static rupvm.sminqtask.utils.AppConstants.IMAGE_CAPTURE_REQUEST_CODE;

public class AddTaskActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imageCapture;
    private ImageView imageUpload;
    private MyTextView textLocationName;
    private MyEditText editLocationName;
    private RecyclerView recyclerView;
    private String locationName;
    private Uri imageUri;
    private String fileName;
    private long taskId;
    private ImageAdapter imageAdapter;
    private MyTextView textView;
    public static List<TASK_FILES_TABLE> task_files_tables = new ArrayList<>();

    public static List<TASK_FILES_TABLE> getTask_files_tables() {
        return task_files_tables;
    }

    public static void setTask_files_tables(List<TASK_FILES_TABLE> task_files_tables) {
        AddTaskActivity.task_files_tables = task_files_tables;
    }

    /**
     * @return file with extension .jpg
     * @throws IOException
     */
    public static File createImageFile() throws IOException {
        File direct = new File(Environment.getExternalStorageDirectory() + "/GeoTag");
        if (!direct.exists()) {
            File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + "/GeoTag/");
            wallpaperDirectory.mkdirs();
        }
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/GeoTag");

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        taskId = TaskTableRepo.getAllTasks(getApplicationContext()).size() + 1;
        imageCapture = (ImageView) findViewById(R.id.imageCapture);
        imageCapture.setOnClickListener(this);
        imageUpload = (ImageView) findViewById(R.id.imageUpload);
        imageUpload.setOnClickListener(this);
        textLocationName = (MyTextView) findViewById(R.id.textLocationName);
        editLocationName = (MyEditText) findViewById(R.id.editLocationName);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        textView = (MyTextView) findViewById(R.id.textView);
        task_files_tables.clear();
        imageAdapter = new ImageAdapter(task_files_tables, getApplicationContext());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(imageAdapter);
        if (task_files_tables.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }else{
            recyclerView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageCapture:
                if (Utils.isCameraPermission(getApplicationContext())) {
                    dispatchTakePictureIntent();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
                break;
            case R.id.imageUpload:
                locationName = editLocationName.getText().toString();
                if (TextUtils.isEmpty(locationName)) {
                    textLocationName.setError(getResources().getString(R.string.error_location_name));
                } else if (task_files_tables.size() == 0) {
                    Utils.showMessage("Please capture atleast 1 picture");

                } else {
                    setResult(RESULT_OK, new Intent().putExtra("taskid", taskId).putExtra("location_name", locationName));
                    finish();
                }

                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean flag = false;
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            flag = true;
        }
        if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            if (flag == true) {
                dispatchTakePictureIntent();
            } else {
                Utils.showMessage("You Need permission");
            }
        } else {
            if (flag == true) {
                Utils.showMessage("You need permssion");
            }
        }
    }

    /**
     * Called when user click on image capture
     */
    private void dispatchTakePictureIntent() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                imageUri = getUriForFile(AddTaskActivity.this, "rupvm.sminqtask.fileprovider", createImageFile());
//                imagePath = "content://rupvm.sminqtask/external_files/GeoTag/" + path;
            } else {
                imageUri = Uri.fromFile(createImageFile());
            }
            File file = new File(imageUri.getPath());
            fileName = file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, IMAGE_CAPTURE_REQUEST_CODE);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                TaskTableRepo.deleteTaskById(getApplicationContext(), taskId);
                TaskFilesTableRepo.getTaskTableDao(getApplicationContext()).queryBuilder().where(TASK_FILES_TABLEDao.Properties.TASKID.eq(taskId)).buildDelete();
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case IMAGE_CAPTURE_REQUEST_CODE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        task_files_tables.add(new TASK_FILES_TABLE(String.valueOf(imageUri)));

                    } else {
                        task_files_tables.add(new TASK_FILES_TABLE(fileName));
                    }
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imageAdapter.notifyDataSetChanged();
                        }
                    }, 2000);
                    if (task_files_tables.size() == 0) {
                        recyclerView.setVisibility(View.GONE);
                        textView.setVisibility(View.VISIBLE);
                    }else{
                        recyclerView.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
}
