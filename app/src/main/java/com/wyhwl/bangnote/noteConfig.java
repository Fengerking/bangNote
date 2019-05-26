package com.wyhwl.bangnote;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;
import android.view.View;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Locale;

public class noteConfig {
    public static int           m_nItemTypeText = 0;
    public static int           m_nItemTypePict = 1;
    public static int           m_nItemTypeAudo = 2;
    public static int           m_nItemTypeVido = 3;

    public static noteListAdapter     m_lstData = null;
    public static boolean       m_bNoteModified = false;
    public static int           m_nNoteID = 10;
    public static int           m_nImagID = 10001;
    public static int           m_nAudoID = 20001;
    public static int           m_nVidoID = 30001;

    public static int           m_nTextColor = 0XFFCCCCCC;
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
    public static String        m_strTagNoteCity    = "[noteCity]";
    public static String        m_strTagNoteWeat    = "[noteWeat]";
    public static String        m_strTagNoteText    = "[noteText]";
    public static String        m_strTagNotePict    = "[notePict]";
    public static String        m_strTagNoteAudo    = "[noteAudo]";
    public static String        m_strTagNoteVido    = "[noteVido]";

    public static String[]      m_lstWeekDays       = {"日", "一", "二", "三", "四", "五", "六"};

    public static String        m_strCityName       = "未知";
    public static String        m_strWeather        = "未知";

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
        if (m_lstData == null) {
            m_lstData = new noteListAdapter(context);
        }

        //baseSystemUtil.getCNBylocation(context);
    }

    public static int       getNoteEditID () {
        return m_nNoteID++;
    }
    public static int       getImagViewID () {
        return m_nImagID++;
    }
    public static int       getAudoViewID () {
        return m_nAudoID++;
    }
    public static int       getVidoViewID () {
        return m_nVidoID++;
    }

    public static int       getNoteviewType (View vwNote) {
        int nID = vwNote.getId();
        if (nID < 10000)
            return m_nItemTypeText;
        else if (nID < 20000)
            return m_nItemTypePict;
        else if (nID < 30000)
            return m_nItemTypeAudo;
        else if (nID < 40000)
            return m_nItemTypeVido;
        else
            return -1;
    }
    public static int       getNoteviewType (int nID) {
        if (nID < 10000)
            return m_nItemTypeText;
        else if (nID < 20000)
            return m_nItemTypePict;
        else if (nID < 30000)
            return m_nItemTypeAudo;
        else if (nID < 40000)
            return m_nItemTypeVido;
        else
            return -1;
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
        m_strNoteFile = m_strNotePath + "aud_" + fmtDate.format(dateNow) + ".bna";
        return m_strNoteFile;
    }

    public static Date parseServerTime(String serverTime, String format) {
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINESE);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        Date date = null;
        try {
            date = sdf.parse(serverTime);
        } catch (Exception e) {
        }
        return date;
    }

    public static String getWeekDay (String serverTime) {
        String strFormat = "yyyy-MM-dd";
        Date date = parseServerTime (serverTime, strFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayIndex = calendar.get(Calendar.DAY_OF_WEEK);
        String strWeekDay = "周" + m_lstWeekDays[dayIndex-1];
        return strWeekDay;
    }
}
