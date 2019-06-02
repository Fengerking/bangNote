package com.wyhwl.bangnote;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class noteBackupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_backup);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

    }
}
