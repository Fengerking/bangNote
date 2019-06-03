package com.wyhwl.bangnote;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.RelativeLayout;
import android.content.Intent;

import com.wyhwl.bangnote.base.*;
import com.wyhwl.bangnote.view.*;

public class MainActivity extends AppCompatActivity {
    private Button              m_btnTest = null;
    private noteAudioEditView   m_layAudio = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setLogo(R.mipmap.ic_launcher);
        //actionBar.setDisplayShowHomeEnabled(true);
        //actionBar.setDisplayUseLogoEnabled(true);

/*
        noteConfig.initConfig(this);

        m_btnTest = (Button)findViewById(R.id.btnTest);
        m_btnTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, noteListActivity.class);
                //intent.setData(Uri.parse(noteConfig.m_strNoteTextPath + "txt_2019-05-17-14-57-19.bnt"));
                startActivity(intent);
            }
        });

        RelativeLayout layMain = (RelativeLayout)findViewById(R.id.layMain);
        m_layAudio = new noteAudioEditView(this);
        layMain.addView(m_layAudio);

        ViewGroup.LayoutParams param = (ViewGroup.LayoutParams)m_layAudio.getLayoutParams();
        param.width = -1;
        m_layAudio.setLayoutParams(param);
*/
    }





}
