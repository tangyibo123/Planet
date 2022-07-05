package com.tangyibo.framework.manager;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.tangyibo.framework.R;
import com.tangyibo.framework.utils.LogUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.rong.calllib.IRongCallListener;
import io.rong.calllib.IRongReceivedCallListener;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.listener.OnReceiveMessageWrapperListener;
import io.rong.imlib.location.message.LocationMessage;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;

public class RongCloudManager {

    //融云服务端 API 的接口 (Token代表一个用户在融云的唯一身份标识,提供准确的 App Key / Secret，使用用户ID换取对应的Token）
    public static final String TOKEN_URL = "https://api-cn.ronghub.com/user/getToken.json";
    //RongCloud App Key
    public static final String RongCloud_KEY = "x18ywvqfxysrc";
    public static final String RongCloud_SECRET = "ygyTJBrI286O";

    //ObjectName
    public static final String MSG_TEXT_NAME = "RC:TxtMsg";
    public static final String MSG_IMAGE_NAME = "RC:ImgMsg";
    public static final String MSG_LOCATION_NAME = "RC:LBSMsg";

    //Msg Type

    //普通消息
    public static final String TYPE_TEXT = "TYPE_TEXT";
    //添加好友消息
    public static final String TYPE_ADD_FRIEND = "TYPE_ADD_FRIEND";
    //同意添加好友的消息
    public static final String TYPE_ARGEED_FRIEND = "TYPE_ARGEED_FRIEND";

    //来电铃声
    public static final String callAudioPath = "http://downsc.chinaz.net/Files/DownLoad/sound1/201501/5363.wav";
    //挂断铃声
    public static final String callAudioHangup = "http://downsc.chinaz.net/Files/DownLoad/sound1/201501/5351.wav";

    private static volatile RongCloudManager mInstnce = null;

    public static RongCloudManager getInstance() {
        if (mInstnce == null) {
            synchronized (RongCloudManager.class) {
                if (mInstnce == null) {
                    mInstnce = new RongCloudManager();
                }
            }
        }
        return mInstnce;
    }

    //---------------------------------连接初始化相关--------------------------------------------------
    //初始化SDK
    public void initRongCloud(Context mContext) {
        LogUtils.i("融云服务初始化中 ... ");
        RongIMClient.init(mContext, RongCloud_KEY);
    }

    //连接融云服务
    public void connectRongCloud(String token) {
        int timeLimit = 10;
        LogUtils.i("融云服务连接中 ... ");
        RongIMClient.connect(token, timeLimit, new RongIMClient.ConnectCallback() {
            @Override
            public void onDatabaseOpened(RongIMClient.DatabaseOpenStatus databaseOpenStatus) {
                if(RongIMClient.DatabaseOpenStatus.DATABASE_OPEN_SUCCESS.equals(databaseOpenStatus)) {
                    //本地数据库打开，跳转到会话列表页面
                    LogUtils.e("本地数据库打开，即将跳转到会话列表~");
                } else {
                    //数据库打开失败，可以弹出 toast 提示。
                    LogUtils.e("数据库打开失败，请重试！");
                }
            }
            @Override
            public void onSuccess(String s) {
                LogUtils.e("连接成功：" + s);
                //sendConnectStatus(true);
            }

            @Override
            public void onError(RongIMClient.ConnectionErrorCode connectionErrorCode) {
                LogUtils.e("连接失败：" + connectionErrorCode);
                //sendConnectStatus(false);

            }

        });
    }

    //连接状态监听
    public void setConnectionStatusListener(RongIMClient.ConnectionStatusListener connectionStatusListener) {
        RongIMClient.setConnectionStatusListener(connectionStatusListener);
    }

    //是否连接
    public boolean isConnect() {
        return RongIMClient.getInstance().getCurrentConnectionStatus()
                == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED;
    }

    //断开连接，有新消息时，仍然能够收到通知提醒
    public void disconnect() {
        RongIMClient.getInstance().disconnect();
    }

    //退出登录，彻底注销登录信息，有新消息时不再收到任何通知提醒
    public void logout() {
        RongIMClient.getInstance().logout();
    }

    //-------------------------------------即时通讯-消息相关---------------------------------------------------

    /**
     * 接收消息的监听器
     *
     * @param listener
     */
    public void setOnReceiveMessageListener(OnReceiveMessageWrapperListener listener) {
        RongCoreClient.addOnReceiveMessageListener(listener);
    }

    //发送文本消息的结果回调
    private IRongCallback.ISendMessageCallback iSendMessageCallback
            = new IRongCallback.ISendMessageCallback() {

        @Override
        public void onAttached(Message message) {
            // 消息成功存到本地数据库的回调
            LogUtils.i("sendMessage onAttached");
        }

        @Override
        public void onSuccess(Message message) {
            // 消息发送成功的回调
            LogUtils.i("sendMessage onSuccess");
        }

        @Override
        public void onError(Message message, RongIMClient.ErrorCode errorCode) {
            // 消息发送失败的回调
            LogUtils.e("sendMessage onError:" + errorCode);
        }
    };

    /**
     * 发送文本消息
     * 一个手机 发送
     * 另外一个手机 接收
     *
     * @param msg 消息内容
     * @param targetId 对方id
     */
    private void sendTextMessage(String msg, String targetId) {
        LogUtils.i("sendTextMessage");
        //构造Message消息实体
        Conversation.ConversationType conversationType = Conversation.ConversationType.PRIVATE;
        TextMessage messageContent = TextMessage.obtain(msg);
        Message message = Message.obtain(targetId, conversationType, messageContent);
        RongIMClient.getInstance().sendMessage(
                message,
                null,
                null,
                iSendMessageCallback
        );
    }

    /**
     * 发送文本消息
     *
     * @param msg
     * @param type
     * @param targetId
     */
    public void sendTextMessage(String msg, String type, String targetId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("msg", msg);
            //如果没有这个Type 就是一条普通消息
            jsonObject.put("type", type);
            sendTextMessage(jsonObject.toString(), targetId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送图片消息的结果回调
    private RongIMClient.SendImageMessageCallback sendImageMessageCallback = new RongIMClient.SendImageMessageCallback() {
        @Override
        public void onAttached(Message message) {
            LogUtils.i("onAttached");
        }

        @Override
        public void onError(Message message, RongIMClient.ErrorCode errorCode) {
            LogUtils.i("onError:" + errorCode);
        }

        @Override
        public void onSuccess(Message message) {
            LogUtils.i("onSuccess");
        }

        @Override
        public void onProgress(Message message, int i) {
            LogUtils.i("onProgress:" + i);
        }
    };

    /**
     * 发送图片消息
     *
     * @param targetId 对方ID
     * @param file     文件
     */
    public void sendImageMessage(String targetId, File file) {
        ImageMessage imageMessage = ImageMessage.obtain(Uri.fromFile(file), Uri.fromFile(file), true);
        RongIMClient.getInstance().sendImageMessage(
                Conversation.ConversationType.PRIVATE,
                targetId,
                imageMessage,
                null,
                null,
                sendImageMessageCallback);
    }

    /**
     * 发送位置信息
     *
     * @param mTargetId
     * @param lat
     * @param lng
     * @param poi
     */
    public void sendLocationMessage(String mTargetId, double lat, double lng, String poi) {
        LocationMessage locationMessage = LocationMessage.obtain(lat, lng, poi, null);
        io.rong.imlib.model.Message message = io.rong.imlib.model.Message.obtain(
                mTargetId, Conversation.ConversationType.PRIVATE, locationMessage);
        RongIMClient.getInstance().sendLocationMessage(message,
                null, null, iSendMessageCallback);
    }

    /**
     * 查询本地的会话记录
     *
     * @param callback
     */
    public void getConversationList(RongIMClient.ResultCallback<List<Conversation>> callback) {
        RongIMClient.getInstance().getConversationList(callback);
    }

    /**
     * 加载本地的历史记录
     *
     * @param targetId
     * @param callback
     */
    public void getHistoryMessages(String targetId, RongIMClient.ResultCallback<List<Message>> callback) {
        RongIMClient.getInstance().getHistoryMessages(Conversation.ConversationType.PRIVATE
                , targetId, -1, 1000, callback);
    }

    /**
     * 获取服务器的历史记录
     *
     * @param targetId
     * @param callback
     */
    public void getRemoteHistoryMessages(String targetId, RongIMClient.ResultCallback<List<Message>> callback) {
        RongIMClient.getInstance().getRemoteHistoryMessages(Conversation.ConversationType.PRIVATE
                , targetId, 0, 20, callback);
    }

    //-------------------------Call Api-------------------------------

    /**
     * 拨打视频/音频
     *
     * @param targetId
     * @param type
     */
    public void startCall(Context mContext, String targetId, RongCallCommon.CallMediaType type) {
        //检查设备可用
        if (!isVoIPEnabled(mContext)) {
            return;
        }
        if(!isConnect()){
            Toast.makeText(mContext, mContext.getString(R.string.text_server_status), Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> userIds = new ArrayList<>();
        userIds.add(targetId);
        RongCallClient.getInstance().startCall(
                Conversation.ConversationType.PRIVATE,
                targetId,
                userIds,
                null,
                type,
                null);
    }

    /**
     * 音频
     *
     * @param targetId
     */
    public void startAudioCall(Context mContext, String targetId) {
        startCall(mContext, targetId, RongCallCommon.CallMediaType.AUDIO);
    }

    /**
     * 视频
     *
     * @param targetId
     */
    public void startVideoCall(Context mContext, String targetId) {
        startCall(mContext, targetId, RongCallCommon.CallMediaType.VIDEO);
    }

    /**
     * 监听音频通话
     *
     * @param listener
     */
    public void setReceivedCallListener(IRongReceivedCallListener listener) {
        if (null == listener) {
            return;
        }
        RongCallClient.setReceivedCallListener(listener);
    }

    /**
     * 接听
     *
     * @param callId
     */
    public void acceptCall(String callId) {
        LogUtils.i("acceptCall:" + callId);
        RongCallClient.getInstance().acceptCall(callId);
    }

    /**
     * 挂断
     *
     * @param callId
     */
    public void hangUpCall(String callId) {
        LogUtils.i("hangUpCall:" + callId);
        RongCallClient.getInstance().hangUpCall(callId);
    }

    /**
     * 切换媒体
     *
     * @param mediaType
     */
    public void changeCallMediaType(RongCallCommon.CallMediaType mediaType) {
        RongCallClient.getInstance().changeCallMediaType(mediaType);
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        RongCallClient.getInstance().switchCamera();
    }

    /**
     * 摄像头开关
     *
     * @param enabled
     */
    public void setEnableLocalVideo(boolean enabled) {
        RongCallClient.getInstance().setEnableLocalVideo(enabled);
    }

    /**
     * 音频开关
     *
     * @param enabled
     */
    public void setEnableLocalAudio(boolean enabled) {
        RongCallClient.getInstance().setEnableLocalAudio(enabled);
    }

    /**
     * 免提开关
     *
     * @param enabled
     */
    public void setEnableSpeakerphone(boolean enabled) {
        RongCallClient.getInstance().setEnableSpeakerphone(enabled);
    }


    /**
     * 监听通话状态
     *
     * @param listener
     */
    public void setVoIPCallListener(IRongCallListener listener) {
        if (null == listener) {
            return;
        }
        RongCallClient.getInstance().setVoIPCallListener(listener);
    }

    /**
     * 检查设备是否可用通话
     *
     * @param mContext
     */
    public boolean isVoIPEnabled(Context mContext) {
        if (!RongCallClient.getInstance().isVoIPEnabled(mContext)) {
            Toast.makeText(mContext, mContext.getString(R.string.text_devices_not_supper_audio), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
