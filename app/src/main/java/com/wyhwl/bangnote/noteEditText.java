package com.wyhwl.bangnote;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;


public class noteEditText extends EditText {
    private Context             m_context = null;
    private int                 m_nID = 0;
    private onNoteEditListener  m_editListener = null;

    public noteEditText(Context context) {
        super(context);
        initEditText(context);
    }
    public noteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initEditText(context);
    }
    public noteEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initEditText(context);
    }

    // The event listener function
    public interface onNoteEditListener{
        public void onMontionEvent (MotionEvent ev, int nID);
        public void onTextChanged (int nID);
    }

    public void setOnNoteEditListener (onNoteEditListener listener) {
        m_editListener = listener;
    }

    public int getId() {
        if (m_nID == 0)
            m_nID = noteConfig.getNoteEditID ();
        return m_nID;
    }

    private void initEditText (Context context) {
        m_context = context;
        setMinHeight(100);
        setHint("插入文字");
        setTextSize(noteConfig.m_nTextSize);
        setTextColor(noteConfig.m_nTextColor);
        setBackground(null);

        super.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            public void afterTextChanged(Editable s) {
                if (m_editListener != null)
                    m_editListener.onTextChanged(m_nID);
            }
        });
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean bRC = super.onTouchEvent(ev);
        if (m_editListener != null)
            m_editListener.onMontionEvent(ev, m_nID);
        return bRC;
    }
}
