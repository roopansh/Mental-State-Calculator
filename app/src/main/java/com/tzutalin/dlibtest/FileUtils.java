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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Tzutalin on 2016/3/30.
 */
public class FileUtils {
    @NonNull
    public static final void copyFileFromRawToOthers(@NonNull final Context context, @RawRes int id, @NonNull final String targetPath) {
        InputStream in = context.getResources().openRawResource(id);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(targetPath);
            byte[] buff = new byte[1024];
            int read = 0;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
