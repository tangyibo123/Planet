package com.tangyibo.framework.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.tangyibo.framework.R;
import com.tangyibo.framework.manager.DialogManager;
import com.tangyibo.framework.utils.AnimUtils;
import com.tangyibo.framework.utils.LogUtils;

public class LoadingView {

    private DialogView mLoadingView;
    private ImageView iv_loading;
    private TextView tv_loading_text;
    private ObjectAnimator mAnim;

    public LoadingView(Context mContext){
        mLoadingView = DialogManager.getmInstance().initView(mContext, R.layout.dialog_loading);
        iv_loading = mLoadingView.findViewById(R.id.iv_loading);
        tv_loading_text = mLoadingView.findViewById(R.id.tv_loading_text);
        mAnim = AnimUtils.rotation(iv_loading);
    }

    /**
     * 设置加载的提示文本
     *
     * @param text
     */
    public void setLoadingText(String text) {
        if (!TextUtils.isEmpty(text)) {
            tv_loading_text.setText(text);
        }
        else{
            LogUtils.d("load text is null!");
        }
    }

    public void show() {
        mAnim.start();
        DialogManager.getmInstance().show(mLoadingView);
    }

    public void show(String text) {
        mAnim.start();
        setLoadingText(text);
        DialogManager.getmInstance().show(mLoadingView);
    }

    public void hide() {
        mAnim.pause();
        DialogManager.getmInstance().hide(mLoadingView);
    }

    /**
     * 外部是否可以点击消失
     *
     * @param flag
     */
    public void setCancelable(boolean flag) {
        mLoadingView.setCancelable(flag);
    }

}
