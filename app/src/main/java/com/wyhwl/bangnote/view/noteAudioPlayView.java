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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.wyhwl.bangnote.R;
import com.wyhwl.bangnote.base.noteConfig;

public class noteAudioPlayView extends FrameLayout
                                    implements View.OnClickListener,
                                        MediaPlayer.OnCompletionListener,
                                        MediaPlayer.OnPreparedListener {

    private Context             m_context = null;
    private int                 m_nID = 0;
    private int                 m_nAudioType = noteConfig.m_nItemTypeAudo;

    private ImageButton         m_btnPause  = null;
    private ImageButton         m_btnPlay   = null;
    private ImageButton         m_btnDelete = null;
    private SeekBar             m_sbPos = null;
    private TextView            m_txtDate = null;
    private TextView            m_txtPos = null;

    private MediaPlayer         m_player = null;
    private String              m_strFile = null;
    private boolean             m_bPlaying = false;
    private int                 m_nDuration = 0;
    private String              m_strDuration = "";
    private Paint               m_pntRect = null;

    private audioPlayViewListener   m_listener = null;

    // The event listener function
    public interface audioPlayViewListener {
        public void onAudioPlayChange (View view, int nCommand);
    }

    public void setAudioPlayListener (audioPlayViewListener listener) {
        m_listener = listener;
    }

    public noteAudioPlayView(Context context, int nType) {
            super(context);
            initView(context, nType);
        }
    public noteAudioPlayView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initView(context, noteConfig.m_nItemTypeAudo);
        }
    public noteAudioPlayView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            initView(context, noteConfig.m_nItemTypeAudo);
        }

    protected void onDetachedFromWindow() {
        if (m_player != null)
            m_player.stop();
        super.onDetachedFromWindow();
    }

    public void initView (Context context, int nType) {
        m_context = context;
        m_nAudioType = nType;
        m_pntRect = new Paint();
        m_pntRect.setColor(0XFF444444);

        View audioView = null;
        if (m_nAudioType == noteConfig.m_nItemTypeAudo) {
            audioView = LayoutInflater.from(m_context).inflate(R.layout.note_audio_play, null);
            m_nID = noteConfig.getAudoViewID ();
        } else if (m_nAudioType == noteConfig.m_nItemTypeMusc) {
            audioView = LayoutInflater.from(m_context).inflate(R.layout.note_audio_music, null);
            m_nID = noteConfig.getMuscViewID ();
        } else {
            return;
        }
        addView(audioView);
        m_btnPlay = (ImageButton)audioView.findViewById(R.id.btnAudioPlay);
        m_btnPlay.setOnClickListener(this);
        m_btnPause = (ImageButton)audioView.findViewById(R.id.btnAudioPause);
        if (m_btnPause != null) {
            m_btnPause.setOnClickListener(this);
            m_btnPause.setVisibility(View.INVISIBLE);
        }
        m_btnDelete = (ImageButton)audioView.findViewById(R.id.btnAudioDelete);
        if (m_btnDelete != null)
            m_btnDelete.setOnClickListener(this);

        m_txtDate = (TextView) audioView.findViewById(R.id.txtDate);
        m_txtPos  = (TextView)audioView.findViewById(R.id.txtDuration);
        m_sbPos   = (SeekBar)audioView.findViewById(R.id.sbPos);
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

    public int getId() {
        return m_nID;
    }

    public String getAudioFile () {
        return m_strFile;
    }

    public void setAudioFile (String strFile) {
        m_strFile = strFile;
        File fileRec = new File (m_strFile);
        if (!fileRec.exists())
            return;

        if (m_nAudioType == noteConfig.m_nItemTypeMusc) {
            int nFind = strFile.indexOf(noteConfig.m_strNotePath);
            if (nFind < 0) {
                String strNewFile = noteConfig.getNoteAudioFile();
                try {
                    FileInputStream fis = new FileInputStream(strFile);
                    FileOutputStream fos = new FileOutputStream(strNewFile);

                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while (-1 != (byteRead = fis.read(buffer))) {
                        fos.write(buffer, 0, byteRead);
                    }
                    fis.close();
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                m_strFile = strNewFile;
            }
            m_txtDate.setText("");
        } else {
            int nPos = m_strFile.indexOf("aud_");
            if (nPos > 0) {
                String strDate = m_strFile.substring(nPos + 4, nPos + 20);
                m_txtDate.setText(strDate);
            }
        }
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

    public void pausePlay () {
        if (!m_bPlaying)
            return;
        if (m_player != null)
            m_player.pause();
        m_bPlaying = false;
        m_btnPlay.setVisibility(View.VISIBLE);
        m_btnPause.setVisibility(View.INVISIBLE);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAudioPlay:
                if (m_listener != null)
                    m_listener.onAudioPlayChange(this, v.getId());
                if (m_player != null) {
                    m_player.start();
                    m_bPlaying = true;
                    m_btnPause.setVisibility(View.VISIBLE);
                    m_btnPlay.setVisibility(View.INVISIBLE);
                    postDelayed(()->updatePos(), 200);
                }
                break;

            case R.id.btnAudioPause:
                pausePlay();
                break;

            case R.id.btnAudioDelete:
                if (m_listener != null)
                    m_listener.onAudioPlayChange(this, v.getId());
                break;
        }
    }

    public void onCompletion(MediaPlayer mp) {
        m_player.seekTo(0);
        pausePlay();
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
