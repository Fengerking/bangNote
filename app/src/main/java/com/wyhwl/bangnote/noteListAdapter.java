package com.wyhwl.bangnote;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

public class noteListAdapter extends BaseAdapter {
    private Context                     m_context       = null;
    public  ArrayList<dataNoteItem>     m_lstAllItem    = null;
    private ArrayList<dataNoteItem>     m_lstSelItem    = null;

    public noteListAdapter (Context context) {
        m_context = context;
        m_lstAllItem = new ArrayList<dataNoteItem>();
        m_lstSelItem = new ArrayList<dataNoteItem>();
        fillFileList(noteConfig.m_strNotePath);
    }

    public dataNoteItem getNoteItem (int nIndex) {
        if (nIndex < 0 || nIndex >= m_lstSelItem.size())
            return null;
        return m_lstSelItem.get(nIndex);
    }

    public void updateNoteItem () {
        m_lstAllItem.clear();
        m_lstSelItem.clear();
        fillFileList(noteConfig.m_strNotePath);
    }

    public int getCount() {
        return m_lstSelItem.size();
    }
    public Object getItem(int arg0) {
        return m_lstSelItem.get(arg0);
    }
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        noteListItemView itemView = new noteListItemView(m_context);
        itemView.setTextSize(80);
        itemView.setDataList(m_lstSelItem.get(position));
        return itemView;
    }

    private void fillFileList(String strPath) {
        String strExtName = null;
        File fPath = new File(strPath);
        File[] fList = fPath.listFiles();
        if (fList != null) {
            for (int i = 0; i < fList.length; i++)
            {
                File file = fList[i];
                if (file.isHidden())
                    continue;
                if (file.isDirectory())
                    continue;

                strExtName = file.getPath();
                strExtName = strExtName.substring(strExtName.length() - 4);
                if (strExtName.compareTo(".bnt") != 0)
                    continue;

                dataNoteItem noteItem = new dataNoteItem();
                noteItem.readFromFile(file.getPath());
                m_lstAllItem.add(noteItem);
                if (noteConfig.m_noteTypeMng.getCurType().compareTo(noteConfig.m_noteTypeMng.m_strTotal) == 0) {
                    if (noteConfig.m_nShowSecurity > 0) {
                        m_lstSelItem.add(noteItem);
                    } else {
                        if (noteConfig.m_noteTypeMng.getLevel(noteItem.m_strType) < 10)
                             m_lstSelItem.add(noteItem);
                    }
                } else if (noteItem.m_strType.compareTo(noteConfig.m_noteTypeMng.getCurType()) == 0) {
                    m_lstSelItem.add(noteItem);
                }
            }
        }

        if (m_lstSelItem.size() > 1) {
            Comparator comp = new dateComparator();
            Collections.sort(m_lstSelItem, comp);
        }
    }

    public class dateComparator implements Comparator<Object> {
        @SuppressWarnings("unchecked")
        public int compare(Object o1, Object o2) {
            dataNoteItem noteItem1 = (dataNoteItem)o1;
            dataNoteItem noteItem2 = (dataNoteItem)o2;
            return noteItem2.m_strDateTime.compareTo(noteItem1.m_strDateTime);
        }
    }
}
