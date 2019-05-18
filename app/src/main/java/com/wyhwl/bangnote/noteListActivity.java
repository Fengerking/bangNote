package com.wyhwl.bangnote;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.net.Uri;
import android.widget.ImageButton;

import java.lang.reflect.Method;
import java.io.File;


public class noteListActivity extends AppCompatActivity
                              implements noteListSlider.switchListener {

    private noteListListView        m_lstView = null;
    private noteListSlider          m_sldList = null;
    private noteListAdapter         m_lstData = null;

    private ImageButton             m_btnNewNote = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        noteConfig.initConfig(this);

        m_sldList = (noteListSlider)findViewById(R.id.sldList);
        m_sldList.setSwitchListener(this);

        m_lstData = new noteListAdapter(this);
        m_lstView = (noteListListView)findViewById(R.id.vwNoteList);
        m_lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dataNoteItem noteItem = m_lstData.getNoteItem(position);
                Intent intent = new Intent(noteListActivity.this, noteViewActivity.class);
                intent.setData(Uri.parse(noteItem.m_strFile));
                startActivity(intent);
            }
        });

        m_lstView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                noteListItemView noteView = (noteListItemView)view;
                noteView.setSelect ();
                return true;
            }
        });
        m_btnNewNote = (ImageButton)findViewById(R.id.btnNewNote);
        m_btnNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(noteListActivity.this, noteEditActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void onResume () {
        super.onResume();
        m_lstData.updateNoteItem();
        m_lstView.setAdapter(m_lstData);
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
                int nCount = m_lstView.getCount();
                for (int i = 0; i < nCount; i++) {
                    noteListItemView noteView = (noteListItemView)m_lstView.getChildAt(i);
                    if (noteView.isSelect()) {
                        File file = new File(noteView.getDataList().m_strFile);
                        file.delete();
                    }
                }
                m_lstData.updateNoteItem();
                m_lstView.setAdapter(m_lstData);
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

    public int onSwitchStart (int nView){
        return 0;
    }

    public int OnSwitchEnd (int nView) {
        return 0;
    }

    public int onMotionEvent (MotionEvent ev) {
        m_lstView.onTouchEvent(ev);
        return 0;
    }
}
