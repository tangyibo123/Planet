package com.tangyibo.planet.ui.me;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.tangyibo.framework.base.BaseBackActivity;
import com.tangyibo.framework.data.Constants;
import com.tangyibo.framework.helper.ActivityHelper;
import com.tangyibo.framework.manager.DialogManager;
import com.tangyibo.framework.manager.RongCloudManager;
import com.tangyibo.framework.utils.LanguageUtils;
import com.tangyibo.framework.utils.SpUtils;
import com.tangyibo.framework.view.DialogView;
import com.tangyibo.planet.R;
import com.tangyibo.planet.service.CloudService;
import com.tangyibo.planet.ui.chat.ChatThemeActivity;
import com.tangyibo.planet.ui.enter.LoginActivity;

import cn.bmob.v3.BmobUser;

public class SettingActivity extends BaseBackActivity implements View.OnClickListener {
    private Switch sw_app_tips;
    private RelativeLayout rl_app_tips;
    private TextView tv_cache_size;
    private RelativeLayout rl_clear_cache;
    private TextView tv_current_languaue;
    private RelativeLayout rl_update_languaue;
    private RelativeLayout rl_check_permissions;
    private Button btn_logout;
    private RelativeLayout rl_chat_theme;

    private DialogView mLanguaueDialog;
    private TextView tv_zh;
    private TextView tv_en;
    private TextView tv_cancel;

    private boolean isTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initView();
    }

    private void initView() {

        sw_app_tips = (Switch) findViewById(R.id.sw_app_tips);
        rl_app_tips = (RelativeLayout) findViewById(R.id.rl_app_tips);
        tv_cache_size = (TextView) findViewById(R.id.tv_cache_size);
        rl_clear_cache = (RelativeLayout) findViewById(R.id.rl_clear_cache);
        tv_current_languaue = (TextView) findViewById(R.id.tv_current_languaue);
        rl_update_languaue = (RelativeLayout) findViewById(R.id.rl_update_languaue);
        rl_check_permissions = (RelativeLayout) findViewById(R.id.rl_check_permissions);
        rl_chat_theme = (RelativeLayout) findViewById(R.id.rl_chat_theme);
        btn_logout = (Button) findViewById(R.id.btn_logout);

        btn_logout.setOnClickListener(this);
        rl_clear_cache.setOnClickListener(this);
        rl_app_tips.setOnClickListener(this);
        rl_update_languaue.setOnClickListener(this);
        rl_check_permissions.setOnClickListener(this);
        rl_chat_theme.setOnClickListener(this);

        isTips = SpUtils.getInstance().getBoolean("isTips", true);
        sw_app_tips.setChecked(isTips);

        tv_cache_size.setText("0.0 MB");

        int languaue = SpUtils.getInstance().getInt(Constants.SP_LANGUAUE, 0);
        tv_current_languaue.setText(languaue == 1 ? getString(R.string.text_setting_en) : getString(R.string.text_setting_zh));
        initLanguaueDialog();
    }


    private void initLanguaueDialog() {
        mLanguaueDialog = DialogManager.getInstance().initView(this, R.layout.dialog_select_photo, Gravity.BOTTOM);
        tv_zh = (TextView) mLanguaueDialog.findViewById(R.id.tv_camera);
        tv_en = (TextView) mLanguaueDialog.findViewById(R.id.tv_ablum);
        tv_cancel = (TextView) mLanguaueDialog.findViewById(R.id.tv_cancel);

        tv_zh.setText(getString(R.string.text_setting_zh));
        tv_en.setText(getString(R.string.text_setting_en));

        tv_zh.setOnClickListener(view -> {
            selectLanguaue(0);
            DialogManager.getInstance().hide(mLanguaueDialog);
        });

        tv_en.setOnClickListener(view -> {
            selectLanguaue(1);
            DialogManager.getInstance().hide(mLanguaueDialog);
        });

        tv_cancel.setOnClickListener(view -> DialogManager.getInstance().hide(mLanguaueDialog));
    }

    /**
     * @param index
     */
    private void selectLanguaue(int index) {
        if (LanguageUtils.SYS_LANGUAGE == index) {
            return;
        }
        SpUtils.getInstance().putInt(Constants.SP_LANGUAUE, index);
        //EventManager.post(EventManager.EVENT_RUPDATE_LANGUAUE);
        Toast.makeText(this, "Test Model , Reboot App ", Toast.LENGTH_SHORT).show();
        //?????????????????????
        finishAffinity();
        System.exit(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_app_tips:
                isTips = !isTips;
                sw_app_tips.setChecked(isTips);
                SpUtils.getInstance().putBoolean("isTips", isTips);
                break;
            case R.id.rl_chat_theme:
                startActivity(new Intent(this, ChatThemeActivity.class));
                break;
            case R.id.rl_clear_cache:

                break;
            case R.id.rl_update_languaue:
                DialogManager.getInstance().show(mLanguaueDialog);
                break;
            case R.id.rl_check_permissions:
                openWindow();
                break;
            case R.id.btn_logout:
                /**
                 * ?????????????????????
                 * 1.???????????????????????????????????????Activity
                 * 2.??????Token
                 * 3.??????Bmob
                 * 4.??????????????????
                 * 5.??????????????????
                 * 7.???????????????
                 */
                logout();
                break;
        }
    }

    /**
     * ????????????
     */
    private void logout() {

        //??????Token
        SpUtils.getInstance().deleteKey(Constants.SP_TOKEN);
        //Bmob????????????
        BmobUser.logOut();
        //??????
        RongCloudManager.getInstance().disconnect();
        RongCloudManager.getInstance().logout();

        ActivityHelper.getInstance().exit();

        stopService(new Intent(this, CloudService.class));

        //??????????????????
        Intent intent_login = new Intent();
        intent_login.setClass(SettingActivity.this, LoginActivity.class);
        intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent_login);
        finish();
    }

    private void openWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                            , Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "?????????????????????", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "?????????????????????", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
