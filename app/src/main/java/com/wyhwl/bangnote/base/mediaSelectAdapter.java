package com.wyhwl.bangnote.base;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.BaseAdapter;

import android.view.LayoutInflater;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.wyhwl.bangnote.R;
import com.wyhwl.bangnote.view.mediaSelectItemView;

public class mediaSelectAdapter extends BaseAdapter {
    public final  static int           m_nMediaFolder   = 0;
    public final  static int           m_nMediaImage    = 1;
    public final  static int           m_nMediaAudio    = 2;
    public final  static int           m_nMediaVideo    = 3;
    public final  static int           m_nMediaBack     = 4;

    private     Context                 m_context = null;
    private     String                  m_strSdcard = null;
    private     String                  m_strFolder = null;
    private     ArrayList<mediaItem>    m_lstItems = null;
    private     int                     m_nSortType = 1;


    public class mediaItem {
        public boolean      m_bSelect = false;
        public String       m_strFile = null;
        public String       m_strName = null;
        public long         m_lTime = 0;
        public int          m_nType = 0;
    }

    public mediaSelectAdapter (Context context) {
        m_context = context;
        m_lstItems = new ArrayList<mediaItem>();

        File file = Environment.getExternalStorageDirectory();
        m_strSdcard = file.getPath();
        setFolder(m_strSdcard);
    }

    public void setFolder (String strFolder) {
        m_lstItems.clear();
        m_strFolder = strFolder;
        int     nMediaType = -1;

        File fPath = new File(m_strFolder);
        File[] fList = fPath.listFiles();
        if (fList != null) {
            for (int i = 0; i < fList.length; i++) {
                File file = fList[i];
                if (file.isHidden())
                    continue;

                nMediaType = -1;
                if (file.isDirectory()) {
                    File fSubFolder = new File(file.getPath());
                    File[] fSubList = fSubFolder.listFiles();
                    if (fSubList != null) {
                        for (int j = 0; j < fSubList.length; j++) {
                            File subfile = fSubList[j];
                            if (subfile.isHidden())
                                continue;
                            if (subfile.isDirectory()) {
                                nMediaType = m_nMediaFolder;
                                break;
                            } else if (getMediaType (subfile.getPath()) > 0) {
                                nMediaType = m_nMediaFolder;
                                break;
                            }
                        }
                    }
                } else {
                    nMediaType = getMediaType (file.getPath());
                }

                if (nMediaType >= 0) {
                    mediaItem item = new mediaItem();
                    item.m_strFile = file.getPath();
                    item.m_lTime = file.lastModified();
                    int nPos = item.m_strFile.lastIndexOf(File.separator);
                    item.m_strName = item.m_strFile.substring(nPos+1);
                    item.m_nType = nMediaType;
                    m_lstItems.add(item);
                }
            }
        }

        sortItem (m_nSortType);

        if (strFolder.compareTo(m_strSdcard) != 0) {
            mediaItem item = new mediaItem();
            int nPos = strFolder.lastIndexOf(File.separator);
            item.m_strFile = strFolder.substring(0, nPos);
            item.m_strName = "返回上一级";
            item.m_nType = m_nMediaBack;
            m_lstItems.add(0, item);
        }
    }

    public void sortItem (int nSorttype) {
        m_nSortType = nSorttype;
        Comparator comp = new itemComparator();
        Collections.sort(m_lstItems, comp);
    }

    public class itemComparator implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            mediaItem noteItem1 = (mediaItem)o1;
            mediaItem noteItem2 = (mediaItem)o2;
            if (noteItem2.m_nType == m_nMediaBack && noteItem1.m_nType != m_nMediaBack)
                return 1;
            if (noteItem1.m_nType == m_nMediaBack && noteItem2.m_nType != m_nMediaBack)
                return -1;
            if (noteItem2.m_nType == m_nMediaFolder && noteItem1.m_nType != m_nMediaFolder)
                return 1;
            if (noteItem1.m_nType == m_nMediaFolder && noteItem2.m_nType != m_nMediaFolder)
                return -1;

            if (m_nSortType == 0) // time down
                return (int) (noteItem2.m_lTime - noteItem1.m_lTime);
            else if (m_nSortType == 1) // time up
                return (int)(noteItem1.m_lTime - noteItem2.m_lTime);
            else if (m_nSortType == 2) // time up
                return noteItem2.m_strName.compareTo(noteItem1.m_strName);
            else if (m_nSortType == 3) // time up
                return noteItem1.m_strName.compareTo(noteItem2.m_strName);
            else
                return (int)(noteItem2.m_lTime - noteItem1.m_lTime);
        }
    }

    public static int getMediaType (String strFile) {
        if (strFile == null)
            return -1;

        String  strExtName = null;
        int     nExtPos = 0;
        int     nMediaType = -1;

        nExtPos = strFile.lastIndexOf('.');
        if (nExtPos > 0) {
            strExtName = strFile.substring(nExtPos+1);
            if (strExtName.compareToIgnoreCase("mp3") == 0)
                nMediaType = m_nMediaAudio;
            else if (strExtName.compareToIgnoreCase("aac") == 0)
                nMediaType = m_nMediaAudio;
            else if (strExtName.compareToIgnoreCase("wav") == 0)
                nMediaType = m_nMediaAudio;
            else if (strExtName.compareToIgnoreCase("jpg") == 0)
                nMediaType = m_nMediaImage;
            else if (strExtName.compareToIgnoreCase("jpeg") == 0)
                nMediaType = m_nMediaImage;
            else if (strExtName.compareToIgnoreCase("png") == 0)
                nMediaType = m_nMediaImage;
            else if (strExtName.compareToIgnoreCase("mp4") == 0)
                nMediaType = m_nMediaVideo;
            else if (strExtName.compareToIgnoreCase("flv") == 0)
                nMediaType = m_nMediaVideo;
        }
        return nMediaType;
    }

    public int getCount() {
        return m_lstItems.size();
    }
    public Object getItem(int arg0) {
        if (m_lstItems.size() <= 0 || arg0 >= m_lstItems.size())
            return null;
        return m_lstItems.get(arg0);
    }
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(m_context).inflate(R.layout.media_select_item, parent, false);
            mediaSelectItemView vwItem = (mediaSelectItemView)convertView.findViewById(R.id.ivMediaItem);
            vwItem.setMediaItem (m_lstItems.get(position));
        }
        return convertView;
    }
}
