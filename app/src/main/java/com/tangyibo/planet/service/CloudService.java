package com.tangyibo.planet.service;


import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tangyibo.framework.bmob.BmobManager;
import com.tangyibo.framework.bmob.Friend;
import com.tangyibo.framework.bmob.PlanetUser;
import com.tangyibo.framework.data.CallRecord;
import com.tangyibo.framework.data.Constants;
import com.tangyibo.framework.data.LitePalManager;
import com.tangyibo.framework.data.NewFriend;
import com.tangyibo.framework.event.EventHelper;
import com.tangyibo.framework.event.MessageEvent;
import com.tangyibo.framework.gson.TextBean;
import com.tangyibo.framework.helper.GlideHelper;
import com.tangyibo.framework.helper.MediaPlayerHelper;
import com.tangyibo.framework.manager.MyWindowManager;
import com.tangyibo.framework.manager.NoticeManager;
import com.tangyibo.framework.manager.RongCloudManager;
import com.tangyibo.framework.utils.CommonUtils;
import com.tangyibo.framework.utils.LogUtils;
import com.tangyibo.framework.utils.SpUtils;
import com.tangyibo.framework.utils.TimeUtils;
import com.tangyibo.planet.MainActivity;
import com.tangyibo.planet.R;
import com.tangyibo.planet.ui.ChatActivity;
import com.tangyibo.planet.ui.NewFriendActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.rong.calllib.IRongCallListener;
import io.rong.calllib.IRongReceivedCallListener;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.imlib.listener.OnReceiveMessageWrapperListener;
import io.rong.imlib.location.message.LocationMessage;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.ReceivedProfile;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;

/**
 * FileName: CloudService
 * Profile: 云服务
 */
public class CloudService extends Service implements View.OnClickListener {

    //计时
    private static final int H_TIME_WHAT = 1000;

    //通话时间
    private int callTimer = 0;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case H_TIME_WHAT:
                    callTimer++;
                    String time = TimeUtils.formatDuring(callTimer * 1000);
                    audio_tv_status.setText(time);
                    video_tv_time.setText(time);
                    mSmallTime.setText(time);
                    mHandler.sendEmptyMessageDelayed(H_TIME_WHAT, 1000);
                    break;
            }
            return false;
        }
    });

    private Disposable disposable;

    //音频窗口
    private View mFullAudioView;
    //头像
    private CircleImageView audio_iv_photo;
    //状态
    private TextView audio_tv_status;
    //接听图片
    private ImageView audio_iv_answer;
    //接听按钮
    private LinearLayout audio_ll_answer;
    //挂断图片
    private ImageView audio_iv_hangup;
    //挂断按钮
    private LinearLayout audio_ll_hangup;
    //免提图片
    private ImageView audio_iv_hf;
    //免提按钮
    private LinearLayout audio_ll_hf;
    //最小化
    private ImageView audio_iv_small;

    //视频窗口
    private View mFullVideoView;
    //大窗口
    private RelativeLayout video_big_video;
    //小窗口
    private RelativeLayout video_small_video;
    //头像
    private CircleImageView video_iv_photo;
    //昵称
    private TextView video_tv_name;
    //状态
    private TextView video_tv_status;
    //个人信息窗口
    private LinearLayout video_ll_info;
    //时间
    private TextView video_tv_time;
    //接听
    private LinearLayout video_ll_answer;
    //挂断
    private LinearLayout video_ll_hangup;

    //最小化的音频View
    private WindowManager.LayoutParams lpSmallView;
    private View mSmallAudioView;
    //时间
    private TextView mSmallTime;

    //通话ID
    private String callId = "";

    //媒体类
    private MediaPlayerHelper mAudioCallMedia;
    private MediaPlayerHelper mAudioHangupMedia;

    //摄像类
    private SurfaceView mLocalView;
    private SurfaceView mRemoteView;

    //是否小窗口显示本地视频
    private boolean isSmallShowLocal = false;

    //拨打状态
    private int isCallTo = 0;
    //接听状态
    private int isReceiverTo = 0;
    //拨打还是接听
    private boolean isCallOrReceiver = true;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initService();
        initWindow();
        linkCloudServer();
    }

    /**
     * 初始化服务
     */
    private void initService() {

        EventHelper.register(this);

        //来电铃声
        mAudioCallMedia = new MediaPlayerHelper();
        //挂断铃声
        mAudioHangupMedia = new MediaPlayerHelper();

        //无限循环
        mAudioCallMedia.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mAudioCallMedia.startPlay(RongCloudManager.callAudioPath);
            }
        });
    }

    /**
     * 初始化窗口
     */
    private void initWindow() {

        //音频
        mFullAudioView = MyWindowManager.getInstance().getView(R.layout.layout_chat_audio);
        audio_iv_photo = mFullAudioView.findViewById(R.id.audio_iv_photo);
        audio_tv_status = mFullAudioView.findViewById(R.id.audio_tv_status);
        audio_iv_answer = mFullAudioView.findViewById(R.id.audio_iv_answer);
        audio_ll_answer = mFullAudioView.findViewById(R.id.audio_ll_answer);
        audio_iv_hangup = mFullAudioView.findViewById(R.id.audio_iv_hangup);
        audio_ll_hangup = mFullAudioView.findViewById(R.id.audio_ll_hangup);
        audio_iv_hf = mFullAudioView.findViewById(R.id.audio_iv_hf);
        audio_ll_hf = mFullAudioView.findViewById(R.id.audio_ll_hf);
        audio_iv_small = mFullAudioView.findViewById(R.id.audio_iv_small);

        audio_ll_answer.setOnClickListener(this);
        audio_ll_hangup.setOnClickListener(this);
        audio_ll_hf.setOnClickListener(this);
        audio_iv_small.setOnClickListener(this);

        //视频
        mFullVideoView = MyWindowManager.getInstance().getView(R.layout.layout_chat_video);
        video_big_video = mFullVideoView.findViewById(R.id.video_big_video);
        video_small_video = mFullVideoView.findViewById(R.id.video_small_video);
        video_iv_photo = mFullVideoView.findViewById(R.id.video_iv_photo);
        video_tv_name = mFullVideoView.findViewById(R.id.video_tv_name);
        video_tv_status = mFullVideoView.findViewById(R.id.video_tv_status);
        video_ll_info = mFullVideoView.findViewById(R.id.video_ll_info);
        video_tv_time = mFullVideoView.findViewById(R.id.video_tv_time);
        video_ll_answer = mFullVideoView.findViewById(R.id.video_ll_answer);
        video_ll_hangup = mFullVideoView.findViewById(R.id.video_ll_hangup);

        video_ll_answer.setOnClickListener(this);
        video_ll_hangup.setOnClickListener(this);
        video_small_video.setOnClickListener(this);

        createSmallAudioView();
    }

    /**
     * 创建最小化的音频窗口
     */
    private void createSmallAudioView() {
        lpSmallView = MyWindowManager.getInstance().createLayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.LEFT);
        mSmallAudioView = MyWindowManager.getInstance().getView(R.layout.layout_chat_small_audio);
        mSmallTime = mSmallAudioView.findViewById(R.id.mSmallTime);

        mSmallAudioView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //最大化
                MyWindowManager.getInstance().hideView(mSmallAudioView);
                MyWindowManager.getInstance().showView(mFullAudioView);
            }
        });

        mSmallAudioView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                /**
                 * OnTouch 和 OnClick 点击冲突
                 * 如何判断是点击 还是 移动
                 * 通过点击下的坐标 - 落地的坐标 如果移动则说明是移动 如果 = 0 ，那说明没有移动则是点击
                 */
                int mStartX = (int) event.getRawX();
                int mStartY = (int) event.getRawY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isMove = false;
                        isDrag = false;
                        mLastX = (int) event.getRawX();
                        mLastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:

                        //偏移量
                        int dx = mStartX - mLastX;
                        int dy = mStartY - mLastY;

                        if (isMove) {
                            isDrag = true;
                        } else {
                            if (dx == 0 && dy == 0) {
                                isMove = false;
                            } else {
                                isMove = true;
                                isDrag = true;
                            }
                        }

                        //移动
                        lpSmallView.x += dx;
                        lpSmallView.y += dy;

                        //重置坐标
                        mLastX = mStartX;
                        mLastY = mStartY;

                        //WindowManager addView removeView updateView
                        MyWindowManager.getInstance().updateView(mSmallAudioView, lpSmallView);

                        break;
                }
                return isDrag;
            }
        });
    }

    //是否移动
    private boolean isMove = false;
    //是否拖拽
    private boolean isDrag = false;
    private int mLastX;
    private int mLastY;

    /**
     * 连接云服务
     */
    private void linkCloudServer() {
        //获取Token
        String token = SpUtils.getInstance().getString(Constants.SP_TOKEN, "");
        LogUtils.e("token:" + token);
        //连接服务
        RongCloudManager.getInstance().connectRongCloud(token);
        //接收消息
        RongCloudManager.getInstance().setOnReceiveMessageListener(new OnReceiveMessageWrapperListener() {
            @Override
            public void onReceivedMessage(Message message, ReceivedProfile profile) {
                //(message, i) -> {
                LogUtils.i("接收消息");
                parsingImMessage(message);
                //return false;
            }

        });

        //监听通话
        RongCloudManager.getInstance().setReceivedCallListener(new IRongReceivedCallListener() {
            @Override
            public void onReceivedCall(RongCallSession rongCallSession) {
                LogUtils.i("rongCallSession");

                //检查设备可用
                if (!RongCloudManager.getInstance().isVoIPEnabled(CloudService.this)) {
                    return;
                }

                /**
                 * 1.获取拨打和接通的ID
                 * 2.来电的话播放铃声
                 * 3.加载个人信息去填充
                 * 4.显示Window
                 */

                //呼叫端的ID
                String callUserId = rongCallSession.getCallerUserId();

                //通话ID
                callId = rongCallSession.getCallId();

                //播放来电铃声
                mAudioCallMedia.startPlay(RongCloudManager.callAudioPath);

                //更新个人信息
                updateWindowInfo(0, rongCallSession.getMediaType(), callUserId);

                if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                    MyWindowManager.getInstance().showView(mFullAudioView);
                } else if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                    MyWindowManager.getInstance().showView(mFullVideoView);
                }

                isReceiverTo = 1;

                isCallOrReceiver = false;
            }

            @Override
            public void onCheckPermission(RongCallSession rongCallSession) {
                LogUtils.i("onCheckPermission:" + rongCallSession.toString());
            }
        });

        //监听通话状态
        RongCloudManager.getInstance().setVoIPCallListener(new IRongCallListener() {

            //电话拨出
            @Override
            public void onCallOutgoing(RongCallSession rongCallSession, SurfaceView surfaceView) {
                LogUtils.i("onCallOutgoing");

                isCallOrReceiver = true;

                isCallTo = 1;

                //更新信息
                String targetId = rongCallSession.getTargetId();
                //更新信息
                updateWindowInfo(1, rongCallSession.getMediaType(), targetId);

                //通话ID
                callId = rongCallSession.getCallId();

                if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                    MyWindowManager.getInstance().showView(mFullAudioView);
                } else if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                    MyWindowManager.getInstance().showView(mFullVideoView);
                    //显示摄像头
                    mLocalView = surfaceView;
                    video_big_video.addView(mLocalView);
                }

            }

            //已建立通话
            @Override
            public void onCallConnected(RongCallSession rongCallSession, SurfaceView surfaceView) {
                LogUtils.i("onCallConnected");

                /**
                 * 1.开始计时
                 * 2.关闭铃声
                 * 3.更新按钮
                 */

                isCallTo = 2;
                isReceiverTo = 2;

                //关闭铃声
                if (mAudioCallMedia.isPlaying()) {
                    mAudioCallMedia.stopPlay();
                }

                //开始计时
                mHandler.sendEmptyMessage(H_TIME_WHAT);

                if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                    goneAudioView(true, false, true, true, true);
                } else if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                    goneVideoView(false, true, true, false, true, true);
                    mLocalView = surfaceView;
                }

            }

            //通话结束
            @Override
            public void onCallDisconnected(RongCallSession rongCallSession, RongCallCommon.CallDisconnectedReason callDisconnectedReason) {
                LogUtils.i("onCallDisconnected");

                String callUserId = rongCallSession.getCallerUserId();
                String recevierId = rongCallSession.getTargetId();

                //关闭计时
                mHandler.removeMessages(H_TIME_WHAT);

                //铃声挂断
                mAudioCallMedia.pausePlay();

                //播放挂断铃声
                mAudioHangupMedia.startPlay(RongCloudManager.callAudioHangup);

                //重置计时器
                callTimer = 0;

                if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                    if (isCallOrReceiver) {
                        if (isCallTo == 1) {
                            //代表只拨打，但是并没有接通
                            saveAudioRecord(recevierId, CallRecord.CALL_STATUS_DIAL);
                        } else if (isCallTo == 2) {
                            saveAudioRecord(recevierId, CallRecord.CALL_STATUS_ANSWER);
                        }
                    } else {
                        if (isReceiverTo == 1) {
                            saveAudioRecord(callUserId, CallRecord.CALL_STATUS_UN_ANSWER);
                        } else if (isReceiverTo == 2) {
                            saveAudioRecord(callUserId, CallRecord.CALL_STATUS_ANSWER);
                        }
                    }

                } else if (rongCallSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                    if (isCallOrReceiver) {
                        if (isCallTo == 1) {
                            //代表只拨打，但是并没有接通
                            saveVideoRecord(recevierId, CallRecord.CALL_STATUS_DIAL);
                        } else if (isCallTo == 2) {
                            saveVideoRecord(recevierId, CallRecord.CALL_STATUS_ANSWER);
                        }
                    } else {
                        if (isReceiverTo == 1) {
                            saveVideoRecord(callUserId, CallRecord.CALL_STATUS_UN_ANSWER);
                        } else if (isReceiverTo == 2) {
                            saveVideoRecord(callUserId, CallRecord.CALL_STATUS_ANSWER);
                        }
                    }
                }

                //如果出现异常,可能无法退出
                MyWindowManager.getInstance().hideView(mFullAudioView);
                MyWindowManager.getInstance().hideView(mSmallAudioView);
                MyWindowManager.getInstance().hideView(mFullVideoView);

                isCallTo = 0;
                isReceiverTo = 0;
            }

            //被叫端正在响铃
            @Override
            public void onRemoteUserRinging(String s) {

            }

            @Override
            public void onRemoteUserAccept(String userId, RongCallCommon.CallMediaType mediaType) {

            }

            //被叫端加入通话
            @Override
            public void onRemoteUserJoined(String s, RongCallCommon.CallMediaType callMediaType, int i, SurfaceView surfaceView) {
                //子线程
                MessageEvent event = new MessageEvent(EventHelper.FLAG_SEND_CAMERA_VIEW);
                event.setmSurfaceView(surfaceView);
                EventHelper.post(event);
            }

            //通话中的某一个参与者邀请好友加入
            @Override
            public void onRemoteUserInvited(String s, RongCallCommon.CallMediaType callMediaType) {

            }

            //通话中的远程参与者离开
            @Override
            public void onRemoteUserLeft(String s, RongCallCommon.CallDisconnectedReason callDisconnectedReason) {

            }

            //媒体切换
            @Override
            public void onMediaTypeChanged(String s, RongCallCommon.CallMediaType callMediaType, SurfaceView surfaceView) {

            }

            //发生错误
            @Override
            public void onError(RongCallCommon.CallErrorCode callErrorCode) {

            }

            //远程端摄像头发生变化
            @Override
            public void onRemoteCameraDisabled(String s, boolean b) {

            }

            //远程端麦克风发生变化
            @Override
            public void onRemoteMicrophoneDisabled(String s, boolean b) {

            }

            //接收丢包率
            @Override
            public void onNetworkReceiveLost(String s, int i) {

            }

            //发送丢包率
            @Override
            public void onNetworkSendLost(int i, int i1) {

            }

            //收到视频第一帧
            @Override
            public void onFirstRemoteVideoFrame(String s, int i, int i1) {

            }

            @Override
            public void onAudioLevelSend(String audioLevel) {

            }

            @Override
            public void onAudioLevelReceive(HashMap<String, String> audioLevel) {

            }

            @Override
            public void onRemoteUserPublishVideoStream(String userId, String streamId, String tag, SurfaceView surfaceView) {

            }

            @Override
            public void onRemoteUserUnpublishVideoStream(String userId, String streamId, String tag) {

            }
        });
    }

    /**
     * 接收消息
     * 解析消息体
     *
     * @param message
     */
    private void parsingImMessage(Message message) {
        LogUtils.i("解析消息中:" + message);
        String objectName = message.getObjectName();
        //文本消息
        if (objectName.equals(RongCloudManager.MSG_TEXT_NAME)) {
            //获取消息主体
            TextMessage textMessage = (TextMessage) message.getContent();
            String content = textMessage.getContent();
            LogUtils.i("content:" + content);
            TextBean textBean = null;
            try {
                LogUtils.i("Gson Try:1");
                textBean = new Gson().fromJson(content, TextBean.class);
                LogUtils.i("Gson Try:2");
            } catch (Exception e) {
                LogUtils.i("Gson Try:" + e.toString());
                e.printStackTrace();
            }
            LogUtils.i("Gson Try:3");
            if (null == textBean) {
                LogUtils.i("Gson Try:4");
                //系统调试消息
                MessageEvent event = new MessageEvent(EventHelper.FLAG_SEND_TEXT);
                event.setText(content);
                event.setUserId(message.getSenderUserId());
                EventHelper.post(event);
                pushSystem(message.getSenderUserId(), 1, 0, 0, content);
                return;
            }
            //普通消息
            if (textBean.getType().equals(RongCloudManager.TYPE_TEXT)) {
                MessageEvent event = new MessageEvent(EventHelper.FLAG_SEND_TEXT);
                event.setText(textBean.getMsg());
                event.setUserId(message.getSenderUserId());
                EventHelper.post(event);
                pushSystem(message.getSenderUserId(), 1, 0, 0, textBean.getMsg());
            }
            //添加好友消息
            else if (textBean.getType().equals(RongCloudManager.TYPE_ADD_FRIEND)) {
                //存入数据库 Bmob RongCloud 都没有提供存储方法
                //使用另外的方法来实现 存入本地数据库
                LogUtils.i("添加好友消息");
                //saveNewFriend(textBean.getMsg(), message.getSenderUserId());
                //查询数据库如果有重复的则不添加
                //防止漏了消息，暂时对消息不过滤处理
                TextBean finalTextBean = textBean;
                disposable = Observable.create((ObservableOnSubscribe<List<NewFriend>>) emitter -> {
                    emitter.onNext(LitePalManager.getInstance().queryNewFriend());
                    emitter.onComplete();
                }).subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(newFriends -> {
                            if (CommonUtils.isEmpty(newFriends)) {
                                boolean isHave = false;
                                for (int j = 0; j < newFriends.size(); j++) {
                                    NewFriend newFriend = newFriends.get(j);
                                    if (message.getSenderUserId().equals(newFriend.getId())) {
                                        isHave = true;
                                        break;
                                    }
                                }
                                //防止重复添加
                                if (!isHave) {
                                    saveNewFriend(finalTextBean.getMsg(), message.getSenderUserId());
                                }
                            } else {
                                saveNewFriend(finalTextBean.getMsg(), message.getSenderUserId());
                            }
                        });
            }
            //同意添加好友消息
            else if (textBean.getType().equals(RongCloudManager.TYPE_ARGEED_FRIEND)) {
                // 判断是否还是好友
                BmobManager.getInstance().queryMyFriends(new FindListener<Friend>() {
                    @Override
                    public void done(List<Friend> list, BmobException e) {
                        if (e == null) {
                            if (CommonUtils.isEmpty(list)) {
                                //你有一个好友列表
                                for (int i = 0; i < list.size(); i++) {
                                    Friend friend = list.get(i);
                                    //判断这个对象中的id是否跟我目前的userId相同
                                    if (!friend.getFriendUser().getObjectId().equals(message.getSenderUserId())) {
                                        //确定数据库中没有，再添加
                                        //1.添加到好友列表
                                        BmobManager.getInstance().addFriend(message.getSenderUserId(), new SaveListener<String>() {
                                            @Override
                                            public void done(String s, BmobException e) {
                                                if (e == null) {
                                                    LogUtils.i("添加到好友列表"+message.getSenderUserId());
                                                    pushSystem(message.getSenderUserId(), 0, 1, 0, "");
                                                    //2.刷新好友列表
                                                    EventHelper.post(EventHelper.FLAG_UPDATE_FRIEND_LIST);
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                            else {
                                BmobManager.getInstance().addFriend(message.getSenderUserId(), new SaveListener<String>() {
                                    @Override
                                    public void done(String s, BmobException e) {
                                        if (e == null) {
                                            LogUtils.i("添加到好友列表"+message.getSenderUserId());
                                            pushSystem(message.getSenderUserId(), 0, 1, 0, "");
                                            //2.刷新好友列表
                                            EventHelper.post(EventHelper.FLAG_UPDATE_FRIEND_LIST);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
        }
        else if (objectName.equals(RongCloudManager.MSG_IMAGE_NAME)) {
            try {
                ImageMessage imageMessage = (ImageMessage) message.getContent();
                String url = imageMessage.getRemoteUri().toString();
                if (!TextUtils.isEmpty(url)) {
                    LogUtils.i("url:" + url);
                    MessageEvent event = new MessageEvent(EventHelper.FLAG_SEND_IMAGE);
                    event.setImgUrl(url);
                    event.setUserId(message.getSenderUserId());
                    EventHelper.post(event);
                    pushSystem(message.getSenderUserId(), 1, 0, 0, getString(R.string.text_chat_record_img));
                }
            } catch (Exception e) {
                LogUtils.e("e." + e.toString());
                e.printStackTrace();
            }
        }
        else if (objectName.equals(RongCloudManager.MSG_LOCATION_NAME)) {
            LocationMessage locationMessage = (LocationMessage) message.getContent();
            LogUtils.e("locationMessage:" + locationMessage.toString());
            MessageEvent event = new MessageEvent(EventHelper.FLAG_SEND_LOCATION);
            event.setLa(locationMessage.getLat());
            event.setLo(locationMessage.getLng());
            event.setUserId(message.getSenderUserId());
            event.setAddress(locationMessage.getPoi());
            EventHelper.post(event);
            pushSystem(message.getSenderUserId(), 1, 0, 0, getString(R.string.text_chat_record_location));
        }
    }

    /**
     * 保存新朋友
     *
     * @param msg
     * @param senderUserId
     */
    private void saveNewFriend(String msg, String senderUserId) {
        pushSystem(senderUserId, 0, 0, 0, msg);
        LitePalManager.getInstance().saveNewFriend(msg, senderUserId);
        LogUtils.i("save...");
    }

    /**
     * 更新窗口上的用户信息
     *
     * @param index 媒体类型
     * @param index 0:接收 1：拨打
     * @param id
     */
    private void updateWindowInfo(final int index, final RongCallCommon.CallMediaType type, String id) {
        //音频
        if (type.equals(RongCallCommon.CallMediaType.AUDIO)) {
            if (index == 0) {
                goneAudioView(false, true, true, false, false);
            } else if (index == 1) {
                goneAudioView(false, false, true, false, false);
            }
            //视频
        } else if (type.equals(RongCallCommon.CallMediaType.VIDEO)) {
            if (index == 0) {
                goneVideoView(true, false, false, true, true, false);
            } else if (index == 1) {
                goneVideoView(true, false, true, false, true, false);
            }
        }

        //加载信息
        BmobManager.getInstance().queryObjectIdUser(id, new FindListener<PlanetUser>() {
            @Override
            public void done(List<PlanetUser> list, BmobException e) {
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        PlanetUser imUser = list.get(0);
                        //音频
                        if (type.equals(RongCallCommon.CallMediaType.AUDIO)) {
                            GlideHelper.loadUrl(CloudService.this, imUser.getPhoto(), audio_iv_photo);
                            if (index == 0) {
                                audio_tv_status.setText(imUser.getNickName() + getString(R.string.text_service_calling));
                            } else if (index == 1) {
                                audio_tv_status.setText(getString(R.string.text_service_call_ing) + imUser.getNickName() + "...");
                            }
                            //视频
                        } else if (type.equals(RongCallCommon.CallMediaType.VIDEO)) {
                            GlideHelper.loadUrl(CloudService.this, imUser.getPhoto(), video_iv_photo);
                            video_tv_name.setText(imUser.getNickName());
                            if (index == 0) {
                                video_tv_status.setText(imUser.getNickName() + getString(R.string.text_service_video_calling));
                            } else if (index == 1) {
                                video_tv_status.setText(getString(R.string.text_service_call_video_ing) + imUser.getNickName() + "...");
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 控制音频窗口的控件显示或者隐藏
     *
     * @param recording
     * @param answer
     * @param hangup
     * @param hf
     * @param small
     */
    private void goneAudioView(boolean recording, boolean answer, boolean hangup, boolean hf,
                               boolean small) {
        // 录音 接听 挂断 免提 最小化
        //audio_ll_recording.setVisibility(recording ? View.VISIBLE : View.GONE);
        audio_ll_answer.setVisibility(answer ? View.VISIBLE : View.GONE);
        audio_ll_hangup.setVisibility(hangup ? View.VISIBLE : View.GONE);
        audio_ll_hf.setVisibility(hf ? View.VISIBLE : View.GONE);
        audio_iv_small.setVisibility(small ? View.VISIBLE : View.GONE);
    }

    /**
     * 控制视频窗口的控件显示或者隐藏
     * * @param info
     * * @param small
     *
     * @param answer
     * @param hangup
     * @param time
     */
    private void goneVideoView(boolean info, boolean small,
                               boolean big, boolean answer, boolean hangup,
                               boolean time) {
        // 个人信息 小窗口  接听  挂断 时间
        video_ll_info.setVisibility(info ? View.VISIBLE : View.GONE);
        video_small_video.setVisibility(small ? View.VISIBLE : View.GONE);
        video_big_video.setVisibility(big ? View.VISIBLE : View.GONE);
        video_ll_answer.setVisibility(answer ? View.VISIBLE : View.GONE);
        video_ll_hangup.setVisibility(hangup ? View.VISIBLE : View.GONE);
        video_tv_time.setVisibility(time ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
        EventHelper.unregister(this);
    }

    private boolean isRecording = false;
    private boolean isHF = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audio_ll_answer:
            case R.id.video_ll_answer:
                //接听
                RongCloudManager.getInstance().acceptCall(callId);
                break;
            case R.id.audio_ll_hangup:
            case R.id.video_ll_hangup:
                //挂断
                RongCloudManager.getInstance().hangUpCall(callId);
                break;
            case R.id.audio_ll_hf:
                isHF = !isHF;
                RongCloudManager.getInstance().setEnableSpeakerphone(isHF);
                audio_iv_hf.setImageResource(isHF ? R.drawable.img_hf_p : R.drawable.img_hf);
                break;
            case R.id.audio_iv_small:
                //最小化
                MyWindowManager.getInstance().hideView(mFullAudioView);
                MyWindowManager.getInstance().showView(mSmallAudioView, lpSmallView);
                break;
            case R.id.video_small_video:
                isSmallShowLocal = !isSmallShowLocal;
                //小窗切换
                updateVideoView();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()) {
            case EventHelper.FLAG_SEND_CAMERA_VIEW:
                SurfaceView sv = event.getmSurfaceView();
                if (sv != null) {
                    mRemoteView = sv;
                }
                updateVideoView();
                break;
        }
    }

    /**
     * 更新视频流
     */
    private void updateVideoView() {
        video_big_video.removeAllViews();
        video_small_video.removeAllViews();

        if (isSmallShowLocal) {
            if (mLocalView != null) {
                video_small_video.addView(mLocalView);
                mLocalView.setZOrderOnTop(true);
            }
            if (mRemoteView != null) {
                video_big_video.addView(mRemoteView);
                mRemoteView.setZOrderOnTop(false);
            }
        } else {
            if (mLocalView != null) {
                video_big_video.addView(mLocalView);
                mLocalView.setZOrderOnTop(false);
            }
            if (mRemoteView != null) {
                video_small_video.addView(mRemoteView);
                mRemoteView.setZOrderOnTop(true);
            }
        }
    }

    /**
     * 保存音频记录
     *
     * @param id
     * @param callStatus
     */
    private void saveAudioRecord(String id, int callStatus) {
        LitePalManager.getInstance()
                .saveCallRecord(id, CallRecord.MEDIA_TYPE_AUDIO, callStatus);
    }

    /**
     * 保存视频记录
     *
     * @param id
     * @param callStatus
     */
    private void saveVideoRecord(String id, int callStatus) {
        LitePalManager.getInstance()
                .saveCallRecord(id, CallRecord.MEDIA_TYPE_VIDEO, callStatus);
    }

    /**
     * @param id          发消息id
     * @param type        0：特殊消息 1：聊天消息
     * @param friendType  0: 添加好友请求 1：同意好友请求
     * @param messageType 0：文本  1：图片 2：位置
     */
    private void pushSystem(final String id, final int type, final int friendType, final int messageType, final String msgText) {
        LogUtils.i("pushSystem");
        BmobManager.getInstance().queryObjectIdUser(id, new FindListener<PlanetUser>() {
            @Override
            public void done(List<PlanetUser> list, BmobException e) {
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        PlanetUser imUser = list.get(0);
                        String text = "";
                        if (type == 0) {
                            switch (friendType) {
                                case 0:
                                    text = imUser.getNickName() + getString(R.string.text_server_noti_send_text);
                                    LogUtils.i(text);
                                    break;
                                case 1:
                                    text = imUser.getNickName() + getString(R.string.text_server_noti_receiver_text);
                                    LogUtils.i(text);
                                    break;
                            }
                        } else if (type == 1) {
                            switch (messageType) {
                                case 0:
                                    text = msgText;
                                    break;
                                case 1:
                                    text = getString(R.string.text_chat_record_img);
                                    break;
                                case 2:
                                    text = getString(R.string.text_chat_record_location);
                                    break;
                            }
                        }
                        pushBitmap(type, friendType, imUser, imUser.getNickName(), text, imUser.getPhoto());
                    }
                }
            }
        });
    }

    /**
     * 发送通知
     *
     * @param type       0：特殊消息 1：聊天消息
     * @param friendType 0: 添加好友请求 1：同意好友请求
     * @param imUser     用户对象
     * @param title      标题
     * @param text       内容
     * @param url        头像Url
     */
    private void pushBitmap(final int type, final int friendType, final PlanetUser imUser, final String title, final String text, String url) {
        LogUtils.i("pushBitmap");
        GlideHelper.loadUrlToBitmap(this, url, new GlideHelper.OnGlideBitmapResultListener() {
            @Override
            public void onResourceReady(Bitmap resource) {
                if (type == 0) {
                    if (friendType == 0) {
                        Intent intent = new Intent(CloudService.this, NewFriendActivity.class);
                        PendingIntent pi = PendingIntent.getActivities(CloudService.this, 0, new Intent[]{intent}, PendingIntent.FLAG_CANCEL_CURRENT);
                        NoticeManager.getInstance().pushAddFriendNotification(imUser.getObjectId(), title, text, resource, pi);
                    } else if (friendType == 1) {
                        Intent intent = new Intent(CloudService.this, MainActivity.class);
                        PendingIntent pi = PendingIntent.getActivities(CloudService.this, 0, new Intent[]{intent}, PendingIntent.FLAG_CANCEL_CURRENT);
                        NoticeManager.getInstance().pushArgeedFriendNotification(imUser.getObjectId(), title, text, resource, pi);
                    }
                } else if (type == 1) {
                    Intent intent = new Intent(CloudService.this, ChatActivity.class);
                    intent.putExtra(Constants.INTENT_USER_ID, imUser.getObjectId());
                    intent.putExtra(Constants.INTENT_USER_NAME, imUser.getNickName());
                    intent.putExtra(Constants.INTENT_USER_PHOTO, imUser.getPhoto());
                    PendingIntent pi = PendingIntent.getActivities(CloudService.this, 0, new Intent[]{intent}, PendingIntent.FLAG_CANCEL_CURRENT);
                    NoticeManager.getInstance().pushMessageNotification(imUser.getObjectId(), title, text, resource, pi);
                }
            }
        });
    }
}

