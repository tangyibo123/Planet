package com.tangyibo.planet.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.tangyibo.framework.base.BaseBackActivity;
import com.tangyibo.framework.bmob.BmobManager;
import com.tangyibo.framework.data.Constants;
import com.tangyibo.framework.event.EventHelper;
import com.tangyibo.framework.event.MessageEvent;
import com.tangyibo.framework.gson.TextBean;
import com.tangyibo.framework.gson.VoiceBean;
import com.tangyibo.framework.manager.FileManager;
import com.tangyibo.framework.manager.MapManager;
import com.tangyibo.framework.manager.RongCloudManager;
import com.tangyibo.framework.manager.VoiceManager;
import com.tangyibo.framework.utils.CommonUtils;
import com.tangyibo.framework.utils.LogUtils;
import com.tangyibo.framework.utils.SpUtils;
import com.tangyibo.framework.view.adapter.CommonAdapter;
import com.tangyibo.framework.view.adapter.CommonViewHolder;
import com.tangyibo.planet.R;
import com.tangyibo.planet.model.ChatModel;
import com.tangyibo.planet.ui.normal.ImagePreviewActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.location.message.LocationMessage;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;

public class ChatActivity extends BaseBackActivity implements View.OnClickListener {
    /**
     *
     * ??????????????????
     * 1.?????????ChatActivity
     * 2.??????????????????????????? ?????????
     * 3.???????????????????????????
     * 4.?????????????????????????????????
     * 5.????????????
     */

    /**
     * ??????????????????
     * 1.??????(???????????????)
     * 2.??????????????????
     * 3.????????????????????????UI
     * 4.??????Service?????????????????????
     * 5.??????UI??????
     */

    /**
     * ?????????????????????
     * 1.????????????
     * 2.??????????????????
     * ???????????????
     * 1.????????????
     * 2.?????????
     * 3.????????????
     */

    //??????
    public static final int TYPE_LEFT_TEXT = 0;
    public static final int TYPE_LEFT_IMAGE = 1;
    public static final int TYPE_LEFT_LOCATION = 2;

    //??????
    public static final int TYPE_RIGHT_TEXT = 3;
    public static final int TYPE_RIGHT_IMAGE = 4;
    public static final int TYPE_RIGHT_LOCATION = 5;

    private static final int LOCATION_REQUEST_CODE = 1888;

    private static final int CHAT_INFO_REQUEST_CODE = 1889;

    /**
     * ??????
     *
     * @param mContext
     * @param userId
     * @param userName
     * @param userPhoto
     */
    public static void startActivity(Context mContext, String userId,
                                     String userName, String userPhoto) {
        if (!RongCloudManager.getInstance().isConnect()) {
            Toast.makeText(mContext, mContext.getString(R.string.text_server_status), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(mContext, ChatActivity.class);
        intent.putExtra(Constants.INTENT_USER_ID, userId);
        intent.putExtra(Constants.INTENT_USER_NAME, userName);
        intent.putExtra(Constants.INTENT_USER_PHOTO, userPhoto);
        mContext.startActivity(intent);
    }

    //????????????
    private RecyclerView mChatView;
    //?????????
    private EditText et_input_msg;
    //????????????
    private Button btn_send_msg;
    //????????????
    private LinearLayout ll_voice;
    //??????
    private LinearLayout ll_camera;
    //??????
    private LinearLayout ll_pic;
    //??????
    private LinearLayout ll_location;

    //????????????
    private LinearLayout ll_chat_bg;

    //??????????????????
    private String yourUserId;
    private String yourUserName;
    private String yourUserPhoto;

    //???????????????
    private String meUserPhoto;

    //??????
    private CommonAdapter<ChatModel> mChatAdapter;
    private List<ChatModel> mList = new ArrayList<>();

    //????????????
    private File uploadFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initView();
    }

    private void initView() {

        mChatView = (RecyclerView) findViewById(R.id.mChatView);
        et_input_msg = (EditText) findViewById(R.id.et_input_msg);
        btn_send_msg = (Button) findViewById(R.id.btn_send_msg);

        ll_voice = (LinearLayout) findViewById(R.id.ll_voice);
        ll_camera = (LinearLayout) findViewById(R.id.ll_camera);
        ll_pic = (LinearLayout) findViewById(R.id.ll_pic);
        ll_location = (LinearLayout) findViewById(R.id.ll_location);
        ll_chat_bg = (LinearLayout) findViewById(R.id.ll_chat_bg);

        btn_send_msg.setOnClickListener(this);
        ll_voice.setOnClickListener(this);
        ll_camera.setOnClickListener(this);
        ll_pic.setOnClickListener(this);
        ll_location.setOnClickListener(this);

        updateChatTheme();

        mChatView.setLayoutManager(new LinearLayoutManager(this));
        mChatAdapter = new CommonAdapter<>(mList, new CommonAdapter.OnMoreBindDataListener<ChatModel>() {
            @Override
            public int getItemType(int position) {
                return mList.get(position).getType();
            }

            @Override
            public void onBindViewHolder(final ChatModel model, CommonViewHolder viewHolder, int type, int position) {
                switch (model.getType()) {
                    case TYPE_LEFT_TEXT:
                        viewHolder.setText(R.id.tv_left_text, model.getText());
                        viewHolder.setImageUrl(ChatActivity.this, R.id.iv_left_photo, yourUserPhoto);
                        break;
                    case TYPE_RIGHT_TEXT:
                        viewHolder.setText(R.id.tv_right_text, model.getText());
                        viewHolder.setImageUrl(ChatActivity.this, R.id.iv_right_photo, meUserPhoto);
                        break;
                    case TYPE_LEFT_IMAGE:
                        viewHolder.setImageUrl(ChatActivity.this, R.id.iv_left_img, model.getImgUrl());
                        viewHolder.setImageUrl(ChatActivity.this, R.id.iv_left_photo, yourUserPhoto);

                        viewHolder.getView(R.id.iv_left_img).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImagePreviewActivity.startActivity(
                                        ChatActivity.this, true, model.getImgUrl());
                            }
                        });

                        break;
                    case TYPE_RIGHT_IMAGE:
                        if (TextUtils.isEmpty(model.getImgUrl())) {
                            if (model.getLocalFile() != null) {
                                //??????????????????
                                viewHolder.setImageFile(ChatActivity.this, R.id.iv_right_img, model.getLocalFile());
                                viewHolder.getView(R.id.iv_right_img).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ImagePreviewActivity.startActivity(
                                                ChatActivity.this, false, model.getLocalFile().getPath());
                                    }
                                });
                            }
                        } else {
                            viewHolder.setImageUrl(ChatActivity.this, R.id.iv_right_img, model.getImgUrl());
                            viewHolder.getView(R.id.iv_right_img).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ImagePreviewActivity.startActivity(
                                            ChatActivity.this, true, model.getImgUrl());
                                }
                            });
                        }
                        viewHolder.setImageUrl(ChatActivity.this, R.id.iv_right_photo, meUserPhoto);
                        break;
                    case TYPE_LEFT_LOCATION:
                        viewHolder.setImageUrl(ChatActivity.this, R.id.iv_left_photo, yourUserPhoto);
                        viewHolder.setImageUrl(ChatActivity.this, R.id.iv_left_location_img
                                , model.getMapUrl());
                        viewHolder.setText(R.id.tv_left_address, model.getAddress());

                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LocationActivity.startActivity(ChatActivity.this, false,
                                        model.getLa(), model.getLo(), model.getAddress(), LOCATION_REQUEST_CODE);
                            }
                        });

                        break;
                    case TYPE_RIGHT_LOCATION:
                        viewHolder.setImageUrl(ChatActivity.this, R.id.iv_right_photo, meUserPhoto);
                        viewHolder.setImageUrl(ChatActivity.this,
                                R.id.iv_right_location_img, model.getMapUrl());
                        viewHolder.setText(R.id.tv_right_address, model.getAddress());

                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LocationActivity.startActivity(ChatActivity.this, false,
                                        model.getLa(), model.getLo(), model.getAddress(), LOCATION_REQUEST_CODE);
                            }
                        });
                        break;
                }
            }

            @Override
            public int getLayoutId(int type) {
                if (type == TYPE_LEFT_TEXT) {
                    return R.layout.layout_chat_left_text;
                } else if (type == TYPE_RIGHT_TEXT) {
                    return R.layout.layout_chat_right_text;
                } else if (type == TYPE_LEFT_IMAGE) {
                    return R.layout.layout_chat_left_img;
                } else if (type == TYPE_RIGHT_IMAGE) {
                    return R.layout.layout_chat_right_img;
                } else if (type == TYPE_LEFT_LOCATION) {
                    return R.layout.layout_chat_left_location;
                } else if (type == TYPE_RIGHT_LOCATION) {
                    return R.layout.layout_chat_right_location;
                }
                return 0;
            }
        });
        mChatView.setAdapter(mChatAdapter);

        loadMeInfo();

        queryMessage();
    }

    /**
     * ????????????
     */
    private void updateChatTheme() {
        //??????????????? 0:?????????
        int chat_theme = SpUtils.getInstance().getInt(Constants.SP_CHAT_THEME, 0);
        switch (chat_theme) {
            case 1:
                ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_1);
                break;
            case 2:
                ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_2);
                break;
            case 3:
                ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_3);
                break;
            case 4:
                ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_4);
                break;
            case 5:
                ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_5);
                break;
            case 6:
                ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_6);
                break;
            case 7:
                ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_7);
                break;
            case 8:
                ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_8);
                break;
            case 9:
                //9????????????????????????????????????????????????????????????
                //ll_chat_bg.setBackgroundResource(R.drawable.img_chat_bg_9);
                break;
        }
    }

    /**
     * ??????????????????
     */
    private void queryMessage() {
        RongCloudManager.getInstance().getHistoryMessages(yourUserId, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (CommonUtils.isEmpty(messages)) {
                    try {
                        parsingListMessage(messages);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    queryRemoteMessage();
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtils.e("errorCode:" + errorCode);
            }
        });
    }

    /**
     * ??????????????????
     *
     * @param messages
     */
    private void parsingListMessage(List<Message> messages) {
        //??????
        Collections.reverse(messages);
        //??????
        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);
            String objectName = m.getObjectName();
            if (objectName.equals(RongCloudManager.MSG_TEXT_NAME)) {
                TextMessage textMessage = (TextMessage) m.getContent();
                String msg = textMessage.getContent();
                LogUtils.i("msg:" + msg);
                try {
                    TextBean textBean = new Gson().fromJson(msg, TextBean.class);
                    if (textBean.getType().equals(RongCloudManager.TYPE_TEXT)) {
                        //?????????UI ???????????? ?????? ???
                        if (m.getSenderUserId().equals(yourUserId)) {
                            addText(0, textBean.getMsg());
                        } else {
                            addText(1, textBean.getMsg());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (objectName.equals(RongCloudManager.MSG_IMAGE_NAME)) {
                ImageMessage imageMessage = (ImageMessage) m.getContent();
                String url = imageMessage.getRemoteUri().toString();
                if (!TextUtils.isEmpty(url)) {
                    LogUtils.i("url:" + url);
                    if (m.getSenderUserId().equals(yourUserId)) {
                        addImage(0, url);
                    } else {
                        addImage(1, url);
                    }
                }
            } else if (objectName.equals(RongCloudManager.MSG_LOCATION_NAME)) {
                LocationMessage locationMessage = (LocationMessage) m.getContent();
                if (m.getSenderUserId().equals(yourUserId)) {
                    addLocation(0, locationMessage.getLat(),
                            locationMessage.getLng(), locationMessage.getPoi());
                } else {
                    addLocation(1, locationMessage.getLat(),
                            locationMessage.getLng(), locationMessage.getPoi());
                }
            }
        }
    }

    /**
     * ???????????????????????????
     */
    private void queryRemoteMessage() {
        RongCloudManager.getInstance().getRemoteHistoryMessages(yourUserId, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (CommonUtils.isEmpty(messages)) {
                    try {
                        parsingListMessage(messages);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LogUtils.e("errorCode:" + errorCode);
            }
        });
    }

    /**
     * ??????????????????
     */
    private void loadMeInfo() {
        Intent intent = getIntent();
        yourUserId = intent.getStringExtra(Constants.INTENT_USER_ID);
        yourUserName = intent.getStringExtra(Constants.INTENT_USER_NAME);
        yourUserPhoto = intent.getStringExtra(Constants.INTENT_USER_PHOTO);

        meUserPhoto = BmobManager.getInstance().getUser().getPhoto();

        LogUtils.i("yourUserPhoto:" + yourUserPhoto);
        LogUtils.i("meUserPhoto:" + meUserPhoto);

        //????????????
        if (!TextUtils.isEmpty(yourUserName)) {
            getSupportActionBar().setTitle(yourUserName);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send_msg:
                String inputText = et_input_msg.getText().toString().trim();
                if (TextUtils.isEmpty(inputText)) {
                    return;
                }
                RongCloudManager.getInstance().sendTextMessage(inputText,
                        RongCloudManager.TYPE_TEXT, yourUserId);
                addText(1, inputText);
                //??????
                et_input_msg.setText("");
                break;
            case R.id.ll_voice:
                VoiceManager.getInstance(this).startSpeak(new RecognizerDialogListener() {
                    @Override
                    public void onResult(RecognizerResult recognizerResult, boolean b) {
                        String result = recognizerResult.getResultString();
                        if (!TextUtils.isEmpty(result)) {
                            LogUtils.i("result:" + result);
                            VoiceBean voiceBean = new Gson().fromJson(result, VoiceBean.class);
                            if (voiceBean.isLs()) {
                                StringBuffer sb = new StringBuffer();
                                for (int i = 0; i < voiceBean.getWs().size(); i++) {
                                    VoiceBean.WsBean wsBean = voiceBean.getWs().get(i);
                                    String sResult = wsBean.getCw().get(0).getW();
                                    sb.append(sResult);
                                }
                                LogUtils.i("result:" + sb.toString());
                                et_input_msg.setText(sb.toString());
                            }
                        }
                    }

                    @Override
                    public void onError(SpeechError speechError) {
                        LogUtils.e("speechError:" + speechError.toString());
                    }
                });
                break;
            case R.id.ll_camera:
                FileManager.getInstance().toCamera(this);
                break;
            case R.id.ll_pic:
                FileManager.getInstance().toAlbum(this);
                break;
            case R.id.ll_location:
                LocationActivity.startActivity(this, true, 0, 0, "", LOCATION_REQUEST_CODE);
                break;
        }
    }

    /**
     * ?????????????????????
     *
     * @param model
     */
    private void baseAddItem(ChatModel model) {
        mList.add(model);
        mChatAdapter.notifyDataSetChanged();
        //???????????????
        mChatView.scrollToPosition(mList.size() - 1);
    }

    /**
     * ??????????????????
     *
     * @param index 0:?????? 1:??????
     * @param text
     */
    private void addText(int index, String text) {
        LogUtils.i("ChatA:" + text);
        ChatModel model = new ChatModel();
        if (index == 0) {
            model.setType(TYPE_LEFT_TEXT);
        } else {
            model.setType(TYPE_RIGHT_TEXT);
        }
        model.setText(text);
        baseAddItem(model);
    }

    /**
     * ????????????
     *
     * @param index
     * @param url
     */
    private void addImage(int index, String url) {
        ChatModel model = new ChatModel();
        if (index == 0) {
            model.setType(TYPE_LEFT_IMAGE);
        } else {
            model.setType(TYPE_RIGHT_IMAGE);
        }
        model.setImgUrl(url);
        baseAddItem(model);
    }

    /**
     * ????????????
     *
     * @param index
     * @param file
     */
    private void addImage(int index, File file) {
        ChatModel model = new ChatModel();
        if (index == 0) {
            model.setType(TYPE_LEFT_IMAGE);
        } else {
            model.setType(TYPE_RIGHT_IMAGE);
        }
        model.setLocalFile(file);
        baseAddItem(model);
    }

    /**
     * ????????????
     *
     * @param index
     * @param la
     * @param lo
     * @param address
     */
    private void addLocation(int index, double la, double lo, String address) {
        ChatModel model = new ChatModel();
        if (index == 0) {
            model.setType(TYPE_LEFT_LOCATION);
        } else {
            model.setType(TYPE_RIGHT_LOCATION);
        }
        model.setLa(la);
        model.setLo(lo);
        model.setAddress(address);
        model.setMapUrl(MapManager.getInstance().getMapUrl(la, lo));
        baseAddItem(model);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (!event.getUserId().equals(yourUserId)) {
            return;
        }
        switch (event.getType()) {
            case EventHelper.FLAG_SEND_TEXT:
                addText(0, event.getText());
                break;
            case EventHelper.FLAG_SEND_IMAGE:
                addImage(0, event.getImgUrl());
                break;
            case EventHelper.FLAG_SEND_LOCATION:
                addLocation(0, event.getLa(), event.getLo(), event.getAddress());
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FileManager.CAMEAR_REQUEST_CODE) {
                uploadFile = FileManager.getInstance().getTempFile();
            }
            else if (requestCode == FileManager.ALBUM_REQUEST_CODE) {
                Uri uri = data.getData();
                if (uri != null) {
                    //String path = uri.getPath();
                    //?????????????????????
                    String path = FileManager.getInstance().getRealPathFromURI(this, uri);
                    //LogUtils.e("path:" + path);
                    if (!TextUtils.isEmpty(path)) {
                        uploadFile = new File(path);
                    }
                }
            }
            else if (requestCode == LOCATION_REQUEST_CODE) {
                double la = data.getDoubleExtra("la", 0);
                double lo = data.getDoubleExtra("lo", 0);
                String address = data.getStringExtra("address");

                LogUtils.i("la:" + la);
                LogUtils.i("lo:" + lo);
                LogUtils.i("address:" + address);

                if (TextUtils.isEmpty(address)) {
                    MapManager.getInstance().poi2address(la, lo, new MapManager.OnPoi2AddressGeocodeListener() {
                        @Override
                        public void poi2address(String address) {
                            //??????????????????
                            RongCloudManager.getInstance().sendLocationMessage(yourUserId, la, lo, address);
                            addLocation(1, la, lo, address);
                        }
                    });
                } else {
                    //??????????????????
                    RongCloudManager.getInstance().sendLocationMessage(yourUserId, la, lo, address);
                    addLocation(1, la, lo, address);
                }

            } else if (requestCode == CHAT_INFO_REQUEST_CODE) {
                finish();
            }
            if (uploadFile != null) {
                //??????????????????
                RongCloudManager.getInstance().sendImageMessage(yourUserId, uploadFile);
                //????????????
                addImage(1, uploadFile);
                uploadFile = null;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_chat_menu:
                ChatInfoActivity.startChatInfo(this, yourUserId, CHAT_INFO_REQUEST_CODE);
                break;
            case R.id.menu_chat_audio:
                if (!checkWindowPermissions()) {
                    requestWindowPermissions();
                } else {
                    RongCloudManager.getInstance().startAudioCall(this, yourUserId);
                }
                break;
            case R.id.menu_chat_video:
                if (!checkWindowPermissions()) {
                    requestWindowPermissions();
                } else {
                    RongCloudManager.getInstance().startVideoCall(this, yourUserId);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
