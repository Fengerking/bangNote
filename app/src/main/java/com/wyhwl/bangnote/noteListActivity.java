package com.wyhwl.note;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

public class noteListActivity extends AppCompatActivity
                              implements noteListSlider.switchListener {

    private noteListListView        m_lstView = null;
    private noteListSlider          m_sldList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        m_sldList = (noteListSlider)findViewById(R.id.sldList);
        m_sldList.setSwitchListener(this);

        m_lstView = (noteListListView)findViewById(R.id.vwNoteList);
        m_lstView.setAdapter(new noteListAdapter(this));
    }

    public int onSwitchStart (int nView){
        return 0;
    }

    public int OnSwitchEnd (int nView) {
        return 0;
    }

    public int onMotionEvent (MotionEvent ev) {
        m_lstView.onTouchEvent(ev);
        return 0;
    }
}
