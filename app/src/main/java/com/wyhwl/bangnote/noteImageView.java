package com.wyhwl.bangnote;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;

public class noteImageView extends ImageView {
    private Context     m_context = null;
    private int         m_nID = 0;

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
        setMinimumHeight(300);
        setBackgroundColor(Color.argb(0, 0, 0, 0));
    }

    public int getId() {
        if (m_nID == 0)
            m_nID = noteConfig.getNoteEditID ();
        return m_nID;
    }
}
