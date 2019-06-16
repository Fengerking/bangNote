package com.wyhwl.bangnote.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.Date;

import com.wyhwl.bangnote.base.LunarCalendar;
import com.wyhwl.bangnote.base.noteConfig;
import com.wyhwl.bangnote.base.dataNoteItem;

public class noteCalendarView extends View {
    private Context     m_context = null;

    private int         m_nYear = 0;
    private int         m_nMonth = 0;
    private int         m_nDate = 0;
    private boolean     m_bNoteAll = true;

    private int         m_nWeekDay = 0;
    private String[]    m_strDays = null;
    private int[]       m_nMonthDyas = null;
    private int         m_nPrevDays = 0;
    private int         m_nNextStart = 0;

    private int         m_nIndexSelect = 0;

    private int         m_nSelYear = 0;
    private int         m_nSelMonth = 0;
    private int         m_nSelDate = 0;

    private int         m_nStartY = 0;
    private int         m_nStartX = 0;
    private int         m_nStepY = 0;
    private int         m_nStepX = 0;

    private Paint       m_pntRect = null;
    private Paint       m_pntTextWeek = null;
    private Paint       m_pntTextOutD = null;
    private Paint       m_pntTextDate = null;
    private Paint       m_pntTextSlct = null;
    private Paint       m_pntTextNote = null;

    private VelocityTracker mVelocityTracker = null;
    private int             mMaxVelocity;

    private LunarCalendar   m_lunar = null;

    private noteCalendarListener   m_listener = null;

    // The event listener function
    public interface noteCalendarListener {
        public void onNoteDateChange (View view, int nYear, int nMonth, int nDay);
    }

    public void setNoteDateChangeListener (noteCalendarListener listener) {
        m_listener = listener;
    }

    public noteCalendarView(Context context) {
        super(context);
        initView (context);
    }
    public noteCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView (context);
    }
    public noteCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView (context);
    }

    public void initView (Context context) {
        m_context = context;
        m_pntRect = new Paint();
        m_pntRect.setColor(0XFF444444);

        m_pntTextWeek = new Paint();
        m_pntTextWeek.setColor(0XFFAAAAAA);
        m_pntTextWeek.setTextSize(60);

        m_pntTextOutD = new Paint();
        m_pntTextOutD.setColor(0XFF888888);
        m_pntTextOutD.setTextSize(60);

        m_pntTextDate = new Paint();
        m_pntTextDate.setColor(0XFFCCCCCC);
        m_pntTextDate.setTextSize(60);

        m_pntTextSlct = new Paint();
        m_pntTextSlct.setColor(0XFFAAAA00);
        m_pntTextSlct.setTextSize(60);

        m_pntTextNote = new Paint();
        m_pntTextNote.setColor(0XFF222222);
        m_pntTextNote.setTextSize(20);

        m_lunar = new LunarCalendar();

        m_strDays = new String[42];
        m_nMonthDyas = new int[]{31,28,31,30,31,30,31,31,30,31,30,31};
        Date date = new Date(System.currentTimeMillis());
        m_nYear = date.getYear() + 1900;
        m_nMonth = date.getMonth() + 1;
        m_nDate = date.getDate();
        updateDate();

        ViewConfiguration config = ViewConfiguration.get(m_context);
        mMaxVelocity = config.getScaledMinimumFlingVelocity();
    }

    public void setYearMonth (int nYear, int nMonth) {
        boolean bUpdate = false;
        if (nYear > 0 && m_nYear != nYear) {
            m_nYear = nYear;
            bUpdate = true;
        }
        if (nMonth > 0 && m_nMonth != nMonth) {
            m_nMonth = nMonth;
            bUpdate = true;
        }
        if (!bUpdate)
            return;

        updateDate();
        invalidate();

        if (m_listener != null && m_nIndexSelect > 0) {
            updateSelDate (m_nIndexSelect);
            m_listener.onNoteDateChange(this, m_nSelYear, m_nSelMonth, m_nSelDate);
        }
    }

    public void setNoteAll (boolean bAll) {
        if (m_bNoteAll == bAll)
            return;
        m_bNoteAll = bAll;

        updateDate();
        invalidate();

        if (m_listener != null && m_nIndexSelect > 0) {
            updateSelDate (m_nIndexSelect);
            m_listener.onNoteDateChange(this, m_nSelYear, m_nSelMonth, m_nSelDate);
        }
    }

    public int getYear () {
        return m_nYear;
    }

    public int getMonth () {
        return m_nMonth;
    }

    public int getDay () {
        return m_nDate;
    }

    private void updateDate () {
        Date date = new Date ();
        date.setYear(m_nYear - 1900);
        date.setMonth(m_nMonth -1);
        date.setDate(1);
        m_nWeekDay = date.getDay();

        int nLastDay = 30;
        if (m_nMonth == 1)
            nLastDay = m_nMonthDyas[11];
        else
            nLastDay = m_nMonthDyas[m_nMonth - 2];
        if (m_nMonth == 3 && (m_nYear % 4) == 0)
            nLastDay = 29;

        if (m_nWeekDay == 0)
            m_nPrevDays = 7;
        else
            m_nPrevDays = m_nWeekDay;

        int nIndex = 0;
        int i = 0;
        for (i = nLastDay - m_nPrevDays + 1; i <= nLastDay; i++) {
            m_strDays[nIndex++] = String.format("%d", i);
        }

        nLastDay = m_nMonthDyas[m_nMonth - 1];
        if (m_nMonth == 2 && (m_nYear % 4) == 0)
            nLastDay = 29;
        for (i = 1; i <= nLastDay; i++) {
            m_strDays[nIndex++] = String.format("%d", i);
            if (i == m_nDate)
                m_nIndexSelect = nIndex - 1;
        }

        m_nNextStart = nIndex;
        for (i = 1; i <= 42 - nLastDay - m_nPrevDays; i++) {
            m_strDays[nIndex++] = String.format("%d", i);
        }

        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);

        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000);
                int initVelocity = (int) mVelocityTracker.getXVelocity() / 2;
                mVelocityTracker.clear();
                if (initVelocity > mMaxVelocity) {
                    // Left ?  -1
                    setNextMonth(false);
                } else if (initVelocity < -mMaxVelocity) {
                    // right ? +1
                    setNextMonth(true);
                } else {
                    updateSelectDay(x, y);
                }
                break;
        }
        return true;
    }

    private  void setNextMonth (boolean bNext) {
        int nMonth = m_nMonth;
        int nYear = m_nYear;
        if (bNext) {
            if (nMonth == 12) {
                nMonth = 1;
                nYear++;
            } else {
                nMonth++;
            }
        } else {
            if (nMonth == 1) {
                nMonth = 12;
                nYear--;
            } else {
                nMonth--;
            }
        }
        setYearMonth(nYear, nMonth);
    }

    private void updateSelectDay (int nX, int nY) {
        if (nY < m_nStartY || nX < m_nStartX)
            return;

        int nRow = (nY - m_nStartY) / m_nStepY;
        int nCol = (nX - m_nStartX) / m_nStepX;
        m_nIndexSelect = nRow * 7 + nCol;

        updateSelDate (m_nIndexSelect);

        invalidate();

        if (m_listener != null)
            m_listener.onNoteDateChange(this, m_nSelYear, m_nSelMonth, m_nSelDate);
    }

    private void updateSelDate (int nIndex) {
        m_nSelYear = m_nYear;
        m_nSelMonth = m_nMonth;
        m_nSelDate = Integer.parseInt(m_strDays[nIndex]);
        if (nIndex < m_nPrevDays) {
            m_nSelMonth--;
            if (m_nSelMonth == 0) {
                m_nSelMonth = 12;
                m_nSelYear--;
            }
        } else if (nIndex >= m_nNextStart) {
            m_nSelMonth++;
            if (m_nSelMonth > 12) {
                m_nSelMonth = 1;
                m_nSelYear++;
            }
        }
    }

    protected void onDraw(Canvas canvas) {
        int nW = getWidth();
        int nH = getHeight();
        m_nStartX = 24;
        m_nStepX = nW / 7;

        RectF rcItemf = new RectF(0, 0, nW, nH);
        m_pntRect.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rcItemf, 8, 8, m_pntRect);

        String[]    strWeekDays = new String[]{"日", "一", "二", "三", "四", "五", "六"};
        int nY = 70;
        for (int i = 0; i < 7; i++)
            canvas.drawText(strWeekDays[i], m_nStartX + m_nStepX * i, nY, m_pntTextWeek);

        int nIndex = 0;
        m_nStartY = 100;
        m_nStepY = (nH - m_nStartY) / 6;
        for (int j = 0; j < 6; j++) {
            nY = 30 + m_nStepY * (j + 1);
            for (int i = 0; i < 7; i++) {
                if (nIndex < m_nPrevDays)
                    drawDate(canvas, m_strDays[nIndex], m_nStartX + m_nStepX * i, nY, m_pntTextOutD, nIndex);
                else if (nIndex >= m_nNextStart)
                    drawDate(canvas, m_strDays[nIndex], m_nStartX + m_nStepX * i, nY, m_pntTextOutD, nIndex);
                else
                    drawDate(canvas, m_strDays[nIndex], m_nStartX + m_nStepX * i, nY, m_pntTextDate, nIndex);
                nIndex++;
            }
        }
    }

    private void drawDate (Canvas canvas, String strDay, int nX, int nY, Paint pnt, int nIndex) {
        updateSelDate(nIndex);

        int nNoteCount = getNoteCount();
        if (nNoteCount > 0) {
            int nW = m_nStepX / 4 + 5;
            canvas.drawCircle(nX + nW, nY, 50, m_pntTextNote);
            String strNum = String.format("%d", nNoteCount);
            pnt.setTextSize(40);
            canvas.drawText(strNum, nX + 75, nY - 30, pnt);
        }

        if (nIndex == m_nIndexSelect) {
            int nW = m_nStepX / 4 + 5;
            canvas.drawCircle(nX + nW, nY, 50, m_pntTextSlct);
        }

        pnt.setTextSize(60);
        if (strDay.length() == 1)
            canvas.drawText(strDay, nX + 18, nY, pnt);
        else
            canvas.drawText(strDay, nX, nY, pnt);

        int nLunarDate = m_lunar.getLunarDateINT (m_nSelYear, m_nSelMonth, m_nSelDate);
        int nDate = nLunarDate % 100;
        String strDate = m_lunar.getChinaDayString (nDate);
        if (nDate == 1) {
            int nMonth = (nLunarDate %10000) / 100 - 1;
            strDate = String.format("%s月", LunarCalendar.chineseNumber[nMonth]);
        }
        pnt.setTextSize(30);
        if (strDate.length() == 1)
            canvas.drawText(strDate, nX + 30, nY + 40, pnt);
        else
            canvas.drawText(strDate, nX + 5, nY + 40, pnt);
    }

    private int getNoteCount () {
        int nCount = 0;
        String strDate = String.format("%d-%02d-%02d", m_nSelYear, m_nSelMonth, m_nSelDate);
        dataNoteItem dataItem = null;

        if (m_bNoteAll) {
            int nNoteSize = noteConfig.m_lstData.m_lstAllItem.size();
            for (int i = 0; i < nNoteSize; i++) {
                dataItem = noteConfig.m_lstData.m_lstAllItem.get(i);
                if (noteConfig.m_nShowSecurity == 0) {
                    if (dataItem.isSecurity())
                        continue;
                }
                if (strDate.compareTo(dataItem.m_strDate) == 0)
                    nCount++;
            }
        } else {
            int nNoteSize = noteConfig.m_lstData.m_lstSelItem.size();
            for (int i = 0; i < nNoteSize; i++) {
                dataItem = noteConfig.m_lstData.m_lstSelItem.get(i);
                if (noteConfig.m_nShowSecurity == 0) {
                    if (dataItem.isSecurity())
                        continue;
                }
                if (strDate.compareTo(dataItem.m_strDate) == 0)
                    nCount++;
            }
        }
        return nCount;
    }
}
