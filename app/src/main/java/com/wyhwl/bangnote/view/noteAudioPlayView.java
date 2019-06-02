package com.wyhwl.bangnote.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import com.wyhwl.bangnote.R;

public class noteAudioPlayView extends FrameLayout
                                    implements View.OnClickListener,
                                        MediaPlayer.OnCompletionListener,
                                        MediaPlayer.OnPreparedListener {

    private Context             m_context = null;
    private boolean             m_bCreate = true;

    private ImageButton         m_btnPause  = null;
    private ImageButton         m_btnPlay   = null;
    private SeekBar             m_sbPos = null;
    private TextView            m_txtDate = null;
    private TextView            m_txtPos = null;

    private MediaPlayer         m_player = null;
    private String              m_strFile = null;
    private boolean             m_bPlaying = false;
    private int                 m_nDuration = 0;
    private String              m_strDuration = "";
    private Paint               m_pntRect = null;

    public noteAudioPlayView(Context context) {
            super(context);
            initView(context, true);
        }
    public noteAudioPlayView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initView(context, false);
        }
    public noteAudioPlayView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            initView(context, false);
        }

    protected void onDetachedFromWindow() {
        if (m_player != null)
            m_player.stop();
        super.onDetachedFromWindow();
    }

    public void initView (Context context, boolean bCreate) {
        m_context = context;
        m_bCreate = bCreate;
        m_pntRect = new Paint();
        m_pntRect.setColor(0XFF444444);

        final View audioEditView = LayoutInflater.from(m_context).inflate(R.layout.note_audio_play,null);
        addView(audioEditView);

        m_btnPlay = (ImageButton)audioEditView.findViewById(R.id.btnAudioPlay);
        m_btnPlay.setOnClickListener(this);
        m_btnPause = (ImageButton)audioEditView.findViewById(R.id.btnAudioPause);
        m_btnPause.setOnClickListener(this);

        m_txtDate = (TextView) audioEditView.findViewById(R.id.txtDate);
        m_txtPos  = (TextView)audioEditView.findViewById(R.id.txtDuration);
        m_sbPos   = (SeekBar)audioEditView.findViewById(R.id.sbPos);
        m_sbPos.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                int nPos = seekBar.getProgress() * (m_nDuration / 100);
                if (m_player != null) {
                    m_player.seekTo(nPos);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
        });
    }

    public String getAudioFile () {
        return m_strFile;
    }

    public void setAudioFile (String strFile) {
        m_strFile = strFile;
        File fileRec = new File (m_strFile);
        if (!fileRec.exists())
            return;

        int nPos = strFile.indexOf("aud_");
        if (nPos > 0) {
            String strDate = strFile.substring(nPos + 4, nPos + 20);
            m_txtDate.setText(strDate);
        }

        m_btnPlay.setEnabled(true);
        m_btnPause.setEnabled(true);
        m_player = new MediaPlayer();
        m_player.setOnCompletionListener(this);
        m_player.setOnPreparedListener(this);
        try {
            m_player.setDataSource(m_strFile);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            m_player.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAudioPlay:
                if (m_player != null) {
                    m_player.start();
                    m_bPlaying = true;
                    postDelayed(()->updatePos(), 200);
                }
                break;

            case R.id.btnAudioPause:
                if (m_player != null) {
                    m_player.pause();
                    m_bPlaying = false;
                }
                break;
        }
    }

    public void onCompletion(MediaPlayer mp) {
        m_player.seekTo(0);
        m_player.pause();
        m_bPlaying = false;
    }

    public void onPrepared(MediaPlayer mp) {
        m_nDuration = m_player.getDuration();
        m_strDuration = String.format("%02d:%02d", m_nDuration / 60000, (m_nDuration % 60000) / 1000);
        postDelayed(()->updatePos(), 200);
    }

    public void updatePos () {
        if (m_player == null)
            return;

        int nPos = m_player.getCurrentPosition();
        if (nPos < 0)
            return;

        String strPos = new String();
        strPos = strPos.format("%02d:%02d", nPos / 60000, (nPos % 60000) / 1000);
        strPos = strPos + "/" + m_strDuration;
        m_txtPos.setText(strPos);
        m_sbPos.setProgress(nPos / (m_nDuration / 100));

        if (m_bPlaying)
            postDelayed(()->updatePos(), 200);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        int nW = getWidth();
        int nH = getHeight();
        RectF rcItemf = new RectF(0, 0, nW, nH);
        m_pntRect.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rcItemf, 16, 16, m_pntRect);
        super.dispatchDraw(canvas);
    }
}
