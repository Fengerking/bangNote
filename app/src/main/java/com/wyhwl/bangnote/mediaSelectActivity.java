package com.wyhwl.bangnote;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.wyhwl.bangnote.base.mediaSelectAdapter;
import com.wyhwl.bangnote.base.mediaSelectAdapter.mediaItem;
import com.wyhwl.bangnote.view.mediaSelectItemView;

import java.io.File;

public class mediaSelectActivity extends AppCompatActivity
                                    implements View.OnClickListener,
                                        AdapterView.OnItemClickListener,
                                            AdapterView.OnItemLongClickListener {
    private GridView                m_grdMedia = null;
    private mediaSelectAdapter      m_mediaAdpater = null;

    private TextView                m_txtPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_select);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        initViews();
    }

    protected void onStop () {
        m_mediaAdpater.setFolder(null);
        super.onStop();
    }

    private void initViews() {
        ((ImageButton) findViewById(R.id.imbBack)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.imbSelect)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.imbSortNameDown)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.imbSortNameUp)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.imbSortTimeDown)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.imbSortTimeUp)).setOnClickListener(this);
        m_txtPath = (TextView)findViewById(R.id.txtPath);

        m_grdMedia = (GridView) findViewById(R.id.grdMedia);
        m_grdMedia.setOnItemClickListener(this);
        m_grdMedia.setOnItemLongClickListener(this);

        m_mediaAdpater = new mediaSelectAdapter (this);
        m_grdMedia.setAdapter(m_mediaAdpater);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imbBack:
                finish();
                break;

            case R.id.imbSelect:
                returnSelect ();
                finish();
                break;

            case R.id.imbSortNameDown:
                m_mediaAdpater.sortItem(2);
                m_grdMedia.setAdapter(m_mediaAdpater);
                m_grdMedia.invalidate();
                break;

            case R.id.imbSortNameUp:
                m_mediaAdpater.sortItem(3);
                m_grdMedia.setAdapter(m_mediaAdpater);
                m_grdMedia.invalidate();
                break;

            case R.id.imbSortTimeDown:
                m_mediaAdpater.sortItem(0);
                m_grdMedia.setAdapter(m_mediaAdpater);
                m_grdMedia.invalidate();
                break;

            case R.id.imbSortTimeUp:
                m_mediaAdpater.sortItem(1);
                m_grdMedia.setAdapter(m_mediaAdpater);
                m_grdMedia.invalidate();
                break;
        }
    }

    private void returnSelect () {
        int         nSelectNum = 0;
        mediaItem   item = null;
        int nCount = m_mediaAdpater.getCount();
        for (int i = 0; i < nCount; i++) {
            item = (mediaItem)m_mediaAdpater.getItem(i);
            if (item.m_bSelect) {
                nSelectNum++;
            }
        }

        int nIndex = 0;
        String strFiles[] = new String[nSelectNum];
        for (int i = 0; i < nCount; i++) {
            item = (mediaItem)m_mediaAdpater.getItem(i);
            if (item.m_bSelect) {
                strFiles[nIndex] = item.m_strFile;
                nIndex++;
            }
        }

        if (nSelectNum > 0) {
            Intent intent = new Intent();
            intent.putExtra("FileList", strFiles);
            intent.putExtra("FileCount", nSelectNum);
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED, null);
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mediaSelectItemView vwItem = (mediaSelectItemView)view.findViewById(R.id.ivMediaItem);
        mediaItem item = vwItem.getMediaItem();
        if (item.m_nType == mediaSelectAdapter.m_nMediaFolder || item.m_nType == mediaSelectAdapter.m_nMediaBack) {
            m_mediaAdpater.setFolder(vwItem.getMediaItem().m_strFile);
            m_grdMedia.setAdapter(m_mediaAdpater);
            m_grdMedia.invalidate();

            int nPos = vwItem.getMediaItem().m_strFile.lastIndexOf(File.separator);
            String strPath = vwItem.getMediaItem().m_strFile.substring(nPos+1);
            m_txtPath.setText(strPath + "(" + (m_mediaAdpater.getCount() - 1) + ")");
        } else {
            item.m_bSelect = !item.m_bSelect;
            vwItem.invalidate();
        }
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        return true;
    }
}

