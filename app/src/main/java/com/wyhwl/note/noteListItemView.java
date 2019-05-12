package com.wyhwl.note;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class noteListItemView extends TextView {
    private dataNoteList    m_dataNote = null;
    private Paint           m_pntDate;
    private Paint           m_pntMonth;
    private Paint           m_pntItem;

    public noteListItemView(Context context) {
        super(context);
        init ();
    }
    public noteListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init ();
    }
    public noteListItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init ();
    }

    public void init () {
        m_pntDate = new Paint();
        m_pntDate.setTextSize(40);
        m_pntDate.setColor(Color.argb(0XFF, 0xFF, 0XFF, 0XFF));

        m_pntMonth =  new Paint();
        m_pntMonth.setTextSize(30);
        m_pntDate.setColor(Color.argb(0XFF, 0xFF, 0XFF, 0XFF));

        m_pntItem =  new Paint();
        m_pntItem.setTextSize(40);
        m_pntDate.setColor(Color.argb(0XFF, 0xFF, 0XFF, 0XFF));
    }

    public void setDataList (dataNoteList noteList) {
        m_dataNote = noteList;
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        if (m_dataNote == null)
            return;

        int nW = getWidth();
        int nH = getHeight();

        float fX = getScaleX();
        float fY = getGravity();

        canvas.drawLine(100, 0, 100, nH, m_pntDate);
        canvas.drawCircle(100, 50, 25, m_pntDate);

        String strDraw = m_dataNote.m_strDate.substring(5, 7);
        canvas.drawText(strDraw, 40, 50, m_pntMonth);
        strDraw = m_dataNote.m_strDate.substring(0, 4);
        canvas.drawText(strDraw, 4, 90, m_pntMonth);

        m_pntDate.setColor(Color.argb(255, 128, 128, 128));
        strDraw = m_dataNote.m_strDate.substring(8, 10);
        canvas.drawText(strDraw, 80, 70, m_pntDate);

        m_pntDate.setColor(Color.argb(255, 50, 50, 50));
        canvas.drawRect(150, 16, nW - 16, nH - 16, m_pntDate);
    }
}
