package rupvm.sminqtask.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import rupvm.sminqtask.R;
import rupvm.sminqtask.custom.MyTextView;
import rupvm.sminqtask.database.TASK_FILES_TABLE;
import rupvm.sminqtask.database.TASK_FILES_TABLEDao;
import rupvm.sminqtask.database.TASK_TABLE;
import rupvm.sminqtask.databaserepo.TaskFilesTableRepo;

/**
 * Created by rup on 8/9/17.
 */

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {
    private List<TASK_TABLE> task_tables;
    private Context context;


    public TaskListAdapter(List<TASK_TABLE> task_tables, Context context) {
        this.task_tables = task_tables;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_row, parent, false);
        return new ViewHolder(view);
    }

    public void addList(List<TASK_TABLE> task_tables) {
        this.task_tables = task_tables;
        notifyDataSetChanged();
    }

    public void clearList(){
        this.task_tables.clear();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TASK_TABLE task_table = task_tables.get(position);
        if (task_table.getUPLOADSTATUS() == 0) {
            Glide.with(context).load(R.drawable.ic_error_red_500_24dp).into(holder.imageUpload);
            holder.textUploadStatus.setText("Saved");

        } else if (task_table.getUPLOADSTATUS() == 1) {
            Glide.with(context).load(R.drawable.ic_done_all_green_500_24dp).into(holder.imageUpload);
            holder.textUploadStatus.setText("Uploaded");

        } else {
            Glide.with(context).asGif().load(R.drawable.syncronise).into(holder.imageUpload);
            holder.textUploadStatus.setText("In progress");

        }
        holder.textLocationName.setText("" + task_table.getLOCATIONNAME());
        holder.task_files_tables.clear();
        holder.task_files_tables.addAll(TaskFilesTableRepo.getTaskTableDao(context).queryBuilder().where(TASK_FILES_TABLEDao.Properties.TASKID.eq(task_table.getTASKID())).list());
        holder.imageAdapter.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return task_tables.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private MyTextView textLocationName;
        private MyTextView textUploadStatus;

        private RecyclerView recyclerView;
        private ImageAdapter imageAdapter;
        private ImageView imageUpload;
        private List<TASK_FILES_TABLE> task_files_tables = new ArrayList<>();

        public ViewHolder(View view) {
            super(view);
            imageUpload = (ImageView) view.findViewById(R.id.imageUpload);
            textLocationName = (MyTextView) view.findViewById(R.id.textLocationName);
            textUploadStatus = (MyTextView) view.findViewById(R.id.textUploadStatus);

            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
            imageAdapter = new ImageAdapter(task_files_tables, context);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(imageAdapter);
        }
    }
}
