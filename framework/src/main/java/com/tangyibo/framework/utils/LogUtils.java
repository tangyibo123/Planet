package com.tangyibo.framework.utils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.tangyibo.framework.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * FileName: LogUtils
 * Founder: TangYibo
 * Create Date: 2021/11
 * Profile: Log print + file
 */

public class LogUtils {

    private static SimpleDateFormat mSimpleDateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //LogUtils.i("xxx");
    public static void v(String text){
        if(BuildConfig.LOG_DEBUG){
            if(!TextUtils.isEmpty(text)){
                Log.v(BuildConfig.LOG_TAG, text);
            }
        }
    }

    public static void d(String text){
        if(BuildConfig.LOG_DEBUG){
            if(!TextUtils.isEmpty(text)){
                Log.d(BuildConfig.LOG_TAG, text);
            }
        }
    }

    public static void i(String text){
        if(BuildConfig.LOG_DEBUG){
            if(!TextUtils.isEmpty(text)){
                Log.i(BuildConfig.LOG_TAG, text);
            }
        }
    }

    public static void w(String text){
        if(BuildConfig.LOG_DEBUG){
            if(!TextUtils.isEmpty(text)){
                Log.w(BuildConfig.LOG_TAG, text);
            }
        }
    }

    public static void e(String text){
        if(BuildConfig.LOG_DEBUG){
            if(!TextUtils.isEmpty(text)){
                Log.e(BuildConfig.LOG_TAG, text);
            }
        }
    }

    public static void writeToFile(String text) {

        //文件路径
        String fileRoot = Environment.getExternalStorageDirectory().getPath() + "/Planet/";
        String fileName = "Planet.log";
        //时间+内容
        String log = mSimpleDateFormat.format(new Date() + " " + text + "\n");
        //实例化一个File对象，里面存放父路径
        File fileGroup = new File(fileRoot);
        //如果父路径不存在，新建目录
        if(!fileGroup.exists()){
            fileGroup.mkdirs();
        }
        //实例化一个File对象，里面存放Planet.log路径
        File fileChild = new File(fileRoot+fileName);
        //如果log文件不存在，新建文件
        if(!fileChild.exists()){
            try {
                fileChild.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(fileRoot+fileName, true); //是否支持续写
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //为了中文部乱码，再包装一层
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, Charset.forName("gbk"));
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
        try {
            bufferedWriter.write(log);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
                bufferedWriter.close();
        } catch (IOException e) {
                e.printStackTrace();
        }
    }
}
