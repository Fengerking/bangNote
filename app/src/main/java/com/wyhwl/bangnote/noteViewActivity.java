package com.wyhwl.bangnote;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class noteViewActivity extends AppCompatActivity
                            implements noteImageShow.noteImageShowListener,
                                        View.OnClickListener {
    private String          m_strNoteFile = null;
    private TextView        m_txtTitle = null;
    private TextView        m_txtDate = null;
    private TextView        m_txtTime = null;
    private TextView        m_txtType = null;
    private LinearLayout    m_layView = null;

    private dataNoteItem    m_dataItem = null;
    private int             m_nWordCount = 0;
    private noteImageShow   m_noteImage = null;

    private String[]        m_strFileList = null;
    private int             m_nFileCount = 0;

    private int             m_nLastY = 0;
    private int             m_nLastYPos = 0;
    private int             m_nDispH = 0;

    private VelocityTracker mVelocityTracker;
    private int             mMaxVelocity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);
        setTitle(R.string.old_note);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.hide();

        noteConfig.initConfig(this);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null)
            m_strNoteFile = uri.toString();
        else
            m_strNoteFile = "/sdcard/bangNote/text/txt_2019-05-18-21-34-19.bnt";

        m_nFileCount = intent.getIntExtra("FileCount", 0);
        if(m_nFileCount > 0) {
            m_strFileList = intent.getStringArrayExtra("FileList");
        }

        initViews();
    }

    protected void onStop () {
        super.onStop();
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void initViews () {
        ((ImageButton)findViewById(R.id.imbBack)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbEditNote)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbCount)).setOnClickListener(this);

        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        m_nDispH = dm.heightPixels;

        m_txtTitle = (TextView)findViewById(R.id.textTitle);
        m_txtDate = (TextView)findViewById(R.id.textDate);
        m_txtTime = (TextView)findViewById(R.id.textTime);
        m_txtType = (TextView)findViewById(R.id.textType);
        m_layView = (LinearLayout)findViewById(R.id.layView);

        ViewConfiguration config = ViewConfiguration.get(this);
        mMaxVelocity = config.getScaledMinimumFlingVelocity();

        m_layView.postDelayed(() -> readFromFile(), 10);
    }

    protected void onResume () {
        super.onResume();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imbBack:
                finish();
                break;

            case R.id.imbEditNote:
                Intent intent = new Intent(noteViewActivity.this, noteEditActivity.class);
                intent.setData(Uri.parse(m_strNoteFile));
                startActivityForResult(intent, 1);
                break;

            case R.id.imbCount:
                showNormalDialog();
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);

        int y = (int) ev.getY();
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                m_nLastY = y;
                m_nLastYPos = y;
                 break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(m_nLastYPos - y) > 20)
                    m_noteImage = null;
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

                if (dy != 0 && nH > m_nDispH)
                    m_layView.scrollBy(0, dy);
                m_nLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(m_nLastYPos - y) > 20)
                    m_noteImage = null;
                if (Math.abs(m_nLastYPos - y) < 200) {
                    mVelocityTracker.computeCurrentVelocity(1000);
                    int initVelocity = (int) mVelocityTracker.getXVelocity() / 2;
                    setTitle("Move " + initVelocity);
                    mVelocityTracker.clear();
                    if (initVelocity > mMaxVelocity) {
                        // Left ?  -1
                        openNextFile(false);
                    } else if (initVelocity < -mMaxVelocity) {
                        // right ? +1
                        openNextFile(true);
                    }
                }
                break;
        }
        return true;
    }

    public int onNoteImageShowEvent (View view, MotionEvent ev){
        m_noteImage = (noteImageShow)view;
        m_layView.postDelayed(() -> openNoteImageActivity(), 500);
        return 0;
    }

    private void openNoteImageActivity () {
        if (m_noteImage == null)
            return;
        String strImgFile = m_noteImage.getImageFile();
        Intent intent = new Intent(noteViewActivity.this, noteImageActivity.class);
        intent.setData(Uri.parse(strImgFile));
        startActivity(intent);
    }

    private void openNextFile (boolean bNext) {
        if (m_nFileCount <= 1)
            return;

        int     nIndex = 0;
        for (int i = 0; i < m_nFileCount; i++) {
            if (m_strFileList[i].compareTo(m_strNoteFile) == 0) {
                nIndex = i;
                break;
            }
        }
        if (bNext) {
            nIndex++;
            if (nIndex == m_nFileCount)
                nIndex = 0;
        }
        if (!bNext) {
            nIndex--;
            if (nIndex < 0)
                nIndex = m_nFileCount - 1;
        }
        m_strNoteFile = m_strFileList[nIndex];
        readFromFile ();
    }

    private void readFromFile () {
        if (m_strNoteFile == null)
            return;

        while (m_layView.getChildCount() > 2)
            m_layView.removeView(m_layView.getChildAt((m_layView.getChildCount() - 1)));

        m_nWordCount = 0;
        m_dataItem = new dataNoteItem();
        m_dataItem.readFromFile(m_strNoteFile);
        m_txtTitle.setText(m_dataItem.m_strTitle);
        m_txtDate.setText(m_dataItem.m_strDate);
        m_txtTime.setText(m_dataItem.m_strTime);
        m_txtType.setText(m_dataItem.m_strType);

        dataNoteItem.dataContent dataItem = null;
        for (int i = 0; i < m_dataItem.m_lstItem.size(); i++) {
            TextView txtView = new TextView(this);
            txtView.setText("\n");
            txtView.setTextSize(4);
            m_layView.addView(txtView);

            dataItem = m_dataItem.m_lstItem.get(i);
            if (dataItem.m_nType == dataNoteItem.m_nItemTypeText) {
                txtView = new TextView(this);
                m_layView.addView(txtView);
                txtView.setText(dataItem.m_strItem);
                txtView.setTextSize(noteConfig.m_nTextSize);
                txtView.setTextColor(noteConfig.m_nTextColor);
                m_nWordCount += dataItem.m_strItem.length();
            } else {
                noteImageShow imgView = new noteImageShow(this);
                m_layView.addView(imgView);
                imgView.setImageFile (dataItem.m_strItem, false);
                imgView.setNoteImageShowListener(this);
            }
        }

        m_layView.post(()->onResizeView());
    }

    public void onResizeView () {
        int nHeight = 0;
        int nCount = m_layView.getChildCount();
        for (int i = 0; i < nCount; i++) {
            nHeight += m_layView.getChildAt(i).getHeight();
        }
        ViewGroup.LayoutParams param = (ViewGroup.LayoutParams)m_layView.getLayoutParams();
        param.height = nHeight + 200;
        m_layView.setLayoutParams(param);
        m_layView.scrollTo(0, 0);
    }

    private void showNormalDialog(){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(noteViewActivity.this);
        normalDialog.setIcon(R.drawable.note_count);
        normalDialog.setTitle("字数统计信息, 一共 " + m_nWordCount + " 个字。");
        normalDialog.setPositiveButton("确定", null);
        normalDialog.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (noteConfig.m_bNoteModified)
            m_layView.postDelayed(()->readFromFile(), 500);
    }

}
