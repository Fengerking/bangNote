package com.wyhwl.bangnote.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

import com.wyhwl.bangnote.base.*;

public class noteImageView extends ImageView {
    private Context                 m_context = null;
    private int                     m_nID = 0;
    private boolean                 m_bSelected = false;
    private String                  m_strFileName = null;
    private onNoteImageListener     m_lsnImage = null;

    public noteImageView(Context context) {
        super(context);
        initImageView(context);
    }
    public noteImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initImageView(context);
    }
    public noteImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initImageView(context);
    }

    // The event listener function
    public interface onNoteImageListener{
        public void onNoteImageEvent (MotionEvent ev, int nID);
    }

    public void setNoteImageListener (onNoteImageListener lsnImage) {
        m_lsnImage = lsnImage;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (m_lsnImage != null)
            m_lsnImage.onNoteImageEvent(ev, m_nID);
        return super.onTouchEvent(ev);
    }

    public void initImageView (Context context) {
        m_context = context;
        m_nID = noteConfig.getImagViewID ();
        setScaleType(ScaleType.FIT_XY);
        setBackgroundColor(Color.argb(0, 0, 0, 0));
    }

    public int getId() {
        return m_nID;
    }

    public int setImageFile (String strFile, boolean bRead) {
        if (bRead) {
            m_strFileName = strFile;
        } else {
            int nFind = strFile.indexOf(noteConfig.m_strNotePath);
            m_strFileName = noteConfig.getNotePictFile();
            try {
                if (true) {
                    Matrix matBmp = null;
                    ExifInterface ei = new ExifInterface(strFile);
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

                    FileInputStream fis = new FileInputStream (strFile);
                    FileOutputStream fos = new FileOutputStream(m_strFileName);
                    Bitmap bmp = BitmapFactory.decodeStream(fis);

                    if (matBmp != null) {
                        Bitmap bmpNew = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matBmp, true);
                        bmpNew.compress(Bitmap.CompressFormat.JPEG, 65, fos);
                        bmpNew.recycle();
                    } else {
                        bmp.compress(Bitmap.CompressFormat.JPEG, 65, fos);
                    }
                    bmp.recycle();
                    fis.close();
                    fos.close();
                } else {
                    FileInputStream fis = new FileInputStream(strFile);
                    FileOutputStream fos = new FileOutputStream(m_strFileName);

                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while (-1 != (byteRead = fis.read(buffer))) {
                        fos.write(buffer, 0, byteRead);
                    }

                    fis.close();
                    fos.flush();
                    fos.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            if (nFind >= 0) {
                File delFile = new File (strFile);
                delFile.delete ();
            }
        }

        try {
            FileInputStream fis = new FileInputStream (m_strFileName);
            Bitmap bmp = BitmapFactory.decodeStream(fis);
            setImageBitmap(bmp);
            fis.close();
        }catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = noteConfig.m_nImageHeight;
        setLayoutParams(params);

        return 1;
    }

    public String getImageFileName () {
        return m_strFileName;
    }

    public void setSelected (boolean bSelected) {
        if (m_bSelected == bSelected)
            return;
        m_bSelected = bSelected;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!m_bSelected)
            return;

        Paint paint = new Paint ();
        paint.setColor(0XFFFF0000);
        paint.setStrokeWidth((float)6.0);

        float[] pts={0,0,getWidth(),0,
                        getWidth(), 0, getWidth(), getHeight(),
                        getWidth(),getHeight(),0,getHeight(),
                        0, getHeight(),0, 0};

        canvas.drawLines(pts, paint);
    }
}
