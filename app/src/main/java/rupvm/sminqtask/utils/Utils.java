package rupvm.sminqtask.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import rupvm.sminqtask.controller.AppController;

/**
 * Created by rup on 8/15/17.
 */

public class Utils {

    private static String mCurrentPhotoPath;



    /**
     * Empty string
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }

        str = str.trim();
        return TextUtils.isEmpty(str);
    }



    /**
     * Show toast message to user
     *
     * @param msg
     */
    public static void showMessage(String msg) {
        if (AppController.getAppContext() != null && !TextUtils.isEmpty(msg))
            Toast.makeText(AppController.getAppContext(), msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Check if camera permission allow or not
     *
     * @param context
     * @return
     */
    public static Boolean isCameraPermission(Context context) {
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    /**
     * Check if Location permssion allowed or not
     * @param context
     * @return
     */
    public static Boolean isLocationPermission(Context context) {
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    public static boolean isNetworkAvailable() {
        if (AppController.getAppContext() == null) {
            return false;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) AppController.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        // if no network is available networkInfo will be null, otherwise check if we are connected

        try {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo == null) return false;
            if (activeNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                return true;
            }
        } catch (Exception e) {
//            LogUtils.printErrorMessage(LogUtils.class.getSimpleName(), e);
        }
        return false;
    }



}
