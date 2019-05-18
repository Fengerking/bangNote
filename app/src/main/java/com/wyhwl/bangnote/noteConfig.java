package com.wyhwl.bangnote;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class noteConfig {
    public static int           m_nNoteID = 10;
    public static int           m_nImagID = 10001;
    public static int           m_nImagIdStart = 10000;

    public static int           m_nTextColor = 0XFF9D9D9D;

    public static int           m_nTextSize = 24;
    public static int           m_nImageHeight = 300;

    public static String        m_strNoteTextPath = null;
    public static String        m_strNotePictPath = null;
    public static String        m_strNoteFile = null;

    public static List<String>  m_lstNoteType = null;
    public static String        m_strNoteType = null;

    public static String        m_strTagNotePrev    = "[note";
    public static String        m_strTagNoteTitle   = "[noteTitle]";
    public static String        m_strTagNoteDate    = "[noteDate]";
    public static String        m_strTagNoteTime    = "[noteTime]";
    public static String        m_strTagNoteType    = "[noteType]";
    public static String        m_strTagNoteText    = "[noteText]";
    public static String        m_strTagNotePict    = "[notePict]";

    public static void initConfig(Context context) {
        m_strNoteTextPath = "/sdcard/bangNote/text/";
        m_strNotePictPath = "/sdcard/bangNote/picture/";
        File file = new File(m_strNoteTextPath);
        file.mkdir();
        file = new File(m_strNotePictPath);
        file.mkdir();

        m_lstNoteType = new ArrayList<String>();
        m_lstNoteType.add("默认笔记");
        m_lstNoteType.add("学习日记");
        m_lstNoteType.add("旅游日记");
        m_lstNoteType.add("心灵鸡汤");
        m_strNoteType = "默认笔记";
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
}
