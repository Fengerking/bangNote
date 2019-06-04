package com.wyhwl.bangnote.base;

import android.content.Context;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class noteAliyunOSS {
    private final static int    OSS_MSG_FILE_LIST       = 0X10000001;
    private final static int    OSS_MSG_FILE_PROG       = 0X10000010;

    private Context     m_context       = null;
    private long 		m_lNativeObj    = 0;
    private Object		m_pObjOSS       = null;

    public ArrayList<String>        m_lstFileList = null;
    private onOssProgressListener   m_prgListener = null;

    public interface onOssProgressListener {
        public void onOssProgress (int nProgress, int nTotal);
    }

    public void setOssProgListener (onOssProgressListener listener) {
        m_prgListener = listener;
    }

    public noteAliyunOSS (Context context) {
        m_context = context;
        m_pObjOSS = new WeakReference<noteAliyunOSS>(this);
        m_lNativeObj = initOSS(m_pObjOSS);

        m_lstFileList = new ArrayList<String>();
    }

    public void uninitOSS () {
        if (m_lNativeObj != 0) {
            uninitOSS (m_lNativeObj);
            m_lNativeObj = 0;
        }
    }

    public static void postEventFromNative(Object oss_ref, int what, int ext1, int ext2, Object obj) {
        noteAliyunOSS oss = (noteAliyunOSS)((WeakReference)oss_ref).get();
        if (oss == null)
            return;
        switch (what) {
            case OSS_MSG_FILE_LIST:
                String strInfo = (String)obj;
                int nPos = strInfo.indexOf('|');
                String strFile = strInfo.substring(0, nPos);
                nPos = strFile.lastIndexOf(File.separator);
                strFile = strFile.substring(nPos+1);
                oss.m_lstFileList.add(strFile);
                break;

            case OSS_MSG_FILE_PROG:
                if (oss.m_prgListener != null)
                    oss.m_prgListener.onOssProgress(ext1, ext2);
                break;

            default:
                break;
        }
    }

    public int  getFileList (String strUser) {
        m_lstFileList.clear();
        return getFileList (m_lNativeObj, strUser);
    }

    public int  uploadFile (String strFile) {
        return uploadFile (m_lNativeObj, strFile);
    }

    public int  downlaodFile (String strFile, String strPath) {
        return downloadFile (m_lNativeObj, strFile, strPath);
    }

    public native long      initOSS(Object oss);
    public native long      uninitOSS(long oss);
    public native int       getFileList(long oss, String strUser);
    public native int       uploadFile(long oss, String strFile);
    public native int       downloadFile(long oss, String strFile, String strPath);

    static {
        System.loadLibrary("native-lib");
    }
}
