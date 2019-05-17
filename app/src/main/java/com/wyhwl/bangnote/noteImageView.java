package com.wyhwl.bangnote;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class noteImageView extends ImageView {
    private Context     m_context = null;
    private int         m_nID = 0;
    private String      m_strFileName = null;

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

    public void initImageView (Context context) {
        m_context = context;
        m_nID = noteConfig.getImagViewID ();
        setScaleType(ScaleType.FIT_XY);
        setBackgroundColor(Color.argb(0, 0, 0, 0));
    }

    public int getId() {
        return m_nID;
    }

    public int setImageFile (String strFile) {
        int nFind = strFile.indexOf(noteConfig.m_strNotePictPath);
        if (nFind >= 0) {
            m_strFileName = strFile;
        } else {
            m_strFileName = noteConfig.getNotePictFile();
            try {
                FileInputStream fis = new FileInputStream (strFile);
                FileOutputStream fos = new FileOutputStream (m_strFileName);

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
}
