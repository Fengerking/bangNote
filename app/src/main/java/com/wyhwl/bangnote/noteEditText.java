package com.wyhwl.bangnote;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;


public class noteEditText extends EditText {
    private Context             m_context = null;
    private boolean             m_bCreate = true;
    private int                 m_nID = 0;
    private onNoteEditListener  m_editListener = null;

    public noteEditText(Context context) {
        super(context);
        initEditText(context, true);
    }
    public noteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initEditText(context, false);
    }
    public noteEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initEditText(context, false);
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
        return m_nID;
    }

    private void initEditText (Context context, boolean bCreate) {
        m_context = context;
        m_bCreate = bCreate;
        m_nID = noteConfig.getNoteEditID ();
        setMinHeight(100);
        setHint("插入文字");
        setTextSize(noteConfig.m_nTextSize);
        setTextColor(noteConfig.m_nTextColor);
        if (m_bCreate) {
            setBackground(null);
        } else {
            m_nID = 1;
        }

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
        if (m_editListener != null)
            m_editListener.onMontionEvent(ev, m_nID);
        return super.onTouchEvent(ev);
    }


}
