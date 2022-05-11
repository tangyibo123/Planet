package com.tangyibo.planet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tangyibo.framework.base.BaseUIActivity;
import com.tangyibo.framework.bmob.BmobManager;
import com.tangyibo.framework.manager.DialogManager;
import com.tangyibo.framework.manager.MediaPlayerManager;
import com.tangyibo.framework.utils.LogUtils;
import com.tangyibo.framework.utils.SpUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //星球
    private ImageView iv_home;
    private TextView tv_home;
    private LinearLayout ll_home;
    //private StarFragment mStarFragment = null;
    //private FragmentTransaction mStarTransaction = null;

    //广场
    private ImageView iv_square;
    private TextView tv_square;
    private LinearLayout ll_square;
    //private SquareFragment mSquareFragment = null;
    //private FragmentTransaction mSquareTransaction = null;

    //聊天
    private ImageView iv_chat;
    private TextView tv_chat;
    private LinearLayout ll_chat;
    //private ChatFragment mChatFragment = null;
    //private FragmentTransaction mChatTransaction = null;

    //我的
    private ImageView iv_me;
    private TextView tv_me;
    private LinearLayout ll_me;
    //private MeFragment mMeFragment = null;
    //private FragmentTransaction mMeTransaction = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    /**
     * 初始化所有View
     */
    private void initView() {

        iv_home = (ImageView) findViewById(R.id.iv_home);
        tv_home = (TextView) findViewById(R.id.tv_home);
        ll_home = (LinearLayout) findViewById(R.id.ll_home);

        iv_square = (ImageView) findViewById(R.id.iv_square);
        tv_square = (TextView) findViewById(R.id.tv_square);
        ll_square = (LinearLayout) findViewById(R.id.ll_square);

        iv_chat = (ImageView) findViewById(R.id.iv_chat);
        tv_chat = (TextView) findViewById(R.id.tv_chat);
        ll_chat = (LinearLayout) findViewById(R.id.ll_chat);

        iv_me = (ImageView) findViewById(R.id.iv_me);
        tv_me = (TextView) findViewById(R.id.tv_me);
        ll_me = (LinearLayout) findViewById(R.id.ll_me);

        ll_home.setOnClickListener(this);
        ll_square.setOnClickListener(this);
        ll_chat.setOnClickListener(this);
        ll_me.setOnClickListener(this);

        //切换默认的选项卡
        checkMainTab(0);

    }

    /**
     * 切换主页选项卡
     *
     * @param index 0：主页
     *              1：广场
     *              2：聊天
     *              3：我的
     */
    private void checkMainTab(int index) {
        switch (index) {
            case 0:
                //showFragment(mStarFragment);
                iv_home.setAlpha((float) 0.9);
                tv_home.setAlpha((float) 0.9);
                iv_square.setAlpha((float) 0.5);
                tv_square.setAlpha((float) 0.5);
                iv_chat.setAlpha((float) 0.5);
                tv_chat.setAlpha((float) 0.5);
                iv_me.setAlpha((float) 0.5);
                tv_me.setAlpha((float) 0.5);
                break;
            case 1:
                //showFragment(mSquareFragment);
                iv_home.setAlpha((float) 0.5);
                tv_home.setAlpha((float) 0.5);
                iv_square.setAlpha((float) 0.9);
                tv_square.setAlpha((float) 0.9);
                iv_chat.setAlpha((float) 0.5);
                tv_chat.setAlpha((float) 0.5);
                iv_me.setAlpha((float) 0.5);
                tv_me.setAlpha((float) 0.5);
                break;
            case 2:
                //showFragment(mChatFragment);
                iv_home.setAlpha((float) 0.5);
                tv_home.setAlpha((float) 0.5);
                iv_square.setAlpha((float) 0.5);
                tv_square.setAlpha((float) 0.5);
                iv_chat.setAlpha((float) 0.9);
                tv_chat.setAlpha((float) 0.9);
                iv_me.setAlpha((float) 0.5);
                tv_me.setAlpha((float) 0.5);
                break;
            case 3:
                //showFragment(mMeFragment);
                iv_home.setAlpha((float) 0.5);
                tv_home.setAlpha((float) 0.5);
                iv_square.setAlpha((float) 0.5);
                tv_square.setAlpha((float) 0.5);
                iv_chat.setAlpha((float) 0.5);
                tv_chat.setAlpha((float) 0.5);
                iv_me.setAlpha((float) 0.9);
                tv_me.setAlpha((float) 0.9);
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_home:
                checkMainTab(0);
                break;
            case R.id.ll_square:
                checkMainTab(1);
                break;
            case R.id.ll_chat:
                checkMainTab(2);
                break;
            case R.id.ll_me:
                checkMainTab(3);
                break;
        }

    }
}