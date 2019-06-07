package com.wyhwl.bangnote.lock;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import com.wyhwl.bangnote.R;
import com.wyhwl.bangnote.noteApplication;

public class LockSettingActivity extends AppCompatActivity
                                    implements View.OnClickListener {

    private GestureLockLayout       mGestureLockLayout;
    private GestureLockDisplayView  mLockDisplayView;
    private TextView                mSettingHintText;
    private Handler                 mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_setting);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        initViews();
        initEvents();
    }

    private void initViews() {
        ((ImageButton)findViewById(R.id.imbBack)).setOnClickListener(this);

        mGestureLockLayout = (GestureLockLayout) findViewById(R.id.l_gesture_view);
        mLockDisplayView = (GestureLockDisplayView) findViewById(R.id.l_display_view);
        mSettingHintText = (TextView) findViewById(R.id.tv_setting_hint);
        //设置提示view 每行每列点的个数
        mLockDisplayView.setDotCount(3);
        //设置提示view 选中状态的颜色
        mLockDisplayView.setDotSelectedColor(Color.parseColor("#01A0E5"));
        //设置提示view 非选中状态的颜色
        mLockDisplayView.setDotUnSelectedColor(Color.TRANSPARENT);
        //设置手势解锁view 每行每列点的个数
        mGestureLockLayout.setDotCount(3);
        //设置手势解锁view 最少连接数
        mGestureLockLayout.setMinCount(3);
        //默认解锁样式为手Q手势解锁样式
        mGestureLockLayout.setLockView(new LockViewFactory() {
            @Override
            public ILockView newLockView() {
                return new JDLockView(LockSettingActivity.this);
            }
        });
        //设置手势解锁view 模式为重置密码模式
        mGestureLockLayout.setMode(GestureLockLayout.RESET_MODE);
    }

    private void initEvents() {
        mGestureLockLayout.setOnLockResetListener(new GestureLockLayout.OnLockResetListener() {
            @Override
            public void onConnectCountUnmatched(int connectCount, int minCount) {
                //连接数小于最小连接数时调用

                mSettingHintText.setText("最少连接" + minCount + "个点");
                resetGesture();
            }

            @Override
            public void onFirstPasswordFinished(List<Integer> answerList) {
                //第一次绘制手势成功时调用
                mSettingHintText.setText("再次输入确认激活密记图案");
                //将答案设置给提示view
                mLockDisplayView.setAnswer(answerList);
                //重置
                resetGesture();
            }

            @Override
            public void onSetPasswordFinished(boolean isMatched, List<Integer> answerList) {
                //第二次密码绘制成功时调用

                if (isMatched) {
                    //两次答案一致，保存
                    noteApplication.getInstance().m_strLockKey = answerList.toString();
                    noteApplication.getInstance().m_isUnlock = false;
                    finish();
                } else {
                    mSettingHintText.setText("绘制错误，请再次绘制图案");
                    resetGesture();
                }
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imbBack:
                finish();
                break;
        }
    }

    /**
     * 重置
     */
    private void resetGesture() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGestureLockLayout.resetGesture();
            }
        }, 200);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
