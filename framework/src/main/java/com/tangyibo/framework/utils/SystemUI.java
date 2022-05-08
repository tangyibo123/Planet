package com.tangyibo.framework.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import com.tangyibo.framework.R;

/**
 * FileName: SystemUI
 * Founder: TangYibo
 * Profile: 沉浸式状态栏实现
 */

public class SystemUI {

    public static void fixSystemUI(Activity mActivity){
        //Android 6.0 以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //如果遇到浅色背景可以加这个参数 View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR设置字体为深灰色
            mActivity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        else{
            //Android 5.0-6.0
            //绘制透明的状态栏（最上）和导航栏（最下）
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //注意要清除 FLAG_TRANSLUCENT_STATUS flag
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //给透明状态栏填充颜色
            mActivity.getWindow().setStatusBarColor(mActivity.getResources().getColor(R.color.green));
            //给透明导航栏填充颜色
            //getWindow().setNavigationBarColor(getResources().getColor(R.color.green));
        }
    }
}
