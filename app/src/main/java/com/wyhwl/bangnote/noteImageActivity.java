package com.wyhwl.bangnote;

import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;


import com.wyhwl.bangnote.view.*;

public class noteImageActivity extends noteBaseActivity
                implements noteImageShow.noteImageShowListener {
    private noteImageSlider     m_imgSlider = null;

    private String              m_strImgFile = null;
    private ImageButton         m_btnZoomIn = null;
    private ImageButton         m_btnZoomOut = null;
    private int                 m_nLastX = 0;
    private int                 m_nLastY = 0;
    private long                m_nLastTime = 0;
    private boolean             m_bHideZoom = false;

    private String[]            m_strFileList = null;
    private int                 m_nFileCount = 0;
    private int                 m_nCurIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_image);

        initViews ();
    }

    protected void onStop () {
        super.onStop();
    }

    private void initViews (){
        m_btnZoomIn = (ImageButton)findViewById(R.id.btnZoomIn);
        m_btnZoomIn.setVisibility(View.INVISIBLE);
        m_btnZoomOut = (ImageButton)findViewById(R.id.btnZoomOut);
        m_btnZoomOut.setVisibility(View.INVISIBLE);
        m_btnZoomOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                m_bHideZoom = false;
                m_imgSlider.getCurView().zoomOut();
            }
        });
        m_btnZoomIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                m_bHideZoom = false;
                m_imgSlider.getCurView().zoomIn();
            }
        });

        m_btnZoomIn.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                m_bHideZoom = true;
                m_imgSlider.postDelayed(()->hideZoomButtons(), 200);
                return true;
            }
        });
        m_btnZoomOut.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                m_bHideZoom = true;
                m_imgSlider.postDelayed(()->hideZoomButtons(), 200);
                return true;
            }
        });

        m_imgSlider = (noteImageSlider)findViewById(R.id.imgShow);
        m_imgSlider.setOnNoteImageShowListener(this);
        Uri uri = getIntent().getData();
        if (uri != null)
            m_strImgFile = uri.toString();
        m_nFileCount = getIntent().getIntExtra("FileCount", 0);
        if(m_nFileCount > 0) {
            m_strFileList = getIntent().getStringArrayExtra("FileList");
            for (int i = 0; i < m_nFileCount; i++) {
                if (m_strFileList[i].compareTo(m_strImgFile) == 0) {
                    m_nCurIndex = i;
                    break;
                }
            }
            m_imgSlider.setImageFiles(m_strFileList, m_nFileCount, m_nCurIndex);
        }

        m_imgSlider.postDelayed(()->hideSystemViews(), 500);
    }

    public int onNoteImageShowEvent (View view, MotionEvent ev){
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                m_nLastY = (int)ev.getY();
                m_nLastX = (int)ev.getX();
                m_nLastTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (System.currentTimeMillis() - m_nLastTime > 1000) {
                    if (Math.abs(m_nLastY - (int)ev.getY()) < 100 &&
                            Math.abs(m_nLastX - (int)ev.getX()) < 100 ) {
                        m_btnZoomIn.setVisibility(View.VISIBLE);
                        m_btnZoomOut.setVisibility(View.VISIBLE);
                        m_bHideZoom = true;
                        m_imgSlider.postDelayed(()->hideZoomButtons(), 3000);
                    }
                }
                break;
        }
        return 0;
    }

    private void hideZoomButtons () {
        if (!m_bHideZoom)
            return;
        m_btnZoomOut.setVisibility(View.INVISIBLE);
        m_btnZoomIn.setVisibility(View.INVISIBLE);
    }

    private void hideSystemViews () {
        m_imgSlider.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}
