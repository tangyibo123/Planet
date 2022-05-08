package com.tangyibo.planet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tangyibo.framework.entity.Constants;
import com.tangyibo.framework.utils.SpUtils;
import com.tangyibo.planet.MainActivity;
import com.tangyibo.planet.R;

/**
 * 启动页
 * 1. 启动页全屏
 * 2. 延迟进入主页
 * 3. 主页显示哪一个？根据逻辑选择进入引导页还是登录页
 * 4. 适配刘海屏
 *
 */

public class StartActivity extends AppCompatActivity {

    private static final int SKIP_MAIN = 1000;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            //接受到发送来的空消息
            switch(msg.what){
                case SKIP_MAIN:
                    startMain();  //如果是SKIP_MAIN，进入主页
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        //发延迟消息
        mHandler.sendEmptyMessageDelayed(SKIP_MAIN, 2 * 1000);
    }

    /**
     * 进入主页
     */
    private void startMain() {
        SpUtils.getInstance().initSp(this);
        //1. 判断APP是否第一次启动（从安装开始至第一次启动 install - first run）
        boolean isFirstRun = SpUtils.getInstance().getBoolean(Constants.SP_IS_FIRST_RUN, true);//当读取不到时使用默认值
        Intent intent = new Intent();
        if (isFirstRun) {
            //如果是第一次进入，跳到引导页
            intent.setClass(this, GuideActivity.class);
            //同时把标志位置为false
            SpUtils.getInstance().putBoolean(Constants.SP_IS_FIRST_RUN, false);
        }
        else{
            //2. 如果之前已经启动过了，判断是否曾经登录过
            String token = SpUtils.getInstance().getString(Constants.SP_TOKEN, "");
            if(TextUtils.isEmpty(token)) {
                //跳转到登录页
                intent.setClass(this, LoginActivity.class);
            }
            else{
                //跳转到主页
                intent.setClass(this, MainActivity.class);
            }
        }

        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        //引导页无需退出
        //super.onBackPressed();
    }
}
