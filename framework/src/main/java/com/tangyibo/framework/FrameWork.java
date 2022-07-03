package com.tangyibo.framework;

import android.content.Context;

import com.tangyibo.framework.bmob.BmobManager;
import com.tangyibo.framework.manager.KeywordManager;
import com.tangyibo.framework.manager.MyWindowManager;
import com.tangyibo.framework.manager.RongCloudManager;
import com.tangyibo.framework.utils.LogUtils;
import com.tangyibo.framework.utils.SpUtils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.litepal.LitePal;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * FileName: FrameWork
 * Founder: TangYibo
 * Create Date: 2021/11
 * Profile: Framework Enter
 */

public class FrameWork {

    private volatile static FrameWork mFramework;

    // 单例模式
    public static FrameWork getFramework(){
        if(mFramework == null){
            synchronized (FrameWork.class){
                if(mFramework == null){
                    mFramework = new FrameWork();
                }
            }
        }
        return mFramework;
    }

    /**
     * 初始化框架 Model
     *
     * @param mContext
     */
    public void initFramework(Context mContext) {
        LogUtils.i("initFramework");
        SpUtils.getInstance().initSp(mContext);
        BmobManager.getInstance().initBmob(mContext);
//        RongCloudManager.getInstance().initRongCloud(mContext);
        LitePal.initialize(mContext);
//        MapManager.getInstance().initMap(mContext);
        MyWindowManager.getInstance().initWindow(mContext);
//        CrashReport.initCrashReport(mContext, BUGLY_KEY, BuildConfig.LOG_DEBUG);
        ZXingLibrary.initDisplayOpinion(mContext);
//        NotificationHelper.getInstance().createChannel(mContext);
        KeywordManager.getInstance().initManager(mContext);

//        //全局捕获RxJava异常
//        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
//            @Override
//            public void accept(Throwable throwable) throws Exception {
//                LogUtils.e("RxJava：" + throwable.toString());
//            }
//        });
    }

}
