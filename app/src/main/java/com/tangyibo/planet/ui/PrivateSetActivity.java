package com.tangyibo.planet.ui;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.tangyibo.framework.base.BaseBackActivity;
import com.tangyibo.framework.bmob.BmobManager;
import com.tangyibo.framework.bmob.PrivateSet;
import com.tangyibo.framework.utils.CommonUtils;
import com.tangyibo.framework.utils.LogUtils;
import com.tangyibo.framework.view.LoadingView;
import com.tangyibo.planet.R;

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * FileName: PrivateSetActivity
 * Profile: 隐私设置
 *
 * 私有库的创建：
 * 1.创建一个BmobObject PrivateSet
 * 2.通过查询PrivateSet里面是否存在自己来判断开关
 * 3.开关的一些操作
 * 打开：则将自己添加到PrivateSet
 * 关闭：则将自己在PrivateSet中删除
 * 4.在查询联系人的时候通过查询PrivateSet过滤
 */
public class PrivateSetActivity extends BaseBackActivity implements View.OnClickListener {

    private Switch sw_kill_contact;

    private LoadingView mLodingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_set);

        initView();
    }

    private void initView() {

        mLodingView = new LoadingView(this);

        sw_kill_contact = (Switch) findViewById(R.id.sw_kill_contact);
        sw_kill_contact.setOnClickListener(this);

        queryPrivateSet();
    }

    //是否选中
    private boolean isCheck = false;

    //当前ID
    private String currentId = "";


    /**
     * 查询私有库
     */
    private void queryPrivateSet() {
        BmobManager.getInstance().queryPrivateSet(new FindListener<PrivateSet>() {
            @Override
            public void done(List<PrivateSet> list, BmobException e) {
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        for (int i = 0; i < list.size(); i++) {
                            PrivateSet set = list.get(i);
                            if (set.getUserId().equals(BmobManager.getInstance().getUser().getObjectId())) {
                                currentId = set.getObjectId();
                                //我存在表中
                                isCheck = true;
                                break;
                            }
                        }
                        LogUtils.i("currentId:" + currentId);
                        sw_kill_contact.setChecked(isCheck);
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sw_kill_contact:
                isCheck = !isCheck;
                sw_kill_contact.setChecked(isCheck);
                if (isCheck) {
                    //添加
                    addPrivateSet();
                } else {
                    //删除
                    delPrivateSet();
                }

                break;
        }
    }

    /**
     * 添加
     */
    private void addPrivateSet() {
        mLodingView.show(getString(R.string.text_private_set_open_ing));
        BmobManager.getInstance().addPrivateSet(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                mLodingView.hide();
                if (e == null) {
                    sw_kill_contact.getTrackDrawable().setColorFilter(
                            new PorterDuffColorFilter(
                                    Color.argb(255,221, 160, 255),
                                    PorterDuff.Mode.MULTIPLY));
                    currentId = s;
                    Toast.makeText(PrivateSetActivity.this, getString(R.string.text_private_set_succeess), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 删除
     */
    private void delPrivateSet() {
        LogUtils.i("delPrivateSet:" + currentId);
        mLodingView.show(getString(R.string.text_private_set_close_ing));
        BmobManager.getInstance().delPrivateSet(currentId, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                mLodingView.hide();
                if (e == null) {
                    sw_kill_contact.getTrackDrawable().setColorFilter(
                            new PorterDuffColorFilter(
                                    Color.argb(255,220, 220, 220),
                                    PorterDuff.Mode.MULTIPLY));
                    Toast.makeText(PrivateSetActivity.this, getString(R.string.text_private_set_fail), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}