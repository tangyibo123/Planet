package com.tangyibo.planet.ui.enter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.tangyibo.framework.base.BaseActivity;
import com.tangyibo.framework.bmob.BmobManager;
import com.tangyibo.framework.bmob.PlanetUser;
import com.tangyibo.framework.data.Constants;
import com.tangyibo.framework.utils.LogUtils;
import com.tangyibo.framework.utils.SpUtils;
import com.tangyibo.framework.view.LoadingView;
import com.tangyibo.planet.MainActivity;
import com.tangyibo.planet.R;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;

/**
 * 登录页
 *      * 1.必须同意协议才能登录，否则登录按钮不可用
 *      * 2.发送验证码，同时按钮变成不可点击，按钮开始倒计时，倒计时结束，按钮可点击，文字变成“发送”
 *      * 3.通过手机号码和验证码进行登录
 *      * 4.登录成功之后获取本地对象
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private EditText et_number;
    private EditText et_ver_code;
    private Button bu_send;
    private Button bu_login;
    private ImageView iv_circle;
    private TextView tv_test;
    private LoadingView dv_loading;

    //60s倒计时
    private static final int H_TIME = 1001;
    private static int TIME = 60;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case H_TIME:
                    TIME --;
                    bu_send.setText(TIME + "s");
                    if (TIME > 0){
                        mHandler.sendEmptyMessageDelayed(H_TIME, 1000);
                    }
                    else {
                        bu_send.setEnabled(true);
                        bu_send.setText("发送");
                    }
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    //初始化控件
    @SuppressLint("ResourceAsColor")
    private void initView(){

        et_number = findViewById(R.id.et_number);
        et_ver_code = findViewById(R.id.et_ver_code);
        bu_send = findViewById(R.id.bu_send);
        bu_send.setOnClickListener(this);
        bu_login = findViewById(R.id.bu_login);
        bu_login.setBackground(ContextCompat.getDrawable(this, R.drawable.corner_unuse));
        SpUtils.getInstance().putBoolean(Constants.SP_AGREEMENT, false);
        bu_login.setOnClickListener(this);
        iv_circle = findViewById(R.id.iv_circle);
        iv_circle.setOnClickListener(this);
        tv_test = findViewById(R.id.tv_test);
        tv_test.setOnClickListener(this);
        dv_loading = new LoadingView(this);

        // 如果曾经登录过，自动填入手机号码
        String phone = SpUtils.getInstance().getString(Constants.SP_PHONE, "");
        if (!TextUtils.isEmpty(phone)) {
            et_number.setText(phone);
        }
    }

    @Override
    public void onClick(View v) {
        boolean agree = SpUtils.getInstance().getBoolean(Constants.SP_AGREEMENT, false);
        switch(v.getId()){
            case R.id.bu_send:
                sendSM();
                break;
            case R.id.iv_circle:
                if(!agree){
                    iv_circle.setImageResource(R.drawable.login_agree);
                    bu_login.setBackground(ContextCompat.getDrawable(this, R.drawable.corner_unuse));
                    SpUtils.getInstance().putBoolean(Constants.SP_AGREEMENT, true);
                }
                else {
                    iv_circle.setImageResource(R.drawable.login_circle);
                    bu_login.setBackground(ContextCompat.getDrawable(this, R.drawable.corner_view));
                    SpUtils.getInstance().putBoolean(Constants.SP_AGREEMENT, false);
                }
                break;
            case R.id.bu_login:
                if(agree){
                    login();
                }
                else {
                    Toast.makeText(this, "请先阅读用户协议、隐私政策并同意！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_test:
                //测试免注册登录
                Intent intent = new Intent(LoginActivity.this, TestLoginActivity.class);
                startActivity(intent);

        }

    }

    /**
     * 发送验证码
     */
    private void sendSM() {
        //1.判断手机号码不为空
        String phone_number = et_number.getText().toString().trim();
        if(TextUtils.isEmpty(phone_number)){
            Toast.makeText(this, "手机号码不能为空！", Toast.LENGTH_SHORT).show();
            return; //如果为空，弹出提示后退出login函数，这样再次点击还会再进入login函数
        }

        //2.请求短信验证码
        BmobManager.getInstance().requestSMS(phone_number, new QueryListener<Integer>() {
            @Override
            public void done(Integer integer, BmobException e) {
                if (e == null) {
                    bu_send.setEnabled(false);
                    mHandler.sendEmptyMessage(H_TIME);
                    Toast.makeText(LoginActivity.this,"发送成功", Toast.LENGTH_SHORT).show();
                } else {
                    LogUtils.e("SMS:" + e.toString());
                    Toast.makeText(LoginActivity.this,"请求失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 登录
     */
    private void login() {
        //1.判断手机号码和验证码都不为空
        String phone_number = et_number.getText().toString().trim();
        if(TextUtils.isEmpty(phone_number)){
            Toast.makeText(this, "手机号码不能为空！", Toast.LENGTH_SHORT).show();
            return; //如果为空，弹出提示后退出login函数，这样再次点击还会再进入login函数
        }
        String ver_code = et_ver_code.getText().toString().trim();
        if(TextUtils.isEmpty(ver_code)){
            Toast.makeText(this, "验证码不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }

        //显示loading view
        if ((!TextUtils.isEmpty(phone_number)) && (!TextUtils.isEmpty(ver_code))) {
            dv_loading.show("Loading...");
            BmobManager.getInstance().signOrLoginByPhone(phone_number, ver_code, new LogInListener<PlanetUser>(){
                @Override
                public void done(PlanetUser planetUser, BmobException e) {
                    dv_loading.hide();
                    if (e == null){
                        //登陆成功无异常
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        SpUtils.getInstance().putString(Constants.SP_PHONE, phone_number);
                        finish();
                    }
                    else {
                        if (e.getErrorCode() == 207){
                            Toast.makeText(LoginActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(LoginActivity.this, "ERROR:" + e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            });

        }




    }
}
