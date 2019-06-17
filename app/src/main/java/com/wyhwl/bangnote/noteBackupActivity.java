package com.wyhwl.bangnote;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Log;
import java.io.File;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import com.wyhwl.bangnote.base.*;

public class noteBackupActivity extends AppCompatActivity
                                implements View.OnClickListener,
                                            noteAliyunOSS.onOssProgressListener {
    private final int           BMSG_PROCESS     = 100;
    private final int           BMSG_END         = 200;

    private noteAliyunOSS       m_noteOSS = null;
    private String              m_strUserID = "";
    private String              m_strUserName = "";

    private TextView            m_txtWechat = null;
    private ImageButton         m_btnWechat = null;
    private ImageButton         m_btnBackup = null;
    private ImageButton         m_btnRestore = null;

    private ProgressBar         m_prgBackup = null;
    private ProgressBar         m_prgRestore = null;

    private boolean             m_bUploading = true;
    private int                 m_nFileCount = 0;
    private int                 m_nFileIndex = 0;

    private backupHandler       m_hdlBackup = null;

    private IWXAPI              m_wxAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_backup);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //通过WXAPIFactory工厂获取IWXApI的示例
        m_wxAPI = WXAPIFactory.createWXAPI(this, noteConfig.APP_ID_WX);//,true);
        //将应用的appid注册到微信
        m_wxAPI.registerApp(noteConfig.APP_ID_WX);

        SharedPreferences settings = getSharedPreferences("note_Setting", 0);
        m_strUserID = settings.getString("wxUserID", "");
        m_strUserName = settings.getString("wxUserName", "");

        m_noteOSS = new noteAliyunOSS (this);
        m_noteOSS.setOssProgListener(this);
        initViews();
    }

    protected void onResume () {
        super.onResume();
        if (noteConfig.g_nWXLoginResult == 1) {
            m_strUserID = noteConfig.g_strWXUnionID;
            m_strUserName = noteConfig.g_strWXNickName;
            SharedPreferences settings = getSharedPreferences("note_Setting", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("wxUserID", m_strUserID);
            editor.putString("wxUserName", m_strUserName);
            editor.commit();
            m_txtWechat.setText (m_strUserName + "已经登录，可以远程备份和恢复");
            noteConfig.g_nWXLoginResult = 0;
        }
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

        m_txtWechat = (TextView)findViewById(R.id.txtWechat);
        m_btnWechat = (ImageButton)findViewById(R.id.btnWechat);
        m_btnBackup = (ImageButton)findViewById(R.id.btnBackup);
        m_btnRestore = (ImageButton)findViewById(R.id.btnRestore);
        m_btnWechat.setOnClickListener(this);
        m_btnBackup.setOnClickListener(this);
        m_btnRestore.setOnClickListener(this);

        m_prgBackup = (ProgressBar)findViewById(R.id.pgbBackup);
        m_prgRestore = (ProgressBar)findViewById(R.id.pgbRestore);

        if (m_strUserID.length() > 6) {
            m_txtWechat.setText (m_strUserName + "已经登录，可以远程备份和恢复");
        }

        m_hdlBackup = new backupHandler();
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
        if (m_strUserID.length() < 6) {
            showMsgDlg ("远程备份", "请先登录微信！");
            return;
        }
        m_prgBackup.setProgress(0);
        m_prgRestore.setProgress(0);
        m_bUploading = true;

        noteBackupRestore noteBackup = new noteBackupRestore(this);
        noteBackup.backupNote();

        backupThread (0);
    }

    private void noteRestore () {
        if (m_strUserID.length() < 6) {
            showMsgDlg ("远程备份", "请先登录微信！");
            return;
        }
        m_prgBackup.setProgress(0);
        m_prgRestore.setProgress(0);
        m_bUploading = false;

        backupThread (1);
    }

    private void wechatLogin () {
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        // req.scope = "snsapi_login";  //提示 scope参数错误，或者没有scope权限
        req.state = "wechat_sdk_test";
        m_wxAPI.sendReq(req);
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
        String strInfo = String.format("Index: %d  Total: %d  Prog: %d  Total: %d  Percent: %d",
                                            m_nFileIndex, m_nFileCount, nProgress, nTotal, nPercent);
        Log.v ("noteBackup", strInfo);
        Message msg = m_hdlBackup.obtainMessage(BMSG_PROCESS, nPercent, 0, null);
        msg.sendToTarget();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void backupThread(int nType) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                m_btnWechat.setEnabled(false);
                m_btnBackup.setEnabled(false);
                m_btnRestore.setEnabled(false);
                if (nType == 0)
                    uloadFiles ();
                else
                    downloadFiles();
                Message msg = m_hdlBackup.obtainMessage(BMSG_END, 0, 0, null);
                msg.sendToTarget();
            }
        }).start();
    }

    private void uloadFiles () {
        if (m_noteOSS.m_lstFileList.size() <= 0)
            m_noteOSS.getFileList(m_strUserID);

        File fPath = new File(noteConfig.m_strBackPath);
        File[] fList = fPath.listFiles();
        if (fList != null) {
            m_nFileIndex = 0;
            m_nFileCount = 0;
            for (int i = 0; i < fList.length; i++) {
                File file = fList[i];
                if (file.isHidden() || file.isDirectory())
                    continue;
                if (findInList (file.getPath()))
                    continue;
                m_nFileCount++;
            }
            for (int i = 0; i < fList.length; i++) {
                File file = fList[i];
                if (file.isHidden() || file.isDirectory())
                    continue;
                if (findInList (file.getPath()))
                    continue;
                m_noteOSS.uploadFile(file.getPath());
                m_nFileIndex++;
            }
        }
    }

    private void downloadFiles () {
        if (m_noteOSS.m_lstFileList.size() <= 0)
            m_noteOSS.getFileList(m_strUserID);

        String strFile = null;
        m_nFileCount = 0;
        m_nFileIndex = 0;
        for (int i = 0; i < m_noteOSS.m_lstFileList.size(); i++) {
            strFile = noteConfig.m_strBackPath + m_noteOSS.m_lstFileList.get (i);
            File backFile = new File(strFile);
            if (!backFile.exists()) {
                m_nFileCount++;
            }
        }
        for (int i = 0; i < m_noteOSS.m_lstFileList.size(); i++) {
            strFile = noteConfig.m_strBackPath + m_noteOSS.m_lstFileList.get (i);
            File backFile = new File(strFile);
            if (!backFile.exists()) {
                m_noteOSS.downlaodFile(m_noteOSS.m_lstFileList.get (i), noteConfig.m_strBackPath);
                m_nFileIndex++;
            }
        }

        noteBackupRestore noteBackup = new noteBackupRestore(this);
        noteBackup.restoreNote();
        noteConfig.m_bNoteModified = true;
        noteConfig.m_lstData.fillFileList(noteConfig.m_strNotePath);
    }

    class backupHandler extends Handler {
        public void handleMessage(Message msg) {
            if (msg.what == BMSG_PROCESS) {
                if (m_bUploading)
                    m_prgBackup.setProgress(msg.arg1);
                else
                    m_prgRestore.setProgress(msg.arg1);
            } else if (msg.what == BMSG_END) {
                if (m_bUploading)
                    showMsgDlg ("远程备份", "远程备份成功！");
                else
                    showMsgDlg ("远程恢复", "远程恢复完成！");

                m_prgBackup.setProgress(0);
                m_prgRestore.setProgress(0);

                m_btnWechat.setEnabled(true);
                m_btnBackup.setEnabled(true);
                m_btnRestore.setEnabled(true);
            }
        }
    }
}
