package com.wyhwl.bangnote;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import android.util.Log;

import java.util.ArrayList;

public class noteShowLayout extends FrameLayout
        implements noteImageShow.noteImageShowListener,
        AdapterView.OnItemSelectedListener {
    private Context         m_context = null;
    private TextView        m_txtTitle = null;
    private TextView        m_txtDate = null;
    private TextView        m_txtTime = null;
    private Spinner         m_spnType = null;
    private TextView        m_txtWeather = null;
    private LinearLayout    m_layView = null;

    private String          m_strNoteFile = null;
    private dataNoteItem    m_dataItem = null;
    private int             m_nWordCount = 0;
    private noteImageShow   m_noteImage = null;
    private boolean         m_bReadFromFile = false;

    private int             m_nLastY = 0;
    private int             m_nDispH = 0;

    public noteShowLayout(Context context) {
        super(context);
        init(context);
    }

    public noteShowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public noteShowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        m_context = context;
        final View noteShowView = LayoutInflater.from(m_context).inflate(R.layout.note_item_view, null);
        addView(noteShowView);

        m_txtTitle = (TextView) noteShowView.findViewById(R.id.textTitle);
        m_txtDate = (TextView) noteShowView.findViewById(R.id.textDate);
        m_txtTime = (TextView) noteShowView.findViewById(R.id.textTime);
        m_txtWeather = (TextView) noteShowView.findViewById(R.id.textWeather);
        m_layView = (LinearLayout) noteShowView.findViewById(R.id.layView);
        m_spnType = (Spinner) noteShowView.findViewById(R.id.spinNoteType);
        m_spnType.setOnItemSelectedListener(this);

        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        m_nDispH = dm.heightPixels;
        m_dataItem = new dataNoteItem();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int y = (int) ev.getY();
        Log.e ("bangNoteDebug", "y = " + y);
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                m_nLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int nPos = m_layView.getScrollY();
                int nH = m_layView.getHeight();
                int dy = m_nLastY - y;
                if (dy < 0) {// move down
                    if (nPos < 0 || nPos + dy < 0)
                        dy = -nPos;
                } else {
                    if (nH - nPos < m_nDispH)
                        dy = (nH - nPos) - m_nDispH;
                    else if (dy > (nH - nPos) - m_nDispH)
                        dy = (nH - nPos) - m_nDispH;
                }

                //if (dy != 0 && nH > m_nDispH)
                    m_layView.scrollBy(0, dy);
                m_nLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    private void initSpinner() {
        ArrayList<String> lstType = noteConfig.m_noteTypeMng.getListName(false);
        lstType.remove(m_dataItem.m_strType);
        lstType.add(0, m_dataItem.m_strType);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(m_context, R.layout.spn_note_type, lstType);
        m_spnType.setAdapter(adapter);
    }

    public void setNoteFile(String strFile) {
        m_strNoteFile = strFile;
        if (m_strNoteFile == null)
            return;

        readFromFile();
    }

    public String getNoteFile () {
        return m_strNoteFile;
    }

    public int onNoteImageShowEvent(View view, MotionEvent ev) {
        return 0;
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

    }

    public void onNothingSelected(AdapterView<?> parent) {
    }


    public void onResizeView() {
        int nHeight = 0;
        int nCount = m_layView.getChildCount();
        for (int i = 0; i < nCount; i++) {
            nHeight += m_layView.getChildAt(i).getHeight();
            Log.e("bangNoteDebug", "Total height = " + nHeight + " i= " + i);
        }

        ViewGroup.LayoutParams param = (ViewGroup.LayoutParams) m_layView.getLayoutParams();
        param.height = nHeight + 600;

        Log.e("bangNoteDebug", "Total height = " + nHeight);

        m_layView.setLayoutParams(param);
        m_layView.scrollTo(0, 0);
    }

    private void readFromFile() {
        if (m_strNoteFile == null)
            return;

        while (m_layView.getChildCount() > 2)
            m_layView.removeView(m_layView.getChildAt((m_layView.getChildCount() - 1)));

        m_bReadFromFile = true;
        m_nWordCount = 0;
        m_dataItem.readFromFile(m_strNoteFile);
        m_txtTitle.setText(m_dataItem.m_strTitle);
        m_txtTime.setText(m_dataItem.m_strTime);
        m_txtWeather.setText(m_dataItem.m_strCity + " " + m_dataItem.m_strWeat);
        String strDate = m_dataItem.m_strDate + " " + noteConfig.getWeekDay(m_dataItem.m_strDate);
        m_txtDate.setText(strDate);

        initSpinner();

        dataNoteItem.dataContent dataItem = null;
        for (int i = 0; i < m_dataItem.m_lstItem.size(); i++) {
            TextView txtView = new TextView(m_context);
            txtView.setText("\n");
            txtView.setTextSize(4);
            m_layView.addView(txtView);

            dataItem = m_dataItem.m_lstItem.get(i);
            if (dataItem.m_nType == noteConfig.m_nItemTypeText) {
                txtView = new TextView(m_context);
                m_layView.addView(txtView);
                txtView.setText(dataItem.m_strItem);
                txtView.setTextSize(noteConfig.m_nTextSize);
                txtView.setTextColor(noteConfig.m_nTextColor);
                m_nWordCount += dataItem.m_strItem.length();
            } else if (dataItem.m_nType == noteConfig.m_nItemTypePict) {
                noteImageShow imgView = new noteImageShow(m_context);
                m_layView.addView(imgView);
                imgView.setImageFile(dataItem.m_strItem, false);
                imgView.setNoteImageShowListener(this);
            } else if (dataItem.m_nType == noteConfig.m_nItemTypeAudo) {
                noteAudioPlayView audView = new noteAudioPlayView(m_context);
                m_layView.addView(audView);
                ViewGroup.LayoutParams param = (ViewGroup.LayoutParams) audView.getLayoutParams();
                param.width = -1;
                audView.setLayoutParams(param);
                audView.setAudioFile(dataItem.m_strItem);
            }
        }
        m_bReadFromFile = false;

        post(() -> onResizeView());
    }
}
