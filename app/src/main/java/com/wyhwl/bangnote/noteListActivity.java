package com.wyhwl.bangnote;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.net.Uri;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.EditText;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class noteListActivity extends AppCompatActivity
                              implements noteListSlider.switchListener,
                                            AdapterView.OnItemClickListener,
                                            AdapterView.OnItemLongClickListener {

    private noteListListView    m_lstView = null;
    private noteListSlider      m_sldList = null;
    private noteListAdapter     m_lstData = null;

    private ImageButton         m_btnNewNote = null;

    private ListView            m_lstViewLeft = null;
    private ListView            m_lstViewRight = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        noteConfig.CheckWritePermission(this, true);
        noteConfig.initConfig(this);

        initViews();
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onStop() {
        super.onStop();
        noteConfig.m_noteTypeMng.writeToFile();
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
        fillLeftList();
        m_lstViewRight = (ListView)findViewById(R.id.lstNoteTypeMng);
        m_lstViewRight.setOnItemClickListener(this);
        fillRightList();

        m_btnNewNote = (ImageButton) findViewById(R.id.btnNewNote);
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
        } else if (parent == (View)m_lstViewRight) {
            if (position == 0)
                addNoteTypeDialog();
            else if (position == 1)
                addNoteTypeDialog();
            else
                addNoteTypeDialog();

        } else if (parent == (View)m_lstViewLeft) {
            TextView tvType = (TextView)view.findViewById(R.id.name);
            String strType = tvType.getText().toString();
            noteConfig.m_noteTypeMng.setCurType(strType);
            updateList();
        }
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        noteListItemView noteView = (noteListItemView) view;
        noteView.setSelect();
        return true;
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
        mapItem.put("name", "垃圾箱");
        mapItem.put("img", R.drawable.lajitong);
        listItem.add(mapItem);
        SimpleAdapter adapter = new SimpleAdapter(this, listItem, R.layout.menu_list_item,
                new String[]{"name","img"}, new int[]{R.id.name, R.id.img});
        m_lstViewRight.setAdapter(adapter);
    }

    protected void fillLeftList () {
        ArrayList<Map<String, Object>> listItem = new ArrayList<Map<String, Object>>();
        HashMap<String, Object> mapItem;

        noteTypeMng.noteTypeItem itemType;
        int nCount = noteConfig.m_noteTypeMng.getCount();
        for (int i = 0; i < nCount; i++) {
            itemType = noteConfig.m_noteTypeMng.getItem(i);
            if (itemType.m_strName.compareTo(noteConfig.m_noteTypeMng.m_strLaji) == 0)
                continue;
            if (itemType.m_nLevel >= 10 && noteConfig.m_nShowSecurity == 0)
                continue;
            mapItem = new HashMap<String, Object>();
            mapItem.put("name", itemType.m_strName);
            mapItem.put("img", itemType.m_nImage);
            listItem.add(mapItem);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, listItem, R.layout.menu_list_item,
                new String[]{"name","img"}, new int[]{R.id.name, R.id.img});
        m_lstViewLeft.setAdapter(adapter);
    }

    protected void updateList () {
        m_lstData.updateNoteItem();
        m_lstView.setAdapter(m_lstData);
        m_lstView.invalidate();
        m_sldList.scrollToPage (1);
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
                addNoteTypeDialog ();
              /*
                int nCount = m_lstView.getCount();
                for (int i = 0; i < nCount; i++) {
                    noteListItemView noteView = (noteListItemView) m_lstView.getChildAt(i);
                    if (noteView.isSelect()) {
                        File file = new File(noteView.getDataList().m_strFile);
                        file.delete();
                    }
                }
                m_lstView.postDelayed(()->updateList(), 200);
                */
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
                    }
                });
        dlgNoteType.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String strType = ((EditText)noteTypeView.findViewById(R.id.noteType)).getText().toString();
                        RadioButton rtbSecurity = (RadioButton)noteTypeView.findViewById(R.id.noteTypeSecurity);
                        int nRC = 0;
                        if (rtbSecurity.isChecked())
                            nRC = noteConfig.m_noteTypeMng.addType(strType, 0);
                        else
                            nRC = noteConfig.m_noteTypeMng.addType(strType, 10);
                        if (nRC < 0)
                            Toast.makeText(noteListActivity.this, "增加笔记类型失败了！", Toast.LENGTH_SHORT).show();
                        fillLeftList();
                    }
                });
        dlgNoteType.show();
    }
}
