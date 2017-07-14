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

import android.app.Application;
import android.util.Log;

import timber.log.Timber;

public class DlibDemoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            //Timber.plant(new DebugLogFileTree(Environment.getExternalStorageDirectory().toString()));
        } else {
            Timber.plant(new ReleaseTree());
        }
    }

    /**
     * A tree which logs important information
     */
    private static class ReleaseTree extends Timber.DebugTree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }
            super.log(priority, tag, message, t);
        }
    }
}
