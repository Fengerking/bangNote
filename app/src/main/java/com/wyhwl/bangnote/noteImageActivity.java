package com.wyhwl.bangnote;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class noteImageActivity extends AppCompatActivity
                implements noteImageShow.noteImageShowListener {
    private noteImageShow       m_imgShow = null;
    private String              m_strImgFile = null;
    private ImageButton         m_btnZoomIn = null;
    private ImageButton         m_btnZoomOut = null;
    private int                 m_nLastX = 0;
    private int                 m_nLastY = 0;
    private long                m_nLastTime = 0;
    private boolean             m_bHideZoom = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_image);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        m_btnZoomIn = (ImageButton)findViewById(R.id.btnZoomIn);
        m_btnZoomIn.setVisibility(View.INVISIBLE);
        m_btnZoomOut = (ImageButton)findViewById(R.id.btnZoomOut);
        m_btnZoomOut.setVisibility(View.INVISIBLE);
        m_btnZoomOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                m_bHideZoom = false;
                m_imgShow.m_nowMatrix.postScale((float)0.8, (float)0.8, m_nLastX, m_nLastY);
                m_imgShow.setImageMatrix(m_imgShow.m_nowMatrix);
                m_imgShow.invalidate();
            }
        });
        m_btnZoomIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                m_bHideZoom = false;
                m_imgShow.m_nowMatrix.postScale((float)1.2, (float)1.2, m_nLastX, m_nLastY);
                m_imgShow.setImageMatrix(m_imgShow.m_nowMatrix);
                m_imgShow.invalidate();
            }
        });

        m_btnZoomIn.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                m_bHideZoom = true;
                m_imgShow.postDelayed(()->hideZoomButtons(), 200);
                return true;
            }
        });
        m_btnZoomOut.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                m_bHideZoom = true;
                m_imgShow.postDelayed(()->hideZoomButtons(), 200);
                return true;
            }
        });

        m_imgShow = findViewById(R.id.imgShow);
        m_imgShow.setNoteImageShowListener (this);
        Uri uri = getIntent().getData();
        if (uri != null)
            m_strImgFile = uri.toString();
        if (m_strImgFile != null)
            m_imgShow.setImageFile(m_strImgFile, true);

        m_imgShow.postDelayed(()->hideSystemViews(), 500);
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
                        m_imgShow.postDelayed(()->hideZoomButtons(), 3000);
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
        m_imgShow.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}
