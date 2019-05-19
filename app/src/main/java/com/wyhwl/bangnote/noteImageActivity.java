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
import android.view.View;
import android.view.ViewGroup;

public class noteImageActivity extends AppCompatActivity {
    private noteImageShow       m_imgShow = null;
    private String              m_strImgFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_image);

        m_imgShow = findViewById(R.id.imgShow);
        Uri uri = getIntent().getData();
        if (uri != null)
            m_strImgFile = uri.toString();

        m_imgShow.postDelayed(()->showFullscreen(), 100);
    }

    private void showFullscreen() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        m_imgShow.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        //m_imgShow.postDelayed(()->showImage(), 500);
        showImage();
    }

    private void showImage () {
        if (m_strImgFile != null) {
            m_imgShow.setImageFile(m_strImgFile, true);
        }
    }
}
