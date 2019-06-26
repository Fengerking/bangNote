package com.wyhwl.bangnote;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.wyhwl.bangnote.base.noteConfig;

public class noteBaseActivity extends AppCompatActivity {
    protected final static int      MSG_DOWNLOAD_APK_PROCSSS        = 100;
    protected final static int      MSG_DOWNLOAD_APK_FINISH         = 101;
    protected final static int      MSG_UPDATE_UI                   = 200;
    protected final static int      MSG_HIDE_BUTTON                 = 201;
    protected final static int      MSG_OSS_PROCESS                 = 300;
    protected final static int      MSG_OSS_END                     = 301;

    public static final int         DLG_NOTETYPE_NEW        = 1;
    public static final int         DLG_NOTETYPE_DEL        = 2;
    public static final int         DLG_NOTETYPE_CHG        = 3;
    public static final int         DLG_NOTETYPE_MOV        = 4;
    public static final int         DLG_NOTEITEM_DEL        = 5;

    protected Dialog        m_dlgWait           = null;
    protected msgHandler    m_msgHandler        = null;

    protected dlgParam      m_dlgParam          = null;

    public class dlgParam {
        public int nType            = 0;
        public int nView            = 0;
        public int nIcon            = R.drawable.note_icon;
        public String strTitle      = null;
        public String strOK         = null;
        public String strCancel     = null;
        public String strOther      = null;

        public View   dlgView     = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        m_dlgParam = new dlgParam();
        m_msgHandler = new msgHandler();
    }

    class msgHandler extends Handler {
        public void handleMessage(Message msg) {
            onMsgHandler (msg);
        }
    }

    protected void onMsgHandler (Message msg) {
    }

    protected void showMsgDlg(String strTitle, String strMsg, boolean bConfirm){
        m_dlgParam.nType = 0;
        final AlertDialog.Builder msgDialog = new AlertDialog.Builder(noteBaseActivity.this);
        msgDialog.setIcon(R.drawable.app_menu_icon);
        if (strTitle != null)
            msgDialog.setTitle(strTitle);
        if (strMsg != null)
            msgDialog.setMessage(strMsg);
        if (bConfirm) {
            msgDialog.setNeutralButton("取消",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            onDlgCnacel();
                        }
                    });
        }
        msgDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (bConfirm) {
                            onDlgOK();
                        }
                    }
                });
        msgDialog.show();
    }

    protected void onDlgOK () {
    }
    protected void onDlgCnacel () {
    }
    protected void onDlgOther () {
    }

    protected void initDlgParam () {
        m_dlgParam.nType = 0;
        m_dlgParam.nIcon = R.drawable.note_icon;
        m_dlgParam.nView = 0;
        m_dlgParam.strTitle = null;
        m_dlgParam.strCancel = null;
        m_dlgParam.strOK = null;
        m_dlgParam.strOther = null;
    }

    protected void showNoteDialog() {
        final View noteView = LayoutInflater.from(noteBaseActivity.this).inflate(m_dlgParam.nView,null);
        m_dlgParam.dlgView = noteView;
        AlertDialog.Builder dlgNote = new AlertDialog.Builder(noteBaseActivity.this){
            public AlertDialog create() {
                onDlgCreate();
                return super.create();
            }
            public AlertDialog show() {
                onDlgShow ();
                return super.show();
            }
        };

        dlgNote.setIcon(m_dlgParam.nIcon);
        if (m_dlgParam.strTitle != null)
            dlgNote.setTitle(m_dlgParam.strTitle);
        dlgNote.setView(noteView);

        if (m_dlgParam.strCancel != null) {
            dlgNote.setNeutralButton(m_dlgParam.strCancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                       onDlgCnacel();
                    }
                });
        }

        if (m_dlgParam.strOK != null) {
            dlgNote.setPositiveButton(m_dlgParam.strOK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onDlgOK();
                    }
                });
        }

        if (m_dlgParam.strOther != null) {
            dlgNote.setNegativeButton(m_dlgParam.strOther,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onDlgOther();
                    }
                });
        }

        dlgNote.show();
    }

    protected void onDlgCreate () {
    }

    protected void onDlgShow () {
    }

    protected void showWaitDialog(String strMsg, boolean bShow) {
        if (bShow) {
            if (m_dlgWait == null) {
                m_dlgWait = new Dialog(this, R.style.progress_dialog);
                m_dlgWait.setContentView(R.layout.dialog_wait);
                m_dlgWait.setCancelable(false);
                m_dlgWait.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                TextView txtMsg = (TextView) m_dlgWait.findViewById(R.id.tvLoadingText);
                txtMsg.setText(strMsg);
            }
            m_dlgWait.show();
        } else {
            if (m_dlgWait == null)
                return;
            m_dlgWait.dismiss();
            m_dlgWait = null;
        }
    }
}
