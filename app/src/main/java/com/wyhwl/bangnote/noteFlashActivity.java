package com.wyhwl.bangnote;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.wyhwl.bangnote.base.noteConfig;

public class noteFlashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_flash);

        noteConfig.initConfig(this);

        openNoteActivity();
    }

    private void openNoteActivity () {
        Intent intent = new Intent(noteFlashActivity.this, noteListActivity.class);
        startActivity(intent);
        finish();
    }

}
