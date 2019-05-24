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
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.SpinnerAdapter;
import android.util.Log;
import android.graphics.Bitmap;
import android.os.StrictMode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class noteEditActivity extends AppCompatActivity
        implements  noteEditText.onNoteEditListener,
                    noteImageView.onNoteImageListener,
                    OnClickListener {
    private static int      RESULT_LOAD_IMAGE       = 10;
    private static int      RESULT_CAPTURE_IMAGE    = 20;
    private TextView        m_txtBarTitle = null;
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
    private String          m_strNoteFile = null;
    private String          m_strImageFile = null;
    private dataNoteItem    m_dataItem = null;
    private boolean         m_bNewNote = true;
    private boolean         m_bReadFromFile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (actionBar != null)
            actionBar.hide();

        initViews();

        Uri uri = getIntent().getData();
        if (uri != null) {
            m_strNoteFile = uri.toString();
            m_bNewNote = false;
            m_txtBarTitle.setText(R.string.note_edit);
        }
        else {
            m_strNoteFile = noteConfig.getNoteTextFile();
            m_bNewNote = true;
            m_txtBarTitle.setText(R.string.new_note);
        }

        m_layView.postDelayed(()->readFromFile(), 100);
    }

    protected void onStop() {
        super.onStop();
        writeToFile();
    }

    private void initViews () {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        ((ImageButton)findViewById(R.id.imbBack)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbCamera)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbNewPic)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbSaveNote)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbDelPic)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbAudio)).setOnClickListener(this);

        m_txtBarTitle = (TextView)findViewById(R.id.txtBarTitle);

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
                noteEditActivity.this, R.layout.spn_note_type, noteConfig.m_noteTypeMng.getListName());
        m_spnType.setAdapter(adapter);

        m_layView = (LinearLayout)findViewById(R.id.layView);
        noteConfig.m_bNoteModified = false;
    }

    private View addNoteView (View vwAfter, int nType) {
        View vwNew;
        if (nType == 0) {
            noteEditText noteEdit = new noteEditText(this);
            noteEdit.setOnNoteEditListener(this);
            vwNew = noteEdit;
        } else {
            noteImageView noteImage = new noteImageView(this);
            noteImage.setNoteImageListener(this);
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
        // the focus view is edittext?
        if (m_nFocusID >= 10 && m_nFocusID < noteConfig.m_nImagIdStart) {
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

        noteImageView imgView = (noteImageView) addNoteView (vwAfter, 1);
        imgView.setImageFile (strImage);

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

    private void deleteImageView () {
        if (m_nFocusID < noteConfig.m_nImagIdStart)
            return;
        View    vwPrev = null;
        View    vwItem = null;
        View    vwNext = null;
        int nCount = m_layView.getChildCount();
        for (int i = 2; i < nCount; i++) {
            vwItem = m_layView.getChildAt(i);
            if (vwItem.getId() == m_nFocusID) {
                vwNext = m_layView.getChildAt(i+1);
                m_layView.removeView(vwItem);
                break;
            }
            vwPrev = vwItem;
        }

        if (vwPrev != null && vwNext != null) {
            if (vwPrev.getId() < noteConfig.m_nImagIdStart && vwNext.getId() < noteConfig.m_nImagIdStart) {
                String strPrev = ((noteEditText)vwPrev).getText ().toString();
                String strNext = ((noteEditText)vwNext).getText ().toString();
                strPrev = strPrev + strNext;
                ((noteEditText)vwPrev).setText (strPrev);
                m_layView.removeView(vwNext);
            }
        }
    }

    public void onClick(View v) {
        Date    dateNow = new Date(System.currentTimeMillis());
        try {
            String strTime = m_txtDate.getText().toString();
            strTime = strTime+ " " + m_txtTime.getText().toString();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateNow = formatter.parse(strTime);
        }catch (Exception e) {
            e.printStackTrace();
        }
        int     themeId = AlertDialog.THEME_HOLO_DARK;;
        int     nID     = v.getId();
        switch (nID) {
            case R.id.btnDate:
                int nYear = dateNow.getYear() + 1900, nMonth = dateNow.getMonth() + 1, nDate = dateNow.getDate();
                DatePickerDialog dlgDate = new DatePickerDialog(this, themeId, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String strDate = String.format("%d-%02d-%02d", year, month, dayOfMonth);
                        m_txtDate.setText(strDate);
                        noteConfig.m_bNoteModified = true;
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
                        noteConfig.m_bNoteModified = true;
                    }
                }, nHour, nMinute, true);
                dlgTime.show();
                break;

            case R.id.imbBack:
                finish();;
                break;

            case R.id.imbCamera:
                captureImage();
                break;

            case R.id.imbNewPic:
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent, RESULT_LOAD_IMAGE);//打开系统相册
                break;

            case R.id.imbDelPic:
                deleteImageView ();
                break;

            case R.id.imbAudio:

                break;

            case R.id.imbSaveNote:
                finish();
                break;
        }
    }

    public void onMontionEvent (MotionEvent ev, int nID) {
        int y = (int) ev.getY();
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                m_nLastY = y;
                if (nID >= 10) {
                    m_nFocusID = nID;
                    View vwItem = null;
                    for (int i = 2; i < m_layView.getChildCount(); i++) {
                        vwItem = m_layView.getChildAt(i);
                        if (vwItem.getId() > noteConfig.m_nImagIdStart) {
                            ((noteImageView) vwItem).setSelected(false);
                        }
                    }
                }
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
        if (m_bReadFromFile)
            return;
        noteConfig.m_bNoteModified = true;
        onResizeView();
    }

    public void onNoteImageEvent (MotionEvent ev, int nID) {
        m_nFocusID = nID;
        if (ev.getAction() != MotionEvent.ACTION_DOWN)
            return;

        View vwItem = null;
        int nCount = m_layView.getChildCount();
        for (int i = 2; i < nCount; i++) {
            vwItem = m_layView.getChildAt(i);
            if (vwItem.getId() >= noteConfig.m_nImagIdStart) {
                if (vwItem.getId() == m_nFocusID)
                    ((noteImageView)vwItem).setSelected(true);
                else
                    ((noteImageView)vwItem).setSelected(false);
            }
        }
    }

    public void onResizeView () {
        int nHeight = 0;
        int nCount = m_layView.getChildCount();
        for (int i = 0; i < nCount; i++) {
            nHeight += m_layView.getChildAt(i).getHeight();
            Log.e("ResizeView", "height = " + nHeight);
        }
        ViewGroup.LayoutParams param = (ViewGroup.LayoutParams)m_layView.getLayoutParams();
        param.height = nHeight + m_nDispH * 3 / 4;
        m_layView.setLayoutParams(param);
        Log.e("ResizeView", "Total height = " + param.height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        onMontionEvent (ev, 0);
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CAPTURE_IMAGE) {
            File file = new File(m_strImageFile);
            if (!file.exists())
                return;
            addImageView(m_strImageFile);
            noteConfig.m_bNoteModified = true;
            return;
        }

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
        noteConfig.m_bNoteModified = true;
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

    private void captureImage () {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        m_strImageFile = noteConfig.getNotePictFile();
        File file = new File(m_strImageFile);
        if (file.exists()) {
              file.delete();
        }
        Uri uri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent, RESULT_CAPTURE_IMAGE);
    }

    private void readFromFile () {
        m_bReadFromFile = true;
        m_dataItem = new dataNoteItem();
        m_dataItem.readFromFile(m_strNoteFile);
        m_edtTitle.setText(m_dataItem.m_strTitle);
        m_txtDate.setText(m_dataItem.m_strDate);
        m_txtTime.setText(m_dataItem.m_strTime);

        String          strType = null;
        SpinnerAdapter  adapter = m_spnType.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            strType = (String)adapter.getItem(i);
            if (strType.compareTo(m_dataItem.m_strType) == 0) {
                m_spnType.setSelection(i);
                break;
            }
        }

        dataNoteItem.dataContent dataItem = null;
        for (int i = 0; i < m_dataItem.m_lstItem.size(); i++) {
            dataItem = m_dataItem.m_lstItem.get(i);
            if (dataItem.m_nType == dataNoteItem.m_nItemTypeText) {
                noteEditText noteEdit = (noteEditText)addNoteView(null, 0);
                noteEdit.setText(dataItem.m_strItem);
            } else {
                if (m_layView.getChildCount() <= 2)
                    addNoteView(null, 0);
                else {
                    View vwLast = m_layView.getChildAt(m_layView.getChildCount() - 1);
                    if (vwLast.getId() >= noteConfig.m_nImagIdStart)
                        addNoteView(null, 0);
                }
                noteImageView noteImage = (noteImageView)addNoteView(null, 1);
                noteImage.setImageFile(dataItem.m_strItem);
            }
        }
        if (m_layView.getChildCount() <= 2)
            addNoteView(null, 0);
        else {
            View vwLast = m_layView.getChildAt(m_layView.getChildCount() - 1);
            if (vwLast.getId() >= noteConfig.m_nImagIdStart)
                addNoteView(null, 0);
        }
        m_bReadFromFile = false;
        m_layView.postDelayed(()->onResizeView(), 100);
    }

    private void writeToFile () {
        View    vwItem = null;
        int     nCount = m_layView.getChildCount();
        String  strName = "";
        String  strText = "";
        int     nSel = m_spnType.getSelectedItemPosition();
        String  strNoteType = (String)m_spnType.getAdapter().getItem(nSel);
        if (noteConfig.m_bNoteModified == false) {
            if (strNoteType.compareTo(m_dataItem.m_strType) == 0)
                return;
            if (m_edtTitle.getText().toString().length() <= 0) {
                for (int i = 2; i < nCount; i++) {
                    vwItem = m_layView.getChildAt(i);
                    if (vwItem.getId() < noteConfig.m_nImagIdStart) {
                        noteEditText noteText = (noteEditText)vwItem;
                        strText = noteText.getText().toString();
                        if (strText.length() > 0) {
                            break;
                        }
                    }
                }
                if (strText.length() <= 0)
                    return;
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream (m_strNoteFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            strName = noteConfig.m_strTagNoteTitle; bw.write((strName+"\n").toCharArray());
            strText = m_edtTitle.getText().toString(); bw.write((strText+"\n").toCharArray());

            strName = noteConfig.m_strTagNoteDate; bw.write((strName+"\n").toCharArray());
            strText = m_txtDate.getText().toString(); bw.write((strText+"\n").toCharArray());

            strName = noteConfig.m_strTagNoteTime; bw.write((strName+"\n").toCharArray());
            strText = m_txtTime.getText().toString(); bw.write((strText+"\n").toCharArray());

            strName = noteConfig.m_strTagNoteType; bw.write((strName+"\n").toCharArray());
            strText = strNoteType; bw.write((strText+"\n").toCharArray());

            for (int i = 2; i < nCount; i++) {
                vwItem = m_layView.getChildAt(i);
                if (vwItem.getId() < noteConfig.m_nImagIdStart) {
                    noteEditText noteText = (noteEditText)vwItem;
                    strText = noteText.getText().toString();
                    if (strText.length() > 0) {
                        strName = noteConfig.m_strTagNoteText;
                        bw.write((strName + "\n").toCharArray());
                        bw.write((strText + "\n").toCharArray());
                    }
                } else if (vwItem.getId() >= noteConfig.m_nImagIdStart) {
                    noteImageView noteImage = (noteImageView)vwItem;
                    strName = noteConfig.m_strTagNotePict; bw.write((strName+"\n").toCharArray());
                    strText = noteImage.getImageFileName(); bw.write((strText+"\n").toCharArray());
                }
            }
            bw.flush();
            fos.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        noteConfig.m_bNoteModified = true;
    }
}
