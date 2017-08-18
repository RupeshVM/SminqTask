package rupvm.sminqtask.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import rupvm.sminqtask.R;
import rupvm.sminqtask.database.TASK_FILES_TABLE;

/**
 * Created by rup on 8/15/17.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private List<TASK_FILES_TABLE> task_files_tables;
    private Context context;

    public ImageAdapter(List<TASK_FILES_TABLE> task_files_tables, Context context) {
        this.task_files_tables = task_files_tables;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TASK_FILES_TABLE task_files_table = task_files_tables.get(position);
//        holder.imageView.setImageURI(Uri.fromFile(new File(task_files_table.getFILEURL())));
        Glide.with(context).load(task_files_table.getFILEURL()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return task_files_tables.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.imageView);
        }
    }
}
