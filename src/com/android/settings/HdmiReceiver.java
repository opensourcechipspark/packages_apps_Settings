/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.os.RemoteException;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.preference.SeekBarPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;

import java.util.Map;

import java.io.*;
import android.os.SystemProperties;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemProperties;
import android.content.ContentResolver;
import java.io.RandomAccessFile;
import static android.provider.Settings.System.HDMI_LCD_TIMEOUT;
public class HdmiReceiver extends BroadcastReceiver{
    private static final String TAG = "HdmiReceiver";
    private Context mcontext;
    private final String HDMI_PLUG_ACTION = "android.intent.action.HDMI_PLUG";
    private final String BOOT_COMPLETED_ACTION = "android.intent.action.BOOT_COMPLETED";
    private File hdmiEnable = new File("/sys/class/display/HDMI/enable");
    private File hdmiMode = new File("/sys/class/display/HDMI/mode");
    private File hdmiScale = new File("/sys/class/display/HDMI/scale");
    private File vgaMode= new File("/sys/class/display/VGA/mode");

    private String[] hdmiResolutionArray = new String[] {
            "1920x1080p-60\n", "1920x1080p-50\n", "1280x720p-60\n", "1280x720p-50\n",
            "720x576p-50\n", "720x480p-60\n"
    };

    private String[] vgaResolutionArray = new String[] {
            "1920x1080p-60\n", "1680x1050p-60\n", "1440x900p-60\n", "1366x768p-60\n",
            "1280x1024p-60\n", "1280x720p-60\n",  "1024x768p-60\n"
    };

    private String[] hdmiEnableArray = new String[] {"0\n", "1\n"};
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	mcontext = context;
	String action = intent.getAction();
        if (action.equals(HDMI_PLUG_ACTION)) {
		SharedPreferences preferences = context.getSharedPreferences("Settings", context.MODE_PRIVATE);
		SharedPreferences preferences_scale = context.getSharedPreferences("HdmiSettings", Context.MODE_PRIVATE);
		int enable = preferences.getInt("enable", 1);
		int hdmiResolutionValue = preferences.getInt("resolution", 2);

		restorePerferenceValue(hdmiEnable, hdmiEnableArray, enable+1);
		restorePerferenceValue(hdmiMode, hdmiResolutionArray, hdmiResolutionValue);
		int scale = preferences_scale.getInt("scale_set",100);
		String[] hdmiScaleArray = new String[]{ String.valueOf(scale) + "\n" };
		restorePerferenceValue(hdmiScale, hdmiScaleArray, 1);

		if(getFBDualDisplayMode()==1){
		    int state=intent.getIntExtra("state", 1);
		    if(state==1){
			    SystemProperties.set("sys.hdmi_screen.scale",String.valueOf((char)scale));
		    }else{
			    SystemProperties.set("sys.hdmi_screen.scale",String.valueOf((char)100)); 
		    }
		}
		if(getFBDualDisplayMode()!=0){
			TurnonScreen();
		}
	}
    }
    
    private void TurnonScreen(){
	//boolean ff = SystemProperties.getBoolean("persist.sys.hdmi_screen", false);
	ContentResolver resolver = mcontext.getContentResolver();
	try {
	      int brightness = Settings.System.getInt(resolver,Settings.System.SCREEN_BRIGHTNESS, 102);
	      IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
	      if (power != null) {
		      power.setTemporaryScreenBrightnessSettingOverride(brightness);
	      }
	} catch (Exception e) {
		Log.e(TAG, "Exception"+e);
	}
    }

    private int getFBDualDisplayMode(){
       int mode = 0;
       File DualModeFile = new File("/sys/class/graphics/fb0/dual_mode");

       if(DualModeFile.exists()){
	       try {
		       byte[] buf = new byte[10];
		       int len = 0;
		       RandomAccessFile rdf = new RandomAccessFile(DualModeFile, "r");
		       len = rdf.read(buf);
		       String modeStr = new String(buf,0,1);
		       mode = Integer.valueOf(modeStr);
	       } catch (IOException re) {
		       Log.e(TAG, "IO Exception");
	       } catch (NumberFormatException re) {
		       Log.e(TAG, "NumberFormatException");
	       }
       }
       return mode;
    }


    public void restorePerferenceValue(File file, String[] resolution, int value) {
        if (file.exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                OutputStreamWriter outputWrite = new OutputStreamWriter(fos);
                PrintWriter      print = new PrintWriter(outputWrite);

                print.print(resolution[value-1]);

             Log.d(TAG, "writeResolutionValue: " + "file=" + file.getAbsolutePath() + ", value:" + (value-1) + ",reso:" + resolution[value-1]);
                print.flush();
                fos.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }else {
                Log.d(TAG, "This device do not support vga output!");
        }
    }

}
