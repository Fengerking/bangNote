package com.wyhwl.bangnote;

import android.app.DatePickerDialog;
import android.app.AlertDialog;
import android.app.TimePickerDialog;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.view.ViewGroup;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import java.io.FileInputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class noteEditActivity extends AppCompatActivity
        implements noteEditText.onNoteEditListener, OnClickListener {
    private static int      RESULT_LOAD_IMAGE = 10;
    private noteEditText    m_edtTitle = null;
    private TextView        m_txtDate = null;
    private TextView        m_txtTime = null;
    private ImageButton     m_btnDate = null;
    private ImageButton     m_btnTime = null;
    private Spinner         m_spnType = null;

    private LinearLayout    m_layView = null;
    private int             m_nLastY = 0;
    private int             m_nDispH = 0;

    private int             m_nFocusID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);
        setTitle(R.string.new_note);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initViews();
    }

    private void initViews () {
        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        m_nDispH = dm.heightPixels;
        m_edtTitle = (noteEditText) findViewById(R.id.editTitle);
        m_txtDate = (TextView)findViewById(R.id.textDate);
        m_txtTime = (TextView)findViewById(R.id.textTime);
        m_spnType = (Spinner)findViewById(R.id.spinNoteType);
        m_btnDate = (ImageButton)findViewById(R.id.btnDate);
        m_btnTime = (ImageButton)findViewById(R.id.btnTime);
        m_btnDate.setOnClickListener(this);
        m_btnTime.setOnClickListener(this);
        m_edtTitle.setOnNoteEditListener(this);

        SimpleDateFormat fmtDate = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        m_txtDate.setText(fmtDate.format(date));
        SimpleDateFormat fmtTime = new SimpleDateFormat("HH:mm:ss");
        m_txtTime.setText(fmtTime.format(date));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                noteEditActivity.this, R.layout.spn_note_type, getNoteType());
        m_spnType.setAdapter(adapter);
        m_spnType.setSelection(2);

        m_layView = (LinearLayout)findViewById(R.id.layView);

        noteEditText noteView = (noteEditText)addNoteView(null, 0);
        noteView.setText ("1\n2\n3\n4");
    }

    private View addNoteView (View vwAfter, int nType) {
        View vwNew;
        if (nType == 0) {
            noteEditText noteEdit = new noteEditText(this);
            noteEdit.setOnNoteEditListener(this);
            vwNew = noteEdit;
        } else {
            noteImageView noteImage = new noteImageView(this);
            vwNew = noteImage;
        }
        if (vwAfter == null) {
            m_layView.addView(vwNew);
        } else {
            int nCount = m_layView.getChildCount();
            for (int i = 0; i < nCount; i++) {
                if (m_layView.getChildAt(i) == vwAfter) {
                    m_layView.addView(vwNew, i + 1);
                    break;
                }
            }
        }
        return vwNew;
    }

    private void addImageView (String strImage) {
        View  vwAfter = null;
        if (m_nFocusID >= 10) {  // the focus view is edittext?
            View vwFocus = null, vwPrev = null;
            int nCount = m_layView.getChildCount();
            for (int i = 2; i < nCount; i++) {
                vwFocus = m_layView.getChildAt(i);
                if (vwFocus.getId() == m_nFocusID) {
                    noteEditText edtView = (noteEditText)vwFocus;
                    String strText = edtView.getText().toString();
                    int nEnd = edtView.getSelectionEnd();
                    if (nEnd < strText.length()) {
                        String strPrev = strText.substring(0, nEnd);
                        String strNext = strText.substring(nEnd);
                        edtView.setText(strPrev);
                        vwAfter = edtView;
                        noteEditText noteView = (noteEditText) addNoteView(vwAfter, 0);
                        noteView.setText(strNext);
                    } else if (strText.length() == 0) {
                        if (vwPrev != null && vwPrev.getId() < noteConfig.m_nImagIdStart ) {
                            m_layView.removeView(vwFocus);
                            vwAfter = vwPrev;
                        } else {
                            vwAfter = vwFocus;
                        }
                    }
                    break;
                }
                vwPrev = vwFocus;
            }
        }

        ImageView imgView = (ImageView) addNoteView (vwAfter, 1);
        try {
            FileInputStream fis = new FileInputStream (strImage);
            Bitmap bmp = BitmapFactory.decodeStream(fis);
            imgView.setImageBitmap(bmp);
            fis.close();
        }catch (Exception e) {
            e.printStackTrace();
        }

        ViewGroup.LayoutParams params = imgView.getLayoutParams();
        params.height = noteConfig.m_nImageHeight;
        imgView.setLayoutParams(params);

        View vwImage = (View)imgView;
        int nCount = m_layView.getChildCount();
        if (m_layView.getChildAt(nCount - 1) == vwImage) {
            addNoteView(null, 0);
        } else {
            View vwNext = null;
            for (int i = 2; i < nCount; i++) {
                if (m_layView.getChildAt(i) == vwImage) {
                    vwNext = m_layView.getChildAt(i+1);
                    if (vwNext.getId() >= noteConfig.m_nImagIdStart) {
                        addNoteView(vwImage, 0);
                    }
                    break;
                }
            }
        }
        onResizeView();
    }

    private List<String> getNoteType() {
        List<String> dataList = new ArrayList<String>();
        dataList.add("默认笔记");
        dataList.add("学习日记");
        dataList.add("旅游日记");
        dataList.add("心灵鸡汤");
        return dataList;
    }

    public void onClick(View v) {
        Date    dateNow = new Date(System.currentTimeMillis());
        int     themeId = AlertDialog.THEME_HOLO_DARK;;
        int     nID     = v.getId();
        switch (nID) {
            case R.id.btnDate:
                int nYear = dateNow.getYear() + 1900, nMonth = dateNow.getMonth() + 1, nDate = dateNow.getDate();
                DatePickerDialog dlgDate = new DatePickerDialog(this, themeId, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String strDate = String.format("%d-%02d-%02d", year, month, dayOfMonth);
                        m_txtDate.setText(strDate);
                    }
                }, nYear, nMonth - 1, nDate);
                dlgDate.show();
                break;

            case R.id.btnTime:
                int nHour = dateNow.getHours(), nMinute = dateNow.getMinutes();
                TimePickerDialog dlgTime = new TimePickerDialog(this, themeId, new TimePickerDialog.OnTimeSetListener() {
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Date    dateNow = new Date(System.currentTimeMillis());
                        int     nSecond = dateNow.getSeconds();
                        String strTime = String.format("%02d:%02d:%02d", hourOfDay, minute, nSecond);
                        m_txtTime.setText(strTime);
                    }
                }, nHour, nMinute, true);
                dlgTime.show();
                break;
        }
    }

    public void onMontionEvent (MotionEvent ev, int nID) {
        int y = (int) ev.getY();
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                m_nLastY = y;
                if (nID >= 10)
                    m_nFocusID = nID;
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

                if (dy != 0 && nH > m_nDispH)
                    m_layView.scrollBy(0, dy);
                m_nLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
    }

    public void onTextChanged (int nID) {

    }

    public void onResizeView () {
        int nHeight = 0;
        int nCount = m_layView.getChildCount();
        for (int i = 0; i < nCount; i++) {
            nHeight += m_layView.getChildAt(i).getHeight();
        }
        ViewGroup.LayoutParams param = (ViewGroup.LayoutParams)m_layView.getLayoutParams();
        param.height = nHeight + m_nDispH / 2;
        m_layView.setLayoutParams(param);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        onMontionEvent (ev, 0);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_noteedit, menu);
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

            case R.id.menu_newpic:
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent, RESULT_LOAD_IMAGE);//打开系统相册
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
            return;
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //如果是document类型的uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content:" +
                        "//downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是File类型的uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }

        addImageView(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
}
