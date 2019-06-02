package com.wyhwl.bangnote.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.SimpleTimeZone;


public class noteAudioRecStatus extends View {
    private Context         m_context = null;
    private Paint           m_pntRect = null;
    private Paint           m_pntText = null;

    private ArrayList<Integer>  m_lstAmp = null;
    private long                m_lRecTime = 0;
    private long                m_lRecSave = 0;
    private SimpleDateFormat    m_fmtTime = null;
    private String              m_strStatus = "";

    private int                 m_nMaxAmp = 30000;

    public noteAudioRecStatus(Context context) {
        super(context);
        initView(context);
    }

    public noteAudioRecStatus(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public noteAudioRecStatus(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void initView (Context context) {
        m_context = context;
        m_pntRect = new Paint ();
        m_pntRect.setColor(0XFF000000);

        m_pntText = new Paint ();
        m_pntText.setColor(0XFF00AA00);
        m_pntText.setTextSize(36);
        m_lstAmp = new ArrayList<Integer>();
        m_lRecTime = System.currentTimeMillis();

        m_fmtTime = new SimpleDateFormat("HH:mm:ss");
        m_fmtTime.setTimeZone(new SimpleTimeZone(0, "GMT"));

        m_strStatus = "Rec:     wait...";
    }

    public void startRecord () {
        m_lRecTime = System.currentTimeMillis();
        m_strStatus = "Rec:     start";
        invalidate();
    }

    public void updateMaxAmp (Integer nAmp) {
        //Log.e("TestRec", "the amp is " + nAmp);
        int nH = getHeight()/2;
        int nY = nH - (nAmp * nH / m_nMaxAmp);
        m_lstAmp.add (nY);
        Date date = new Date(System.currentTimeMillis() - m_lRecTime + m_lRecSave);
        m_strStatus = "Rec:     " + m_fmtTime.format(date);
        invalidate();
    }

    public void stopRecord () {
        m_strStatus = "Rec:     stop";
        m_lRecSave += System.currentTimeMillis() - m_lRecTime;
        invalidate();
    }

    public void playRecord () {
        m_strStatus = "Rec:     playing";
        invalidate();
    }

    public void playFinish () {
        m_lRecSave = 0;
        m_strStatus = "Rec:     wait...";
        invalidate();
    }

    public void deleteRecord () {
        m_strStatus = "Rec: delete";
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        int nW = getWidth();
        int nH = getHeight();

        RectF rcItemf = new RectF(8, 16, nW, nH - 16);
        m_pntRect.setStyle(Paint.Style.FILL);
        m_pntRect.setColor(0XFF000000);
        canvas.drawRoundRect(rcItemf, 24, 24, m_pntRect);

        m_pntRect.setColor(0XFF00FF00);
        m_pntRect.setStrokeWidth(4);
        int nCount = m_lstAmp.size();
        int nSize = m_lstAmp.size();
        int nStepX = 10;
        if (nSize > 40)
            nSize = 40;
        float[] ftPos = new float[nSize * 4];
        for (int i = 0; i < nSize - 1; i++) {
            ftPos[i*4] = i * nStepX + 8;
            ftPos[i*4+1] = m_lstAmp.get((nCount - nSize) + i);
            ftPos[i*4+2] = (i + 1) * nStepX + 8;
            ftPos[i*4+3] = m_lstAmp.get((nCount - nSize) + i + 1);
        }
        canvas.drawLines(ftPos, m_pntRect);

        m_pntRect.setColor(0XFFFFFFFF);
        canvas.drawLine(12, nH/2, nW - 4, nH/2, m_pntRect);

        canvas.drawText(m_strStatus, 0, m_strStatus.length(), 32, nH - 24, m_pntText);
    }
}
