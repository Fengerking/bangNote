package com.wyhwl.bangnote.base;

import android.content.Context;

import java.lang.ref.WeakReference;

public class noteAliyunOSS {
    private Context     m_context       = null;
    private long 		m_lNativeObj    = 0;
    private Object		m_pObjOSS       = null;

    public noteAliyunOSS (Context context) {
        m_context = context;
        m_pObjOSS = new WeakReference<noteAliyunOSS>(this);
        m_lNativeObj = initOSS(m_pObjOSS);
    }

    private static void postEventFromNative(Object oss_ref, int what, int ext1, int ext2, Object obj) {
        noteAliyunOSS oss = (noteAliyunOSS)((WeakReference)oss_ref).get();
        if (oss == null)
            return;

    }

    public native long      initOSS(Object oss);
    public native long      uninitOSS(long oss);
    public native String    getFileList(long oss, String strUser);
    public native int       uploadFile(long oss, String strFile);
    public native int       downloadFile(long oss, String strFile);

    static {
        System.loadLibrary("native-lib");
    }
}
