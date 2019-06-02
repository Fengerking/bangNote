package com.wyhwl.bangnote;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import java.util.ArrayList;

public class noteBaseSlider extends ViewGroup {
    private String          LOG_TAG = "noteBaseSlider";

    protected Context         m_context = null;

    protected int             m_nLastX = 0;
    protected int             m_nLastY = 0;
    protected int             m_nMove = 0;
    protected Scroller        m_scroller = null;
    protected boolean         m_bStartSwitch = false;
    protected VelocityTracker mVelocityTracker = null;
    protected int             mMaxVelocity = 0;

    protected int             m_nCurPage = 0;
    protected int             m_nDefaultPage = 0;
    protected boolean         m_bInitLayout = false;

    protected ArrayList<View>       m_lstChildView = null;
    protected noteSliderListener    m_listener = null;

    // The event listener function
    public interface noteSliderListener{
        public void onNoteSliderStart (int nView);
        public void onNoteSliderEnd (int nView);
    }

    public void setSliderListener (noteSliderListener listener) {
        m_listener = listener;
    }

    public noteBaseSlider(Context context) {
        super(context);
        initSlider (context);
    }
    public noteBaseSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSlider (context);
    }
    public noteBaseSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSlider (context);
    }

    protected void initSlider (Context context) {
        m_context = context;
        m_scroller = new Scroller(context, new DecelerateInterpolator());
        m_lstChildView = new ArrayList<View>();
        ViewConfiguration config = ViewConfiguration.get(m_context);
        mMaxVelocity = config.getScaledMinimumFlingVelocity();
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
        if (m_bInitLayout)
            return;
        m_bInitLayout = true;
        m_lstChildView.clear();
        for (int i = 0; i < getChildCount(); i++){
            View child = getChildAt(i);
            child.layout(getWidth() * i, t, getWidth() * (i + 1), b * 4);
            m_lstChildView.add(child);
        }
        if (m_nDefaultPage != 0)
            post(()->scrollTo(getWidth() * m_nDefaultPage, 0));
    }

    protected void onDetachedFromWindow() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
        super.onDetachedFromWindow();
    }

    public View getCurrentView () {
        return m_lstChildView.get(m_nCurPage);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (getChildCount() <= 1)
            return true;

        if(mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);

        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(!m_scroller.isFinished())
                    m_scroller.abortAnimation();
                m_nLastX = x;
                m_nLastY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                m_nMove = m_nLastX - x;
                scrollBy(m_nMove, 0);
                m_nLastX = x;
                m_nLastY = y;
                break;

            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000);
                int initVelocity = (int) mVelocityTracker.getXVelocity() / 2;
                mVelocityTracker.clear();
                if (initVelocity > mMaxVelocity) {
                    // Left ?  -1
                    scrollFast (true);
                } else if (initVelocity < -mMaxVelocity) {
                    // right ? +1
                    scrollFast (false);
                } else {
                    scrollToPage();
                }
                break;
        }
        return true;
    }

    protected void scrollToPage() {
        int nWidth = getWidth();
        int nPosX = getScrollX();
        int nPage = getScrollX() / nWidth;
        int nPos = getScrollX() % nWidth;
        int nMov = 0;
        if (m_nMove > 0) {
            if (nPos < nWidth / 2) {
                nMov = -nPos;
            } else {
                nMov = nWidth - nPos;
            }
        } else {
            if (nPos < nWidth / 2) {
                nMov = -nPos;
            } else {
                nMov = nWidth - nPos;
            }
        }
        if (nMov != 0) {
            m_bStartSwitch = true;
            onStartSlider ();
            m_scroller.startScroll(nPosX, 0, nMov, 0, Math.abs(nMov) / 2);
            invalidate();
        }
    }

    protected void scrollFast (boolean bLeft) {
        int nWidth = getWidth();
        int nPos = getScrollX() % nWidth;
        int nMov = 0;
        if (bLeft)
            nMov = -nPos;
        else
            nMov = nWidth - nPos;
        m_bStartSwitch = true;
        onStartSlider();
        m_scroller.startScroll(getScrollX(), 0, nMov, 0, Math.abs(nMov) / 2);
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(m_scroller.computeScrollOffset()){
            scrollTo(m_scroller.getCurrX(),m_scroller.getCurrY());
            invalidate();
        } else if (m_bStartSwitch) {
            m_bStartSwitch = false;
            m_nCurPage = (getScrollX() + 100) / getWidth();
            View vwChild = null;
            int nChildNum = m_lstChildView.size();
            if (m_nCurPage == nChildNum - 1) {
                for (int i = 1; i < nChildNum; i++){
                    vwChild = m_lstChildView.get(i);
                    vwChild.layout(getWidth() * (i-1), 0, getWidth() * (i), getHeight());
                }
                vwChild = m_lstChildView.get(0);
                vwChild.layout(getWidth() * (nChildNum-1), 0, getWidth() * (nChildNum), getHeight() * 4);

                needUpdateView (vwChild);

                m_lstChildView.remove(vwChild);
                m_lstChildView.add(vwChild);
                scrollTo (getWidth() * (nChildNum - 2), 0);

                m_nCurPage--;
            } else if (m_nCurPage == 0) {
                for (int i = 0; i < nChildNum-1; i++){
                    vwChild = m_lstChildView.get(i);
                    vwChild.layout(getWidth() * (i+1), 0, getWidth() * (i+2), getHeight());
                }
                vwChild = m_lstChildView.get(nChildNum-1);
                vwChild.layout(0, 0, getWidth(), getHeight() * 4);

                needUpdateView (vwChild);

                m_lstChildView.remove(vwChild);
                m_lstChildView.add(0, vwChild);
                scrollTo (getWidth(), 0);

                m_nCurPage++;
            }
            onEndSlider();
        }
    }

    protected void onStartSlider () {

    }

    protected void onEndSlider () {

    }

    protected void needUpdateView (View view) {

    }

}
