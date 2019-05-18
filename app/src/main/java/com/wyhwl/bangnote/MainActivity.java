package com.wyhwl.bangnote;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {
    public static final int     REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private Button              m_btnTest = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.mipmap.ic_launcher);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        CheckWritePermission(true);
        noteConfig.initConfig(this);

        m_btnTest = (Button)findViewById(R.id.btnTest);
        m_btnTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, noteListActivity.class);
                //intent.setData(Uri.parse(noteConfig.m_strNoteTextPath + "txt_2019-05-17-14-57-19.bnt"));
                startActivity(intent);
            }
        });
    }


    private boolean CheckWritePermission (boolean bInit) {
        if (bInit) {
            //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "请开通文件读写权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
                }
                //申请权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);

            } else {
                //Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();
                return true;
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "请开通文件读写权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    public native String stringFromJNI();

    static {
        System.loadLibrary("native-lib");
    }
}
