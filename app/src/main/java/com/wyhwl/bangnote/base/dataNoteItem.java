package com.wyhwl.bangnote.base;

import android.graphics.Bitmap;
import android.view.View;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class dataNoteItem {
    public String               m_strFile = "";
    public String               m_strTitle = "";
    public String               m_strDate = "";
    public String               m_strTime = "";
    public String               m_strType = "";
    public String               m_strCity = "";
    public String               m_strWeat = "";

    public View                 m_view = null;
    public Bitmap               m_thumb = null;

    public String               m_strDateTime = "";
    public String               m_strFirstLine = "";
    public String               m_strImgFile = null;
    public String               m_strAudFile = null;
    public String               m_strVidFile = null;

    public boolean              m_bModified = false;
    public boolean              m_bHadRead = false;

    private boolean             m_bSelect = false;


    public ArrayList<dataContent>    m_lstItem = null;


    public class dataContent {
        public int      m_nType = 0;
        public String   m_strItem = null;
    }

    public dataNoteItem () {
        m_lstItem = new ArrayList<dataContent>();

        SimpleDateFormat fmtDate = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        m_strDate = fmtDate.format(date);
        SimpleDateFormat fmtTime = new SimpleDateFormat("HH:mm:ss");
        m_strTime = fmtTime.format(date);
        m_strType = noteConfig.m_noteTypeMng.getCurType();
    }


    public void setSelect () {
        m_bSelect = !m_bSelect;
    }

    public boolean isSelect () {
        return m_bSelect;
    }

    public boolean isSecurity () {
        return noteConfig.m_noteTypeMng.getLevel(m_strType) >= 10;
    }

    public int readFromFile (String strFile) {
        m_strFile = strFile;
        m_lstItem.clear();
        try {
            noteFileInputStream fis = new noteFileInputStream (strFile);
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
        m_bModified = false;
        m_bHadRead = true;
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
            } else if (strLine.compareTo(noteConfig.m_strTagNoteCity) == 0) {
                m_strCity = br.readLine();
            } else if (strLine.compareTo(noteConfig.m_strTagNoteWeat) == 0) {
                m_strWeat = br.readLine();
            } else if (strLine.compareTo(noteConfig.m_strTagNoteText) == 0) {
                String strContent = "";
                while ((strLine = br.readLine()) != null) {
                    if (strLine.indexOf(noteConfig.m_strTagNotePrev) >= 0)
                        break;
                    if (m_strFirstLine.length() == 0) {
                        m_strFirstLine = strLine;
                        while (m_strFirstLine.substring(0,1).compareTo(" ") == 0)
                            m_strFirstLine = m_strFirstLine.substring(1);
                    }
                    if (strContent.length() > 0)
                        strContent += "\n";
                    strContent += strLine;
                }
                dataContent content = new dataContent();
                content.m_nType = noteConfig.m_nItemTypeText;
                content.m_strItem = strContent;
                m_lstItem.add(content);

                if (strLine != null)
                    parseLineText(strLine, br);
            } else if (strLine.compareTo(noteConfig.m_strTagNotePict) == 0) {
                dataContent content = new dataContent();
                content.m_nType = noteConfig.m_nItemTypePict;
                content.m_strItem = noteConfig.m_strNotePath + br.readLine();
                if (m_strImgFile == null)
                    m_strImgFile = content.m_strItem;
                m_lstItem.add(content);
            } else if (strLine.compareTo(noteConfig.m_strTagNoteAudo) == 0) {
                dataContent content = new dataContent();
                content.m_nType = noteConfig.m_nItemTypeAudo;
                content.m_strItem = noteConfig.m_strNotePath + br.readLine();
                if (m_strAudFile == null)
                    m_strAudFile = content.m_strItem;
                m_lstItem.add(content);
            } else if (strLine.compareTo(noteConfig.m_strTagNoteVido) == 0) {
                dataContent content = new dataContent();
                content.m_nType = noteConfig.m_nItemTypeVido;
                content.m_strItem = noteConfig.m_strNotePath + br.readLine();
                if (m_strVidFile == null)
                    m_strVidFile = content.m_strItem;
                m_lstItem.add(content);
            } else if (strLine.compareTo(noteConfig.m_strTagNoteMusc) == 0) {
                dataContent content = new dataContent();
                content.m_nType = noteConfig.m_nItemTypeMusc;
                content.m_strItem = noteConfig.m_strNotePath + br.readLine();
                if (m_strAudFile == null)
                    m_strAudFile = content.m_strItem;
                m_lstItem.add(content);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int writeToFile () {
        try {
            noteFileOutputStream fos = new noteFileOutputStream (m_strFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            bw.write((noteConfig.m_strTagNoteTitle +"\n").toCharArray());
            bw.write((m_strTitle +"\n").toCharArray());

            bw.write((noteConfig.m_strTagNoteDate +"\n").toCharArray());
            bw.write((m_strDate +"\n").toCharArray());
            bw.write((noteConfig.m_strTagNoteTime +"\n").toCharArray());
            bw.write((m_strTime +"\n").toCharArray());
            bw.write((noteConfig.m_strTagNoteCity +"\n").toCharArray());
            bw.write((m_strCity +"\n").toCharArray());
            bw.write((noteConfig.m_strTagNoteWeat +"\n").toCharArray());
            bw.write((m_strWeat +"\n").toCharArray());
            bw.write((noteConfig.m_strTagNoteType +"\n").toCharArray());
            bw.write((m_strType +"\n").toCharArray());

            int     nNotePathLen = noteConfig.m_strNotePath.length();
            dataContent itemData = null;
            int nCount = m_lstItem.size();
            for (int i = 0; i < nCount; i++) {
                itemData = m_lstItem.get(i);
                if (itemData.m_nType == noteConfig.m_nItemTypeText) {
                    bw.write((noteConfig.m_strTagNoteText +"\n").toCharArray());
                    bw.write((itemData.m_strItem +"\n").toCharArray());
                } else if (itemData.m_nType == noteConfig.m_nItemTypePict) {
                    bw.write((noteConfig.m_strTagNotePict +"\n").toCharArray());
                    bw.write((itemData.m_strItem.substring(nNotePathLen) +"\n").toCharArray());
                } else if (itemData.m_nType == noteConfig.m_nItemTypeAudo) {
                    bw.write((noteConfig.m_strTagNoteAudo +"\n").toCharArray());
                    bw.write((itemData.m_strItem.substring(nNotePathLen) +"\n").toCharArray());
                } else if (itemData.m_nType == noteConfig.m_nItemTypeVido) {
                    bw.write((noteConfig.m_strTagNoteVido +"\n").toCharArray());
                    bw.write((itemData.m_strItem.substring(nNotePathLen) +"\n").toCharArray());
                } else if (itemData.m_nType == noteConfig.m_nItemTypeMusc) {
                    bw.write((noteConfig.m_strTagNoteMusc +"\n").toCharArray());
                    bw.write((itemData.m_strItem.substring(nNotePathLen) +"\n").toCharArray());
                }
            }
            bw.flush();
            fos.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        m_bModified = true;
        return 0;
    }

}
