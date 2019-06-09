package com.wyhwl.bangnote.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.wyhwl.bangnote.base.*;
import com.wyhwl.bangnote.R;

public class noteAudioEditView extends FrameLayout
                                implements View.OnClickListener,
                                    MediaPlayer.OnCompletionListener,
                                    MediaPlayer.OnPreparedListener {
    private Context             m_context = null;
    private boolean             m_bCreate = true;
    private int                 m_nID = 0;

    private ImageButton         m_btnStart  = null;
    private ImageButton         m_btnStop   = null;
    private ImageButton         m_btnPause  = null;
    private ImageButton         m_btnPlay   = null;
    private ImageButton         m_btnDelete = null;

    private String              m_strRecFile = null;
    private String              m_strTmpFile = null;

    private MediaRecorder       m_recorder = null;
    private MediaPlayer         m_player = null;
    private boolean             m_bRecording = false;
    private boolean             m_bPlaying = false;
    private RecordAmplitude     m_recAmplitude = null;

    private noteAudioRecStatus    m_vwRecStatus = null;

    private audioChangeListener m_audioListener = null;

    private Paint               m_pntRect = null;

    // The event listener function
    public interface audioChangeListener {
        public void onAudioChange (String strAudioFile);
    }

    public void setAudioChangeListener (audioChangeListener listener) {
        m_audioListener = listener;
    }

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

    protected void onDetachedFromWindow() {
        stopRecord();
        super.onDetachedFromWindow();
    }

    public void initView (Context context, boolean bCreate) {
        m_context = context;
        m_bCreate = bCreate;
        m_nID = noteConfig.getAudoViewID ();

        m_pntRect = new Paint();
        m_pntRect.setColor(0XFF444444);

        final View audioEditView = LayoutInflater.from(m_context).inflate(R.layout.note_audio_edit,null);
        addView(audioEditView);

        m_btnStart = (ImageButton)audioEditView.findViewById(R.id.btnAudioStart);
        m_btnStart.setOnClickListener(this);
        m_btnStop = (ImageButton)audioEditView.findViewById(R.id.btnAudioStop);
        m_btnStop.setOnClickListener(this);
        m_btnPlay = (ImageButton)audioEditView.findViewById(R.id.btnAudioPlay);
        m_btnPlay.setOnClickListener(this);
        m_btnDelete = (ImageButton)audioEditView.findViewById(R.id.btnAudioDelete);
        m_btnDelete.setOnClickListener(this);

        m_vwRecStatus = (noteAudioRecStatus)audioEditView.findViewById(R.id.viewRecStatus);

        m_btnStop.setEnabled(false);
        m_btnPlay.setEnabled(false);
        m_btnDelete.setEnabled(false);

        m_strRecFile = noteConfig.getNoteAudioFile();
    }

    public int getId() {
        return m_nID;
    }

    public String getAudioFile () {
        return m_strRecFile;
    }

    public void setAudioFile (String strFile) {
        m_strRecFile = strFile;
        File fileRec = new File (m_strRecFile);
        if (fileRec.exists()) {
            m_btnStop.setEnabled(false);
            m_btnStart.setEnabled(true);
            m_btnPlay.setEnabled(true);
            m_btnDelete.setEnabled(true);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAudioStart:
                startRecord ();
                m_vwRecStatus.startRecord();
                break;

            case R.id.btnAudioStop:
                stopRecord ();
                break;

            case R.id.btnAudioPlay:
                startPlay();
                m_vwRecStatus.playRecord();
                break;

            case R.id.btnAudioDelete:
                File fileDel = new File(m_strRecFile);
                fileDel.delete();
                m_vwRecStatus.deleteRecord();
                break;
        }
    }

    private int startRecord () {
        m_recorder = new MediaRecorder();
        m_recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        m_recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        m_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        File fileRec = new File (m_strRecFile);
        if (fileRec.exists()) {
            m_strTmpFile = noteConfig.getNoteAudioFile();
            m_recorder.setOutputFile(m_strTmpFile);
        } else {
            m_recorder.setOutputFile(m_strRecFile);
        }
        try {
            m_recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        m_recorder.start();
        m_bRecording = true;
        m_recAmplitude = new RecordAmplitude();
        m_recAmplitude.execute();

        m_btnStop.setEnabled(true);
        m_btnStart.setEnabled(false);
        m_btnPlay.setEnabled(false);
        m_btnDelete.setEnabled(false);

        if (m_audioListener != null)
            m_audioListener.onAudioChange(m_strRecFile);

        return 1;
    }

    private void stopRecord () {
        if (m_bRecording) {
            m_bRecording = false;
            m_recAmplitude.cancel(true);

            m_recorder.stop();
            m_recorder.release();
            m_recorder = null;
            if (m_strTmpFile != null) {
                try {
                    noteFileInputStream fis = new noteFileInputStream (m_strTmpFile);
                    noteFileOutputStream fos = new noteFileOutputStream (m_strRecFile, true);
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while (-1 != (byteRead = fis.read(buffer))) {
                        fos.write(buffer, 0, byteRead);
                    }
                    fis.close();
                    fos.flush();
                    fos.close();
                }catch (Exception e) {
                    e.printStackTrace();
                }
                File fileDel = new File(m_strTmpFile);
                fileDel.delete();
            }
            m_vwRecStatus.stopRecord();
        } else if (m_bPlaying){
            m_bPlaying = false;
            m_player.stop();
            m_player.release();
            m_player = null;
            m_vwRecStatus.playFinish();
        }

        m_btnStop.setEnabled(false);
        m_btnStart.setEnabled(true);
        m_btnPlay.setEnabled(true);
        m_btnDelete.setEnabled(true);
    }

    private void startPlay () {
        File fileRec = new File (m_strRecFile);
        if (!fileRec.exists()) {
            return;
        }
        m_player = new MediaPlayer();
        m_player.setOnCompletionListener(this);
        m_player.setOnPreparedListener(this);
        try {
            m_player.setDataSource(m_strRecFile);
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
        m_bPlaying = true;
        m_btnStop.setEnabled(true);
        m_btnStart.setEnabled(false);
        m_btnPlay.setEnabled(false);
        m_btnDelete.setEnabled(false);
    }

    private class RecordAmplitude extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            while (m_bRecording) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                publishProgress(m_recorder.getMaxAmplitude());
            }
            return null;
        }


        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //amplitudeTextView.setText(values[0].toString());
            m_vwRecStatus.updateMaxAmp(values[0]);
        }
    }

    public void onCompletion(MediaPlayer mp) {
       stopRecord();
    }

    public void onPrepared(MediaPlayer mp) {
        m_player.start();
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
