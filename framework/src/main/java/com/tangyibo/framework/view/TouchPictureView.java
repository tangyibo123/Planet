package com.tangyibo.framework.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.tangyibo.framework.R;

public class TouchPictureView extends View {

    //背景块
    private Bitmap mBitmap_bg;
    //背景画笔
    private Paint mPaint_bg;
    //空白块
    private Bitmap mBitmap_null;
    //空白画笔
    private Paint mPaint_null;
    //移动块
    private Bitmap mBitmap_move;
    //移动画笔
    private Paint mPaint_move;

    //draw_bg的大小
    private int mWidth;
    private int mHeight;

    //方块大小
    private int CARD_SIZE = 200;
    //方块坐标
    private int LINE_W = 0;
    private int LINE_H = 0;

    //移动方块横坐标
    private int moveX = 0;
    //误差值
    private int errorValues = 10;

    private OnViewResultListener viewResultListener;

    private void init(){
        mPaint_bg = new Paint();
        mPaint_null = new Paint();
        mPaint_move = new Paint();
    }

    public interface OnViewResultListener{
        void onResult();
    }

    public TouchPictureView(Context context) {
        super(context);
        init();
    }

    public TouchPictureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchPictureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TouchPictureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setViewResultListener(OnViewResultListener viewResultListener){
        this.viewResultListener = viewResultListener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBg(canvas);
        drawNull(canvas);
        drawMove(canvas);
    }

    /**
     * 画背景块
     * @param canvas //画布
     */
    private void drawBg(Canvas canvas) {
        //1.创建一个最大的空白图作为背景，然后贴到View的幕布中
        //mBitmap_bg = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        //canvas.drawBitmap(mBitmap_bg, null, new Rect(0, 0, mWidth, mHeight), mPaint_bg);
        //2.获取验证图片
        //Bitmap code_pic = BitmapFactory.decodeResource(getResources(), R.drawable.code_pic);
        //1.获取图片
        Bitmap code_pic = BitmapFactory.decodeResource(getResources(), R.drawable.code_pic);
        //2.创建一个和view大小一致的空bitmap
        mBitmap_bg = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        //3.将图片绘制到空的Bitmap
        Canvas canvas_view = new Canvas(mBitmap_bg);
        canvas_view.drawBitmap(mBitmap_bg, null, new Rect(0, 0, mWidth, mHeight), mPaint_bg);
        //4.将bgBitmap绘制到View上
        canvas.drawBitmap(code_pic, null, new Rect(0, 0, mWidth, mHeight), mPaint_bg);

    }

    /**
     * 画空块
     * @param canvas
     */
    @SuppressLint("ResourceAsColor")
    private void drawNull(Canvas canvas) {
        //1.获取图片
        mBitmap_null = BitmapFactory.decodeResource(getResources(), R.drawable.img_null_card);
        //mBitmap_null = Bitmap.createBitmap(CARD_SIZE, CARD_SIZE, Bitmap.Config.ARGB_8888);
        //2.计算值
        CARD_SIZE = mBitmap_null.getWidth();

        //99 / 3 = 33 * 2 = 66
        LINE_W = mWidth / 3 * 2;
        //除以2并不是中心
        LINE_H = mHeight / 2 - (CARD_SIZE / 2);

        //3.绘制
        canvas.drawBitmap(mBitmap_null, LINE_W, LINE_H, mPaint_null);
    }

    /**
     * 画滑动块
     * @param canvas
     */
    private void drawMove(Canvas canvas) {
        //2.创建一个和view大小一致的空bitmap
        //moveX = LINE_W = mWidth / 3;
        mBitmap_move = Bitmap.createBitmap(mBitmap_bg, LINE_W, LINE_H, CARD_SIZE, CARD_SIZE);
        canvas.drawBitmap(mBitmap_move, moveX, LINE_H, mPaint_move);
    }



}
