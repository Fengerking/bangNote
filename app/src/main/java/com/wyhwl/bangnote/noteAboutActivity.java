package com.wyhwl.bangnote;

import android.content.pm.PackageManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class noteAboutActivity extends AppCompatActivity
                                    implements View.OnClickListener {
    private TextView        m_txtFeatureInfo = null;
    private TextView        m_txtContactInfo = null;
    private String          m_strVersion = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_about);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        try {
            m_strVersion = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            //bldVer = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }
        initViews ();
    }

    private void initViews () {
        ((ImageButton)findViewById(R.id.imbBack)).setOnClickListener(this);

        m_txtFeatureInfo = (TextView)findViewById(R.id.txtFeatureDetail);
        m_txtContactInfo = (TextView)findViewById(R.id.txtContactInfo);

        m_txtFeatureInfo.setText("1、可以方便插入图片，相机，录音机素材。\n" +
                                 "2、支持远程云备份，同步不同设备。\n" +
                                 "3、支持私密笔记。\n" +
                                 "4、方便搜索，阅读。\n");

        m_txtContactInfo.setText ("1、邮件：fengernote@163.com \n" +
                                  "2、QQ： 2598425114 \n");

        ((TextView)findViewById(R.id.txtVersion)).setText(m_strVersion);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imbBack:
                finish();
                break;
        }
    }
}
