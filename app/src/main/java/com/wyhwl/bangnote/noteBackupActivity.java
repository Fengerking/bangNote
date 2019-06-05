package com.wyhwl.bangnote;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.wyhwl.bangnote.base.*;

import java.io.File;


public class noteBackupActivity extends AppCompatActivity
                                implements View.OnClickListener,
                                            noteAliyunOSS.onOssProgressListener {
    private noteAliyunOSS       m_noteOSS = null;
    private String              m_strUserID = null;

    private ImageButton         m_btnWechat = null;
    private ImageButton         m_btnBackup = null;
    private ImageButton         m_btnRestore = null;

    private ProgressBar         m_prgBackup = null;
    private ProgressBar         m_prgRestore = null;

    private boolean             m_bUploading = true;
    private int                 m_nFileCount = 0;
    private int                 m_nFileIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_backup);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        m_strUserID = "jin_bangfei-";
        if (Build.BRAND.compareTo("google") == 0)
            m_strUserID += Build.BRAND + "_x86";
        else
            m_strUserID += Build.BRAND + "_" + Build.MODEL;
        m_noteOSS = new noteAliyunOSS (this);
        m_noteOSS.setOssProgListener(this);
        initViews();
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

        m_btnWechat = (ImageButton)findViewById(R.id.btnWechat);
        m_btnBackup = (ImageButton)findViewById(R.id.btnBackup);
        m_btnRestore = (ImageButton)findViewById(R.id.btnRestore);
        m_btnWechat.setOnClickListener(this);
        m_btnBackup.setOnClickListener(this);
        m_btnRestore.setOnClickListener(this);

        m_prgBackup = (ProgressBar)findViewById(R.id.pgbBackup);
        m_prgRestore = (ProgressBar)findViewById(R.id.pgbRestore);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imbBack:
                finish();
                break;

            case R.id.btnWechat:
                wechatLogin ();
                break;

            case R.id.btnBackup:
                noteBackup();
                break;

            case R.id.btnRestore:
                noteRestore ();
                break;

            default:
                break;
        }
    }

    private void noteBackup () {
        m_prgBackup.setProgress(0);
        m_prgRestore.setProgress(0);
        m_bUploading = true;

        ProgressDialog dlgWait = new ProgressDialog(this);
        dlgWait.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dlgWait.setMessage("wait...");
        dlgWait.setIndeterminate(true);
        dlgWait.setCancelable(false);

        noteBackupRestore noteBackup = new noteBackupRestore(this);
        noteBackup.backupNote();

        if (m_noteOSS.m_lstFileList.size() <= 0)
            m_noteOSS.getFileList(m_strUserID);

        File fPath = new File(noteConfig.m_strBackPath);
        File[] fList = fPath.listFiles();
        if (fList != null) {
            m_nFileIndex = 0;
            m_nFileCount = fList.length;
            for (int i = 0; i < fList.length; i++) {
                File file = fList[i];
                if (file.isHidden())
                    continue;
                if (file.isDirectory())
                    continue;
                if (findInList (file.getPath()))
                    continue;
                m_noteOSS.uploadFile(file.getPath());
                m_nFileIndex = i;
            }
        }
        dlgWait.cancel();
        showMsgDlg ("远程备份", "远程备份成功！");

        m_prgBackup.setProgress(0);
        m_prgRestore.setProgress(0);
    }

    private void noteRestore () {
        m_prgBackup.setProgress(0);
        m_prgRestore.setProgress(0);
        m_bUploading = false;

        ProgressDialog dlgWait = new ProgressDialog(this);
        dlgWait.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dlgWait.setMessage("wait...");
        dlgWait.setIndeterminate(true);
        dlgWait.setCancelable(false);

        if (m_noteOSS.m_lstFileList.size() <= 0)
            m_noteOSS.getFileList(m_strUserID);

        String strFile = null;
        m_nFileCount = m_noteOSS.m_lstFileList.size();
        m_nFileIndex = 0;
        for (int i = 0; i < m_noteOSS.m_lstFileList.size(); i++) {
            strFile = noteConfig.m_strBackPath + m_noteOSS.m_lstFileList.get (i);
            File backFile = new File(strFile);
            if (!backFile.exists()) {
                m_noteOSS.downlaodFile(m_noteOSS.m_lstFileList.get (i), noteConfig.m_strBackPath);
            }
            m_nFileIndex = i;
        }

        noteBackupRestore noteBackup = new noteBackupRestore(this);
        noteBackup.restoreNote();
        noteConfig.m_bNoteModified = true;
        noteConfig.m_lstData.fillFileList(noteConfig.m_strNotePath);
        dlgWait.cancel();
        showMsgDlg ("远程恢复", "远程恢复完成！");

        m_prgBackup.setProgress(0);
        m_prgRestore.setProgress(0);
    }

    private void wechatLogin () {

    }

    private boolean findInList (String strFile) {
        int nPos = strFile.lastIndexOf(File.separator);
        String strName = strFile.substring(nPos+1);
        for (int i = 0; i < m_noteOSS.m_lstFileList.size(); i++) {
            if (strName.compareTo(m_noteOSS.m_lstFileList.get(i)) == 0)
                return true;
        }
        return false;
    }

    private void showMsgDlg(String strTitle, String strMsg){
        final AlertDialog.Builder msgDialog = new AlertDialog.Builder(noteBackupActivity.this);
        msgDialog.setIcon(R.drawable.app_menu_icon);
        if (strTitle != null)
            msgDialog.setTitle(strTitle);
        if (strMsg != null)
            msgDialog.setMessage(strMsg);
        msgDialog.setPositiveButton("确定", null);
        msgDialog.show();
    }

    public void onOssProgress (int nProgress, int nTotal){
        int nCurPages = m_nFileIndex * 100 / m_nFileCount;
        int nPercent = nCurPages + (nProgress * 100 / nTotal) / m_nFileCount;
        if (m_bUploading)
            m_prgBackup.setProgress(nPercent);
        else
            m_prgRestore.setProgress(nPercent);
    }
}
