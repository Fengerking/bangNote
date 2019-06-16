package com.wyhwl.bangnote.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.wyhwl.bangnote.view.noteListItemView;

import java.util.ArrayList;
import java.util.Comparator;

public class noteDateAdapter extends BaseAdapter {
    private Context m_context                    = null;
    public ArrayList<dataNoteItem> m_lstItem     = null;

    public noteDateAdapter (Context context) {
        m_context = context;
        m_lstItem = new ArrayList<dataNoteItem>();
    }

    public int getCount() {
        return m_lstItem.size();
    }
    public Object getItem(int arg0) {
        if (m_lstItem.size() <= 0 || arg0 >= m_lstItem.size())
            return null;
        return m_lstItem.get(arg0);
    }
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        noteListItemView itemView = new noteListItemView(m_context);
        itemView.setTextSize(80);
        itemView.setDataList(m_lstItem.get(position));
        return itemView;
    }
}
