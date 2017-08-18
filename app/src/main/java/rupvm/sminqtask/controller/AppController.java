package rupvm.sminqtask.controller;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import rupvm.sminqtask.database.DaoMaster;
import rupvm.sminqtask.database.DaoSession;

import static com.android.volley.VolleyLog.TAG;

/**
 * Created by rup on 8/9/17.
 */

public class AppController extends Application {
    private static final java.lang.String DB_NAME = "TASKDATABASE";
    private static AppController appControllerInstance;
    private DaoSession daoSession;
    private RequestQueue mRequestQueue;

    /**
     * Gets the instance of AppController throughout the App
     *
     * @return AppController
     */
    public static AppController getInstance() {
        return appControllerInstance;
    }

    /**
     * Get the context from AppController   throughout the App
     *
     * @return Context
     */
    public static Context getAppContext() {
        return appControllerInstance.getApplicationContext();
    }

    /**
     * Gets the instance of AppController throughout the App(Sync)
     *
     * @return Application
     */
    public static synchronized AppController getInstanceSyn() {
        return appControllerInstance;
    }

    /**
     * Save to preferences
     *
     * @param context
     * @param preferenceName
     * @param preferenceValue
     */
    public static void saveToPreferences(Context context, String preferenceName, String preferenceValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    public static String getStringPreference(Context context, String key) {
        String value = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getString(key, null);
        }
        return value;
    }

    /**
     * Read from preferences
     *
     * @param context
     * @param preferenceName
     * @return
     */
    public static String readFromPreferences(Context context, String preferenceName) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return sharedPreferences.getString(preferenceName, "");
    }


    public static void clear(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * Get the Singulton object of RequestQueue
     *
     * @return RequestQueue
     */
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    /**
     * Add the any request into the queue
     *
     * @param req
     * @param tag
     * @param <T>
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    /**
     * CancelPendingRequests form the queue
     *
     * @param tag
     */

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        setupDatabase();
        appControllerInstance = this;
    }

    /**
     * Set up database once when load application
     * called when Application class call load inside @onCreate() method
     */
    private void setupDatabase() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, DB_NAME, null);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.setLockingEnabled(false);
        db.execSQL("PRAGMA read_uncommitted = true;");
        db.execSQL("PRAGMA synchronous=OFF");
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    /**
     * @return dao session at set up database time
     */
    public DaoSession getDaoSession() {
        return daoSession;
    }
}
