package com.wyhwl.note;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import android.view.animation.*;

import android.util.Log;

public class noteListSlider extends ViewGroup {
    private int                 mLastX;
    private int                 mLastY;
    private Scroller            mScroller;
    private int                 mCurrentPage = 0;
    private boolean             mStartSwitch = false;

    private switchListener      mSwitchListener = null;

    private int                 m_nLeftWidth = 0;
    private int                 m_nRightWidth = 0;


    // The event listener function
    public interface switchListener{
        public int onSwitchStart (int nView);
        public int OnSwitchEnd (int nView);
        public int onMotionEvent (MotionEvent ev);
    }

    public noteListSlider(Context context) {
        super(context);
        init(context);
    }
    public noteListSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public noteListSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setSwitchListener (switchListener pListener) {
        mSwitchListener = pListener;
    }

    private void init(Context context) {
        //mScroller = new Scroller(context, new AccelerateInterpolator());
        mScroller = new Scroller(context, new DecelerateInterpolator());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for(int i = 0; i < getChildCount(); i++){
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        m_nLeftWidth = getWidth() / 2;
        m_nRightWidth = getWidth() / 2;
        View child = getChildAt(0);
        child.layout(0, t, m_nLeftWidth, b);
        child = getChildAt(1);
        child.layout(m_nLeftWidth, t, getWidth() + m_nLeftWidth, b);
        child = getChildAt(2);
        child.layout(getWidth() + m_nLeftWidth, t, getWidth() + m_nLeftWidth + m_nRightWidth, b);
        mCurrentPage = 1;
        scrollBy(m_nLeftWidth,0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(!mScroller.isFinished())
                    mScroller.abortAnimation();
                mLastX = x;
                mLastY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                int nPos = getScrollX();
                int nMov = mLastX - x;
                int dY = mLastY - y;
                if (mCurrentPage == 0) {
                    if (nMov < 0 && nPos + nMov < 0) {
                        nMov = -nPos;
                    }
                    if (nMov > 0 && nPos + nMov > m_nLeftWidth) {
                        nMov = m_nLeftWidth - nPos;
                    }
                } else if (mCurrentPage == 1) {
                    int nRight = m_nLeftWidth + getWidth() - m_nRightWidth;
                    if (nMov < 0 && nPos + nMov < 0) {
                        nMov = -nPos;
                    }
                    if (nMov > 0 && nPos + nMov > nRight) {
                        nMov = nRight - nPos;
                    }
                } else {
                    int nRight = m_nLeftWidth + m_nRightWidth;
                    if (nMov < 0 && nPos + nMov < m_nLeftWidth) {
                        nMov = -(nPos - m_nLeftWidth);
                    }
                    if (nMov > 0 && nPos + nMov > nRight) {
                        nMov = nRight - nPos;
                    }
                }
                if (Math.abs(dY) < Math.abs(nMov))
                    scrollBy(nMov,0);
                mLastX = x;
                mLastY = y;
                break;

            case MotionEvent.ACTION_UP:
                scrollToPage();
                break;
        }

        if (mSwitchListener != null)
            mSwitchListener.onMotionEvent(ev);
        return true;
    }

    private void scrollToPage() {
        int nPos = getScrollX();
        if(nPos < m_nLeftWidth / 2)
            mCurrentPage = 0;
        else if (nPos > m_nLeftWidth + m_nRightWidth / 2)
            mCurrentPage = 2;
        else
            mCurrentPage = 1;

        if (mSwitchListener != null)
           mSwitchListener.onSwitchStart(mCurrentPage);

        mStartSwitch = true;
        int dx = 0;
        if (mCurrentPage == 0)
            dx = -nPos;
        else if (mCurrentPage == 1)
            dx = -(nPos - m_nLeftWidth);
        else
            dx = -(nPos - (m_nLeftWidth + m_nRightWidth));
        mScroller.startScroll(getScrollX(),0, dx, 0, Math.abs(dx) * 2);
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if(mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            invalidate();
        } else if (mStartSwitch) {
            mStartSwitch = false;
            if (mSwitchListener != null)
                mSwitchListener.OnSwitchEnd(mCurrentPage);
        }
    }
}
