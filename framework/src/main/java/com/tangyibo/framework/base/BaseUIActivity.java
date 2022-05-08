package com.tangyibo.framework.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tangyibo.framework.utils.SystemUI;

/**
 * FileName: BaseUIActivity
 * Founder: TangYibo
 * Profile: 所有activity置为沉浸式状态栏
 */
public class BaseUIActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemUI.fixSystemUI(this);
    }
}
