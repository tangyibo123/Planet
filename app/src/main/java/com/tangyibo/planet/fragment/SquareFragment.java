package com.tangyibo.planet.fragment;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tangyibo.framework.base.BaseFragment;
import com.tangyibo.framework.bmob.BmobManager;
import com.tangyibo.framework.bmob.PlanetUser;
import com.tangyibo.framework.bmob.SquareSet;
import com.tangyibo.framework.helper.MediaPlayerHelper;
import com.tangyibo.framework.manager.FileManager;
import com.tangyibo.framework.manager.MyWindowManager;
import com.tangyibo.framework.utils.AnimUtils;
import com.tangyibo.framework.utils.CommonUtils;
import com.tangyibo.framework.utils.TimeUtils;
import com.tangyibo.framework.view.VideoJzvdStd;
import com.tangyibo.framework.view.adapter.CommonAdapter;
import com.tangyibo.framework.view.adapter.CommonViewHolder;
import com.tangyibo.planet.R;
import com.tangyibo.planet.ui.normal.ImagePreviewActivity;
import com.tangyibo.planet.ui.square.PushSquareActivity;
import com.tangyibo.planet.ui.normal.UserInfoActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 1.?????????????????????????????? SquareSet
 * 2.??????????????????????????? PsuhSquareActivity
 * 3.???????????? ?????????????????????????????????????????????
 */

public class SquareFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener{

    private static final int REQUEST_CODE = 1000;

    private ImageView iv_push;
    private RecyclerView mSquareView;
    private SwipeRefreshLayout mSquareSwipeLayout;
    private View item_empty_view;

    //????????????
    private FloatingActionButton fb_squaue_top;

    private List<SquareSet> mList = new ArrayList<>();
    private CommonAdapter<SquareSet> mSquareAdapter;

    private SimpleDateFormat dateFormat;

    //??????
    private MediaPlayerHelper mMusicManager;
    //?????????????????????
    private boolean isMusicPlay = false;

    //???????????????
    private WindowManager.LayoutParams lpMusicParams;
    private View musicWindowView;
    private ImageView iv_music_photo;
    private ProgressBar pb_music_pos;
    private TextView tv_music_cur;
    private TextView tv_music_all;

    //????????????
    private boolean isMove = false;
    //????????????
    private boolean isDrag = false;
    private int mLastX;
    private int mLastY;

    //????????????
    private ObjectAnimator objAnimMusic;

    //????????????
    private static final int UPDATE_POS = 1235;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_POS:
                    int pos = msg.arg1;
                    tv_music_cur.setText(TimeUtils.formatDuring(pos));
                    pb_music_pos.setProgress(pos);
                    break;
            }
            return false;
        }
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_square, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        initMusicWindow();

        mMusicManager = new MediaPlayerHelper();
        mMusicManager.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isMusicPlay = false;
            }
        });

        mMusicManager.setOnProgressListener(new MediaPlayerHelper.OnMusicProgressListener() {
            @Override
            public void OnProgress(int progress, int pos) {
                Message message = new Message();
                message.what = UPDATE_POS;
                message.arg1 = progress;
                mHandler.sendMessage(message);
            }
        });

        iv_push = view.findViewById(R.id.iv_push);
        mSquareView = view.findViewById(R.id.mSquareView);
        mSquareSwipeLayout = view.findViewById(R.id.mSquareSwipeLayout);
        item_empty_view = view.findViewById(R.id.item_empty_view);
        fb_squaue_top = view.findViewById(R.id.fb_square_top);

        iv_push.setOnClickListener(this);
        fb_squaue_top.setOnClickListener(this);
        mSquareSwipeLayout.setOnRefreshListener(this);

        mSquareView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSquareView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        //????????????
        ((SimpleItemAnimator) mSquareView.getItemAnimator()).setSupportsChangeAnimations(false);

        mSquareAdapter = new CommonAdapter<>(mList, new CommonAdapter.OnMoreBindDataListener<SquareSet>() {
            @Override
            public int getItemType(int position) {
                return position;
            }

            @Override
            public void onBindViewHolder(final SquareSet model, CommonViewHolder viewHolder, int type, int position) {
                //??????????????????
                //loadMeInfo(model.getUserId(), viewHolder);
                BmobManager.getInstance().queryObjectIdUser(model.getUserId(), new FindListener<PlanetUser>() {
                    @Override
                    public void done(List<PlanetUser> list, BmobException e) {
                        if (e == null) {
                            if (CommonUtils.isEmpty(list)) {
                                PlanetUser imUser = list.get(0);
                                if (!TextUtils.isEmpty(imUser.getPhoto())) {
                                    viewHolder.setImageUrl(getActivity(), R.id.iv_photo, imUser.getPhoto(), 50, 50);
                                }
                                viewHolder.setText(R.id.tv_nickname, imUser.getNickName());
                                viewHolder.setText(R.id.tv_square_age, imUser.getAge() + getString(R.string.text_search_age));
                                //??????????????????????????????
                                String constellation = imUser.getConstellation();
                                if (!TextUtils.isEmpty(constellation)) {
                                    viewHolder.setText(R.id.tv_square_constellation, constellation);
                                    viewHolder.setVisibility(R.id.tv_square_constellation, View.VISIBLE);
                                }

                                String hobby = imUser.getHobby();
                                if (!TextUtils.isEmpty(hobby)) {
                                    viewHolder.setText(R.id.tv_square_hobby, "???" + hobby);
                                    viewHolder.setVisibility(R.id.tv_square_hobby, View.VISIBLE);
                                }
                                String status = imUser.getStatus();
                                if (!TextUtils.isEmpty(status)) {
                                    viewHolder.setText(R.id.tv_square_status, imUser.getStatus());
                                    viewHolder.setVisibility(R.id.tv_square_status, View.VISIBLE);
                                }
                            }
                        }
                    }
                });
                //????????????
                viewHolder.setText(R.id.tv_time, dateFormat.format(model.getPushTime()));

                //????????????????????????
                viewHolder.getView(R.id.iv_photo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserInfoActivity.startActivity(getActivity(), model.getUserId());
                    }
                });

                if (!TextUtils.isEmpty(model.getText())) {
                    viewHolder.setText(R.id.tv_text, model.getText());
                } else {
                    viewHolder.setVisibility(R.id.tv_text, View.GONE);
                }

                //?????????
                switch (model.getPushType()) {
                    case SquareSet.PUSH_TEXT:
                        goneItemView(viewHolder, false, false, false);
                        break;
                    case SquareSet.PUSH_IMAGE:
                        goneItemView(viewHolder, true, false, false);
                        viewHolder.setImageUrl(getActivity(), R.id.iv_img, model.getMediaUrl());
                        viewHolder.getView(R.id.iv_img).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImagePreviewActivity.startActivity(getActivity(), true, model.getMediaUrl());
                            }
                        });
                        break;
                    case SquareSet.PUSH_MUSIC:
                        goneItemView(viewHolder, false, true, false);
                        viewHolder.getView(R.id.ll_music).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //???????????????
                                if (!checkWindowPermissions()) {
                                    requestWindowPermissions();
                                } else {
                                    //????????????
                                    if (mMusicManager.isPlaying()) {
                                        hideMusicWindow();
                                    } else {
                                        if (isMusicPlay) {
                                            mMusicManager.continuePlay();
                                        } else {
                                            mMusicManager.startPlay(model.getMediaUrl());
                                            isMusicPlay = true;
                                        }
                                        showMusicWindow();
                                    }
                                }
                            }
                        });
                        break;
                    case SquareSet.PUSH_VIDEO:
                        goneItemView(viewHolder, false, false, true);
                        viewHolder.setVisibility(R.id.tv_text, View.GONE);

                        //?????????????????????
                        final VideoJzvdStd jzvdStd = viewHolder.getView(R.id.jz_video);
                        jzvdStd.setUp(model.getMediaUrl(), model.getText());
                        Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
                            Bitmap mBitmap = FileManager.getInstance()
                                    .getNetVideoBitmap(model.getMediaUrl());
                            if (mBitmap != null) {
                                emitter.onNext(mBitmap);
                                emitter.onComplete();
                            }
                        }).subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(bitmap -> {
                                    if (bitmap != null) {
                                        jzvdStd.thumbImageView.setImageBitmap(bitmap);
                                    }
                                });
                        break;
                }

            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_square_item;
            }
        });
        mSquareView.setAdapter(mSquareAdapter);

        //??????????????????
        mSquareView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                        int position = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                        if(position > 5 ){
                            fb_squaue_top.setVisibility(View.VISIBLE);
                        }else {
                            fb_squaue_top.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (!CommonUtils.isEmpty(mList)) {
                loadSquare();
            }
        }
    }

    // ????????????????????????
    private void initMusicWindow() {
        lpMusicParams = MyWindowManager.getInstance().createLayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.START);
        musicWindowView = MyWindowManager.getInstance().getView(R.layout.layout_square_music_item);

        //?????????View
        iv_music_photo = musicWindowView.findViewById(R.id.iv_music_photo);
        pb_music_pos = musicWindowView.findViewById(R.id.pb_music_pos);
        tv_music_cur = musicWindowView.findViewById(R.id.tv_music_cur);
        tv_music_all = musicWindowView.findViewById(R.id.tv_music_all);

        objAnimMusic = AnimUtils.rotation(iv_music_photo);

        musicWindowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMusicWindow();
            }
        });

        musicWindowView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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

                        //?????????
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

                        //??????
                        lpMusicParams.x += dx;
                        lpMusicParams.y += dy;

                        //????????????
                        mLastX = mStartX;
                        mLastY = mStartY;

                        //WindowManager addView removeView updateView
                        MyWindowManager.getInstance().updateView(musicWindowView, lpMusicParams);

                        break;
                }
                return isDrag;
            }
        });
    }

    // ????????????
    private void showMusicWindow() {
        pb_music_pos.setMax(mMusicManager.getDuration());
        tv_music_all.setText(TimeUtils.formatDuring(mMusicManager.getDuration()));
        objAnimMusic.start();
        MyWindowManager.getInstance().showView(musicWindowView, lpMusicParams);
    }

    // ????????????
    private void hideMusicWindow() {
        mMusicManager.pausePlay();
        objAnimMusic.pause();
        MyWindowManager.getInstance().hideView(musicWindowView);
    }

    /**
     * ??????View
     *
     * @param viewHolder
     * @param img
     * @param audio
     * @param video
     */
    private void goneItemView(CommonViewHolder viewHolder,
                              boolean img, boolean audio, boolean video) {
        viewHolder.getView(R.id.tv_text).setVisibility(View.VISIBLE);
        viewHolder.getView(R.id.iv_img).setVisibility(img ? View.VISIBLE : View.GONE);
        viewHolder.getView(R.id.ll_music).setVisibility(audio ? View.VISIBLE : View.GONE);
        viewHolder.getView(R.id.ll_video).setVisibility(video ? View.VISIBLE : View.GONE);
    }

    /**
     * ????????????
     */
    private void loadSquare() {
        mSquareSwipeLayout.setRefreshing(true);
        BmobManager.getInstance().queryAllSquare(new FindListener<SquareSet>() {
            @Override
            public void done(List<SquareSet> list, BmobException e) {
                mSquareSwipeLayout.setRefreshing(false);
                if (e == null) {
                    if (CommonUtils.isEmpty(list)) {
                        //??????
                        Collections.reverse(list);
                        mSquareView.setVisibility(View.VISIBLE);
                        item_empty_view.setVisibility(View.GONE);
                        if (mList.size() > 0) {
                            mList.clear();
                        }
                        mList.addAll(list);
                        mSquareAdapter.notifyDataSetChanged();
                    } else {
                        mSquareView.setVisibility(View.GONE);
                        item_empty_view.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_push:
                Intent intent = new Intent(getActivity(), PushSquareActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.fb_square_top:
                mSquareView.smoothScrollToPosition(0);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                //??????
                loadSquare();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRefresh() {
        loadSquare();
    }
}
