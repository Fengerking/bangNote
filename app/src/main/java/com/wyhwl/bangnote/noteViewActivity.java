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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class noteViewActivity extends AppCompatActivity
                            implements noteImageShow.noteImageShowListener,
                                        AdapterView.OnItemSelectedListener,
                                        View.OnClickListener {
    private String          m_strNoteFile = null;
    private TextView        m_txtTitle = null;
    private TextView        m_txtDate = null;
    private TextView        m_txtTime = null;
    private Spinner         m_spnType = null;
    private TextView        m_txtWeather = null;
    private LinearLayout    m_layView = null;

    private dataNoteItem    m_dataItem = null;
    private int             m_nWordCount = 0;
    private noteImageShow   m_noteImage = null;

    private String[]        m_strFileList = null;
    private int             m_nFileCount = 0;

    private int             m_nLastY = 0;
    private int             m_nLastYPos = 0;
    private int             m_nDispH = 0;
    private long            m_lLastShowTime = 0;

    private VelocityTracker mVelocityTracker;
    private int             mMaxVelocity;
    private boolean         m_bReadFromFile = false;

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

        m_dataItem = new dataNoteItem();
        initViews();
    }

    protected void onResume () {
        super.onResume();
        if (noteConfig.m_bNoteModified) {
            readFromFile();
        }
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
        ((ImageButton)findViewById(R.id.imbShareNote)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbDeleteNote)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbCount)).setOnClickListener(this);

        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        m_nDispH = dm.heightPixels;

        m_txtTitle = (TextView)findViewById(R.id.textTitle);
        m_txtDate = (TextView)findViewById(R.id.textDate);
        m_txtTime = (TextView)findViewById(R.id.textTime);
        m_txtWeather = (TextView)findViewById(R.id.textWeather);
        m_layView = (LinearLayout)findViewById(R.id.layView);
        m_spnType = (Spinner)findViewById(R.id.spinNoteType);
        m_spnType.setOnItemSelectedListener(this);

        noteConfig.m_bNoteModified = false;

        ViewConfiguration config = ViewConfiguration.get(this);
        mMaxVelocity = config.getScaledMinimumFlingVelocity();

        m_layView.postDelayed(() -> readFromFile(), 10);
    }

    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.imbBack:
                finish();
                break;

            case R.id.imbEditNote:
                intent = new Intent(noteViewActivity.this, noteEditActivity.class);
                intent.setData(Uri.parse(m_strNoteFile));
                startActivityForResult(intent, 1);
                break;

            case R.id.imbShareNote:
                String strShareText = "";
                dataNoteItem.dataContent dataItem = null;
                for (int i = 0; i < m_dataItem.m_lstItem.size(); i++) {
                    dataItem = m_dataItem.m_lstItem.get(i);
                    if (dataItem.m_nType == noteConfig.m_nItemTypeText) {
                        strShareText += dataItem.m_strItem;
                    }
                }

                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "分享笔记");
                intent.putExtra(Intent.EXTRA_TEXT, strShareText);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, getTitle()));
                break;

            case R.id.imbDeleteNote:
                if (m_dataItem == null)
                    break;
                if (m_dataItem.m_strType.compareTo(noteConfig.m_noteTypeMng.m_strRubbish) != 0) {
                    m_dataItem.m_strType = noteConfig.m_noteTypeMng.m_strRubbish;
                    m_dataItem.writeToFile();
                    noteConfig.m_lstData.updNoteFile(m_dataItem.m_strFile);
                    if (m_nFileCount == 1) {
                        finish();
                        return;
                    }

                    String[] strNewFiles = new String[m_nFileCount-1];
                    int nIndex = 0;
                    for (int i = 0; i < m_nFileCount; i++) {
                        if (m_strFileList[i].compareTo(m_dataItem.m_strFile) == 0)
                            continue;
                        strNewFiles[nIndex++] = m_strFileList[i];
                    }
                    m_nFileCount -= 1;
                    m_strFileList = new String[m_nFileCount];
                    for (int i = 0; i < m_nFileCount; i++) {
                        m_strFileList[i] = strNewFiles[i];
                    }
                    noteConfig.m_bNoteModified = true;
                    openNextFile(true);
                }
                break;

            case R.id.imbCount:
                showNormalDialog();
                break;
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (m_bReadFromFile)
            return;
        TextView tvType = (TextView) view.findViewById(R.id.txtNoteType);
        if (m_dataItem.m_strType.compareTo(tvType.getText().toString()) != 0) {
            m_dataItem.m_strType = tvType.getText().toString();
            m_dataItem.writeToFile();
            noteConfig.m_lstData.updNoteFile(m_dataItem.m_strFile);
            noteConfig.m_bNoteModified = true;
        }
    }
    public void onNothingSelected(AdapterView<?> parent) {
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
                if (Math.abs(m_nLastYPos - y) < 300) {
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
        if (System.currentTimeMillis() - m_lLastShowTime < 5000) {
            return;
        }
        m_lLastShowTime = System.currentTimeMillis();
        String strImgFile = m_noteImage.getImageFile();
        Intent intent = new Intent(noteViewActivity.this, noteImageActivity.class);
        intent.setData(Uri.parse(strImgFile));
        startActivity(intent);
    }

    private void openNextFile (boolean bNext) {
        if (m_nFileCount <= 1) {
            if (m_strNoteFile.compareTo(m_strFileList[0]) != 0) {
                m_strNoteFile = m_strFileList[0];
                readFromFile();
            }
            return;
        }

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

        m_bReadFromFile = true;
        m_nWordCount = 0;
        m_dataItem.readFromFile(m_strNoteFile);
        m_txtTitle.setText(m_dataItem.m_strTitle);
        m_txtDate.setText(m_dataItem.m_strDate);
        m_txtTime.setText(m_dataItem.m_strTime);
        m_txtWeather.setText(m_dataItem.m_strCity + " " + m_dataItem.m_strWeat);
        String strDate = m_dataItem.m_strDate + " " + noteConfig.getWeekDay(m_dataItem.m_strDate);
        m_txtDate.setText(strDate);

        initSpinner();

        dataNoteItem.dataContent dataItem = null;
        for (int i = 0; i < m_dataItem.m_lstItem.size(); i++) {
            TextView txtView = new TextView(this);
            txtView.setText("\n");
            txtView.setTextSize(4);
            m_layView.addView(txtView);

            dataItem = m_dataItem.m_lstItem.get(i);
            if (dataItem.m_nType == noteConfig.m_nItemTypeText) {
                txtView = new TextView(this);
                m_layView.addView(txtView);
                txtView.setText(dataItem.m_strItem);
                txtView.setTextSize(noteConfig.m_nTextSize);
                txtView.setTextColor(noteConfig.m_nTextColor);
                m_nWordCount += dataItem.m_strItem.length();
            } else if (dataItem.m_nType == noteConfig.m_nItemTypePict) {
                noteImageShow imgView = new noteImageShow(this);
                m_layView.addView(imgView);
                imgView.setImageFile (dataItem.m_strItem, false);
                imgView.setNoteImageShowListener(this);
            } else if (dataItem.m_nType == noteConfig.m_nItemTypeAudo) {
                noteAudioPlayView audView = new noteAudioPlayView(this);
                m_layView.addView(audView);
                ViewGroup.LayoutParams param = (ViewGroup.LayoutParams)audView.getLayoutParams();
                param.width = -1;
                audView.setLayoutParams(param);
                audView.setAudioFile(dataItem.m_strItem);
            }
        }
        m_bReadFromFile = false;

        m_layView.post(()->onResizeView());
    }

    private void initSpinner () {
        ArrayList<String> lstType = noteConfig.m_noteTypeMng.getListName(false);
        lstType.remove(m_dataItem.m_strType);
        lstType.add(0, m_dataItem.m_strType);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                noteViewActivity.this, R.layout.spn_note_type, lstType);
        m_spnType.setAdapter(adapter);
    }

    public void onResizeView () {
        int nHeight = 0;
        int nCount = m_layView.getChildCount();
        for (int i = 0; i < nCount; i++) {
            nHeight += m_layView.getChildAt(i).getHeight();
        }
        ViewGroup.LayoutParams param = (ViewGroup.LayoutParams)m_layView.getLayoutParams();
        param.height = nHeight + 600;
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
    }
}
