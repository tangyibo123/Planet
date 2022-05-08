package com.tangyibo.framework.manager;

import android.content.Context;
import android.view.Gravity;

import com.tangyibo.framework.R;
import com.tangyibo.framework.view.DialogView;

public class DialogManager {

    private static volatile DialogManager mInstance = null;

    public static DialogManager getmInstance() {
        if(mInstance == null){
            synchronized (DialogManager.class){
                if(mInstance == null){
                    mInstance = new DialogManager();
                }
            }
        }
        return mInstance;
    }

    public DialogView initView(Context mContext, int layout){
        //返回一个DialogView的对象，返回类型是DialogView
        return new DialogView(mContext, layout, R.style.Base_Theme_MaterialComponents_Light_Dialog, Gravity.CENTER);
    }

    public DialogView initView(Context mContext, int layout, int gravity){
        return new DialogView(mContext, layout, R.style.Base_Theme_MaterialComponents_Light_Dialog, gravity);
    }

    public void show(DialogView view){
        if(view != null){
            if(!view.isShowing()){
                view.show();
            }
        }
    }

    public void hide(DialogView view){
        if(view != null){
            if(view.isShowing()){
                view.dismiss();
            }
        }
    }


}
