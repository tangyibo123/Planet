package com.tangyibo.planet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
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
import com.tangyibo.planet.fragment.ChatFragment;
import com.tangyibo.planet.fragment.HomeFragment;
import com.tangyibo.planet.fragment.MeFragment;
import com.tangyibo.planet.fragment.SquareFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //星球
    private ImageView iv_home;
    private TextView tv_home;
    private LinearLayout ll_home;
    private HomeFragment mHomeFragment = null;
    private FragmentTransaction mHomeTransaction = null;

    //广场
    private ImageView iv_square;
    private TextView tv_square;
    private LinearLayout ll_square;
    private SquareFragment mSquareFragment = null;
    private FragmentTransaction mSquareTransaction = null;

    //聊天
    private ImageView iv_chat;
    private TextView tv_chat;
    private LinearLayout ll_chat;
    private ChatFragment mChatFragment = null;
    private FragmentTransaction mChatTransaction = null;

    //我的
    private ImageView iv_me;
    private TextView tv_me;
    private LinearLayout ll_me;
    private MeFragment mMeFragment = null;
    private FragmentTransaction mMeTransaction = null;

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

        //初始化fragment
        initFragment();

        //切换默认的选项卡
        checkMainTab(0);

    }

    /**
     * 初始化Fragment
     */
    private void initFragment() {

        //星球
        if (mHomeFragment == null) {
            mHomeFragment = new HomeFragment();
            mHomeTransaction = getSupportFragmentManager().beginTransaction();
            mHomeTransaction.add(R.id.mMainLayout, mHomeFragment);
            mHomeTransaction.commit();
        }

        //广场
        if (mSquareFragment == null) {
            mSquareFragment = new SquareFragment();
            mSquareTransaction = getSupportFragmentManager().beginTransaction();
            mSquareTransaction.add(R.id.mMainLayout, mSquareFragment);
            mSquareTransaction.commit();
        }

        //聊天
        if (mChatFragment == null) {
            mChatFragment = new ChatFragment();
            mChatTransaction = getSupportFragmentManager().beginTransaction();
            mChatTransaction.add(R.id.mMainLayout, mChatFragment);
            mChatTransaction.commit();
        }

        //我的
        if (mMeFragment == null) {
            mMeFragment = new MeFragment();
            mMeTransaction = getSupportFragmentManager().beginTransaction();
            mMeTransaction.add(R.id.mMainLayout, mMeFragment);
            mMeTransaction.commit();
        }
    }

    /**
     * 显示Fragment
     *
     * @param fragment
     */
    private void showFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            hideAllFragment(transaction);
            transaction.show(fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    /**
     * 隐藏所有的Fragment
     *
     * @param transaction
     */
    private void hideAllFragment(FragmentTransaction transaction) {
        if (mHomeFragment != null) {
            transaction.hide(mHomeFragment);
        }
        if (mSquareFragment != null) {
            transaction.hide(mSquareFragment);
        }
        if (mChatFragment != null) {
            transaction.hide(mChatFragment);
        }
        if (mMeFragment != null) {
            transaction.hide(mMeFragment);
        }
    }

    /**
     * 防止重叠
     * 当应用的内存紧张的时候，系统会回收掉Fragment对象
     * 再一次进入的时候会重新创建Fragment
     * 非原来对象，我们无法控制，导致重叠
     *
     * @param fragment
     */
    @Override
    public void onAttachFragment(Fragment fragment) {
        if (mHomeFragment != null && fragment instanceof HomeFragment) {
            mHomeFragment = (HomeFragment) fragment;
        }
        if (mSquareFragment != null && fragment instanceof SquareFragment) {
            mSquareFragment = (SquareFragment) fragment;
        }
        if (mChatFragment != null && fragment instanceof ChatFragment) {
            mChatFragment = (ChatFragment) fragment;
        }
        if (mMeFragment != null && fragment instanceof MeFragment) {
            mMeFragment = (MeFragment) fragment;
        }
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
                showFragment(mHomeFragment);
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
                showFragment(mSquareFragment);
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
                showFragment(mChatFragment);
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
                showFragment(mMeFragment);
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