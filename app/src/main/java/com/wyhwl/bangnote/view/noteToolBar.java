package com.wyhwl.bangnote.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class noteToolBar extends LinearLayout {
    private Context         m_context = null;

    public noteToolBar(Context context) {
        super(context);
        initBar(context);
    }
    public noteToolBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBar(context);
    }
    public noteToolBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBar(context);
    }

    private void initBar (Context context) {
        m_context = context;
    }

}
