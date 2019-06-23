package com.wyhwl.bangnote.view;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class noteImageSlider extends noteBaseSlider
                            implements noteImageShow.noteImageShowListener{
    private String              LOG_TAG = "noteImageSlider";
    private int                 m_nFileCount = 0;
    private int                 m_nFileIndex = 0;
    private ArrayList<String>   m_lstFiles = new ArrayList<String>();
    private noteImageShow.noteImageShowListener m_imgListener = null;

    public noteImageSlider(Context context) {
        super(context);
    }
    public noteImageSlider(Context context, AttributeSet attrs) {
        super(context, attrs); }
    public noteImageSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnNoteImageShowListener (noteImageShow.noteImageShowListener listener) {
        m_imgListener = listener;
    }

    public void setImageFiles (String[] strFileList, int nCount, int nIndex) {
        for (int i = 0; i < nCount; i++) {
            m_lstFiles.add(strFileList[i]);
        }
        m_nFileCount = nCount;
        m_nFileIndex = nIndex;

        noteImageShow imgView = null;
        for (int i = 0;i < 3; i++) {
            imgView = new noteImageShow(m_context, true);
            addView(imgView);
            imgView.setNoteImageShowListener(this);
            m_lstChildView.add(imgView);
            if (m_nFileCount == 1) {
                imgView.setImageFile(m_lstFiles.get(m_nFileIndex), true);
                return;
            }
        }
        updateImageFile (true);
        m_nCurPage = 1;
        postDelayed(()->scrollTo(getWidth(), 0), 100);
    }

    public noteImageShow getCurView () {
        return (noteImageShow)m_lstChildView.get(1);
    }

    private void updateImageFile (boolean bInit) {
        if (m_nFileCount == 1)
            return;

        noteImageShow noteView = null;
        if (bInit) {
            noteView = (noteImageShow)m_lstChildView.get(1);
            noteView.setImageFile(m_lstFiles.get(m_nFileIndex), true);
        }

        if (m_nFileCount == 2) {
            int nIndex = 0;
            if (m_nFileIndex == 0)
                nIndex = 1;
            Log.v (LOG_TAG, "Index nCurFile = " + m_nFileIndex + "   nIndex = " + nIndex);
            noteView = (noteImageShow)m_lstChildView.get(0);
            if (noteView.getImageFile() == null) {
                noteView.setImageFile(m_lstFiles.get(nIndex), true);
            }
            noteView = (noteImageShow)m_lstChildView.get(2);
            if (noteView.getImageFile() == null) {
                noteView.setImageFile(m_lstFiles.get(nIndex), true);
            }
            return;
        }

        int nNextFile = m_nFileIndex;
        int nPrevFile = m_nFileIndex;
        if (nNextFile == m_nFileCount - 1)
            nNextFile = 0;
        else
            nNextFile++;
        if (nPrevFile == 0)
            nPrevFile = m_nFileCount - 1;
        else
            nPrevFile--;

        noteView = (noteImageShow)m_lstChildView.get(0);
        if (noteView.getImageFile() == null) {
            if (bInit)
                noteView.setImageFile(m_lstFiles.get(nPrevFile), true);
            else
                noteView.setImageFile(m_lstFiles.get(nNextFile), true);
        }
        noteView = (noteImageShow)m_lstChildView.get(2);
        if (noteView.getImageFile() == null) {
            if (bInit)
                noteView.setImageFile(m_lstFiles.get(nNextFile), true);
            else
                noteView.setImageFile(m_lstFiles.get(nPrevFile), true);
        }
    }

    public int onNoteImageShowEvent (View view, MotionEvent ev){
        super.onTouchEvent(ev);
        if (m_imgListener != null)
            m_imgListener.onNoteImageShowEvent(view, ev);
        return 0;
    }

    protected void needUpdateView (View view) {
        noteImageShow noteView = (noteImageShow)m_lstChildView.get(m_nCurPage);
        String strImgFile = noteView.getImageFile();
        for (int i = 0; i < m_nFileCount; i++) {
            if (strImgFile.compareTo(m_lstFiles.get(i)) == 0) {
                m_nFileIndex = i;
                break;
            }
        }

        ((noteImageShow)view).setImageFile(null, true);
        updateImageFile(false);
    }
}
