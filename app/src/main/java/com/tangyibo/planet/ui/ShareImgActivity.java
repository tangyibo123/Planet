package com.tangyibo.planet.ui;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tangyibo.framework.base.BaseBackActivity;
import com.tangyibo.framework.bmob.BmobManager;
import com.tangyibo.framework.bmob.PlanetUser;
import com.tangyibo.framework.helper.GlideHelper;
import com.tangyibo.framework.manager.FileManager;
import com.tangyibo.framework.view.LoadingView;
import com.tangyibo.planet.R;
import com.uuzuche.lib_zxing.activity.CodeUtils;

public class ShareImgActivity extends BaseBackActivity implements View.OnClickListener {
    //头像
    private ImageView iv_photo;
    //昵称
    private TextView tv_name;
    //性别
    private TextView tv_sex;
    //年龄
    private TextView tv_age;
    //二维码
    private ImageView iv_qrcode;
    //根布局
    private LinearLayout ll_content;
    //下载
    private LinearLayout ll_download;

    private LoadingView mLodingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_img);

        initView();
    }

    private void initView() {

        mLodingView = new LoadingView(this);
        mLodingView.setLoadingText(getString(R.string.text_shar_save_ing));

        iv_photo = (ImageView) findViewById(R.id.iv_photo);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_sex = (TextView) findViewById(R.id.tv_sex);
        tv_age = (TextView) findViewById(R.id.tv_age);
        iv_qrcode = (ImageView) findViewById(R.id.iv_qrcode);
        ll_content = (LinearLayout) findViewById(R.id.ll_content);
        ll_download = (LinearLayout) findViewById(R.id.ll_download);

        ll_download.setOnClickListener(this);

        loadInfo();
    }

    /**
     * 加载个人信息
     */
    private void loadInfo() {
        PlanetUser imUser = BmobManager.getInstance().getUser();

        GlideHelper.loadUrl(this, imUser.getPhoto(), iv_photo);
        tv_name.setText(imUser.getNickName());
        tv_sex.setText(imUser.isSex() ? R.string.text_me_info_boy : R.string.text_me_info_girl);
        tv_age.setText(imUser.getAge() + " " + getString(R.string.text_search_age));

        createQRCode(imUser.getObjectId());
    }

    /**
     * 创建二维码
     */
    private void createQRCode(final String userId) {

        /**
         * View的绘制
         */

        iv_qrcode.post(new Runnable() {
            @Override
            public void run() {
                String textContent = "下载星球App，搜索我的id：" + userId + " 就可以添加好友啦！一起来玩鸭~";
                Bitmap mBitmap = CodeUtils.createImage(textContent,
                        iv_qrcode.getWidth(), iv_qrcode.getHeight(), null);
                iv_qrcode.setImageBitmap(mBitmap);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_download:
                if (!checkSinglePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    requestMyPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        startActivity(intent);
                        return;
                    }
                    else {
                        /**
                         * 1.View截图
                         * 2.创建一个Bitmap
                         * 3.保存到相册
                         */

                        mLodingView.show();

                        /**
                         * setDrawingCacheEnabled
                         * 保留我们的绘制副本
                         * 1.重新测量
                         * 2.重新布局
                         * 3.得到我们的DrawingCache
                         * 4.转换成Bitmap
                         */
                        //ll_content.setDrawingCacheEnabled(true);
                        //ll_content.buildDrawingCache();

                        ll_content.measure(
                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

                        ll_content.layout(0, 0, ll_content.getMeasuredWidth(),
                                ll_content.getMeasuredHeight());

                        //Bitmap mBitmap = ll_content.getDrawingCache();

                        Bitmap mBitmap = Bitmap.createBitmap(ll_content.getMeasuredWidth(),
                                ll_content.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                        Canvas c = new Canvas(mBitmap);
                        c.scale(1, 1);
                        ll_content.draw(c);

                        if (mBitmap != null) {
                            FileManager.getInstance().saveBitmapToAlbum(this, mBitmap);
                            mLodingView.hide();
                        }
                        //ll_content.setDrawingCacheEnabled(false);
                        //ll_content.destroyDrawingCache();
                        break;
                    }
                    }
                }
    }
}
