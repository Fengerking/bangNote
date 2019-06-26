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

import com.wyhwl.bangnote.base.*;
import com.wyhwl.bangnote.view.*;
import com.wyhwl.bangnote.lock.LockActivity;
import com.wyhwl.bangnote.lock.LockSettingActivity;

public class noteListActivity extends noteBaseActivity
                              implements noteListSlider.switchListener,
                                            AdapterView.OnItemClickListener,
                                            AdapterView.OnItemLongClickListener,
                                            View.OnClickListener {
    public static final int     REQUEST_STORAGE         = 1;

    public static final int     ACTIVITY_BACKUP         = 1;
    public static final int     ACTIVITY_NOTEVIEW       = 2;
    public static final int     ACTIVITY_NOTEEDIT       = 3;
    public static final int     ACTIVITY_NOTESETKEY     = 4;
    public static final int     ACTIVITY_NOTEUNLOCK     = 5;
    public static final int     ACTIVITY_NOTEABOUT      = 6;
    public static final int     ACTIVITY_CALENDAR       = 7;

    private ListView            m_lstView = null;
    private noteListSlider      m_sldList = null;

    private ImageButton         m_btnNewNote = null;
    private ImageButton         m_btnSortUp = null;
    private ImageButton         m_btnSortDown = null;

    private LinearLayout        m_layToolBarList = null;
    private RelativeLayout      m_layToolBarSearch = null;
    private ListView            m_lstViewLeft = null;
    private ListView            m_lstViewRight = null;
    private String              m_strNewNote = "新建笔记";
    private String              m_strCalendar = "日历笔记";

    public  ArrayList<dataNoteItem>  m_lstRubbish = new ArrayList<dataNoteItem>();

    private noteBaseInfo        m_noteInfo = null;
    private boolean             m_bSelectMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        m_noteInfo = new noteBaseInfo(this);
        initViews();

        CheckWritePermission();
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
        noteConfig.m_lstData.stopUpdate();
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
        ((ImageButton)findViewById(R.id.imbHome)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbNewNote)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbDelNote)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbSearchNote)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbSearchAll)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbSearchSelect)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.imbCalendar)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.appBack)).setOnClickListener(this);

        ((ImageButton)findViewById(R.id.imbCalendar)).setVisibility(View.INVISIBLE);

        m_btnSortUp = (ImageButton)findViewById(R.id.imbSortUp);
        m_btnSortDown = (ImageButton)findViewById(R.id.imbSortDown);
        m_btnSortUp.setOnClickListener(this);
        m_btnSortDown.setOnClickListener(this);
        m_btnSortDown.setVisibility(View.INVISIBLE);

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
                startActivityForResult(intent, ACTIVITY_NOTEEDIT);
            }
        });

        m_lstView.postDelayed(()->m_noteInfo.getTodayWeather(), 5000);
    }

    public void onClick(View v) {
        Intent intent = null;
        int nID = v.getId();
        switch (nID) {
            case R.id.imbHome:
                intent = new Intent(noteListActivity.this, noteAboutActivity.class);
                startActivityForResult(intent, ACTIVITY_NOTEABOUT);
                break;

            case R.id.imbNewNote:
                intent = new Intent(noteListActivity.this, noteEditActivity.class);
                startActivityForResult(intent, ACTIVITY_NOTEEDIT);
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
                m_layToolBarList.setVisibility(View.VISIBLE);
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

            case R.id.imbCalendar:
                intent = new Intent(noteListActivity.this, noteCalendarActivity.class);
                startActivityForResult(intent, ACTIVITY_CALENDAR);
                break;

            case R.id.imbSortUp:
                m_btnSortDown.setVisibility(View.VISIBLE);
                m_btnSortUp.setVisibility(View.INVISIBLE);
                noteConfig.m_lstData.sortData(1);
                m_lstView.setAdapter(noteConfig.m_lstData);
                m_lstView.invalidate();
                break;

            case R.id.imbSortDown:
                m_btnSortDown.setVisibility(View.INVISIBLE);
                m_btnSortUp.setVisibility(View.VISIBLE);
                noteConfig.m_lstData.sortData(0);
                m_lstView.setAdapter(noteConfig.m_lstData);
                m_lstView.invalidate();
                break;
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == (View)m_lstView) {
            if (m_bSelectMode) {
                noteConfig.m_lstData.getNoteItem(position).setSelect();
                view.invalidate();
                if (noteConfig.m_lstData.getSelectNum() <= 0)
                    m_bSelectMode = false;
                return;
            }
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

            startActivityForResult(intent, ACTIVITY_NOTEVIEW);
        } else if (parent == (View)m_lstViewLeft) {
            TextView tvType = (TextView) view.findViewById(R.id.name);
            String strType = tvType.getText().toString();
            if (strType.compareTo(m_strNewNote) == 0) {
                Intent intent = new Intent(noteListActivity.this, noteEditActivity.class);
                startActivityForResult(intent, ACTIVITY_NOTEEDIT);
            } else if (strType.compareTo(m_strCalendar) == 0) {
                Intent intent = new Intent(noteListActivity.this, noteCalendarActivity.class);
                startActivityForResult(intent, ACTIVITY_CALENDAR);
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
                    showMsgDlg ("备份笔记成功", null, false);
                else
                    showMsgDlg ("备份笔记失败", null, false);
                m_sldList.scrollToPage (1);
            } else if (strCommand.compareTo("恢复备份") == 0) {
                noteBackupRestore noteBackup = new noteBackupRestore(this);
                if (noteBackup.restoreNote () > 0) {
                    noteConfig.m_noteTypeMng.readFromFile();
                    noteConfig.m_lstData.fillFileList(noteConfig.m_strNotePath);
                    fillLeftList(false);
                    updateList();
                    showMsgDlg("恢复笔记成功", null, false);
                } else {
                    showMsgDlg("恢复笔记失败", null, false);
                }
                m_sldList.scrollToPage (1);
            } else if (strCommand.compareTo("备份管理") == 0) {
                Intent intent = new Intent(noteListActivity.this, noteBackupActivity.class);
                startActivityForResult(intent, ACTIVITY_BACKUP);
                m_sldList.scrollToPage (1);
            } else if (strCommand.compareTo("激活密记") == 0) {
                String strKeyFile = noteConfig.getNoteLockKeyFile();
                noteApplication.getInstance().readLockKey(strKeyFile);
                if (noteApplication.getInstance().m_strLockKey.length() > 1) {
                    Intent intent = new Intent(noteListActivity.this, LockActivity.class);
                    startActivityForResult(intent, ACTIVITY_NOTEUNLOCK);
                } else {
                    if (noteConfig.m_noteTypeMng.haveSecurityType()) {
                        if (noteConfig.m_nShowSecurity == 0) {
                            m_sldList.scrollToPage (1);
                            return;
                        }
                    }
                    Intent intent = new Intent(noteListActivity.this, LockSettingActivity.class);
                    startActivityForResult(intent, ACTIVITY_NOTESETKEY);
                }
                m_sldList.scrollToPage (1);
            } else if (strCommand.compareTo("笔记设置") == 0) {
                Intent intent = new Intent(noteListActivity.this, noteAboutActivity.class);
                startActivityForResult(intent, ACTIVITY_NOTEABOUT);
                m_sldList.scrollToPage (1);
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
            if (noteView.getDataList().isSelect()) {
                m_bSelectMode = true;
            } else {
                if (noteConfig.m_lstData.getSelectNum() <= 0)
                    m_bSelectMode = false;
            }
        } else if (parent == (View)m_lstViewLeft) {
            TextView tvType = (TextView)view.findViewById(R.id.name);
            String strType = tvType.getText().toString();
            if (strType.compareTo(noteConfig.m_noteTypeMng.m_strTotal) == 0) {
                long lTimeNow = System.currentTimeMillis();
                if (lTimeNow - noteConfig.m_nMoveLastTime < 5000 && noteConfig.m_nMoveCount == noteConfig.m_nMoveNeedTime) {
                    noteConfig.m_nShowSecurity = 1;
                    fillLeftList(false);

                    String strKeyFile = noteConfig.getNoteLockKeyFile();
                    File fileDel = new File (strKeyFile);
                    if (fileDel.exists())
                        fileDel.delete();
                    noteApplication.getInstance().m_strLockKey = "";

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
            mapItem.put("name", m_strCalendar);
            mapItem.put("img", R.drawable.note_search_calendar);
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
        //addRightCommand (listItem, "删除笔记", R.drawable.note_delete);
        addRightCommand (listItem, "清除垃圾", R.drawable.lajitong);
        addRightCommand (listItem, "全部选中", R.drawable.notetype_selall);
        addRightCommand (listItem, "清除选择", R.drawable.notetype_selnone);
        //addRightCommand (listItem, "备份笔记", R.drawable.note_backup);
        //addRightCommand (listItem, "恢复备份", R.drawable.note_restore);
        addRightCommand (listItem, "备份管理", R.drawable.note_send);
        addRightCommand (listItem, "激活密记", R.drawable.note_lock);
        addRightCommand (listItem, "笔记设置", R.drawable.note_setting);
        //addRightCommand (listItem, "退出笔记", R.drawable.note_exit);

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
                    delDataItemContent (dataItem, false);
                } else {
                    dataItem.m_strType = noteConfig.m_noteTypeMng.m_strRubbish;
                    dataItem.writeToFile();
                }
            }
        }

        for (int i = 0; i < lstDelFiles.size (); i++) {
            String strDelFile = lstDelFiles.get(i);
            noteConfig.m_lstData.delNoteFile(strDelFile);
            File fileDel = new File (strDelFile);
            fileDel.delete();
        }
        updateList();
    }

    private void delDataItemContent (dataNoteItem dataItem, boolean bDelFile) {
        for (int j = 0; j < dataItem.m_lstItem.size(); j++) {
            dataNoteItem.dataContent dataContent = dataItem.m_lstItem.get(j);
            if (dataContent.m_nType == noteConfig.m_nItemTypePict ||
                    dataContent.m_nType == noteConfig.m_nItemTypeAudo ||
                    dataContent.m_nType == noteConfig.m_nItemTypeMusc ||
                    dataContent.m_nType == noteConfig.m_nItemTypeVido) {
                File fileDel = new File (dataContent.m_strItem);
                if (fileDel.exists())
                    fileDel.delete();
                if (dataContent.m_nType == noteConfig.m_nItemTypeVido) {
                    String strThumbFile = dataContent.m_strItem.substring(0, dataContent.m_strItem.length()-3);
                    strThumbFile += "tmb";
                    File fileThumb = new File(strThumbFile);
                    if (fileThumb.exists()) {
                        fileThumb.delete();
                    }
                }
            }
        }
        if (bDelFile) {
            noteConfig.m_lstData.delNoteFile(dataItem.m_strFile);
            File fileDel = new File(dataItem.m_strFile);
            fileDel.delete();
        }
    }

    protected void selectAllItems (boolean bSelect) {
        dataNoteItem    dataItem = null;
        int             nCount = noteConfig.m_lstData.getCount();
        for (int i = 0; i < nCount; i++) {
            dataItem = (dataNoteItem)noteConfig.m_lstData.getItem(i);
            if (bSelect) {
                if (!dataItem.isSelect())
                    dataItem.setSelect();
                m_bSelectMode = true;
            } else {
                if (dataItem.isSelect())
                    dataItem.setSelect();
                m_bSelectMode = false;
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
        if (requestCode == ACTIVITY_BACKUP) {
            updateList();
        } else if (requestCode == ACTIVITY_NOTESETKEY) {
            if (noteApplication.getInstance().m_strLockKey.length() < 1)
                return;
            String strKeyFile = noteConfig.getNoteLockKeyFile();
            noteApplication.getInstance().writeLockKey(strKeyFile);
            noteConfig.m_nShowSecurity = 1;
            fillLeftList(false);
            showMsgDlg("密记已经激活", "先添加密记笔记类型", false);
         } else if (requestCode == ACTIVITY_NOTEUNLOCK) {
            if (!noteApplication.getInstance().m_isUnlock)
                return;
            noteConfig.m_nShowSecurity = 1;
            fillLeftList(false);
            Toast.makeText(this,"密记已经激活！",Toast.LENGTH_SHORT).show();
        }
    }

    protected void onDlgCreate () {
        switch (m_dlgParam.nType) {
            case DLG_NOTETYPE_NEW:
                RadioButton rbnNormal = (RadioButton)m_dlgParam.dlgView.findViewById(R.id.noteTypeNormal);
                rbnNormal.setChecked(true);
                RadioButton rbnSecurity = (RadioButton)m_dlgParam.dlgView.findViewById(R.id.noteTypeSecurity);
                rbnSecurity.setChecked(false);
                if (noteConfig.m_nShowSecurity == 0) {
                    m_dlgParam.dlgView.findViewById(R.id.laySecurity).setVisibility(View.INVISIBLE);
                }
                break;

            case DLG_NOTETYPE_DEL:
                LinearLayout layItems = (LinearLayout)m_dlgParam.dlgView.findViewById(R.id.layItems);
                noteTypeMng.noteTypeItem    itemType = null;
                int                         nCount = noteConfig.m_noteTypeMng.getCount();
                for (int i = 0; i < nCount; i++) {
                    itemType = noteConfig.m_noteTypeMng.getItem(i);
                    if (noteConfig.m_nShowSecurity == 0 && itemType.m_nLevel >= 10)
                        continue;
                    if (itemType.m_nLevel < 0)
                        continue;

                    CheckBox chkNoteType = new CheckBox(noteListActivity.this);
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
                break;

            case DLG_NOTETYPE_CHG:
                EditText edtType = (EditText)m_dlgParam.dlgView.findViewById(R.id.edtOldType);
                edtType.setText(noteConfig.m_noteTypeMng.getCurType());
                edtType.setEnabled(false);
                break;

            case DLG_NOTETYPE_MOV:
                Spinner spnType = (Spinner)m_dlgParam.dlgView.findViewById(R.id.spinNoteType);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        noteListActivity.this, R.layout.spn_note_type, noteConfig.m_noteTypeMng.getListName(false));
                spnType.setAdapter(adapter);
                break;

            case DLG_NOTEITEM_DEL:
                m_lstRubbish.clear();
                layItems = (LinearLayout)m_dlgParam.dlgView.findViewById(R.id.layItems);
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
                    else if (itemData.m_strFirstLine.length() > 0)
                        chkNoteType.setText(itemData.m_strFirstLine);
                    else
                        chkNoteType.setText("没有标题");
                    chkNoteType.setChecked(false);
                    m_lstRubbish.add(itemData);
                    layItems.addView(chkNoteType);
                }
                if (layItems.getChildCount() <= 0) {
                    TextView tvText = new TextView(noteListActivity.this);
                    tvText.setText("没有垃圾笔记！");
                    layItems.addView(tvText);
                }
                break;
        }
    }

    protected void onDlgShow () {
    }

    protected void onDlgOK () {
        String strType = "";
        switch (m_dlgParam.nType) {
            case DLG_NOTETYPE_NEW:
                strType = ((EditText)m_dlgParam.dlgView.findViewById(R.id.noteType)).getText().toString();
                RadioButton rtbSecurity = (RadioButton)m_dlgParam.dlgView.findViewById(R.id.noteTypeSecurity);
                if (strType.length() > 0) {
                    int nRC = 0;
                    if (rtbSecurity.isChecked())
                        nRC = noteConfig.m_noteTypeMng.addType(strType, 10);
                    else
                        nRC = noteConfig.m_noteTypeMng.addType(strType, 0);
                    if (nRC < 0)
                        showMsgDlg("增加笔记类型", "操作失败。", false);
                    else {
                        m_sldList.scrollToPage (1);
                        fillLeftList(true);
                    }
                }
                break;

            case DLG_NOTETYPE_DEL:
                LinearLayout    layItems = (LinearLayout)m_dlgParam.dlgView.findViewById(R.id.layItems);
                int             nCount = layItems.getChildCount();
                CheckBox        chkNoteType = null;
                for (int i = 0; i < nCount; i++) {
                    chkNoteType = (CheckBox)layItems.getChildAt(i);
                    if (chkNoteType.isChecked()) {
                        strType = chkNoteType.getText().toString();
                        noteConfig.m_noteTypeMng.delType(strType);
                    }
                }
                m_sldList.scrollToPage (1);
                fillLeftList(true);
                break;

            case DLG_NOTETYPE_CHG:
                String strOldType = ((EditText)m_dlgParam.dlgView.findViewById(R.id.edtOldType)).getText().toString();
                String strNewType = ((EditText)m_dlgParam.dlgView.findViewById(R.id.edtNewType)).getText().toString();
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
                break;

            case DLG_NOTETYPE_MOV:
                Spinner spnType = (Spinner)m_dlgParam.dlgView.findViewById(R.id.spinNoteType);
                int     nSel = spnType.getSelectedItemPosition();
                String  strNoteType = (String)spnType.getAdapter().getItem(nSel);
                dataNoteItem    dataItem = null;
                nCount = noteConfig.m_lstData.getCount();
                for (int i = 0; i < nCount; i++) {
                    dataItem = (dataNoteItem)noteConfig.m_lstData.getItem(i);
                    if (dataItem.isSelect()) {
                        dataItem.m_strType = strNoteType;
                        dataItem.writeToFile();
                    }
                }
                m_sldList.scrollToPage (1);
                updateList();
                break;

            case DLG_NOTEITEM_DEL:
                layItems = (LinearLayout)m_dlgParam.dlgView.findViewById(R.id.layItems);
                nCount = m_lstRubbish.size();
                for (int i = 0; i < nCount; i++) {
                    chkNoteType = (CheckBox)layItems.getChildAt(i);
                    if (chkNoteType.isChecked()) {
                        dataItem = m_lstRubbish.get(i);
                        delDataItemContent (dataItem, true);
                    }
                }
                m_sldList.scrollToPage (1);
                if (nCount > 0)
                    updateList();
                break;

        }
        m_sldList.scrollToPage (1);
    }
    protected void onDlgCnacel () {
        m_sldList.scrollToPage (1);
    }
    protected void onDlgOther () {
        if (m_dlgParam.nType == DLG_NOTEITEM_DEL) {
            int nCount = m_lstRubbish.size();
            for (int i = 0; i < nCount; i++) {
                dataNoteItem dataItem = m_lstRubbish.get(i);
                delDataItemContent (dataItem, true);
            }
            m_sldList.scrollToPage (1);
            if (nCount > 0)
                updateList();
        }
    }

    private void addNoteTypeDialog() {
        initDlgParam();
        m_dlgParam.nType = DLG_NOTETYPE_NEW;
        m_dlgParam.nIcon = R.drawable.notetype_aa;
        m_dlgParam.nView = R.layout.note_type_input;
        m_dlgParam.strTitle = "输入笔记类型名称：";
        m_dlgParam.strCancel = "取消";
        m_dlgParam.strOK = "确定";
        showNoteDialog ();
    }

    private void delNoteTypeDialog() {
        initDlgParam();
        m_dlgParam.nType = DLG_NOTETYPE_DEL;
        m_dlgParam.nIcon = R.drawable.notetype_del;
        m_dlgParam.nView = R.layout.note_type_delete;
        m_dlgParam.strTitle = "删除笔记类型：";
        m_dlgParam.strCancel = "取消";
        m_dlgParam.strOK = "确定";
        showNoteDialog ();
    }

    private void chgNoteTypeDialog() {
        initDlgParam();
        m_dlgParam.nType = DLG_NOTETYPE_CHG;
        m_dlgParam.nIcon = R.drawable.notetype_del;
        m_dlgParam.nView = R.layout.note_type_modify;
        m_dlgParam.strTitle = "修改笔记类型名称：";
        m_dlgParam.strCancel = "取消";
        m_dlgParam.strOK = "确定";
        showNoteDialog ();
    }

    private void movNoteTypeDialog() {
        initDlgParam();
        m_dlgParam.nType = DLG_NOTETYPE_MOV;
        m_dlgParam.nIcon = R.drawable.notetype_del;
        m_dlgParam.nView = R.layout.note_type_move;
        m_dlgParam.strTitle = "把选中笔记移到新的类型";
        m_dlgParam.strCancel = "取消";
        m_dlgParam.strOK = "确定";
        showNoteDialog ();
    }

    private void delNoteItemDialog() {
        initDlgParam();
        m_dlgParam.nType = DLG_NOTEITEM_DEL;
        m_dlgParam.nIcon = R.drawable.notetype_del;
        m_dlgParam.nView = R.layout.note_type_delete;
        m_dlgParam.strTitle = "清理垃圾笔记";
        m_dlgParam.strCancel = "取消";
        m_dlgParam.strOK = "确定";
        m_dlgParam.strOther = "全部删除";
        showNoteDialog ();
    }

    public void CheckWritePermission () {
        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
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
                    noteConfig.m_noteTypeMng.readFromFile();
                    noteConfig.m_lstData.fillFileList(noteConfig.m_strNotePath);
                    fillLeftList(false);
                    updateList();
                } else {
                    // 勾选了不再询问
                    Toast.makeText(this, "用户拒绝相机权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}
