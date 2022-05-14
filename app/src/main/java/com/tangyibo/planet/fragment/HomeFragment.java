package com.tangyibo.planet.fragment;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.moxun.tagcloudlib.view.TagCloudView;
import com.tangyibo.framework.base.BaseFragment;
import com.tangyibo.framework.bmob.PlanetUser;
import com.tangyibo.framework.manager.DialogManager;
import com.tangyibo.framework.view.DialogView;
import com.tangyibo.planet.R;

import com.tangyibo.framework.view.LoadingView;
import com.tangyibo.planet.adapter.CloudTagAdapter;
import com.tangyibo.planet.model.PlanetModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends BaseFragment implements View.OnClickListener {


    //二维码结果
    private static final int REQUEST_CODE = 1235;

    private TextView tv_star_title;
    private ImageView iv_camera;
    private ImageView iv_add;

    private TagCloudView mCloudView;

    private LinearLayout ll_random;
    private LinearLayout ll_soul;
    private LinearLayout ll_fate;
    private LinearLayout ll_love;

    private CloudTagAdapter mCloudTagAdapter;
    private List<PlanetModel> mStarList = new ArrayList<>();

    private LoadingView mLoadingView;

    private DialogView mNullDialogView;
    private TextView tv_null_text;
    private TextView tv_null_cancel;

    //连接状态
    private TextView tv_connect_status;

    //全部好友
    private List<PlanetUser> mAllUserList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, null);
        initView(view);
        return view;
    }

    /**
     * 初始化View
     *
     * @param view
     */
    private void initView(View view) {

        mLoadingView = new LoadingView(getActivity());
        mLoadingView.setCancelable(false);

        mNullDialogView = DialogManager.getmInstance().initView(getActivity(), R.layout.layout_star_null_item, Gravity.BOTTOM);
        tv_null_text = mNullDialogView.findViewById(R.id.tv_null_text);
        tv_null_cancel = mNullDialogView.findViewById(R.id.tv_cancel);
        tv_null_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogManager.getmInstance().hide(mNullDialogView);
            }
        });

        iv_camera = view.findViewById(R.id.iv_camera);
        iv_add = view.findViewById(R.id.iv_add);
        tv_connect_status = view.findViewById(R.id.tv_connect_status);

        tv_star_title = view.findViewById(R.id.tv_star_title);

        mCloudView = view.findViewById(R.id.mCloudView);

        ll_random = view.findViewById(R.id.ll_random);
        ll_soul = view.findViewById(R.id.ll_soul);
        ll_fate = view.findViewById(R.id.ll_fate);
        ll_love = view.findViewById(R.id.ll_love);

        iv_camera.setOnClickListener(this);
        iv_add.setOnClickListener(this);

        ll_random.setOnClickListener(this);
        ll_soul.setOnClickListener(this);
        ll_fate.setOnClickListener(this);
        ll_love.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

    }
}
