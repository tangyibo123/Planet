package com.tangyibo.framework;

/**
 * FileName: FrameWork
 * Founder: TangYibo
 * Create Date: 2021/11
 * Profile: Framework Enter
 */

public class Framework {

    private volatile static Framework mFramework;

    private Framework(){


    }

    public static Framework getmFramework(){
        if(mFramework == null){
            synchronized (Framework.class){
                if(mFramework == null){
                    mFramework = new Framework();
                }
            }
        }
        return mFramework;
    }


}
