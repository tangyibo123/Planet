package com.tangyibo.framework.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * 旋转动画工具类
 * 传入某个view，旋转起来
 */

public class AnimUtils {

    public static ObjectAnimator rotation(View view){
        ObjectAnimator mAnim = ObjectAnimator.ofFloat(view, "rotation", 0f,360f);
        mAnim.setDuration(2 * 1000);
        mAnim.setRepeatMode(ValueAnimator.RESTART);
        mAnim.setRepeatCount(ValueAnimator.INFINITE);
        mAnim.setInterpolator(new LinearInterpolator());
        return mAnim;
    }

}
