package com.tangyibo.framework.manager;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

/**
 * FileName: KeyWordManager
 * Profile: 软键盘管理类
 */

public class KeywordManager {
    private Context mContext;
    private InputMethodManager imm;

    private static volatile KeywordManager mInstance = null;

    private KeywordManager() {

    }

    public static KeywordManager getInstance() {
        if (mInstance == null) {
            synchronized (KeywordManager.class) {
                if (mInstance == null) {
                    mInstance = new KeywordManager();
                }
            }
        }
        return mInstance;
    }

    public void initManager(Context mContext) {
        this.mContext = mContext;
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    /**
     * 隐藏软键盘
     *
     * @param mActivity
     */
    public void hideKeyWord(Activity mActivity) {
        if (mActivity != null && !mActivity.isDestroyed()) {
            if (imm != null) {
                imm.hideSoftInputFromWindow(mActivity.getWindow().getDecorView().getWindowToken(), 0);
            }
        }
    }
}
