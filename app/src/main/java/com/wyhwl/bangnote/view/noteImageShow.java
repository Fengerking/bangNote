package com.wyhwl.bangnote.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ImageView;

import android.util.Log;

import java.io.FileInputStream;
import java.io.File;
import android.media.ExifInterface;

import com.wyhwl.bangnote.R;
import com.wyhwl.bangnote.base.*;

public class noteImageShow extends ImageView {
    private static final int MODE_NONE = 0;
    private static final int MODE_DRAG = 1;
    private static final int MODE_ZOOM = 2;

    private Context     m_context   = null;
    private boolean     m_bImage    = true;
    private int         m_nID       = 0;
    private String      m_strFile   = null;
    private int         m_nMode     = MODE_NONE;
    private float       m_oldDist   = 0;
    public  Matrix      m_nowMatrix = new Matrix();
    private Matrix      m_oldMatrix = new Matrix();
    private PointF      m_ptStart   = new PointF();
    private PointF      m_ptMid     = new PointF();

    private long        m_nLastClickTime = 0;

    private boolean     m_bCanZoom  = false;
    public int          m_nBmpWidth = 0;
    public int          m_nBmpHeight= 0;

    public int          m_nScrWidth = 0;
    public int          m_nScrHeight = 0;

    public float        m_fMovScale = 0;
    public float        m_fBmpScale = 1;

    public int          m_nOffsetX = 0;
    public int          m_nOffsetY = 0;
    public int          m_nOffMovX = 0;
    public int          m_nOffMovY = 0;

    private noteImageShowListener   m_imgListener = null;

    // The event listener function
    public interface noteImageShowListener{
        public int onNoteImageShowEvent (View view, MotionEvent ev);
    }

    public void setNoteImageShowListener (noteImageShowListener listener) {
        m_imgListener = listener;
    }

    public noteImageShow(Context context, boolean bImage) {
        super(context);
        m_bImage = bImage;
        initView (context);
    }

    public noteImageShow(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView (context);
    }

    public noteImageShow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView (context);
    }

    private void initView (Context context) {
        m_context = context;
        if (m_bImage)
            m_nID = noteConfig.getImagViewID ();
        else
            m_nID = noteConfig.getVidoViewID ();
        setScaleType(ImageView.ScaleType.MATRIX);
        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        m_nScrWidth = dm.widthPixels;
        m_nScrHeight = dm.heightPixels;
    }

    public int getId() {
        return m_nID;
    }

    public String getImageFile () {
        return m_strFile;
    }

    public void setImageFile (String strFile, boolean bCanZoom) {
        m_strFile = strFile;
        m_bCanZoom = bCanZoom;
        if (strFile == null)
            return;

        try {
            Matrix matBmp = null;
            Bitmap bmp = null;
            if (m_bImage) {
                FileInputStream fis = null;
                String strExt = strFile.substring(strFile.length() - 4);
                if (strExt.compareTo(".bnp") == 0)
                    fis = new noteFileInputStream(strFile);
                else
                    fis = new FileInputStream(strFile);
                bmp = BitmapFactory.decodeStream(fis);
                fis.close();

                ExifInterface ei = new ExifInterface(m_strFile);
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matBmp = new Matrix();
                        matBmp.postRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matBmp = new Matrix();
                        matBmp.postRotate(180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matBmp = new Matrix();
                        matBmp.postRotate(270);
                        break;
                }
            } else {
                String strThumbFile = strFile.substring(0, strFile.length()-3);
                strThumbFile += "tmb";
                File fileThumb = new File(strThumbFile);
                if (fileThumb.exists()) {
                    FileInputStream fis = new noteFileInputStream(strThumbFile);
                    bmp = BitmapFactory.decodeStream(fis);
                    fis.close();
                } else {
                    bmp = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.note_video_view);
                }
            }

            m_nBmpWidth = bmp.getWidth();
            m_nBmpHeight = bmp.getHeight();
            if (matBmp != null) {
                m_nBmpWidth = bmp.getHeight();
                m_nBmpHeight = bmp.getWidth();
                Bitmap bmpNew = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matBmp, true);
                setImageBitmap(bmpNew);
                bmp.recycle();
            } else {
                setImageBitmap(bmp);
            }

            float scale = (float)m_nScrWidth / m_nBmpWidth;
            if (!m_bCanZoom) {
                ViewGroup.LayoutParams param = (ViewGroup.LayoutParams) getLayoutParams();
                param.height = (int) (m_nBmpHeight * scale);
                setLayoutParams(param);
            }

            m_nowMatrix.reset();
            m_nowMatrix.setScale(scale, scale, 0, 0);
            m_fBmpScale = scale;

            m_nOffsetX = 0;
            m_nOffsetY = 0;
            if (m_bCanZoom) {
                int nHeight = (int)(m_nBmpHeight * scale);
                if (nHeight < m_nScrHeight) {
                    m_nOffsetY = (m_nScrHeight - nHeight) / 2;
                    m_nowMatrix.postTranslate(0, m_nOffsetY);
                    Log.e("DebugScale", "Trans 00  X= " + m_nOffsetX + "  Y=  " + m_nOffsetY);
                }
            }
            setImageMatrix(m_nowMatrix);
            invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!m_bCanZoom) {
            if (m_imgListener != null)
               m_imgListener.onNoteImageShowEvent(this, event);
            return false;
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                m_oldMatrix.set(m_nowMatrix);
                m_ptStart.set(event.getX(), event.getY());
                m_nMode = MODE_DRAG;
                break;
            // 多点触控
            case MotionEvent.ACTION_POINTER_DOWN:
                m_oldDist = spacing(event);
                if (m_oldDist > 10f) {
                    m_oldMatrix.set(m_nowMatrix);
                    midPoint(m_ptMid, event);
                    m_nMode = MODE_ZOOM;
                }
                break;

            case MotionEvent.ACTION_UP:
                m_nMode = MODE_NONE;
                // double click to zoom 1:1
                if (System.currentTimeMillis() - m_nLastClickTime < 250) {
                    if (m_fBmpScale == 1) {
                        m_fBmpScale = (float)m_nScrWidth / m_nBmpWidth;
                        m_nowMatrix.reset();
                        m_nowMatrix.setScale(m_fBmpScale, m_fBmpScale);
                        m_nOffsetX = 0;
                        int nHeight = (int)(m_nBmpHeight * m_fBmpScale);
                        if (nHeight < m_nScrHeight) {
                            m_nOffsetY = (m_nScrHeight - nHeight) / 2;
                            m_nowMatrix.postTranslate(0, m_nOffsetY);
                         }
                    } else {
                        m_fBmpScale = 1;
                        m_nowMatrix.reset();
                        m_nowMatrix.setScale(m_fBmpScale, m_fBmpScale);
                        m_nOffsetX = -(m_nBmpWidth - m_nScrWidth) / 2;
                        m_nOffMovY = -(m_nBmpHeight - m_nScrHeight) / 2;
                        m_nowMatrix.postTranslate(m_nOffsetX, m_nOffMovY);
                    }
                    setImageMatrix(m_nowMatrix);
                    invalidate();
                } else {
                    m_nLastClickTime = System.currentTimeMillis();
                    if (m_nOffMovX != 0 || m_nOffMovY != 0) {
                        m_nOffsetX += m_nOffMovX;
                        m_nOffsetY += m_nOffMovY;
                        m_nOffMovX = 0;
                        m_nOffMovY = 0;
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                m_nMode = MODE_NONE;
                if (m_fMovScale != 0) {
                     if (m_fMovScale > 1)
                        m_nOffsetX = m_nOffsetX - (int)((m_ptMid.x - m_nOffsetX) * (m_fMovScale - 1));
                    else
                        m_nOffsetX = m_nOffsetX + (int)((m_ptMid.x - m_nOffsetX) * (1 - m_fMovScale));
                    if (m_fMovScale > 1)
                        m_nOffsetY = m_nOffsetY - (int)((m_ptMid.y - m_nOffsetY) * (m_fMovScale - 1));
                    else
                        m_nOffsetY = m_nOffsetY + (int)((m_ptMid.y - m_nOffsetY) * (1 - m_fMovScale));
                    m_fBmpScale = m_fBmpScale * m_fMovScale;
                    m_fMovScale = 0;

                    if (m_fBmpScale * m_nBmpWidth < m_nScrWidth) {
                        m_fBmpScale = (float)m_nScrWidth / m_nBmpWidth;
                        m_nowMatrix.reset();
                        m_nowMatrix.setScale(m_fBmpScale, m_fBmpScale);

                        m_nOffsetX = 0;
                        m_nOffsetY = 0;
                        int nHeight = (int)(m_nBmpHeight * m_fBmpScale);
                        if (nHeight < m_nScrHeight) {
                            m_nOffsetY = (m_nScrHeight - nHeight) / 2;
                            m_nowMatrix.postTranslate(0, m_nOffsetY);
                        }
                        setImageMatrix(m_nowMatrix);
                        invalidate();
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (m_nMode == MODE_DRAG) {
                    m_nowMatrix.set(m_oldMatrix);
                    m_nOffMovX = (int)(event.getX() - m_ptStart.x);
                    m_nOffMovY = (int)(event.getY() - m_ptStart.y);
                    if (m_nOffsetX + m_nOffMovX > 0)
                        m_nOffMovX = -m_nOffsetX;
                    if (m_nScrWidth - m_nOffMovX - m_nOffsetX > m_nBmpWidth * m_fBmpScale)
                        m_nOffMovX = (int)(m_nScrWidth - m_nOffsetX - m_nBmpWidth * m_fBmpScale);
                    m_nowMatrix.postTranslate(m_nOffMovX, m_nOffMovY);
                    if (m_nOffsetX >= 0 && (event.getX() > m_ptStart.x)) {
                        if (m_imgListener != null)
                            m_imgListener.onNoteImageShowEvent(this, event);
                        return true;
                    }
                    if ((m_nScrWidth - m_nOffsetX + 1 >= m_nBmpWidth * m_fBmpScale) && (event.getX() < m_ptStart.x)) {
                        if (m_imgListener != null)
                            m_imgListener.onNoteImageShowEvent(this, event);
                        return true;
                    }
                    setImageMatrix(m_nowMatrix);
                    invalidate();
                    return true;
                } else if (m_nMode == MODE_ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        m_nowMatrix.set(m_oldMatrix);
                        float scale = newDist / m_oldDist;
                        m_nowMatrix.postScale(scale, scale, m_ptMid.x, m_ptMid.y);
                        m_fMovScale = scale;
                    }
                }
                setImageMatrix(m_nowMatrix);
                invalidate();
                break;
            }
        if (m_imgListener != null)
            m_imgListener.onNoteImageShowEvent(this, event);
        return true;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public void zoomIn () {
        m_nowMatrix.postScale((float)1.2, (float)1.2,  m_nScrWidth / 2, m_nScrHeight / 2);
        m_fBmpScale = (float)(m_fBmpScale * 1.2);
        setImageMatrix(m_nowMatrix);
        invalidate();
    }

    public void zoomOut () {
        m_nowMatrix.postScale((float)0.8, (float)0.8,  m_nScrWidth / 2, m_nScrHeight / 2);
        m_fBmpScale = (float)(m_fBmpScale * 0.8);
        setImageMatrix(m_nowMatrix);
        invalidate();
    }
}