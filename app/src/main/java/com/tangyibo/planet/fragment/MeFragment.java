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
import com.tangyibo.framework.event.EventHelper;
import com.tangyibo.framework.event.MessageEvent;
import com.tangyibo.framework.helper.GlideHelper;
import com.tangyibo.framework.manager.RongCloudManager;
import com.tangyibo.framework.utils.CommonUtils;
import com.tangyibo.framework.utils.LogUtils;
import com.tangyibo.framework.utils.SpUtils;
import com.tangyibo.planet.R;
import com.tangyibo.planet.ui.me.MeInfoActivity;
import com.tangyibo.planet.ui.me.NewFriendActivity;
import com.tangyibo.planet.ui.me.NoticeActivity;
import com.tangyibo.planet.ui.me.PrivateSetActivity;
import com.tangyibo.planet.ui.me.SettingActivity;
import com.tangyibo.planet.ui.me.ShareImgActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

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

        //??????????????????
        RongCloudManager.getInstance().setConnectionStatusListener(connectionStatus -> {
            if(isAdded()){
                if (null != connectionStatus) {
                    LogUtils.i("connectionStatus:" + connectionStatus);
                    if (connectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
                        //????????????
                        tv_server_status.setText(getString(R.string.text_server_status_text_1));
                    } else if (connectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTING) {
                        //?????????
                        tv_server_status.setText(getString(R.string.text_server_status_text_2));
                    } else if (connectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.UNCONNECTED) {
                        //????????????
                        tv_server_status.setText(getString(R.string.text_server_status_text_3));
                    } else if (connectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT) {
                        //???????????????????????????
                    } else if (connectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.NETWORK_UNAVAILABLE) {
                        //???????????????
                        tv_server_status.setText(getString(R.string.text_server_status_text_4));
                    } else if (connectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.TOKEN_INCORRECT) {
                        //Token?????????
                        tv_server_status.setText(getString(R.string.text_server_status_text_6));
                    }
                }
            }
        });
    }

    // ??????????????????????????????
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
            case R.id.iv_me_photo:
                // ????????????
                loadMeInfo();
            case R.id.ll_me_info:
                //????????????
                startActivity(new Intent(getActivity(), MeInfoActivity.class));
                break;
            case R.id.ll_new_friend:
                //?????????
                startActivity(new Intent(getActivity(), NewFriendActivity.class));
                break;
            case R.id.ll_private_set:
                //????????????
                startActivity(new Intent(getActivity(), PrivateSetActivity.class));
                break;
            case R.id.ll_share:
                //??????
                startActivity(new Intent(getActivity(), ShareImgActivity.class));
                break;
            case R.id.ll_notice:
                //??????
                startActivity(new Intent(getActivity(), NoticeActivity.class));
                break;
            case R.id.ll_setting:
                //??????
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()) {
            case EventHelper.EVENT_REFRE_ME_INFO:
                loadMeInfo();
                break;
        }
    }
}
