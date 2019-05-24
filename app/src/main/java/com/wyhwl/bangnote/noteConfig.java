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
    public static boolean       m_bNoteModified = false;
    public static int           m_nNoteID = 10;
    public static int           m_nImagID = 10001;
    public static int           m_nAudoID = 20001;
    public static int           m_nVidoID = 30001;
    public static int           m_nImagIdStart = 10000;

    public static int           m_nTextColor = 0XFF000000;
    public static int           m_nTextSize = 24;
    public static int           m_nImageHeight = 300;
    public static int           m_nAudioHeight = 300;
    public static int           m_nVideoHeight = 300;

    public static String        m_strRootPath = null;
    public static String        m_strNotePath = null;
    public static String        m_strBackPath = null;
    public static String        m_strNoteFile = null;

    public static noteTypeMng   m_noteTypeMng = null;
    public static int           m_nShowSecurity = 0;
    public static int           m_nMoveCount    = 0;
    public static long          m_nMoveLastTime = 0;
    public static long          m_nMoveNeedTime = 5;

    public static String        m_strTagNotePrev    = "[note";
    public static String        m_strTagNoteTitle   = "[noteTitle]";
    public static String        m_strTagNoteDate    = "[noteDate]";
    public static String        m_strTagNoteTime    = "[noteTime]";
    public static String        m_strTagNoteType    = "[noteType]";
    public static String        m_strTagNoteText    = "[noteText]";
    public static String        m_strTagNotePict    = "[notePict]";
    public static String        m_strTagNoteAudo    = "[noteAudo]";
    public static String        m_strTagNoteVido    = "[noteVido]";

    public static void initConfig(Context context) {
        File file = Environment.getExternalStorageDirectory();
        m_strRootPath = file.getPath() + "/bangNote/";
        m_strNotePath = file.getPath() + "/bangNote/.data/";
        m_strBackPath = file.getPath() + "/bangNote/.backup/";
        file = new File(m_strRootPath);
        file.mkdir();
        file = new File(m_strNotePath);
        file.mkdir();
        file = new File(m_strBackPath);
        file.mkdir();

        if (m_noteTypeMng == null) {
            m_noteTypeMng = new noteTypeMng();
        }
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
        m_strNoteFile = m_strNotePath + "txt_" + fmtDate.format(dateNow) + ".bnt";
        return m_strNoteFile;
    }

    public static String    getNotePictFile () {
        Date dateNow = new Date(System.currentTimeMillis());
        SimpleDateFormat fmtDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        m_strNoteFile = m_strNotePath + "pic_" + fmtDate.format(dateNow) + ".bnp";
        return m_strNoteFile;
    }

    public static String    getNoteZipFile () {
        Date dateNow = new Date(System.currentTimeMillis());
        SimpleDateFormat fmtDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        m_strNoteFile = m_strBackPath + "bck_" + fmtDate.format(dateNow) + ".bnz";
        return m_strNoteFile;
    }

    public static String    getNoteAudioFile () {
        Date dateNow = new Date(System.currentTimeMillis());
        SimpleDateFormat fmtDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        m_strNoteFile = m_strBackPath + "bck_" + fmtDate.format(dateNow) + ".bna";
        return m_strNoteFile;
    }
}
