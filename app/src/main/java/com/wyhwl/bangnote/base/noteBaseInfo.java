package com.wyhwl.bangnote.base;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;

public class noteBaseInfo {
    private Context     m_context = null;

    public noteBaseInfo (Context context) {
        m_context = context;
    }

    public void getTodayWeather () {
        SharedPreferences settings = m_context.getSharedPreferences("note_Setting", 0);
        String strLastDay = settings.getString("weatherLastDay", "");

        SimpleDateFormat fmtDate = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        String strToday = fmtDate.format(date);
        if (strToday.compareTo(strLastDay) == 0) {
            noteConfig.m_strCityName = settings.getString("weatherCity", "未知");
            noteConfig.m_strWeather = settings.getString("weatherToday", "未知");
            return;
        }

        String strURL = "https://www.tianqiapi.com/api/?version=v1";
        OkHttpUtils
                .get().url(strURL).id(101)
                .build().execute(new httpDataCallBack());
    }

    public class httpDataCallBack extends StringCallback {
        public void onError(Call call, Exception e, int id) {
        }

        public void onResponse(String response, int id) {
            if (id == 101) {
                JSONObject jsnResult = JSON.parseObject(response);
                noteConfig.m_strCityName = jsnResult.getString("city");
                JSONArray jsnData = jsnResult.getJSONArray("data");
                JSONObject  jsnToday = jsnData.getJSONObject(0);
                noteConfig.m_strWeather = jsnToday.getString("wea");
                String strTemp1 = jsnToday.getString("tem2");
                String strTemp2 = jsnToday.getString("tem1");
                noteConfig.m_strWeather += " " + strTemp1 + "-" + strTemp2;

                SharedPreferences settings = m_context.getSharedPreferences("note_Setting", 0);
                SharedPreferences.Editor editor = settings.edit();

                SimpleDateFormat fmtDate = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date(System.currentTimeMillis());
                String strToday = fmtDate.format(date);
                editor.putString("weatherLastDay", strToday);
                editor.putString("weatherCity", noteConfig.m_strCityName);
                editor.putString("weatherToday", noteConfig.m_strWeather);
                editor.commit();
            }
        }
    }

}
