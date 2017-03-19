package hu.rics.videostreamandroid;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hu.rics.camera1util.LibraryInfo.TAG;

/**
 * Created by rics on 2017.03.01..
 */

public class PermissionHandler {
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 123;
    private Activity activity;
    private boolean hasRights = false;
    List<String> allPermissions;
    List<String> permissionsMissing;

    public PermissionHandler(Activity activity) {
        this.activity = activity;
    }

    public boolean requestPermission(String[] permissionsNeeded) {
        // permission check (https://inthecheesefactory.com/blog/things-you-need-to-know-about-android-m-permission-developer-edition/en)
        allPermissions = new ArrayList<>(Arrays.asList(permissionsNeeded));
        permissionsMissing = new ArrayList<>();

        final List<String> permissionsHave = new ArrayList<>();
        for( String permission : allPermissions ) {
            if (!addPermission(permissionsHave, permission)) {
                Log.i(TAG,"permissions missing:" + permission);
                permissionsMissing.add(permission);
            }
        }
        Log.i(TAG,"permissions - current: " + permissionsHave.size() + " missing:" + permissionsMissing.size());
        if (permissionsHave.size() > 0) {
            if (permissionsMissing.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsMissing.get(0);
                for (int i = 1; i < permissionsMissing.size(); i++)
                    message = message + ", " + permissionsMissing.get(i);
                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity,permissionsHave.toArray(new String[permissionsHave.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return false;
            }
            ActivityCompat.requestPermissions(activity,permissionsHave.toArray(new String[permissionsHave.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return false;
        } else {
            hasRights = true;
        }
        return true;
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(activity,permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,permission)) {
                return false;
            }
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissionsRequested, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
            {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                for(String permission : allPermissions) {
                    perms.put(permission, PackageManager.PERMISSION_GRANTED);
                }
                // Fill with results
                for (int i = 0; i < permissionsRequested.length; i++) {
                    Log.i(TAG,"i:" + i + ":" + permissionsRequested[i] + ":" + grantResults[i] );
                    perms.put(permissionsRequested[i], grantResults[i]);
                }
                if( !perms.containsValue(PackageManager.PERMISSION_DENIED) ) {
                    // All PermissionHandler Granted
                    hasRights = true;
                } else {
                    // Permission Denied
                    Toast.makeText(activity, "Some Permission is Denied", Toast.LENGTH_SHORT)
                            .show();
                }
            }
            break;
        }
    }

    public boolean hasRights() {
        return hasRights;
    }
}
