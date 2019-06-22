package com.wyhwl.bangnote;

import android.app.Application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.tencent.bugly.crashreport.CrashReport;
import com.wyhwl.bangnote.base.noteConfig;

public class noteApplication extends Application {
    private static  noteApplication m_hInstance;
    public String                   m_strLockKey = "";
    public boolean                  m_isUnlock = false;

    @Override
    public void onCreate() {
       super.onCreate();
       m_hInstance = this;

       //closeAndroidPDialog ();

       CrashReport.initCrashReport(getApplicationContext(), "6c790217f8", false);
    }

    public static noteApplication getInstance(){
        return m_hInstance;
    }

    public void readLockKey (String strKeyFile) {
        if (m_strLockKey.length() > 1)
            return;
        File fileKey = new File (strKeyFile);
        if (!fileKey.exists ())
            return;
        try {
            FileInputStream fis = new FileInputStream (strKeyFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            m_strLockKey = br.readLine();
            fis.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeLockKey (String strKeyFile) {
        if (m_strLockKey.length() < 1)
            return;
        try {
            FileOutputStream fos = new FileOutputStream (strKeyFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write((m_strLockKey +"\n\n").toCharArray());
            bw.flush();
            fos.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void closeAndroidPDialog(){
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
