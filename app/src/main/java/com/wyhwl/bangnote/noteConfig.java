package com.wyhwl.bangnote;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class noteConfig {
    public static final int     REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    public static boolean       m_bNoteModified = false;
    public static int           m_nNoteID = 10;
    public static int           m_nImagID = 10001;
    public static int           m_nImagIdStart = 10000;

    public static int           m_nTextColor = 0XFF000000;
    public static int           m_nTextSize = 24;
    public static int           m_nImageHeight = 300;

    public static String        m_strNoteTextPath = null;
    public static String        m_strNotePictPath = null;
    public static String        m_strNoteFile = null;

    public static noteTypeMng   m_noteTypeMng = null;
    public static int           m_nShowSecurity = 1;

    public static String        m_strTagNotePrev    = "[note";
    public static String        m_strTagNoteTitle   = "[noteTitle]";
    public static String        m_strTagNoteDate    = "[noteDate]";
    public static String        m_strTagNoteTime    = "[noteTime]";
    public static String        m_strTagNoteType    = "[noteType]";
    public static String        m_strTagNoteText    = "[noteText]";
    public static String        m_strTagNotePict    = "[notePict]";

    public static void initConfig(Context context) {
        File file = Environment.getExternalStorageDirectory();
        m_strNoteTextPath = file.getPath() + "/bangNote/text/";
        m_strNotePictPath = file.getPath() + "/bangNote/picture/";
        file = new File(file.getPath() + "/bangNote/");
        file.mkdir();
        file = new File(m_strNoteTextPath);
        file.mkdir();
        file = new File(m_strNotePictPath);
        file.mkdir();

        if (m_noteTypeMng == null)
            m_noteTypeMng = new noteTypeMng();
    }

    public static int       getNoteEditID () {
        return m_nNoteID++;
    }

    public static int       getImagViewID () {
        return m_nImagID++;
    }

    public static String    getNoteTextFile () {
        Date dateNow = new Date(System.currentTimeMillis());
        SimpleDateFormat fmtDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        m_strNoteFile = m_strNoteTextPath + "txt_" + fmtDate.format(dateNow) + ".bnt";
        return m_strNoteFile;
    }

    public static String    getNotePictFile () {
        Date dateNow = new Date(System.currentTimeMillis());
        SimpleDateFormat fmtDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        m_strNoteFile = m_strNotePictPath + "pic_" + fmtDate.format(dateNow) + ".bnp";
        return m_strNoteFile;
    }

    public static boolean CheckWritePermission (Activity activity, boolean bInit) {
        if (bInit) {
            //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(activity, "请开通文件读写权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
                }
                //申请权限
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);

            } else {
                //Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();
                return true;
            }
        } else {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "请开通文件读写权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }
}
