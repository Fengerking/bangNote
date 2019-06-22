package com.wyhwl.bangnote;

import android.app.DatePickerDialog;
import android.app.AlertDialog;
import android.app.TimePickerDialog;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
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
import android.widget.AdapterView;
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

import com.wyhwl.bangnote.base.*;
import com.wyhwl.bangnote.view.*;

public class noteEditActivity extends AppCompatActivity
        implements  noteEditText.onNoteEditListener,
                    noteImageView.onNoteImageListener,
                    AdapterView.OnItemSelectedListener,
                    noteAudioEditView.audioChangeListener,
                    noteAudioPlayView.audioPlayViewListener,
                    OnClickListener {
    private static int      RESULT_LOAD_IMAGE       = 10;
    private static int      RESULT_LOAD_AUDIO       = 11;
    private static int      RESULT_CAPTURE_IMAGE    = 20;
    private static int      RESULT_LOAD_MEDIA       = 50;

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
    private String          m_strMusicFile = null;
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

        m_dataItem = new dataNoteItem();
        m_layView.postDelayed(()->readFromFile(), 100);
    }

    protected void onPause() {
        writeToFile();
        super.onPause();
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
        ((ImageButton)findViewById(R.id.imbMusic)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbMusic)).setVisibility(View.INVISIBLE);

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
                noteEditActivity.this, R.layout.spn_note_type, noteConfig.m_noteTypeMng.getListName(m_bNewNote));
        m_spnType.setAdapter(adapter);
        m_spnType.setOnItemSelectedListener(this);

        m_layView = (LinearLayout)findViewById(R.id.layView);
        noteConfig.m_bNoteModified = false;
    }

    private View addNoteView (View vwAfter, int nType) {
        View vwNew = null;
        if (nType == noteConfig.m_nItemTypeText) {
            noteEditText noteEdit = new noteEditText(this);
            noteEdit.setOnNoteEditListener(this);
            vwNew = noteEdit;
        } else if (nType == noteConfig.m_nItemTypePict){
            noteImageView noteImage = new noteImageView(this);
            noteImage.setNoteImageListener(this);
            vwNew = noteImage;
        } else if (nType == noteConfig.m_nItemTypeAudo){
            noteAudioEditView noteAudio = new noteAudioEditView(this);
            vwNew = noteAudio;
        } else if (nType == noteConfig.m_nItemTypeMusc){
            noteAudioPlayView noteMusic = new noteAudioPlayView(this, noteConfig.m_nItemTypeMusc);
            if (m_strMusicFile != null)
                noteMusic.setAudioFile(m_strMusicFile);
            vwNew = noteMusic;
        } else {
            return null;
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

    private void addMediaView (String strFile, int nType) {
        View  vwAfter = null;
        // the focus view is edittext?
        if (noteConfig.getNoteviewType(m_nFocusID) == noteConfig.m_nItemTypeText) {
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
                        noteEditText noteView = (noteEditText) addNoteView(vwAfter, noteConfig.m_nItemTypeText);
                        noteView.setText(strNext);
                    } else if (strText.length() == 0) {
                        if (vwPrev != null && noteConfig.getNoteviewType(vwPrev) == noteConfig.m_nItemTypeText ) {
                            m_layView.removeView(vwFocus);
                            vwAfter = vwPrev;
                        } else {
                            vwAfter = vwFocus;
                        }
                    } else {
                        vwAfter = vwFocus;
                    }
                    break;
                }
                vwPrev = vwFocus;
            }
        } else {
            int nCount = m_layView.getChildCount();
            for (int i = 2; i < nCount; i++) {
                 if (m_layView.getChildAt(i).getId() == m_nFocusID) {
                     vwAfter = m_layView.getChildAt(i);
                     vwAfter = addNoteView(vwAfter, noteConfig.m_nItemTypeText);
                     break;
                }
            }
        }

        View vwLast = null;
        if (nType == noteConfig.m_nItemTypePict) {
            noteImageView imgView = (noteImageView) addNoteView(vwAfter, noteConfig.m_nItemTypePict);
            imgView.setImageFile(strFile, false);
            vwLast = imgView;
            noteConfig.m_bNoteModified = true;
        } else if (nType == noteConfig.m_nItemTypeAudo) {
            noteAudioEditView audView = (noteAudioEditView) addNoteView(vwAfter, noteConfig.m_nItemTypeAudo);
            audView.setAudioChangeListener(this);
            ViewGroup.LayoutParams param = (ViewGroup.LayoutParams)audView.getLayoutParams();
            param.width = -1;
            audView.setLayoutParams(param);
            vwLast = audView;
            noteConfig.m_bNoteModified = true;
        } else if (nType == noteConfig.m_nItemTypeMusc) {
            noteAudioPlayView audView = (noteAudioPlayView) addNoteView(vwAfter, noteConfig.m_nItemTypeMusc);
            audView.setAudioPlayListener(this);
            ViewGroup.LayoutParams param = (ViewGroup.LayoutParams)audView.getLayoutParams();
            param.width = -1;
            audView.setLayoutParams(param);
            vwLast = audView;
            noteConfig.m_bNoteModified = true;
        }

        int nCount = m_layView.getChildCount();
        if (m_layView.getChildAt(nCount - 1) == vwLast) {
            addNoteView(null, noteConfig.m_nItemTypeText);
        } else {
            View vwNext = null;
            for (int i = 2; i < nCount; i++) {
                if (m_layView.getChildAt(i) == vwLast) {
                    vwNext = m_layView.getChildAt(i+1);
                    if (noteConfig.getNoteviewType(vwNext) != noteConfig.m_nItemTypeText) {
                        addNoteView(vwLast, noteConfig.m_nItemTypeText);
                    }
                    break;
                }
            }
        }
        onResizeView();
    }

    private void deleteMediaView (View view) {
        if (view == null && noteConfig.getNoteviewType(m_nFocusID) == noteConfig.m_nItemTypeText)
            return;
        View    vwPrev = null;
        View    vwItem = null;
        View    vwNext = null;
        int nCount = m_layView.getChildCount();
        for (int i = 2; i < nCount; i++) {
            vwItem = m_layView.getChildAt(i);
            if (view == null) {
                if (vwItem.getId() == m_nFocusID) {
                    vwNext = m_layView.getChildAt(i + 1);
                    m_layView.removeView(vwItem);
                    break;
                }
            } else {
                if (vwItem == view) {
                    vwNext = m_layView.getChildAt(i + 1);
                    m_layView.removeView(vwItem);
                    break;
                }
            }
            vwPrev = vwItem;
        }

        if (vwPrev != null && vwNext != null) {
            if (noteConfig.getNoteviewType(vwPrev) == noteConfig.m_nItemTypeText &&
                    noteConfig.getNoteviewType(vwNext) == noteConfig.m_nItemTypeText) {
                String strPrev = ((noteEditText)vwPrev).getText ().toString();
                String strNext = ((noteEditText)vwNext).getText ().toString();
                strPrev = strPrev + strNext;
                ((noteEditText)vwPrev).setText (strPrev);
                m_layView.removeView(vwNext);
            }
        }
        noteConfig.m_bNoteModified = true;
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
        Intent  intent = null;
        int     themeId = AlertDialog.THEME_HOLO_DARK;;
        int     nID     = v.getId();
        switch (nID) {
            case R.id.btnDate:
                int nYear = dateNow.getYear() + 1900, nMonth = dateNow.getMonth() + 1, nDate = dateNow.getDate();
                DatePickerDialog dlgDate = new DatePickerDialog(this, themeId, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String strDate = String.format("%d-%02d-%02d", year, month+1, dayOfMonth);
                        m_txtDate.setText(strDate);

                        if (m_bNewNote) {
                            Date dateNow = new Date(System.currentTimeMillis());
                            if (dateNow.getDate() != dayOfMonth || dateNow.getMonth() != month)
                                m_dataItem.m_strWeat = "";
                            else
                                m_dataItem.m_strWeat = noteConfig.m_strWeather;
                        }

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
               //intent = new Intent("android.intent.action.GET_CONTENT");
               //intent.setType("image/*");
               // startActivityForResult(intent, RESULT_LOAD_IMAGE);//打开系统相册
                intent = new Intent(noteEditActivity.this, mediaSelectActivity.class);
                startActivityForResult(intent, RESULT_LOAD_MEDIA);
                break;

            case R.id.imbDelPic:
                deleteMediaView (null);
                break;

            case R.id.imbAudio:
                addMediaView(null, noteConfig.m_nItemTypeAudo);
                break;

            case R.id.imbMusic:
                m_strMusicFile = null;
                intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("audio/*");
                startActivityForResult(intent, RESULT_LOAD_AUDIO);
                break;

            case R.id.imbSaveNote:
                finish();
                break;
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        TextView tvType = (TextView) view.findViewById(R.id.txtNoteType);
        if (m_dataItem.m_strType.compareTo(tvType.getText().toString()) != 0) {
            m_dataItem.m_strType = tvType.getText().toString();
            noteConfig.m_bNoteModified = true;
        }
    }
    public void onNothingSelected(AdapterView<?> parent) {
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
                        if (noteConfig.getNoteviewType(vwItem) == noteConfig.m_nItemTypePict) {
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
            if (noteConfig.getNoteviewType(vwItem) == noteConfig.m_nItemTypePict) {
                if (vwItem.getId() == m_nFocusID)
                    ((noteImageView)vwItem).setSelected(true);
                else
                    ((noteImageView)vwItem).setSelected(false);
            }
        }
    }

    public void onAudioChange (View view, String strFile) {
        noteConfig.m_bNoteModified = true;
        if (strFile == null)
            deleteMediaView(view);
    }

    public void onAudioPlayChange (View view, int nCommand) {
        if (nCommand == R.id.btnAudioDelete)
            deleteMediaView(view);
    }

    public void onResizeView () {
        int nHeight = 0;
        int nCount = m_layView.getChildCount();
        for (int i = 0; i < nCount; i++) {
            nHeight += m_layView.getChildAt(i).getHeight();
            Log.v("bangNoteDebug", "height = " + nHeight);
        }
        ViewGroup.LayoutParams param = (ViewGroup.LayoutParams)m_layView.getLayoutParams();
        param.height = nHeight + m_nDispH * 3 / 4;
        m_layView.setLayoutParams(param);
        Log.v("bangNoteDebug", "Total height = " + param.height);
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
            addMediaView(m_strImageFile, noteConfig.m_nItemTypePict);
            return;
        } else if (requestCode == RESULT_LOAD_MEDIA && resultCode == RESULT_OK) {
            if (data == null)
                return;
            int nFileNum = data.getIntExtra("FileCount", 0);
            String[] strFiles = data.getStringArrayExtra("FileList");
            for (int i = 0; i < nFileNum; i++) {
                int nMediaType = mediaSelectAdapter.getMediaType(strFiles[i]);
                if (nMediaType == mediaSelectAdapter.m_nMediaImage) {
                    addMediaView(strFiles[i], noteConfig.m_nItemTypePict);
                } else if (nMediaType == mediaSelectAdapter.m_nMediaAudio) {
                    m_strMusicFile = strFiles[i];
                    addMediaView(strFiles[i], noteConfig.m_nItemTypeMusc);
                }
            }
            m_layView.postDelayed(()->onResizeView(), 200);
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
                if (requestCode == RESULT_LOAD_IMAGE) {
                    imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, requestCode);
                } else if (requestCode == RESULT_LOAD_AUDIO) {
                    imagePath = getImagePath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, selection, requestCode);
                }
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content:" +
                                                        "//downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null, requestCode);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的uri，则使用普通方式处理
            //imagePath = getImagePath(uri, null, requestCode);
            String strName = uri.getEncodedPath();
            int nPos = strName.indexOf(File.separatorChar, 2);
            strName = strName.substring(nPos);
            File file = Environment.getExternalStorageDirectory();
            String strExtPath = file.getPath();
            imagePath = strExtPath + strName;
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是File类型的uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        if (imagePath != null) {
            if (requestCode == RESULT_LOAD_IMAGE) {
                addMediaView(imagePath, noteConfig.m_nItemTypePict);
            } else if (requestCode == RESULT_LOAD_AUDIO) {
                m_strMusicFile = imagePath;
                addMediaView(imagePath, noteConfig.m_nItemTypeMusc);
            }
        }
    }

    private String getImagePath(Uri uri, String selection, int requestCode) {
        String path = null;
        //通过uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if (requestCode == RESULT_LOAD_IMAGE)
                    path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                else if (requestCode == RESULT_LOAD_AUDIO)
                    path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
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
        m_dataItem.readFromFile(m_strNoteFile);
        if (m_bNewNote) {
            m_dataItem.m_strCity = noteConfig.m_strCityName;
            m_dataItem.m_strWeat = noteConfig.m_strWeather;
            if (noteConfig.m_noteTypeMng.isSelTotalType())
                m_dataItem.m_strType = noteConfig.m_noteTypeMng.m_strDefault;
            else
                m_dataItem.m_strType = noteConfig.m_noteTypeMng.getCurType();

        }
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
            if (dataItem.m_nType == noteConfig.m_nItemTypeText) {
                noteEditText noteEdit = (noteEditText)addNoteView(null, noteConfig.m_nItemTypeText);
                noteEdit.setText(dataItem.m_strItem);
            } else {
                if (m_layView.getChildCount() <= 2)
                    addNoteView(null, noteConfig.m_nItemTypeText);
                else {
                    View vwLast = m_layView.getChildAt(m_layView.getChildCount() - 1);
                    if (noteConfig.getNoteviewType(vwLast) != noteConfig.m_nItemTypeText)
                        addNoteView(null, noteConfig.m_nItemTypeText);
                }

                if (dataItem.m_nType == noteConfig.m_nItemTypePict) {
                    noteImageView noteImage = (noteImageView)addNoteView(null, noteConfig.m_nItemTypePict);
                    noteImage.setImageFile(dataItem.m_strItem, true);
                } else if (dataItem.m_nType == noteConfig.m_nItemTypeAudo) {
                    noteAudioEditView noteAudio = (noteAudioEditView)addNoteView(null, noteConfig.m_nItemTypeAudo);
                    noteAudio.setAudioFile(dataItem.m_strItem);
                    noteAudio.setAudioChangeListener(this);
                } else if (dataItem.m_nType == noteConfig.m_nItemTypeMusc) {
                    noteAudioPlayView noteMusic = (noteAudioPlayView)addNoteView(null, noteConfig.m_nItemTypeMusc);
                    noteMusic.setAudioFile(dataItem.m_strItem);
                    //noteMusic.setAudioChangeListener(this);
                }
            }
        }
        if (m_layView.getChildCount() <= 2)
            addNoteView(null, noteConfig.m_nItemTypeText);
        else {
            View vwLast = m_layView.getChildAt(m_layView.getChildCount() - 1);
            if (noteConfig.getNoteviewType(vwLast) != noteConfig.m_nItemTypeText)
                addNoteView(null, noteConfig.m_nItemTypeText);
        }
        m_bReadFromFile = false;
        m_layView.postDelayed(()->onResizeView(), 100);
    }

    private void writeToFile () {
        if (noteConfig.m_bNoteModified == false) {
            return;
        }
        View    vwItem = null;
        int     nCount = m_layView.getChildCount();
        if (m_bNewNote) {
            boolean bModified = false;
            if (m_edtTitle.getText().toString().length() > 0)
                bModified = true;
            if (nCount > 2) {
                vwItem = m_layView.getChildAt(2);
                if (noteConfig.getNoteviewType(vwItem) == noteConfig.m_nItemTypeText) {
                    if (((noteEditText) vwItem).getText().toString().length() > 0)
                        bModified = true;
                }
            }
            if (nCount > 3) {
                vwItem = m_layView.getChildAt(3);
                if (noteConfig.getNoteviewType(vwItem) != noteConfig.m_nItemTypeText)
                    bModified = true;
            }
            if (!bModified) {
                noteConfig.m_bNoteModified = false;
                return;
            }
        }

        String  strName = "";
        String  strText = "";
        try {
            noteFileOutputStream fos = new noteFileOutputStream (m_strNoteFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            strName = noteConfig.m_strTagNoteTitle; bw.write((strName+"\n").toCharArray());
            strText = m_edtTitle.getText().toString(); bw.write((strText+"\n").toCharArray());

            strName = noteConfig.m_strTagNoteDate; bw.write((strName+"\n").toCharArray());
            strText = m_txtDate.getText().toString(); bw.write((strText+"\n").toCharArray());

            strName = noteConfig.m_strTagNoteTime; bw.write((strName+"\n").toCharArray());
            strText = m_txtTime.getText().toString(); bw.write((strText+"\n").toCharArray());

            if (m_dataItem.m_strCity != null && m_dataItem.m_strCity.length() > 0) {
                strName = noteConfig.m_strTagNoteCity; bw.write((strName+"\n").toCharArray());
                strText = m_dataItem.m_strCity; bw.write((strText+"\n").toCharArray());
            }
            if (m_dataItem.m_strWeat != null && m_dataItem.m_strWeat.length() > 0) {
                strName = noteConfig.m_strTagNoteWeat; bw.write((strName+"\n").toCharArray());
                strText = m_dataItem.m_strWeat; bw.write((strText+"\n").toCharArray());
            }

            strName = noteConfig.m_strTagNoteType; bw.write((strName+"\n").toCharArray());
            strText = m_dataItem.m_strType; bw.write((strText+"\n").toCharArray());

            int     nNotePathLen = noteConfig.m_strNotePath.length();
            for (int i = 2; i < nCount; i++) {
                vwItem = m_layView.getChildAt(i);
                if (noteConfig.getNoteviewType(vwItem) == noteConfig.m_nItemTypeText) {
                    noteEditText noteText = (noteEditText)vwItem;
                    strText = noteText.getText().toString();
                    if (strText.length() > 0) {
                        strName = noteConfig.m_strTagNoteText;
                        bw.write((strName + "\n").toCharArray());
                        bw.write((strText + "\n").toCharArray());
                    }
                } else if (noteConfig.getNoteviewType(vwItem) == noteConfig.m_nItemTypePict) {
                    noteImageView noteImage = (noteImageView)vwItem;
                    strName = noteConfig.m_strTagNotePict; bw.write((strName+"\n").toCharArray());
                    strText = noteImage.getImageFileName().substring(nNotePathLen); bw.write((strText+"\n").toCharArray());
                } else if (noteConfig.getNoteviewType(vwItem) == noteConfig.m_nItemTypeAudo) {
                    noteAudioEditView noteAudio = (noteAudioEditView)vwItem;
                    String strAudioFile = noteAudio.getAudioFile();
                    if (strAudioFile != null) {
                        File fileAudio = new File (strAudioFile);
                        if (fileAudio.exists()) {
                            strName = noteConfig.m_strTagNoteAudo; bw.write((strName+"\n").toCharArray());
                            strText = strAudioFile.substring(nNotePathLen); bw.write((strText+"\n").toCharArray());
                        }
                    }
                } else if (noteConfig.getNoteviewType(vwItem) == noteConfig.m_nItemTypeMusc) {
                    noteAudioPlayView noteMusic = (noteAudioPlayView)vwItem;
                    String strAudioFile = noteMusic.getAudioFile();
                    strName = noteConfig.m_strTagNoteMusc; bw.write((strName+"\n").toCharArray());
                    strText = strAudioFile.substring(nNotePathLen); bw.write((strText+"\n").toCharArray());
                }
            }
            bw.flush();
            fos.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        if (m_bNewNote)
            noteConfig.m_lstData.newNoteFile(m_dataItem.m_strFile);
        else
            noteConfig.m_lstData.updNoteFile(m_dataItem.m_strFile);
    }
}
