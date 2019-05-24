package com.wyhwl.bangnote;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class noteAudioPlayView extends FrameLayout
                                    implements View.OnClickListener{
    private Context     m_context = null;
    private boolean     m_bCreate = true;

    private ImageButton m_btnPlay   = null;


    public noteAudioPlayView(Context context) {
            super(context);
            initView(context, true);
        }
    public noteAudioPlayView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initView(context, false);
        }
    public noteAudioPlayView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            initView(context, false);
        }

    public void initView (Context context, boolean bCreate) {
        m_context = context;
        m_bCreate = bCreate;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAudioPause:
                break;
        }
    }

}
