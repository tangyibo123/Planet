package com.tangyibo.planet.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tangyibo.framework.base.BaseFragment;
import com.tangyibo.framework.bmob.BmobManager;
import com.tangyibo.framework.bmob.PlanetUser;
import com.tangyibo.framework.data.Constants;
import com.tangyibo.framework.helper.GlideHelper;
import com.tangyibo.framework.manager.RongCloudManager;
import com.tangyibo.framework.utils.CommonUtils;
import com.tangyibo.framework.utils.LogUtils;
import com.tangyibo.framework.utils.SpUtils;
import com.tangyibo.planet.R;
import com.tangyibo.planet.ui.MeInfoActivity;
import com.tangyibo.planet.ui.NewFriendActivity;
import com.tangyibo.planet.ui.NoticeActivity;
import com.tangyibo.planet.ui.PrivateSetActivity;
import com.tangyibo.planet.ui.SettingActivity;
import com.tangyibo.planet.ui.ShareImgActivity;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;
import io.rong.imlib.RongIMClient;

public class MeFragment extends BaseFragment implements View.OnClickListener {
    private CircleImageView iv_me_photo;
    private TextView tv_nickname;
    private LinearLayout ll_me_info;
    private LinearLayout ll_new_friend;
    private LinearLayout ll_private_set;
    private LinearLayout ll_share;
    private LinearLayout ll_setting;
    private LinearLayout ll_notice;

    private TextView tv_server_status;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, null);
        initView(view);
        return view;
        
    }

    private void initView(View view) {
        iv_me_photo = view.findViewById(R.id.iv_me_photo);
        tv_nickname = view.findViewById(R.id.tv_nickname);

        ll_me_info = view.findViewById(R.id.ll_me_info);
        ll_new_friend = view.findViewById(R.id.ll_new_friend);
        ll_private_set = view.findViewById(R.id.ll_private_set);
        ll_share = view.findViewById(R.id.ll_share);
        ll_setting = view.findViewById(R.id.ll_setting);
        ll_notice = view.findViewById(R.id.ll_notice);
        tv_server_status = view.findViewById(R.id.tv_server_status);

        ll_me_info.setOnClickListener(this);
        ll_new_friend.setOnClickListener(this);
        ll_private_set.setOnClickListener(this);
        ll_share.setOnClickListener(this);
        ll_setting.setOnClickListener(this);
        ll_notice.setOnClickListener(this);

        loadMeInfo();

        //监听连接状态
        RongCloudManager.getInstance().setConnectionStatusListener(connectionStatus -> {
            if(isAdded()){
                if (null != connectionStatus) {
                    LogUtils.i("connectionStatus:" + connectionStatus);
                    if (connectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
                        //连接成功
                        tv_server_status.setText(getString(R.string.text_server_status_text_1));
                    } else if (connectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTING) {
                        //连接中
                        tv_server_status.setText(getString(R.string.text_server_status_text_2));
                    } else if (connectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.UNCONNECTED) {
                        //断开连接
                        tv_server_status.setText(getString(R.string.text_server_status_text_3));
                    } else if (connectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT) {
                        //用户在其他地方登陆
                    } else if (connectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.NETWORK_UNAVAILABLE) {
                        //网络不可用
                        tv_server_status.setText(getString(R.string.text_server_status_text_4));
                    } else if (connectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.TOKEN_INCORRECT) {
                        //Token不正确
                        tv_server_status.setText(getString(R.string.text_server_status_text_6));
                    }
                }
            }
        });
    }

    // 加载本账号的个人信息
    private void loadMeInfo() {
        BmobManager.getInstance().queryPhoneUser(SpUtils.getInstance().getString(Constants.SP_PHONE, ""), new FindListener<PlanetUser>() {
            @Override
            public void done(List<PlanetUser> list, BmobException e) {
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        PlanetUser imUser = list.get(0);
                        if (!TextUtils.isEmpty(imUser.getPhoto())) {
                            GlideHelper.loadSmollUrl(getActivity(), imUser.getPhoto(), 150, 150, iv_me_photo);
                        }
                        tv_nickname.setText(imUser.getNickName());
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_me_info:
                //个人信息
                startActivity(new Intent(getActivity(), MeInfoActivity.class));
                break;
            case R.id.ll_new_friend:
                //新朋友
                startActivity(new Intent(getActivity(), NewFriendActivity.class));
                break;
            case R.id.ll_private_set:
                //隐私设置
                startActivity(new Intent(getActivity(), PrivateSetActivity.class));
                break;
            case R.id.ll_share:
                //分享
                startActivity(new Intent(getActivity(), ShareImgActivity.class));
                break;
            case R.id.ll_notice:
                //通知
                startActivity(new Intent(getActivity(), NoticeActivity.class));
                break;
            case R.id.ll_setting:
                //设置
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
        }
    }
}
