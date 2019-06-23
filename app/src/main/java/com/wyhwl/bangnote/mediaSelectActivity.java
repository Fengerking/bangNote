package com.wyhwl.bangnote;

import android.content.Intent;
import android.net.Uri;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class mediaSelectActivity extends AppCompatActivity
                                    implements View.OnClickListener,
                                        AdapterView.OnItemClickListener,
                                            AdapterView.OnItemLongClickListener {
    private GridView                m_grdMedia = null;
    private mediaSelectAdapter      m_mediaAdpater = null;

    private TextView                m_txtPath = null;
    private int                     m_nSelectNum = 0;

    private ImageButton             m_btnNameUp = null;
    private ImageButton             m_btnNameDown = null;
    private ImageButton             m_btnTimeUp = null;
    private ImageButton             m_btnTimeDown = null;

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
        ((ImageButton) findViewById(R.id.imbImageShow)).setOnClickListener(this);
        m_txtPath = (TextView)findViewById(R.id.txtPath);

        m_btnNameUp = (ImageButton) findViewById(R.id.imbSortNameUp);
        m_btnNameDown = (ImageButton) findViewById(R.id.imbSortNameDown);
        m_btnNameUp.setVisibility(View.INVISIBLE);
        m_btnTimeUp = (ImageButton) findViewById(R.id.imbSortTimeUp);
        m_btnTimeDown = (ImageButton) findViewById(R.id.imbSortTimeDown);
        m_btnTimeUp.setVisibility(View.INVISIBLE);

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

            case R.id.imbImageShow:
                showImage ();
                break;

            case R.id.imbSortNameDown:
                m_btnNameDown.setVisibility(View.INVISIBLE);
                m_btnNameUp.setVisibility(View.VISIBLE);
                m_mediaAdpater.sortItem(2);
                m_grdMedia.setAdapter(m_mediaAdpater);
                m_grdMedia.invalidate();
                break;

            case R.id.imbSortNameUp:
                m_btnNameDown.setVisibility(View.VISIBLE);
                m_btnNameUp.setVisibility(View.INVISIBLE);
                m_mediaAdpater.sortItem(3);
                m_grdMedia.setAdapter(m_mediaAdpater);
                m_grdMedia.invalidate();
                break;

            case R.id.imbSortTimeDown:
                m_btnTimeDown.setVisibility(View.INVISIBLE);
                m_btnTimeUp.setVisibility(View.VISIBLE);
                m_mediaAdpater.sortItem(0);
                m_grdMedia.setAdapter(m_mediaAdpater);
                m_grdMedia.invalidate();
                break;

            case R.id.imbSortTimeUp:
                m_btnTimeDown.setVisibility(View.VISIBLE);
                m_btnTimeUp.setVisibility(View.INVISIBLE);
                m_mediaAdpater.sortItem(1);
                m_grdMedia.setAdapter(m_mediaAdpater);
                m_grdMedia.invalidate();
                break;
        }
    }

    private void returnSelect () {
        ArrayList<mediaItem>    lstSelect = new ArrayList<mediaItem>();
        mediaItem               item = null;
        int nCount = m_mediaAdpater.getCount();
        for (int i = 0; i < nCount; i++) {
            item = (mediaItem)m_mediaAdpater.getItem(i);
            if (item.m_nSelect > 0) {
                lstSelect.add(item);
            }
        }

        Comparator comp = new selectComparator();
        Collections.sort(lstSelect, comp);

        String strFiles[] = new String[lstSelect.size()];
        for (int i = 0; i < lstSelect.size(); i++) {
            strFiles[i] = lstSelect.get(i).m_strFile;
        }

        if (lstSelect.size() > 0) {
            Intent intent = new Intent();
            intent.putExtra("FileList", strFiles);
            intent.putExtra("FileCount", lstSelect.size());
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED, null);
        }
    }

    private void showImage () {
        int nImageCount = 0;
        int nCount = m_mediaAdpater.m_lstItems.size();
        for (int i = 0; i < nCount; i++) {
            if (m_mediaAdpater.m_lstItems.get(i).m_nType == m_mediaAdpater.m_nMediaImage) {
                nImageCount++;
            }
        }
        if (nImageCount <= 0)
            return;

        m_mediaAdpater.setFolder(null);

        String strFiles[] = new String[nImageCount];
        nImageCount = 0;
        for (int i = 0; i < nCount; i++) {
            if (m_mediaAdpater.m_lstItems.get(i).m_nType == m_mediaAdpater.m_nMediaImage) {
                strFiles[nImageCount++] = m_mediaAdpater.m_lstItems.get(i).m_strFile;
            }
        }
        Intent intent = new Intent(mediaSelectActivity.this, noteImageActivity.class);
        intent.setData(Uri.parse(strFiles[0]));
        intent.putExtra("FileList", strFiles);
        intent.putExtra("FileCount", nImageCount);
        startActivity(intent);
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
            m_txtPath.setText(strPath + " (" + (m_mediaAdpater.getCount() - 1) + ")");
        } else {
            if (item.m_nSelect > 0) {
                int nCount = m_mediaAdpater.m_lstItems.size();
                for (int i = 0; i < nCount; i++) {
                    if (m_mediaAdpater.m_lstItems.get(i).m_nSelect > item.m_nSelect) {
                        m_mediaAdpater.m_lstItems.get(i).m_nSelect--;
                        if (m_mediaAdpater.m_lstItems.get(i).m_view != null)
                            m_mediaAdpater.m_lstItems.get(i).m_view.invalidate();
                    }
                }
                m_nSelectNum--;
                item.m_nSelect = 0;
            } else {
                m_nSelectNum++;
                item.m_nSelect = m_nSelectNum;
            }
            vwItem.invalidate();
        }
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return true;
    }

    public class selectComparator implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            mediaItem noteItem1 = (mediaItem)o1;
            mediaItem noteItem2 = (mediaItem)o2;
            return noteItem1.m_nSelect >= noteItem2.m_nSelect ? 1 : -1;
        }
    }
}

