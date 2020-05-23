package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class PermissionUtils {
    public static void verify(Activity activity){
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            },1);
        }
    }
}
