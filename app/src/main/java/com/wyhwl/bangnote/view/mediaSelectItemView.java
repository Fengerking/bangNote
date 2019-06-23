package com.wyhwl.bangnote.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.wyhwl.bangnote.R;
import com.wyhwl.bangnote.base.mediaSelectAdapter;
import com.wyhwl.bangnote.base.mediaSelectAdapter.*;
import com.wyhwl.bangnote.base.noteFileInputStream;

import java.io.FileInputStream;

public class mediaSelectItemView extends View {
    private     Context     m_context = null;
    private     mediaItem   m_itmMedia = null;

    private     Paint       m_pntRect;
    private     Paint       m_pntText;

    public mediaSelectItemView(Context context) {
        super(context);
        init (context);
    }
    public mediaSelectItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init (context);
    }
    public mediaSelectItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init (context);
    }

    public void init (Context context) {
        m_context = context;

        m_pntRect = new Paint();
        m_pntRect.setTextSize(40);
        m_pntRect.setColor(0XFFFFFFFF);

        m_pntText = new Paint();
        m_pntText.setTextSize(48);
        m_pntText.setColor(0XFFCCCCCC);
    }

    public void setMediaItem (mediaItem item) {
        m_itmMedia = item;
    }
    public mediaItem getMediaItem () {
        return m_itmMedia;
    }

    protected void onDraw(Canvas canvas) {
        if (m_itmMedia == null)
            return;

        int nW = getWidth();
        int nH = getHeight();

        Bitmap bmpItem = null;
        if (m_itmMedia.m_nType == mediaSelectAdapter.m_nMediaFolder) {
            bmpItem = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.media_folder);
        } else if (m_itmMedia.m_nType == mediaSelectAdapter.m_nMediaAudio) {
            bmpItem = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.media_audio);
        } else if (m_itmMedia.m_nType == mediaSelectAdapter.m_nMediaVideo) {
            bmpItem = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.media_video);
        } else if (m_itmMedia.m_nType == mediaSelectAdapter.m_nMediaBack) {
            bmpItem = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.media_back);
        } else {
            if (m_itmMedia.m_thumb == null)
                bmpItem = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.media_image);
            else
                bmpItem = m_itmMedia.m_thumb;
        }

        int nTextH = 80;
        if (bmpItem != null) {
            int nOff = 12;
            if (m_itmMedia.m_nType == mediaSelectAdapter.m_nMediaBack)
                nOff = 48;
            Rect rcSrc = new Rect(0, 0, bmpItem.getWidth(), bmpItem.getHeight());
            Rect rcDst = new Rect(nOff, nOff, nW-nOff, nH-nTextH-nOff);
            canvas.drawBitmap(bmpItem, rcSrc, rcDst, m_pntRect);
            if (bmpItem != m_itmMedia.m_thumb)
                bmpItem.recycle();
        }

        if (m_itmMedia.m_nType >= mediaSelectAdapter.m_nMediaImage && m_itmMedia.m_nType <= mediaSelectAdapter.m_nMediaVideo) {
            bmpItem = null;
            if (m_itmMedia.m_nSelect > 0) {
                bmpItem = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.media_select);
            } else {
                //bmpItem = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.media_unselect);
            }
            if (bmpItem != null) {
                int nOff = 120;
                Rect rcSrc = new Rect(0, 0, bmpItem.getWidth(), bmpItem.getHeight());
                Rect rcDst = new Rect(nW - nOff, 0, nW, nOff);
                canvas.drawBitmap(bmpItem, rcSrc, rcDst, m_pntRect);
                bmpItem.recycle();

                String strNum = String.format("%d", m_itmMedia.m_nSelect);
                m_pntText.setColor(0XFF000000);
                rcDst.top = 50;
                if (m_itmMedia.m_nSelect >= 10)
                    rcDst.left += 10;
                else
                    rcDst.left += 30;
                drawRectText (strNum, rcDst, canvas, m_pntText, 0);
            }
        }

        Rect rcText = new Rect(4, nH - 30, nW - 4, nH - 4);
        m_pntText.setColor(0XFFCCCCCC);
        drawRectText (m_itmMedia.m_strName, rcText, canvas, m_pntText, 1);
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
        canvas.drawText(strDraw, 0, nTextLen, nStart, rcDraw.top, paint);
    }
}
