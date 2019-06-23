package com.wyhwl.bangnote;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.VideoView;

import com.wyhwl.bangnote.base.noteConfig;
import com.wyhwl.bangnote.video.StrokeTextView;

import java.util.Date;

public class noteVideoActivity extends AppCompatActivity
                                implements MediaPlayer.OnPreparedListener,
                                            MediaPlayer.OnCompletionListener,
                                            MediaPlayer.OnErrorListener,
                                            MediaPlayer.OnInfoListener,
                                            MediaPlayer.OnVideoSizeChangedListener,
                                            MediaPlayer.OnSeekCompleteListener,
                                            View.OnClickListener {
    private final static int    MSG_UPDATE_UI   = 100;
    private final static int    MSG_HIDE_BUTTON = 101;

    private VideoView           m_vwVideo = null;

    private RelativeLayout      m_layVideo      = null;
    private RelativeLayout 	    m_layButtons    = null;

    private ImageButton         m_btnFull       = null;
    private ImageButton         m_btnPlay       = null;
    private StrokeTextView      m_txtPos        = null;
    private StrokeTextView      m_txtDur        = null;
    private SeekBar             m_sbPos         = null;

    private updateHandler       m_updHandler    = null;

    private boolean             m_bPlaying      = false;
    private int                 m_nWidth        = 0;
    private int                 m_nHeight       = 0;
    private int                 m_nDuration     = 0;

    private long                m_lLastTime     = 0;
    private long                m_lShowTime     = 5000;

    private String              m_strVideoFile  = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_video);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null)
            m_strVideoFile = uri.toString();

        initViews();
    }

    protected void onPause() {
        super.onPause();
        if (m_vwVideo != null) {
            m_vwVideo.pause();
            m_bPlaying = false;
        }
    }

    protected void onResume() {
        super.onResume();
        showControls();
    }

    private void initViews () {
        m_vwVideo = (VideoView)findViewById(R.id.vwVideo);
        m_vwVideo.setOnPreparedListener(this);

        m_layVideo = (RelativeLayout) findViewById(R.id.layVideo);
        m_layButtons = (RelativeLayout) findViewById(R.id.layButtons);

        m_btnFull = (ImageButton)findViewById(R.id.btnFull);
        m_btnPlay = (ImageButton)findViewById(R.id.btnPlay);
        m_btnFull.setOnClickListener(this);
        m_btnPlay.setOnClickListener(this);

        m_txtPos = (StrokeTextView) findViewById(R.id.txtPos);
        m_txtPos.setText("00:00");
        m_sbPos = (SeekBar)findViewById(R.id.sbPos);
        m_txtDur = (StrokeTextView) findViewById(R.id.txtDur);
        m_txtDur.setText("00:00");
        m_txtDur.setVisibility(View.INVISIBLE);
        m_sbPos.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                int nPos = seekBar.getProgress() * (m_nDuration / 100);
                if (m_vwVideo != null) {
                    m_vwVideo.seekTo(nPos);
                }
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                showControls();
            }
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
        });

        m_updHandler = new updateHandler();

        m_layVideo.postDelayed(()->hideSystemViews(), 20);

        m_layVideo.postDelayed(()->openVideo(), 10);
    }

    private void openVideo () {
        if (m_strVideoFile != null)
            m_vwVideo.setVideoPath(m_strVideoFile);
    }

    public void onPrepared(MediaPlayer mp) {
        m_nWidth = mp.getVideoWidth();
        m_nHeight = mp.getVideoHeight();
        m_nDuration = mp.getDuration();
        UpdateSurfaceViewPos (m_nWidth, m_nHeight);
        playVideo ();
        m_updHandler.sendEmptyMessageDelayed(MSG_UPDATE_UI, 500);
    }

    public void onCompletion(MediaPlayer mp){
        m_bPlaying = false;
        mp.seekTo(0);
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        return true;
    }

    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return true;
    }

    public void onVideoSizeChanged(MediaPlayer mp, int width, int height){
        UpdateSurfaceViewPos (width, height);
    }

    public void onSeekComplete(MediaPlayer mp) {
        playVideo ();
    }

    private void playVideo () {
        if (m_bPlaying)
            return;
        m_vwVideo.start();
        m_btnPlay.setBackgroundResource(R.drawable.btn_pause);
        m_bPlaying = true;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnFull:
                if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    m_btnFull.setBackgroundResource(R.drawable.btn_video_nofull);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    m_btnFull.setBackgroundResource(R.drawable.btn_video_full);
                }
                showControls();
                break;

            case R.id.btnPlay:
                if (m_vwVideo != null) {
                    if (m_bPlaying) {
                        m_vwVideo.pause();
                        m_btnPlay.setBackgroundResource(R.drawable.btn_play);
                    } else {
                        m_vwVideo.start();
                        m_btnPlay.setBackgroundResource(R.drawable.btn_pause);
                    }
                    m_bPlaying = !m_bPlaying;
                }
                showControls();
                break;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        int nAction = event.getAction();
        switch (nAction) {
            case MotionEvent.ACTION_UP:
                showControls ();
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        UpdateSurfaceViewPos(m_nWidth, m_nHeight);
    }

    private void UpdateSurfaceViewPos (int nW, int nH) {
        if (nW == 0 || nH == 0)
            return;
        //if(getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        DisplayMetrics dm = this.getResources().getDisplayMetrics();

        RelativeLayout.LayoutParams lpSVideo = (RelativeLayout.LayoutParams)m_layVideo.getLayoutParams();
        RelativeLayout.LayoutParams lpButton = (RelativeLayout.LayoutParams)m_layButtons.getLayoutParams();

        lpSVideo.width = dm.widthPixels;
        lpSVideo.height  = lpSVideo.width * nH / nW;
        if (lpSVideo.height > dm.heightPixels){
            lpSVideo.height = dm.heightPixels;
            lpSVideo.width = nW * dm.heightPixels / nH;
        }

        lpButton.width = dm.widthPixels;
        lpButton.height = lpSVideo.height;
        m_layVideo.setLayoutParams(lpSVideo);
        m_layButtons.setLayoutParams(lpButton);
    }

    private void showControls (){
        if (m_layButtons.getVisibility() == View.INVISIBLE)
            m_layButtons.setVisibility(View.VISIBLE);
        m_lLastTime = System.currentTimeMillis();
        m_updHandler.sendEmptyMessageDelayed(MSG_HIDE_BUTTON, m_lShowTime);
    }

    private void hideControls () {
        m_layButtons.setVisibility(View.INVISIBLE);
    }

    class updateHandler extends Handler {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE_UI) {
                int nPos = m_vwVideo.getCurrentPosition();
                if (nPos + 100 >= m_nDuration) {
                    m_bPlaying = false;
                    m_vwVideo.seekTo(0);
                    playVideo();
                }

                String strPos = new String();
                strPos = strPos.format("%02d:%02d / %02d:%02d  ", nPos / 60000, (nPos % 60000) / 1000, m_nDuration / 60000, (m_nDuration % 60000) / 1000);
                m_txtPos.setText(strPos);
                if (m_nDuration > 0) {
                    nPos = nPos / (m_nDuration / 100);
                    if (nPos < 2)
                        nPos = 2;
                    else if (nPos > 98)
                        nPos = 98;
                    m_sbPos.setProgress(nPos);
                }

                m_updHandler.sendEmptyMessageDelayed(MSG_UPDATE_UI, 500);
            } else if (msg.what == MSG_HIDE_BUTTON) {
                long lNowTime = System.currentTimeMillis();
                if (lNowTime - m_lLastTime >= m_lShowTime) {
                    hideControls();
                } else {
                    m_updHandler.sendEmptyMessageDelayed(MSG_HIDE_BUTTON, m_lShowTime - (lNowTime - m_lLastTime));
                }
            }
        }
    }

    private void hideSystemViews () {
        m_vwVideo.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}
