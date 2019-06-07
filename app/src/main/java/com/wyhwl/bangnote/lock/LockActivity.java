package com.wyhwl.bangnote.lock;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ImageButton;

import me.yokeyword.fragmentation.SupportActivity;
import com.wyhwl.bangnote.R;

public class LockActivity extends SupportActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        loadRootFragment(R.id.fl_fragment_container,LockFragment.newInstance());
    }

}
