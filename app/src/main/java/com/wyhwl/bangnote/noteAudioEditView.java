package com.wyhwl.bangnote;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class noteAudioEditView extends FrameLayout
                                implements View.OnClickListener{
    private Context             m_context = null;
    private boolean             m_bCreate = true;

    private ImageButton         m_btnStart  = null;
    private ImageButton         m_btnStop   = null;
    private ImageButton         m_btnPause  = null;
    private ImageButton         m_btnPlay   = null;
    private ImageButton         m_btnDelete = null;


    public noteAudioEditView(Context context) {
        super(context);
        initView(context, true);
    }
    public noteAudioEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, false);
    }
    public noteAudioEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, false);
    }

    public void initView (Context context, boolean bCreate) {
        m_context = context;
        m_bCreate = bCreate;

        final View audioEditView = LayoutInflater.from(m_context).inflate(R.layout.note_audio_edit,null);
        addView(audioEditView);

        m_btnStart = (ImageButton)audioEditView.findViewById(R.id.btnAudioStart);
        m_btnStart.setOnClickListener(this);

        /*
        m_btnStart = new ImageButton(m_context);
        addView(m_btnStart);
        m_btnStart.setImageResource(R.drawable.note_audio_pause);

        m_btnStop = new ImageButton(m_context);
        addView(m_btnStop);
        m_btnStart.setImageResource(R.drawable.note_audio_play);
        */
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAudioStart:

                break;
            case R.id.btnAudioPause:

                break;
        }
    }
}
