package com.wyhwl.bangnote;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
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
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;

public class noteListActivity extends AppCompatActivity
                              implements noteListSlider.switchListener,
                                            AdapterView.OnItemClickListener,
                                            AdapterView.OnItemLongClickListener {
    public static final int     REQUEST_STORAGE     = 1;
    public static final int     REQUEST_CAMERA      = 2;
    private noteListListView    m_lstView = null;
    private noteListSlider      m_sldList = null;
    private noteListAdapter     m_lstData = null;

    private ImageButton         m_btnNewNote = null;

    private ListView            m_lstViewLeft = null;
    private ListView            m_lstViewRight = null;
    private String              m_strNewNote = "新建笔记";

    public  ArrayList<dataNoteItem>  m_lstRubbish = new ArrayList<dataNoteItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setLogo(R.drawable.note_icon);
        //actionBar.setDisplayShowHomeEnabled(true);
        //actionBar.setDisplayUseLogoEnabled(true);

        //actionBar.setDisplayHomeAsUpEnabled(true);

        CheckWritePermission();
        noteConfig.initConfig(this);

        initViews();
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onStop() {
        super.onStop();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            noteConfig.m_nShowSecurity = 0;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void initViews () {
        m_sldList = (noteListSlider) findViewById(R.id.sldList);
        m_sldList.setSwitchListener(this);

        m_lstData = new noteListAdapter(this);
        m_lstView = (noteListListView) findViewById(R.id.vwNoteList);
        m_lstView.setOnItemClickListener(this);
        m_lstView.setOnItemLongClickListener(this);

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

        m_lstView.postDelayed(()->updateList(), 20);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == (View)m_lstView) {
            dataNoteItem noteItem = m_lstData.getNoteItem(position);
            Intent intent = new Intent(noteListActivity.this, noteViewActivity.class);
            intent.setData(Uri.parse(noteItem.m_strFile));
            startActivityForResult(intent, 1);
        } else if (parent == (View)m_lstViewLeft) {
            TextView tvType = (TextView) view.findViewById(R.id.name);
            String strType = tvType.getText().toString();
            if (strType.compareTo(m_strNewNote) == 0) {
                Intent intent = new Intent(noteListActivity.this, noteEditActivity.class);
                startActivityForResult(intent, 1);
            } else {
                noteConfig.m_noteTypeMng.setCurType(strType);
                fillLeftList (false);
                updateList();
            }
        }else if (parent == (View)m_lstViewRight) {
            if (position == 0)          // new note type
                addNoteTypeDialog();
            else if (position == 1)     // delete note type
                mngNoteTypeDialog();
            else if (position == 2)     // delete note item
                deleteSelectedNote();
            else if (position == 3)  {  // clean rubbish
                delNoteItemDialog();
            } else if (position == 4) { // setting

            } else if (position == 5) { // exit
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
                new String[]{"name","img"}, new int[]{R.id.name, R.id.img});
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
        mapItem = new HashMap<String, Object>();
        mapItem.put("name", "增加类型");
        mapItem.put("img", R.drawable.notetype_new);
        listItem.add(mapItem);

        mapItem = new HashMap<String, Object>();
        mapItem.put("name", "管理类型");
        mapItem.put("img", R.drawable.notetype_del);
        listItem.add(mapItem);

        mapItem = new HashMap<String, Object>();
        mapItem.put("name", "删除笔记");
        mapItem.put("img", R.drawable.note_delete);
        listItem.add(mapItem);

        mapItem = new HashMap<String, Object>();
        mapItem.put("name", "清除垃圾");
        mapItem.put("img", R.drawable.lajitong);
        listItem.add(mapItem);

        mapItem = new HashMap<String, Object>();
        mapItem.put("name", "笔记设置");
        mapItem.put("img", R.drawable.note_setting);
        listItem.add(mapItem);

        mapItem = new HashMap<String, Object>();
        mapItem.put("name", "退出笔记");
        mapItem.put("img", R.drawable.note_exit);
        listItem.add(mapItem);

        SimpleAdapter adapter = new SimpleAdapter(this, listItem, R.layout.menu_list_item,
                new String[]{"name","img"}, new int[]{R.id.name, R.id.img});
        m_lstViewRight.setAdapter(adapter);
    }

    protected void updateList () {
        m_lstData.updateNoteItem();
        m_lstView.setAdapter(m_lstData);
        m_lstView.invalidate();
        m_sldList.scrollToPage (1);
    }

    protected void deleteSelectedNote () {
        dataNoteItem    dataItem = null;
        int             nCount = m_lstData.getCount();
        for (int i = 0; i < nCount; i++) {
            dataItem = (dataNoteItem)m_lstData.getItem(i);
            if (dataItem.isSelect()) {
                dataItem.m_strType = noteConfig.m_noteTypeMng.m_strRubbish;
                dataItem.writeToFile();
            }
        }
        m_lstView.postDelayed(()->updateList(), 200);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notelist, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //Toast.makeText(MainActivity.this,"提交文本："+s,Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //Toast.makeText(MainActivity.this,"当前文本："+s,Toast.LENGTH_SHORT).show();
                return false;
            }
        });
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

            case R.id.menu_deletenote:
                deleteSelectedNote ();
                break;

            case R.id.menu_newnote:
                Intent intent = new Intent(noteListActivity.this, noteEditActivity.class);
                startActivityForResult(intent, 1);
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
        if (noteConfig.m_bNoteModified)
            m_lstView.postDelayed(()->updateList(), 1000);
    }

    private void addNoteTypeDialog() {
        final View noteTypeView = LayoutInflater.from(noteListActivity.this)
                .inflate(R.layout.note_type_input,null);
        AlertDialog.Builder dlgNoteType =
                new AlertDialog.Builder(noteListActivity.this){
                    public AlertDialog create() {
                        return super.create();
                    }
                    public AlertDialog show() {
                        RadioButton rbnNormal = (RadioButton)noteTypeView.findViewById(R.id.noteTypeNormal);
                        rbnNormal.setChecked(true);
                        RadioButton rbnSecurity = (RadioButton)noteTypeView.findViewById(R.id.noteTypeSecurity);
                        rbnSecurity.setChecked(false);
                        if (noteConfig.m_nShowSecurity == 0) {
                            noteTypeView.findViewById(R.id.laySecurity).setVisibility(View.INVISIBLE);
                        }
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

    private void mngNoteTypeDialog() {
        final View noteDelView = LayoutInflater.from(noteListActivity.this)
                .inflate(R.layout.note_type_delete,null);
        AlertDialog.Builder dlgNoteTDel =
                new AlertDialog.Builder(noteListActivity.this){
                    public AlertDialog create() {
                        return super.create();
                    }
                    public AlertDialog show() {
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
                            int nAllCount = m_lstData.m_lstAllItem.size();
                            for (int j = 0; j < nAllCount; j++) {
                                itemData = m_lstData.m_lstAllItem.get(j);
                                if (itemData.m_strType.compareTo(itemType.m_strName) == 0) {
                                    chkNoteType.setEnabled(false);
                                    break;
                                }
                            }
                        }
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
                        return super.create();
                    }
                    public AlertDialog show() {
                        m_lstRubbish.clear();
                        LinearLayout layItems = (LinearLayout)noteDelView.findViewById(R.id.layItems);
                        dataNoteItem itemData = null;
                        int nAllCount = m_lstData.m_lstAllItem.size();
                        for (int j = 0; j < nAllCount; j++) {
                            itemData = m_lstData.m_lstAllItem.get(j);
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
        dlgNoteTDel.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                LinearLayout    layItems = (LinearLayout)noteDelView.findViewById(R.id.layItems);
                int             nCount = layItems.getChildCount();
                CheckBox        chkNoteType = null;
                for (int i = 0; i < nCount; i++) {
                    chkNoteType = (CheckBox)layItems.getChildAt(i);
                    if (chkNoteType.isChecked()) {
                        dataNoteItem dataItem = m_lstRubbish.get(i);
                        for (int j = 0; j < dataItem.m_lstItem.size(); j++) {
                            dataNoteItem.dataContent dataContent = dataItem.m_lstItem.get(j);
                            if (dataContent.m_nType == dataNoteItem.m_nItemTypePict) {
                                File filePic = new File (dataContent.m_strItem);
                                filePic.delete();
                            }
                        }
                        File fileDel = new File (dataItem.m_strFile);
                        fileDel.delete();
                    }
                }
                m_sldList.scrollToPage (1);
                updateList();
            }
        });
        dlgNoteTDel.show();
    }

    public void CheckWritePermission () {
        String[] permissions = new String[]{Manifest.permission.CAMERA,
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
                    m_lstView.postDelayed(()->updateList(), 100);
                } else {
                    // 勾选了不再询问
                    Toast.makeText(this, "用户拒绝相机权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}
