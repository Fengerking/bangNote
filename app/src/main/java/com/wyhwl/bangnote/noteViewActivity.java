package com.wyhwl.bangnote;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

public class noteViewActivity extends AppCompatActivity
                            implements noteImageShow.noteImageShowListener {
    private String          m_strNoteFile = null;
    private TextView        m_txtTitle = null;
    private TextView        m_txtDate = null;
    private TextView        m_txtTime = null;
    private TextView        m_txtType = null;
    private LinearLayout    m_layView = null;

    private dataNoteItem    m_dataItem = null;
    private noteImageShow   m_noteImage = null;

    private int             m_nLastY = 0;
    private int             m_nLastYPos = 0;
    private int             m_nDispH = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);
        setTitle(R.string.old_note);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        noteConfig.initConfig(this);

        Uri uri = getIntent().getData();
        if (uri != null)
            m_strNoteFile = uri.toString();
        else
            m_strNoteFile = "/sdcard/bangNote/text/txt_2019-05-18-21-34-19.bnt";

        initViews();
    }

    private void initViews () {
        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        m_nDispH = dm.heightPixels;

        m_txtTitle = (TextView)findViewById(R.id.textTitle);
        m_txtDate = (TextView)findViewById(R.id.textDate);
        m_txtTime = (TextView)findViewById(R.id.textTime);
        m_txtType = (TextView)findViewById(R.id.textType);
        m_layView = (LinearLayout)findViewById(R.id.layView);

        m_layView.postDelayed(() -> readFromFile(), 10);
    }

    protected void onResume () {
        super.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
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
        startActivityForResult(intent, 1);
    }

    private void readFromFile () {
        if (m_strNoteFile == null)
            return;

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
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        m_layView.postDelayed(()->readFromFile(), 500);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_noteview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override // show the icon on menu
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;

            case R.id.menu_edit:
                Intent intent = new Intent(noteViewActivity.this, noteEditActivity.class);
                intent.setData(Uri.parse(m_strNoteFile));
                startActivityForResult(intent, 1);
                break;

            case R.id.menu_notesave:
                break;
            case R.id.menu_notecount:
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
