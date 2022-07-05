package com.tangyibo.framework.base;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.tangyibo.framework.event.EventHelper;
import com.tangyibo.framework.event.MessageEvent;
import com.tangyibo.framework.helper.ActivityHelper;
import com.tangyibo.framework.utils.LanguageUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 *  动态申请危险权限和特殊权限
 */


public class BaseActivity extends AppCompatActivity {

    //申请运行时权限的Code
    private static final int PERMISSION_REQUEST_CODE = 1000;
    //申请窗口权限的Code
    public static final int PERMISSION_WINDOW_REQUEST_CODE = 1001;

    //申明所需权限
    private String[] mStrPermission = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_FINE_LOCATION

    };

    //保存当前未授予的权限
    private List<String> mPerList = new ArrayList<>();
    //保存当前未授予，用户拒绝申请的权限
    private List<String> mPerNoList = new ArrayList<>();

    private PermissionsResult mPermissionsResult;

    // 提供给外部类的动态申请所有权限的接口
    protected void requestDyPermissions(PermissionsResult permissionsResult) {
        if (!checkAllPermissions()) {
            requestAllPermissions(permissionsResult);
        }
    }

    // 告诉外界是否成功申请权限的接口
    protected interface PermissionsResult{
        void OnSuccess();
        void OnFail(List<String> noPermissions);
    }

    // 检查当前是否已经有某个权限
    protected boolean checkSinglePermission(String permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = checkSelfPermission(permissions);
            return check == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    // 检查当前是否已经有所有的权限
    protected boolean checkAllPermissions() {
        mPerList.clear();
        for (int i = 0; i < mStrPermission.length; i++) {
            boolean check = checkSinglePermission(mStrPermission[i]);
            //如果未授权则请求
            if (!check) {
                mPerList.add(mStrPermission[i]);
            }
        }
        return mPerList.size() > 0 ? false : true;
    }

    // 请求权限（单个和多个通用一种方法）
    protected void requestMyPermission(String[] mPermissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Activity请求权限的接口
            requestPermissions(mPermissions, PERMISSION_REQUEST_CODE);
        }
    }

    // 请求所有的权限
    protected void requestAllPermissions(PermissionsResult permissionsResult) {
        mPermissionsResult = permissionsResult; // 把外界传进来的接口实现拿过来
        requestMyPermission(mPerList.toArray(new String[mPerList.size()]));
    }

    // 用户选择拒绝或同意后必会调用的回调函数，我们可以在这个函数里写一些额外的逻辑，比如说打印出拒绝的权限
    // 然后将这些失败权限传给外部类
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mPerNoList.clear();
        // 回调结果和当时申请的code对应上，表示这是当时申请的回调结果
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        // 拒绝权限申请
                        mPerNoList.add(permissions[i]);
                    }
                }
                if (mPermissionsResult != null) {
                    if (mPerNoList.size() == 0) {
                        mPermissionsResult.OnSuccess();
                    } else {
                        mPermissionsResult.OnFail(mPerNoList);
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // 检查判断窗口权限
    protected boolean checkWindowPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    // 请求窗口权限
    protected void requestWindowPermissions() {
        Toast.makeText(this, "申请窗口权限，暂时没做UI交互", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                , Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, PERMISSION_WINDOW_REQUEST_CODE);
    }

    /**
     * EventBus的步骤：
     * 1.注册
     * 2.声明注册方法 onEvent
     * 3.发送事件
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityHelper.getInstance().addActivity(this);

        LanguageUtils.updateLanguaue(this);
        EventHelper.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventHelper.unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()) {
            case EventHelper.EVENT_RUPDATE_LANGUAUE:
                LanguageUtils.updateLanguaue(this);
                recreate();
                break;
        }
    }

}
