package com.wyhwl.bangnote;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class noteConfig {
    public static int   m_nNoteID = 1;
    public static int   m_nImagID = 10000;

    public static int   m_nTextColor = 0XFF9D9D9D;

    public static int   m_nTextSize = 24;


    public static void initConfig(Context context) {

    }

    public static int   getNoteEditID () {
        return m_nNoteID++;
    }

    public static int   getImagViewID () {
        return m_nImagID++;
    }
}
