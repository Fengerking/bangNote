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
    private Context                     m_context = null;
    private ArrayList<dataNoteList>     m_lstNote = null;

    private ArrayList<String>           m_lstFiles = null;

    public noteListAdapter (Context context) {
        m_context = context;
        m_lstNote = new ArrayList<dataNoteList>();
        //dataNoteList noteList = new dataNoteList();
        //m_lstNote.add(noteList);
        //writeNote();
        readNote();
    }

    public int getCount() {
        return m_lstNote.size();
    }
    public Object getItem(int arg0) {
        return arg0;
    }
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        noteListItemView itemView = new noteListItemView(m_context);
        itemView.setTextSize(60);
        itemView.setDataList(m_lstNote.get(position));
        return itemView;
    }

    private void readNote () {
        try {
            FileInputStream fis = new FileInputStream ("/sdcard/bangNote/noteList.dat");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String strLine = null;
            while((strLine = br.readLine())!=null) {
                dataNoteList noteList = new dataNoteList();
                m_lstNote.add(noteList);
                noteList.m_strDate = strLine;
                noteList.m_strTitle = br.readLine();
                noteList.m_strLine = br.readLine();
                noteList.m_strType = br.readLine();
                noteList.m_strThumb = br.readLine();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeNote () {
        try {
            File file = new File("/sdcard/bangNote/");
            file.mkdir();

            dataNoteList noteList;
            FileOutputStream fos = new FileOutputStream ("/sdcard/bangNote/noteList.dat");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            for (int i = 0; i < m_lstNote.size(); i++) {
                noteList = m_lstNote.get(i);

                bw.write((noteList.m_strDate+"\r\n").toCharArray());
                bw.write((noteList.m_strTitle+"\r\n").toCharArray());
                bw.write((noteList.m_strLine+"\r\n").toCharArray());
                bw.write((noteList.m_strType+"\r\n").toCharArray());
                bw.write((noteList.m_strThumb+"\r\n").toCharArray());
            }
            bw.flush();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillFileList(String strPath) {
        if (m_lstFiles != null)
            m_lstFiles.clear();
        else
            m_lstFiles = new ArrayList<String>();
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
                m_lstFiles.add (file.getPath());
            }
        }

        Comparator comp = new dateComparator();
        Collections.sort(m_lstFiles, comp);
    }

    public class dateComparator implements Comparator<Object> {
        @SuppressWarnings("unchecked")
        public int compare(Object o1, Object o2) {
            return 1;
        }
    }
}
