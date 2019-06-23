package com.wyhwl.bangnote.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.BaseAdapter;

import android.view.LayoutInflater;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.wyhwl.bangnote.R;
import com.wyhwl.bangnote.view.mediaSelectItemView;

public class mediaSelectAdapter extends BaseAdapter {
    public final  static int           MSG_MEDIA_UPDATE_VIEW   = 1000;

    public final  static int           m_nMediaBack     = -1;
    public final  static int           m_nMediaFolder   = 0;
    public final  static int           m_nMediaImage    = 1;
    public final  static int           m_nMediaAudio    = 2;
    public final  static int           m_nMediaVideo    = 3;

    private     Context                 m_context = null;
    private     String                  m_strSdcard = null;
    private     String                  m_strFolder = null;
    public      ArrayList<mediaItem>    m_lstItems = null;
    private     int                     m_nSortType = 3;

    private     boolean                 m_bFinished = true;
    private     boolean                 m_bCanceled = false;

    private     updateViewHandler       m_updHandler = null;


    public class mediaItem {
        public int          m_nSelect = 0;
        public String       m_strFile = null;
        public String       m_strName = null;
        public long         m_lTime = 0;
        public int          m_nType = 0;

        public Bitmap       m_thumb = null;
        public View         m_view = null;
    }

    public mediaSelectAdapter (Context context) {
        m_context = context;
        m_updHandler = new updateViewHandler();
        m_lstItems = new ArrayList<mediaItem>();

        File file = Environment.getExternalStorageDirectory();
        m_strSdcard = file.getPath();
        setFolder(m_strSdcard);
    }

    public void setFolder (String strFolder) {
        if (!m_bFinished) {
            m_bCanceled = true;
            while (!m_bFinished) {
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (strFolder == null)
            return;

        m_lstItems.clear();
        m_strFolder = strFolder;
        int     nMediaType = -1;

        File fPath = new File(m_strFolder);
        File[] fList = fPath.listFiles();
        if (fList != null) {
            for (int i = 0; i < fList.length; i++) {
                File file = fList[i];
                if (noteConfig.m_nShowSecurity == 0) {
                    if (file.isHidden())
                        continue;
                }
                nMediaType = -1;
                if (file.isDirectory()) {
                    if (haveMediaInFolder (file.getPath()))
                        nMediaType = m_nMediaFolder;
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

        int nPos = m_strFolder.lastIndexOf(File.separatorChar);
        String strName = m_strFolder.substring(nPos+1);
        if (strName.compareToIgnoreCase("camera") == 0)
            sortItem (0);
        else
            sortItem (3);

        if (strFolder.compareTo(m_strSdcard) != 0) {
            mediaItem item = new mediaItem();
            nPos = strFolder.lastIndexOf(File.separator);
            item.m_strFile = strFolder.substring(0, nPos);
            item.m_strName = "返回上一级";
            item.m_nType = m_nMediaBack;
            m_lstItems.add(0, item);
        }

        updateThumbThread ();
    }

    public void continueThumb () {
        if (!m_bCanceled)
            return;
        updateThumbThread();
    }

    private boolean haveMediaInFolder (String strFolder) {
        File fFolder = new File(strFolder);
        File[] fList = fFolder.listFiles();
        if (fList == null)
            return false;

        int i = 0;
        for (i = 0; i < fList.length; i++) {
            File file = fList[i];
            if (noteConfig.m_nShowSecurity == 0) {
                if (file.isHidden())
                    continue;
            }
            if (file.isDirectory())
                continue;
            if (getMediaType (file.getPath()) > 0)
                return true;
        }

        for (i = 0; i < fList.length; i++) {
            File file = fList[i];
            if (noteConfig.m_nShowSecurity == 0) {
                if (file.isHidden())
                    continue;
            }
            if (file.isDirectory()) {
                if (haveMediaInFolder (file.getPath()))
                    return true;
            }
        }
        return false;
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
                return noteItem2.m_lTime >= noteItem1.m_lTime ? 1 : -1;
            else if (m_nSortType == 1) // time up
                return noteItem1.m_lTime >= noteItem2.m_lTime ? 1 : -1;
            else if (m_nSortType == 2) // time up
                return noteItem2.m_strName.compareToIgnoreCase(noteItem1.m_strName);
            else if (m_nSortType == 3) // time up
                return noteItem1.m_strName.compareToIgnoreCase(noteItem2.m_strName);
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
            else if (strExtName.compareToIgnoreCase("bnp") == 0)
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
        View vmLayout = null;
        if (convertView == null)
            vmLayout = LayoutInflater.from(m_context).inflate(R.layout.media_select_item, parent, false);
        else {
            for (int i = 0; i < m_lstItems.size(); i++) {
                if (m_lstItems.get(i).m_view == convertView) {
                    m_lstItems.get(i).m_view = null;
                    break;
                }
            }
            vmLayout = convertView;
        }
        mediaSelectItemView vwItem = (mediaSelectItemView) vmLayout.findViewById(R.id.ivMediaItem);
        vwItem.setMediaItem (m_lstItems.get(position));
        vwItem.invalidate();
        m_lstItems.get(position).m_view = vwItem;
        return vmLayout;
    }

    private void updateThumbThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                m_bFinished = false;
                m_bCanceled = false;

                int             nCount = m_lstItems.size();
                mediaItem       dataItem = null;
                for (int i = 0; i < nCount; i++) {
                    dataItem = m_lstItems.get(i);
                    if (dataItem.m_thumb == null && dataItem.m_nType == m_nMediaImage) {
                        Bitmap bmpFile = null;
                        try {
                            FileInputStream fis = null;
                            String strExt = dataItem.m_strFile.substring(dataItem.m_strFile.length() - 4);
                            if (strExt.compareTo(".bnp") == 0)
                                fis = new noteFileInputStream (dataItem.m_strFile);
                            else
                                fis = new FileInputStream (dataItem.m_strFile);
                            bmpFile = BitmapFactory.decodeStream(fis);
                            fis.close();

                            Matrix matBmp = null;
                            try {
                                ExifInterface ei = new ExifInterface(dataItem.m_strFile);
                                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                                switch (orientation) {
                                    case ExifInterface.ORIENTATION_ROTATE_90:
                                        matBmp = new Matrix();
                                        matBmp.postRotate(90);
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_180:
                                        matBmp = new Matrix();
                                        matBmp.postRotate(180);
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_270:
                                        matBmp = new Matrix();
                                        matBmp.postRotate(270);
                                        break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            int nBmpW = bmpFile.getWidth();
                            int nBmpH = bmpFile.getHeight();
                            if (nBmpW * nBmpH > 200 * 200) {
                                float fScale = (float)200 / nBmpW;
                                if (matBmp == null)
                                    matBmp = new Matrix();
                                matBmp.postScale(fScale, fScale);

                            }
                            if (matBmp != null) {
                                dataItem.m_thumb = Bitmap.createBitmap(bmpFile, 0, 0, bmpFile.getWidth(), bmpFile.getHeight(), matBmp, true);
                                bmpFile.recycle();
                            } else {
                                dataItem.m_thumb = bmpFile;
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (dataItem.m_thumb != null && !m_bCanceled) {
                            Message msg = m_updHandler.obtainMessage(MSG_MEDIA_UPDATE_VIEW, i, 0, null);
                            msg.sendToTarget();
                        }
                    }

                    if (m_bCanceled)
                        break;
                }
                m_bFinished = true;
            }
        }).start();
    }

    class updateViewHandler extends Handler {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_MEDIA_UPDATE_VIEW) {
                if (m_lstItems.size() <= msg.arg1)
                    return;
                if (m_lstItems.get(msg.arg1).m_view != null)
                    m_lstItems.get(msg.arg1).m_view.invalidate();
            }
        }
    }
}
