package com.wyhwl.bangnote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class noteListItemView extends TextView {
    private dataNoteItem    m_dataItem = null;

    private Paint           m_pntTextLeft;
    private Paint           m_pntTextTitle;
    private Paint           m_pntTextItem;

    private Paint           m_pntLeft;
    private Paint           m_pntRect;

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
        m_pntTextLeft = new Paint();
        m_pntTextLeft.setTextSize(40);
        m_pntTextLeft.setColor(0XFF222222);

        m_pntTextTitle =  new Paint();
        m_pntTextTitle.setTextSize(48);
        m_pntTextTitle.setColor(0XFFEEEEEE);

        m_pntTextItem =  new Paint();
        m_pntTextItem.setTextSize(40);
        m_pntTextItem.setColor(0XFFCCCCCC);

        m_pntLeft =  new Paint();
        m_pntLeft.setTextSize(30);
        m_pntLeft.setColor(0XFFCCCCCC);

        m_pntRect =  new Paint();
        m_pntRect.setTextSize(30);
        m_pntRect.setColor(0XFF666666);
    }

    public void setDataList (dataNoteItem noteItem) {
        m_dataItem = noteItem;
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        if (m_dataItem == null)
            return;

        int nW = getWidth();
        int nH = getHeight();

        int nL = 150;
        int nY = 90;
        int nC = 40;

        canvas.drawLine(nL, 0, nL, nH, m_pntLeft);
        canvas.drawCircle(nL, nY, nC, m_pntLeft);

        String strDraw = m_dataItem.m_strDate.substring(5, 7);
        canvas.drawText(strDraw, 48, nY, m_pntTextLeft);
        strDraw = m_dataItem.m_strDate.substring(0, 4);
        canvas.drawText(strDraw, 4, nY + 50, m_pntTextLeft);
        strDraw = m_dataItem.m_strDate.substring(8, 10);
        canvas.drawText(strDraw, nL - nC + 15, nY + 15, m_pntTextLeft);

        Rect rcItem = new Rect(nL + 60, 16, nW - 16, nH - 16);
        canvas.drawRect(rcItem, m_pntRect);

        int nLeft = rcItem.left + 10;
        int nTop  = rcItem.top + 60;
        Rect rcText = new Rect();
        if (m_dataItem.m_strTitle.length() > 0) {
            m_pntTextTitle.getTextBounds(m_dataItem.m_strTitle, 0, m_dataItem.m_strTitle.length(), rcText);
            int nStart = rcItem.left + ((rcItem.right - rcItem.left) - (rcText.right - rcText.left))/ 2;
            canvas.drawText(m_dataItem.m_strTitle, nStart, nTop, m_pntTextTitle);
        }

        nTop  = nTop + 72;
        canvas.drawText(m_dataItem.m_strFirstLine, nLeft, nTop, m_pntTextItem);
        nTop  = rcItem.bottom - 8;
        canvas.drawText(m_dataItem.m_strTime, nLeft, nTop, m_pntTextItem);

        m_pntTextItem.getTextBounds(m_dataItem.m_strType, 0, m_dataItem.m_strType.length(), rcText);
        int nStart = rcItem.right - (rcText.right - rcText.left) - 12;
        canvas.drawText(m_dataItem.m_strType, nStart, nTop, m_pntTextItem);


    }
}
