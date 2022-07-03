package com.tangyibo.planet.ui;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.tangyibo.framework.base.BaseActivity;
import com.tangyibo.framework.helper.MediaPlayerHelper;
import com.tangyibo.framework.view.adapter.BasePagerAdapter;
import com.tangyibo.planet.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 引导页
 * 1.ViewPager : 适配器|帧动画播放
 * 2.歌曲的播放
 * 3.属性动画旋转
 * 4.跳转
 */

public class GuideActivity extends BaseActivity implements View.OnClickListener {

    private ImageView iv_music_switch; //音乐图标
    private TextView tv_guide_skip; //跳过
    private Button bu_exp; //立即体验
    private ViewPager mViewPager;

    private View view1; //guide_1
    private View view2; //guide_2
    private View view3; //guide_3

    private List<View> mPageList = new ArrayList<>(); //用list保存三个页面view
    public BasePagerAdapter mPagerAdapter;

    private MediaPlayerHelper mediaPlayerHelper;

    private void initView() {
        iv_music_switch = (ImageView) findViewById(R.id.iv_music_switch);
        iv_music_switch.setOnClickListener(this);
        tv_guide_skip = (TextView) findViewById(R.id.tv_guide_skip);
        tv_guide_skip.setOnClickListener(this);

        mViewPager = (ViewPager) findViewById(R.id.mViewPager);

        //三个页面布局文件传给view
        view1 = View.inflate(this, R.layout.layout_pager_guide_1, null);
        view2 = View.inflate(this, R.layout.layout_pager_guide_2, null);
        view3 = View.inflate(this, R.layout.layout_pager_guide_3, null);

        bu_exp = (Button) view3.findViewById(R.id.bu_exp);
        bu_exp.setOnClickListener(this);

        //用一个list管理三个引导页面
        mPageList.add(view1);
        mPageList.add(view2);
        mPageList.add(view3);

        //预加载
        mViewPager.setOffscreenPageLimit(mPageList.size());

        mPagerAdapter = new BasePagerAdapter(mPageList);
        mViewPager.setAdapter(mPagerAdapter);

        startMusic();


    }

    /**
     * 播放背景音乐
     */
    private void startMusic() {
        mediaPlayerHelper = new MediaPlayerHelper();
        mediaPlayerHelper.setLooping(true);
        AssetFileDescriptor fileDescriptor = getResources().openRawResourceFd(R.raw.purple);
        mediaPlayerHelper.startPlay(fileDescriptor);

        mediaPlayerHelper.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayerHelper.startPlay(fileDescriptor);
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayerHelper.stopPlay();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_music_switch:
                if(mediaPlayerHelper.MEDIA_STATUS == MediaPlayerHelper.MEDIA_STATUS_PLAY){
                    mediaPlayerHelper.pausePlay();
                    iv_music_switch.setImageResource(R.drawable.img_guide_music_off);
                }
                else if(mediaPlayerHelper.MEDIA_STATUS == MediaPlayerHelper.MEDIA_STATUS_PAUSE){
                    mediaPlayerHelper.continuePlay();
                    iv_music_switch.setImageResource(R.drawable.img_guide_music);
                }
                break;
            case R.id.tv_guide_skip:
            case R.id.bu_exp:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }

    }
}
