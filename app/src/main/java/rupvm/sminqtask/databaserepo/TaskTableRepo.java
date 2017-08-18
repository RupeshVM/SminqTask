package rupvm.sminqtask.databaserepo;

import android.content.Context;

import java.util.List;

import rupvm.sminqtask.controller.AppController;
import rupvm.sminqtask.database.TASK_TABLE;
import rupvm.sminqtask.database.TASK_TABLEDao;

/**
 * Created by rup on 8/9/17.
 */

public class TaskTableRepo {


    /**
     * Insert or update data inside database
     * @param context
     * @param task_table
     */
    public static void insertOrUpdate(Context context, TASK_TABLE task_table) {
        getTaskTableDao(context).insertOrReplace(task_table);
    }

    /**
     * Clear all task from databse
     * @param context
     */
    public static void clearAllTask(Context context) {
        getTaskTableDao(context).deleteAll();
    }

    /**
     * Delete task by id
     * @param context
     * @param id primary key
     */
    public static void deleteTaskById(Context context, long id) {
        getTaskTableDao(context).delete(getTaskTableById(context, id));
    }

    /**
     * Get All tasks from database
     *
     * @param context
     * @return
     */
    public static List<TASK_TABLE> getAllTasks(Context context) {
        return getTaskTableDao(context).loadAll();
    }

    /**
     * Get task by id from database
     *
     * @param context
     * @param id      contains primary key
     * @return
     */
    public static TASK_TABLE getTaskTableById(Context context, Long id) {
        return getTaskTableDao(context).load(id);
    }

    /**
     * Get Dao session to get data
     *
     * @param context
     * @return
     */
    public static TASK_TABLEDao getTaskTableDao(Context context) {
        return ((AppController) context.getApplicationContext()).getDaoSession().getTASK_TABLEDao();
    }


}
