/*
Name of the module : MainActivity.java

Date on which the module was created: 03/4/2017

Author’s name: Shivam Gupta

Modification history :  By Shubham Singhal 06/04/2017
                        By Roopansh Bansal 11/04/2017

Synopsis of the module : MainActivity File which is executed when the app starts.

Different functions supported, along with their input/output parameters.

Global variables accessed/modified by the module.
*/

//Various imported libraries 
package com.tzutalin.dlibtest;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.dexafree.materialList.view.MaterialListView;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.PedestrianDet;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import hugo.weaving.DebugLog;

@EActivity(R.layout.activity_main)

//Defining the main activity
public class MainActivity extends AppCompatActivity {
    private static final int RESULT_LOAD_IMG = 1;
    private static final int REQUEST_CODE_PERMISSION = 2;

    private static final String TAG = "MainActivity";

    // Ask Permissions to read & Write in Storage
    private static String[] PERMISSIONS_REQ = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    protected String mTestImgPath;

    // Linking UserInterface elements with objects in java
    @ViewById(R.id.material_listview)
    protected MaterialListView mListView;
    @ViewById(R.id.fab_cam)
    protected FloatingActionButton mFabCamActionBt;
    @ViewById(R.id.toolbar)
    protected Toolbar mToolbar;

    // It stores the detected faces and persons
    FaceDet mFaceDet;
    PedestrianDet mPersonDet;

    // Oncreate runs when activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	/**
         * it loads the saved instance to app and link listview to backend
         * adds functionality to mToolbar
         */
        super.onCreate(savedInstanceState);
        mListView = (MaterialListView) findViewById(R.id.material_listview);
        setSupportActionBar(mToolbar);

        // to check read & write permissions in external storage
        isExternalStorageWritable();
        isExternalStorageReadable();

        // For API 23+ we need to request the read/write permissions even if they are already in manifest.
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;

        if (currentapiVersion >= Build.VERSION_CODES.M) {
            verifyPermissions(this);
        }
    }

    /**
     * It sets title for toolbar 
     * Display a toast to provide necessary steps to use app to user
     */
    @AfterViews
    protected void setupUI() {
        mToolbar.setTitle(getString(R.string.app_name));
        Toast.makeText(MainActivity.this, getString(R.string.description_info), Toast.LENGTH_LONG).show();
    }

    // It calls CameraActivity.java whenever we click on the camera button in UI
    @Click({R.id.fab_cam})
    protected void launchCameraPreview() {
        startActivity(new Intent(this, CameraActivity.class));
    }

    /**
     * Checks if the app has permission to write to device storage or open camera
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     */
    @DebugLog
    private static boolean verifyPermissions(Activity activity) {
        // Check for permission to Write,Read in ExternalStorage and to use camera
        int write_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read_persmission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int camera_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        // IF any of permission is not provided by user,then it prompt the user for permission
        if (write_permission != PackageManager.PERMISSION_GRANTED ||
                read_persmission != PackageManager.PERMISSION_GRANTED ||
                camera_permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_REQ,
                    REQUEST_CODE_PERMISSION
            );
            return false;
        } else {
            return true;
        }
    }

    /* Checks if external storage is available for read and write */
    @DebugLog
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to read */
    @DebugLog
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
