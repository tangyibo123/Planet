package com.tangyibo.planet.ui.enter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tangyibo.framework.bmob.BmobManager;
import com.tangyibo.framework.data.Constants;
import com.tangyibo.framework.utils.SpUtils;
import com.tangyibo.planet.MainActivity;
import com.tangyibo.planet.R;
import com.tangyibo.planet.ui.enter.GuideActivity;
import com.tangyibo.planet.ui.enter.LoginActivity;

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
                //3.判断Bmob现在是否登录
                if (BmobManager.getInstance().isLogin()) {
                    //跳转到主页
                    intent.setClass(this, MainActivity.class);
                } else {
                    //跳转到登录页
                    intent.setClass(this, LoginActivity.class);
                }
            }
            else{
                    //跳转到主页
                    intent.setClass(this, MainActivity.class);
            }
        }

        startActivity(intent);
        finish();
    }

    /**
     * 优化
     * 冷启动经过的步骤：
     * 1.第一次安装，加载应用程序并且启动
     * 2.启动后显示一个空白的窗口 getWindow()
     * 3.启动/创建了我们的应用进程
     *
     * App内部：
     * 1.创建App对象/Application对象
     * 2.启动主线程(Main/UI Thread)
     * 3.创建应用入口/LAUNCHER
     * 4.填充ViewGroup中的View
     * 5.绘制View measure -> layout -> draw
     *
     * 优化手段：
     * 1.视图优化
     *   1.设置主题透明
     *   2.设置启动图片
     * 2.代码优化
     *   1.优化Application
     *   2.布局的优化，不需要繁琐的布局
     *   3.阻塞UI线程的操作
     *   4.加载Bitmap/大图
     *   5.其他的一个占用主线程的操作
     *
     *
     * 检测App Activity的启动时间
     * 1.Shell
     *   ActivityManager -> adb shell am start -S -W com.imooc.meet/com.imooc.meet.ui.IndexActivity
     *   ThisTime: 478ms 最后一个Activity的启动耗时
     *   TotalTime: 478ms 启动一连串Activity的总耗时
     *   WaitTime: 501ms 应用创建的时间 + TotalTime
     *   应用创建时间： WaitTime - TotalTime（501 - 478 = 23ms）
     * 2.Log
     *   Android 4.4 开始，ActivityManager增加了Log TAG = displayed
     */

    @Override
    public void onBackPressed() {
        //引导页无需退出
        //super.onBackPressed();
    }
}
