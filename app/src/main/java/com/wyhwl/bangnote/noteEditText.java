package com.wyhwl.bangnote;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

public class noteEditText extends EditText {
    private Context                 m_context = null;
    private onTouchEventListener    m_touchListener = null;

    // The event listener function
    public interface onTouchEventListener{
        public void onTouch (MotionEvent ev);
    }

    public void setOnTouchEventListener (onTouchEventListener touchListener) {
        m_touchListener = touchListener;
    }

    public noteEditText(Context context) {
        super(context);
        m_context = context;
    }
    public noteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_context = context;
    }
    public noteEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        m_context = context;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (m_touchListener != null)
            m_touchListener.onTouch(ev);
        //return super.onTouchEvent(ev);
        return true;
    }
}
