package com.wyhwl.bangnote;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import com.wyhwl.bangnote.base.dataNoteItem;
import com.wyhwl.bangnote.base.noteConfig;
import com.wyhwl.bangnote.base.noteDateAdapter;
import com.wyhwl.bangnote.base.noteListAdapter;
import com.wyhwl.bangnote.view.*;

public class noteCalendarActivity extends AppCompatActivity
                                    implements View.OnClickListener,
                                    AdapterView.OnItemClickListener,
                                    noteCalendarView.noteCalendarListener {
    private ListView            m_lstViewYear;
    private ListView            m_lstViewMonth;
    private noteCalendarView    m_vwCalendar = null;
    private TextView            m_txtDate = null;

    private ListView            m_lstViewNote = null;
    private noteDateAdapter     m_noteAdapter = null;

    private int                 m_nYear = 0;
    private int                 m_nMonth = 0;
    private int                 m_nDay = 0;

    private boolean             m_bDayMode = true;
    private boolean             m_bNoteAll = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_calendar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        initViews();
    }

    protected void onResume () {
        super.onResume();
        if (noteConfig.m_bNoteModified) {
            onNoteDateChange(m_vwCalendar, m_nYear, m_nMonth, m_nDay);
        }
    }

    private void initViews () {
        ((ImageButton)findViewById(R.id.imbBack)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbNoteSel)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbNoteAll)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbMonth)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbDay)).setOnClickListener(this);

        m_txtDate = (TextView)findViewById(R.id.txtCalendar);
        m_vwCalendar = (noteCalendarView)findViewById(R.id.cldDate);
        m_vwCalendar.setNoteDateChangeListener(this);

        m_lstViewYear = (ListView)findViewById(R.id.lstYear);
        m_lstViewYear.setOnItemClickListener(this);
        m_lstViewMonth = (ListView)findViewById(R.id.lstMonth);
        m_lstViewMonth.setOnItemClickListener(this);

        String[] strMonths = new String[]{"01","02", "03", "04", "05", "06","07","08", "09", "10", "11", "12"};
        m_lstViewMonth.setAdapter(new ArrayAdapter<String>(this, R.layout.view_calendar_item, strMonths));

        Date date = new Date(System.currentTimeMillis());
        int nYear = date.getYear() + 1900;
        String[]    strYears = new String[nYear-2000+1];
        for (int i = 2000; i <= nYear; i++)
            strYears[i-2000] = String.format("%d", i);
        m_lstViewYear.setAdapter(new ArrayAdapter<String>(this, R.layout.view_calendar_item, strYears));

        nYear = m_vwCalendar.getYear();
        int nMonth = m_vwCalendar.getMonth();
        m_lstViewYear.setSelection(nYear - 2000 - 5);

        String strDdte = String.format(" %d 年 %02d 月", nYear, nMonth);
        m_txtDate.setText(strDdte);

        m_lstViewNote = (ListView) findViewById(R.id.vwNoteList);
        m_lstViewNote.setOnItemClickListener(this);

        m_noteAdapter = new noteDateAdapter(this);
        onNoteDateChange (m_vwCalendar, m_vwCalendar.getYear(), m_vwCalendar.getMonth(), m_vwCalendar.getDay());
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == (View)m_lstViewYear) {
            ArrayAdapter adapter = (ArrayAdapter) m_lstViewYear.getAdapter();
            String strYear = (String) adapter.getItem(position).toString();
            int nYear = Integer.parseInt(strYear);
            m_vwCalendar.setYearMonth(nYear, -1);
        } else if (parent == (View)m_lstViewMonth) {
            ArrayAdapter adapter = (ArrayAdapter) m_lstViewMonth.getAdapter();
            String strMonth = (String) adapter.getItem(position).toString();
            int nMonth = Integer.parseInt(strMonth);
            m_vwCalendar.setYearMonth(-1, nMonth);
        } else if (parent == (View)m_lstViewNote) {
            dataNoteItem noteItem = (dataNoteItem)m_noteAdapter.getItem(position);
            Intent intent = new Intent(noteCalendarActivity.this, noteViewActivity.class);
            intent.setData(Uri.parse(noteItem.m_strFile));

            int nSize = m_noteAdapter.getCount();
            String[] strFileList = new String[nSize];
            for (int i = 0; i < nSize; i++) {
                strFileList[i] = ((dataNoteItem)m_noteAdapter.getItem(i)).m_strFile;
            }
            intent.putExtra("FileList", strFileList);
            intent.putExtra("FileCount", nSize);

            startActivityForResult(intent, 0);
            return;
        }

        int nYear = m_vwCalendar.getYear();
        int nMonth = m_vwCalendar.getMonth();
        String strDdte = String.format(" %d 年 %02d 月", nYear, nMonth);
        m_txtDate.setText(strDdte);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imbBack:
                finish();
                break;

            case R.id.imbNoteAll:
                if (!m_bNoteAll) {
                    m_bNoteAll = true;
                    m_vwCalendar.setNoteAll(true);
                }
                break;

            case R.id.imbNoteSel:
                if (m_bNoteAll) {
                    m_bNoteAll = false;
                    m_vwCalendar.setNoteAll(false);
                }
                break;

            case R.id.imbMonth:
                if (m_bDayMode) {
                    m_bDayMode = false;
                    onNoteDateChange(null, m_nYear, m_nMonth, m_nDay);
                }
                break;

            case R.id.imbDay:
                if (!m_bDayMode) {
                    m_bDayMode = true;
                    onNoteDateChange(null, m_nYear, m_nMonth, m_nDay);
                }
                break;
        }
    }

    public void onNoteDateChange (View view, int nYear, int nMonth, int nDay){
        m_nYear = nYear;
        m_nMonth = nMonth;
        m_nDay = nDay;
        String strDdte = String.format(" %d年 %02d月 %02d日", nYear, nMonth, nDay);
        m_txtDate.setText(strDdte);
        m_noteAdapter.m_lstItem.clear();
        String strDate = String.format("%d-%02d-%02d", nYear, nMonth, nDay);
        dataNoteItem dataItem = null;

        if (m_bDayMode && m_bNoteAll) {
            int nNoteSize = noteConfig.m_lstData.m_lstAllItem.size();
            for (int i = 0; i < nNoteSize; i++) {
                dataItem = noteConfig.m_lstData.m_lstAllItem.get(i);
                if (noteConfig.m_nShowSecurity == 0) {
                    if (dataItem.isSecurity())
                        continue;
                }
                if (strDate.compareTo(dataItem.m_strDate) == 0) {
                    m_noteAdapter.m_lstItem.add(dataItem);
                }
            }
        } else if (m_bDayMode && !m_bNoteAll) {
            int nNoteSize = noteConfig.m_lstData.m_lstSelItem.size();
            for (int i = 0; i < nNoteSize; i++) {
                dataItem = noteConfig.m_lstData.m_lstSelItem.get(i);
                if (noteConfig.m_nShowSecurity == 0) {
                    if (dataItem.isSecurity())
                        continue;
                }
                if (strDate.compareTo(dataItem.m_strDate) == 0) {
                    m_noteAdapter.m_lstItem.add(dataItem);
                }
            }
        } else if (!m_bDayMode && m_bNoteAll) {
            String strDate1 = String.format("%d-%02d-%02d", nYear, nMonth, 0);
            String strDate2 = String.format("%d-%02d-%02d", nYear, nMonth, 32);
            int nNoteSize = noteConfig.m_lstData.m_lstAllItem.size();
            for (int i = 0; i < nNoteSize; i++) {
                dataItem = noteConfig.m_lstData.m_lstAllItem.get(i);
                if (noteConfig.m_nShowSecurity == 0) {
                    if (dataItem.isSecurity())
                        continue;
                }
                if (strDate1.compareTo(dataItem.m_strDate) < 0 &&
                        strDate2.compareTo(dataItem.m_strDate) > 0) {
                    m_noteAdapter.m_lstItem.add(dataItem);
                }
            }
        } else if (!m_bDayMode && !m_bNoteAll) {
            String strDate1 = String.format("%d-%02d-%02d", nYear, nMonth, 0);
            String strDate2 = String.format("%d-%02d-%02d", nYear, nMonth, 32);
            int nNoteSize = noteConfig.m_lstData.m_lstSelItem.size();
            for (int i = 0; i < nNoteSize; i++) {
                dataItem = noteConfig.m_lstData.m_lstSelItem.get(i);
                if (noteConfig.m_nShowSecurity == 0) {
                    if (dataItem.isSecurity())
                        continue;
                }
                if (strDate1.compareTo(dataItem.m_strDate) < 0 &&
                        strDate2.compareTo(dataItem.m_strDate) > 0) {
                    m_noteAdapter.m_lstItem.add(dataItem);
                }
            }
        }

        m_lstViewNote.setAdapter(m_noteAdapter);
        m_lstViewNote.invalidate();

        Comparator comp = new dateComparator();
        Collections.sort(m_noteAdapter.m_lstItem, comp);
    }

    public class dateComparator implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            dataNoteItem noteItem1 = (dataNoteItem)o1;
            dataNoteItem noteItem2 = (dataNoteItem)o2;
            return noteItem2.m_strDateTime.compareTo(noteItem1.m_strDateTime);
        }
    }
}
