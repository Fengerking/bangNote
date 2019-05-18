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

public class noteViewActivity extends AppCompatActivity {
    private String          m_strNoteFile = null;
    private TextView        m_txtTitle = null;
    private TextView        m_txtDate = null;
    private TextView        m_txtTime = null;
    private TextView        m_txtType = null;
    private LinearLayout    m_layView = null;

    private dataNoteItem    m_dataItem = null;

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
        m_txtTitle = (TextView)findViewById(R.id.textTitle);
        m_txtDate = (TextView)findViewById(R.id.textDate);
        m_txtTime = (TextView)findViewById(R.id.textTime);
        m_txtType = (TextView)findViewById(R.id.textType);
        m_layView = (LinearLayout)findViewById(R.id.layView);
    }

    protected void onResume () {
        super.onResume();
        readFromFile();
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
            dataItem = m_dataItem.m_lstItem.get(i);
            if (dataItem.m_nType == dataNoteItem.m_nItemTypeText) {
                TextView txtView = new TextView(this);
                m_layView.addView(txtView);
                txtView.setText(dataItem.m_strItem);
                txtView.setTextSize(noteConfig.m_nTextSize);
                txtView.setTextColor(noteConfig.m_nTextColor);
            } else {
                ImageView imgView = new ImageView(this);
                m_layView.addView(imgView);

                try {
                    FileInputStream fis = new FileInputStream (dataItem.m_strItem);
                    Bitmap bmp = BitmapFactory.decodeStream(fis);
                    fis.close();
                    imgView.setImageBitmap(bmp);
                    ViewGroup.LayoutParams params = imgView.getLayoutParams();
                    params.width = -1;
                    params.height = bmp.getHeight() * 1000 / bmp.getWidth();
                    imgView.setLayoutParams(params);
                    imgView.setScaleType(ImageView.ScaleType.FIT_XY);
                }catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }

        int nHeight = 0;
        int nCount = m_layView.getChildCount();
        for (int i = 0; i < nCount; i++) {
            nHeight += m_layView.getChildAt(i).getHeight();
        }
        ViewGroup.LayoutParams param = (ViewGroup.LayoutParams)m_layView.getLayoutParams();
        param.height = nHeight + 1200;
        m_layView.setLayoutParams(param);
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
                break;

            case R.id.menu_edit:
                Intent intent = new Intent(noteViewActivity.this, noteEditActivity.class);
                intent.setData(Uri.parse(m_strNoteFile));
                startActivity(intent);
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
