package com.wyhwl.bangnote;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;

import com.wyhwl.bangnote.base.*;


public class noteBackupActivity extends AppCompatActivity
                                implements View.OnClickListener{

    private ImageButton         m_btnWechat = null;
    private ImageButton         m_btnBackup = null;
    private ImageButton         m_btnRestore = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_backup);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        noteConfig.initConfig(this);
        initViews();
    }

    private void initViews () {
        ((ImageButton)findViewById(R.id.imbBack)).setOnClickListener(this);

        m_btnWechat = (ImageButton)findViewById(R.id.btnWechat);
        m_btnBackup = (ImageButton)findViewById(R.id.btnBackup);
        m_btnRestore = (ImageButton)findViewById(R.id.btnRestore);
        m_btnWechat.setOnClickListener(this);
        m_btnBackup.setOnClickListener(this);
        m_btnRestore.setOnClickListener(this);

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
        noteBackupRestore noteBackup = new noteBackupRestore(this);
        noteBackup.backupNote();
    }

    private void noteRestore () {
        noteBackupRestore noteBackup = new noteBackupRestore(this);
        noteBackup.restoreNote();
    }

    private void wechatLogin () {

    }
}
