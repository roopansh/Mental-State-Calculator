/*
Name of the module : CameraActivity.java

Date on which the module was created: 03/4/2017

Author’s name: By Shubham Singhal 03/04/2017

Modification history :  By  Roopansh Bansal 05/04/2017

Synopsis of the module : executed when user clicks on camera button in home page of app.

Different functions supported, along with their input/output parameters.

Global variables accessed/modified by the module.
*/
package com.tzutalin.dlibtest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.Toast;

public class CameraActivity extends Activity {

    private static int OVERLAY_PERMISSION_REQ_CODE = 1;

    //Oncreate runs when the activity is created
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //keeping screen always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //setting the view in UI
        setContentView(R.layout.activity_camera);

        //Checking for permissions reqd for the app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this.getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            }
        }

        //on older versions of android directly start camera. CameraConnectionFragment is called which controls the camera
        if (null == savedInstanceState) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, CameraConnectionFragment.newInstance())
                    .commit();
        }
    }

    //After "asking permissions" activity is over. Alert if permission not acquired otherwise start the CameraConnectionFragment
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this.getApplicationContext())) {
                    Toast.makeText(CameraActivity.this, "CameraActivity\", \"SYSTEM_ALERT_WINDOW, permission not granted...", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
            }
        }
    }
}