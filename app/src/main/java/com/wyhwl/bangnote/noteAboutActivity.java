package com.wyhwl.bangnote;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ProgressBar;

import android.os.Build;

import com.wyhwl.bangnote.base.noteAliyunOSS;
import com.wyhwl.bangnote.base.noteConfig;

import java.io.File;

public class noteAboutActivity extends noteBaseActivity
                                    implements View.OnClickListener,
                                        noteAliyunOSS.onOssProgressListener  {
    private TextView        m_txtFeatureInfo = null;
    private TextView        m_txtContactInfo = null;
    private String          m_strVersion = null;
    private int             m_nVerNum = 0;

    private noteAliyunOSS   m_noteOSS       = null;
    private String          m_strApp        = "app";
    private String          m_strAPKFile    = null;
    private ProgressBar     m_prgDownload   = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_about);

        try {
            m_strVersion = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            m_nVerNum = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }
        initViews ();
    }

    protected void onStop () {
        super.onStop();
        if (m_noteOSS != null) {
            m_noteOSS.uninitOSS();
            m_noteOSS = null;
        }
    }

    private void initViews () {
        ((ImageButton)findViewById(R.id.imbBack)).setOnClickListener(this);
        ((Button)findViewById(R.id.btnCheckUpdate)).setOnClickListener(this);

        m_prgDownload = (ProgressBar)findViewById(R.id.pgbDownload);
        m_prgDownload.setVisibility(View.INVISIBLE);

        m_txtFeatureInfo = (TextView)findViewById(R.id.txtFeatureDetail);
        m_txtContactInfo = (TextView)findViewById(R.id.txtContactInfo);

        m_txtFeatureInfo.setText("1、可以方便插入录音，音乐。\n" +
                                 "2、可以方便插入图片，相机。\n" +
                                 "3、远程云备份，不同设备之间同步。\n" +
                                 "4、支持私密笔记。\n" +
                                 "5、方便搜索，阅读。\n");

        m_txtContactInfo.setText ("1、邮件：fengernote@163.com \n" +
                                  "2、QQ：  2598425114 \n");

        ((TextView)findViewById(R.id.txtVersion)).setText(m_strVersion);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imbBack:
                finish();
                break;

            case R.id.btnCheckUpdate:
                checkNewVersion();
                break;
        }
    }

    private void checkNewVersion () {
        if (m_noteOSS == null) {
            m_noteOSS = new noteAliyunOSS(this);
            m_noteOSS.setOssProgListener(this);
        }
        if (m_noteOSS.m_lstFileList.size() <= 0)
            m_noteOSS.getFileList(m_strApp);
        if (m_noteOSS.m_lstFileList.size() <= 0) {
            showMsgDlg ("检查结果", "没有发现新版本。", false);
            return;
        }

        // The file name should be bangnote_vxx.apk
        for (int i = 0; i < m_noteOSS.m_lstFileList.size(); i++) {
            m_strAPKFile = m_noteOSS.m_lstFileList.get(i);
            if (m_strAPKFile.length() > 12)
                break;
        }
        if (m_strAPKFile.length() < 12) {
            showMsgDlg ("检查结果", "没有发现新版本。", false);
            return;
        }

        int     nDot = m_strAPKFile.indexOf('.');
        String  strVer = m_strAPKFile.substring(10, nDot);
        int     nVer = Integer.parseInt(strVer);
        if (m_nVerNum >= nVer) {
            showMsgDlg ("检查结果", "你已经安装了最新版本！", false);
            return;
        }

        showMsgDlg ("检查结果", "发行新版本，确认要更新吗？", true);
    }

    public void onOssProgress (int nProgress, int nTotal){
        int nPercent = nProgress * 100 / nTotal;
        String strInfo = String.format("Prog: %d  Total: %d  Percent: %d", nProgress, nTotal, nPercent);
        Log.v ("noteBackup", strInfo);
        Message msg = m_msgHandler.obtainMessage(MSG_DOWNLOAD_APK_PROCSSS, nPercent, 0, null);
        msg.sendToTarget();
    }

    private void downlaodThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                m_noteOSS.downlaodFile(m_strAPKFile, noteConfig.m_strRootPath);
                Message msg = m_msgHandler.obtainMessage(MSG_DOWNLOAD_APK_FINISH, 0, 0, null);
                msg.sendToTarget();
            }
        }).start();
    }

    protected void onMsgHandler (Message msg) {
        if (msg.what == MSG_DOWNLOAD_APK_PROCSSS) {
            if (m_prgDownload.getVisibility() == View.INVISIBLE)
                m_prgDownload.setVisibility(View.VISIBLE);
            m_prgDownload.setProgress(msg.arg1);
        } else if (msg.what == MSG_DOWNLOAD_APK_FINISH) {
            showWaitDialog(null,false);
            m_prgDownload.setVisibility(View.INVISIBLE);
            String strApkFile = noteConfig.m_strRootPath + m_strAPKFile;
            installApk (strApkFile);
        }
    }

    protected void onDlgOK () {
        m_prgDownload.setProgress(0);
        downlaodThread();
        showWaitDialog("正在下载安装文件。。。",true);
    }

    protected void installApk(String strAPK) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Android N 写法
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(noteAboutActivity.this,
                                                        BuildConfig.APPLICATION_ID+".fileProvider",
                                                            new File (strAPK));
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(new File(strAPK)), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(intent);
    }

}
