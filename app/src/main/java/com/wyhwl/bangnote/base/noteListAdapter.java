package com.wyhwl.bangnote.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

import com.wyhwl.bangnote.base.*;
import com.wyhwl.bangnote.view.*;

public class noteListAdapter extends BaseAdapter {
    private Context                     m_context       = null;
    public  ArrayList<dataNoteItem>     m_lstAllItem    = null;
    public  ArrayList<dataNoteItem>     m_lstSelItem    = null;

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
        int             i = 0;
        dataNoteItem    dataItem = null;
        for (i = 0; i < m_lstAllItem.size(); i++) {
            dataItem = m_lstAllItem.get(i);
            if (dataItem.m_bModified) {
                dataItem.readFromFile(dataItem.m_strFile);
            }
        }
        updNoteType (noteConfig.m_noteTypeMng.getCurType());
    }

    public void updNoteType (String strType) {
        int             i = 0;
        dataNoteItem    dataItem = null;
        m_lstSelItem.clear();
        if (strType.compareTo(noteConfig.m_noteTypeMng.m_strTotal) == 0) {
            for (i = 0; i < m_lstAllItem.size(); i++) {
                dataItem = m_lstAllItem.get(i);
                if (noteConfig.m_nShowSecurity == 0) {
                    if (noteConfig.m_noteTypeMng.getLevel(dataItem.m_strType) >= 10) {
                        continue;
                    }
                    m_lstSelItem.add(dataItem);
                }
            }
        } else {
            for (i = 0; i < m_lstAllItem.size(); i++) {
                dataItem = m_lstAllItem.get(i);
                if (dataItem.m_strType.compareTo(strType) == 0) {
                    m_lstSelItem.add(dataItem);
                }
            }
        }
        Comparator comp = new dateComparator();
        Collections.sort(m_lstSelItem, comp);
    }

    public void searchNoteItem (String strFilter, boolean bAll) {
        ArrayList<dataNoteItem> lstTemp = null;
        if (bAll)
            lstTemp = new ArrayList<>(m_lstAllItem);
        else
            lstTemp = new ArrayList<>(m_lstSelItem);
        m_lstSelItem.clear();
        dataNoteItem dataItem = null;
        for (int i = 0; i < lstTemp.size(); i++) {
            dataItem = lstTemp.get(i);
            if (noteConfig.m_nShowSecurity == 0 && dataItem.isSecurity())
                continue;
            if (dataItem.m_strTitle.indexOf(strFilter) >= 0) {
                m_lstSelItem.add(dataItem);
                continue;
            }

            dataNoteItem.dataContent dataContent = null;
            for (int j = 0; j < dataItem.m_lstItem.size(); j++) {
                dataContent = dataItem.m_lstItem.get(j);
                if (dataContent.m_nType == noteConfig.m_nItemTypeText) {
                    if (dataContent.m_strItem.indexOf(strFilter) >= 0) {
                        m_lstSelItem.add(dataItem);
                        continue;
                    }
                }
            }
        }
    }

    public void updNoteFile (String strFile) {
        int             i = 0;
        dataNoteItem    dataItem = null;
        for (i = 0; i < m_lstAllItem.size(); i++) {
            dataItem = m_lstAllItem.get(i);
            if (dataItem.m_strFile.compareTo(strFile) == 0) {
                dataItem.readFromFile(dataItem.m_strFile);
                updateDataItem4Sel (dataItem);
                break;
            }
        }
    }

    public void newNoteFile (String strFile) {
        int             i = 0;
        boolean         bFound = false;
        dataNoteItem    dataItem = null;
        for (i = 0; i < m_lstAllItem.size(); i++) {
            dataItem = m_lstAllItem.get(i);
            if (dataItem.m_strFile.compareTo(strFile) == 0) {
                bFound = true;
                break;
            }
        }
        if (!bFound) {
            dataItem = new dataNoteItem();
            m_lstAllItem.add(dataItem);
        }
        dataItem.readFromFile(strFile);
        updateDataItem4Sel (dataItem);
    }

    public void delNoteFile (String strFile) {
        int i = 0;
        for (i = 0; i < m_lstAllItem.size(); i++) {
            if (m_lstAllItem.get(i).m_strFile.compareTo(strFile) == 0) {
                m_lstAllItem.remove(i);
                break;
            }
        }
        for (i = 0; i < m_lstSelItem.size(); i++) {
            if (m_lstSelItem.get(i).m_strFile.compareTo(strFile) == 0) {
                m_lstSelItem.remove(i);
                break;
            }
        }
    }

    private void updateDataItem4Sel (dataNoteItem updItem) {
        boolean         bFound = false;
        dataNoteItem    selItem = null;
        for (int i = 0; i < m_lstSelItem.size(); i++) {
            selItem = m_lstSelItem.get(i);
            if (selItem.m_strFile.compareTo(updItem.m_strFile) == 0) {
                bFound = true;
                break;
            }
        }
        if (noteConfig.m_noteTypeMng.isSelTotalType()) {
            if (!bFound)
                m_lstSelItem.add(updItem);
        } else {
            if (updItem.m_strType.compareTo(noteConfig.m_noteTypeMng.getCurType()) == 0) {
                if(!bFound)
                    m_lstSelItem.add(updItem);
            } else {
                if (bFound)
                    m_lstSelItem.remove(selItem);
            }
        }
        Comparator comp = new dateComparator();
        Collections.sort(m_lstSelItem, comp);
    }

    public int getItemCount (String strType) {
        int nCount = 0;
        dataNoteItem itemData = null;
        int nAllCount = m_lstAllItem.size();
        if (strType.compareTo(noteConfig.m_noteTypeMng.m_strTotal) == 0) {
            if (noteConfig.m_nShowSecurity == 0) {
                for (int j = 0; j < nAllCount; j++) {
                    itemData = m_lstAllItem.get(j);
                    if (noteConfig.m_noteTypeMng.getLevel(itemData.m_strType) < 10) {
                        nCount++;
                    }
                }
            } else {
                nCount = nAllCount;
            }
        } else {
            for (int j = 0; j < nAllCount; j++) {
                itemData = m_lstAllItem.get(j);
                if (itemData.m_strType.compareTo(strType) == 0) {
                    nCount++;
                }
            }
        }
        return nCount;
    }

    public int getCount() {
        return m_lstSelItem.size();
    }
    public Object getItem(int arg0) {
        if (m_lstSelItem.size() <= 0 || arg0 >= m_lstSelItem.size())
            return null;
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
