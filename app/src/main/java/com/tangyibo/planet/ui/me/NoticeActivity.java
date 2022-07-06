package com.tangyibo.planet.ui.me;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tangyibo.framework.base.BaseBackActivity;
import com.tangyibo.framework.helper.UpdateHelper;
import com.tangyibo.planet.R;

public class NoticeActivity extends BaseBackActivity implements View.OnClickListener{

    private LinearLayout ll_know_more;
    private LinearLayout ll_app_intro;
    private LinearLayout ll_advice_feedback;
    private LinearLayout ll_version_update;

    private TextView tv_app_version;
    private TextView tv_new_version;

    private UpdateHelper mUpdateHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        initView();
    }

    private void initView() {

        ll_know_more = (LinearLayout) findViewById(R.id.ll_know_more);
        ll_app_intro = (LinearLayout) findViewById(R.id.ll_app_intro);
        ll_advice_feedback = (LinearLayout) findViewById(R.id.ll_advice_feedback);
        ll_version_update = (LinearLayout) findViewById(R.id.ll_version_update);

        tv_app_version = (TextView) findViewById(R.id.tv_app_version);
        tv_new_version = (TextView) findViewById(R.id.tv_new_version);

        ll_know_more.setOnClickListener(this);
        ll_app_intro.setOnClickListener(this);
        ll_advice_feedback.setOnClickListener(this);
        ll_version_update.setOnClickListener(this);

        try {
            tv_app_version.setText(String.format("Version %s",
                    getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mUpdateHelper = new UpdateHelper(this);
        updateApp();
    }

    private void updateApp() {
        mUpdateHelper.updateApp(isUpdate -> tv_new_version.setVisibility(isUpdate ? View.VISIBLE : View.GONE));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_know_more:
                // 了解更多
            case R.id.ll_app_intro:
                // 功能介绍
                Uri uri = Uri.parse("https://github.com/tangyibo123/Planet");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.ll_advice_feedback:
                // 意见反馈
            case R.id.ll_version_update:
                // 版本更新
                updateApp();
                break;
        }

    }
}
