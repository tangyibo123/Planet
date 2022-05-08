package com.tangyibo.framework.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

public class DialogView extends Dialog {

    public DialogView(Context mContext, int layout, int style, int gravity){
        super(mContext, style);
        //显示传入的dialog布局
        setContentView(layout);
        //获取最顶层View（dialog）
        Window window = getWindow();
        //获取最顶层View的属性
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        //重置dialog的宽高重心属性
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = gravity;
        //设置生效
        window.setAttributes(layoutParams);
    }

}
