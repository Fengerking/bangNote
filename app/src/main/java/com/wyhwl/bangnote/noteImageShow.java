package com.wyhwl.bangnote;

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
import android.media.ExifInterface;

public class noteImageShow extends ImageView {
    private static final int MODE_NONE = 0;
    private static final int MODE_DRAG = 1;
    private static final int MODE_ZOOM = 2;

    private Context     m_context   = null;
    private String      m_strFile   = null;
    private int         m_nMode     = MODE_NONE;
    private float       m_oldDist   = 0;
    public  Matrix      m_nowMatrix = new Matrix();
    private Matrix      m_oldMatrix = new Matrix();
    private PointF      m_ptStart   = new PointF();
    private PointF      m_ptMid     = new PointF();

    private boolean     m_bCanZoom  = false;
    public int          m_nBmpWidth = 0;
    public int          m_nBmpHeight= 0;

    private noteImageShowListener   m_imgListener = null;

    // The event listener function
    public interface noteImageShowListener{
        public int onNoteImageShowEvent (View view, MotionEvent ev);
    }

    public void setNoteImageShowListener (noteImageShowListener listener) {
        m_imgListener = listener;
    }

    public noteImageShow(Context context) {
        super(context);
        m_context = context;
    }

    public noteImageShow(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_context = context;
    }

    public noteImageShow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public String getImageFile () {
        return m_strFile;
    }

    public void setImageFile (String strFile, boolean bCanZoom) {
        m_strFile = strFile;
        m_bCanZoom = bCanZoom;
        setScaleType(ImageView.ScaleType.MATRIX);

        try {
            FileInputStream fis = new FileInputStream(strFile);
            Bitmap bmp = BitmapFactory.decodeStream(fis);
            fis.close();

            Matrix matBmp = null;
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

            m_nBmpWidth = bmp.getWidth();
            m_nBmpHeight = bmp.getHeight();
            if (matBmp != null) {
                m_nBmpWidth = bmp.getHeight();
                m_nBmpHeight = bmp.getWidth();
                Bitmap bmpNew = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matBmp, true);
                setImageBitmap(bmpNew);
            } else {
                setImageBitmap(bmp);
            }

            DisplayMetrics dm = this.getResources().getDisplayMetrics();
            float scale = (float)dm.widthPixels / m_nBmpWidth;

            if (!m_bCanZoom) {
                ViewGroup.LayoutParams param = (ViewGroup.LayoutParams) getLayoutParams();
                param.height = (int) (m_nBmpHeight * scale);
                setLayoutParams(param);
            }

            m_nowMatrix.postScale(scale, scale, 0, 0);
            if (m_bCanZoom) {
                int nHeight = (int)(m_nBmpHeight * scale);
                if (nHeight < dm.heightPixels) {
                    m_nowMatrix.postTranslate(0, (dm.heightPixels - nHeight) / 2);
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
            case MotionEvent.ACTION_POINTER_UP:
                m_nMode = MODE_NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (m_nMode == MODE_DRAG) {
                    m_nowMatrix.set(m_oldMatrix);
                    m_nowMatrix.postTranslate(event.getX() - m_ptStart.x, event.getY() - m_ptStart.y);
                } else if (m_nMode == MODE_ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        m_nowMatrix.set(m_oldMatrix);
                        float scale = newDist / m_oldDist;
                        m_nowMatrix.postScale(scale, scale, m_ptMid.x, m_ptMid.y);
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

}