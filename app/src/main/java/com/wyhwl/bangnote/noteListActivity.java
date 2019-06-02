package com.wyhwl.bangnote;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.net.Uri;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.EditText;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.RadioGroup;
import android.view.inputmethod.InputMethodManager;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.zip.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import okhttp3.Call;

import com.wyhwl.bangnote.base.*;
import com.wyhwl.bangnote.view.*;

public class noteListActivity extends AppCompatActivity
                              implements noteListSlider.switchListener,
                                            AdapterView.OnItemClickListener,
                                            AdapterView.OnItemLongClickListener,
                                            View.OnClickListener {
    public static final int     REQUEST_STORAGE     = 1;

    private ListView            m_lstView = null;
    private noteListSlider      m_sldList = null;

    private ImageButton         m_btnNewNote = null;

    private LinearLayout        m_layToolBarList = null;
    private RelativeLayout      m_layToolBarSearch = null;
    private ListView            m_lstViewLeft = null;
    private ListView            m_lstViewRight = null;
    private String              m_strNewNote = "新建笔记";

    public  ArrayList<dataNoteItem>  m_lstRubbish = new ArrayList<dataNoteItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        CheckWritePermission();
        noteConfig.initConfig(this);
        noteConfig.m_lstData = noteConfig.m_lstData;

        initViews();
    }

    protected void onResume() {
        super.onResume();
        if (noteConfig.m_bNoteModified) {
            m_lstView.setAdapter(noteConfig.m_lstData);
            m_lstView.invalidate();
            fillLeftList(false);
        }
    }

    protected void onStop() {
        super.onStop();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            noteConfig.m_nShowSecurity = 0;
            noteConfig.m_noteTypeMng.checkSecurity();
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void initViews () {
        ((ImageButton)findViewById(R.id.imbNewNote)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbDelNote)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbSearchNote)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbSearchAll)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbSearchSelect)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.appBack)).setOnClickListener(this);

        m_layToolBarList = (LinearLayout) findViewById(R.id.ntlList);
        //m_layToolBarList.setVisibility(View.INVISIBLE);
        m_layToolBarSearch = (RelativeLayout)findViewById(R.id.ntlSearch);
        m_layToolBarSearch.setVisibility(View.INVISIBLE);

        m_sldList = (noteListSlider) findViewById(R.id.sldList);
        m_sldList.setSwitchListener(this);

        noteConfig.m_lstData = new noteListAdapter(this);
        m_lstView = (ListView) findViewById(R.id.vwNoteList);
        m_lstView.setOnItemClickListener(this);
        m_lstView.setOnItemLongClickListener(this);
        m_lstView.setAdapter(noteConfig.m_lstData);

        m_lstViewLeft = (ListView)findViewById(R.id.lstNoteTypeSel);
        m_lstViewLeft.setOnItemClickListener(this);
        m_lstViewLeft.setOnItemLongClickListener(this);
        fillLeftList(false);
        m_lstViewRight = (ListView)findViewById(R.id.lstNoteTypeMng);
        m_lstViewRight.setOnItemClickListener(this);
        fillRightList();

        m_btnNewNote = (ImageButton) findViewById(R.id.btnNewNote);
        m_btnNewNote.setVisibility(View.INVISIBLE);
        m_btnNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(noteListActivity.this, noteEditActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        getTodayWeather();
    }

    public void onClick(View v) {
        int nID = v.getId();
        switch (nID) {
            case R.id.imbNewNote:
                Intent intent = new Intent(noteListActivity.this, noteEditActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.imbDelNote:
                deleteSelectedNote();
                break;

            case R.id.imbSearchNote:
                m_layToolBarSearch.setVisibility(View.VISIBLE);
                m_layToolBarList.setVisibility(View.INVISIBLE);
                break;

            case R.id.appBack:
                m_layToolBarList.setVisibility(View.VISIBLE);
                m_layToolBarSearch.setVisibility(View.INVISIBLE);
                break;

            case R.id.imbSearchAll:
            case R.id.imbSearchSelect:
                m_layToolBarSearch.setVisibility(View.INVISIBLE);
                String strFilter = ((EditText)findViewById(R.id.edtSearch)).getText().toString();
                if (strFilter.length() <= 0)
                    break;
                if (nID == R.id.imbSearchAll)
                    noteConfig.m_lstData.searchNoteItem(strFilter, true);
                else
                    noteConfig.m_lstData.searchNoteItem(strFilter, false);
                m_lstView.setAdapter(noteConfig.m_lstData);
                m_lstView.invalidate();
                InputMethodManager imm = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.edtSearch)).getWindowToken(), 0);
                break;
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == (View)m_lstView) {
            dataNoteItem noteItem = noteConfig.m_lstData.getNoteItem(position);
            Intent intent = new Intent(noteListActivity.this, noteViewActivity.class);
            intent.setData(Uri.parse(noteItem.m_strFile));

            int nSize = noteConfig.m_lstData.getCount();
            String[] strFileList = new String[nSize];
            for (int i = 0; i < nSize; i++) {
                strFileList[i] = ((dataNoteItem)noteConfig.m_lstData.getItem(i)).m_strFile;
            }
            intent.putExtra("FileList", strFileList);
            intent.putExtra("FileCount", nSize);

            startActivityForResult(intent, 1);
        } else if (parent == (View)m_lstViewLeft) {
            TextView tvType = (TextView) view.findViewById(R.id.name);
            String strType = tvType.getText().toString();
            if (strType.compareTo(m_strNewNote) == 0) {
                Intent intent = new Intent(noteListActivity.this, noteEditActivity.class);
                startActivityForResult(intent, 1);
            } else {
                noteConfig.m_noteTypeMng.setCurType(strType);
                noteConfig.m_lstData.updNoteType(strType);
                fillLeftList (true);
                updateList();
            }
        }else if (parent == (View)m_lstViewRight) {
            TextView tvText = (TextView) view.findViewById(R.id.name);
            String strCommand = tvText.getText().toString();
            if (strCommand.compareTo("增加类型") == 0) {
                addNoteTypeDialog();
            } else if (strCommand.compareTo("删除类型") == 0) {
                delNoteTypeDialog();
            } else if (strCommand.compareTo("修改类型") == 0) {
                chgNoteTypeDialog();
            } else if (strCommand.compareTo("转移类型") == 0) {
                movNoteTypeDialog();
            } else if (strCommand.compareTo("删除笔记") == 0) {
                deleteSelectedNote();
            } else if (strCommand.compareTo("清除垃圾") == 0) {
                delNoteItemDialog();
            } else if (strCommand.compareTo("全部选中") == 0) {
                selectAllItems (true);
            } else if (strCommand.compareTo("清除选择") == 0) {
                selectAllItems (false);
            } else if (strCommand.compareTo("备份笔记") == 0) {
                noteBackupRestore noteBackup = new noteBackupRestore(this);
                if (noteBackup.backupNote () > 0)
                    showMsgDlg ("备份笔记成功", null);
                else
                    showMsgDlg ("备份笔记失败", null);
            } else if (strCommand.compareTo("恢复备份") == 0) {
                noteBackupRestore noteBackup = new noteBackupRestore(this);
                if (noteBackup.restoreNote () > 0) {
                    showMsgDlg("恢复笔记成功", null);
                    noteConfig.m_lstData.fillFileList(noteConfig.m_strNotePath);
                    updateList();
                } else {
                    showMsgDlg("恢复笔记失败", null);
                }
            } else if (strCommand.compareTo("发送备份") == 0) {

            } else if (strCommand.compareTo("笔记设置") == 0) {

            } else if (strCommand.compareTo("退出笔记") == 0) {
                System.exit(0);
            }
        }
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == (View)m_lstView) {
            noteListItemView noteView = (noteListItemView) view;
            noteView.getDataList().setSelect();
            noteView.invalidate();
        } else if (parent == (View)m_lstViewLeft) {
            TextView tvType = (TextView)view.findViewById(R.id.name);
            String strType = tvType.getText().toString();
            if (strType.compareTo(noteConfig.m_noteTypeMng.m_strTotal) == 0) {
                long lTimeNow = System.currentTimeMillis();
                if (lTimeNow - noteConfig.m_nMoveLastTime < 5000 && noteConfig.m_nMoveCount == noteConfig.m_nMoveNeedTime) {
                    noteConfig.m_nShowSecurity = 1;
                    fillLeftList(false);
                    Toast.makeText(this,"开启秘密笔记！",Toast.LENGTH_SHORT).show();
                }
            }
        }
        return true;
    }

    protected void fillLeftList (boolean bSave) {
        ArrayList<Map<String, Object>> listItem = new ArrayList<Map<String, Object>>();
        addNoteType (listItem, 1);
        addNoteType (listItem, 2);
        addNoteType (listItem, 3);
        addNoteType (listItem, 4);

        SimpleAdapter adapter = new SimpleAdapter(this, listItem, R.layout.menu_list_item,
                new String[]{"name","count","img"}, new int[]{R.id.name, R.id.count,R.id.img});
        m_lstViewLeft.setAdapter(adapter);

        if (bSave)
            noteConfig.m_noteTypeMng.writeToFile();
    }

    private void addNoteType (ArrayList<Map<String, Object>> listItem, int nType) {
        if (nType == 1 && noteConfig.m_nShowSecurity == 0)
            return;

        HashMap<String, Object>     mapItem;
        noteTypeMng.noteTypeItem    itemType;
        if (nType == 4) {
            mapItem = new HashMap<String, Object>();
            mapItem.put("name", m_strNewNote);
            mapItem.put("img", R.drawable.newnote);
            listItem.add(mapItem);
            return;
        }
        int nCount = noteConfig.m_noteTypeMng.getCount();
        for (int i = 0; i < nCount; i++) {
            itemType = noteConfig.m_noteTypeMng.getItem(i);
            if (nType == 1) {
                if (itemType.m_nLevel < 10)
                    continue;
            } else if (nType == 2) {
                if (itemType.m_nLevel < 0 || itemType.m_nLevel >= 10)
                    continue;
            } else {
                if (itemType.m_nLevel >= 0)
                    continue;
            }
            mapItem = new HashMap<String, Object>();
            mapItem.put("name", itemType.m_strName);
            mapItem.put("count", "{" + noteConfig.m_lstData.getItemCount(itemType.m_strName) + "}");
            if (itemType.m_nUsing > 0) {
                mapItem.put("img", R.drawable.notetype_sel);
            } else {
                if (nType == 1)
                    mapItem.put("img", R.drawable.notetype_lock);
                else
                    mapItem.put("img", itemType.m_nImage);
            }
            listItem.add(mapItem);
        }
    }

    protected void fillRightList () {
        ArrayList<Map<String, Object>> listItem = new ArrayList<Map<String, Object>>();
        HashMap<String, Object> mapItem;
        addRightCommand (listItem, "增加类型", R.drawable.notetype_new);
        addRightCommand (listItem, "删除类型", R.drawable.notetype_del);
        addRightCommand (listItem, "修改类型", R.drawable.notetype_modify);
        addRightCommand (listItem, "转移类型", R.drawable.notetype_move);
        addRightCommand (listItem, "删除笔记", R.drawable.note_delete);
        addRightCommand (listItem, "清除垃圾", R.drawable.lajitong);
        addRightCommand (listItem, "全部选中", R.drawable.notetype_selall);
        addRightCommand (listItem, "清除选择", R.drawable.notetype_selnone);
        addRightCommand (listItem, "备份笔记", R.drawable.note_backup);
        addRightCommand (listItem, "恢复备份", R.drawable.note_restore);
        //addRightCommand (listItem, "发送备份", R.drawable.note_send);
        addRightCommand (listItem, "笔记设置", R.drawable.note_setting);
        addRightCommand (listItem, "退出笔记", R.drawable.note_exit);

        SimpleAdapter adapter = new SimpleAdapter(this, listItem, R.layout.menu_list_item,
                new String[]{"name","img"}, new int[]{R.id.name, R.id.img});
        m_lstViewRight.setAdapter(adapter);
    }

    private void addRightCommand (ArrayList<Map<String, Object>> listItem, String strName, int nIcon) {
        HashMap<String, Object> mapItem;
        mapItem = new HashMap<String, Object>();
        mapItem.put("name", strName);
        mapItem.put("img", nIcon);
        listItem.add(mapItem);
    }

    protected void updateList () {
        noteConfig.m_lstData.updateNoteItem();
        m_lstView.setAdapter(noteConfig.m_lstData);
        m_lstView.invalidate();
        fillLeftList(false);
        selectAllItems (false);
        m_sldList.scrollToPage (1);
    }

    protected void deleteSelectedNote () {
        dataNoteItem        dataItem = null;
        int                 nCount = noteConfig.m_lstData.getCount();
        ArrayList<String>   lstDelFiles = new ArrayList<String>();
        for (int i = 0; i < nCount; i++) {
            dataItem = (dataNoteItem)noteConfig.m_lstData.getItem(i);
            if (dataItem.isSelect()) {
                if (dataItem.m_strType.compareTo(noteConfig.m_noteTypeMng.m_strRubbish) == 0) {
                    lstDelFiles.add(dataItem.m_strFile);
                } else {
                    dataItem.m_strType = noteConfig.m_noteTypeMng.m_strRubbish;
                    dataItem.writeToFile();
                }
            }
        }
        for (int i = 0; i < lstDelFiles.size(); i++) {
            noteConfig.m_lstData.delNoteFile(lstDelFiles.get(i));
            File fileDel = new File (lstDelFiles.get(i));
            fileDel.delete();
        }
        lstDelFiles.clear();

        updateList();
    }

    protected void selectAllItems (boolean bSelect) {
        dataNoteItem    dataItem = null;
        int             nCount = noteConfig.m_lstData.getCount();
        for (int i = 0; i < nCount; i++) {
            dataItem = (dataNoteItem)noteConfig.m_lstData.getItem(i);
            if (bSelect) {
                if (!dataItem.isSelect())
                    dataItem.setSelect();
            } else {
                if (dataItem.isSelect())
                    dataItem.setSelect();
            }
        }
        m_lstView.setAdapter(noteConfig.m_lstData);
        m_lstView.invalidate();
        m_sldList.scrollToPage (1);
    }

    public int onSwitchStart(int nView) {
        return 0;
    }

    public int OnSwitchEnd(int nView) {
        return 0;
    }

    public int onMotionEvent(MotionEvent ev) {
        m_lstView.onTouchEvent(ev);
        return 0;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addNoteTypeDialog() {
        final View noteTypeView = LayoutInflater.from(noteListActivity.this)
                .inflate(R.layout.note_type_input,null);
        AlertDialog.Builder dlgNoteType = new AlertDialog.Builder(noteListActivity.this){
            public AlertDialog create() {
                RadioButton rbnNormal = (RadioButton)noteTypeView.findViewById(R.id.noteTypeNormal);
                rbnNormal.setChecked(true);
                RadioButton rbnSecurity = (RadioButton)noteTypeView.findViewById(R.id.noteTypeSecurity);
                rbnSecurity.setChecked(false);
                if (noteConfig.m_nShowSecurity == 0) {
                    noteTypeView.findViewById(R.id.laySecurity).setVisibility(View.INVISIBLE);
                }
                return super.create();
            }
            public AlertDialog show() {
                return super.show();
            }
        };

        dlgNoteType.setIcon(R.drawable.notetype_aa);
        dlgNoteType.setTitle("输入笔记类型名称：");
        dlgNoteType.setView(noteTypeView);
        dlgNoteType.setNeutralButton("取消",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        m_sldList.scrollToPage (1);
                    }
                });
        dlgNoteType.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String strType = ((EditText)noteTypeView.findViewById(R.id.noteType)).getText().toString();
                        RadioButton rtbSecurity = (RadioButton)noteTypeView.findViewById(R.id.noteTypeSecurity);
                        if (strType.length() > 0) {
                            int nRC = 0;
                            if (rtbSecurity.isChecked())
                                nRC = noteConfig.m_noteTypeMng.addType(strType, 10);
                            else
                                nRC = noteConfig.m_noteTypeMng.addType(strType, 0);
                            if (nRC < 0)
                                Toast.makeText(noteListActivity.this, "增加笔记类型失败了！", Toast.LENGTH_SHORT).show();
                            else {
                                m_sldList.scrollToPage (1);
                                fillLeftList(true);
                            }
                        }
                    }
                });
        dlgNoteType.show();
    }

    private void delNoteTypeDialog() {
        final View noteDelView = LayoutInflater.from(noteListActivity.this)
                .inflate(R.layout.note_type_delete,null);
        AlertDialog.Builder dlgNoteTDel =
                new AlertDialog.Builder(noteListActivity.this){
                    public AlertDialog create() {
                        LinearLayout layItems = (LinearLayout)noteDelView.findViewById(R.id.layItems);
                        noteTypeMng.noteTypeItem    itemType = null;
                        int                         nCount = noteConfig.m_noteTypeMng.getCount();
                        for (int i = 0; i < nCount; i++) {
                            itemType = noteConfig.m_noteTypeMng.getItem(i);
                            if (noteConfig.m_nShowSecurity == 0 && itemType.m_nLevel >= 10)
                                continue;
                            if (itemType.m_nLevel < 0)
                                continue;

                            CheckBox chkNoteType = new CheckBox (noteListActivity.this);
                            chkNoteType.setText(itemType.m_strName);
                            chkNoteType.setChecked(false);
                            layItems.addView(chkNoteType);

                            dataNoteItem itemData = null;
                            int nAllCount = noteConfig.m_lstData.m_lstAllItem.size();
                            for (int j = 0; j < nAllCount; j++) {
                                itemData = noteConfig.m_lstData.m_lstAllItem.get(j);
                                if (itemData.m_strType.compareTo(itemType.m_strName) == 0) {
                                    chkNoteType.setEnabled(false);
                                    break;
                                }
                            }
                        }
                        return super.create();
                    }
                    public AlertDialog show() {
                        return super.show();
                    }
                };

        dlgNoteTDel.setIcon(R.drawable.notetype_del);
        dlgNoteTDel.setTitle("删除笔记类型");
        dlgNoteTDel.setView(noteDelView);
        dlgNoteTDel.setNeutralButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                m_sldList.scrollToPage (1);
            }
        });
        dlgNoteTDel.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                LinearLayout    layItems = (LinearLayout)noteDelView.findViewById(R.id.layItems);
                int             nCount = layItems.getChildCount();
                CheckBox        chkNoteType = null;
                for (int i = 0; i < nCount; i++) {
                    chkNoteType = (CheckBox)layItems.getChildAt(i);
                    if (chkNoteType.isChecked()) {
                        String strType = chkNoteType.getText().toString();
                        noteConfig.m_noteTypeMng.delType(strType);
                    }
                }
                m_sldList.scrollToPage (1);
                fillLeftList(true);
            }
        });
        dlgNoteTDel.show();
    }

    private void delNoteItemDialog() {
        final View noteDelView = LayoutInflater.from(noteListActivity.this)
                .inflate(R.layout.note_type_delete,null);
        AlertDialog.Builder dlgNoteTDel =
                new AlertDialog.Builder(noteListActivity.this){
                    public AlertDialog create() {
                        m_lstRubbish.clear();
                        LinearLayout layItems = (LinearLayout)noteDelView.findViewById(R.id.layItems);
                        dataNoteItem itemData = null;
                        int nAllCount = noteConfig.m_lstData.m_lstAllItem.size();
                        for (int j = 0; j < nAllCount; j++) {
                            itemData = noteConfig.m_lstData.m_lstAllItem.get(j);
                            if (itemData.m_strType.compareTo(noteConfig.m_noteTypeMng.m_strRubbish) != 0) {
                                continue;
                            }
                            CheckBox chkNoteType = new CheckBox (noteListActivity.this);
                            if (itemData.m_strTitle.length() > 0)
                                chkNoteType.setText(itemData.m_strTitle);
                            else
                                chkNoteType.setText(itemData.m_strFirstLine);
                            chkNoteType.setChecked(false);
                            m_lstRubbish.add(itemData);
                            layItems.addView(chkNoteType);
                        }
                        if (layItems.getChildCount() <= 0) {
                            TextView tvText = new TextView(noteListActivity.this);
                            tvText.setText("没有垃圾笔记！");
                            layItems.addView(tvText);
                        }
                        return super.create();
                    }
                    public AlertDialog show() {
                        return super.show();
                    }
                };

        dlgNoteTDel.setIcon(R.drawable.notetype_del);
        dlgNoteTDel.setTitle("清理垃圾笔记");
        dlgNoteTDel.setView(noteDelView);
        dlgNoteTDel.setNeutralButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                m_sldList.scrollToPage (1);
            }
        });
        dlgNoteTDel.setNegativeButton("全部删除", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                int             nCount = m_lstRubbish.size();
                for (int i = 0; i < nCount; i++) {
                    dataNoteItem dataItem = m_lstRubbish.get(i);
                    for (int j = 0; j < dataItem.m_lstItem.size(); j++) {
                        dataNoteItem.dataContent dataContent = dataItem.m_lstItem.get(j);
                        if (dataContent.m_nType == noteConfig.m_nItemTypePict ||
                                dataContent.m_nType == noteConfig.m_nItemTypeAudo ||
                                dataContent.m_nType == noteConfig.m_nItemTypeVido) {
                            File filePic = new File (dataContent.m_strItem);
                            filePic.delete();
                        }
                    }
                    noteConfig.m_lstData.delNoteFile(dataItem.m_strFile);
                    File fileDel = new File (dataItem.m_strFile);
                    fileDel.delete();
                }
                m_sldList.scrollToPage (1);
                if (nCount > 0)
                    updateList();
            }
        });
        dlgNoteTDel.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                LinearLayout    layItems = (LinearLayout)noteDelView.findViewById(R.id.layItems);
                int             nCount = m_lstRubbish.size();
                CheckBox        chkNoteType = null;
                for (int i = 0; i < nCount; i++) {
                    chkNoteType = (CheckBox)layItems.getChildAt(i);
                    if (chkNoteType.isChecked()) {
                        dataNoteItem dataItem = m_lstRubbish.get(i);
                        for (int j = 0; j < dataItem.m_lstItem.size(); j++) {
                            dataNoteItem.dataContent dataContent = dataItem.m_lstItem.get(j);
                            if (dataContent.m_nType == noteConfig.m_nItemTypePict ||
                                    dataContent.m_nType == noteConfig.m_nItemTypeAudo ||
                                    dataContent.m_nType == noteConfig.m_nItemTypeVido) {
                                File filePic = new File (dataContent.m_strItem);
                                filePic.delete();
                            }
                        }
                        noteConfig.m_lstData.delNoteFile(dataItem.m_strFile);
                        File fileDel = new File (dataItem.m_strFile);
                        fileDel.delete();
                    }
                }
                m_sldList.scrollToPage (1);
                if (nCount > 0)
                    updateList();
            }
        });
        dlgNoteTDel.show();
    }

    private void chgNoteTypeDialog() {
        final View noteChgView = LayoutInflater.from(noteListActivity.this)
                .inflate(R.layout.note_type_modify,null);
        AlertDialog.Builder dlgNoteTDel =
                new AlertDialog.Builder(noteListActivity.this){
                    public AlertDialog create() {
                        return super.create();
                    }
                    public AlertDialog show() {
                        EditText edtType = (EditText)noteChgView.findViewById(R.id.edtOldType);
                        edtType.setText(noteConfig.m_noteTypeMng.getCurType());
                        edtType.setEnabled(false);
                        return super.show();
                    }
                };

        dlgNoteTDel.setIcon(R.drawable.notetype_del);
        dlgNoteTDel.setTitle("修改笔记类型名称");
        dlgNoteTDel.setView(noteChgView);
        dlgNoteTDel.setNeutralButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                m_sldList.scrollToPage (1);
            }
        });
        dlgNoteTDel.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String strOldType = ((EditText)noteChgView.findViewById(R.id.edtOldType)).getText().toString();
                String strNewType = ((EditText)noteChgView.findViewById(R.id.edtNewType)).getText().toString();
                if (noteConfig.m_noteTypeMng.changeType(strOldType, strNewType) < 0) {
                    return;
                }
                dataNoteItem itemData = null;
                int nAllCount = noteConfig.m_lstData.m_lstAllItem.size();
                for (int j = 0; j < nAllCount; j++) {
                    itemData = noteConfig.m_lstData.m_lstAllItem.get(j);
                    if (itemData.m_strType.compareTo(strOldType) == 0) {
                        itemData.m_strType = strNewType;
                        itemData.writeToFile();
                    }
                }
                m_sldList.scrollToPage (1);
                fillLeftList(true);
                updateList();
            }
        });
        dlgNoteTDel.show();
    }

    private void movNoteTypeDialog() {
        final View noteMovView = LayoutInflater.from(noteListActivity.this)
                .inflate(R.layout.note_type_move,null);
        AlertDialog.Builder dlgNoteTDel = new AlertDialog.Builder(noteListActivity.this){
            public AlertDialog create() {
                Spinner spnType = (Spinner)noteMovView.findViewById(R.id.spinNoteType);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        noteListActivity.this, R.layout.spn_note_type, noteConfig.m_noteTypeMng.getListName(false));
                spnType.setAdapter(adapter);
                return super.create();
            }
            public AlertDialog show() {
                return super.show();
            }
        };

        dlgNoteTDel.setIcon(R.drawable.notetype_del);
        dlgNoteTDel.setTitle("把选中笔记移到新的类型");
        dlgNoteTDel.setView(noteMovView);
        dlgNoteTDel.setNeutralButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                m_sldList.scrollToPage (1);
            }
        });
        dlgNoteTDel.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Spinner spnType = (Spinner)noteMovView.findViewById(R.id.spinNoteType);
                int     nSel = spnType.getSelectedItemPosition();
                String  strNoteType = (String)spnType.getAdapter().getItem(nSel);
                dataNoteItem    dataItem = null;
                int             nCount = noteConfig.m_lstData.getCount();
                for (int i = 0; i < nCount; i++) {
                    dataItem = (dataNoteItem)noteConfig.m_lstData.getItem(i);
                    if (dataItem.isSelect()) {
                        dataItem.m_strType = strNoteType;
                        dataItem.writeToFile();
                    }
                }
                m_sldList.scrollToPage (1);
                updateList();
            }
        });
        dlgNoteTDel.show();
    }

    private void showMsgDlg(String strTitle, String strMsg){
        final AlertDialog.Builder msgDialog = new AlertDialog.Builder(noteListActivity.this);
        msgDialog.setIcon(R.drawable.app_menu_icon);
        if (strTitle != null)
            msgDialog.setTitle(strTitle);
        if (strMsg != null)
            msgDialog.setMessage(strMsg);
        msgDialog.setPositiveButton("确定", null);
        msgDialog.show();
    }

    public void CheckWritePermission () {
        String[] permissions = new String[]{Manifest.permission.CAMERA,
                                            Manifest.permission.RECORD_AUDIO,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.ACCESS_COARSE_LOCATION};
        //检查权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 之前拒绝了权限，但没有点击 不再询问 这个时候让它继续请求权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                Toast.makeText(this, "用户曾拒绝打开相机权限", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, permissions, REQUEST_STORAGE);
            } else {
                //注册相机权限
                ActivityCompat.requestPermissions(this, permissions, REQUEST_STORAGE);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //成功
                    //Toast.makeText(this, "用户授权相机权限", Toast.LENGTH_SHORT).show();
                    m_lstView.postDelayed(()->updateList(), 500);
                } else {
                    // 勾选了不再询问
                    Toast.makeText(this, "用户拒绝相机权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void getTodayWeather () {
        String strURL = "https://www.tianqiapi.com/api/?version=v1";
        OkHttpUtils
                .get().url(strURL).id(101)
                .build().execute(new httpDataCallBack());
    }

    public class httpDataCallBack extends StringCallback {
        public void onError(Call call, Exception e, int id) {
        }

        public void onResponse(String response, int id) {
            if (id == 101) {
                JSONObject  jsnResult = JSON.parseObject(response);
                noteConfig.m_strCityName = jsnResult.getString("city");
                JSONArray   jsnData = jsnResult.getJSONArray("data");
                JSONObject  jsnToday = jsnData.getJSONObject(0);
                noteConfig.m_strWeather = jsnToday.getString("wea");
                String strTemp1 = jsnToday.getString("tem2");
                String strTemp2 = jsnToday.getString("tem1");
                noteConfig.m_strWeather += " " + strTemp1 + "-" + strTemp2;
            }
        }
    }

}
