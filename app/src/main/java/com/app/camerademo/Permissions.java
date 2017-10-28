package com.app.camerademo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by leidyzulu on 26/10/17.
 */

public class Permissions {
    public static boolean isGrantedPermissions(Context context, String permissionType) {
        int permission = ActivityCompat.checkSelfPermission(context, permissionType);

        return permission == PackageManager.PERMISSION_GRANTED;
    }

    public static void verifyPermissions(Activity activity, String[] permissionsType) {
        ActivityCompat.requestPermissions(activity, permissionsType, Constants.REQUEST_CODE_PERMISSION);
    }
}
