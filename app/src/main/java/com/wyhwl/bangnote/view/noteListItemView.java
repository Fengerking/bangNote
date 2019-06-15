package com.wyhwl.bangnote.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.util.Log;
import android.text.method.LinkMovementMethod;

import java.io.FileInputStream;

import com.wyhwl.bangnote.base.*;
import com.wyhwl.bangnote.R;

public class noteListItemView extends TextView {
    private Context         m_context = null;
    private dataNoteItem    m_dataItem = null;

    private Paint           m_pntTextLeft;
    private Paint           m_pntTextTitle;
    private Paint           m_pntTextItem;

    private Paint           m_pntLeft;
    private Paint           m_pntRect;
    private Paint           m_pntSelect;

    public noteListItemView(Context context) {
        super(context);
        init (context);
    }
    public noteListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init (context);
    }
    public noteListItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init (context);
    }

    public void init (Context context) {
        m_context = context;
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
        m_pntLeft.setColor(0XFFCCCCCC);

        m_pntRect =  new Paint();
        m_pntRect.setColor(0XFF444444);

        m_pntSelect =  new Paint();
        m_pntSelect.setColor(0XFF999999);
    }

    public void setDataList (dataNoteItem noteItem) {
        m_dataItem = noteItem;
        invalidate();
    }

    public dataNoteItem getDataList () {
        return m_dataItem;
    }

    protected void onDraw(Canvas canvas) {
        if (m_dataItem == null)
            return;

        int nW = getWidth();
        int nH = getHeight();

        int nL = 150;
        int nY = 90;
        int nC = 45;

        if (m_dataItem.isSelect()) {
            Rect rcView = new Rect(4, 4, nW - 4, nH - 4);
            canvas.drawRect(rcView, m_pntSelect);
        }

        canvas.drawLine(nL, 0, nL, nH, m_pntLeft);
        canvas.drawCircle(nL, nY, nC, m_pntLeft);

        String strDraw = m_dataItem.m_strDate.substring(5, 7);
        canvas.drawText(strDraw, 48, nY, m_pntTextLeft);
        strDraw = m_dataItem.m_strDate.substring(0, 4);
        canvas.drawText(strDraw, 4, nY + 50, m_pntTextLeft);
        m_pntTextLeft.setTextSize(50);
        strDraw = m_dataItem.m_strDate.substring(8, 10);
        canvas.drawText(strDraw, nL - nC + 18, nY + 18, m_pntTextLeft);

        Rect    rcItem = new Rect(nL + 60, 16, nW - 16, nH - 16);
        RectF   rcItemf = new RectF(nL + 60, 16, nW - 16, nH - 16);
        m_pntRect.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rcItemf, 24, 24, m_pntRect);

        Bitmap bmpItem = null;
        if (m_dataItem.m_strImgFile != null) {
            try {
                noteFileInputStream fis = new noteFileInputStream (m_dataItem.m_strImgFile);
                bmpItem = BitmapFactory.decodeStream(fis);
                fis.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        } else if (m_dataItem.m_strAudFile != null) {
            bmpItem = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.note_music_icon);
        } else {
            bmpItem = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.noteitem_icon);
        }
        if (bmpItem != null) {
            int nOff = 12;
            Rect rcSrc = new Rect(0, 0, bmpItem.getWidth(), bmpItem.getHeight());
            Rect rcDst = new Rect(rcItem.left + nOff, rcItem.top + nOff, rcItem.left + (rcItem.bottom - rcItem.top) - nOff * 2, rcItem.bottom - nOff * 2);
            canvas.drawBitmap(bmpItem, rcSrc, rcDst, m_pntRect);

            m_pntRect.setStrokeWidth((float) 24.0);
            m_pntRect.setStyle(Paint.Style.STROKE);
            RectF rcBmp = new RectF (rcDst);
            canvas.drawRoundRect(rcBmp, 24, 24, m_pntRect);
        }

        int nLeft = rcItem.left + 10 + rcItem.bottom - rcItem.top;
        int nTop  = rcItem.top + 60;
        String strTitle = "无标题";
        if (m_dataItem.m_strTitle.length() > 0)
            strTitle = m_dataItem.m_strTitle;
        Rect rcDraw = new Rect (nLeft, nTop, rcItem.right - 8, rcItem.bottom - 8);
        drawRectText(strTitle, rcDraw, canvas, m_pntTextTitle, 1);

        rcDraw.top += 72;
        drawRectText(m_dataItem.m_strFirstLine, rcDraw, canvas, m_pntTextItem, 0);
        rcDraw.top = rcItem.bottom - 16;
        drawRectText(m_dataItem.m_strTime, rcDraw, canvas, m_pntTextItem, 0);

        drawRectText(m_dataItem.m_strType, rcDraw, canvas, m_pntTextItem, 2);
    }

    private void drawRectText (String strText, Rect rcDraw, Canvas canvas, Paint paint, int nType) {
        Rect rcText = new Rect();
        int nTextLen = strText.length();
        paint.getTextBounds(strText, 0, nTextLen, rcText);
        while ((rcText.right - rcText.left) > (rcDraw.right - rcDraw.left)) {
            nTextLen--;
            paint.getTextBounds(strText, 0, nTextLen, rcText);
        }

        String strDraw = strText.substring(0, nTextLen);
        int nStart = rcDraw.left;
        if (nType == 1) { // center
            nStart = rcDraw.left + ((rcDraw.right - rcDraw.left) - (rcText.right - rcText.left))/ 2;
        } else if (nType == 2) {  // right
            nStart = rcDraw.right - rcText.right;
        }
        canvas.drawText(strDraw, 0, nTextLen, nStart, rcDraw.top + rcText.bottom, paint);
    }
}
