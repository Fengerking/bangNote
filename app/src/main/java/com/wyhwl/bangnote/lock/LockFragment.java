package com.wyhwl.bangnote.lock;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;

import me.yokeyword.fragmentation.SupportFragment;
import com.wyhwl.bangnote.noteApplication;
import com.wyhwl.bangnote.R;

public class LockFragment extends SupportFragment
                        implements View.OnClickListener{
    private GestureLockLayout mGestureLockLayout;
    private TextView mHintText;
    private Handler mHandler = new Handler();

    public static LockFragment newInstance() {

        Bundle args = new Bundle();

        LockFragment fragment = new LockFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lock,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initEvents();
    }

    private void initViews(View view) {
        ((ImageButton)view.findViewById(R.id.imbBack)).setOnClickListener(this);
        mGestureLockLayout = (GestureLockLayout) view.findViewById(R.id.l_lock_view);
        mHintText = (TextView) view.findViewById(R.id.tv_hint);
        //设置手势解锁模式为验证模式
        mGestureLockLayout.setMode(GestureLockLayout.VERIFY_MODE);
        //设置手势解锁每行每列点的个数
        mGestureLockLayout.setDotCount(3);
        //设置手势解锁最大尝试次数 默认 5
        mGestureLockLayout.setTryTimes(3);
        //设置手势解锁正确答案
        mGestureLockLayout.setAnswer(noteApplication.getInstance().m_strLockKey);
    }

    private void initEvents() {
        mGestureLockLayout.setOnLockVerifyListener(new GestureLockLayout.OnLockVerifyListener() {
            @Override
            public void onGestureSelected(int id) {
                //每选中一个点时调用
            }

            @Override
            public void onGestureFinished(boolean isMatched) {
                //绘制手势解锁完成时调用

                if (isMatched) {
                    //密码匹配
                    noteApplication.getInstance().m_isUnlock = true;
                    _mActivity.finish();
                } else {
                    //不匹配
                    mHintText.setText("还有" + mGestureLockLayout.getTryTimes() + "次机会");
                    resetGesture();
                }
            }

            @Override
            public void onGestureTryTimesBoundary() {
                //超出最大尝试次数时调用

                mGestureLockLayout.setTouchable(false);
            }
        });
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imbBack:
                _mActivity.finish();
                break;
        }
    }

    private void resetGesture() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGestureLockLayout.resetGesture();
            }
        }, 200);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
