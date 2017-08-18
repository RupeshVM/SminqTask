package com.sminqtask;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Schema;

public class TaskDatabase {
    private static final String DB_PACKAGE_NAME = "rupvm.sminqtask.database";
    private static final int DB_VERSION = 1;
    private static final String TASK_TABLE = "TASK_TABLE";
    private static final String TASK_FILES_TABLE = "TASK_FILES_TABLE";

    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(DB_VERSION, DB_PACKAGE_NAME);
        addTaskTable(schema);
        addFilesTable(schema);
        new DaoGenerator().generateAll(schema, "./app/src/main/java");
    }

    /**
     * Add files url when user is offline
     *
     * @param schema
     */
    private static void addFilesTable(Schema schema) {
        Entity entity = schema.addEntity(TASK_FILES_TABLE);
        entity.addLongProperty("FILEID").primaryKey();
        entity.addLongProperty("TASKID");
        entity.addStringProperty("FILEURL");
        entity.addIntProperty("UPLOADSTATUS");//0 FOR NOT UPLOAD  // 1 FOR UPLOADED //2 FOR IN PROGRESS
    }

    /**
     * Add task table field when user is offline
     *
     * @param schema
     */
    private static void addTaskTable(Schema schema) {
        Entity entity = schema.addEntity(TASK_TABLE);
        entity.addLongProperty("TASKID").primaryKey();
        entity.addStringProperty("LOCATIONNAME");
        entity.addIntProperty("UPLOADSTATUS");//0 NOT UPLOAD  1 FOR UPLOADED
    }

}
