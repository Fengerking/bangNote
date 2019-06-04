package com.wyhwl.bangnote.base;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.ArrayList;

public class noteBackupRestore {
    private Context     m_context = null;

    private long        m_lLastBackupTime = 0;

    public noteBackupRestore (Context context) {
        m_context = context;
    }

    public int backupNote () {
        findLastModifedTime ();
        if (!needBackupNote())
            return 1;

        String strZipFile = noteConfig.getNoteZipFile();
        try {
            ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(strZipFile));
            File file = new File(noteConfig.m_strNotePath);
            if (zipFiles(file.getParent() + File.separator, file.getName(), outZip) < 0) {
                return -1;
            }
            outZip.finish();
            outZip.close();
        }catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return 1;
    }

    public int restoreNote () {
        ArrayList<String>   lstZipFiles = new ArrayList<String>();

        File fPath = new File(noteConfig.m_strBackPath);
        File[] fList = fPath.listFiles();
        if (fList != null) {
            for (int i = 0; i < fList.length; i++) {
                File file = fList[i];
                if (file.isHidden())
                    continue;
                if (file.isDirectory())
                    continue;
                lstZipFiles.add (file.getPath());
            }
        }

        Comparator comp = new nameComparator();
        Collections.sort(lstZipFiles, comp);

        for (int i = 0; i < lstZipFiles.size(); i++) {
            unzipFolder(lstZipFiles.get(i), noteConfig.m_strRootPath);
        }
        return 1;
    }

    private boolean needZipFile (String strFolder, String strFile) {
        File fileCheck = new File (strFolder + strFile);
        if (fileCheck.exists()) {
            if (fileCheck.lastModified() < m_lLastBackupTime) {
                return false;
            }
        }
        return true;
    }

    private boolean needUnzipFile (String strFolder, String strFile, long lZipTime) {
        String strNoteFile = strFolder + strFile;
        File noteFile = new File (strNoteFile);
        if (noteFile.exists()) {
            long lModifyTime = noteFile.lastModified();
            if (lModifyTime >= lZipTime)
                return false;
        }
        return true;
    }

    private int zipFiles(String strFolder, String strName, ZipOutputStream zipOutputSteam) {
        if(zipOutputSteam == null)
            return -1;
        try {
            File file = new File(strFolder+strName);
            if (file.isFile()) {
                if (!needZipFile(strFolder, strName)) {
                    return 1;
                }
                ZipEntry zipEntry =  new ZipEntry(strName);
                zipEntry.setTime(file.lastModified());
                FileInputStream inputStream = new FileInputStream(file);
                zipOutputSteam.putNextEntry(zipEntry);
                int len;
                byte[] buffer = new byte[4096];
                while((len=inputStream.read(buffer)) != -1) {
                    zipOutputSteam.write(buffer, 0, len);
                }
                zipOutputSteam.closeEntry();
            } else {
                // Folder
                String fileList[] = file.list();
                //没有子文件和压缩
                if (fileList.length <= 0) {
                    ZipEntry zipEntry =  new ZipEntry(strName+File.separator);
                    zipOutputSteam.putNextEntry(zipEntry);
                    zipOutputSteam.closeEntry();
                }
                //子文件和递归
                for (int i = 0; i < fileList.length; i++) {
                    zipFiles(strFolder, strName+ File.separator+fileList[i], zipOutputSteam);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public void unzipFolder(String strZipFile, String strOutPath) {
        try {
            ZipInputStream  inZip = new ZipInputStream(new FileInputStream(strZipFile));
            ZipEntry        zipEntry;
            String          szName = "";
            while ((zipEntry = inZip.getNextEntry()) != null) {
                szName = zipEntry.getName();
                if (!needUnzipFile (strOutPath, szName, zipEntry.getTime()))
                    continue;
                if (zipEntry.isDirectory()) {
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(strOutPath + File.separator + szName);
                    folder.mkdirs();
                } else {
                    File file = new File(strOutPath + File.separator + szName);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }

                    FileOutputStream out = new FileOutputStream(file);
                    int len;
                    byte[] buffer = new byte[1024];
                    while ((len = inZip.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                    out.close();

                }
            }
            inZip.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findLastModifedTime () {
        String strLastName = "";
        File fPath = new File(noteConfig.m_strBackPath);
        File[] fList = fPath.listFiles();
        if (fList != null) {
            for (int i = 0; i < fList.length; i++) {
                File file = fList[i];
                if (file.isHidden())
                    continue;
                if (file.isDirectory())
                    continue;
                if (file.getName().compareTo(strLastName) > 0)
                    strLastName = file.getName();
            }
        }

        if (strLastName.length() <= 0)
            return;
        strLastName = strLastName.substring(4, strLastName.length() - 4);
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            Date dateLast = formatter.parse(strLastName);
            m_lLastBackupTime = dateLast.getTime();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean needBackupNote () {
        File fPath = new File(noteConfig.m_strNotePath);
        File[] fList = fPath.listFiles();
        if (fList != null) {
            for (int i = 0; i < fList.length; i++) {
                File file = fList[i];
                if (file.isHidden())
                    continue;
                if (file.isDirectory())
                    continue;
                if (file.lastModified() > m_lLastBackupTime)
                    return true;
            }
        }
        return false;
    }

    public class nameComparator implements Comparator<Object> {
        @SuppressWarnings("unchecked")
        public int compare(Object o1, Object o2) {
            String strName1 = (String)o1;
            String strName2 = (String)o2;
            return strName1.compareTo(strName2);
        }
    }
}
