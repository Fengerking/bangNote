package com.wyhwl.bangnote.base;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import com.wyhwl.bangnote.R;

public class noteTypeMng {
    public final static  int            m_nSecurityLevel    = 10;
    public final static  int            m_nTypeTotal        = -1;
    public final static  int            m_nTypeRubbish      = -2;
    public final static  int            m_nTypeDefault      = -3;

    public String                       m_strFile       = null;
    public ArrayList<noteTypeItem>      m_lstType       = null;
    public String                       m_strRubbish    = "垃圾笔记";
    public String                       m_strDefault    = "默认笔记";
    public String                       m_strTotal      = "所有笔记";

    private int[]  m_nTypeImage = {R.drawable.notetype_a, R.drawable.notetype_b,
                                   R.drawable.notetype_c, R.drawable.notetype_d,
                                   R.drawable.notetype_e, R.drawable.notetype_f};

    public class noteTypeItem  {
        public String   m_strName = null;
        public int      m_nUsing = 0;
        // -1 Total , -2, rubbish, -3 default. 0 - 9 normal. > 10 security
        public int      m_nLevel = 0;  // < 0 system,
        public int      m_nImage = 0;


        public noteTypeItem () {
            m_strName = "丰耳笔记";
            m_nUsing = 0;
            m_nLevel = 0;
            m_nImage = m_nTypeImage[0];
        }
    }

    public noteTypeMng () {
        m_strFile = noteConfig.m_strNotePath + "notetype.bns";
        m_lstType = new ArrayList<noteTypeItem>();
        readFromFile();
        checkSecurity();
    }

    public int getCount () {
        return m_lstType.size();
    }
    public noteTypeItem getItem (int nIndex) {
        if (nIndex < 0 || nIndex >= m_lstType.size())
            return null;
        return m_lstType.get(nIndex);
    }
    public String getName (int nIndex) {
        if (nIndex < 0 || nIndex >= m_lstType.size())
            return null;
        return m_lstType.get(nIndex).m_strName;
    }
    public int getLevel (int nIndex) {
        if (nIndex < 0 || nIndex >= m_lstType.size())
            return m_nTypeTotal;
        return m_lstType.get(nIndex).m_nLevel;
    }
    public int getLevel (String strName) {
        for (int i = 0; i < m_lstType.size(); i++) {
            if (m_lstType.get(i).m_strName.compareTo(strName) == 0)
                return m_lstType.get(i).m_nLevel;
        }
        return -101;
    }
    public int getImage (int nIndex) {
        if (nIndex < 0 || nIndex >= m_lstType.size())
            return -1;
        return m_lstType.get(nIndex).m_nImage;
    }

    public int addType (String strName, int nLevel) {
        for (int i = 0; i < m_lstType.size(); i++) {
            if (m_lstType.get(i).m_strName.compareTo(strName) == 0)
                return -1;
        }
        noteTypeItem itemType = new noteTypeItem();
        itemType.m_strName = strName;
        itemType.m_nLevel = nLevel;
        itemType.m_nImage = m_nTypeImage[m_lstType.size()%6];
        m_lstType.add(itemType);
        return 0;
    }

    public int delType (String strType) {
        for (int i = 0; i < m_lstType.size(); i++) {
            if (m_lstType.get(i).m_strName.compareTo(strType) == 0) {
                m_lstType.remove(i);
                return 1;
            }
        }
        return -1;
    }

    public String getCurType () {
        for (int i = 0; i < m_lstType.size(); i++) {
            if (m_lstType.get(i).m_nUsing > 0)
                return m_lstType.get(i).m_strName;
        }
        return null;
    }

    public int setCurType (String strType) {
        for (int i = 0; i < m_lstType.size(); i++) {
            m_lstType.get(i).m_nUsing = 0;
            if (m_lstType.get(i).m_strName.compareTo(strType) == 0)
                m_lstType.get(i).m_nUsing = 1;
        }
        return 0;
    }

    public void checkSecurity () {
        String  strCurType = getCurType();
        int     nLevel = getLevel(strCurType);
        if (nLevel >= 10)
            setCurType(m_strTotal);
    }

    public int changeType (String strOld, String strNew) {
        if (strOld.length() <= 0 || strNew.length() <= 0)
            return -1;
        if (getLevel(strOld) < -100)
            return -1;
        for (int i = 0; i < m_lstType.size(); i++) {
            if (m_lstType.get(i).m_strName.compareTo(strOld) == 0) {
                m_lstType.get(i).m_strName = strNew;
                return 1;
            }
        }
        return -1;
    }

    public boolean isSelTotalType () {
        if (m_strTotal.compareTo(getCurType()) == 0)
            return true;
        return false;
    }

    public boolean haveSecurityType () {
        for (int i = 0; i < m_lstType.size(); i++) {
            if (m_lstType.get(i).m_nLevel >= m_nSecurityLevel) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getListName (boolean bNewNote) {
        ArrayList<String> lstName = new ArrayList<String> ();
        for (int i = 0; i < m_lstType.size(); i++) {
            if (m_lstType.get(i).m_nLevel == m_nTypeTotal)  // total type
                continue;
            if (bNewNote && m_lstType.get(i).m_nLevel == m_nTypeRubbish)  // rubbish
                continue;
            if (noteConfig.m_nShowSecurity == 0) {
                if (m_lstType.get(i).m_nLevel >= 10)
                    continue;
            }
            lstName.add(m_lstType.get(i).m_strName);
        }
        return lstName;
    }

    public void readFromFile () {
        noteTypeItem    itemType = null;

        m_lstType.clear();

        try {
            noteFileInputStream fis = new noteFileInputStream (m_strFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String          strLine = null;
            int             nNextPos = 0;
            while((strLine = br.readLine())!=null) {
                itemType = new noteTypeItem();
                nNextPos = strLine.indexOf(',', 0);
                itemType.m_strName = strLine.substring(0, nNextPos);

                strLine = strLine.substring(nNextPos+1);
                itemType.m_nUsing = Integer.parseInt(strLine.substring(0, 1));
                itemType.m_nLevel = Integer.parseInt(strLine.substring(2));

                itemType.m_nImage = m_nTypeImage[m_lstType.size()%6];
                if (itemType.m_nLevel == m_nTypeTotal)
                    itemType.m_nImage = R.drawable.notetype_aa;
                else if (itemType.m_nLevel == m_nTypeRubbish)
                    itemType.m_nImage = R.drawable.lajitong;
                else if (itemType.m_nLevel == m_nTypeDefault)
                    itemType.m_nImage = m_nTypeImage[0];
                m_lstType.add(itemType);
            }
            fis.close();
        }catch (Exception e) {
            e.printStackTrace();
        }

        if (m_lstType.size() == 0) {
            itemType = new noteTypeItem ();
            itemType.m_strName = m_strDefault;
            itemType.m_nLevel = m_nTypeDefault;
            itemType.m_nImage = m_nTypeImage[0];
            m_lstType.add(itemType);

            itemType = new noteTypeItem ();
            itemType.m_strName = m_strTotal;
            itemType.m_nLevel = m_nTypeTotal;
            itemType.m_nUsing = 1;
            itemType.m_nImage = R.drawable.notetype_aa;
            m_lstType.add(itemType);

            itemType = new noteTypeItem ();
            itemType.m_strName = m_strRubbish;
            itemType.m_nLevel = m_nTypeRubbish;
            itemType.m_nImage = R.drawable.lajitong;

            m_lstType.add(itemType);
        }

        if (noteConfig.m_nShowSecurity == 0) {
            boolean bFound = false;
            noteTypeItem    itemDefault = null;
            for (int i = 0; i < m_lstType.size(); i++) {
                itemType = m_lstType.get(i);
                if (itemType.m_nLevel == m_nTypeDefault) {
                    itemDefault = itemType;
                }
                if (itemType.m_nUsing > 0) {
                    if (itemType.m_nLevel >= m_nSecurityLevel) {
                        itemType.m_nUsing = 0;
                        bFound = true;
                        break;
                    }
                }
            }
            if (bFound && itemDefault != null)
                itemDefault.m_nUsing = 1;
        }
    }

    public void writeToFile () {
        try {
            noteFileOutputStream fos = new noteFileOutputStream (m_strFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            for (int i = 0; i < m_lstType.size(); i++) {
                bw.write((m_lstType.get(i).m_strName + "," +
                        m_lstType.get(i).m_nUsing + "," +
                        m_lstType.get(i).m_nLevel + "\n").toCharArray());
            }
            bw.flush();
            fos.close();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
