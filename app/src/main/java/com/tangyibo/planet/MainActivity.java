package com.tangyibo.planet;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tangyibo.framework.base.BaseActivity;
import com.tangyibo.framework.bmob.BmobManager;
import com.tangyibo.framework.bmob.PlanetUser;
import com.tangyibo.framework.data.Constants;
import com.tangyibo.framework.event.EventHelper;
import com.tangyibo.framework.event.MessageEvent;
import com.tangyibo.framework.gson.TokenBean;
import com.tangyibo.framework.manager.DialogManager;
import com.tangyibo.framework.manager.HttpManager;
import com.tangyibo.framework.utils.CommonUtils;
import com.tangyibo.framework.utils.LogUtils;
import com.tangyibo.framework.utils.SpUtils;
import com.tangyibo.framework.view.DialogView;
import com.tangyibo.planet.fragment.ChatFragment;
import com.tangyibo.planet.fragment.HomeFragment;
import com.tangyibo.planet.fragment.MeFragment;
import com.tangyibo.planet.fragment.SquareFragment;
import com.tangyibo.planet.service.CloudService;
import com.tangyibo.planet.ui.home.FirstUploadActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity implements View.OnClickListener{

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

    private Disposable disposable;
    private DialogView mUploadView;

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

        requestPermiss();

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

        //检查登陆的token
        checkToken();

        //模拟数据
        //SimulationData.testData();
    }

    /**
     * 检查Token Bmob
     */
    private void checkToken() {
        LogUtils.i("Check Token ...");
        if (mUploadView != null) {
            DialogManager.getInstance().hide(mUploadView);
        }
        //获取token 需要三个参数：1. 用户id， 2. 头像地址 3. 昵称
        String token = SpUtils.getInstance().getString(Constants.SP_TOKEN, "");
        if (!TextUtils.isEmpty(token)) {
            // 启动云服务去连接融云
            LogUtils.i("启动云服务连接融云");
            startCloudService();
        }
        else {
            //1.有三个参数
            String tokenPhoto = BmobManager.getInstance().getUser().getTokenPhoto();
            String tokenName = BmobManager.getInstance().getUser().getTokenNickName();
            if (!TextUtils.isEmpty(tokenName)) {
                //创建Token
                createToken();
            } else {
                //创建上传提示框
                createUploadDialog();
            }
        }
    }

    private void createUploadDialog() {
        mUploadView = DialogManager.getInstance().initView(this, R.layout.dialog_first_upload);
        //外部点击不能消失
        mUploadView.setCancelable(false);
        ImageView iv_go_upload = mUploadView.findViewById(R.id.iv_go_upload);
        iv_go_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirstUploadActivity.startActivity(MainActivity.this);
            }
        });
        DialogManager.getInstance().show(mUploadView);
    }

    // 创建用户token
    private void createToken() {
        LogUtils.i("createToken");
        /**
         * 1.根据用户id去融云服务端接口获取Token
         * 2.连接融云
         */
        BmobManager.getInstance().queryPhoneUser(SpUtils.getInstance().getString(Constants.SP_PHONE, ""), new FindListener<PlanetUser>() {
            @Override
            public void done(List<PlanetUser> list, BmobException e) {
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        PlanetUser imUser = list.get(0);
                        final HashMap<String, String> map = new HashMap<>();
                        map.put("userId", imUser.getObjectId());
                        map.put("name", imUser.getNickName());
                        map.put("portraitUri", imUser.getPhoto());

                        //通过OkHttp请求Token
                        disposable = Observable.create(new ObservableOnSubscribe<String>() {
                            @Override
                            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                                //执行请求过程
                                String json = HttpManager.getInstance().postCloudToken(map);
                                LogUtils.i("json:" + json);
                                emitter.onNext(json);
                                emitter.onComplete();
                            }
                            //线程调度
                        }).subscribeOn(Schedulers.newThread())
                                .subscribeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<String>() {
                                    @Override
                                    public void accept(String s) throws Exception {
                                        parsingCloudToken(s);
                                    }
                                });
                    }
                }
            }
        });
    }

    // 解析Token
    private void parsingCloudToken(String s) {
        try {
            LogUtils.i("解析融云返回的token:" + s);
            TokenBean tokenBean = new Gson().fromJson(s, TokenBean.class);
            if (tokenBean.getCode() == 200) {
                if (!TextUtils.isEmpty(tokenBean.getToken())) {
                    //保存Token
                    SpUtils.getInstance().putString(Constants.SP_TOKEN, tokenBean.getToken());
                    LogUtils.i("融云id->token: "+tokenBean.getToken());
                    startCloudService();
                }
            } else if (tokenBean.getCode() == 2007) {
                Toast.makeText(this, "注册人数已达上限，请替换成自己的Key", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            LogUtils.i("parsingCloudToken:" + e.toString());
        }
    }

    private void startCloudService() {
        LogUtils.i("startCloudService");
        startService(new Intent(this, CloudService.class));
        //检查更新
        //new UpdateHelper(this).updateApp(null);
    }

    // 请求权限
    private void requestPermiss() {
        // 申请危险权限, 传入接口的实现类的对象（匿名内部类实现接口，new一个匿名内部类的对象）
        requestDyPermissions(new PermissionsResult() {
            @Override
            public void OnSuccess() {
            }
            @Override
            public void OnFail(List<String> noPermissions) {
                LogUtils.i("noPermissions:" + noPermissions.toString());
            }
        });
    }

    //初始化Fragment
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

    //显示Fragment
    private void showFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            hideAllFragment(transaction);
            transaction.show(fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    // 隐藏所有的Fragment
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
                iv_home.setAlpha((float) 0.87);
                tv_home.setAlpha((float) 0.87);
                iv_square.setAlpha((float) 0.6);
                tv_square.setAlpha((float) 0.6);
                iv_chat.setAlpha((float) 0.6);
                tv_chat.setAlpha((float) 0.6);
                iv_me.setAlpha((float) 0.6);
                tv_me.setAlpha((float) 0.6);
                break;
            case 1:
                showFragment(mSquareFragment);
                iv_home.setAlpha((float) 0.6);
                tv_home.setAlpha((float) 0.6);
                iv_square.setAlpha((float) 0.87);
                tv_square.setAlpha((float) 0.87);
                iv_chat.setAlpha((float) 0.6);
                tv_chat.setAlpha((float) 0.6);
                iv_me.setAlpha((float) 0.6);
                tv_me.setAlpha((float) 0.6);
                break;
            case 2:
                showFragment(mChatFragment);
                iv_home.setAlpha((float) 0.6);
                tv_home.setAlpha((float) 0.6);
                iv_square.setAlpha((float) 0.6);
                tv_square.setAlpha((float) 0.6);
                iv_chat.setAlpha((float) 0.87);
                tv_chat.setAlpha((float) 0.87);
                iv_me.setAlpha((float) 0.6);
                tv_me.setAlpha((float) 0.6);
                break;
            case 3:
                showFragment(mMeFragment);
                iv_home.setAlpha((float) 0.6);
                tv_home.setAlpha((float) 0.6);
                iv_square.setAlpha((float) 0.6);
                tv_square.setAlpha((float) 0.6);
                iv_chat.setAlpha((float) 0.6);
                tv_chat.setAlpha((float) 0.6);
                iv_me.setAlpha((float) 0.87);
                tv_me.setAlpha((float) 0.87);
                //BmobManager.getInstance().updateAccount("13284525087", "123456");
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
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()) {
            case EventHelper.EVENT_REFRE_TOKEN_STATUS:
                checkToken();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    //第一次按下时间
    private long firstClick;

    @Override
    public void onBackPressed() {
        AppExit();
        //super.onBackPressed();
    }

    /**
     * 再按一次退出
     */
    public void AppExit() {
        if (System.currentTimeMillis() - this.firstClick > 2000L) {
            this.firstClick = System.currentTimeMillis();
            Toast.makeText(this, getString(R.string.text_main_exit), Toast.LENGTH_LONG).show();
            return;
        }
        finish();
    }
}