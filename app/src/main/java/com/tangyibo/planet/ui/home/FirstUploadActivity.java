package com.tangyibo.planet.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tangyibo.framework.base.BaseBackActivity;
import com.tangyibo.framework.bmob.BmobManager;
import com.tangyibo.framework.manager.FileManager;
import com.tangyibo.framework.manager.DialogManager;
import com.tangyibo.framework.utils.LogUtils;
import com.tangyibo.framework.view.DialogView;
import com.tangyibo.framework.view.LoadingView;
import com.tangyibo.planet.MainActivity;
import com.tangyibo.planet.R;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class FirstUploadActivity extends BaseBackActivity implements View.OnClickListener {

    //跳转
    public static void startActivity(Activity mActivity) {
        Intent intent = new Intent(mActivity, FirstUploadActivity.class);
        mActivity.startActivity(intent);
    }

    private File uploadFile = null;

    private TextView tv_camera;
    private TextView tv_ablum;
    private TextView tv_cancel;

    //圆形头像
    private CircleImageView iv_photo;
    private EditText et_nickname;
    private Button btn_upload;

    private LoadingView mLoadingView;
    private DialogView mPhotoSelectView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_upload);
        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {

        initPhotoView();

        iv_photo = (CircleImageView) findViewById(R.id.iv_photo);
        et_nickname = (EditText) findViewById(R.id.et_nickname);
        btn_upload = (Button) findViewById(R.id.btn_upload);

        iv_photo.setOnClickListener(this);
        btn_upload.setOnClickListener(this);

        btn_upload.setEnabled(false);

        et_nickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                btn_upload.setEnabled(charSequence.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * 初始化选择框
     */
    private void initPhotoView() {

        mLoadingView = new LoadingView(this);
        mLoadingView.setLoadingText(getString(R.string.text_upload_photo_loding));

        mPhotoSelectView = DialogManager.getInstance()
                .initView(this, R.layout.dialog_select_photo, Gravity.BOTTOM);

        tv_camera = (TextView) mPhotoSelectView.findViewById(R.id.tv_camera);
        tv_camera.setOnClickListener(this);
        tv_ablum = (TextView) mPhotoSelectView.findViewById(R.id.tv_ablum);
        tv_ablum.setOnClickListener(this);
        tv_cancel = (TextView) mPhotoSelectView.findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_camera:
                DialogManager.getInstance().hide(mPhotoSelectView);
                if (!checkSinglePermission(Manifest.permission.CAMERA)) {
                    requestMyPermission(new String[]{Manifest.permission.CAMERA});
                } else {
                    //跳转到相机
                    FileManager.getInstance().toCamera(this);
                }
                break;
            case R.id.tv_ablum:
                DialogManager.getInstance().hide(mPhotoSelectView);
                //跳转到相册
                FileManager.getInstance().toAlbum(this);
                break;
            case R.id.tv_cancel:
                DialogManager.getInstance().hide(mPhotoSelectView);
                break;
            case R.id.iv_photo:
                //显示选择提示框
                DialogManager.getInstance().show(mPhotoSelectView);
                break;
            case R.id.btn_upload:
                uploadName();
                break;
        }
    }

    /**
     * 更新信息
     */
    private void uploadName() {
        //如果条件没有满足，是走不到这里的
        String nickName = et_nickname.getText().toString().trim();
        mLoadingView.show();
        BmobManager.getInstance().uploadNickName(FirstUploadActivity.this, nickName);
        mLoadingView.hide();
        startActivity(new Intent(FirstUploadActivity.this, MainActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.i("requestCode:" + requestCode);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FileManager.CAMEAR_REQUEST_CODE) {
                FileManager.getInstance().startPhotoZoom(this, FileManager.getInstance().getTempFile());
            } else if (requestCode == FileManager.ALBUM_REQUEST_CODE) {
                Uri uri = data.getData();
                if (uri != null) {
                    String path = FileManager.getInstance().getRealPathFromURI(this, uri);
                    if (!TextUtils.isEmpty(path)) {
                        uploadFile = new File(path);
                        FileManager.getInstance().startPhotoZoom(this, uploadFile);
                    }
                }
            } else if (requestCode == FileManager.CAMERA_CROP_RESULT) {
                LogUtils.i("CAMERA_CROP_RESULT");
                uploadFile = new File(FileManager.getInstance().getCropPath());
                LogUtils.i("uploadPhotoFile:" + uploadFile.getPath());
            }
            //设置头像
            if (uploadFile != null) {
                Bitmap mBitmap = BitmapFactory.decodeFile(uploadFile.getPath());
                iv_photo.setImageBitmap(mBitmap);

                //判断当前的输入框
                String nickName = et_nickname.getText().toString().trim();
                btn_upload.setEnabled(!TextUtils.isEmpty(nickName));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
