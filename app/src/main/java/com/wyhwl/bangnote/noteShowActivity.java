package com.wyhwl.bangnote;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;

public class noteShowActivity extends AppCompatActivity
        implements View.OnClickListener {
    private noteShowSlider          m_showSlider = null;
    private String                  m_strNoteFile = null;
    private String[]                m_strFileList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_show);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null)
            m_strNoteFile = uri.toString();
        else
            m_strNoteFile = "/sdcard/bangNote/text/txt_2019-05-18-21-34-19.bnt";

        int nFileCount = intent.getIntExtra("FileCount", 0);
        if(nFileCount > 0) {
            m_strFileList = intent.getStringArrayExtra("FileList");
        }

        initViews();
    }

    protected void onResume () {
        super.onResume();
        if (noteConfig.m_bNoteModified) {
            m_showSlider.setNoteFile (m_strNoteFile, false);
        }
    }

    private void initViews () {
        ((ImageButton) findViewById(R.id.imbBack)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.imbEditNote)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.imbShareNote)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.imbDeleteNote)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.imbCount)).setOnClickListener(this);

        m_showSlider = (noteShowSlider)findViewById(R.id.sldNoteShow);
        m_showSlider.setNoteFile (m_strNoteFile, true);
    }

    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.imbBack:
                finish();
                break;

            case R.id.imbEditNote:
                m_strNoteFile = m_showSlider.getNotefile();
                intent = new Intent(noteShowActivity.this, noteEditActivity.class);
                intent.setData(Uri.parse(m_strNoteFile));
                startActivityForResult(intent, 1);
                break;

            case R.id.imbShareNote:
                String strShareText = "";

                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "分享笔记");
                intent.putExtra(Intent.EXTRA_TEXT, strShareText);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, getTitle()));
                break;

            case R.id.imbDeleteNote:

                break;

            case R.id.imbCount:

                break;
        }
    }

}
