package com.wyhwl.bangnote;

import android.content.Context;
import android.util.AttributeSet;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class noteShowSlider extends noteBaseSlider {
    private String              LOG_TAG = "noteShowSlider";
    private int                 m_nItemCount = 0;

    private String              m_strNoteFile = null;

    public noteShowSlider(Context context) {
        super(context);
    }
    public noteShowSlider(Context context, AttributeSet attrs) {
        super(context, attrs); }
    public noteShowSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, 0, r, b);
    }

    public void setNoteFile (String strFile) {
        m_strNoteFile = strFile;
        m_nItemCount = noteConfig.m_lstData.m_lstSelItem.size();

        noteShowLayout layItem = null;
        for (int i = 0;i < 3; i++) {
            layItem = new noteShowLayout(m_context);
            addView(layItem);
            m_lstChildView.add(layItem);
            if (m_nItemCount == 1) {
                layItem.setNoteFile(m_strNoteFile);
                return;
            }
        }
        updateNoteFile (true);
        m_nCurPage = 1;
        postDelayed(()->scrollTo(getWidth(), 0), 100);

        Log.v (LOG_TAG, "m_nItemCount = " + m_nItemCount);
    }

    private void updateNoteFile (boolean bInit) {
        m_nItemCount = noteConfig.m_lstData.m_lstSelItem.size();
        if (m_nItemCount == 1)
            return;

        int             nCurFile = -1;
        dataNoteItem    dataItem = null;
        for (int i = 0; i < noteConfig.m_lstData.m_lstSelItem.size(); i++) {
            dataItem = noteConfig.m_lstData.m_lstSelItem.get(i);
            if (dataItem.m_strFile.compareTo(m_strNoteFile) == 0) {
                nCurFile = i;
                break;
            }
        }

        noteShowLayout noteView = null;
        if (bInit) {
            noteView = (noteShowLayout)m_lstChildView.get(1);
            noteView.setNoteFile(m_strNoteFile);
        }

        if (m_nItemCount == 2) {
            int nIndex = 0;
            if (nCurFile == 0)
                nIndex = 1;
            Log.v (LOG_TAG, "Index nCurFile = " + nCurFile + "   nIndex = " + nIndex);
            noteView = (noteShowLayout)m_lstChildView.get(0);
            if (noteView.getNoteFile() == null) {
                noteView.setNoteFile(noteConfig.m_lstData.m_lstSelItem.get(nIndex).m_strFile);
            }
            noteView = (noteShowLayout)m_lstChildView.get(2);
            if (noteView.getNoteFile() == null) {
                noteView.setNoteFile(noteConfig.m_lstData.m_lstSelItem.get(nIndex).m_strFile);
            }
            return;
        }

        int nNextFile = nCurFile;
        int nPrevFile = nCurFile;
        if (nNextFile == m_nItemCount - 1)
            nNextFile = 0;
        else
            nNextFile++;
        if (nPrevFile == 0)
            nPrevFile = m_nItemCount - 1;
        else
            nPrevFile--;

        Log.v (LOG_TAG, "Index nCurFile = " + nCurFile + "   Next = " + nNextFile + "  Prev = " + nPrevFile);

        noteView = (noteShowLayout)m_lstChildView.get(0);
        if (noteView.getNoteFile() == null) {
            if (bInit)
                noteView.setNoteFile(noteConfig.m_lstData.m_lstSelItem.get(nNextFile).m_strFile);
            else
                noteView.setNoteFile(noteConfig.m_lstData.m_lstSelItem.get(nPrevFile).m_strFile);
        }
        noteView = (noteShowLayout)m_lstChildView.get(2);
        if (noteView.getNoteFile() == null) {
            if (bInit)
                noteView.setNoteFile(noteConfig.m_lstData.m_lstSelItem.get(nPrevFile).m_strFile);
            else
                noteView.setNoteFile(noteConfig.m_lstData.m_lstSelItem.get(nNextFile).m_strFile);
        }
    }

    protected void onStartSlider () {
    }

    protected void needUpdateView (View view) {
        noteShowLayout noteView = (noteShowLayout)m_lstChildView.get(m_nCurPage);
        m_strNoteFile = noteView.getNoteFile();

        ((noteShowLayout)view).setNoteFile(null);
        updateNoteFile(false);
    }
}
