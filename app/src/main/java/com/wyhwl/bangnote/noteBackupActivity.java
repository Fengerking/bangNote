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
        initViews();
    }

    private void initViews () {

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imbBack:
                finish();
                break;

            case R.id.btnWechat:
                break;

            case R.id.btnBackup:
                break;

            case R.id.btnRestore:
                break;

            default:
                break;
        }
    }
}
