package com.wyhwl.bangnote;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class dataNoteItem {
    public static int           m_nItemTypeText = 0;
    public static int           m_nItemTypePict = 1;

    public String               m_strTitle = "";
    public String               m_strDate = "";
    public String               m_strTime = "";
    public String               m_strType = "";

    public String               m_strDateTime = "";
    public String               m_strFirstLine = "";

    public ArrayList<dataContent>    m_lstItem = null;

    public dataNoteItem () {
        m_lstItem = new ArrayList<dataContent>();

        SimpleDateFormat fmtDate = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        m_strDate = fmtDate.format(date);
        SimpleDateFormat fmtTime = new SimpleDateFormat("HH:mm:ss");
        m_strTime = fmtTime.format(date);
        m_strType = noteConfig.m_lstNoteType.get(0);
    }

    public int readFromFile (String strFile) {
        try {
            FileInputStream fis = new FileInputStream (strFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String strLine = null;
            while((strLine = br.readLine())!=null) {
                parseLineText(strLine, br);
            }
            fis.close();
        }catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        m_strDateTime = m_strDate + " " + m_strTime;

        return 0;
    }


    private void parseLineText (String strText, BufferedReader br) {
        try {
            String strLine = strText;
            if (strLine.compareTo(noteConfig.m_strTagNoteTitle) == 0) {
                m_strTitle = br.readLine();
            } else if (strLine.compareTo(noteConfig.m_strTagNoteDate) == 0) {
                m_strDate = br.readLine();
            } else if (strLine.compareTo(noteConfig.m_strTagNoteTime) == 0) {
                m_strTime = br.readLine();
            } else if (strLine.compareTo(noteConfig.m_strTagNoteType) == 0) {
                m_strType = br.readLine();
            } else if (strLine.compareTo(noteConfig.m_strTagNoteText) == 0) {
                String strContent = "";
                while ((strLine = br.readLine()) != null) {
                    if (strLine.indexOf(noteConfig.m_strTagNotePrev) >= 0)
                        break;
                    if (m_strFirstLine.length() == 0)
                        m_strFirstLine = strLine;
                    if (strContent.length() > 0)
                        strContent += "\n";
                    strContent += strLine;
                }
                dataContent content = new dataContent();
                content.m_nType = m_nItemTypeText;
                content.m_strItem = strContent;
                m_lstItem.add(content);

                if (strLine != null)
                    parseLineText(strLine, br);
            } else if (strLine.compareTo(noteConfig.m_strTagNotePict) == 0) {
                dataContent content = new dataContent();
                content.m_nType = m_nItemTypePict;
                content.m_strItem = br.readLine();
                m_lstItem.add(content);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class dataContent {
        public int      m_nType = 0;
        public String   m_strItem = null;
    }
}
